package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.*;
import com.codeborne.selenide.ex.ElementNotFound;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.scenario.BlocksCollection;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.WebScenario;
import ru.at.library.web.step.browser.BrowserSteps;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.utils.helpers.ScopedVariables.resolveVars;

/**
 * -----------------------------------------------------------------------------------------------------------------
 * -----------------------------------------------Дополнительные методы---------------------------------------------
 * -----------------------------------------------------------------------------------------------------------------
 */
public class BlocksCollectionOtherMethod {

    /**
     * Скроллит указанный элемент в центр видимой области окна с помощью JS.
     * Используется всеми методами поиска/проверки в списках блоков, чтобы избежать дублирования строки
     * с вызовом {@code Selenide.executeJavaScript("arguments[0].scrollIntoView({block: 'center', inline: 'center'});", element)}.
     */
    static boolean scrollToElementCenter(SelenideElement element) {
        try {
            // Проверка намеренно быстрая: общим временем управляет внешний polling deadline.
            if (!element.exists()) {
                return false;
            }
            Selenide.executeJavaScript(
                    "arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});",
                    element
            );
            return true;
        } catch (StaleElementReferenceException | ElementNotFound e) {
            // Фронт перерисовал DOM между exists() и executeJavaScript().
            // Внешний polling-проход заново получит актуальный список блоков.
            return false;
        }
    }

    private static CorePage findCorePageByCondition(List<CorePage> blocksList,
                                                    String elementName,
                                                    WebElementCondition condition,
                                                    String notFoundMessagePrefix) {
        return BlockSearchExecutor.findInSnapshot(
                blocksList,
                elementName,
                condition,
                notFoundMessagePrefix
        );
    }

    @Step("Поиск блока в котором элемента '{elementName}' отображается")
    public static CorePage findCorePageByVisibleElement(List<CorePage> blocksList, String elementName) {
        WebElementCondition condition = Condition.visible;
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " элемент не отображается";
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
    }

