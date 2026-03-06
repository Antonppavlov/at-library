package ru.at.library.api.steps.response;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import lombok.extern.log4j.Log4j2;
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
 * Шаги для проверки и извлечения значений из строковых переменных,
 * содержащих структурированные данные (JSON, XML, URL-параметры).
 * <p>
 * В отличие от {@link JsonResponseSteps}, {@link XmlResponseSteps}, {@link YamlResponseSteps},
 * которые работают с объектом {@code Response}, данный класс работает
 * с произвольными строками, сохранёнными в переменных сценария.
 * <p>
 * Формат данных определяется автоматически или задаётся явно.
 * <p>
 * Поддерживаемые операции сравнения в таблице:
 * <ul>
 *   <li>{@code ==} — точное совпадение</li>
 *   <li>{@code !=} — неравенство</li>
 *   <li>{@code ~}  — совпадение с regex-шаблоном</li>
 *   <li>{@code !~} — несовпадение с regex-шаблоном</li>
 * </ul>
 */
@Log4j2
public class StringContentSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    // =======================================================================
    // ПРОВЕРКА ЗНАЧЕНИЙ
    // =======================================================================

    /**
     * Проверка значений в строковой переменной с автоопределением формата.
     * <p>
     * Пример:
     * <pre>
     *   И в строке "myVar" значения соответствуют таблице:
     *     | $.name   | == | Иван |
     *     | $.age    | != | 0    |
     *     | $.email  | ~  | .+@.+\..+ |
     * </pre>
     *
     * @param variableName             имя переменной или путь к данным
     * @param caseInsensitiveIndicator "без учета регистра " или пусто
     * @param dataTable                таблица: путь, операция (==, !=, ~, !~), ожидаемое значение
     */
    @И("^в строке \"([^\"]+)\" значения ((?:|без учета регистра ))соответствуют таблице:$")
    public void verifyValues(String variableName, String caseInsensitiveIndicator, DataTable dataTable) {
        doVerifyValues(null, variableName, !caseInsensitiveIndicator.isEmpty(), dataTable);
    }

    /**
     * Проверка значений в строковой переменной с явным указанием формата.
     * <p>
     * Пример:
     * <pre>
     *   И в JSON строке "myVar" значения без учета регистра соответствуют таблице:
     *     | $.status | == | ok |
     * </pre>
     *
     * @param format                   ожидаемый формат (JSON, XML, PARAMS)
     * @param variableName             имя переменной или путь к данным
     * @param caseInsensitiveIndicator "без учета регистра " или пусто
     * @param dataTable                таблица: путь, операция (==, !=, ~, !~), ожидаемое значение
     */
    @И("^в ((?:JSON|XML|PARAMS)) строке \"([^\"]+)\" значения ((?:|без учета регистра ))соответствуют таблице:$")
    public void verifyValues(TextFormat format, String variableName, String caseInsensitiveIndicator, DataTable dataTable) {
        doVerifyValues(format, variableName, !caseInsensitiveIndicator.isEmpty(), dataTable);
    }

    // =======================================================================
    // ИЗВЛЕЧЕНИЕ ЗНАЧЕНИЙ
    // =======================================================================

    /**
     * Извлечение значений из строковой переменной с автоопределением формата.
     * <p>
     * Пример:
     * <pre>
     *   И из строки "myVar" извлекаю значения по таблице:
     *     | $.name | savedName |
     *     | $.age  | savedAge  |
     * </pre>
     *
     * @param variableName имя переменной или путь к данным
     * @param dataTable    таблица: путь и имя переменной для сохранения
     */
    @И("^из строки \"([^\"]+)\" извлекаю значения по таблице:$")
    public void extractValues(String variableName, DataTable dataTable) {
        doExtractValues(null, variableName, dataTable);
    }

    /**
     * Извлечение значений из строковой переменной с явным указанием формата.
     * <p>
     * Пример:
     * <pre>
     *   И из XML строки "myVar" извлекаю значения по таблице:
     *     | //name | savedName |
     * </pre>
     *
     * @param format       ожидаемый формат (JSON, XML, PARAMS)
     * @param variableName имя переменной или путь к данным
     * @param dataTable    таблица: путь и имя переменной для сохранения
     */
    @И("^из ((?:JSON|XML|PARAMS)) строки \"([^\"]+)\" извлекаю значения по таблице:$")
    public void extractValues(TextFormat format, String variableName, DataTable dataTable) {
        doExtractValues(format, variableName, dataTable);
    }

    // =======================================================================
    // ВНУТРЕННЯЯ ЛОГИКА
    // =======================================================================

    private void doVerifyValues(TextFormat format, String variableName, boolean caseInsensitive, DataTable dataTable) {
        if (dataTable == null || dataTable.isEmpty()) {
            fail("Таблица с проверками не может быть пустой");
        }

        StringBuilder errors = new StringBuilder();
        FormattedDataContainer data = createContainer(format, variableName);

        int rowIndex = 0;
        for (List<String> row : dataTable.asLists()) {
            rowIndex++;
            if (row == null || row.size() < 3) {
                fail("Ожидается минимум 3 колонки (путь, операция, значение). Строка: " + rowIndex);
            }

            String path = row.get(0);
            String operation = row.get(1);
            String expectedRaw = row.get(2);

            if (path == null || path.trim().isEmpty()) {
                fail("Путь не может быть пустым. Строка: " + rowIndex);
            }
            if (operation == null || operation.trim().isEmpty()) {
                fail("Операция сравнения не может быть пустой. Строка: " + rowIndex);
            }

            Function<String, Matcher<? super String>> matcher = resolveOperation(operation);
            String expectedValue = PropertyLoader.cycleSubstitutionFromFileOrPropertyOrVariable(expectedRaw);
            String actualValue = data.readValue(path);

            if (caseInsensitive) {
                expectedValue = expectedValue.toLowerCase();
                actualValue = actualValue.toLowerCase();
            }

            try {
                assertThat("\nНеверное содержимое элемента: " + path,
                        actualValue, matcher.apply(expectedValue));
            } catch (AssertionError e) {
                errors.append(e.getMessage());
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }

    private void doExtractValues(TextFormat format, String variableName, DataTable dataTable) {
        if (dataTable == null || dataTable.isEmpty()) {
            fail("Таблица с путями для сохранения не может быть пустой");
        }

        StringBuilder errors = new StringBuilder();
        FormattedDataContainer data = createContainer(format, variableName);

        int rowIndex = 0;
        for (List<String> row : dataTable.asLists()) {
            rowIndex++;
            if (row == null || row.size() < 2) {
                fail("Ожидается минимум 2 колонки (путь, переменная). Строка: " + rowIndex);
            }

            String path = row.get(0);
            String targetVariable = row.get(1);

            if (path == null || path.trim().isEmpty()) {
                fail("Путь не может быть пустым. Строка: " + rowIndex);
            }
            if (targetVariable == null || targetVariable.trim().isEmpty()) {
                fail("Имя переменной для сохранения не может быть пустым. Строка: " + rowIndex);
            }

            try {
                String actualValue = data.readValue(path);
                coreScenario.setVar(targetVariable, actualValue);
            } catch (Exception e) {
                errors.append("Не найдено значение: ").append(path).append("\n");
            }
        }

        if (errors.length() > 0) {
            fail(errors.toString());
        }
    }

    /**
     * Определение операции сравнения:
     * {@code ==} — равенство, {@code !=} — неравенство,
     * {@code ~} — regex-совпадение, {@code !~} — regex-несовпадение.
     */
    static Function<String, Matcher<? super String>> resolveOperation(String operationString) {
        if (operationString == null || operationString.trim().isEmpty()) {
            fail("Операция сравнения не может быть null или пустой");
        }

        switch (operationString.trim()) {
            case "==":
                return Matchers::equalTo;
            case "!=":
                return s -> not(equalTo(s));
            case "~":
                return MatchesPattern::matchesPattern;
            case "!~":
                return s -> not(matchesPattern(s));
            default:
                fail("Неизвестная операция сравнения: " + operationString);
                return null; // unreachable
        }
    }

    private FormattedDataContainer createContainer(TextFormat format, String variableName) {
        String valueString = PropertyLoader.cycleSubstitutionFromFileOrPropertyOrVariable(variableName);
        TextFormat resolvedFormat = Utils.defineOrCheckDataFormat(valueString, format);
        return new FormattedDataContainer(resolvedFormat, valueString);
    }
}
