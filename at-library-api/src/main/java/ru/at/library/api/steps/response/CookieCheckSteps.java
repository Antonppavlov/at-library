package ru.at.library.api.steps.response;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;

import java.util.List;

/**
 * Шаги проверки и извлечения cookies из HTTP-ответа.
 */
@Log4j2
public class CookieCheckSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Проверка значений cookies в HTTP-ответе.
     * Пример:
     *  И в ответе "response" cookies равны значениям из таблицы:
     *    | session_id | abc123 |
     *    | theme      | dark   |
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param dataTable   таблица с парами: имя cookie и ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" cookies равны значениям из таблицы:$")
    public void checkCookies(String responseVar, DataTable dataTable) {
        Response response = ResponseHelper.getResponse(responseVar);
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String cookieName = row.get(0);
            String expectedValue = row.get(1);
            String actualValue = response.cookie(cookieName);

            if (actualValue == null) {
                errors.append(String.format("Cookie '%s' отсутствует в ответе%n", cookieName));
            } else if (!actualValue.equals(expectedValue)) {
                errors.append(String.format("Cookie '%s': ожидалось '%s', получено '%s'%n", cookieName, expectedValue, actualValue));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    /**
     * Сохранение значений cookies из HTTP-ответа в переменные.
     * Пример:
     *  И cookies ответа "response" сохранены в переменные из таблицы:
     *    | session_id | my_session |
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param dataTable   таблица с парами: имя cookie и имя переменной для сохранения
     */
    @И("^cookies ответа \"([^\"]+)\" сохранены в переменные из таблицы:$")
    public void saveCookies(String responseVar, DataTable dataTable) {
        Response response = ResponseHelper.getResponse(responseVar);

        for (List<String> row : dataTable.asLists()) {
            String cookieName = row.get(0);
            String targetVar = row.get(1);
            String value = response.cookie(cookieName);

            if (value == null) {
                throw new RuntimeException(String.format("Cookie '%s' отсутствует в ответе", cookieName));
            }

            coreScenario.setVar(targetVar, value);
            log.trace("cookie '{}' = '{}' сохранено в переменную '{}'", cookieName, value, targetVar);
        }
    }
}
