package ru.at.library.web.step.elementcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.ru.И;
import lombok.extern.log4j.Log4j2;
import ru.at.library.web.entities.CommonStepResult;
import ru.at.library.web.scenario.IStepResult;
import ru.at.library.web.scenario.WebScenario;

import static com.codeborne.selenide.Condition.*;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.web.step.elementcollection.ElementsCollectionCheckSteps.getRandomElementFromCollection;

/**
 * Действия с ElementsCollection
 */
@Log4j2
public class ElementsCollectionActionSteps {


    @И("^в списке элементов \"([^\"]*)\" выполнено нажатие на элемент с текстом \"([^\"]*)\"$")
    public IStepResult clickOnListElementWithExactText(String listName, String expectedValue) {
        return clickOnListElementWithExactText(
                WebScenario.getCurrentPage().getElementsList(listName),
                expectedValue
        );
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на элемент с текстом \"([^\"]*)\"$")
    public IStepResult clickOnListElementWithExactText(String blockName, String listName, String expectedValue) {
        return clickOnListElementWithExactText(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                expectedValue
        );
    }

    /**
     * Выбор из списка со страницы элемента с заданным значением
     * (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    public IStepResult clickOnListElementWithExactText(ElementsCollection elements, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = elements.find(Condition.or(
                        "Поиск элемента с текстом для дальнейшего нажатия",
                        exactText(expectedValue),
                        exactValue(expectedValue)
                )
        );
        element.click();
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" выполнено двойное нажатие на элемент с текстом \"([^\"]*)\"$")
    public IStepResult doubleClickOnListElementWithExactText(String listName, String expectedValue) {
        return clickOnListElementWithExactText(
                WebScenario.getCurrentPage().getElementsList(listName),
                expectedValue
        );
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено двойное нажатие на элемент с текстом \"([^\"]*)\"$")
    public IStepResult doubleClickOnListElementWithExactText(String blockName, String listName, String expectedValue) {
        return clickOnListElementWithExactText(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                expectedValue
        );
    }

    /**
     * Выбор из списка со страницы элемента с заданным значением
     * (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    public IStepResult doubleClickOnListElementWithExactText(ElementsCollection elements, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = elements.find(Condition.or(
                        "Поиск элемента с текстом для дальнейшего нажатия",
                        exactText(expectedValue),
                        exactValue(expectedValue)
                )
        );
        element.doubleClick();
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" выполнено нажатие на элемент содержащий текст \"([^\"]*)\"$")
    public IStepResult clickOnListElementWithContainsText(String listName, String expectedValue) {
        return clickOnListElementWithContainsText(
                WebScenario.getCurrentPage().getElementsList(listName),
                expectedValue);
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на элемент содержащий текст \"([^\"]*)\"$")
    public IStepResult clickOnListElementWithContainsText(String blockName, String listName, String expectedValue) {
        return clickOnListElementWithContainsText(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                expectedValue);
    }

    /**
     * Выбор из списка со страницы элемента, который содержит заданный текст
     * (в приоритете: из property, из переменной сценария, значение аргумента)
     * Не чувствителен к регистру
     */
    public IStepResult clickOnListElementWithContainsText(ElementsCollection elements, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = elements.find(Condition.or(
                "Поиск элемента содержащего текст для дальнейшего нажатия",
                text(expectedValue),
                value(expectedValue)
        ));
        element.click();
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" выполнено нажатие на \"(\\d+)\" элемент$")
    public IStepResult clickOnListElementWithIndex(String listName, int number) {
        return clickOnListElementWithIndex(
                WebScenario.getCurrentPage().getElementsList(listName),
                number);
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на \"(\\d+)\" элемент$")
    public IStepResult clickOnListElementWithIndex(String blockName, String listName, int number) {
        return clickOnListElementWithIndex(
                WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName),
                number);
    }

    /**
     * Выбор n-го элемента из списка со страницы
     * Нумерация элементов начинается с 1
     */
    public IStepResult clickOnListElementWithIndex(ElementsCollection elements, int number) {
        SelenideElement element = elements.get(number - 1);
        element.click();
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" выполнено нажатие на случайный элемент$")
    public IStepResult clickOnListElementWithRandomIndex(String listName) {
        return clickOnListElementWithRandomIndex(WebScenario.getCurrentPage().getElementsList(listName));
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на случайный элемент$")
    public IStepResult clickOnListElementWithRandomIndex(String blockName, String listName) {
        return clickOnListElementWithRandomIndex(WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName));
    }

    /**
     * Выполнено нажатие на случайный элемент
     */
    public IStepResult clickOnListElementWithRandomIndex(ElementsCollection elements) {
        elements = elements.filter(visible);
        SelenideElement element = getRandomElementFromCollection(elements.filter(visible));
        element.click();
        log.trace("Выполнено нажатие на случайный элемент: " + element);
        return new CommonStepResult(element);
    }



    /**
     * ######################################################################################################################
     */

    @И("^в списке элементов \"([^\"]*)\" выполнено нажатие на последний элемент$")
    public IStepResult clickOnListElementWithLast(String listName) {
        return clickOnListElementWithLast(WebScenario.getCurrentPage().getElementsList(listName));
    }

    @И("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на последний элемент$")
    public IStepResult clickOnListElementWithLast(String blockName, String listName) {
        return clickOnListElementWithLast(WebScenario.getCurrentPage().getBlock(blockName).getElementsList(listName));
    }

    /**
     * Выполнено нажатие на последний элемент
     */
    public IStepResult clickOnListElementWithLast(ElementsCollection elements) {
        elements = elements.filter(visible);
        SelenideElement element = elements.last();
        element.click();
        log.trace("Выполнено нажатие на последний элемент: " + element);
        return new CommonStepResult(element);
    }


}
