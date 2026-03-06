package ru.at.library.api.steps.response;

import com.google.common.collect.Ordering;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.matcher.RestAssuredMatchers;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import ru.at.library.api.helpers.Utils;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Шаги для работы с XML: проверка значений, сохранение, сравнение, схемы и списки узлов.
 */
@Log4j2
public class XmlResponseSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    // =======================================================================
    // ПРОВЕРКА ЗНАЧЕНИЙ В XML ОТВЕТА
    // =======================================================================

    /**
     * Проверка одного значения в xml ответа по xPath.
     * <p>Сравнение выполняется как строковое.</p>
     * Пример:
     *  И в ответе "response" содержимое найденное по xPath "//status" равно "available"
     *
     * @param responseVar   имя переменной, содержащей {@link Response}
     * @param xPath         xPath-выражение
     * @param expectedValue ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" содержимое найденное по xPath \"([^\"]+)\" равно \"([^\"]+)\"$")
    public void checkXmlResponseValue(String responseVar, String xPath, String expectedValue) {
        expectedValue = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(expectedValue);
        Document doc = getXmlDocument(responseVar);
        String actualValue = findXPathValue(doc, xPath);
        if (actualValue == null) {
            throw new AssertionError(String.format("По xPath '%s' значение не найдено", xPath));
        }
        assertThat("xPath '" + xPath + "'", actualValue, equalTo(expectedValue));
    }

    /**
     * Сохранение одного значения из xml ответа по xPath в переменную.
     * Пример:
     *  И из ответа "response" содержимое найденное по xPath "//status" сохранено в "pet_status"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param xPath       xPath-выражение
     * @param targetVar   имя переменной для сохранения
     */
    @И("^из ответа \"([^\"]+)\" содержимое найденное по xPath \"([^\"]+)\" сохранено в \"([^\"]+)\"$")
    public void saveXmlResponseValue(String responseVar, String xPath, String targetVar) {
        Document doc = getXmlDocument(responseVar);
        String value = findXPathValue(doc, xPath);
        if (value == null) {
            throw new RuntimeException(String.format("По xPath '%s' значение не найдено в ответе", xPath));
        }
        coreScenario.setVar(targetVar, value);
        log.trace("xPath '{}' = '{}' сохранено в '{}'", xPath, value, targetVar);
    }

    /**
     * Проверка значений в xml ответа по xPath (простое равенство).
     * Пример:
     *  И в ответе "response" содержимые найденные по xPath равны:
     *    | //status | available |
     *  И в ответе "response" содержимые найденные по xPath без учета регистра равны:
     *    | //status | AVAILABLE |
     *
     * @param responseVar             имя переменной, содержащей {@link Response}
     * @param caseInsensitiveIndicator пусто или "без учета регистра "
     * @param dataTable               таблица: xPath, ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" содержимые найденные по xPath (|без учета регистра )равны:$")
    public void checkXmlResponseValues(String responseVar, String caseInsensitiveIndicator, DataTable dataTable) {
        Document doc = getXmlDocument(responseVar);
        boolean caseInsensitive = !caseInsensitiveIndicator.isEmpty();
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String expectedValue = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(row.get(1));
            String actualValue = findXPathValue(doc, path);

            if (actualValue == null) {
                errors.append(String.format("По xPath '%s' значение не найдено%n", path));
                continue;
            }

            if (caseInsensitive) {
                expectedValue = expectedValue.toLowerCase();
                actualValue = actualValue.toLowerCase();
            }

            if (!actualValue.equals(expectedValue)) {
                errors.append(String.format("xPath '%s': ожидалось '%s', получено '%s'%n", path, expectedValue, actualValue));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    // =======================================================================
    // СОХРАНЕНИЕ ЗНАЧЕНИЙ ИЗ XML
    // =======================================================================

    /**
     * Сохранение значений из xml ответа по xPath.
     * Пример:
     *  И из ответа "response" содержимые найденные по xPath сохранены в переменные:
     *    | //status | pet_status |
     *    | //id     | pet_id     |
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param dataTable   таблица: xPath, имя переменной
     */
    @И("^из ответа \"([^\"]+)\" содержимые найденные по xPath сохранены в переменные:$")
    public void saveFromXmlResponse(String responseVar, DataTable dataTable) {
        Document doc = getXmlDocument(responseVar);

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String targetVar = row.get(1);
            String value = findXPathValue(doc, path);

            if (value == null) {
                throw new RuntimeException(String.format("По xPath '%s' значение не найдено в ответе", path));
            }

            coreScenario.setVar(targetVar, value);
            log.trace("xPath '{}' = '{}' сохранено в '{}'", path, value, targetVar);
        }
    }

    // =======================================================================
    // СРАВНЕНИЕ
    // =======================================================================

    /**
     * Сравнение значений по xPath между двумя ответами.
     * Пример:
     *  И в ответах "response1" и "response2" содержимые найденные по xPath совпадают:
     *    | //status |
     *    | //name   |
     *
     * @param responseVar1 имя первого ответа
     * @param responseVar2 имя второго ответа
     * @param dataTable    таблица с xPath-ключами
     */
    @И("^в ответах \"([^\"]+)\" и \"([^\"]+)\" содержимые найденные по xPath совпадают:$")
    public void compareXmlResponses(String responseVar1, String responseVar2, DataTable dataTable) {
        Document doc1 = getXmlDocument(responseVar1);
        Document doc2 = getXmlDocument(responseVar2);
        StringBuilder errors = new StringBuilder();

        for (List<String> row : dataTable.asLists()) {
            String path = row.get(0);
            String value1 = findXPathValue(doc1, path);
            String value2 = findXPathValue(doc2, path);

            if (value1 == null) {
                errors.append(String.format("xPath '%s' не найден в ответе '%s'%n", path, responseVar1));
                continue;
            }
            if (value2 == null) {
                errors.append(String.format("xPath '%s' не найден в ответе '%s'%n", path, responseVar2));
                continue;
            }
            if (!value1.equals(value2)) {
                errors.append(String.format("xPath '%s': '%s' != '%s'%n", path, value1, value2));
            }
        }

        if (errors.length() > 0) {
            throw new AssertionError(errors.toString());
        }
    }

    /**
     * Сравнение body ответа с эталонным XML.
     * Пример:
     *  И в ответе "response" содержимое равно xml "expected.xml"
     *
     * @param responseVar имя переменной с {@link Response}
     * @param expectedXml путь к эталонному XML (property/resource)
     */
    @И("^в ответе \"([^\"]+)\" содержимое равно xml \"([^\"]+)\"$")
    public void compareXmlBody(String responseVar, String expectedXml) {
        String xml = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(expectedXml);
        Response response = ResponseHelper.getResponse(responseVar);
        response.then().body(equalTo(xml));
    }

    // =======================================================================
    // СХЕМЫ
    // =======================================================================

    /**
     * Проверка ответа на соответствие XSD-схеме.
     * Пример:
     *  И в ответе "response" содержимое соответствует xsd схеме "xsd/pet_schema.xsd"
     *
     * @param responseVar имя переменной с {@link Response}
     * @param schemaPath  путь до .xsd файла со схемой в classpath
     */
    @И("^в ответе \"([^\"]+)\" содержимое соответствует xsd схеме \"([^\"]+)\"$")
    public void validateXsdSchema(String responseVar, String schemaPath) {
        Response response = ResponseHelper.getResponse(responseVar);
        response.then().assertThat().body(RestAssuredMatchers.matchesXsdInClasspath(schemaPath));
    }

    // =======================================================================
    // СПИСКИ УЗЛОВ
    // =======================================================================

    /**
     * Проверка что список значений найденных по xPath содержит указанное значение.
     * Пример:
     *  И в ответе "response" список значений найденных по xPath "//status" содержит значение "available"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param xPath       xPath-выражение
     * @param value       ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" список значений найденных по xPath \"([^\"]+)\" содержит значение \"([^\"]+)\"$")
    public void listContains(String responseVar, String xPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getXPathList(responseVar, xPath);
        if (!list.contains(value)) {
            throw new AssertionError(String.format(
                    "Список '%s' не содержит значение '%s'. Фактические значения: %s", xPath, value, list));
        }
    }

    /**
     * Проверка что все элементы списка равны указанному значению.
     * Пример:
     *  И в ответе "response" список значений найденных по xPath "//status" все значения равны "available"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param xPath       xPath-выражение
     * @param value       ожидаемое значение
     */
    @И("^в ответе \"([^\"]+)\" список значений найденных по xPath \"([^\"]+)\" все значения равны \"([^\"]+)\"$")
    public void listAllEqual(String responseVar, String xPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getXPathList(responseVar, xPath);
        for (String actual : list) {
            if (!actual.equals(value)) {
                throw new AssertionError(String.format("Список '%s' содержит '%s', ожидалось все '%s'", xPath, actual, value));
            }
        }
    }

    /**
     * Проверка что все элементы списка содержат подстроку.
     * Пример:
     *  И в ответе "response" список значений найденных по xPath "//status" все значения содержат "avail"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param xPath       xPath-выражение
     * @param value       ожидаемая подстрока
     */
    @И("^в ответе \"([^\"]+)\" список значений найденных по xPath \"([^\"]+)\" все значения содержат \"([^\"]+)\"$")
    public void listAllContain(String responseVar, String xPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        List<String> list = getXPathList(responseVar, xPath);
        for (String actual : list) {
            if (!actual.contains(value)) {
                throw new AssertionError(String.format("Список '%s' содержит '%s', которое не содержит подстроку '%s'", xPath, actual, value));
            }
        }
    }

    /**
     * Проверка размера списка узлов по xPath.
     * Пример:
     *  И в ответе "response" список значений найденных по xPath "//tag" размер 3
     *
     * @param responseVar  имя переменной, содержащей {@link Response}
     * @param xPath        xPath-выражение
     * @param expectedSize ожидаемый размер списка
     */
    @И("^в ответе \"([^\"]+)\" список значений найденных по xPath \"([^\"]+)\" размер (\\d+)$")
    public void listSize(String responseVar, String xPath, int expectedSize) {
        List<String> list = getXPathList(responseVar, xPath);
        assertThat("Размер списка '" + xPath + "'", list.size(), equalTo(expectedSize));
    }

    /**
     * Проверка что список отсортирован по возрастанию.
     * Пример:
     *  И в ответе "response" список значений найденных по xPath "//name" отсортирован по возрастанию
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param xPath       xPath-выражение
     */
    @И("^в ответе \"([^\"]+)\" список значений найденных по xPath \"([^\"]+)\" отсортирован по возрастанию$")
    public void listSortedAsc(String responseVar, String xPath) {
        List<String> list = getXPathList(responseVar, xPath);
        if (!Ordering.natural().nullsLast().isOrdered(list)) {
            throw new AssertionError(String.format("Список '%s' не отсортирован по возрастанию: %s", xPath, list));
        }
    }

    /**
     * Проверка что список отсортирован по убыванию.
     * Пример:
     *  И в ответе "response" список значений найденных по xPath "//name" отсортирован по убыванию
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param xPath       xPath-выражение
     */
    @И("^в ответе \"([^\"]+)\" список значений найденных по xPath \"([^\"]+)\" отсортирован по убыванию$")
    public void listSortedDesc(String responseVar, String xPath) {
        List<String> list = getXPathList(responseVar, xPath);
        if (!Ordering.natural().nullsLast().reverse().isOrdered(list)) {
            throw new AssertionError(String.format("Список '%s' не отсортирован по убыванию: %s", xPath, list));
        }
    }

    /**
     * Проверка что все даты в списке находятся в указанном периоде.
     * Пример:
     *  И в ответе "response" список значений найденных по xPath "//date" в периоде между "date_start" и "date_end" в формате "date_format"
     *
     * @param responseVar имя переменной, содержащей {@link Response}
     * @param xPath       xPath-выражение
     * @param startVar    начальная дата периода (или имя переменной/property)
     * @param endVar      конечная дата периода (или имя переменной/property)
     * @param formatVar   формат даты (или имя переменной/property)
     */
    @И("^в ответе \"([^\"]+)\" список значений найденных по xPath \"([^\"]+)\" в периоде между \"([^\"]+)\" и \"([^\"]+)\" в формате \"([^\"]+)\"$")
    public void listDatesInRange(String responseVar, String xPath, String startVar, String endVar, String formatVar) {
        String startStr = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(startVar);
        String endStr = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(endVar);
        String format = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(formatVar);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        OffsetDateTime startDate = OffsetDateTime.parse(startStr, formatter);
        OffsetDateTime endDate = OffsetDateTime.parse(endStr, formatter);

        List<String> list = getXPathList(responseVar, xPath);
        for (String item : list) {
            OffsetDateTime date = OffsetDateTime.parse(item, formatter);
            if (date.isBefore(startDate) || date.isAfter(endDate)) {
                throw new AssertionError(String.format(
                        "Список '%s': дата '%s' вне периода [%s, %s]", xPath, item, startStr, endStr));
            }
        }
    }

    // =======================================================================
    // ВНУТРЕННИЕ МЕТОДЫ
    // =======================================================================

    private Document getXmlDocument(String responseVar) {
        String body = ResponseHelper.getResponse(responseVar).body().asString();
        return Utils.readXml(new ByteArrayInputStream(body.getBytes()));
    }

    private String findXPathValue(Document doc, String xPath) {
        NodeList nodes = Utils.filterNodesByXPath(doc, xPath);
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }

    private List<String> getXPathList(String responseVar, String xPath) {
        Document doc = getXmlDocument(responseVar);
        NodeList nodes = Utils.filterNodesByXPath(doc, xPath);
        if (nodes == null || nodes.getLength() == 0) {
            throw new AssertionError(String.format("По xPath '%s' список узлов не найден", xPath));
        }
        List<String> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i).getTextContent());
        }
        return list;
    }
}
