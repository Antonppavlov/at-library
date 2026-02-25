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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * Счётчик запусков каждого сценария: stableKey -> количество запусков.
     * Ключ — {@code uri:line} (одинаков при retry, в отличие от {@code scenario.getId()}).
     * Если значение > 1, значит сценарий перезапускался (retry).
     */
    private static final ConcurrentHashMap<String, AtomicInteger> scenarioRunCounts = new ConcurrentHashMap<>();

    /**
     * Оригинальный порядковый номер сценария (присвоенный при первом запуске): stableKey -> номер.
     * При retry повторно используется тот же номер, чтобы не превышать totalScenarios.
     */
    private static final ConcurrentHashMap<String, Integer> scenarioOriginalNumbers = new ConcurrentHashMap<>();

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

        int total = totalScenarios;
        String scenarioId = getScenarioId(scenario);
        String stableKey = getStableScenarioKey(scenario);

        // Считаем, какой это запуск данного сценария (1 = первый, 2 = первый retry и т.д.)
        int runNumber = scenarioRunCounts
                .computeIfAbsent(stableKey, k -> new AtomicInteger(0))
                .incrementAndGet();

        // При первом запуске присваиваем новый номер, при retry — используем оригинальный
        int testNumber;
        if (runNumber == 1) {
            testNumber = scenarioNumber.getAndIncrement();
            scenarioOriginalNumbers.put(stableKey, testNumber);
        } else {
            testNumber = scenarioOriginalNumbers.getOrDefault(stableKey, -1);
        }

        // запоминаем время старта, имя и порядковый номер запуска для последующего расчёта длительности и мониторинга
        runningScenarios.put(scenarioId, new ScenarioRunInfo(scenario.getName(), System.currentTimeMillis(), testNumber));

        String retryInfo = runNumber > 1
                ? String.format("\n🔄 ПЕРЕЗАПУСК #%d", runNumber - 1)
                : "";

        log.info(String.format(
                "\n++++++++++++\n" +
                "Запущен сценарий: %d/%d\n" +
                "Имя: [%s]\n" +
                "id: %s" +
                "%s\n" +
                "++++++++++++",
                testNumber, total, scenario.getName(), scenarioId, retryInfo
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
        
        // Определяем статус теста
        String status = scenario.getStatus().toString();
        String statusEmoji = scenario.isFailed() ? "❌" : "✅";
        String statusRu = scenario.isFailed() ? "ПРОВАЛЕН" : "УСПЕШНО";

        // Информация о retry
        String stableKey = getStableScenarioKey(scenario);
        AtomicInteger counter = scenarioRunCounts.get(stableKey);
        int runNumber = counter != null ? counter.get() : 1;
        String retryInfo = runNumber > 1
                ? String.format("\n🔄 ПЕРЕЗАПУСК #%d", runNumber - 1)
                : "";

        if (total > 0 && sequenceNumber > 0) {
            log.info(String.format(
                    "\n++++++++++++\nЗавершён сценарий: %d/%d\nИмя: [%s]\nid: %s\nСтатус: %s %s (%s)\nДлительность: %s%s\nСейчас выполняется сценариев: %d\n++++++++++++",
                    sequenceNumber, total, scenario.getName(), scenarioId, statusEmoji, statusRu, status, durationInfo, retryInfo, runningNow
            ));
        } else {
            log.info(String.format(
                    "\n++++++++++++\nЗавершён сценарий\nИмя: [%s]\nid: %s\nСтатус: %s %s (%s)\nДлительность: %s%s\nСейчас выполняется сценариев: %d\n++++++++++++",
                    scenario.getName(), scenarioId, statusEmoji, statusRu, status, durationInfo, retryInfo, runningNow
            ));
        }

        // Получаем накопленный лог сценария
        String scenarioLog = ScenarioLogAppender.getAndClearScenarioLog();

        if (scenarioLog != null && !scenarioLog.isEmpty()) {
            writeScenarioLogToFile(scenario, sequenceNumber, scenarioLog);
            Allure.addAttachment("Лог сценария: " + scenario.getName(), "text/plain", scenarioLog);
        }

    }

    /**
     * Запись лога конкретного сценария в отдельный файл.
     * Формат имени: logs/scenarios/<scenarioName>.log, где scenarioName – человеко-читаемое имя сценария
     * (с сохранением кириллицы, очищенное только от символов, недопустимых в именах файлов).
     */
    private void writeScenarioLogToFile(Scenario scenario, int sequenceNumber, String scenarioLog) {
        try {
            String rawName = scenario.getName();
            // Очищаем имя сценария для использования в имени файла
            // Удаляем только символы, недопустимые в именах файлов: / \ : * ? " < > |
            String safeScenarioName = rawName.replaceAll("[/\\\\:*?\"<>|]", "_");

            Path dir = Paths.get("logs", "scenarios");
            Files.createDirectories(dir);

            Path logFile = dir.resolve(safeScenarioName + ".log");
            // UTF-8 BOM (EF BB BF) — чтобы браузер/Jenkins корректно определял кодировку
            byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] content = scenarioLog.getBytes(StandardCharsets.UTF_8);
            byte[] result = new byte[bom.length + content.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(content, 0, result, bom.length, content.length);
            Files.write(logFile, result);

            log.debug("Лог сценария записан в файл: {}", logFile.toAbsolutePath());
        } catch (Exception e) {
            // Не должен ломать тест, если файл по какой-то причине не записался
            log.error("Не удалось записать лог сценария в файл", e);
        }
    }

    /**
     * Возвращает сокращенный ID сценария (уникальный для каждого запуска, меняется при retry).
     *
     * @return ID сценария в формате feature_file.feature:ID
     */
    public static String getScenarioId(Scenario scenario) {
        String fullID = scenario.getId();
        return fullID.substring(fullID.lastIndexOf('/') + 1).replace(':', '_');
    }

    /**
     * Возвращает стабильный ключ сценария (одинаковый при retry).
     * Основан на URI feature-файла и номере строки сценария.
     */
    private static String getStableScenarioKey(Scenario scenario) {
        String uri = scenario.getUri().toString();
        String fileName = uri.substring(uri.lastIndexOf('/') + 1);
        return fileName + ":" + scenario.getLine();
    }
}
