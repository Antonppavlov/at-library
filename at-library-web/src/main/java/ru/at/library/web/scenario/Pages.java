package ru.at.library.web.scenario;

import com.codeborne.selenide.Selenide;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Реестр страниц, используемых при прогоне web-тестов.
 */
public final class Pages {

    /**
     * Страницы, на которых будет производится тестирование < Имя, Страница >
     */
    private Map<String, CorePage> pages;

    /**
     * Страница, на которой в текущий момент производится тестирование
     */
    private CorePage currentPage;

    public Pages() {
        pages = Maps.newHashMap();
    }

    /**
     * Реализация анонимных методов со страницей в качестве аргумента.
     *
     * @param clazz                   класс страницы
     * @param checkIfElementsAppeared проверка всех не помеченных "@Optional" элементов
     */
    public static <T extends CorePage> void withPage(Class<T> clazz, boolean checkIfElementsAppeared, Consumer<T> consumer) {
        T page = getPage(clazz, checkIfElementsAppeared);
        consumer.accept(page);
    }

    /**
     * Получение страницы по классу с возможностью выполнить проверку элементов страницы.
     */
    public static <T extends CorePage> T getPage(Class<T> clazz, boolean checkIfElementsAppeared) {
        T page = Selenide.page(clazz);
        if (checkIfElementsAppeared) {
            page.initialize().isAppeared();
        }
        return page;
    }

    /**
     * Возвращает текущую страницу, на которой в текущий момент производится тестирование.
     */
    public CorePage getCurrentPage() {
        if (currentPage == null) throw new IllegalStateException("Текущая страница не задана");
        return currentPage.initialize();
    }

    /**
     * Задает текущую страницу.
     */
    public void setCurrentPage(CorePage page) {
        this.currentPage = page;
    }

    /**
     * Получение страницы из "pages" по имени.
     */
    public CorePage get(String pageName) {
        CorePage pageFromPagesByName = getPageFromPagesByName(pageName);
        CorePage page = Selenide.page(pageFromPagesByName);
        return page.initialize();
    }

    /**
     * Получение страницы по классу и имени.
     */
    @SuppressWarnings("unchecked")
    public <T extends CorePage> T get(Class<T> clazz, String name) {
        CorePage page = Selenide.page(getPageFromPagesByName(name)).initialize();

        if (!clazz.isInstance(page)) {
            throw new IllegalStateException(name + " page is not an instance of " + clazz + ". Named page is a " + page);
        }
        return (T) page;
    }

    private Map<String, CorePage> getPageMapInstanceInternal() {
        return pages;
    }

    private CorePage getPageFromPagesByName(String pageName) throws IllegalArgumentException {
        CorePage page = getPageMapInstanceInternal().get(pageName);
        if (page == null)
            throw new IllegalArgumentException(
                    pageName + " страница не указана в списке доступных страниц\n" +
                            "Список доступных стнаниц: " + getPageMapInstanceInternal().toString());
        return page;
    }

    /**
     * Добавление инстанциированной страницы в "pages" с проверкой на NULL.
     */
    public <T extends CorePage> void put(String pageName, T page) throws IllegalArgumentException {
        if (page == null)
            throw new IllegalArgumentException("Была передана пустая страница");
        pages.put(pageName, page);
    }

    /**
     * Добавление страницы в "pages" по классу.
     */
    @SneakyThrows
    public void put(String pageName, Class<? extends CorePage> page) {
        if (page == null)
            throw new IllegalArgumentException("Была передана пустая страница");
        Constructor<? extends CorePage> constructor = page.getDeclaredConstructor();
        constructor.setAccessible(true);
        CorePage p = page.newInstance();
        p.setName(pageName);
        pages.put(pageName, p);
    }
}