    @Step("Поиск блока в котором текст элемента '{elementName}' равен : '{expectedText}'")
    public static CorePage findCorePageByTextInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        WebElementCondition condition = BlockConditions.textEquals(expectedText);
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText;
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
    }


    @Step("Поиск блока в котором текст элемента '{elementName}' содержит : '{expectedText}'")
    public static CorePage findCorePageByTextContainInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        WebElementCondition condition = BlockConditions.textContains(expectedText);
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText;
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
    }


    @Step("Поиск блока в котором текст элемента '{elementName}' соответствует регулярному выражению: '{expectedText}'")
    public static CorePage findCorePageByRegExpInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        WebElementCondition condition = BlockConditions.textMatches(expectedText);
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText;
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
    }


    @Step("ShouldHave что Matching элемента '{elementName}' : '{regExp}'")
    public static void shouldHaveTextMatches(CorePage block, String elementName, String regExp) {
        block.getElement(elementName).shouldHave(BlockConditions.textMatches(regExp));
    }

    @Step("Check что Matching элемента '{elementName}' : '{regExp}'")
    public static boolean checkTextMatches(CorePage block, String elementName, String regExp) {
        return block.getElement(elementName).is(BlockConditions.textMatches(regExp));
    }

    @Step("Проверка что текст элемента '{elementName}' равен: '{expectedText}'")
    public static void checkText(CorePage block, String elementName, String expectedText) {
        SelenideElement element = block.getElement(elementName);

        element.shouldHave(BlockConditions.textEquals(expectedText));
    }


    @Step("Проверка что в элементе: '{elementName}' css: '{cssName}' равен: '{cssValue}'")
    public static void methodCheckHasCssInBlockList(CorePage block, String elementName, String cssName, String cssValue) {
        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.cssValue(cssName, cssValue));
    }


    @Step("Проверка что в элементе: '{elementName}' css: '{cssName}' НЕ равен: '{cssValue}'")
    public static void methodCheckNotHasCssInBlockList(CorePage block, String elementName, String cssName, String cssValue) {
        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldNotHave(Condition.cssValue(cssName, cssValue));
    }


    @Step("Проверка что количество блоков '{listName}' {comparison} '{count}'")
    public static List<CorePage> getBlockListWithCheckingTheQuantity(String listName, CustomCondition.Comparison comparison, int count) {
        BlocksCollection<? extends CorePage> collection =
                WebScenario.getCurrentPage().getBlocksCollection(listName);
        return awaitBlockListSize(collection, listName, null, comparison, count);
    }


    @Step("В блоке '{blockName}' проверка что количество блоков '{listName}' {comparison} '{count}'")
    public static List<CorePage> getBlockListWithCheckingTheQuantity(String blockName, String listName, CustomCondition.Comparison comparison, int count) {
        BlocksCollection<? extends CorePage> collection = WebScenario.getCurrentPage()
                .getBlock(blockName)
                .getBlocksCollection(listName);
        return awaitBlockListSize(collection, listName, blockName, comparison, count);
    }

    private static List<CorePage> awaitBlockListSize(BlocksCollection<? extends CorePage> collection,
                                                     String listName,
                                                     String containerName,
                                                     CustomCondition.Comparison comparison,
                                                     int count) {
        try {
            collection.getRoots().shouldHave(
                    CustomCondition.getElementsCollectionSizeCondition(comparison, count)
            );
        } catch (AssertionError e) {
            int actualSize = collection.getRoots().size();
            BrowserSteps.takeScreenshot();
            throw new AssertionError(
                    BlockListContext.describe(listName, containerName) +
                            "\nУсловие по количеству блоков: " + comparison +
                            "\nОжидаемое количество блоков: " + count +
                            "\nФактическое количество блоков: " + actualSize,
                    e
            );
        }

        List<CorePage> result = new ArrayList<>();
        collection.forEach(result::add);
        return result;
    }

    @Step("Поиск блока соответствующего условиям")
    public static List<CorePage> getBlockListWithComplexCondition(List<CorePage> blockList, DataTable conditionsTable) {
        List<ComplexCondition> conditions = resolveConditions(conditionsTable);
        return BlockSearchExecutor.filterInSnapshot(
                blockList,
                block -> matchesAllConditions(block, conditions),
                complexConditionNotFoundMessage(blockList, conditions)
        );
    }

    static List<CorePage> getBlockListWithComplexCondition(BlockListContext context,
                                                           DataTable conditionsTable,
                                                           Consumer<List<CorePage>> onMatched) {
        List<ComplexCondition> conditions = resolveConditions(conditionsTable);
        return BlockSearchExecutor.filterInContext(
                context,
                block -> matchesAllConditions(block, conditions),
                onMatched,
                "В списке блоков не найден ни один блок, удовлетворяющий всем условиям" +
                        "\n" + context.describe() +
                        "\nУсловия:\n" + conditionsToString(conditions)
        );
    }

    static List<CorePage> getBlockListWithComplexCondition(BlockListContext context,
                                                           DataTable conditionsTable) {
        return getBlockListWithComplexCondition(
                context,
                conditionsTable,
                matchedBlocks -> {
                }
        );
    }

    private static boolean matchesAllConditions(CorePage block,
                                                List<ComplexCondition> conditions) {
        for (int index = 0; index < conditions.size(); index++) {
            int conditionNumber = index + 1;
            ComplexCondition condition = conditions.get(index);
            boolean matched = Allure.step(
                    "Условие №" + conditionNumber + ": " + condition.description(),
                    step -> {
                        SelenideElement element = block.getElement(condition.elementName());
                        if (condition.requiresExistingElement() && !scrollToElementCenter(element)) {
                            step.name("Условие №" + conditionNumber + " — элемент пока недоступен");
                            return false;
                        }

                        boolean conditionMatched = element.is(condition.condition());
                        step.name("Условие №" + conditionNumber +
                                (conditionMatched ? " — выполнено" : " — не выполнено"));
                        return conditionMatched;
                    }
            );
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static List<ComplexCondition> resolveConditions(DataTable conditionsTable) {
        validationConditionsTable(conditionsTable);
        List<ComplexCondition> conditions = new ArrayList<>();

        for (List<String> row : conditionsTable.asLists()) {
            String expectedValue = resolveVars(getPropertyOrStringVariableOrValue(row.get(2)));
            String resolvedValue = expectedValue == null ? "" : expectedValue;
            conditions.add(new ComplexCondition(
                    row.get(0),
                    row.get(1),
                    resolvedValue,
                    getSelenideCondition(row.get(1), resolvedValue)
            ));
        }
        return conditions;
    }

    private static String complexConditionNotFoundMessage(List<CorePage> blockList,
                                                          List<ComplexCondition> conditions) {
        return "В списке блоков не найден ни один блок, удовлетворяющий всем условиям" +
                "\nУсловия:\n" + conditionsToString(conditions) +
                "\nРазмер списка блоков: " + blockList.size();
    }

    private static String conditionsToString(List<ComplexCondition> conditions) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < conditions.size(); index++) {
            result.append(index + 1)
                    .append(". ")
                    .append(conditions.get(index).description())
                    .append('\n');
        }
        return result.toString();
    }

    public static WebElementCondition getSelenideCondition(String elementCondition, String expectedValue) {
        WebElementCondition condition;
        switch (elementCondition) {
            case "текст равен": {
                condition = BlockConditions.textEquals(expectedValue);
                break;
            }
            case "текст содержит": {
                condition = BlockConditions.textContains(expectedValue);
                break;
            }
            case "текст в формате": {
                condition = BlockConditions.textMatches(expectedValue);
                break;
            }
            case "текст не содержит": {
                condition = Condition.not(Condition.or("текст элемента не содержит",
                        Condition.text(expectedValue),
                        Condition.value(expectedValue),
                        Condition.attributeMatching(
                                "title",
                                ".*" + Pattern.quote(expectedValue) + ".*"
                        )));
                break;
            }
            case "содержит css": {
                String[] cssKeyValue = expectedValue.split(";");
                condition = Condition.cssValue(cssKeyValue[0], getPropertyOrStringVariableOrValue(cssKeyValue[1]));
                break;
            }
            case "содержит атрибут": {
                String[] attrKeyValue = expectedValue.split(";");
                condition = attrKeyValue.length == 1
                        ? Condition.attribute(expectedValue)
                        : Condition.attribute(attrKeyValue[0], getPropertyOrStringVariableOrValue(attrKeyValue[1]));
                break;
            }
            case "отображается на странице": {
                condition = Condition.visible;
                break;
            }
            case "не отображается на странице": {
                condition = Condition.hidden;
                break;
            }
            case "не существует на странице": {
                condition = Condition.not(Condition.exist);
                break;
            }
            case "изображение загрузилось": {
                condition = Condition.image;
                break;
            }
            case "доступен для нажатия": {
                condition = Condition.enabled;
                break;
            }
            case "недоступен для нажатия": {
                condition = Condition.disabled;
                break;
            }
            case "псевдо-недоступен": {
                condition = Condition.cssValue("background-color", "rgba(240, 242, 245, 1)");
                break;
            }
            case "не псевдо-недоступен": {
                condition = Condition.not(Condition.cssValue("background-color", "rgba(240, 242, 245, 1)"));
                break;
            }
            case "поле пусто": {
                condition = Condition.empty;
                break;
            }
            case "поле не пусто": {
                condition = Condition.not(Condition.empty);
                break;
            }
            case "в фокусе": {
                condition = Condition.focused;
                break;
            }
            case "только для чтения": {
                condition = Condition.readonly;
                break;
            }
            default:
                throw new IllegalArgumentException(String.format("Отсутствует реализация условия: %s", elementCondition));
        }
        return condition;
    }

    @SuppressWarnings("deprecation")
    public static String blockListToString(List<CorePage> blockList) {
        StringBuilder sb = new StringBuilder();
        int counter = 1;
        for (CorePage block : blockList) {
            sb.append("Блок ")
                    .append(counter)
                    .append(": ")
                    .append(block.getSelf().toString())
                    .append("\n");
            counter++;
        }
        return sb.toString();
    }

    private static void validationConditionsTable(DataTable conditionsTable) {
        List<List<String>> conditionsRows = conditionsTable.asLists();

        if (conditionsRows.isEmpty()) {
            throw new IllegalArgumentException("Таблица conditionsTable не должна быть пустой!");
        }

        for (int index = 0; index < conditionsRows.size(); index++) {
            if (conditionsRows.get(index).size() != 3) {
                throw new IllegalArgumentException(
                        "Неверный формат условия в строке " + (index + 1) +
                                ". Требуемый формат: |<Название элемента>|<Условие>|<Ожидаемое значение>|"
                );
            }
        }
    }

    private record ComplexCondition(String elementName,
                                    String sourceCondition,
                                    String expectedValue,
                                    WebElementCondition condition) {

        private boolean requiresExistingElement() {
            return !"не существует на странице".equals(sourceCondition)
                    && !"не отображается на странице".equals(sourceCondition);
        }

        private String description() {
            return "элемент '" + elementName + "' " + sourceCondition + " '" + expectedValue + "'";
        }
    }

    @Step("Очистка содержимого элемента")
    public static void clearField(SelenideElement element) {
        element.clear();

        if (element.is(Condition.not(Condition.empty))) {
            element.sendKeys(Keys.chord(Keys.CONTROL + "a" + Keys.BACK_SPACE));
        }

        if (element.is(Condition.not(Condition.empty))) {
            for (int i = 0; i < Objects.requireNonNull(element.getValue()).length(); ++i) {
                element.sendKeys(Keys.BACK_SPACE);
            }
        }
    }
}
