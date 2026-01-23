package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.entities.BlockListStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
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

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * -----------------------------------------------------------------------------------------------------------------
     * -----------------------------------------------Действия списка блоков------------------------------------------------
     * -----------------------------------------------------------------------------------------------------------------
     */

    private IStepResult clickBlockWhereTextEquals(BlockListContext blockListContext, String elementNameSearch, String expectedTextSearch) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = blockListContext.findByTextEquals(elementNameSearch, expectedTextSearch);
        SelenideElement root = block.getSelf();
        root.shouldHave(Condition.visible);
        root.hover();
        root.click();

        return new BlockListStepResult(block, elementNameSearch);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    public IStepResult clickBlockInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch) {
        return clickBlockWhereTextEquals(BlockListContext.fromList(blockListName), elementNameSearch, expectedTextSearch);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    public IStepResult clickBlockInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch) {
        return clickBlockWhereTextEquals(BlockListContext.fromBlock(blockName, blockListName), elementNameSearch, expectedTextSearch);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult clickElementInBlockWhereTextEquals(BlockListContext blockListContext, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = blockListContext.findByTextEquals(elementNameSearch, expectedTextSearch);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return clickElementInBlockWhereTextEquals(BlockListContext.fromList(blockListName), elementNameSearch, expectedTextSearch, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return clickElementInBlockWhereTextEquals(BlockListContext.fromBlock(blockName, blockListName), elementNameSearch, expectedTextSearch, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */


    private IStepResult clickElementInBlockWhereElementVisible(BlockListContext blockListContext, String elementNameSearch, String elementNameClick) {
        CorePage block = blockListContext.findByVisibleElement(elementNameSearch);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @То("^в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereElementVisible(String blockListName, String elementNameSearch, String elementNameClick) {
        return clickElementInBlockWhereElementVisible(BlockListContext.fromList(blockListName), elementNameSearch, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereElementVisible(String blockName, String blockListName, String elementNameSearch, String elementNameClick) {
        return clickElementInBlockWhereElementVisible(BlockListContext.fromBlock(blockName, blockListName), elementNameSearch, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult hoverOnElementInBlockWhereTextEquals(BlockListContext blockListContext, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage block = blockListContext.findByTextEquals(elementNameSearch, expectedTextSearch);
        SelenideElement element = block.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();

        return new BlockListStepResult(block, elementNameClick, elementNameSearch);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    public IStepResult hoverOnElementInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return hoverOnElementInBlockWhereTextEquals(BlockListContext.fromList(blockListName), elementNameSearch, expectedTextSearch, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    public IStepResult hoverOnElementInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        return hoverOnElementInBlockWhereTextEquals(BlockListContext.fromBlock(blockName, blockListName), elementNameSearch, expectedTextSearch, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult inputValueInBlockWhereTextEquals(BlockListContext blockListContext, String elementNameSearch, String expectedTextSearch, String elementName, String inputText, boolean useClearField) throws Exception {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        inputText = OtherSteps.getPropertyOrStringVariableOrValue(inputText);

        CorePage block = blockListContext.findByTextEquals(elementNameSearch, expectedTextSearch);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.visible);
        element.click();
        if (useClearField) {
            clearField(element);
        } else {
            element.clear();
        }
        element.sendKeys(inputText);

        return new BlockListStepResult(block, elementNameSearch, elementName);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public IStepResult inputValueInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementName, String inputText) throws Exception {
        return inputValueInBlockWhereTextEquals(BlockListContext.fromList(blockListName), elementNameSearch, expectedTextSearch, elementName, inputText, true);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public IStepResult inputValueInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementName, String inputText) throws Exception {
        return inputValueInBlockWhereTextEquals(BlockListContext.fromBlock(blockName, blockListName), elementNameSearch, expectedTextSearch, elementName, inputText, false);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult clickOnElementInNthBlock(BlockListContext blockListContext, int blockIndex, String elementNameClick) {
        CorePage block = blockListContext.nthBlock(blockIndex);
        block.getElement(elementNameClick).click();
        return new BlockListStepResult(block, elementNameClick);
    }

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickOnElementBlockInBlockList(String blockListName, int blockIndex, String elementNameClick) {
        return clickOnElementInNthBlock(BlockListContext.fromList(blockListName), blockIndex, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickOnElementBlockInBlockList(String blockName, String blockListName, int blockIndex, String elementNameClick) {
        return clickOnElementInNthBlock(BlockListContext.fromBlock(blockName, blockListName), blockIndex, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult clickOnNthBlock(BlockListContext blockListContext, int blockIndex) {
        CorePage block = blockListContext.nthBlock(blockIndex);
        block.getSelf().shouldBe(Condition.enabled).click();
        return new BlockListStepResult(block);
    }

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    public IStepResult clickOnBlockInBlockList(String blockListName, int blockIndex) {
        return clickOnNthBlock(BlockListContext.fromList(blockListName), blockIndex);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    public IStepResult clickOnBlockInBlockList(String blockName, String blockListName, int blockIndex) {
        return clickOnNthBlock(BlockListContext.fromBlock(blockName, blockListName), blockIndex);
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult clickOnBlockWithComplexCondition(BlockListContext blockListContext, DataTable conditionsTable) {
        List<CorePage> resultList = blockListContext.filterByConditions(conditionsTable);

        if (resultList.size() != 1) {
            throw new IllegalArgumentException("По заданному списку условий найдено 0 или более 1 блока\n" + blockListToString(resultList));
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
