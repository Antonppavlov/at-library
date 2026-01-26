package ru.at.library.web.setup;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import ru.at.library.web.scenario.WebScenario;

/**
 * Web-специфичная начальная настройка.
 */
@Log4j2
public class WebInitialSetup {

    @Before(order = 2)
    @Step("Сканирование всех классов с аннотацией {@link Name} и регистрация их в реестре страниц")
     public void initPages(Scenario scenario) throws Exception {
        WebScenario.initPages();
    }

}