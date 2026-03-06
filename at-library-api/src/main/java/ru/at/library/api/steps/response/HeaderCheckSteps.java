package ru.at.library.api.steps.response;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;

import java.util.List;

/**
 * Шаги проверки и извлечения HTTP-заголовков ответа.
 */
@Log4j2
public class HeaderCheckSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Проверка значений заголовков в HTTP-ответе.
     * Пример:
     *  И в ответе "response" headers равны значениям из таблицы:
     *    | Content-Type | application/json |
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param dataTable   таблица с парами: имя заголовка и ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" headers равны значениям из таблицы:$")
    public void checkHeaders(String responseVar, DataTable dataTable) {
        Response response = ResponseHelper.getResponse(responseVar);
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String headerName = row.get(0);
            String expectedValue = row.get(1);
            String actualValue = response.header(headerName);

            if (actualValue == null) {
                errors.append(String.format("Header '%s' отсутствует в ответе%n", headerName));
            } else if (!actualValue.equals(expectedValue)) {
                errors.append(String.format("Header '%s': ожидалось '%s', получено '%s'%n", headerName, expectedValue, actualValue));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    /**
     * Сохранение значений заголовков из HTTP-ответа в переменные.
     * Пример:
     *  И headers ответа "response" сохранены в переменные из таблицы:
     *    | Content-Type | content_type_var |
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param dataTable   таблица с парами: имя заголовка и имя переменной для сохранения
     */
    @И("^headers ответа \"([^\"]+)\" сохранены в переменные из таблицы:$")
    public void saveHeaders(String responseVar, DataTable dataTable) {
        Response response = ResponseHelper.getResponse(responseVar);

        for (List<String> row : dataTable.asLists()) {
            String headerName = row.get(0);
            String targetVar = row.get(1);
            String value = response.header(headerName);

            if (value == null) {
                throw new RuntimeException(String.format("Header '%s' отсутствует в ответе", headerName));
            }

            coreScenario.setVar(targetVar, value);
            log.trace("header '{}' = '{}' сохранено в переменную '{}'", headerName, value, targetVar);
        }
    }
}
