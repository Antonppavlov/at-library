package ru.at.library.api.helpers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Общая логика шагов «массив значений найденных по jsonPath/yamlPath …».
 * <p>
 * JsonPath/YAML возвращают значения в «родных» типах (Integer, Boolean, Map …), а шаги сравнивают строки.
 * Здесь значения приводятся к строкам явно, а любые нештатные ситуации (нет массива, не массив,
 * элементы-объекты) превращаются в понятный {@link AssertionError} вместо {@link ClassCastException}.
 */
public final class ArrayValues {

    private ArrayValues() {
    }

    /**
     * Проверяет, что по пути найден именно массив.
     *
     * @param found значение, найденное по пути ({@code null}, если пути нет или значение JSON null)
     * @param kind  тип пути для сообщения: {@code jsonPath} или {@code yamlPath}
     * @param path  сам путь
     */
    public static List<Object> requireList(Object found, String kind, String path) {
        if (found == null) {
            throw new AssertionError(String.format("По %s '%s' массив не найден", kind, path));
        }
        if (!(found instanceof List<?> list)) {
            throw new AssertionError(String.format(
                    "По %s '%s' найден не массив, а %s: %s", kind, path, found.getClass().getSimpleName(), found));
        }
        return new ArrayList<>(list);
    }

    /**
     * Приводит скалярные элементы к строкам ({@code 2} -> {@code "2"}, {@code true} -> {@code "true"}).
     * Объекты и вложенные массивы сравнивать со строкой нельзя — в этом случае подсказывается путь до скалярного поля.
     */
    public static List<String> toStrings(List<?> raw, String kind, String path) {
        List<String> result = new ArrayList<>(raw.size());
        for (Object element : raw) {
            if (element instanceof Map || element instanceof Collection) {
                throw new AssertionError(String.format(
                        "Элементы массива по %s '%s' не скалярные (объекты или массивы) и не сравниваются со строкой. "
                                + "Укажите путь до скалярного поля, например '%s.name'. Фактические значения: %s",
                        kind, path, path, raw));
            }
            result.add(element == null ? null : String.valueOf(element));
        }
        return result;
    }

    /**
     * Проверяет упорядоченность массива. Числа сравниваются как числа, остальное — по естественному порядку;
     * {@code null} считается больше любого значения (при сортировке по убыванию — меньше), как и раньше.
     */
    public static boolean isOrdered(List<?> list, boolean ascending, String kind, String path) {
        Comparator<Object> ascendingOrder = Comparator.nullsLast(ArrayValues::compareValues);
        Comparator<Object> order = ascending ? ascendingOrder : ascendingOrder.reversed();
        try {
            for (int i = 1; i < list.size(); i++) {
                if (order.compare(list.get(i - 1), list.get(i)) > 0) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e) {
            throw new AssertionError(String.format(
                    "Элементы массива по %s '%s' нельзя сравнить между собой для проверки сортировки: %s",
                    kind, path, list));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compareValues(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return new BigDecimal(left.toString()).compareTo(new BigDecimal(right.toString()));
        }
        return ((Comparable) left).compareTo(right);
    }
}
