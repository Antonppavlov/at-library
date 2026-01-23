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
import io.qameta.allure.Step;
import lombok.experimental.Delegate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.at.library.core.cucumber.api.CoreEnvironment;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.AssertionHelper;

/**
 * Начальная настройка ядра: создание CoreEnvironment и AssertionHelper,
 * а также установка базового URL для тестов (Configuration.baseUrl).
 *
 * Веб-драйвер и RestAssured настраиваются в специализированных модулях
 * (at-library-web, at-library-api) через отдельные хуки.
 */
public class CoreInitialSetup {

    private static final Logger log = LogManager.getLogger(CoreInitialSetup.class);

    public volatile static int scenarioNumber = 1;

    @Delegate
    CoreScenario coreScenario = CoreScenario.getInstance();

/**
     * Действия выполняемые перед каждым сценарием:
     * - логирование старта сценария;
     * - создание CoreEnvironment и AssertionHelper.
     *
     * Установка baseUrl для Selenide выполняется в веб-модуле.
     */
    @Before(order = 1)
    @Step("Инициализация CoreEnvironment")
    public void initializingCoreEnvironment(Scenario scenario) throws Exception {
        int testNumber = scenarioNumber++;
        log.info(String.format("%s: Старт сценария %d с именем [%s]", getScenarioId(scenario), testNumber, scenario.getName()));

        coreScenario.setEnvironment(new CoreEnvironment(scenario));
        coreScenario.setAssertionHelper(new AssertionHelper());
    }

    /**
     * Действия выполняемые после каждого сценария:
     * - логирование завершения сценария.
     * Закрытие браузера/драйвера выполняется в модуле web.
     */
    @After(order = 1)
    @Step("Завершение сценария")
    public void afterScenario(Scenario scenario) {
        log.info(String.format("%s: Завершение сценария с именем [%s]", getScenarioId(scenario), scenario.getName()));
    }

    /**
     * Возвращает сокращенный ID сценария
     *
     * @return      ID сценария в формате feature_file.feature:ID
     */
    public static String getScenarioId(Scenario scenario) {
        String fullID = scenario.getId();
        return fullID.substring(fullID.lastIndexOf('/') + 1).replace(':', '_');
    }
}
