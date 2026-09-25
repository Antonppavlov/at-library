package ru.at.library.web.step.elementcollection;

import com.codeborne.selenide.*;
import io.cucumber.java.ru.А;
import io.cucumber.java.ru.И;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.web.entities.CommonStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.IStepResult;
import ru.at.library.web.scenario.WebScenario;
import org.openqa.selenium.StaleElementReferenceException;

import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.steps.OtherSteps.getRandom;


/**
 * Проверки ElementsCollection.
 *
 * Шаги "в блоке ..." и без него объединены в один метод на каждую проверку: короткий
 * вариант шага получает фиктивную пустую захватывающую группу {@code ()} в начале
 * regex, чтобы количество групп совпадало с "блочным" вариантом.
 */
@Log4j2
public class ElementsCollectionCheckSteps {

    /**
     * Проверка отображения списка на странице
     */
    @И("^()список элементов \"([^\"]*)\" отображается на странице$")
    @А("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" отображается на странице$")
    public IStepResult shouldVisible(String blockName, String listName) {
        SelenideElement first = resolveOwner(blockName).getElementsList(listName).first();
        first.shouldHave(visible);
        return new CommonStepResult(first);
    }

    /**
     * Проверка не отображения списка на странице
     */
    @И("^()список элементов \"([^\"]*)\" не отображается на странице$")
    @А("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" не отображается на странице$")
    public IStepResult isHidden(String blockName, String listName) {
        SelenideElement first = resolveOwner(blockName).getElementsList(listName).first();
        first.shouldHave(not(visible));
        return new CommonStepResult(first);
    }

    @И("^()список элементов \"([^\"]*)\" включает в себя список из таблицы$")
    @А("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" включает в себя список из таблицы$")
    public IStepResult containsList(String blockName, String listName, List<String> textTable) {
        textTable = getPropertyOrStringVariableOrValue(textTable);
        ElementsCollection elements = resolveOwner(blockName).getElementsList(listName);
        for (String expectedText : textTable) {
            elements.find(text(expectedText)).shouldHave(text(expectedText));
        }
        return new CommonStepResult(elements);
    }

    /**
     * Проверка, что список со страницы состоит только из элементов,
     * перечисленных в таблице
     */
    @И("^()список элементов \"([^\"]*)\" равен списку из таблицы$")
    @А("^в блоке \"([^\"]*)\" список элементов \"([^\"]*)\" равен списку из таблицы$")
    public IStepResult equalsToList(String blockName, String listName, List<String> textTable) {
        textTable = getPropertyOrStringVariableOrValue(textTable);
        ElementsCollection elements = resolveOwner(blockName).getElementsList(listName);
        elements.shouldHave(CollectionCondition.exactTexts(textTable));
        return new CommonStepResult(elements);
    }

    /**
     * Выбор из списка со страницы любого случайного элемента и сохранение его значения в переменную
     */
    @И("^()в списке элементов \"([^\"]*)\" текст любого из элементов сохранен в переменную \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" текст любого из элементов сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveRandomListElementTextToVar(String blockName, String listName, String varName) {
        ElementsCollection elements = resolveOwner(blockName).getElementsList(listName);
        SelenideElement element = getRandomElementFromCollection(elements.filter(visible));
        String text = element.getText();
        CoreScenario.getInstance().setVar(varName, text);
        return new CommonStepResult(element);
    }

    /**
     * Проверка текста в элементе списка
     */
    @И("^()в списке элементов \"([^\"]*)\" текст в элементе \"(\\d+)\" равен \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" текст в элементе \"(\\d+)\" равен \"([^\"]*)\"$")
    public IStepResult listElementWithIndexHasExactText(String blockName, String listName, int number, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement selenideElement = resolveOwner(blockName).getElementsList(listName).get(number - 1);
        SelenideElement element = selenideElement.shouldHave(text(expectedValue));
        return new CommonStepResult(element);
    }

    /**
     * Проверка что элемент c текстом выбран
     */
    @И("^()в списке элементов \"([^\"]*)\" элемент c текстом \"([^\"]*)\" выбран$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" элемент c текстом \"([^\"]*)\" выбран$")
    public IStepResult listElementWithIndexHasSelected(String blockName, String listName, String elementText) {
        SelenideElement selenideElement = resolveOwner(blockName).getElementsList(listName).find(Condition.text(elementText));
        SelenideElement element = selenideElement.shouldHave(selected);
        return new CommonStepResult(element);
    }

    /**
     * Проверка, что каждый элемент списка содержит ожидаемый текст
     */
    @И("^()в списке элементов \"([^\"]*)\" содержится элемент с текстом \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" содержится элемент с текстом \"([^\"]*)\"$")
    public IStepResult containsElementWithText(String blockName, String listName, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = resolveOwner(blockName).getElementsList(listName)
                .find(Condition.text(expectedValue))
                .shouldHave(text(expectedValue));
        return new CommonStepResult(element);
    }

    /**
     * Проверка, что каждый элемент списка не содержит ожидаемый текст
     */
    @И("^()в списке элементов \"([^\"]*)\" не содержится элемент с текстом \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" не содержится элемент с текстом \"([^\"]*)\"$")
    public void notContainsElementWithExactText(String blockName, String listName, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        resolveOwner(blockName).getElementsList(listName)
                .filter(Condition.exactText(expectedValue))
                .shouldHave(CollectionCondition.size(0));
    }

    /**
     * Производится проверка соответствия числа элементов списка условию и значению, указанному в шаге
     */
    @И("^()в списке элементов \"([^\"]*)\" количество элементов (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)$")
    @А("^в блоке \"([^\"]*)\" в списке элементов \"([^\"]*)\" количество элементов (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)$")
    public IStepResult checkSize(String blockName, String listName, String condition, String expectedSize) {
        ElementsCollection elements = resolveOwner(blockName).getElementsList(listName);
        WebElementsCondition webElementsCondition = CustomCondition.getElementsCollectionSizeCondition(
                CustomCondition.Comparison.fromString(getPropertyOrStringVariableOrValue(condition)),
                Integer.parseInt(getPropertyOrStringVariableOrValue(expectedSize))
        );
        elements.shouldHave(webElementsCondition);
        return new CommonStepResult(elements);
    }

    public static SelenideElement getRandomElementFromCollection(ElementsCollection elementsCollection) {
        // .snapshot() фиксирует коллекцию один раз: .size() и .get(index) — это два
        // отдельных live-запроса к DOM, и если между ними список успевает измениться,
        // индекс, посчитанный по старому размеру, перестаёт попадать в новый (IndexOutOfBounds).
        // Часть портлетов сайдбара MediaWiki (например, "в других проектах") дорисовывается
        // через JS уже после первичной загрузки страницы — поэтому сначала дожидаемся, пока
        // коллекция станет непустой (со штатным Selenide-таймаутом), и только потом берём
        // snapshot. Сам snapshot — это уже разрешённые WebElement-ссылки, а не live-локатор,
        // поэтому если DOM всё равно переотрисовался ПОСЛЕ snapshot(), Selenide не может сам
        // перелокатить протухший элемент — приходится заново брать snapshot целиком и повторить.
        RuntimeException lastException = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                ElementsCollection snapshot = elementsCollection.shouldHave(CollectionCondition.sizeGreaterThan(0)).snapshot();
                return snapshot.get(getRandom(snapshot.size())).shouldBe(visible);
            } catch (StaleElementReferenceException | IndexOutOfBoundsException e) {
                lastException = e;
            }
        }
        throw lastException;
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
