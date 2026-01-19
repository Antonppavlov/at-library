package ru.at.library.api.steps.response;

import com.google.common.collect.Ordering;
import io.cucumber.java.ru.И;
import io.restassured.response.Response;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Набор шагов для проверки JSON-массивов в HTTP-ответах (размер, сортировка, значения и интервалы дат).
 */
public class JsonArrayVerificationSteps {

    /**
     * Проверка что массив найденный по jsonPath в ответе responseName содержит value
     *
     * @param responseName переменная в которой сохранен Response
     * @param key          jsonPath поиска массива
     * @param value        ожидаемое значение
     */
    @И("^в ответе \"([^\"]*)\" по ключу: \"([^\"]*)\" массив содержит \"([^\"]*)\"$")
    public void checkArrayHasItem(String responseName, String key, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        Response response = (Response) CoreScenario.getInstance().getVar(responseName);
        response
                .then()
                .body(key, Matchers.hasItem(value));
    }

    /**
     * Все объекты в коллекции имеют поле с определенным названием, содержащим конкретное значение
     *
     * @param responseName переменная в которой сохранен Response
     * @param jsonPath     jsonPath поиска массива
     * @param value        ожидаемое значение
     */
    @И("^в ответе \"([^\"]*)\" по ключу: \"([^\"]*)\" весь массив соотвествует \"([^\"]*)\"$")
    public void checkArrayEqualAllItem(String responseName, String jsonPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        Response response = (Response) CoreScenario.getInstance().getVar(responseName);

        List<String> list = response
                .getBody().jsonPath().getList(jsonPath);

        for (String actualValue : list) {
            if (!actualValue.equals(value)) {
                throw new AssertionError(
                        "Найденный по jsonPath" + jsonPath +
                                "\n список: " + list +
                                "\n содержит значения которые не equals: " + value);
            }
        }
    }

    /**
     * Все объекты в коллекции имеют поле с определенным названием, содержащим конкретное значение
     *
     * @param responseName переменная в которой сохранен Response
     * @param jsonPath     jsonPath поиска массива
     * @param value        ожидаемая часть значения
     */
    @И("^в ответе \"([^\"]*)\" по ключу: \"([^\"]*)\" весь массив частично соотвествует \"([^\"]*)\"$")
    public void checkArrayContainsAllItem(String responseName, String jsonPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        Response response = (Response) CoreScenario.getInstance().getVar(responseName);

        List<String> list = response
                .getBody().jsonPath().getList(jsonPath);

        for (String actualValue : list) {
            if (!actualValue.contains(value)) {
                throw new AssertionError(
                        "Найденный по jsonPath" + jsonPath +
                                "\n список: " + list +
                                "\n содержит значения которые не contains: " + value);
            }
        }
    }

    /**
     * Все объекты в коллекции имеют поле с определенным названием, содержащим конкретное значение
     *
     * @param responseName переменная в которой сохранен Response
     * @param jsonPath     jsonPath поиска массива
     * @param valueStart   ожидаемое начало периода
     * @param valueEnd     ожидаемый конец периода
     */
    @И("^в ответе \"([^\"]*)\" по ключу: \"([^\"]*)\" весь массив соответствуют периоду между \"([^\"]*)\" и \"([^\"]*)\" в формате \"([^\"]*)\"$")
    public void checkArrayContainsDataBetweenDatesAllItem(String responseName, String jsonPath, String valueStart,
                                                          String valueEnd, String format) {
        valueStart = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(valueStart);
        valueEnd = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(valueEnd);
        format = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(format);
        Response response = (Response) CoreScenario.getInstance().getVar(responseName);

        OffsetDateTime startDate = OffsetDateTime.parse(valueStart, DateTimeFormatter.ofPattern(format));
        OffsetDateTime endDate = OffsetDateTime.parse(valueEnd, DateTimeFormatter.ofPattern(format));

        List<String> list = response
                .getBody().jsonPath().getList(jsonPath);

        for (String actualValue : list) {
            OffsetDateTime testDate = OffsetDateTime.parse(actualValue, DateTimeFormatter.ofPattern(format));
            if (testDate.isBefore(startDate) || testDate.isAfter(endDate)) {
                throw new AssertionError(
                        "Найденный по jsonPath" + jsonPath +
                                "\n список: " + list +
                                "\n содержит значения которые не соответствуют периоду между : " + valueStart + " и "
                                + valueEnd);
            }
        }
    }

    /**
     * Проверка что массив найденный по jsonPath отсортирован по возрастанию
     *
     * @param responseName переменная в которой сохранен Response
     * @param jsonPath     jsonPath поиска массива
     */
    @И("^в ответе \"([^\"]*)\" по ключу: \"([^\"]*)\" элементы отсортированы по возрастанию")
    public void checkSortElementOrder(String responseName, String jsonPath) {
        Response response = (Response) CoreScenario.getInstance().getVar(responseName);

        List<String> list = response
                .getBody().jsonPath().getList(jsonPath);

        if (!Ordering.natural().nullsLast().isOrdered(list))
            throw new AssertionError(
                    "Найденный по jsonPath" + jsonPath +
                            "\n список: " + list +
                            "\n содержит значения которые не отсортированы по возрастанию");
    }

    /**
     * Проверка что массив найденный по jsonPath отсортирован по убыванию
     *
     * @param responseName переменная в которой сохранен Response
     * @param jsonPath     jsonPath поиска массива
     */
    @И("^в ответе \"([^\"]*)\" по ключу: \"([^\"]*)\" элементы отсортированы по убыванию")
    public void checkSortDescElementOrder(String responseName, String jsonPath) {
        Response response = (Response) CoreScenario.getInstance().getVar(responseName);

        List<String> list = response
                .getBody().jsonPath().getList(jsonPath);

        if (!Ordering.natural().nullsLast().reverse().isOrdered(list))
            throw new AssertionError(
                    "Найденный по jsonPath" + jsonPath +
                            "\n список: " + list +
                            "\n содержит значения которые не отсортированы по убыванию");
    }

    /**
     * Проверка что массив найденный по  jsonPath размером = value
     *
     * @param responseName переменная в которой сохранен Response
     * @param jsonPath     jsonPath поиска массива
     * @param value        size
     */
    @И("^в ответе \"([^\"]*)\" по ключу: \"([^\"]*)\" размер массива \"([^\"]*)\"$")
    public void checkArraySize(String responseName, String jsonPath, String value) {
        value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(value);
        Response response = (Response) CoreScenario.getInstance().getVar(responseName);
        MatcherAssert.assertThat(
                response.jsonPath().getList(jsonPath).size(),
                Matchers.equalTo(Integer.valueOf(value)));
    }
}
