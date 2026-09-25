package ru.at.library.web.step.selenideelement;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.ru.А;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Но;
import io.cucumber.java.ru.То;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.entities.CommonStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.IStepResult;
import ru.at.library.web.scenario.WebScenario;

import java.time.Duration;
import java.util.Objects;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.steps.OtherSteps.getTranslateNormalizeSpaceText;

/**
 * Проверки SelenideElement.
 *
 * Шаги "в блоке ..." и без него объединены в один метод на каждую проверку: короткий
 * вариант шага получает фиктивную пустую захватывающую группу {@code ()} в начале
 * regex, чтобы количество групп совпадало с "блочным" вариантом.
 */
@Log4j2
public class SelenideElementCheckSteps {

    @И("^()элемент \"([^\"]*)\" отображается на странице$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" отображается на странице$")
    public void isVisible(String blockName, String elementName) {
        isVisible(resolveOwner(blockName).getElement(elementName));
    }

    @И("^отображается элемент с текстом \"([^\"]*)\"$")
    @А("^отображается элемент с текстом$")
    public IStepResult elementWithTextIsVisible(String text) {
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(getPropertyOrStringVariableOrValue(text))));
        isVisible(element);
        return new CommonStepResult(element);
    }

    @А("^в блоке \"([^\"]*)\" отображается элемент с текстом \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" отображается элемент с текстом$")
    public IStepResult elementWithTextIsVisible(String blockName, String text) {
        // Ищем элемент по тексту во всём документе, без использования getSelf(),
        // чтобы избежать поиска по несуществующему *[name='self']
        String resolved = getPropertyOrStringVariableOrValue(text);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        isVisible(element);
        return new CommonStepResult(element);
    }

    /**
     * Проверка появления элемента(не списка) на странице в течение Configuration.timeout.
     * В случае, если свойство "waitingCustomElementsTimeout" в properties не задано,
     * таймаут равен 10 секундам
     */
    public void isVisible(SelenideElement element) {
        element.shouldHave(appear);
    }

    @И("^()элемент \"([^\"]*)\" отобразится на странице в течение (\\d+) (?:секунд|секунды)")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" отобразится на странице в течение (\\d+) (?:секунд|секунды)")
    public void isVisibleWithTimeout(String blockName, String elementName, int seconds) {
        resolveOwner(blockName).getElement(elementName).shouldHave(appear, Duration.ofSeconds(seconds));
    }

    @И("^()элемент \"([^\"]*)\" не отображается на странице$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не отображается на странице$")
    public void isHidden(String blockName, String elementName) {
        isHidden(resolveOwner(blockName).getElement(elementName));
    }

    @И("^не отображается элемент с текстом \"([^\"]*)\"$")
    @А("^не отображается элемент с текстом$")
    public IStepResult elementWithTextIsHidden(String text) {
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(getPropertyOrStringVariableOrValue(text))));
        isHidden(element);
        return new CommonStepResult(element);
    }

    @А("^в блоке \"([^\"]*)\" не отображается элемент с текстом \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" не отображается элемент с текстом$")
    public IStepResult elementWithTextIsHidden(String blockName, String text) {
        // Аналогично видимому элементу: ищем по тексту глобально, без getSelf()
        String resolved = getPropertyOrStringVariableOrValue(text);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        isHidden(element);
        return new CommonStepResult(element);
    }

    /**
     * Проверка появления элемента(не списка) на странице в течение Configuration.timeout.
     * В случае, если свойство "waitingCustomElementsTimeout" в properties не задано,
     * таймаут равен 10 секундам
     *
     * @param element SelenideElement
     */
    public void isHidden(SelenideElement element) {
        element.shouldHave(hidden);
    }

    @И("^()элемент \"([^\"]*)\" не (?:отобразится|отображается) на странице в течение (\\d+) (?:секунд|секунды)")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не (?:отобразится|отображается) на странице в течение (\\d+) (?:секунд|секунды)")
    public void isHiddenWithTimeout(String blockName, String elementName, int seconds) {
        resolveOwner(blockName).getElement(elementName).shouldHave(hidden, Duration.ofSeconds(seconds));
    }

    @И("^()элемент \"([^\"]*)\" в фокусе$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" в фокусе$")
    public void isFocused(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(Condition.focused);
    }

    /**
     * Проверка на то, что элемент отображается на странице, является картинкой (img) и картинка загрузилась
     */
    @И("^()элемент \"([^\"]*)\" является изображением и отображается на странице")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" является изображением и отображается на странице")
    public void isImageLoaded(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName)
                .shouldHave(image)
                .shouldHave(visible);
    }

    @И("^()элемент \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    public void inBounds(String blockName, String elementName, String boundsCondition) {
        inBounds(resolveOwner(blockName).getElement(elementName), boundsCondition);
    }

    @И("^()элемент содержащий текст \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    @А("^в блоке \"([^\"]*)\" элемент содержащий текст \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    public void elementWihTextInBounds(String blockName, String expectedValue, String boundsCondition) {
        // Для совместимости шагов с блоком используем такой же глобальный поиск по тексту
        // (blockName не используется)
        String resolved = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        inBounds(element, boundsCondition);
    }

    /**
     * Проверка появления элемента(не списка) в видимой части браузера
     *
     * @param element элемент для проверки
     */
    public static void inBounds(SelenideElement element, String boundsCondition) {
        int elementLeftBound = element.getLocation().x;
        int elementUpperBound = element.getLocation().y;
        int elementRightBound = elementLeftBound + element.getSize().width;
        int elementLowerBound = elementUpperBound + element.getSize().height;

        Number winLeftBound = executeJavaScript("return window.pageXOffset");
        Number winUpperBound = executeJavaScript("return window.pageYOffset");
        Number winWidth = executeJavaScript("return document.documentElement.clientWidth");
        Number winHeight = executeJavaScript("return document.documentElement.clientHeight");
        if (winLeftBound == null || winUpperBound == null || winWidth == null || winHeight == null) {
            throw new RuntimeException("Ошибка при получении размера окан браузера или координат элемента");
        }
        long left = winLeftBound.longValue();
        long upper = winUpperBound.longValue();
        long right = left + winWidth.longValue();
        long lower = upper + winHeight.longValue();

        boolean inBounds = left <= elementLeftBound
                && upper <= elementUpperBound
                && right >= elementRightBound
                && lower >= elementLowerBound;

        boolean expectedCondition = boundsCondition.equals("в");

        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("Элемент %s видимой части браузера.\nВидимая область: %d %d %d %d\nКоординаты элемента: %d %d %d %d", boundsCondition,
                        left, upper, right, lower, elementLeftBound, elementUpperBound, elementRightBound, elementLowerBound),
                inBounds,
                is(equalTo(expectedCondition))
        );
    }

    /**
     * Проверка, что элемент на странице доступен для нажатия
     */
    @И("^()(?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия$")
    @А("^в блоке \"([^\"]*)\" (?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия$")
    public void isClickable(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(enabled);
    }

    @И("^()(?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия в течение (\\d+) (?:секунд|секунды)$")
    @А("^в блоке \"([^\"]*)\" (?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия в течение (\\d+) (?:секунд|секунды)$")
    public void isClickableWithTimeout(String blockName, String elementName, int second) {
        resolveOwner(blockName).getElement(elementName).shouldHave(enabled, Duration.ofSeconds(second));
    }

    /**
     * Проверка, что элемент недоступен для нажатия
     */
    @И("^()(?:кнопка|элемент) \"([^\"]*)\" (?:недоступна|недоступен) для нажатия$")
    @А("^в блоке \"([^\"]*)\" (?:кнопка|элемент) \"([^\"]*)\" (?:недоступна|недоступен) для нажатия$")
    public void isDisabled(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(disabled);
    }

    /**
     * Проверка, что поле для ввода пусто
     */
    @И("^()поле \"([^\"]*)\" пусто$")
    @А("^в блоке \"([^\"]*)\" поле \"([^\"]*)\" пусто$")
    public void inputIsEmpty(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(empty);
    }

    @И("^()поле \"([^\"]*)\" не пусто$")
    @А("^в блоке \"([^\"]*)\" поле \"([^\"]*)\" не пусто$")
    public void inputIsNotEmpty(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldNotBe(Condition.empty);
    }

    /**
     * Сохранение значения элемента в переменную
     */
    @И("^()текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public void saveElementTextToVar(String blockName, String elementName, String variableName) {
        String text = resolveOwner(blockName).getElement(elementName).getText();
        CoreScenario.getInstance().setVar(variableName, text);
        log.trace("Значение [" + text + "] сохранено в переменную [" + variableName + "]");
    }

    /**
     * Проверка, что у элемента есть атрибут с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    @И("^()элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\"$")
    public void containsAttribute(String blockName, String elementName, String attribute) {
        attribute = getPropertyOrStringVariableOrValue(attribute);
        resolveOwner(blockName).getElement(elementName).shouldHave(attribute(attribute));
    }

    /**
     * Проверка, что у элемента есть атрибут с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    @И("^()элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void containsAttributeWithValue(String blockName, String elementName, String attribute, String expectedAttributeValue) {
        attribute = getPropertyOrStringVariableOrValue(attribute);
        expectedAttributeValue = getPropertyOrStringVariableOrValue(expectedAttributeValue);
        resolveOwner(blockName).getElement(elementName).shouldHave(attribute(attribute, expectedAttributeValue));
    }

    /**
     * Проверка, что у элемента есть css с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    @И("^()элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void containsCssWithValue(String blockName, String elementName, String cssName, String cssValue) {
        cssName = getPropertyOrStringVariableOrValue(cssName);
        cssValue = getPropertyOrStringVariableOrValue(cssValue);
        resolveOwner(blockName).getElement(elementName).shouldHave(cssValue(cssName, cssValue));
    }

    /**
     * Проверка, что у элемента нет css с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    @И("^()элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void notContainsCssWithValue(String blockName, String elementName, String cssName, String cssValue) {
        cssName = getPropertyOrStringVariableOrValue(cssName);
        cssValue = getPropertyOrStringVariableOrValue(cssValue);
        resolveOwner(blockName).getElement(elementName).shouldNotHave(cssValue(cssName, cssValue));
    }

    /**
     * Проверка, что значение в поле содержит текст, указанный в шаге
     * (в приоритете: из property, из переменной сценария, значение аргумента).
     * Не чувствителен к регистру
     */
    @А("^()элемент \"([^\"]*)\" содержит текст")
    @И("^()элемент \"([^\"]*)\" содержит текст \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст")
    @Но("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст \"([^\"]*)\"$")
    public void containsText(String blockName, String elementName, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        resolveOwner(blockName).getElement(elementName).shouldHave(
                or("Текст элемента содержит",
                        text(expectedValue),
                        value(expectedValue)));
    }

    /**
     * Проверка, что значение в поле содержит текст, указанный в шаге
     * (в приоритете: из property, из переменной сценария, значение аргумента).
     * Не чувствителен к регистру
     */
    @А("^()элемент \"([^\"]*)\" не содержит текст")
    @И("^()элемент \"([^\"]*)\" не содержит текст \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не содержит текст")
    @Но("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не содержит текст \"([^\"]*)\"$")
    public void notContainsText(String blockName, String elementName, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        resolveOwner(blockName).getElement(elementName).shouldHave(
                and("Текст элемента не содержит",
                        not(text(expectedValue)),
                        not(value(expectedValue))));
    }

    /**
     * Проверка, что текста в поле равен значению, указанному в шаге
     * (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    @И("^()текст элемента \"([^\"]*)\" равен \"([^\"]*)\"$")
    @А("^()текст элемента \"([^\"]*)\" равен$")
    @Но("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" равен \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" равен$")
    public void hasExactText(String blockName, String elementName, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        resolveOwner(blockName).getElement(elementName).shouldHave(
                or("Текст элемента равен",
                        exactText(expectedValue),
                        exactValue(expectedValue)));
    }

    @А("^()текст элемента \"([^\"]*)\" соответствует регулярному выражению \"([^\"]*)\"$")
    @И("^()текст элемента \"([^\"]*)\" соответствует регулярному выражению$")
    @Но("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" соответствует регулярному выражению \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" соответствует регулярному выражению$")
    public void matchesRegexp(String blockName, String elementName, String expectedValue) {
        expectedValue = OtherSteps.getPropertyOrStringVariableOrValue(expectedValue);
        resolveOwner(blockName).getElement(elementName).shouldHave(Condition.matchText(expectedValue));
    }

    /**
     * Производится проверка количества символов в элементе со значением, указанным в шаге
     */
    @И("^()в элементе \"([^\"]*)\" содержится (\\d+) символов$")
    @А("^в блоке \"([^\"]*)\" в элементе \"([^\"]*)\" содержится (\\d+) символов$")
    public void checkFieldSymbolsCount(String blockName, String elementName, int expectedLength) {
        SelenideElement element = resolveOwner(blockName).getElement(elementName);
        element.should(visible);
        int length;
        if (element.getTagName().equalsIgnoreCase("input")) {
            length = Objects.requireNonNull(element.getAttribute("value")).length();
        } else {
            length = element.getText().length();
        }
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("Неверное количество символов. Ожидаемый результат: %s, текущий результат: %s", expectedLength, length),
                length,
                is(equalTo(expectedLength))
        );
    }

    /**
     * -----------------------------------------Проверки радиокнопок/чекбоксов------------------------------------------
     */

    /**
     * Проверка, что радиокнопка выбрана
     */
    @И("^()радиокнопка \"([^\"]*)\" выбрана$")
    @А("^в блоке \"([^\"]*)\" радиокнопка \"([^\"]*)\" выбрана$")
    public void radioButtonIsSelected(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(selected);
    }

    /**
     * Проверка, что радиокнопка не выбрана
     */
    @И("^()радиокнопка \"([^\"]*)\" не выбрана")
    @А("^в блоке \"([^\"]*)\" радиокнопка \"([^\"]*)\" не выбрана")
    public void radioButtonIsNotSelected(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(not(selected));
    }

    /**
     * Проверка, что чекбокс отмечен
     */
    @И("^()чекбокс \"([^\"]*)\" выбран$")
    @А("^в блоке \"([^\"]*)\" чекбокс \"([^\"]*)\" выбран$")
    public void checkBoxIsChecked(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(checked);
    }

    /**
     * Проверка, что чекбокс не отмечен
     */
    @И("^()чекбокс \"([^\"]*)\" не выбран$")
    @А("^в блоке \"([^\"]*)\" чекбокс \"([^\"]*)\" не выбран$")
    public void checkBoxIsNotChecked(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).shouldHave(not(checked));
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
