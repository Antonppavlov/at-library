package ru.at.library.api.steps.response;

import com.google.common.collect.Ordering;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import ru.at.library.api.helpers.Utils;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Шаги для работы с JSON: проверка значений, сохранение, шаблоны, сравнение, схемы и массивы.
 */
@Log4j2
public class JsonResponseSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    // =======================================================================
    // ПРОВЕРКА ЗНАЧЕНИЙ В JSON ОТВЕТА
    // =======================================================================

    /**
     * Проверка одного значения в json ответа по jsonPath.
     * <p>Сравнение выполняется как строковое, даже если значение — число или boolean.</p>
     * Пример:
     * И в ответе "response" содержимое найденное по jsonPath "[0].status" равно "available"
     *
     * @param responseVar   имя переменной, содержащей {@link Response}
     * @param jsonPath      jsonPath-выражение
     * @param expectedValue ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" содержимое найденное по jsonPath \"([^\"]+)\" равно \"([^\"]+)\"$")
    public void checkJsonResponseValue(String responseVar, String jsonPath, String expectedValue) {
        expectedValue = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(expectedValue);
        String actualValue = ResponseHelper.getResponse(responseVar).jsonPath().getString(jsonPath);
        if (actualValue == null) {
            throw new AssertionError(String.format("По jsonPath '%s' значение не найдено", jsonPath));
        }
        assertThat("jsonPath '" + jsonPath + "'", actualValue, equalTo(expectedValue));
    }

    /**
     * Сохранение одного значения из json ответа по jsonPath в переменную.
     * Пример:
     * И из ответа "response" содержимое найденное по jsonPath "[0].status" сохранено в "pet_status"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     * @param targetVar   имя переменной для сохранения
     */
    @И("^из ответа \"([^\"]+)\" содержимое найденное по jsonPath \"([^\"]+)\" сохранено в \"([^\"]+)\"$")
    public void saveJsonResponseValue(String responseVar, String jsonPath, String targetVar) {
        String value = ResponseHelper.getResponse(responseVar).jsonPath().getString(jsonPath);
        if (value == null) {
            throw new RuntimeException(String.format("По jsonPath '%s' значение не найдено в ответе", jsonPath));
        }
        coreScenario.setVar(targetVar, value);
        log.trace("jsonPath '{}' = '{}' сохранено в '{}'", jsonPath, value, targetVar);
    }

    /**
     * Проверка значений в json ответа по jsonPath (простое равенство).
     * Пример:
     * И в ответе "response" содержимые найденные по jsonPath равны:
     * | [0].status | available |
     * И в ответе "response" содержимые найденные по jsonPath без учета регистра равны:
     * | [0].status | AVAILABLE |
     *
     * @param responseVar              имя переменной, содержащей {@link Response}
     * @param caseInsensitiveIndicator пусто или "без учета регистра "
     * @param dataTable                таблица: jsonPath, ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" содержимые найденные по jsonPath (|без учета регистра )равны:$")
    public void checkJsonResponseValues(String responseVar, String caseInsensitiveIndicator, DataTable dataTable) {
        Response response = ResponseHelper.getResponse(responseVar);
        boolean caseInsensitive = !caseInsensitiveIndicator.isEmpty();
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String expectedValue = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(row.get(1));
            String actualValue = response.jsonPath().getString(path);

            if (actualValue == null) {
                errors.append(String.format("По jsonPath '%s' значение не найдено%n", path));
                continue;
            }

            if (caseInsensitive) {
                expectedValue = expectedValue.toLowerCase();
                actualValue = actualValue.toLowerCase();
            }

            if (!actualValue.equals(expectedValue)) {
                errors.append(String.format("jsonPath '%s': ожидалось '%s', получено '%s'%n", path, expectedValue, actualValue));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    // =======================================================================
    // СОХРАНЕНИЕ ЗНАЧЕНИЙ ИЗ JSON
    // =======================================================================

    /**
     * Сохранение значений из json ответа по jsonPath.
     * Пример:
     * И из ответа "response" содержимые найденные по jsonPath сохранены в переменные:
     * | [0].status | first_pet_status |
     * | [0].id     | first_pet_id     |
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param dataTable   таблица: jsonPath, имя переменной
     */
    @И("^из ответа \"([^\"]+)\" содержимые найденные по jsonPath сохранены в переменные:$")
    public void saveFromJsonResponse(String responseVar, DataTable dataTable) {
        Response response = ResponseHelper.getResponse(responseVar);

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String targetVar = row.get(1);
            String value = response.jsonPath().getString(path);

            if (value == null) {
                throw new RuntimeException(String.format("По jsonPath '%s' значение не найдено в ответе", path));
            }

            coreScenario.setVar(targetVar, value);
            log.trace("jsonPath '{}' = '{}' сохранено в '{}'", path, value, targetVar);
        }
    }

    // =======================================================================
    // СРАВНЕНИЕ
    // =======================================================================

    /**
     * Сравнение значений по jsonPath между двумя ответами.
     * Пример:
     * И в ответах "response1" и "response2" содержимые найденные по jsonPath совпадают:
     * | [0].status |
     * | [0].name   |
     *
     * @param responseVar1 имя первого ответа
     * @param responseVar2 имя второго ответа
     * @param dataTable    таблица с jsonPath-ключами
     */
    @И("^в ответах \"([^\"]+)\" и \"([^\"]+)\" содержимые найденные по jsonPath совпадают:$")
    public void compareJsonResponses(String responseVar1, String responseVar2, DataTable dataTable) {
        Response response1 = ResponseHelper.getResponse(responseVar1);
        Response response2 = ResponseHelper.getResponse(responseVar2);
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String value1 = response1.jsonPath().getString(path);
            String value2 = response2.jsonPath().getString(path);

            if (value1 == null) {
                errors.append(String.format("jsonPath '%s' не найден в ответе '%s'%n", path, responseVar1));
                continue;
            }
            if (value2 == null) {
                errors.append(String.format("jsonPath '%s' не найден в ответе '%s'%n", path, responseVar2));
                continue;
            }
            if (!value1.equals(value2)) {
                errors.append(String.format("jsonPath '%s': '%s' != '%s'%n", path, value1, value2));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    /**
     * Сравнение body ответа с эталонным JSON.
     * Пример:
     * И в ответе "response" содержимое равно json "expected.json"
     *
     * @param responseVar  имя переменной с {@link Response}
     * @param expectedJson путь к эталонному JSON (property/resource)
     */
    @И("^в ответе \"([^\"]+)\" содержимое равно json \"([^\"]+)\"$")
    public void compareJsonBody(String responseVar, String expectedJson) {
        String json = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(expectedJson);
        json = Utils.resolveJsonVars(json);
        Response response = ResponseHelper.getResponse(responseVar);
        response.then().body(equalTo(json));
    }

    // =======================================================================
    // СХЕМЫ
    // =======================================================================

    /**
     * Проверка ответа на соответствие JSON-схеме.
     * Пример:
     * И в ответе "response" содержимое соответствует json схеме "json/pet_array_schema.json"
     *
     * @param responseVar имя переменной с {@link Response}
     * @param schemaPath  путь до .json файла со схемой в classpath
     */
    @И("^в ответе \"([^\"]+)\" содержимое соответствует json схеме \"([^\"]+)\"$")
    public void validateJsonSchema(String responseVar, String schemaPath) {
        Response response = ResponseHelper.getResponse(responseVar);
        response.then().assertThat().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }

    // =======================================================================
    // МАССИВЫ
    // =======================================================================

    /**
     * Проверка что массив значений найденных по jsonPath содержит указанное значение.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "status" содержит значение "available"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     * @param value       ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" содержит значение \"([^\"]+)\"$")
    public void arrayContains(String responseVar, String jsonPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getJsonList(responseVar, jsonPath);
        if (!list.contains(value)) {
            throw new AssertionError(String.format(
                    "Массив '%s' не содержит значение '%s'. Фактические значения: %s", jsonPath, value, list));
        }
    }

    /**
     * Проверка что все элементы массива равны указанному значению.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "status" все значения равны "available"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     * @param value       ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" все значения равны \"([^\"]+)\"$")
    public void arrayAllEqual(String responseVar, String jsonPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getJsonList(responseVar, jsonPath);
        for (String actual : list) {
            if (!actual.equals(value)) {
                throw new AssertionError(String.format("Массив '%s' содержит '%s', ожидалось все '%s'", jsonPath, actual, value));
            }
        }
    }

    /**
     * Проверка что все элементы массива содержат подстроку.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "status" все значения содержат "avail"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     * @param value       ожидаемая подстрока
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" все значения содержат \"([^\"]+)\"$")
    public void arrayAllContain(String responseVar, String jsonPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getJsonList(responseVar, jsonPath);
        for (String actual : list) {
            if (!actual.contains(value)) {
                throw new AssertionError(String.format("Массив '%s' содержит '%s', которое не содержит подстроку '%s'", jsonPath, actual, value));
            }
        }
    }

    /**
     * Проверка размера массива по jsonPath.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "[0].tags" размер 0
     *
     * @param responseVar  имя переменной, содержащей {@link Response}
     * @param jsonPath     jsonPath-выражение
     * @param expectedSize ожидаемый размер массива
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" размер (\\d+)$")
    public void arraySize(String responseVar, String jsonPath, int expectedSize) {
        List<String> list = getJsonList(responseVar, jsonPath);
        assertThat("Размер массива '" + jsonPath + "'", list.size(), equalTo(expectedSize));
    }

    /**
     * Проверка размера массива по jsonPath.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "[0].tags" не пустой
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" не пустой$")
    public void arraySizeNotNull(String responseVar, String jsonPath) {
        List<String> list = getJsonList(responseVar, jsonPath);
        assertThat("Размер массива '" + jsonPath + "'", list.size(), is(notNullValue()));
    }

    /**
     * Проверка что массив отсортирован по возрастанию.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "status" отсортирован по возрастанию
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" отсортирован по возрастанию$")
    public void arraySortedAsc(String responseVar, String jsonPath) {
        List<String> list = getJsonList(responseVar, jsonPath);
        if (!Ordering.natural().nullsLast().isOrdered(list)) {
            throw new AssertionError(String.format("Массив '%s' не отсортирован по возрастанию: %s", jsonPath, list));
        }
    }

    /**
     * Проверка что массив отсортирован по убыванию.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "status" отсортирован по убыванию
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" отсортирован по убыванию$")
    public void arraySortedDesc(String responseVar, String jsonPath) {
        List<String> list = getJsonList(responseVar, jsonPath);
        if (!Ordering.natural().nullsLast().reverse().isOrdered(list)) {
            throw new AssertionError(String.format("Массив '%s' не отсортирован по убыванию: %s", jsonPath, list));
        }
    }

    /**
     * Проверка что все даты в массиве находятся в указанном периоде.
     * Пример:
     * И в ответе "response" массив значений найденных по jsonPath "dates" в периоде между "date_start" и "date_end" в формате "date_format"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param jsonPath    jsonPath-выражение
     * @param startVar    начальная дата периода (или имя переменной/property)
     * @param endVar      конечная дата периода (или имя переменной/property)
     * @param formatVar   формат даты (или имя переменной/property)
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по jsonPath \"([^\"]+)\" в периоде между \"([^\"]+)\" и \"([^\"]+)\" в формате \"([^\"]+)\"$")
    public void arrayDatesInRange(String responseVar, String jsonPath, String startVar, String endVar, String formatVar) {
        String startStr = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(startVar);
        String endStr = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(endVar);
        String format = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(formatVar);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        OffsetDateTime startDate = OffsetDateTime.parse(startStr, formatter);
        OffsetDateTime endDate = OffsetDateTime.parse(endStr, formatter);

        List<String> list = getJsonList(responseVar, jsonPath);
        for (String item : list) {
            OffsetDateTime date = parseOffsetDateTime(item, formatter);
            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                throw new AssertionError(String.format(
                        "Массив '%s': дата '%s' вне периода [%s, %s]", jsonPath, item, startStr, endStr));
            }
        }
    }

    private OffsetDateTime parseOffsetDateTime(String value, DateTimeFormatter formatter) {
        try {
            return OffsetDateTime.parse(value, formatter);
        } catch (Exception ignored) {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    // =======================================================================
    // ВНУТРЕННИЕ МЕТОДЫ
    // =======================================================================

    private List<String> getJsonList(String responseVar, String jsonPath) {
        List<String> list = ResponseHelper.getResponse(responseVar).getBody().jsonPath().getList(jsonPath);
        if (list == null) {
            throw new AssertionError(String.format("По jsonPath '%s' массив не найден", jsonPath));
        }
        return list;
    }
}
