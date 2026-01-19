package ru.at.library.web.step.listcorepage;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import ru.at.library.core.cucumber.api.CorePage;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.core.CustomCondition;
import ru.at.library.web.step.browser.BrowserSteps;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.codeborne.selenide.Selenide.sleep;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.utils.helpers.ScopedVariables.resolveVars;

/**
 * -----------------------------------------------------------------------------------------------------------------
 * -----------------------------------------------Дополнительные методы---------------------------------------------
 * -----------------------------------------------------------------------------------------------------------------
 */
public class ListCorePageOtherMethod {

    @Step("Поиск блока в котором элемента '{elementName}' отображается")
    public static CorePage findCorePageByVisibleElement(List<CorePage> blocksList, String elementName) {

        for (CorePage page : blocksList) {
            SelenideElement element = page.getElement(elementName);
            Selenide.executeJavaScript("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", element);

            boolean expectedTextFind = element.is(Condition.visible);

            if (expectedTextFind) {
                return page;
            }
        }
        BrowserSteps.takeScreenshot();
        //TODO добавить имя блок и имя элемента
        throw new AssertionError(
                "Во всех блоках в элементах " + elementName + " элемент не отображается"
                        + "\nРазмер блоков: " + blocksList.size()
                        + "\nСодержимое блоков: " + blocksList);
    }

    @Step("Поиск блока в котором текст элемента '{elementName}' равен : '{expectedText}'")
    public static CorePage findCorePageByTextInElement(List<CorePage> blocksList, String elementName, String expectedText) {

        for (CorePage page : blocksList) {
            SelenideElement element = page.getElement(elementName);
            Selenide.executeJavaScript("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", element);

            boolean expectedTextFind = element.is(
                    Condition.or("проверка на текст",
                            Condition.exactText(expectedText),
                            Condition.exactValue(expectedText),
                            Condition.attribute("title", expectedText)
                    ));

            if (expectedTextFind) {
                return page;
            }
        }
        BrowserSteps.takeScreenshot();
        //TODO добавить имя блок и имя элемента
        throw new AssertionError(
                "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText
                        + "\nРазмер блоков: " + blocksList.size()
                        + "\nСодержимое блоков: " + blocksList);
    }


