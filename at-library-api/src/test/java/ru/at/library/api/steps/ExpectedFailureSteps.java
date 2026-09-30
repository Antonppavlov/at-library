package ru.at.library.api.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;
import io.cucumber.java.ru.Тогда;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Отрицательные проверки шагов: вызывает шаг API-модуля по его тексту и убеждается, что он завершился ошибкой
 * с ожидаемым фрагментом сообщения.
 * <p>
 * Текст вложенного шага (и, при необходимости, его таблица) передаются в DocString:
 * <pre>
 *   Тогда выполнение шага завершается ошибкой, содержащей "ожидалось":
 *     """
 *     в ответе "r" содержимые найденные по jsonPath равны:
 *     | name | alpha |
 *     """
 * </pre>
 * Шаг ищется по regex реальных аннотаций, поэтому сценарий одновременно проверяет и формулировку шага.
 */
public class ExpectedFailureSteps {

    private static final Pattern CELL_SEPARATOR = Pattern.compile("(?<!\\\\)\\|");

    @Тогда("^выполнение шага завершается ошибкой, содержащей \"(.*)\":?$")
    public void stepFails(String expectedFragment, String stepDocString) throws Exception {
        List<String> lines = stepDocString.lines().map(String::strip).filter(l -> !l.isEmpty()).toList();
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("DocString с текстом шага пуст");
        }
        String stepText = lines.get(0);
        DataTable table = parseTable(lines.subList(1, lines.size()));

        ApiStepRegistry.Match match = ApiStepRegistry.match(stepText);
        Object[] args = match.arguments(table);
        Object instance = match.def().owner().getDeclaredConstructor().newInstance();

        Throwable thrown = null;
        try {
            match.def().method().invoke(instance, args);
        } catch (InvocationTargetException e) {
            thrown = e.getCause();
        }

        if (thrown == null) {
            throw new AssertionError("Шаг должен был завершиться ошибкой, но выполнился успешно: " + stepText);
        }
        String description = describe(thrown);
        if (!description.contains(expectedFragment)) {
            throw new AssertionError(String.format(
                    "Шаг '%s' упал, но не с тем сообщением.%nОжидался фрагмент: %s%nФактически: %s",
                    stepText, expectedFragment, description));
        }
    }

    private static DataTable parseTable(List<String> rowLines) {
        if (rowLines.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        for (String line : rowLines) {
            if (!line.startsWith("|")) {
                throw new IllegalArgumentException("Строка таблицы должна начинаться с '|': " + line);
            }
            String inner = line.substring(1, line.endsWith("|") ? line.length() - 1 : line.length());
            List<String> cells = new ArrayList<>();
            for (String cell : CELL_SEPARATOR.split(inner, -1)) {
                cells.add(cell.strip().replace("\\|", "|"));
            }
            rows.add(cells);
        }
        // Конвертер нужен шагам, которые вызывают dataTable.asLists()
        return DataTable.create(rows, new DataTableTypeRegistryTableConverter(new DataTableTypeRegistry(Locale.ENGLISH)));
    }

    /** Класс и сообщение исключения плюс сообщения всей цепочки причин. */
    private static String describe(Throwable thrown) {
        StringBuilder sb = new StringBuilder();
        for (Throwable t = thrown; t != null; t = t.getCause()) {
            if (sb.length() > 0) {
                sb.append(" <- ");
            }
            sb.append(t.getClass().getName()).append(": ").append(t.getMessage());
            if (t.getCause() == t) {
                break;
            }
        }
        return sb.toString();
    }
}
