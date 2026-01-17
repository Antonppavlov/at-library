package ru.at.library.web.scenario;

import com.codeborne.selenide.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.at.library.web.scenario.annotations.Name;
import ru.at.library.web.selenide.ElementCheck;
import ru.at.library.web.selenide.IElementCheck;
import ru.at.library.web.selenide.PageElement;
import ru.at.library.web.selenide.PageElement.ElementMode;
import ru.at.library.web.selenide.PageElement.ElementType;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static ru.at.library.core.utils.helpers.PropertyLoader.loadProperty;
import static ru.at.library.web.selenide.ElementChecker.*;

/**
 * Класс для реализации паттерна PageObject (web-специфичный, основан на Selenide).
 */
@Slf4j
public abstract class CorePage {

    public static boolean isAppeared = Boolean.parseBoolean(loadProperty("isAppeared", "false"));
    public static boolean isHidden = Boolean.parseBoolean(loadProperty("isHidden", "true"));
    public static boolean isMandatory = Boolean.parseBoolean(loadProperty("isMandatory", "true"));

    /**
     * Имя страницы
     */
    @Getter
    @Setter
    private String name;

    /**
     * Корневой элемент страницы/блока.
     * Ранее предоставлялся Selenide ElementsContainer#getSelf().
     * По умолчанию используется весь документ (html), если явно не задано.
     */
    private SelenideElement self;

    /**
     * Список всех элементов страницы
     */
    private Map<String, PageElement> namedElements;

    public CorePage() {
        super();
    }

    /**
     * Поиск и инициализации элементов страницы с аннотацией @Name и сбор их в Map<String, PageElement> namedElements
     */
    public CorePage initialize() {
        checkNamedAnnotations();
        namedElements = new HashMap<>();
        Arrays.stream(getClass().getFields())
                .filter(field -> field.getAnnotation(Name.class) != null)
                .peek(this::checkFieldType)
                .forEach(fieldCheckedType -> {
                    String name = fieldCheckedType.getAnnotation(Name.class).value();
                    Object obj = extractFieldValueViaReflection(fieldCheckedType);

                    // Для полей-блоков (наследников CorePage) Selenide не инициализирует значение автоматически.
                    // Если значение поля равно null, создаём экземпляр блока через Selenide.page(...),
                    // чтобы далее можно было работать с ним как с обычной страницей.
                    if (obj == null && CorePage.class.isAssignableFrom(fieldCheckedType.getType())) {
                        obj = com.codeborne.selenide.Selenide.page((Class<? extends CorePage>) fieldCheckedType.getType());
                    }

                    PageElement.ElementType type = PageElement.ElementType.getType(obj);
                    PageElement pageElement = new PageElement(obj, name, type, PageElement.ElementMode.getMode(fieldCheckedType));
                    namedElements.put(name, pageElement);
                });
        return this;
    }

    /**
     * Получение элемента со страницы по имени (аннотированного "Name")
     */
    public SelenideElement getElement(String elementName) {
        return castToSelenideElement(Optional.ofNullable(namedElements.get(elementName))
                .orElseThrow(() -> new IllegalArgumentException("SelenideElement " + elementName + " не описан на странице " + this.getClass().getName()))
                .getElement());
    }

    /**
     * Получение элемента-списка со страницы по имени
     */
    public ElementsCollection getElementsList(String listName) {
        return castToElementsCollection(Optional.ofNullable(namedElements.get(listName))
                .orElseThrow(() -> new IllegalArgumentException("ElementsCollection " + listName + " не описан на странице " + this.getClass().getName()))
                .getElement());
    }

    /**
     * Получение блока со страницы по имени (аннотированного "Name")
     */
    public CorePage getBlock(String blockName) {
        return castToCorePage(Optional.ofNullable(namedElements.get(blockName))
                .orElseThrow(() -> new IllegalArgumentException("CorePage " + blockName + " не описан на странице " + this.getClass().getName()))
                .getElement());
    }

    /**
     * Получение списка блоков со страницы по имени (аннотированного "Name")
     */
    @SuppressWarnings("unchecked")
    public List<CorePage> getBlocksList(String listCorePage) {
        Object value = namedElements.get(listCorePage).getElement();
        if (!(value instanceof List)) {
            throw new IllegalArgumentException("List<CorePage> " + listCorePage + " не описан на странице " + this.getClass().getName());
        }
        Stream<Object> stream = ((List<Object>) value).stream();

        return stream.map(CorePage::castToCorePage).collect(toList());
    }

    /**
     * Проверка того, что элементы, не помеченные аннотацией "Optional", отображаются,
     * а элементы, помеченные аннотацией "Hidden", скрыты.
     */
    public void isAppeared() {
        if (isMandatory){
            checkMandatory();
        }
        if (isHidden){
            checkHidden();
        }
        if (isAppeared){
            checkPrimary(!isMandatory);
        }
    }

