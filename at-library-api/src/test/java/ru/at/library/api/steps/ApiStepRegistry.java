package ru.at.library.api.steps;

import com.google.common.reflect.ClassPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import ru.at.library.api.helpers.TextFormat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реестр публичных шагов API-модуля: все методы с {@code @И} в пакете {@code ru.at.library.api.steps}.
 * <p>
 * Нужен двум потребителям: {@link ExpectedFailureSteps} (вызвать шаг по тексту и убедиться, что он падает)
 * и {@code ApiStepCoverageTest} (убедиться, что у каждого шага есть хотя бы один тест).
 */
public final class ApiStepRegistry {

    private static final String STEPS_PACKAGE = "ru.at.library.api.steps";
    private static final List<StepDef> STEPS = scan();

    private ApiStepRegistry() {
    }

    /** Один шаг: класс, метод и регулярное выражение аннотации. */
    public record StepDef(Class<?> owner, Method method, String expression, Pattern pattern) {
        public String title() {
            return owner.getSimpleName() + "#" + method.getName() + " " + expression;
        }
    }

    /** Найденный по тексту шаг вместе с уже разобранными группами regex. */
    public record Match(StepDef def, Matcher matcher) {

        /**
         * Приводит группы regex и таблицу к типам параметров метода так же, как это делает Cucumber:
         * String, int/Integer, {@link TextFormat}, {@link DataTable}.
         */
        public Object[] arguments(DataTable table) {
            Class<?>[] types = def.method().getParameterTypes();
            List<Object> args = new ArrayList<>();
            int group = 1;
            for (Class<?> type : types) {
                if (type == DataTable.class) {
                    if (table == null) {
                        throw new IllegalArgumentException("Шаг ожидает таблицу, но в тексте её нет: " + def.title());
                    }
                    args.add(table);
                    continue;
                }
                args.add(convert(type, matcher.group(group++)));
            }
            if (group - 1 != matcher.groupCount()) {
                throw new IllegalStateException(String.format(
                        "Число групп regex (%d) не совпадает с числом параметров метода (%d): %s",
                        matcher.groupCount(), group - 1, def.title()));
            }
            if (table != null && !List.of(types).contains(DataTable.class)) {
                throw new IllegalArgumentException("Шаг не принимает таблицу, но она передана: " + def.title());
            }
            return args.toArray();
        }

        private static Object convert(Class<?> type, String raw) {
            if (type == String.class) {
                return raw;
            }
            if (type == int.class || type == Integer.class) {
                return raw == null ? null : Integer.valueOf(raw);
            }
            if (type == TextFormat.class) {
                return TextFormat.valueOf(raw);
            }
            throw new IllegalArgumentException("Неподдерживаемый тип параметра шага: " + type.getName());
        }
    }

    public static List<StepDef> all() {
        return STEPS;
    }

    /**
     * Ищет ровно один шаг, регулярное выражение которого полностью совпадает с текстом.
     * Отсутствие совпадения и неоднозначность — ошибки, поэтому вызов заодно проверяет контракт regex.
     */
    public static Match match(String stepText) {
        List<Match> matches = new ArrayList<>();
        for (StepDef def : STEPS) {
            Matcher matcher = def.pattern().matcher(stepText);
            if (matcher.matches()) {
                matches.add(new Match(def, matcher));
            }
        }
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Не найден шаг API-модуля для текста: " + stepText);
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Текст подходит сразу нескольким шагам: " + stepText + " -> "
                    + matches.stream().map(m -> m.def().title()).toList());
        }
        return matches.get(0);
    }

    private static List<StepDef> scan() {
        try {
            List<StepDef> result = new ArrayList<>();
            ClassPath classPath = ClassPath.from(ApiStepRegistry.class.getClassLoader());
            for (ClassPath.ClassInfo info : classPath.getTopLevelClassesRecursive(STEPS_PACKAGE)) {
                Class<?> owner = info.load();
                for (Method method : owner.getDeclaredMethods()) {
                    И annotation = method.getAnnotation(И.class);
                    if (annotation != null) {
                        result.add(new StepDef(owner, method, annotation.value(), Pattern.compile(annotation.value())));
                    }
                }
            }
            result.sort(Comparator.comparing((StepDef d) -> d.owner().getName()).thenComparing(StepDef::expression));
            return List.copyOf(result);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось просканировать шаги в пакете " + STEPS_PACKAGE, e);
        }
    }
}
