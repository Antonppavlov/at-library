package ru.at.library.api.setup;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.utils.helpers.PropertyLoader;

/**
 * Начальная настройка для API-сценариев: установка baseURI RestAssured.
 * Рекомендуется помечать API-фичи тегом @api, чтобы не выполнять этот хук для не-API сценариев.
 */
@Log4j2
public class ApiInitialSetup {

    @Before(order = 2, value = "@api")
    @Step("Настройка RestAssured baseURI для API-сценария")
    public void configureRestAssuredBaseUri(Scenario scenario) {
        String baseUri = System.getProperty("baseURI", PropertyLoader.tryLoadProperty("baseURI"));
        if (baseUri == null || baseUri.trim().isEmpty()) {
            log.warn("Не задан baseURI для RestAssured (system property 'baseURI' или property 'baseURI'). Будут использоваться абсолютные URL в шагах.");
            return;
        }
        RestAssured.baseURI = baseUri;
        log.info("[API] baseURI={} для сценария '{}': {}", baseUri, scenario.getId(), scenario.getName());
    }
}