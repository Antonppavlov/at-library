package ru.at.library.api.steps.response;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.text.MatchesPattern;
import ru.at.library.api.helpers.FormattedDataContainer;
import ru.at.library.api.helpers.TextFormat;
import ru.at.library.api.helpers.Utils;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.testng.Assert.fail;

/**
 * Шаги проверки HTTP-ответов: статус-коды, заголовки, cookies, тело ответа и форматированные данные (JSON/XML/PARAMS).
 */
public class CheckResponseSteps {

    private static final Logger log = LogManager.getLogger(CheckResponseSteps.class);

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Сравнение кода HTTP-ответа с ожидаемым.
     *
     * @param responseNameVariable имя переменной, в которой сохранён {@link Response}
     * @param expectedStatusCode   ожидаемый HTTP статус-код
     */
    @И("^в ответе \"([^\"]+)\" statusCode: (\\d+)$")
    public void checkResponseStatusCode(String responseNameVariable, int expectedStatusCode) {
        Response response = getResponse(responseNameVariable);
        response.then().statusCode(expectedStatusCode);
    }

    /**
     * Проверка значений форматированного текста.
     * В строке JSON/XML/PARAMS происходит проверка значений по jsonPath/xmlPath/paramName из первого столбца таблицы
     * по операциям из второго столбца таблицы ('==' - равенство, '!=' - неравенство, '~' - соответствие регулярному выражению,
     * '!~' - несоответствие регулярному выражению) и по значениям в третьем столбце таблицы.
     * Ожидается не менее трёх колонок в каждой строке таблицы: путь, операция, ожидаемое значение.
     *
     * @param checkingValuePath        строка для обработки (URL/JSON/XML/параметры) после подстановки переменных
     * @param caseInsensitiveIndicator индикатор независимости от регистра ("без учета регистра" / пусто)
     * @param dataTable                таблица с путями, операциями и ожидаемыми значениями; в URL и значениях можно использовать
     *                                 переменные из properties и хранилища {@link CoreScenario}, заключая их в фигурные скобки,
     *                                 например: {@code http://{hostname}?user={username}}.
     */
    @И("^значения в \"([^\"]+)\" проверены ((?:|без учета регистра ))по таблице:$")
    public void checkFormattedData(String checkingValuePath, String caseInsensitiveIndicator, DataTable dataTable) {
        checkFormattedData(null, checkingValuePath, !caseInsensitiveIndicator.isEmpty(), dataTable);
    }

    /**
     * Проверка значений форматированного текста с явным указанием формата.
     * Поведение аналогично {@link #checkFormattedData(String, String, DataTable)}, но формат (XML/JSON/PARAMS)
     * передаётся явно и при несовпадении с фактическими данными будет выброшено исключение.
     *
     * @param checkingValueType        ожидаемый формат переданной строки (XML/JSON/PARAMS)
     * @param checkingValuePath        строка для обработки (URL/JSON/XML/параметры) после подстановки переменных
     * @param caseInsensitiveIndicator индикатор независимости от регистра ("без учета регистра" / пусто)
     * @param dataTable                таблица с путями, операциями и ожидаемыми значениями; каждая строка должна содержать
     *                                 не менее трёх колонок: путь, операцию и значение
     */
    @И("^значения в ((?:XML|JSON|PARAMS)) \"([^\"]+)\" проверены ((?:|без учета регистра ))по таблице:$")
    public void checkFormattedData(TextFormat checkingValueType, String checkingValuePath, String caseInsensitiveIndicator, DataTable dataTable) {
        checkFormattedData(checkingValueType, checkingValuePath, !caseInsensitiveIndicator.isEmpty(), dataTable);
    }

