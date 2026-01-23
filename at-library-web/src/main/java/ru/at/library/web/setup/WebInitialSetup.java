package ru.at.library.web.setup;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.at.library.web.scenario.WebScenario;

/**
 * Web-специфичная начальная настройка.
 */
public class WebInitialSetup {

    private static final Logger log = LogManager.getLogger(WebInitialSetup.class);

    @Before(order = 2)
    @Step("Сканирование всех классов с аннотацией {@link Name} и регистрация их в реестре страниц")
     public void initPages(Scenario scenario) throws Exception {
        WebScenario.initPages();
    }

}