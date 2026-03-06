package ru.at.library.api.steps.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Ordering;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Шаги для работы с YAML: проверка значений, сохранение, сравнение и массивы.
 * <p>YAML парсится в JSON-структуру, после чего запросы выполняются через jsonPath-синтаксис.
 * Поэтому yamlPath использует тот же синтаксис, что и jsonPath.</p>
 */
@Log4j2
public class YamlResponseSteps {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    // =======================================================================
    // ПРОВЕРКА ЗНАЧЕНИЙ В YAML ОТВЕТА
    // =======================================================================

    /**
     * Проверка одного значения в yaml ответа по yamlPath.
     * <p>Сравнение выполняется как строковое. yamlPath использует jsonPath-синтаксис.</p>
     * Пример:
     *  И в ответе "response" содержимое найденное по yamlPath "person.name" равно "John"
     *
     * @param responseVar   имя переменной, содержащей {@link Response}
     * @param yamlPath      yamlPath-выражение (jsonPath-синтаксис)
     * @param expectedValue ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" содержимое найденное по yamlPath \"([^\"]+)\" равно \"([^\"]+)\"$")
    public void checkYamlResponseValue(String responseVar, String yamlPath, String expectedValue) {
        expectedValue = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(expectedValue);
        JsonPath jsonPath = getYamlAsJsonPath(responseVar);
        String actualValue = jsonPath.getString(yamlPath);
        if (actualValue == null) {
            throw new AssertionError(String.format("По yamlPath '%s' значение не найдено", yamlPath));
        }
        assertThat("yamlPath '" + yamlPath + "'", actualValue, equalTo(expectedValue));
    }

    /**
     * Сохранение одного значения из yaml ответа по yamlPath в переменную.
     * Пример:
     *  И из ответа "response" содержимое найденное по yamlPath "person.name" сохранено в "name_var"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param yamlPath    yamlPath-выражение (jsonPath-синтаксис)
     * @param targetVar   имя переменной для сохранения
     */
    @И("^из ответа \"([^\"]+)\" содержимое найденное по yamlPath \"([^\"]+)\" сохранено в \"([^\"]+)\"$")
    public void saveYamlResponseValue(String responseVar, String yamlPath, String targetVar) {
        JsonPath jsonPath = getYamlAsJsonPath(responseVar);
        String value = jsonPath.getString(yamlPath);
        if (value == null) {
            throw new RuntimeException(String.format("По yamlPath '%s' значение не найдено в ответе", yamlPath));
        }
        coreScenario.setVar(targetVar, value);
        log.trace("yamlPath '{}' = '{}' сохранено в '{}'", yamlPath, value, targetVar);
    }

