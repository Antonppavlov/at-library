package ru.at.library.web.step.listcorepage;

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
import static ru.at.library.web.step.listcorepage.ListCorePageOtherMethod.*;

public class ListCorePageActionSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * -----------------------------------------------------------------------------------------------------------------
     * -----------------------------------------------Действия списка блоков------------------------------------------------
     * -----------------------------------------------------------------------------------------------------------------
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    public IStepResult clickBlockInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        SelenideElement element = corePageByTextInElement.getSelf();
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на блок$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на блок$")
    public IStepResult clickBlockInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);
        SelenideElement element = corePageByTextInElement.getSelf();
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);
        SelenideElement element = corePageByTextInElement.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(corePageByTextInElement, elementNameClick, elementNameSearch);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение и нажатие на элемент \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);
        SelenideElement element = corePageByTextInElement.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(corePageByTextInElement, elementNameClick, elementNameSearch);
    }

    /**
     * ######################################################################################################################
     */


    @То("^в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereElementVisible(String blockListName, String elementNameSearch, String elementNameClick) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByVisibleElement(blocksList, elementNameSearch);
        SelenideElement element = corePageByTextInElement.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(corePageByTextInElement, elementNameClick, elementNameSearch);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где элемент \"([^\"]*)\" отображается выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickButtonInBlockListWhereElementVisible(String blockName, String blockListName, String elementNameSearch, String elementNameClick) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByVisibleElement(blocksList, elementNameSearch);
        SelenideElement element = corePageByTextInElement.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();
        element.click();

        return new BlockListStepResult(corePageByTextInElement, elementNameClick, elementNameSearch);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    public IStepResult hoverOnElementInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);
        SelenideElement element = corePageByTextInElement.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();

        return new BlockListStepResult(corePageByTextInElement, elementNameClick, elementNameSearch);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено наведение на элемент \"([^\"]*)\"$")
    public IStepResult hoverOnElementInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameClick) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);
        SelenideElement element = corePageByTextInElement.getElement(elementNameClick);
        element.shouldHave(Condition.visible);
        element.hover();

        return new BlockListStepResult(corePageByTextInElement, elementNameClick, elementNameSearch);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public IStepResult inputValueInBlockListWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String elementName, String inputText) throws Exception {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        inputText = OtherSteps.getPropertyOrStringVariableOrValue(inputText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);
        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        SelenideElement element = corePageByTextInElement.getElement(elementName);
        element.shouldHave(Condition.visible);
        element.click();
        clearField(element);
        element.sendKeys(inputText);

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public IStepResult inputValueInBlockListWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementName, String inputText) throws Exception {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        inputText = OtherSteps.getPropertyOrStringVariableOrValue(inputText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);
        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        SelenideElement element = corePageByTextInElement.getElement(elementName);
        element.shouldHave(Condition.visible);
        element.click();
        element.clear();
        element.sendKeys(inputText);

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickOnElementBlockInBlockList(String blockListName, int blockIndex, String elementNameClick) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);
        block.getElement(elementNameClick).click();
        return new BlockListStepResult(block, elementNameClick);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке выполнено нажатие на элемент \"([^\"]*)\"$")
    public IStepResult clickOnElementBlockInBlockList(String blockName, String blockListName, int blockIndex, String elementNameClick) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);
        block.getElement(elementNameClick).click();
        return new BlockListStepResult(block, elementNameClick);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    public IStepResult clickOnBlockInBlockList(String blockListName, int blockIndex) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        block.getSelf().shouldBe(Condition.enabled).click();
        return new BlockListStepResult(block);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" выполнено нажатие на (\\d+) блок$")
    public IStepResult clickOnBlockInBlockList(String blockName, String blockListName, int blockIndex) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        block.getSelf().shouldBe(Condition.enabled).click();
        return new BlockListStepResult(block);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на блок элементы которого соответствуют списку$")
    public IStepResult clickOnBlockInBlockListWIthComplexCondition(String blockListName, DataTable conditionsTable) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> resultList = getBlockListWithComplexCondition(blocksList, conditionsTable);

        if (resultList.size() != 1) {
            throw new IllegalArgumentException("По заданному списку условий найдено 0 или более 1 блока\n" + blockListToString(resultList));
        }

        resultList.get(0).getSelf().shouldBe(Condition.enabled).click();

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" выполнено нажатие на блок элементы которого соответствуют списку$")
    public IStepResult clickOnBlockInBlockListWIthComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> resultList = getBlockListWithComplexCondition(blocksList, conditionsTable);

        if (resultList.size() != 1) {
            throw new IllegalArgumentException("По заданному списку условий найдено 0 или более 1 блока\n" + blockListToString(resultList));
        }

        resultList.get(0).getSelf().shouldBe(Condition.enabled).click();

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" выполнено нажатие на элемент \"([^\"]*)\" блока который соответствуют условию")
    public IStepResult clickOnElementInBlockListWIthComplexCondition(String blockListName, String elementNameClick, DataTable conditionsTable) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> resultList = getBlockListWithComplexCondition(blocksList, conditionsTable);


        resultList.get(0).getElement(elementNameClick).shouldBe(Condition.enabled).click();

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }
}
