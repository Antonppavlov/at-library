package ru.at.library.web.scenario;

import org.reflections.Reflections;
import ru.at.library.web.scenario.annotations.Name;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Поиск классов с заданной аннотацией среди всех классов в проекте на основе механизма рефлексии.
 * Web-специфичная версия (используется для поиска страниц, аннотированных {@link Name}).
 */
public class AnnotationScanner {

    /**
     * Reflections-сканер, ограниченный пространством имён web-модуля.
     *
     * Используем пакет "ru.at.library.web", чтобы находить как production-страницы,
     * так и тестовые страниц/блоки под этим namespace, если они есть на classpath.
     */
    private static final Reflections reflection = new Reflections("ru.at.library.web");

    public Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotation) {
        return reflection.getTypesAnnotatedWith(annotation);
    }
}
