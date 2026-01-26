/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.at.library.core.setup;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.experimental.Delegate;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreEnvironment;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.AssertionHelper;
import ru.at.library.core.utils.log.ScenarioLogAppender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Начальная настройка ядра: создание CoreEnvironment и AssertionHelper,
 * а также установка базового URL для тестов (Configuration.baseUrl).
 * <p>
 * Веб-драйвер и RestAssured настраиваются в специализированных модулях
 * (at-library-web, at-library-api) через отдельные хуки.
 */
@Log4j2
public class CoreInitialSetup {

    static {
        // Инициализируем дополнительный логгер для сбора логов сценария
        ScenarioLogAppender.installIfNeeded();
    }

    /**
     * Счётчик запущенных сценариев в рамках одного запуска.
     * <p>Используется только для логирования ("текущий номер / общее количество").</p>
     */
    public static final AtomicInteger scenarioNumber = new AtomicInteger(1);

    /**
     * Общее количество сценариев, которые будут выполнены в данном запуске.
     * <p>Заполняется в тестовом раннере (RunFeaturesTest) при формировании {@code DataProvider}.</p>
     */
    public static volatile int totalScenarios = 0;

/**
     * Информация о запущенных сценариях: время старта, человеко-читаемое имя и порядковый номер запуска.
     */
    private static final class ScenarioRunInfo {
        final String name;
        final long startTimeMs;
        /**
         * Порядковый номер сценария в рамках запуска (1..N), присваивается в {@link #initializingCoreEnvironment}.
         */
        final int sequenceNumber;

        private ScenarioRunInfo(String name, long startTimeMs, int sequenceNumber) {
            this.name = name;
            this.startTimeMs = startTimeMs;
            this.sequenceNumber = sequenceNumber;
        }
    }

    /**
     * Все текущие выполняющиеся сценарии: id -> информация о запуске.
     */
    private static final ConcurrentHashMap<String, ScenarioRunInfo> runningScenarios = new ConcurrentHashMap<>();

    /**
     * Флаг, что watchdog-поток уже запущен.
     */
    private static final AtomicInteger watchdogStarted = new AtomicInteger(0);

    /**
     * Порог для вывода предупреждения о "подвисшем" сценарии (10 минут).
     */
    private static final long WATCHDOG_THRESHOLD_MS = 10 * 60 * 1000L;

    /**
     * Интервал проверки сценариев watchdog'ом (1 минута).
     */
    private static final long WATCHDOG_INTERVAL_MS = 60 * 1000L;

    private static void ensureWatchdogStarted() {
        if (watchdogStarted.compareAndSet(0, 1)) {
            log.info("\n++++++++++++\nЗапуск мониторинга сценариев (watchdog)\n++++++++++++");

            Thread t = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        long now = System.currentTimeMillis();
                        for (Map.Entry<String, ScenarioRunInfo> entry : runningScenarios.entrySet()) {
                            String id = entry.getKey();
                            ScenarioRunInfo info = entry.getValue();
                            long durationMs = now - info.startTimeMs;
                            if (durationMs >= WATCHDOG_THRESHOLD_MS) {
                                long secondsTotal = durationMs / 1000;
                                long minutes = secondsTotal / 60;
                                long seconds = secondsTotal % 60;
                                log.warn(String.format(
                                        "\n++++++++++++\n[WATCHDOG] Долгий сценарий\nИмя: [%s]\nid: %s\nДлительность: %d мин %d с\n++++++++++++",
                                        info.name, id, minutes, seconds
                                ));
                            }
                        }
                        Thread.sleep(WATCHDOG_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable t1) {
                        log.error("Ошибка в watchdog сценариев", t1);
                    }
                }
            }, "scenario-watchdog");
            t.setDaemon(true);
            t.start();
        }
    }

    @Delegate
    CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Действия выполняемые перед каждым сценарием:
     * - логирование старта сценария;
     * - создание CoreEnvironment и AssertionHelper.
     * <p>
     * Установка baseUrl для Selenide выполняется в веб-модуле.
     */
    @Before(order = 1)
    @Step("Инициализация CoreEnvironment")
    public void initializingCoreEnvironment(Scenario scenario) throws Exception {
        ensureWatchdogStarted();

        // Запускаем накопление логов для текущего сценария
        ScenarioLogAppender.startScenarioLogging();

        int testNumber = scenarioNumber.getAndIncrement();
        int total = totalScenarios;
        String scenarioId = getScenarioId(scenario);

        // запоминаем время старта, имя и порядковый номер запуска для последующего расчёта длительности и мониторинга
        runningScenarios.put(scenarioId, new ScenarioRunInfo(scenario.getName(), System.currentTimeMillis(), testNumber));

        log.info(String.format(
                "\n++++++++++++\n" +
                "Запущен сценарий: %d/%d\n" +
                "Имя: [%s]\n" +
                "id: %s\n" +
                "++++++++++++",
                testNumber, total, scenario.getName(), scenarioId
        ));

        coreScenario.setEnvironment(new CoreEnvironment(scenario));
        coreScenario.setAssertionHelper(new AssertionHelper());
    }

    /**
     * Действия выполняемые после каждого сценария:
     * - логирование завершения сценария.
     * Закрытие браузера/драйвера выполняется в модуле web.
     */
    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        int total = totalScenarios;
        String scenarioId = getScenarioId(scenario);

        ScenarioRunInfo info = runningScenarios.remove(scenarioId);
        int sequenceNumber = info != null ? info.sequenceNumber : -1;
        long durationMs = info != null ? (System.currentTimeMillis() - info.startTimeMs) : -1L;

        String durationInfo;
        if (durationMs >= 0) {
            long secondsTotal = durationMs / 1000;
            long minutes = secondsTotal / 60;
            long seconds = secondsTotal % 60;
            durationInfo = String.format("%d мин %d с", minutes, seconds);
        } else {
            durationInfo = "н/д";
        }

        int runningNow = runningScenarios.size();

        if (total > 0 && sequenceNumber > 0) {
            log.info(String.format(
                    "\n++++++++++++\nЗавершён сценарий: %d/%d\nИмя: [%s]\nid: %s\nДлительность: %s\nСейчас выполняется сценариев: %d\n++++++++++++",
                    sequenceNumber, total, scenario.getName(), scenarioId, durationInfo, runningNow
            ));
        } else {
            log.info(String.format(
                    "\n++++++++++++\nЗавершён сценарий\nИмя: [%s]\nid: %s\nДлительность: %s\nСейчас выполняется сценариев: %d\n++++++++++++",
                    scenario.getName(), scenarioId, durationInfo, runningNow
            ));
        }

        // Прикладываем к Allure накопленный лог текущего сценария
        String scenarioLog = ScenarioLogAppender.getAndClearScenarioLog();
        if (scenarioLog != null && !scenarioLog.isEmpty()) {
            Allure.addAttachment("Лог сценария: " + scenario.getName(), "text/plain", scenarioLog);
        }

    }

    /**
     * Возвращает сокращенный ID сценария
     *
     * @return ID сценария в формате feature_file.feature:ID
     */
    public static String getScenarioId(Scenario scenario) {
        String fullID = scenario.getId();
        return fullID.substring(fullID.lastIndexOf('/') + 1).replace(':', '_');
    }
}
