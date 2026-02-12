package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.entities.BlockListStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.IStepResult;

import java.util.List;
import java.util.stream.Collectors;

import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.*;

/**
 * Шаги-действия для работы со списками блоков (List<CorePage>), построенные поверх {@link BlockListContext}.
 *
 * Все публичные Cucumber-методы остаются без изменений; общая логика выбора блока/элемента вынесена
 * во внутренние helper-методы с понятными именами (clickBlockWhere..., clickElementInBlockWhere..., и т.д.),
 * чтобы избежать дублирования и упростить сопровождение.
 */
public class BlocksCollectionActionSteps {

    /**
     * -----------------------------------------------------------------------------------------------------------------
     * -----------------------------------------------Действия списка блоков------------------------------------------------
     * -----------------------------------------------------------------------------------------------------------------
     */

    private IStepResult clickBlockWhereTextEquals(String blockListName,
                                                  String elementNameSearch,
                                                  String expectedTextSearch) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = waitUntilBlockWhereTextEquals(blockListName, elementNameSearch, resolvedExpectedText);
        SelenideElement root = block.getSelf();
        root.shouldHave(Condition.visible);
        root.hover();
        root.click();

        return new BlockListStepResult(block, elementNameSearch);
    }

    private IStepResult clickBlockWhereTextEquals(String blockName,
                                                  String blockListName,
                                                  String elementNameSearch,
                                                  String expectedTextSearch) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = waitUntilBlockWhereTextEquals(blockName, blockListName, elementNameSearch, resolvedExpectedText);
        SelenideElement root = block.getSelf();
        root.shouldHave(Condition.visible);
        root.hover();
        root.click();

        return new BlockListStepResult(block, elementNameSearch);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    public IStepResult clickBlockInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch) {
        return clickBlockWhereTextEquals(blockListName, elementNameSearch, expectedTextSearch);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    public IStepResult clickBlockInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch) {
        return clickBlockWhereTextEquals(blockName, blockListName, elementNameSearch, expectedTextSearch);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult clickElementInBlockWhereTextEquals(String blockListName,
                                                           String elementNameSearch,
                                                           String expectedTextSearch,
                                                           String elementNameClick) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = waitUntilBlockWhereTextEquals(blockListName, elementNameSearch, resolvedExpectedText);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    private IStepResult clickElementInBlockWhereTextEquals(String blockName,
                                                           String blockListName,
                                                           String elementNameSearch,
                                                           String expectedTextSearch,
                                                           String elementNameClick) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = waitUntilBlockWhereTextEquals(blockName, blockListName, elementNameSearch, resolvedExpectedText);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return clickElementInBlockWhereTextEquals(blockListName, elementNameSearch, expectedTextSearch, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return clickElementInBlockWhereTextEquals(blockName, blockListName, elementNameSearch, expectedTextSearch, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */


    private IStepResult clickElementInBlockWhereElementVisible(String blockListName,
                                                               String elementNameSearch,
                                                               String elementNameClick) {
        CorePage block = waitUntilBlockWhereElementVisible(blockListName, elementNameSearch);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    private IStepResult clickElementInBlockWhereElementVisible(String blockName,
                                                               String blockListName,
                                                               String elementNameSearch,
                                                               String elementNameClick) {
        CorePage block = waitUntilBlockWhereElementVisible(blockName, blockListName, elementNameSearch);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @То("^в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereElementVisible(String blockListName, String elementNameSearch, String elementNameClick) {
        return clickElementInBlockWhereElementVisible(blockListName, elementNameSearch, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereElementVisible(String blockName, String blockListName, String elementNameSearch, String elementNameClick) {
        return clickElementInBlockWhereElementVisible(blockName, blockListName, elementNameSearch, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult hoverOnElementInBlockWhereTextEquals(String blockListName,
                                                             String elementNameSearch,
                                                             String expectedTextSearch,
                                                             String elementNameClick) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = waitUntilBlockWhereTextEquals(blockListName, elementNameSearch, resolvedExpectedText);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    private IStepResult hoverOnElementInBlockWhereTextEquals(String blockName,
                                                             String blockListName,
                                                             String elementNameSearch,
                                                             String expectedTextSearch,
                                                             String elementNameClick) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = waitUntilBlockWhereTextEquals(blockName, blockListName, elementNameSearch, resolvedExpectedText);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    public IStepResult hoverOnElementInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return hoverOnElementInBlockWhereTextEquals(blockListName, elementNameSearch, expectedTextSearch, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    public IStepResult hoverOnElementInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return hoverOnElementInBlockWhereTextEquals(blockName, blockListName, elementNameSearch, expectedTextSearch, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult inputValueInBlockWhereTextEquals(String blockListName,
                                                         String elementNameSearch,
                                                         String expectedTextSearch,
                                                         String elementName,
                                                         String inputText,
                                                         boolean useClearField) throws Exception {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedInputText = OtherSteps.getPropertyOrStringVariableOrValue(inputText);

        CorePage block = waitUntilBlockWhereTextEquals(blockListName, elementNameSearch, resolvedExpectedText);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.visible);
        element.click();
        if (useClearField) {
            clearField(element);
        } else {
            element.clear();
        }
        element.sendKeys(resolvedInputText);

        return new BlockListStepResult(block, elementNameSearch, elementName);
    }

    private IStepResult inputValueInBlockWhereTextEquals(String blockName,
                                                         String blockListName,
                                                         String elementNameSearch,
                                                         String expectedTextSearch,
                                                         String elementName,
                                                         String inputText,
                                                         boolean useClearField) throws Exception {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedInputText = OtherSteps.getPropertyOrStringVariableOrValue(inputText);

        CorePage block = waitUntilBlockWhereTextEquals(blockName, blockListName, elementNameSearch, resolvedExpectedText);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.visible);
        element.click();
        if (useClearField) {
            clearField(element);
        } else {
            element.clear();
        }
        element.sendKeys(resolvedInputText);

        return new BlockListStepResult(block, elementNameSearch, elementName);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public IStepResult inputValueInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementName, String inputText) throws Exception {
        return inputValueInBlockWhereTextEquals(blockListName, elementNameSearch, expectedTextSearch, elementName, inputText, true);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public IStepResult inputValueInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementName, String inputText) throws Exception {
        return inputValueInBlockWhereTextEquals(blockName, blockListName, elementNameSearch, expectedTextSearch, elementName, inputText, false);
    }

    /**
     * ######################################################################################################################
     */

    private CorePage waitUntilBlockWhereTextEquals(String blockListName,
                                                   String elementNameSearch,
                                                   String resolvedExpectedText) {
        WebElementCondition condition = Condition.or("проверка на текст",
                Condition.exactText(resolvedExpectedText),
                Condition.exactValue(resolvedExpectedText),
                Condition.attribute("title", resolvedExpectedText)
        );

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        List<CorePage> lastBlocks = null;

        while (true) {
            BlockListContext ctx = BlockListContext.fromList(blockListName);

            List<CorePage> blocks = ctx.getBlocks();
            lastBlocks = blocks;

            for (CorePage page : blocks) {
                SelenideElement element = page.getElement(elementNameSearch);
                scrollToElementCenter(element);
                if (element.is(condition)) {
                    element.should(condition);
                    return page;
                }
            }

            if (System.currentTimeMillis() >= endTime) {
                String notFoundMessage = "Во всех блоках в элементах " + elementNameSearch +
                        " не найден текст:" + resolvedExpectedText;
                if (lastBlocks != null) {
                    notFoundMessage += "\nРазмер блоков: " + lastBlocks.size() +
                            "\nСодержимое блоков: " + blockListToString(lastBlocks);
                }
                throw new AssertionError(notFoundMessage);
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    private CorePage waitUntilBlockWhereTextEquals(String blockName,
                                                   String blockListName,
                                                   String elementNameSearch,
                                                   String resolvedExpectedText) {
        WebElementCondition condition = Condition.or("проверка на текст",
                Condition.exactText(resolvedExpectedText),
                Condition.exactValue(resolvedExpectedText),
                Condition.attribute("title", resolvedExpectedText)
        );

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        List<CorePage> lastBlocks = null;

        while (true) {
            BlockListContext ctx = BlockListContext.fromBlock(blockName, blockListName);

            List<CorePage> blocks = ctx.getBlocks();
            lastBlocks = blocks;

            for (CorePage page : blocks) {
                SelenideElement element = page.getElement(elementNameSearch);
                scrollToElementCenter(element);
                if (element.is(condition)) {
                    element.should(condition);
                    return page;
                }
            }

            if (System.currentTimeMillis() >= endTime) {
                String notFoundMessage = "Во всех блоках в элементах " + elementNameSearch +
                        " не найден текст:" + resolvedExpectedText;
                if (lastBlocks != null) {
                    notFoundMessage += "\nРазмер блоков: " + lastBlocks.size() +
                            "\nСодержимое блоков: " + blockListToString(lastBlocks);
                }
                throw new AssertionError(notFoundMessage);
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    private CorePage waitUntilBlockWhereElementVisible(String blockListName,
                                                       String elementNameSearch) {
        WebElementCondition condition = Condition.visible;

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        List<CorePage> lastBlocks = null;

        while (true) {
            BlockListContext ctx = BlockListContext.fromList(blockListName);

            List<CorePage> blocks = ctx.getBlocks();
            lastBlocks = blocks;

            for (CorePage page : blocks) {
                SelenideElement element = page.getElement(elementNameSearch);
                scrollToElementCenter(element);
                if (element.is(condition)) {
                    element.shouldBe(condition);
                    return page;
                }
            }

            if (System.currentTimeMillis() >= endTime) {
                String notFoundMessage = "Во всех блоках элемент '" + elementNameSearch +
                        "' не стал видимым в течение таймаута " + timeoutMs + " мс";
                if (lastBlocks != null) {
                    notFoundMessage += "\nРазмер блоков: " + lastBlocks.size() +
                            "\nСодержимое блоков: " + blockListToString(lastBlocks);
                }
                throw new AssertionError(notFoundMessage);
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    private CorePage waitUntilBlockWhereElementVisible(String blockName,
                                                       String blockListName,
                                                       String elementNameSearch) {
        WebElementCondition condition = Condition.visible;

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        List<CorePage> lastBlocks = null;

        while (true) {
            BlockListContext ctx = BlockListContext.fromBlock(blockName, blockListName);

            List<CorePage> blocks = ctx.getBlocks();
            lastBlocks = blocks;

            for (CorePage page : blocks) {
                SelenideElement element = page.getElement(elementNameSearch);
                scrollToElementCenter(element);
                if (element.is(condition)) {
                    element.shouldBe(condition);
                    return page;
                }
            }

            if (System.currentTimeMillis() >= endTime) {
                String notFoundMessage = "Во всех блоках элемент '" + elementNameSearch +
                        "' не стал видимым в течение таймаута " + timeoutMs + " мс";
                if (lastBlocks != null) {
                    notFoundMessage += "\nРазмер блоков: " + lastBlocks.size() +
                            "\nСодержимое блоков: " + blockListToString(lastBlocks);
                }
                throw new AssertionError(notFoundMessage);
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    private IStepResult clickOnElementInBlockByNumber(String blockListName, int blockNumber, String elementNameClick) {
        CorePage block = waitUntilElementInBlockByNumberClickable(blockListName, blockNumber, elementNameClick);
        return new BlockListStepResult(block, elementNameClick);
    }

    private IStepResult clickOnElementInBlockByNumber(String blockName, String blockListName, int blockNumber, String elementNameClick) {
        CorePage block = waitUntilElementInBlockByNumberClickable(blockName, blockListName, blockNumber, elementNameClick);
        return new BlockListStepResult(block, elementNameClick);
    }

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickOnElementBlockInBlockList(String blockListName, int blockNumber, String elementNameClick) {
        return clickOnElementInBlockByNumber(blockListName, blockNumber, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickOnElementBlockInBlockList(String blockName, String blockListName, int blockNumber, String elementNameClick) {
        return clickOnElementInBlockByNumber(blockName, blockListName, blockNumber, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult clickOnBlockByNumber(BlockListContext blockListContext, int blockNumber) {
        String listName = blockListContext.getListName();
        String containerName = blockListContext.getContainerName();

        CorePage block = containerName == null
                ? waitUntilBlockByNumberClickable(listName, blockNumber)
                : waitUntilBlockByNumberClickable(containerName, listName, blockNumber);

        return new BlockListStepResult(block);
    }

    private CorePage waitUntilElementInBlockByNumberClickable(String blockListName,
                                                              int blockNumber,
                                                              String elementNameClick) {
        if (blockNumber < 1) {
            throw new IllegalArgumentException("Индекс блока должен начинаться с 1, получено: " + blockNumber);
        }

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        // считаем, что "кликабелен" = видим и enabled
        WebElementCondition clickable = Condition.and("кликабелен", Condition.visible, Condition.enabled);

        while (true) {
            BlockListContext ctx = BlockListContext.fromList(blockListName);
            CorePage block = ctx.getBlockByNumber(blockNumber);
            SelenideElement element = block.getElement(elementNameClick);

            if (element.is(clickable)) {
                element.shouldBe(clickable).click();
                return block;
            }

            if (System.currentTimeMillis() >= endTime) {
                throw new AssertionError("Не удалось дождаться кликабельности элемента '" + elementNameClick + "' в " +
                        blockNumber + " блоке списка блоков '" + blockListName + "' в течение таймаута " + timeoutMs + " мс");
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    private CorePage waitUntilElementInBlockByNumberClickable(String blockName,
                                                              String blockListName,
                                                              int blockNumber,
                                                              String elementNameClick) {
        if (blockNumber < 1) {
            throw new IllegalArgumentException("Индекс блока должен начинаться с 1, получено: " + blockNumber);
        }

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        WebElementCondition clickable = Condition.and("кликабелен", Condition.visible, Condition.enabled);

        while (true) {
            BlockListContext ctx = BlockListContext.fromBlock(blockName, blockListName);
            CorePage block = ctx.getBlockByNumber(blockNumber);
            SelenideElement element = block.getElement(elementNameClick);

            if (element.is(clickable)) {
                element.shouldBe(clickable).click();
                return block;
            }

            if (System.currentTimeMillis() >= endTime) {
                throw new AssertionError("Не удалось дождаться кликабельности элемента '" + elementNameClick + "' в " +
                        blockNumber + " блоке списка блоков '" + blockListName + "' внутри блока '" + blockName +
                        "' в течение таймаута " + timeoutMs + " мс");
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    private CorePage waitUntilBlockByNumberClickable(String blockListName,
                                                     int blockNumber) {
        if (blockNumber < 1) {
            throw new IllegalArgumentException("Индекс блока должен начинаться с 1, получено: " + blockNumber);
        }

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        WebElementCondition clickable = Condition.and("кликабелен", Condition.visible, Condition.enabled);

        while (true) {
            BlockListContext ctx = BlockListContext.fromList(blockListName);
            CorePage block = ctx.getBlockByNumber(blockNumber);
            SelenideElement self = block.getSelf();

            if (self.is(clickable)) {
                self.shouldBe(clickable).click();
                return block;
            }

            if (System.currentTimeMillis() >= endTime) {
                throw new AssertionError("Не удалось дождаться кликабельности " + blockNumber +
                        " блока списка блоков '" + blockListName + "' в течение таймаута " + timeoutMs + " мс");
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    private CorePage waitUntilBlockByNumberClickable(String blockName,
                                                     String blockListName,
                                                     int blockNumber) {
        if (blockNumber < 1) {
            throw new IllegalArgumentException("Индекс блока должен начинаться с 1, получено: " + blockNumber);
        }

        long timeoutMs = com.codeborne.selenide.Configuration.timeout;
        long pollingMs = com.codeborne.selenide.Configuration.pollingInterval;
        long endTime = System.currentTimeMillis() + timeoutMs;

        WebElementCondition clickable = Condition.and("кликабелен", Condition.visible, Condition.enabled);

        while (true) {
            BlockListContext ctx = BlockListContext.fromBlock(blockName, blockListName);
            CorePage block = ctx.getBlockByNumber(blockNumber);
            SelenideElement self = block.getSelf();

            if (self.is(clickable)) {
                self.shouldBe(clickable).click();
                return block;
            }

            if (System.currentTimeMillis() >= endTime) {
                throw new AssertionError("Не удалось дождаться кликабельности " + blockNumber +
                        " блока списка блоков '" + blockListName + "' внутри блока '" + blockName +
                        "' в течение таймаута " + timeoutMs + " мс");
            }

            com.codeborne.selenide.Selenide.sleep(pollingMs);
        }
    }

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    public IStepResult clickOnBlockInBlockList(String blockListName, int blockNumber) {
        return clickOnBlockByNumber(BlockListContext.fromList(blockListName), blockNumber);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    public IStepResult clickOnBlockInBlockList(String blockName, String blockListName, int blockNumber) {
        return clickOnBlockByNumber(BlockListContext.fromBlock(blockName, blockListName), blockNumber);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult clickOnBlockWithComplexCondition(BlockListContext blockListContext, DataTable conditionsTable) {
        List<CorePage> resultList = blockListContext.filterByConditions(conditionsTable);

        if (resultList.size() != 1) {
            throw new IllegalArgumentException("По заданному списку условий найдено 0 или более одного блока\n" + blockListToString(resultList));
        }

        resultList.get(0).getSelf().shouldBe(Condition.enabled).click();

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на блок элементы которого соответствуют списку$")
    public IStepResult clickOnBlockInBlockListWIthComplexCondition(String blockListName, DataTable conditionsTable) {
        return clickOnBlockWithComplexCondition(BlockListContext.fromList(blockListName), conditionsTable);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" выполнено нажатие на блок элементы которого соответствуют списку$")
    public IStepResult clickOnBlockInBlockListWIthComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        return clickOnBlockWithComplexCondition(BlockListContext.fromBlock(blockName, blockListName), conditionsTable);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\" блока который соответствуют условию")
    public IStepResult clickOnElementInBlockListWIthComplexCondition(String blockListName, String elementNameClick, DataTable conditionsTable) {
        BlockListContext blockListContext = BlockListContext.fromList(blockListName);
        List<CorePage> resultList = blockListContext.filterByConditions(conditionsTable);

        resultList.get(0).getElement(elementNameClick).shouldBe(Condition.enabled).click();

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }
}
