package ru.at.library.api.steps.response;

import io.restassured.response.Response;
import ru.at.library.core.cucumber.api.CoreScenario;

/**
 * Утилитный класс для получения {@link Response} из хранилища переменных {@link CoreScenario}.
 */
public final class ResponseHelper {


    /**
     * Возвращает {@link Response} из хранилища переменных по имени.
     * Бросает осмысленные исключения, если имя пустое или в переменной лежит не {@link Response}.
     *
     * @param responseNameVariable имя переменной, в которой ожидается {@link Response}
     * @return объект {@link Response}
     */
    public static Response getResponse(String responseNameVariable) {
        if (responseNameVariable == null || responseNameVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя переменной с Response не может быть null или пустым");
        }

        Object value = CoreScenario.getInstance().getVar(responseNameVariable);
        if (!(value instanceof Response)) {
            throw new IllegalStateException("Переменная '" + responseNameVariable + "' не содержит объект Response");
        }

        return (Response) value;
    }
}
