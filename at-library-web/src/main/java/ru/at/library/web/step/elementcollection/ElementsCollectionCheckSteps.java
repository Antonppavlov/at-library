package ru.at.library.web.step.elementcollection;

import com.codeborne.selenide.*;
import io.cucumber.java.ru.И;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.web.entities.CommonStepResult;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.IStepResult;
import ru.at.library.web.scenario.WebScenario;

import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.steps.OtherSteps.getRandom;


/**
 * Проверки ElementsCollection
 */
@Log4j2
public class ElementsCollectionCheckSteps {

    @И("^список элементов \"([^\"]*)\" отображается на странице$")
    public IStepResult shouldVisible(String listName) {
        return shouldVisible(WebScenario.getCurrentPage().getElementsList(listName));
    }

    @И("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" отображается на странице$")
    public IStepResult shouldVisible(String blockName, String listName) {
        return shouldVisible(WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName));
    }

    /**
     * Проверка отображения списка на странице
     */
    public IStepResult shouldVisible(ElementsCollection elements) {
        elements.first().shouldHave(visible);
        return new CommonStepResult(elements.first());
    }

    /**
     * ######################################################################################################################
     */

    @И("^список элементов \"([^\"]*)\" не отображается на странице$")
    public IStepResult isHidden(String listName) {
        return isHidden(WebScenario.getCurrentPage().getElementsList(listName));
    }

    @И("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" не отображается на странице$")
    public IStepResult isHidden(String blockName, String listName) {
        return isHidden(WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName));
    }

    /**
     * Проверка не отображения списка на странице
     */
    public IStepResult isHidden(ElementsCollection elements) {
        elements.first().shouldHave(not(visible));
        return new CommonStepResult(elements.first());
    }

    /**
     * ######################################################################################################################
     */

    @И("^список элементов \"([^\"]*)\" включает в себя список из таблицы$")
    public IStepResult containsList(String listName, List<String> textTable) {
        return containsList(
                WebScenario.getCurrentPage().getElementsList(listName),
                textTable);
    }

