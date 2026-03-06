package ru.at.library.api.steps.response;

import io.cucumber.java.ru.И;
import io.restassured.response.Response;

/**
 * Шаги проверки HTTP статус-кодов.
 */
public class StatusCodeCheckSteps {

    /**
     * Проверка HTTP статус-кода ответа.
     * Пример:
     *  И в ответе "response" statusCode: 200
     *
     * @param responseVar    имя переменной, содержащей {@link Response}
     * @param expectedStatus ожидаемый HTTP статус-код
     */
    @И("^в ответе \"([^\"]+)\" statusCode: (\\d+)$")
    public void checkResponseStatusCode(String responseVar, int expectedStatus) {
        Response response = ResponseHelper.getResponse(responseVar);
        response.then().statusCode(expectedStatus);
    }
}