    /**
     * Проверка, что все (SelenideElement/ElementCollection/Наследники CorePage) на странице исчезли.
     */
    public void isDisappeared() {
        List<PageElement.ElementMode> modesToCheck = Arrays.asList(PageElement.ElementMode.MANDATORY, PageElement.ElementMode.PRIMARY, PageElement.ElementMode.HIDDEN, ElementMode.OPTIONAL);
        List<IElementCheck> elementChecks = buildElementChecksForModes(
                modesToCheck,
                modesToCheck,
                Condition.hidden,
                "не отображается на странице");

        List<IElementCheck> checkResult = checkElements(elementChecks, Configuration.timeout);
        attachCheckListResults("Успешные проверки исчезновения элементов", checkResult, true);
        // Используем CoreScenario только для AssertionHelper (ядро остаётся общим)
        ru.at.library.core.cucumber.api.CoreScenario.getInstance().getAssertionHelper()
                .hamcrestAssert(
                        String.format("На текущей странице не исчезли все описанные на странице элементы: %d из %d\n%s",
                                getFailedCheckList(checkResult).size(), elementChecks.size(), elementCheckListAsString(getFailedCheckList(checkResult))),
                        checkResult.stream().allMatch(IElementCheck::getStatus),
                        is(equalTo(true))
                );
    }

    /**
     * Проверка, что все (SelenideElement/ElementCollection/Наследники CorePage) c аннотацией Mandatory отображаются на странице.
     */
    public void checkMandatory() {
        List<ElementMode> parentModesToCheck = Collections.singletonList(ElementMode.MANDATORY);
        List<ElementMode> childModesToCheck = Arrays.asList(ElementMode.MANDATORY, ElementMode.PRIMARY);
        List<IElementCheck> elementChecks = buildElementChecksForModes(
                parentModesToCheck,
                childModesToCheck,
                Condition.appear,
                "отображается на странице");

        List<IElementCheck> checkResult = checkElements(elementChecks, Configuration.timeout);
        attachCheckListResults("Успешные проверки обязательных элементов", checkResult, true);
        if (!checkResult.stream().allMatch(IElementCheck::getStatus)) {
            throw new AssertionError(String.format("На текущей странице не отобразились все обязательные элементы: %d из %d\n%s",
                    getFailedCheckList(checkResult).size(), elementChecks.size(), elementCheckListAsString(getFailedCheckList(checkResult))));
        }
    }

    /**
     * Проверка, что все (SelenideElement/ElementCollection/Наследники CorePage) c аннотацией Hidden не отображаются на странице.
     */
    public void checkHidden() {
        List<ElementMode> modesToCheck = Collections.singletonList(ElementMode.HIDDEN);
        List<IElementCheck> elementChecks = buildElementChecksForModes(
                modesToCheck,
                modesToCheck,
                Condition.hidden,
                "не отображается на странице");

        List<IElementCheck> checkResult = checkElements(elementChecks, Configuration.timeout);
        attachCheckListResults("Успешные проверки скрытых элементов", checkResult, true);
        ru.at.library.core.cucumber.api.CoreScenario.getInstance().getAssertionHelper()
                .hamcrestAssert(
                        String.format("На текущей странице не исчезли все элементы помеченные Hidden: %d из %d\n%s",
                                getFailedCheckList(checkResult).size(), elementChecks.size(), elementCheckListAsString(getFailedCheckList(checkResult))),
                        checkResult.stream().allMatch(IElementCheck::getStatus),
                        is(equalTo(true))
                );
    }