    @И("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" включает в себя список из таблицы$")
    public IStepResult containsList(String blockName, String listName, List<String> textTable) {
        return containsList(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                textTable);
    }

    public IStepResult containsList(ElementsCollection elements, List<String> textTable) {
        textTable = getPropertyOrStringVariableOrValue(textTable);
        for (String expectedText : textTable) {
            elements.find(text(expectedText)).shouldHave(text(expectedText));
        }
        return new CommonStepResult(elements);
    }

    /**
     * ######################################################################################################################
     */

    @И("^список элементов \"([^\"]*)\" равен списку из таблицы$")
    public IStepResult equalsToList(String listName, List<String> textTable) {
        return equalsToList(
                WebScenario.getCurrentPage().getElementsList(listName),
                textTable);
    }

    @И("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" равен списку из таблицы$")
    public IStepResult equalsToList(String blockName, String listName, List<String> textTable) {
        return equalsToList(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                textTable);
    }

    /**
     * Проверка, что список со страницы состоит только из элементов,
     * перечисленных в таблице
     */
    public IStepResult equalsToList(ElementsCollection elements, List<String> textTable) {
        textTable = getPropertyOrStringVariableOrValue(textTable);
        elements.shouldHave(CollectionCondition.exactTexts(textTable));
        return new CommonStepResult(elements);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" текст любого из элементов сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveRandomListElementTextToVar(String listName, String varName) {
        return saveRandomListElementTextToVar(
                WebScenario.getCurrentPage().getElementsList(listName),
                varName
        );
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" текст любого из элементов сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveRandomListElementTextToVar(String blockName, String listName, String varName) {
        return saveRandomListElementTextToVar(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                varName
        );
    }

    /**
     * Выбор из списка со страницы любого случайного элемента и сохранение его значения в переменную
     */
    public IStepResult saveRandomListElementTextToVar(ElementsCollection elements, String varName) {
        SelenideElement element = getRandomElementFromCollection(elements.filter(visible));
        String text = element.getText();
        CoreScenario.getInstance().setVar(varName, text);
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" текст в элементе \"(\\d+)\" равен \"([^\"]*)\"$")
    public IStepResult listElementWithIndexHasExactText(String listName, int number, String expectedValue) {
        return listElementWithIndexHasExactText(
                WebScenario.getCurrentPage().getElementsList(listName),
                number,
                expectedValue
        );
    }


    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" текст в элементе \"(\\d+)\" равен \"([^\"]*)\"$")
    public IStepResult listElementWithIndexHasExactText(String blockName, String listName, int number, String expectedValue) {
        return listElementWithIndexHasExactText(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                number,
                expectedValue
        );
    }

    /**
     * Проверка текста в элементе списка
     */
    public IStepResult listElementWithIndexHasExactText(ElementsCollection elements, int number, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement selenideElement = elements.get(number - 1);
        SelenideElement element = selenideElement.shouldHave(text(expectedValue));
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" элемент c текстом \"([^\"]*)\" выбран$")
    public IStepResult listElementWithIndexHasSelected(String listName, String elementText) {
        return listElementWithIndexHasSelected(
                WebScenario.getCurrentPage().getElementsList(listName),
                elementText
        );
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" элемент c текстом \"([^\"]*)\" выбран$")
    public IStepResult listElementWithIndexHasSelected(String blockName, String listName, String elementText) {
        return listElementWithIndexHasSelected(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                elementText
        );
    }

    /**
     * Проверка что элемент c текстом выбран
     */
    public IStepResult listElementWithIndexHasSelected(ElementsCollection elements, String elementText) {
        SelenideElement selenideElement = elements.find(Condition.text(elementText));
        SelenideElement element = selenideElement.shouldHave(selected);
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" содержится элемент с текстом \"([^\"]*)\"$")
    public IStepResult containsElementWithText(String listName, String expectedValue) {
        return containsElementWithText(
                WebScenario.getCurrentPage().getElementsList(listName),
                expectedValue);
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" содержится элемент с текстом \"([^\"]*)\"$")
    public IStepResult containsElementWithText(String blockName, String listName, String expectedValue) {
        return containsElementWithText(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                expectedValue);
    }

    /**
     * Проверка, что каждый элемент списка содержит ожидаемый текст
     */
    public IStepResult containsElementWithText(ElementsCollection elements, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = elements.find(Condition.text(expectedValue))
                .shouldHave(text(expectedValue));
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" не содержится элемент с текстом \"([^\"]*)\"$")
    public void notContainsElementWithExactText(String listName, String expectedValue) {
        notContainsElementWithExactText(
                WebScenario.getCurrentPage().getElementsList(listName),
                expectedValue);
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" не содержится элемент с текстом \"([^\"]*)\"$")
    public void notContainsElementWithExactText(String blockName, String listName, String expectedValue) {
        notContainsElementWithExactText(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                expectedValue);
    }

    /**
     * Проверка, что каждый элемент списка не содержит ожидаемый текст
     */
    public void notContainsElementWithExactText(ElementsCollection elements, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        elements.filter(Condition.exactText(expectedValue)).shouldHave(CollectionCondition.size(0));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" количество элементов (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)$")
    public IStepResult checkSize(String listName, String condition, String expectedSize) {
        return checkSize(
                WebScenario.getCurrentPage().getElementsList(listName),
                condition,
                expectedSize);
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" количество элементов (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)")
    public IStepResult checkSize(String blockName, String listName, String condition, String expectedSize) {
        return checkSize(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                condition,
                expectedSize);
    }

    /**
     * Производится проверка соответствия числа элементов списка условию и значению, указанному в шаге
     */
    public IStepResult checkSize(ElementsCollection elements, String condition, String expectedSize) {
        WebElementsCondition webElementsCondition = CustomCondition.getElementsCollectionSizeCondition(
                CustomCondition.Comparison.fromString(getPropertyOrStringVariableOrValue(condition)),
                Integer.parseInt(getPropertyOrStringVariableOrValue(expectedSize))
        );
        elements.shouldHave(webElementsCondition);
        return new CommonStepResult(elements);
    }

    /**
     * ######################################################################################################################
     */

    public static SelenideElement getRandomElementFromCollection(ElementsCollection elementsCollection) {
        return elementsCollection.get(getRandom(elementsCollection.size())).shouldBe(visible);
    }

}
