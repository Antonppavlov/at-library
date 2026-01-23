package ru.at.library.web.scenario;

import org.reflections.Reflections;
import ru.at.library.web.scenario.annotations.Name;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Утилита для поиска классов с заданной аннотацией во всём classpath.
 * <p>В web‑модуле используется для нахождения всех классов, помеченных {@link Name},
 * чтобы затем зарегистрировать их как страницы в {@link Pages} через {@link WebScenario#initPages()}.</p>
 */
public class AnnotationScanner {

    /**
     * Reflections-сканер, ограниченный пространством имён web-модуля.
     *
     * Используем пакет "ru.at.library.web", чтобы находить как production-страницы,
     * так и тестовые страниц/блоки под этим namespace, если они есть на classpath.
     */
    private static final Reflections reflection = new Reflections("");

    public Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotation) {
        return reflection.getTypesAnnotatedWith(annotation);
    }
}