    /**
     * Проверка, что все (SelenideElement/ElementCollection/Наследники CorePage) без аннотации Hidden/Optional отображаются на странице.
     */
    public void checkPrimary(boolean includeMandatory) {
        List<ElementMode> parentModesToCheck = includeMandatory
                ? Arrays.asList(ElementMode.MANDATORY, ElementMode.PRIMARY)
                : Collections.singletonList(ElementMode.PRIMARY);
        List<ElementMode> childModesToCheck = Arrays.asList(ElementMode.MANDATORY, ElementMode.PRIMARY);
        List<IElementCheck> elementChecks = buildElementChecksForModes(
                parentModesToCheck,
                childModesToCheck,
                Condition.appear,
                "отображается на странице");

        List<IElementCheck> checkResult = checkElements(elementChecks, Configuration.timeout);
        attachCheckListResults("Успешные проверки основных элементов", checkResult, true);
        ru.at.library.core.cucumber.api.CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("На текущей странице не отобразились все основные элементы: %d из %d\n%s",
                        getFailedCheckList(checkResult).size(), elementChecks.size(), elementCheckListAsString(getFailedCheckList(checkResult))),
                checkResult.stream().allMatch(IElementCheck::getStatus),
                is(equalTo(true)));
    }

    public List<PageElement> getElementsWithModes(List<ElementMode> parentElementsModes, List<ElementMode> childElementsModes) {
        return namedElements.values().stream()
                .filter(pageElement -> pageElement.checkMode(parentElementsModes))
                .flatMap(elementWithMode -> elementWithMode.getType().equals(ElementType.LIST_CORE_PAGE)
                        ? ((List<?>) elementWithMode.getElement()).stream()
                            .map(subElement -> new PageElement(subElement, elementWithMode.getName(), ElementType.CORE_PAGE, elementWithMode.getMode()))
                        : Stream.of(elementWithMode))
                .flatMap(v -> v.getType().equals(ElementType.CORE_PAGE)
                        ? castToCorePage(v.getElement()).getElementsWithModes(childElementsModes, childElementsModes).stream()
                        : Stream.of(v))
                .collect(toList());
    }

    private List<IElementCheck> buildElementChecksForModes(List<ElementMode> parentElementsModes,
                                                                                             List<PageElement.ElementMode> childElementsModes,
                                                                                             com.codeborne.selenide.WebElementCondition condition,
                                                                                             String message) {
        List<PageElement> elements = getElementsWithModes(parentElementsModes, childElementsModes);
        return pageElementToElementCheck(elements, condition, message);
    }

    public List<IElementCheck> pageElementToElementCheck(Collection<PageElement> values, com.codeborne.selenide.WebElementCondition condition, String message) {
        return values.stream()
                .map(pageElement ->
                        pageElementToElementCheck(pageElement, condition, format("Элемент '%s' %s", pageElement.getName(), message))
                ).collect(toList());
    }

    public ElementCheck pageElementToElementCheck(PageElement pageElement, com.codeborne.selenide.WebElementCondition condition, String message) {
        SelenideElement element = pageElement.getType().equals(ElementType.ELEMENTS_COLLECTION)
                ? ((ElementsCollection) pageElement.getElement()).first()
                : castToSelenideElement(pageElement.getElement());
        return new ElementCheck(pageElement.getName(), element, condition, message);
    }

    private void checkFieldType(Field f) {
        Class<?> fieldType = f.getType();
        if (SelenideElement.class.isAssignableFrom(fieldType) || CorePage.class.isAssignableFrom(fieldType)) {
            return;
        }
        if (ElementsCollection.class.isAssignableFrom(fieldType)) {
            return;
        }
        if (List.class.isAssignableFrom(fieldType)) {
            ParameterizedType listType = (ParameterizedType) f.getGenericType();
            Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
            if (SelenideElement.class.isAssignableFrom(listClass) || CorePage.class.isAssignableFrom(listClass)) {
                return;
            }
        }
        throw new IllegalStateException(
                format("Поле с аннотацией @Name должно иметь тип SelenideElement или ElementsCollection.\n" +
                        "Если поле описывает блок, оно должно принадлежать классу, унаследованному от CorePage.\n" +
                        "Найдено поле с типом %s", f.getType()));
    }

    private void checkNamedAnnotations() {
        Set<String> uniques = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        Arrays.stream(getClass().getFields())
                .filter(f -> f.getAnnotation(Name.class) != null)
                .map(f -> f.getAnnotation(Name.class).value())
                .forEach(name -> {
                    if (!uniques.add(name)) {
                        duplicates.add(name);
                    }
                });
        if (!duplicates.isEmpty()) {
            throw new IllegalStateException(String.format("Найдено несколько аннотаций @Name с одинаковым значением в классе %s\nДубликаты: %s", this.getClass().getName(), duplicates));
        }
    }

    private Object extractFieldValueViaReflection(Field field) {
        return Reflection.extractFieldValue(field, this);
    }

    private SelenideElement castToSelenideElement(Object element) {
        if (element == null) {
            throw new IllegalArgumentException("Object is null и не может быть приведён к SelenideElement");
        }
        if (!(element instanceof SelenideElement)) {
            throw new IllegalArgumentException("Object: " + element.getClass() + " не является объектом SelenideElement");
        }
        return (SelenideElement) element;
    }

    private ElementsCollection castToElementsCollection(Object list) {
        if (list == null) {
            throw new IllegalArgumentException("Object is null и не может быть приведён к ElementsCollection");
        }
        if (!(list instanceof ElementsCollection)) {
            throw new IllegalArgumentException("Object: " + list.getClass() + " не является объектом ElementsCollection");
        }
        return (ElementsCollection) list;
    }

    private static CorePage castToCorePage(Object corePage) {
        if (corePage == null) {
            throw new IllegalArgumentException("Object is null и не может быть приведён к CorePage. " +
                    "Вероятно, блок не был инициализирован. Проверьте, что поле-блок корректно описано и страница загружена перед обращением к нему.");
        }
        if (!(corePage instanceof CorePage)) {
            throw new IllegalArgumentException("Object: " + corePage.getClass() + " не является объектом CorePage");
        }
        return Selenide.page((CorePage) corePage).initialize();
    }

    /**
     * Возвращает корневой элемент страницы/блока.
     * Если явно не задано (self == null), используется весь документ (html).
     */
    public SelenideElement getSelf() {
        return self != null ? self : Selenide.$("html");
    }

    /**
     * Позволяет явно задать корневой элемент страницы/блока.
     * Это может быть полезно для вложенных блоков.
     */
    public void setSelf(SelenideElement self) {
        this.self = self;
    }
}
