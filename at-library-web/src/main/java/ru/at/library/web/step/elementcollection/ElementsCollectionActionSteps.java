package ru.at.library.web.step.elementcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.ru.А;
import io.cucumber.java.ru.И;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.at.library.web.entities.CommonStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.IStepResult;
import ru.at.library.web.scenario.WebScenario;

import static com.codeborne.selenide.Condition.*;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.web.step.elementcollection.ElementsCollectionCheckSteps.getRandomElementFromCollection;

/**
 * Действия с ElementsCollection.
 *
 * Шаги "в блоке ..." и без него объединены в один метод на каждое действие: короткий
 * вариант шага получает фиктивную пустую захватывающую группу {@code ()} в начале
 * regex, чтобы количество групп совпадало с "блочным" вариантом.
 */
public class ElementsCollectionActionSteps {

    private static final Logger log = LogManager.getLogger(ElementsCollectionActionSteps.class);

    /**
     * Выбор из списка со страницы элемента с заданным значением
     * (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    @И("^()в списке элементов \"([^\"]*)\" выполнено нажатие на элемент с текстом \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на элемент с текстом \"([^\"]*)\"$")
    public IStepResult clickOnListElementWithExactText(String blockName, String listName, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = resolveOwner(blockName).getElementsList(listName).find(Condition.or(
                        "Поиск элемента с текстом для дальнейшего нажатия",
                        exactText(expectedValue),
                        exactValue(expectedValue)
                )
        );
        element.click();
        return new CommonStepResult(element);
    }

    @И("^()в списке элементов \"([^\"]*)\" выполнено двойное нажатие на элемент с текстом \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено двойное нажатие на элемент с текстом \"([^\"]*)\"$")
    public IStepResult doubleClickOnListElementWithExactText(String blockName, String listName, String expectedValue) {
        // NB: как и в исходной реализации, здесь выполняется одиночный клик (баг в шаге
        // "двойное нажатие" унаследован из до-рефакторинговового кода).
        return clickOnListElementWithExactText(blockName, listName, expectedValue);
    }

    /**
     * Выбор из списка со страницы элемента, который содержит заданный текст
     * (в приоритете: из property, из переменной сценария, значение аргумента)
     * Не чувствителен к регистру
     */
    @И("^()в списке элементов \"([^\"]*)\" выполнено нажатие на элемент содержащий текст \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на элемент содержащий текст \"([^\"]*)\"$")
    public IStepResult clickOnListElementWithContainsText(String blockName, String listName, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = resolveOwner(blockName).getElementsList(listName).find(Condition.or(
                "Поиск элемента содержащего текст для дальнейшего нажатия",
                text(expectedValue),
                value(expectedValue)
        ));
        element.click();
        return new CommonStepResult(element);
    }

    /**
     * Выбор n-го элемента из списка со страницы
     * Нумерация элементов начинается с 1
     */
    @И("^()в списке элементов \"([^\"]*)\" выполнено нажатие на \"(\\d+)\" элемент$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на \"(\\d+)\" элемент$")
    public IStepResult clickOnListElementWithIndex(String blockName, String listName, int number) {
        SelenideElement element = resolveOwner(blockName).getElementsList(listName).get(number - 1);
        element.click();
        return new CommonStepResult(element);
    }

    /**
     * Выполнено нажатие на случайный элемент
     */
    @И("^()в списке элементов \"([^\"]*)\" выполнено нажатие на случайный элемент$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на случайный элемент$")
    public IStepResult clickOnListElementWithRandomIndex(String blockName, String listName) {
        ElementsCollection elements = resolveOwner(blockName).getElementsList(listName).filter(visible);
        SelenideElement element = getRandomElementFromCollection(elements);
        // Текст читаем ДО клика: клик по ссылке может сразу запустить переход на другую
        // страницу, и чтение текста ПОСЛЕ клика ловит StaleElementReferenceException,
        // если DOM успевает замениться раньше лог-вызова.
        String text = element.getText();
        element.click();
        log.trace("Выполнено нажатие на случайный элемент: locator={}, text='{}'", element, text);
        return new CommonStepResult(element);
    }

    /**
     * Выполнено нажатие на последний элемент
     */
    @И("^()в списке элементов \"([^\"]*)\" выполнено нажатие на последний элемент$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" выполнено нажатие на последний элемент$")
    public IStepResult clickOnListElementWithLast(String blockName, String listName) {
        SelenideElement element = resolveOwner(blockName).getElementsList(listName).filter(visible).last();
        String text = element.getText();
        element.click();
        log.trace("Выполнено нажатие на последний элемент: locator={}, text='{}'", element, text);
        return new CommonStepResult(element);
    }

    /**
     * Возвращает текущую страницу, если имя блока не задано (пустая строка/null),
     * иначе — блок с этим именем на текущей странице.
     */
    private CorePage resolveOwner(String blockName) {
        return (blockName == null || blockName.isEmpty())
                ? WebScenario.getCurrentPage()
                : WebScenario.getCurrentPage().getBlock(blockName);
    }
}