    /**
     * Проверка значений форматированного текста для значения, сохранённого "по умолчанию".
     * Формат и сама строка берутся из ранее сохранённого контекста, а таблица описывает пути, операции и ожидаемые значения.
     *
     * @param caseInsensitiveIndicator индикатор независимости от регистра ("без учета регистра" / пусто)
     * @param dataTable                таблица с путями, операциями и ожидаемыми значениями; каждая строка должна содержать
     *                                 не менее трёх колонок: путь, операцию и значение
     */
    @И("^значения в нём проверены ((?:|без учета регистра ))по таблице:$")
    public void checkFormattedData(String caseInsensitiveIndicator, DataTable dataTable) {
        checkFormattedData(null, null, !caseInsensitiveIndicator.isEmpty(), dataTable);
    }

    /**
     * Проверка значений форматированного текста.
     * В строке JSON/XML/PARAMS происходит проверка значений по jsonPath/xmlPath/paramName из первого столбца таблицы
     * по операциям из второго столбца таблицы ('==' - равенство, '!=' - неравенство, '~' - соответствие регулярному выражению,
     * '!~' - несоответствие регулярному выражению) и по значениям в третьем столбце таблицы.
     * В случае некорректного формата таблицы (пустые значения, неизвестная операция и т.п.) будет выброшено понятное исключение.
     *
     * @param checkingValueType формат переданной строки (может быть {@code null} для автоопределения)
     * @param checkingValuePath строка для обработки (может быть {@code null} для значения по умолчанию)
     * @param caseInsensitive   {@code true}, если сравнение должно выполняться без учёта регистра
     * @param dataTable         таблица с путями, операциями и ожидаемыми значениями
     */
    private void checkFormattedData(TextFormat checkingValueType, String checkingValuePath, boolean caseInsensitive, DataTable dataTable) {
        if (dataTable == null || dataTable.isEmpty()) {
            fail("Таблица с проверками не может быть пустой");
        }

        StringBuilder errorMessage = new StringBuilder();

        FormattedDataContainer formattedData = createFormattedDataContainer(checkingValueType, checkingValuePath);
        int rowIndex = 0;
        for (List<String> row : dataTable.asLists()) {
            rowIndex++;
            if (row == null || row.size() < 3) {
                fail("Ожидается минимум 3 колонки в строке таблицы (path, operation, value). Строка: " + rowIndex);
            }

            String path = row.get(0);
            String operation = row.get(1);
            String value = row.get(2);

            if (path == null || path.trim().isEmpty()) {
                fail("Путь (jsonPath/xmlPath/paramName) не может быть пустым. Строка: " + rowIndex);
            }

            if (operation == null || operation.trim().isEmpty()) {
                fail("Операция сравнения не может быть пустой. Строка: " + rowIndex);
            }

            Function<String, Matcher<? super String>> matcher = defineOperation(operation);
            String expectedValue = PropertyLoader.cycleSubstitutionFromFileOrPropertyOrVariable(value);
            String actualValue = formattedData.readValue(path);

            if (caseInsensitive) {
                expectedValue = expectedValue.toLowerCase();
                actualValue = actualValue.toLowerCase();
            }

            try {
                assertThat("\nНеверное содержимое элемента: " + path,
                        actualValue, matcher.apply(expectedValue));
            } catch (AssertionError e) {
                errorMessage.append(e.getMessage());
            }
        }
        if (!errorMessage.toString().isEmpty()) {
            fail(errorMessage.toString());
        }
    }

    /**
     * Определение типа операции по проверке:
     * '==' - равенство, '!=' - неравенство, '~' - соответствие регулярному выражению, '!~' - несоответствие регулярному выражению.
     * В случае передачи {@code null}, пустой строки или неизвестной операции будет выброшено понятное исключение.
     *
     * @param operationString строка с операцией проверки
     * @return матчер для проверки
     */
    static Function<String, Matcher<? super String>> defineOperation(String operationString) {
        if (operationString == null || operationString.trim().isEmpty()) {
            fail("Операция сравнения не может быть null или пустой");
        }

        String op = operationString.trim();
        Function<String, Matcher<? super String>> matcher = null;
        switch (op) {
            case "==":
                matcher = Matchers::equalTo;
                break;
            case "!=":
                matcher = s -> not(equalTo(s));
                break;
            case "~":
                matcher = MatchesPattern::matchesPattern;
                break;
            case "!~":
                matcher = s -> not(matchesPattern(s));
                break;
            default:
                fail("Нечитаемый формат операции: " + operationString);
                break;
        }
        return matcher;
    }

