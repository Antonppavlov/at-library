package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.entities.BlockListStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.IStepResult;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.*;

/**
 * Шаги-действия для работы со списками блоков (List<CorePage>), построенные поверх {@link BlockListContext}.
 *
 * Шаги "в блоке ..." и без него объединены в один метод на каждое действие: короткий
 * вариант шага получает фиктивную пустую захватывающую группу {@code ()} в начале
 * regex, чтобы количество групп совпадало с "блочным" вариантом — это сохраняет
 * подсказки/навигацию IntelliJ для обеих формулировок и не требует делегирования
 * между двумя разными Cucumber-шагами. Общая логика выбора блока/элемента вынесена
 * во внутренние helper-методы (clickBlockWhere..., clickElementInBlockWhere..., и т.д.).
 */
public class BlocksCollectionActionSteps {

    /**
     * -----------------------------------------------------------------------------------------------------------------
     * -----------------------------------------------Действия списка блоков------------------------------------------------
     * -----------------------------------------------------------------------------------------------------------------
     */

    private IStepResult clickBlockWhereTextEquals(BlockListContext context,
                                                  String elementNameSearch,
                                                  String expectedTextSearch) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage block = findBlock(
                context,
                elementNameSearch,
                resolvedExpectedText,
                foundBlock -> {
                    SelenideElement root = foundBlock.getSelf();
                    root.shouldBe(Condition.visible);
                    root.hover();
                    root.click();
                }
        );

