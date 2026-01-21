package ru.at.library.web.scenario;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.at.library.web.scenario.annotations.Name;

import java.util.Set;

/**
 * Web-специфичный сценарный контекст для работы со страницами CorePage.
 *
 * Держит реестр страниц в ThreadLocal и инициализирует его, сканируя классы,
 * аннотированные {@link Name} и наследующие {@link CorePage}.
 */
public final class WebScenario {

    private static final Logger log = LogManager.getLogger(WebScenario.class);

    private static final ThreadLocal<Pages> PAGES_HOLDER = ThreadLocal.withInitial(Pages::new);

    private WebScenario() {
    }

    public static Pages getPages() {
        return PAGES_HOLDER.get();
    }

    /**
     * Сканирование всех классов с аннотацией {@link Name} и регистрация их в реестре страниц.
     * Вызывается перед каждым @web-сценарием.
     */
    @SuppressWarnings("unchecked")
    public static void initPages() {
        Pages pages = new Pages();
        Set<Class<?>> annotated = new AnnotationScanner().getClassesAnnotatedWith(Name.class);
        annotated.stream().forEach(clazz -> {
            if (CorePage.class.isAssignableFrom(clazz)) {
                Class<? extends CorePage> pageClass = (Class<? extends CorePage>) clazz;
                Name[] names = pageClass.getAnnotationsByType(Name.class);
                if (names.length == 0) {
                    throw new IllegalStateException("Класс " + pageClass.getName() + " должен быть аннотирован @Name");
                }
                String pageName = names[0].value();
                pages.put(pageName, pageClass);
            } else {
                throw new IllegalStateException("Класс " + clazz.getName() + " должен наследоваться от CorePage");
            }
        });

        // Дополнительная регистрация страниц-примеров, которые могут находиться в тестовых источниках
        registerExamplePages(pages);

        PAGES_HOLDER.set(pages);
        log.debug("Инициализировано {} страниц для web-сценария", annotated.size());
    }

    /**
     * Регистрация example-страниц (из тестовых пакетов), если они присутствуют на classpath.
     * Это нужно, чтобы примеры вроде WikipediaPage корректно работали при запуске тестов библиотеки.
     */
    @SuppressWarnings("unchecked")
    private static void registerExamplePages(Pages pages) {
        try {
            Class<?> wikiClass = Class.forName("ru.at.library.web.page.wiki.WikipediaPage");
            if (CorePage.class.isAssignableFrom(wikiClass)) {
                Class<? extends CorePage> pageClass = (Class<? extends CorePage>) wikiClass;
                pages.put("Википедия", pageClass);
            }
        } catch (ClassNotFoundException ignored) {
            // Страница-пример отсутствует на classpath – ничего страшного
        }
    }

    public static CorePage getCurrentPage() {
        return getPages().getCurrentPage();
    }

    public static void setCurrentPage(CorePage page) {
        getPages().setCurrentPage(page);
    }

    public static CorePage getPage(String name) {
        return getPages().get(name);
    }

    public static <T extends CorePage> T getPage(Class<T> clazz, String name) {
        return getPages().get(clazz, name);
    }
}
