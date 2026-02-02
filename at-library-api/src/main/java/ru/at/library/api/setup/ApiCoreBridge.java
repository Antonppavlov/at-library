package ru.at.library.api.setup;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.setup.CoreInitialSetup;

/**
 * Bridge-хуки для API-модуля, делегирующие работу core-инициализации
 * (watchdog, CoreEnvironment, ScenarioLogAppender и пр.)
 * в {@link CoreInitialSetup}.
 *
 * В некоторых окружениях Cucumber не подхватывает хуки из зависимостей,
 * поэтому для API-сценариев регистрируем явный glue-класс внутри модуля.
 */
@Log4j2
public class ApiCoreBridge {

    private final CoreInitialSetup coreInitialSetup = new CoreInitialSetup();

    @Before(order = 1)
    @Step("Инициализация CoreEnvironment (bridge для API)")
    public void beforeScenario(Scenario scenario) throws Exception {
        coreInitialSetup.initializingCoreEnvironment(scenario);
    }

    @After(order = 1)
    public void afterScenario(Scenario scenario) {
        coreInitialSetup.afterScenario(scenario);
    }
}
