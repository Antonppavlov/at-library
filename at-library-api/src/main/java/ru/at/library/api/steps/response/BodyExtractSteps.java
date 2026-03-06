package ru.at.library.api.steps.response;

import io.cucumber.java.ru.И;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;

/**
 * Шаг извлечения тела HTTP-ответа и сохранения в переменную.
 */
@Log4j2
public class BodyExtractSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Сохраняет тело HTTP-ответа как строку в переменную сценария.
     * Пример:
     *  И body ответа "pets_response" сохранено в переменную "pets_body"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param targetVar   имя переменной для сохранения body
     */
    @И("^body ответа \"([^\"]+)\" сохранено в переменную \"([^\"]+)\"$")
    public void saveResponseBody(String responseVar, String targetVar) {
        String body = ResponseHelper.getResponse(responseVar).getBody().asString();
        coreScenario.setVar(targetVar, body);
        log.trace("body ответа '{}' сохранено в переменную '{}'", responseVar, targetVar);
    }
}