    @Step("Поиск блока в котором текст элемента '{elementName}' содержит : '{expectedText}'")
    public static CorePage findCorePageByTextContainInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        for (CorePage page : blocksList) {
            SelenideElement element = page.getElement(elementName);

            Selenide.executeJavaScript("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", element);

            boolean expectedTextFind = element.is(
                    Condition.or("проверка на текст",
                            Condition.text(expectedText),
                            Condition.value(expectedText),
                            Condition.attribute("title", expectedText)
                    ));

            if (expectedTextFind) {
                return page;
            }
        }
        BrowserSteps.takeScreenshot();
        //TODO добавить имя блок и имя элемента
        throw new AssertionError(
                "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText
                        + "\nРазмер блоков: " + blocksList.size()
                        + "\nСодержимое блоков: " + blocksList);
    }


    @Step("Поиск блока в котором текст элемента '{elementName}' соответствует регулярному выражению: '{expectedText}'")
    public static CorePage findCorePageByRegExpInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        for (CorePage page : blocksList) {
            SelenideElement element = page.getElement(elementName);

            Selenide.executeJavaScript("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", element);

            boolean expectedTextFind = element.is(
                    Condition.or("проверка на текст",
                            Condition.matchText(expectedText),
                            Condition.attributeMatching("value", expectedText),
                            Condition.attributeMatching("title", expectedText)
                    ));

            if (expectedTextFind) {
                return page;
            }
        }
        BrowserSteps.takeScreenshot();
        //TODO добавить имя блок и имя элемента
        throw new AssertionError(
                "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText
                        + "\nРазмер блоков: " + blocksList.size()
                        + "\nСодержимое блоков: " + blocksList);
    }


    @Step("ShouldHave что Matching элемента '{elementName}' : '{regExp}'")
    public static void shouldHaveTextMatches(CorePage block, String elementName, String regExp) {
        block.getElement(elementName).shouldHave(Condition.or("Проверка что TextMatches элемента",
                Condition.attributeMatching("value", regExp),
                Condition.attributeMatching("title", regExp),
                Condition.matchText(regExp)
        ));
    }

    @Step("Check что Matching элемента '{elementName}' : '{regExp}'")
    public static boolean checkTextMatches(CorePage block, String elementName, String regExp) {
        return block.getElement(elementName).is(Condition.or("Проверка что TextMatches элемента",
                Condition.attributeMatching("value", regExp),
                Condition.attributeMatching("title", regExp),
                Condition.matchText(regExp)
        ));
    }

    @Step("Проверка что текст элемента '{elementName}' равен: '{expectedText}'")
    public static void checkText(CorePage block, String elementName, String expectedText) {
        SelenideElement element = block.getElement(elementName);

        element.shouldHave(Condition.or("проверка на текст",
                Condition.exactText(expectedText),
                Condition.exactValue(expectedText),
                Condition.attribute("title", expectedText)
        ));
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
        List<CorePage> blocksList;

        int blocksListSize = -1;
        sleep(3000);
        for (int i = 0; i < Configuration.timeout; i = (int) (i + Configuration.pollingInterval)) {
            sleep(100);
            blocksList = CoreScenario.getInstance().getCurrentPage().getBlocksList(listName);
            blocksListSize = blocksList.size();
            if (CustomCondition.comparisonInt(comparison, blocksListSize, count)) {
                return blocksList;
            }
        }

        BrowserSteps.takeScreenshot();
        throw new AssertionError(
                "CurrentPage: '" + CoreScenario.getInstance().getCurrentPage().getName() + "'" +
                        "\nList<CorePage>: '" + listName + "'" +
                        "\nРеальное количество блоков: '" + blocksListSize + "'" +
                        "\nПроверяемое условие: '" + comparison.toString() + "'" +
                        "\nПроверяемое количество: '" + count + "'"
        );
//        String message = String.format("CurrentPage: '%s'\nList<CorePage>: '%s'\nФактическое количество блоков: '%s'\nПроверяемое условие: '%s'\nОжидаемое количество: '%s'\n",
//                CoreScenario.getInstance().getCurrentPage().getName(), listName, blocksListSize, comparison.toString(), count);
//
//        CoreScenario.getInstance().getAssertionHelper()
//                .hamcrestAssert(message, CustomCondition.comparisonInt(comparison, blocksListSize, count), is(equalTo(true)));
//        return blocksList;
    }


    @Step("В блоке '{blockName}' проверка что количество блоков '{listName}' {comparison} '{count}'")
    public static List<CorePage> getBlockListWithCheckingTheQuantity(String blockName, String listName, CustomCondition.Comparison comparison, int count) {
        List<CorePage> blocksList;

        int blocksListSize = -1;
        sleep(3000);
        for (int i = 0; i < Configuration.timeout; i = (int) (i + Configuration.pollingInterval)) {
            sleep(100);
            blocksList = CoreScenario.getInstance().getCurrentPage().getBlock(blockName).getBlocksList(listName);
            blocksListSize = blocksList.size();

            if (CustomCondition.comparisonInt(comparison, blocksListSize, count)) {
                return blocksList;
            }
        }

        BrowserSteps.takeScreenshot();
        throw new AssertionError(
                "CurrentPage: '" + CoreScenario.getInstance().getCurrentPage().getName() + "'" +
                        "\nblockName: '" + blockName + "'" +
                        "\nList<CorePage>: '" + listName + "'" +
                        "\nРеальное количество блоков: '" + blocksListSize + "'" +
                        "\nПроверяемое условие: '" + comparison.toString() + "'" +
                        "\nПроверяемое количество: '" + count + "'"
        );
    }

    @Step("Поиск блока соответствующего условиям")
    public static List<CorePage> getBlockListWithComplexCondition(List<CorePage> blockList, DataTable conditionsTable) {
        validationConditionsTable(conditionsTable);

        List<List<String>> conditionsRows = conditionsTable.asLists();
        List<CorePage> blocksWithElements = new ArrayList<>();
        String resultMessageTemplate = "Найден блок(и) где у элемента %s %s %s :\n%s\n";
        StringBuilder resultMessage = new StringBuilder();

        try {
            for (List<String> conditionsRow : conditionsRows) {

                String elementName = conditionsRow.get(0);
                String elementCondition = conditionsRow.get(1);
                String expectedValue = resolveVars(getPropertyOrStringVariableOrValue(conditionsRow.get(2)));
                if (expectedValue == null) {
                    expectedValue = "";
                }
                if (blocksWithElements.isEmpty()) {
                    blocksWithElements = findCorePageByConditionInElement(blockList, elementName, elementCondition, expectedValue);
                } else {
                    blocksWithElements = findCorePageByConditionInElement(blocksWithElements, elementName, elementCondition, expectedValue);
                }
                resultMessage.append(String.format(resultMessageTemplate, elementName, elementCondition, expectedValue, blockListToString(blocksWithElements)));
            }
        } catch (AssertionError | NoSuchElementException e) {
            if (e instanceof AssertionError) {
                throw new AssertionError(resultMessage + e.getMessage());
            } else throw e;
        }
        return blocksWithElements;
    }

    @Step("поиск блока в котором текст элемента {elementName} {textCondition} {expectedText}")
    private static List<CorePage> findCorePageByConditionInElement(List<CorePage> blockList, String elementName, String elementCondition, String expectedValue) {
        List<CorePage> resultList = new ArrayList<>();
        Condition condition = getSelenideCondition(elementCondition, expectedValue);
        for (CorePage page : blockList) {
            SelenideElement element = page.getElement(elementName);
            if (element.is(Condition.exist)) {
                Selenide.executeJavaScript("arguments[0].scrollIntoView({block: \"center\", inLine: \"center\"});", element);
                if (element.is(condition)) {
                    resultList.add(page);
                }
            }
        }

        if (resultList.isEmpty()) {
            throw new AssertionError("В списке блоков не найден элемент " + elementName
                    + " с проверкой: " + elementCondition + " " + expectedValue
                    + "\nРазмер блоков: " + blockList.size()
                    + "\nСодержимое блоков: " + blockListToString(blockList));
        }
        return resultList;
    }

    public static Condition getSelenideCondition(String elementCondition, String expectedValue) {
        Condition condition;
        switch (elementCondition) {
            case "текст равен": {
                condition = Condition.or("текст элемента равен",
                        Condition.exactText(expectedValue),
                        Condition.exactValue(expectedValue),
                        Condition.attribute("title", expectedValue));
                break;
            }
            case "текст содержит": {
                condition = Condition.or("текст элемента содержит",
                        Condition.text(expectedValue),
                        Condition.value(expectedValue),
                        Condition.attributeMatching("title", expectedValue));
                break;
            }
            case "текст в формате": {
                condition = Condition.or("текст элемента соответствует регулярному выражению",
                        Condition.matchText(expectedValue),
                        Condition.attributeMatching("value", expectedValue),
                        Condition.attributeMatching("title", expectedValue));
                break;
            }
            case "текст не содержит": {
                condition = Condition.not(Condition.or("текст элемента не содержит",
                        Condition.text(expectedValue),
                        Condition.value(expectedValue),
                        Condition.attributeMatching("title", expectedValue)));
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
        int counter = 0;
        for (CorePage block : blockList) {
            sb.append("Блок ").append(counter).append(": ").append(block.getSelf().toString()).append("\n");
            counter++;
        }
        return sb.toString();
    }

    @Step("Валидация таблицы условий")
    private static void validationConditionsTable(DataTable conditionsTable) {
        List<List<String>> conditionsRows = conditionsTable.asLists();

        if (conditionsRows.size() < 1) {
            throw new IllegalArgumentException("Таблица conditionsTable не должна быть пустой!");
        }

        if (conditionsRows.get(0).size() != 3) {
            throw new IllegalArgumentException("Неверный формат условия. Требуемый формат: |<Название элемента>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Ожидаемый текст/регулярное выражение>|");
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
