package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.*;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.WebScenario;
import ru.at.library.web.step.browser.BrowserSteps;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    static void scrollToElementCenter(SelenideElement element) {
        Selenide.executeJavaScript("arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});", element);
    }

    /**
     * Строит человекочитаемое описание контекста списка блоков для сообщений об ошибках.
     * Используется как в шагах проверки количества блоков, так и в сложных поисках.
     */
    private static String buildBlockListContextDescription(String listName, String containerName) {
        StringBuilder description = new StringBuilder();
        description.append("Текущая страница: '")
                .append(WebScenario.getCurrentPage().getName())
                .append("'");

        if (containerName != null) {
            description.append("\nБлок-контейнер: '")
                    .append(containerName)
                    .append("'");
        }

        description.append("\nСписок блоков: '")
                .append(listName)
                .append("'");

        return description.toString();
    }

    private static CorePage findCorePageByCondition(List<CorePage> blocksList,
                                                    String elementName,
                                                    WebElementCondition condition,
                                                    String notFoundMessagePrefix) {
        // Ищем блок, в котором элемент удовлетворяет условию, в течение всего периода
        // Configuration.timeout, перебирая все элементы коллекции. Используем быструю
        // проверку {@code element.is(condition)} без генерации промежуточных ошибок и
        // сами организуем цикл с повторными попытками, чтобы не ждать таймаут по каждому
        // элементу отдельно.
        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        while (true) {
            for (CorePage page : blocksList) {
                SelenideElement element = page.getElement(elementName);
                scrollToElementCenter(element);
                if (element.is(condition)) {
                    return page;
                }
            }

            if (System.currentTimeMillis() >= endTime) {
                // Ни один блок так и не удовлетворил условию за отведённый таймаут — падаем один раз
                BrowserSteps.takeScreenshot();
                throw new AssertionError(
                        notFoundMessagePrefix +
                                "\nРазмер блоков: " + blocksList.size() +
                                "\nСодержимое блоков: " + blockListToString(blocksList)
                );
            }

            // Пауза между повторами перебора, чтобы не крутить цикл слишком агрессивно
            Selenide.sleep(pollingMs);
        }
    }

    @Step("Поиск блока в котором элемента '{elementName}' отображается")
    public static CorePage findCorePageByVisibleElement(List<CorePage> blocksList, String elementName) {
        WebElementCondition condition = Condition.visible;
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " элемент не отображается";
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
    }

    @Step("Поиск блока в котором текст элемента '{elementName}' равен : '{expectedText}'")
    public static CorePage findCorePageByTextInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        WebElementCondition condition = Condition.or("проверка на текст",
                Condition.exactText(expectedText),
                Condition.exactValue(expectedText),
                Condition.attribute("title", expectedText)
        );
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText;
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
    }


    @Step("Поиск блока в котором текст элемента '{elementName}' содержит : '{expectedText}'")
    public static CorePage findCorePageByTextContainInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        WebElementCondition condition = Condition.or("проверка на текст",
                Condition.text(expectedText),
                Condition.value(expectedText),
                Condition.attribute("title", expectedText)
        );
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText;
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
    }


    @Step("Поиск блока в котором текст элемента '{elementName}' соответствует регулярному выражению: '{expectedText}'")
    public static CorePage findCorePageByRegExpInElement(List<CorePage> blocksList, String elementName, String expectedText) {
        WebElementCondition condition = Condition.or("проверка на текст",
                Condition.matchText(expectedText),
                Condition.attributeMatching("value", expectedText),
                Condition.attributeMatching("title", expectedText)
        );
        String notFoundMessage = "Во всех блоках в элементах " + elementName + " не найден текст:" + expectedText;
        return findCorePageByCondition(blocksList, elementName, condition, notFoundMessage);
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
        CorePage currentPage = WebScenario.getCurrentPage();
        ru.at.library.web.scenario.BlocksCollection<? extends CorePage> blocksCollection = currentPage.getBlocksCollection(listName);

        // Ждём нужный размер через стандартные CollectionCondition
        try {
            blocksCollection.getRoots().shouldHave(CustomCondition.getElementsCollectionSizeCondition(comparison, count));
        } catch (AssertionError e) {
            int actualSize = blocksCollection.getRoots().size();
            BrowserSteps.takeScreenshot();
            throw new AssertionError(
                    buildBlockListContextDescription(listName, null) +
                            "\nУсловие по количеству блоков: " + comparison.toString() +
                            "\nОжидаемое количество блоков: " + count +
                            "\nФактическое количество блоков: " + actualSize,
                    e
            );
        }

        // Возвращаем снимок актуального списка блоков
        List<CorePage> result = new ArrayList<>();
        for (CorePage block : blocksCollection) {
            result.add(block);
        }
        return result;
    }


    @Step("В блоке '{blockName}' проверка что количество блоков '{listName}' {comparison} '{count}'")
    public static List<CorePage> getBlockListWithCheckingTheQuantity(String blockName, String listName, CustomCondition.Comparison comparison, int count) {
        CorePage containerBlock = WebScenario.getCurrentPage().getBlock(blockName);
        ru.at.library.web.scenario.BlocksCollection<? extends CorePage> blocksCollection = containerBlock.getBlocksCollection(listName);

        try {
            blocksCollection.getRoots().shouldHave(CustomCondition.getElementsCollectionSizeCondition(comparison, count));
        } catch (AssertionError e) {
            int actualSize = blocksCollection.getRoots().size();
            BrowserSteps.takeScreenshot();
            throw new AssertionError(
                    buildBlockListContextDescription(listName, blockName) +
                            "\nУсловие по количеству блоков: " + comparison.toString() +
                            "\nОжидаемое количество блоков: " + count +
                            "\nФактическое количество блоков: " + actualSize,
                    e
            );
        }

        List<CorePage> result = new ArrayList<>();
        for (CorePage block : blocksCollection) {
            result.add(block);
        }
        return result;
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
        WebElementCondition condition = getSelenideCondition(elementCondition, expectedValue);
        for (CorePage page : blockList) {
            SelenideElement element = page.getElement(elementName);
            if (element.is(Condition.exist)) {
                scrollToElementCenter(element);
                if (element.is(condition)) {
                    resultList.add(page);
                }
            }
        }

        if (resultList.isEmpty()) {
            throw new AssertionError("В списке блоков не найден ни один блок, в котором элемент '" + elementName +
                    "' удовлетворяет условию: " + elementCondition + " '" + expectedValue + "'" +
                    "\nРазмер списка блоков: " + blockList.size() +
                    "\nСодержимое списка блоков:\n" + blockListToString(blockList));
        }
        return resultList;
    }

    public static WebElementCondition getSelenideCondition(String elementCondition, String expectedValue) {
        WebElementCondition condition;
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