    /**
     * Создаёт контейнер форматированных данных на основе строки и формата.
     * Выполняет подстановку переменных в исходной строке и определяет/проверяет формат.
     *
     * @param valueType ожидаемый формат (может быть {@code null} для автоопределения)
     * @param valuePath исходная строка (URL/JSON/XML/параметры) или ссылка на неё через properties/переменные
     * @return контейнер форматированных данных
     */
    private FormattedDataContainer createFormattedDataContainer(TextFormat valueType, String valuePath) {
        String valueString = PropertyLoader.cycleSubstitutionFromFileOrPropertyOrVariable(valuePath);
        TextFormat resolvedType = Utils.defineOrCheckDataFormat(valueString, valueType);
        return new FormattedDataContainer(resolvedType, valueString);
    }

    /**
     * Сохранение значений форматированного текста.
     * В строке JSON/XML/PARAMS происходит поиск значений по jsonPath/xmlPath/paramName из первого столбца таблицы.
     * Полученные значения сохраняются в переменных. Название переменной указывается во втором столбце таблицы.
     *
     * @param processingValuePath строка для обработки (URL/JSON/XML/параметры) после подстановки переменных
     * @param dataTable           таблица с путями и именами переменных для сохранения
     */
    @И("^значения из \"([^\"]+)\" сохранены в переменные по таблице:$")
    public void saveValuesFromFormattedData(String processingValuePath, DataTable dataTable) {
        saveValuesFromFormattedData(null, processingValuePath, dataTable);
    }

    /**
     * Сохранение значений форматированного текста из значения, сохранённого "по умолчанию".
     * В строке JSON/XML/PARAMS, сохранённой ранее в контексте, происходит поиск значений по jsonPath/xmlPath/paramName
     * из первого столбца таблицы. Полученные значения сохраняются в переменных. Название переменной указывается во втором столбце.
     *
     * @param dataTable таблица с путями и именами переменных для сохранения
     */
    @И("^значения из него сохранены в переменные по таблице:$")
    public void saveValuesFromFormattedData(DataTable dataTable) {
        saveValuesFromFormattedData(null, null, dataTable);
    }

    /**
     * Сохранение значений форматированного текста с явным указанием формата.
     * Поведение аналогично {@link #saveValuesFromFormattedData(String, DataTable)}, но формат (XML/JSON/PARAMS)
     * передаётся явно и при несовпадении с фактическими данными будет выброшено исключение.
     *
     * @param processingValueType ожидаемый формат переданной строки (XML/JSON/PARAMS)
     * @param processingValuePath строка для обработки (URL/JSON/XML/параметры) после подстановки переменных
     * @param dataTable           таблица с путями и именами переменных для сохранения; каждая строка должна содержать
     *                            не менее двух колонок: путь и имя переменной
     */
    @И("^значения из ((?:XML|JSON|PARAMS)) \"([^\"]+)\" сохранены в переменные по таблице:$")
    public void saveValuesFromFormattedData(TextFormat processingValueType, String processingValuePath, DataTable dataTable) {
        if (dataTable == null || dataTable.isEmpty()) {
            fail("Таблица с путями для сохранения не может быть пустой");
        }

        StringBuilder errorMessage = new StringBuilder();

        FormattedDataContainer formattedData = createFormattedDataContainer(processingValueType, processingValuePath);
        int rowIndex = 0;
        for (List<String> row : dataTable.asLists()) {
            rowIndex++;
            if (row == null || row.size() < 2) {
                fail("Ожидается минимум 2 колонки в строке таблицы (path, variable). Строка: " + rowIndex);
            }

            String path = row.get(0);
            String variableToSave = row.get(1);

            if (path == null || path.trim().isEmpty()) {
                fail("Путь (jsonPath/xmlPath/paramName) не может быть пустым. Строка: " + rowIndex);
            }
            if (variableToSave == null || variableToSave.trim().isEmpty()) {
                fail("Имя переменной для сохранения не может быть пустым. Строка: " + rowIndex);
            }

            try {
                String actualValue = formattedData.readValue(path);
                coreScenario.setVar(variableToSave, actualValue);
            } catch (Exception e) {
                errorMessage.append("Не найдено значение: ").append(path).append("\n");
            }
        }
        if (!errorMessage.toString().isEmpty()) {
            fail(errorMessage.toString());
        }
    }

