package ru.at.library.api.steps.response;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import ru.at.library.api.helpers.Utils;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.util.List;

/**
 * Шаг для создания JSON/XML на основе шаблонов с подстановкой значений из таблицы.
 */
public class TemplateJsonSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Загружает файл-шаблон, подставляет значения из таблицы и сохраняет результат в переменную.
     * <p>
     * Пример:
     * <pre>
     *   И заполнение JSON-шаблон "templates/request.json" данными из таблицы и сохранение в переменную "body"
     *     | {{name}} | Иван |
     *     | {{age}}  | 30   |
     * </pre>
     *
     * @param type         тип шаблона (JSON или XML) — используется для валидации результата
     * @param templatePath путь к файлу шаблона (файл, property, переменная)
     * @param variableName имя переменной для сохранения заполненного шаблона
     * @param dataTable    таблица: ключ для замены → значение
     */
    @И("^заполнение (JSON|XML)-шаблон \"([^\"]*)\" данными из таблицы и сохранение в переменную \"([^\"]*)\"$")
    public void fillTemplateAndSave(String type, String templatePath, String variableName, DataTable dataTable) {
        String resolvedPath = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(templatePath);
        String content = PropertyLoader.loadValueFromFileOrVariableOrDefault(resolvedPath);

        if (dataTable != null) {
            for (List<String> row : dataTable.asLists()) {
                String placeholder = row.get(0);
                String value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(row.get(1));
                content = content.replaceAll(placeholder, value);
            }
        }

        if ("JSON".equals(type) && !Utils.isJSONValid(content)) {
            throw new IllegalArgumentException("JSON '" + variableName + "' не прошел валидацию:\n" + content);
        }
        if ("XML".equals(type) && !Utils.isXMLValid(content)) {
            throw new IllegalArgumentException("XML '" + variableName + "' не прошел валидацию:\n" + content);
        }

        coreScenario.setVar(variableName, content);
    }
}