    /**
     * Проверка значений в yaml ответа по yamlPath (простое равенство).
     * Пример:
     *  И в ответе "response" содержимые найденные по yamlPath равны:
     *    | person.name | John |
     *  И в ответе "response" содержимые найденные по yamlPath без учета регистра равны:
     *    | person.name | JOHN |
     *
     * @param responseVar             имя переменной, содержащей {@link Response}
     * @param caseInsensitiveIndicator пусто или "без учета регистра "
     * @param dataTable               таблица: yamlPath, ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" содержимые найденные по yamlPath (|без учета регистра )равны:$")
    public void checkYamlResponseValues(String responseVar, String caseInsensitiveIndicator, DataTable dataTable) {
        JsonPath jsonPath = getYamlAsJsonPath(responseVar);
        boolean caseInsensitive = !caseInsensitiveIndicator.isEmpty();
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String expectedValue = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(row.get(1));
            String actualValue = jsonPath.getString(path);

            if (actualValue == null) {
                errors.append(String.format("По yamlPath '%s' значение не найдено%n", path));
                continue;
            }

            if (caseInsensitive) {
                expectedValue = expectedValue.toLowerCase();
                actualValue = actualValue.toLowerCase();
            }

            if (!actualValue.equals(expectedValue)) {
                errors.append(String.format("yamlPath '%s': ожидалось '%s', получено '%s'%n", path, expectedValue, actualValue));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    // =======================================================================
    // СОХРАНЕНИЕ ЗНАЧЕНИЙ ИЗ YAML
    // =======================================================================

    /**
     * Сохранение значений из yaml ответа по yamlPath.
     * Пример:
     *  И из ответа "response" содержимые найденные по yamlPath сохранены в переменные:
     *    | person.name | name_var |
     *    | person.age  | age_var  |
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param dataTable   таблица: yamlPath, имя переменной
     */
    @И("^из ответа \"([^\"]+)\" содержимые найденные по yamlPath сохранены в переменные:$")
    public void saveFromYamlResponse(String responseVar, DataTable dataTable) {
        JsonPath jsonPath = getYamlAsJsonPath(responseVar);

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String targetVar = row.get(1);
            String value = jsonPath.getString(path);

            if (value == null) {
                throw new RuntimeException(String.format("По yamlPath '%s' значение не найдено в ответе", path));
            }

            coreScenario.setVar(targetVar, value);
            log.trace("yamlPath '{}' = '{}' сохранено в '{}'", path, value, targetVar);
        }
    }

    // =======================================================================
    // СРАВНЕНИЕ
    // =======================================================================

    /**
     * Сравнение значений по yamlPath между двумя ответами.
     * Пример:
     *  И в ответах "response1" и "response2" содержимые найденные по yamlPath совпадают:
     *    | person.name |
     *    | person.age  |
     *
     * @param responseVar1 имя первого ответа
     * @param responseVar2 имя второго ответа
     * @param dataTable    таблица с yamlPath-ключами
     */
    @И("^в ответах \"([^\"]+)\" и \"([^\"]+)\" содержимые найденные по yamlPath совпадают:$")
    public void compareYamlResponses(String responseVar1, String responseVar2, DataTable dataTable) {
        JsonPath jsonPath1 = getYamlAsJsonPath(responseVar1);
        JsonPath jsonPath2 = getYamlAsJsonPath(responseVar2);
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String value1 = jsonPath1.getString(path);
            String value2 = jsonPath2.getString(path);

            if (value1 == null) {
                errors.append(String.format("yamlPath '%s' не найден в ответе '%s'%n", path, responseVar1));
                continue;
            }
            if (value2 == null) {
                errors.append(String.format("yamlPath '%s' не найден в ответе '%s'%n", path, responseVar2));
                continue;
            }
            if (!value1.equals(value2)) {
                errors.append(String.format("yamlPath '%s': '%s' != '%s'%n", path, value1, value2));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    /**
     * Сравнение body ответа с эталонным YAML.
     * Пример:
     *  И в ответе "response" содержимое равно yaml "expected.yaml"
     *
     * @param responseVar  имя переменной с {@link Response}
     * @param expectedYaml путь к эталонному YAML (property/resource)
     */
    @И("^в ответе \"([^\"]+)\" содержимое равно yaml \"([^\"]+)\"$")
    public void compareYamlBody(String responseVar, String expectedYaml) {
        String yaml = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(expectedYaml);
        Response response = ResponseHelper.getResponse(responseVar);
        response.then().body(equalTo(yaml));
    }

    // =======================================================================
    // МАССИВЫ
    // =======================================================================

    /**
     * Проверка что массив значений найденных по yamlPath содержит указанное значение.
     * Пример:
     *  И в ответе "response" массив значений найденных по yamlPath "items.status" содержит значение "active"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param yamlPath    yamlPath-выражение (jsonPath-синтаксис)
     * @param value       ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по yamlPath \"([^\"]+)\" содержит значение \"([^\"]+)\"$")
    public void arrayContains(String responseVar, String yamlPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getYamlList(responseVar, yamlPath);
        if (!list.contains(value)) {
            throw new AssertionError(String.format(
                    "Массив '%s' не содержит значение '%s'. Фактические значения: %s", yamlPath, value, list));
        }
    }

    /**
     * Проверка что все элементы массива равны указанному значению.
     * Пример:
     *  И в ответе "response" массив значений найденных по yamlPath "items.status" все значения равны "active"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param yamlPath    yamlPath-выражение (jsonPath-синтаксис)
     * @param value       ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по yamlPath \"([^\"]+)\" все значения равны \"([^\"]+)\"$")
    public void arrayAllEqual(String responseVar, String yamlPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getYamlList(responseVar, yamlPath);
        for (String actual : list) {
            if (!actual.equals(value)) {
                throw new AssertionError(String.format("Массив '%s' содержит '%s', ожидалось все '%s'", yamlPath, actual, value));
            }
        }
    }

    /**
     * Проверка что все элементы массива содержат подстроку.
     * Пример:
     *  И в ответе "response" массив значений найденных по yamlPath "items.name" все значения содержат "test"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param yamlPath    yamlPath-выражение (jsonPath-синтаксис)
     * @param value       ожидаемая подстрока
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по yamlPath \"([^\"]+)\" все значения содержат \"([^\"]+)\"$")
    public void arrayAllContain(String responseVar, String yamlPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getYamlList(responseVar, yamlPath);
        for (String actual : list) {
            if (!actual.contains(value)) {
                throw new AssertionError(String.format("Массив '%s' содержит '%s', которое не содержит подстроку '%s'", yamlPath, actual, value));
            }
        }
    }

    /**
     * Проверка размера массива по yamlPath.
     * Пример:
     *  И в ответе "response" массив значений найденных по yamlPath "items" размер 5
     *
     * @param responseVar  имя переменной, содержащей {@link Response}
     * @param yamlPath     yamlPath-выражение (jsonPath-синтаксис)
     * @param expectedSize ожидаемый размер массива
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по yamlPath \"([^\"]+)\" размер (\\d+)$")
    public void arraySize(String responseVar, String yamlPath, int expectedSize) {
        List<String> list = getYamlList(responseVar, yamlPath);
        assertThat("Размер массива '" + yamlPath + "'", list.size(), equalTo(expectedSize));
    }

    /**
     * Проверка что массив отсортирован по возрастанию.
     * Пример:
     *  И в ответе "response" массив значений найденных по yamlPath "items.name" отсортирован по возрастанию
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param yamlPath    yamlPath-выражение (jsonPath-синтаксис)
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по yamlPath \"([^\"]+)\" отсортирован по возрастанию$")
    public void arraySortedAsc(String responseVar, String yamlPath) {
        List<String> list = getYamlList(responseVar, yamlPath);
        if (!Ordering.natural().nullsLast().isOrdered(list)) {
            throw new AssertionError(String.format("Массив '%s' не отсортирован по возрастанию: %s", yamlPath, list));
        }
    }

    /**
     * Проверка что массив отсортирован по убыванию.
     * Пример:
     *  И в ответе "response" массив значений найденных по yamlPath "items.name" отсортирован по убыванию
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param yamlPath    yamlPath-выражение (jsonPath-синтаксис)
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по yamlPath \"([^\"]+)\" отсортирован по убыванию$")
    public void arraySortedDesc(String responseVar, String yamlPath) {
        List<String> list = getYamlList(responseVar, yamlPath);
        if (!Ordering.natural().nullsLast().reverse().isOrdered(list)) {
            throw new AssertionError(String.format("Массив '%s' не отсортирован по убыванию: %s", yamlPath, list));
        }
    }

    /**
     * Проверка что все даты в массиве находятся в указанном периоде.
     * Пример:
     *  И в ответе "response" массив значений найденных по yamlPath "events.date" в периоде между "start" и "end" в формате "fmt"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param yamlPath    yamlPath-выражение (jsonPath-синтаксис)
     * @param startVar    начальная дата периода (или имя переменной/property)
     * @param endVar      конечная дата периода (или имя переменной/property)
     * @param formatVar   формат даты (или имя переменной/property)
     */
    @И("^в ответе \"([^\"]+)\" массив значений найденных по yamlPath \"([^\"]+)\" в периоде между \"([^\"]+)\" и \"([^\"]+)\" в формате \"([^\"]+)\"$")
    public void arrayDatesInRange(String responseVar, String yamlPath, String startVar, String endVar, String formatVar) {
        String startStr = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(startVar);
        String endStr = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(endVar);
        String format = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(formatVar);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        OffsetDateTime startDate = OffsetDateTime.parse(startStr, formatter);
        OffsetDateTime endDate = OffsetDateTime.parse(endStr, formatter);

        List<String> list = getYamlList(responseVar, yamlPath);
        for (String item : list) {
            OffsetDateTime date = OffsetDateTime.parse(item, formatter);
            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                throw new AssertionError(String.format(
                        "Массив '%s': дата '%s' вне периода [%s, %s]", yamlPath, item, startStr, endStr));
            }
        }
    }

    // =======================================================================
    // ВНУТРЕННИЕ МЕТОДЫ
    // =======================================================================

    /**
     * Преобразует YAML-тело ответа в rest-assured JsonPath для выполнения запросов.
     */
    private JsonPath getYamlAsJsonPath(String responseVar) {
        String yamlBody = ResponseHelper.getResponse(responseVar).body().asString();
        try {
            Object yamlObj = YAML_MAPPER.readValue(yamlBody, Object.class);
            String json = JSON_MAPPER.writeValueAsString(yamlObj);
            return new JsonPath(json);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось преобразовать YAML в JSON: " + e.getMessage(), e);
        }
    }

    private List<String> getYamlList(String responseVar, String yamlPath) {
        JsonPath jsonPath = getYamlAsJsonPath(responseVar);
        List<String> list = jsonPath.getList(yamlPath);
        if (list == null) {
            throw new AssertionError(String.format("По yamlPath '%s' массив не найден", yamlPath));
        }
        return list;
    }
}