        return new BlockListStepResult(block, elementNameSearch);
    }

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    public IStepResult clickBlockInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch) {
        return clickBlockWhereTextEquals(
                BlockListContext.live(blockName, blockListName),
                elementNameSearch,
                expectedTextSearch
        );
    }

    private IStepResult clickElementInBlockWhereTextEquals(BlockListContext context,
                                                           String elementNameSearch,
                                                           String expectedTextSearch,
                                                           String elementNameClick) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage block = findBlock(
                context,
                elementNameSearch,
                resolvedExpectedText,
                foundBlock -> {
                    SelenideElement element = foundBlock.getElement(elementNameClick);
                    element.shouldBe(Condition.visible);
                    element.hover();
                    element.click();
                }
        );

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return clickElementInBlockWhereTextEquals(
                BlockListContext.live(blockName, blockListName),
                elementNameSearch,
                expectedTextSearch,
                elementNameClick
        );
    }

    private IStepResult clickElementInBlockWhereElementVisible(BlockListContext context,
                                                               String elementNameSearch,
                                                               String elementNameClick) {
        CorePage block = BlockSearchExecutor.findInContext(
                context,
                elementNameSearch,
                Condition.visible,
                foundBlock -> {
                    SelenideElement element = foundBlock.getElement(elementNameClick);
                    element.shouldBe(Condition.visible);
                    element.hover();
                    element.click();
                },
                "Во всех блоках элемент '" + elementNameSearch + "' не стал видимым"
        );

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @То("^()в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereElementVisible(String blockName, String blockListName, String elementNameSearch, String elementNameClick) {
        return clickElementInBlockWhereElementVisible(
                BlockListContext.live(blockName, blockListName),
                elementNameSearch,
                elementNameClick
        );
    }

    private IStepResult hoverOnElementInBlockWhereTextEquals(BlockListContext context,
                                                             String elementNameSearch,
                                                             String expectedTextSearch,
                                                             String elementNameClick) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage block = findBlock(
                context,
                elementNameSearch,
                resolvedExpectedText,
                foundBlock -> foundBlock.getElement(elementNameClick)
                        .shouldBe(Condition.visible)
                        .hover()
        );

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    public IStepResult hoverOnElementInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return hoverOnElementInBlockWhereTextEquals(
                BlockListContext.live(blockName, blockListName),
                elementNameSearch,
                expectedTextSearch,
                elementNameClick
        );
    }

    // NB: в исходном коде до-рефакторинга вариант без блока чистил поле через
    // clearField(element) (посимвольный Ctrl+A/Backspace), а блочный вариант —
    // обычным element.clear(). Это выглядит как случайное расхождение, а не
    // намеренное решение, но при слиянии в один метод сохраняем оба поведения
    // как есть, привязав их к наличию имени блока, чтобы не менять поведение
    // существующих сценариев незаметно.
    private IStepResult inputValueInBlockWhereTextEquals(BlockListContext context,
                                                         String elementNameSearch,
                                                         String expectedTextSearch,
                                                         String elementName,
                                                         String inputText,
                                                         boolean useClearField) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedInputText = OtherSteps.getPropertyOrStringVariableOrValue(inputText);

        CorePage block = findBlock(
                context,
                elementNameSearch,
                resolvedExpectedText,
                foundBlock -> {
                    SelenideElement element = foundBlock.getElement(elementName).shouldBe(Condition.visible);
                    element.click();
                    if (useClearField) {
                        clearField(element);
                    } else {
                        element.clear();
                    }
                    element.sendKeys(resolvedInputText);
                }
        );

        return new BlockListStepResult(block, elementNameSearch, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public IStepResult inputValueInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementName, String inputText) {
        boolean noBlockGiven = blockName == null || blockName.isEmpty();
        return inputValueInBlockWhereTextEquals(
                BlockListContext.live(blockName, blockListName),
                elementNameSearch,
                expectedTextSearch,
                elementName,
                inputText,
                noBlockGiven
        );
    }

    private CorePage findBlock(BlockListContext context,
                               String elementName,
                               String expectedText,
                               Consumer<CorePage> onMatched) {
        return BlockSearchExecutor.findInContext(
                context,
                elementName,
                BlockConditions.textEquals(expectedText),
                onMatched,
                "Во всех блоках в элементах " + elementName +
                        " не найден текст:" + expectedText
        );
    }

    private IStepResult clickOnElementInBlockByNumber(BlockListContext context,
                                                      int blockNumber,
                                                      String elementNameClick) {
        CorePage block = BlockSearchExecutor.awaitElementInBlock(
                context,
                blockNumber,
                elementNameClick,
                BlockConditions.clickable(),
                SelenideElement::click,
                "Не удалось дождаться кликабельности элемента '" + elementNameClick + "' в " +
                        blockNumber + " блоке\n" + context.describe()
        );
        return new BlockListStepResult(block, elementNameClick);
    }

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickOnElementBlockInBlockList(String blockName, String blockListName, int blockNumber, String elementNameClick) {
        return clickOnElementInBlockByNumber(
                BlockListContext.live(blockName, blockListName),
                blockNumber,
                elementNameClick
        );
    }

    private IStepResult clickOnBlockByNumber(BlockListContext blockListContext, int blockNumber) {
        CorePage block = BlockSearchExecutor.awaitBlockRoot(
                blockListContext,
                blockNumber,
                BlockConditions.clickable(),
                SelenideElement::click,
                "Не удалось дождаться кликабельности " + blockNumber +
                        " блока\n" + blockListContext.describe()
        );
        return new BlockListStepResult(block);
    }

    @И("^()в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    public IStepResult clickOnBlockInBlockList(String blockName, String blockListName, int blockNumber) {
        return clickOnBlockByNumber(BlockListContext.live(blockName, blockListName), blockNumber);
    }

    private IStepResult clickOnBlockWithComplexCondition(BlockListContext blockListContext, DataTable conditionsTable) {
        List<CorePage> resultList = getBlockListWithComplexCondition(
                blockListContext,
                conditionsTable,
                matchedBlocks -> {
                    if (matchedBlocks.size() != 1) {
                        throw new IllegalArgumentException(
                                "По заданному списку условий найдено более одного блока\n" +
                                        blockListToString(matchedBlocks)
                        );
                    }
                    matchedBlocks.get(0).getSelf().shouldBe(Condition.enabled).click();
                }
        );

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }

    @И("^()в списке блоков \"([^\"]*)\" выполнено нажатие на блок элементы которого соответствуют списку$")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" выполнено нажатие на блок элементы которого соответствуют списку$")
    public IStepResult clickOnBlockInBlockListWIthComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        return clickOnBlockWithComplexCondition(
                BlockListContext.live(blockName, blockListName),
                conditionsTable
        );
    }

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\" блока который соответствуют условию")
    public IStepResult clickOnElementInBlockListWIthComplexCondition(String blockListName, String elementNameClick, DataTable conditionsTable) {
        BlockListContext blockListContext = BlockListContext.live(blockListName);
        List<CorePage> resultList = getBlockListWithComplexCondition(
                blockListContext,
                conditionsTable,
                matchedBlocks -> matchedBlocks.get(0)
                        .getElement(elementNameClick)
                        .shouldBe(Condition.enabled)
                        .click()
        );

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }
}