    /**
     * Ответ, сохранённый в переменной, записывается в переменную сценария как строка body.
     *
     * @param responseNameVariable имя переменной, которая содержит {@link Response}
     * @param variableName         имя переменной хранилища {@link CoreScenario}, в которую необходимо сохранить значение
     */
    @И("^значение из body ответа \"([^\"]+)\" сохранено в переменную \"([^\"]+)\"$")
    public void getValuesFromBodyAsString(String responseNameVariable, String variableName) {
        Response response = getResponse(responseNameVariable);

        coreScenario.setVar(variableName, response.getBody().asString());
        log.trace("Значение: " + response.getBody().asString() + ", записано в переменную: " + variableName);
    }

    /**
     * Получение Cookies из ответа
     *
     * @param responseNameVariable имя переменной которая содержит Response
     * @param dataTable            И в URL, и в значениях в таблице можно использовать переменные и из properties,
     *                             и из хранилища переменных из CoreScenario.
     *                             Для этого достаточно заключить переменные в фигурные скобки, например: http://{hostname}?user={username}.
     */
    @И("^значения из cookies ответа \"([^\"]+)\", сохранены в переменные из таблицы$")
    public void getValuesFromCookiesAsString(String responseNameVariable, DataTable dataTable) {
        Response response = getResponse(responseNameVariable);

        for (List<String> row : dataTable.asLists()) {
            String nameCookies = row.get(0);
            String varName = row.get(1);

            String value = response.cookie(nameCookies);

            if (value == null) {
                throw new RuntimeException("В " + response.getCookies() + " не найдено значение по заданному nameCookies: " + nameCookies);
            }

            coreScenario.setVar(varName, value);
            log.trace("Значение Cookies с именем: " + nameCookies + " с value: " + value + ", записано в переменную: " + varName);
        }
    }

    /**
     * Сравнение в http ответе реальных header с ожидаемыми
     *
     * @param responseNameVariable переменная в которой сохранен Response
     * @param dataTable            массив с параметрами
     */
    @И("^в ответе \"([^\"]+)\" содержатся header со значениями из таблицы$")
    public void checkResponseHeaderValues(String responseNameVariable, DataTable dataTable) {
        Response response = getResponse(responseNameVariable);

        for (List<String> row : dataTable.asLists()) {
            String expectedHeaderName = row.get(0);
            String expectedHeaderValue = row.get(1);

            if (expectedHeaderName.isEmpty() || expectedHeaderValue.isEmpty()) {
                throw new RuntimeException("Header и значение не могут быть пустыми");
            }

            response.then()
                    .assertThat().header(expectedHeaderName, expectedHeaderValue);
        }
    }


    /**
     * Возвращает {@link Response} из хранилища переменных по имени.
     * Бросает осмысленные исключения, если имя пустое или в переменной лежит не {@link Response}.
     *
     * @param responseNameVariable имя переменной, в которой ожидается {@link Response}
     * @return объект {@link Response}
     */
    private Response getResponse(String responseNameVariable) {
        if (responseNameVariable == null || responseNameVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя переменной с Response не может быть null или пустым");
        }

        Object value = coreScenario.getVar(responseNameVariable);
        if (!(value instanceof Response)) {
            throw new IllegalStateException("Переменная '" + responseNameVariable + "' не содержит объект Response");
        }

        return (Response) value;
    }
}
