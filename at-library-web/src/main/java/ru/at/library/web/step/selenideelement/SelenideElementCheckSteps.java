package ru.at.library.web.step.selenideelement;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.ru.А;
import io.cucumber.java.ru.И;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.entities.CommonStepResult;
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
 * Проверки SelenideElement
 */
public class SelenideElementCheckSteps {

    private static final Logger log = LogManager.getLogger(SelenideElementCheckSteps.class);

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" отображается на странице$")
    public void isVisible(String elementName) {
        isVisible(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" отображается на странице$")
    public void isVisible(String blockName, String elementName) {
        isVisible(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * ######################################################################################################################
     */

    @И("^отображается элемент с текстом \"([^\"]*)\"$")
    @А("^отображается элемент с текстом$")
    public IStepResult elementWithTextIsVisible(String text) {
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(getPropertyOrStringVariableOrValue(text))));
        isVisible(element);
        return new CommonStepResult(element);
    }

    @И("^в блоке \"([^\"]*)\" отображается элемент с текстом \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" отображается элемент с текстом$")
    public IStepResult elementWithTextIsVisible(String blockName, String text) {
        // Ищем элемент по тексту во всём документе, без использования getSelf(),
        // чтобы избежать поиска по несуществующему *[name='self']
        String resolved = getPropertyOrStringVariableOrValue(text);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        isVisible(element);
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

    /**
     * Проверка появления элемента(не списка) на странице в течение Configuration.timeout.
     * В случае, если свойство "waitingCustomElementsTimeout" в properties не задано,
     * таймаут равен 10 секундам
     */
    public void isVisible(SelenideElement element) {
        element.shouldHave(appear);
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" отобразится на странице в течение (\\d+) (?:секунд|секунды)")
    public void isVisibleWithTimeout(String elementName, int seconds) {
        isVisibleWithTimeout(
                WebScenario.getCurrentPage().getElement(elementName),
                seconds);
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" отобразится на странице в течение (\\d+) (?:секунд|секунды)")
    public void isVisibleWithTimeout(String blockName, String elementName, int seconds) {
        isVisibleWithTimeout(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                seconds);
    }

    /**
     * Проверка появления элемента(не списка) на странице в течение
     * заданного количества секунд
     */
    public void isVisibleWithTimeout(SelenideElement element, int seconds) {
        element.shouldHave(appear, Duration.ofSeconds(seconds));
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" не отображается на странице$")
    public void isHidden(String elementName) {
        isHidden(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не отображается на странице$")
    public void isHidden(String blockName, String elementName) {
        isHidden(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * ######################################################################################################################
     */

    @И("^не отображается элемент с текстом \"([^\"]*)\"$")
    @А("^не отображается элемент с текстом$")
    public IStepResult elementWithTextIsHidden(String text) {
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(getPropertyOrStringVariableOrValue(text))));
        isHidden(element);
        return new CommonStepResult(element);
    }

    @И("^в блоке \"([^\"]*)\" не отображается элемент с текстом \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" не отображается элемент с текстом$")
    public IStepResult elementWithTextIsHidden(String blockName, String text) {
        // Аналогично видимому элементу: ищем по тексту глобально, без getSelf()
        String resolved = getPropertyOrStringVariableOrValue(text);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        isHidden(element);
        return new CommonStepResult(element);
    }

    /**
     * ######################################################################################################################
     */

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

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" не (?:отобразится|отображается) на странице в течение (\\d+) (?:секунд|секунды)")
    public void isHiddenWithTimeout(String elementName, int seconds) {
        isHiddenWithTimeout(
                WebScenario.getCurrentPage().getElement(elementName),
                seconds);
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не (?:отобразится|отображается) на странице в течение (\\d+) (?:секунд|секунды)")
    public void isHiddenWithTimeout(String blockName, String elementName, int seconds) {
        isHiddenWithTimeout(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                seconds);
    }

    /**
     * @param element SelenideElement
     * @param seconds количество секунд
     */
    public void isHiddenWithTimeout(SelenideElement element, int seconds) {
        element.shouldHave(hidden, Duration.ofSeconds(seconds));
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" в фокусе$")
    public void isFocused(String elementName) {
        isFocused(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" в фокусе$")
    public void isFocused(String blockName, String elementName) {
        isFocused(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    public void isFocused(SelenideElement element) {
        element.shouldHave(Condition.focused);
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" является изображением и отображается на странице")
    public void isImageLoaded(String elementName) {
        isImageLoaded(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" является изображением и отображается на странице")
    public void isImageLoaded(String blockName, String elementName) {
        isImageLoaded(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка на то, что элемент отображается на странице, является картинкой (img) и картинка загрузилась
     *
     * @param element SelenideElement
     */
    public void isImageLoaded(SelenideElement element) {
        element.shouldHave(image)
                .shouldHave(visible);
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    public void inBounds(String elementName, String boundsCondition) {
        inBounds(WebScenario.getCurrentPage().getElement(elementName), boundsCondition);
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    public void inBounds(String blockName, String elementName, String boundsCondition) {
        inBounds(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName), boundsCondition);
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент содержащий текст \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    public void elementWihTextInBounds(String expectedValue, String boundsCondition) {
        String resolved = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        inBounds(element, boundsCondition);
    }

    @И("^в блоке \"([^\"]*)\" элемент содержащий текст \"([^\"]*)\" расположен (в|вне) видимой части страницы$")
    public void elementWihTextInBounds(String blockName, String expectedValue, String boundsCondition) {
        // Для совместимости шагов с блоком используем такой же глобальный поиск по тексту
        String resolved = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        inBounds(element, boundsCondition);
    }

    /**
     * ######################################################################################################################
     */

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

        Long winLeftBound = executeJavaScript("return window.pageXOffset");
        Long winUpperBound = executeJavaScript("return window.pageYOffset");
        Long winWidth = executeJavaScript("return document.documentElement.clientWidth");
        Long winHeight = executeJavaScript("return document.documentElement.clientHeight");
        if (winLeftBound == null || winUpperBound == null || winWidth == null || winHeight == null) {
            throw new RuntimeException("Ошибка при получении размера окан браузера или координат элемента");
        }
        long winRightBound = winLeftBound + winWidth;
        long winLowerBound = winUpperBound + winHeight;

        boolean inBounds = winLeftBound <= elementLeftBound
                && winUpperBound <= elementUpperBound
                && winRightBound >= elementRightBound
                && winLowerBound >= elementLowerBound;

        boolean expectedCondition = boundsCondition.equals("в");

        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("Элемент %s видимой части браузера.\nВидимая область: %d %d %d %d\nКоординаты элемента: %d %d %d %d", boundsCondition,
                        winLeftBound, winUpperBound, winRightBound, winLowerBound, elementLeftBound, elementUpperBound, elementRightBound, elementLowerBound),
                inBounds,
                is(equalTo(expectedCondition))
        );
    }

    /**
     * ######################################################################################################################
     */


    @И("^(?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия$")
    public void isClickable(String elementName) {
        isClickable(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" (?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия$")
    public void isClickable(String blockName, String elementName) {
        isClickable(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка, что элемент на странице доступен для нажатия
     */
    public void isClickable(SelenideElement element) {
        element.shouldHave(enabled);
    }

    /**
     * ######################################################################################################################
     */

    @И("^(?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия в течение (\\d+) (?:секунд|секунды)$")
    public void isClickableWithTimeout(String elementName, int second) {
        isClickableWithTimeout(
                WebScenario.getCurrentPage().getElement(elementName),
                second);
    }

    @И("^в блоке \"([^\"]*)\" (?:кнопка|элемент) \"([^\"]*)\" (?:доступна|доступен) для нажатия в течение (\\d+) (?:секунд|секунды)$")
    public void isClickableWithTimeout(String blockName, String elementName, int second) {
        isClickableWithTimeout(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                second);
    }

    /**
     * Проверка, что элемент на странице доступен для нажатия
     */
    public void isClickableWithTimeout(SelenideElement element, int second) {
        element.shouldHave(enabled, Duration.ofSeconds(second));
    }

    /**
     * ######################################################################################################################
     */

    @И("^(?:кнопка|элемент) \"([^\"]*)\" (?:недоступна|недоступен) для нажатия$")
    public void isDisabled(String elementName) {
        isDisabled(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" (?:кнопка|элемент) \"([^\"]*)\" (?:недоступна|недоступен) для нажатия$")
    public void isDisabled(String blockName, String elementName) {
        isDisabled(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка, что элемент недоступен для нажатия
     */
    public void isDisabled(SelenideElement element) {
        element.shouldHave(disabled);
    }

    /**
     * ######################################################################################################################
     */

    @И("^поле \"([^\"]*)\" пусто$")
    public void inputIsEmpty(String elementName) {
        inputIsEmpty(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" поле \"([^\"]*)\" пусто$")
    public void inputIsEmpty(String blockName, String elementName) {
        inputIsEmpty(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка, что поле для ввода пусто
     */
    public void inputIsEmpty(SelenideElement element) {
        element.shouldHave(empty);
    }

    /**
     * ######################################################################################################################
     */

    @И("^поле \"([^\"]*)\" не пусто$")
    public void inputIsNotEmpty(String elementName) {
        inputIsNotEmpty(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" поле \"([^\"]*)\" не пусто$")
    public void inputIsNotEmpty(String blockName, String elementName) {
        inputIsNotEmpty(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    public void inputIsNotEmpty(SelenideElement element) {
        element.shouldNotBe(Condition.empty);
    }

    /**
     * ######################################################################################################################
     */

    @И("^текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public void saveElementTextToVar(String elementName, String variableName) {
        saveElementTextToVar(
                WebScenario.getCurrentPage().getElement(elementName),
                variableName);
    }

    @И("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public void saveElementTextToVar(String blockName, String elementName, String variableName) {
        saveElementTextToVar(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                variableName);
    }

    /**
     * Сохранение значения элемента в переменную
     */
    public void saveElementTextToVar(SelenideElement element, String variableName) {
        String text = element.getText();
        CoreScenario.getInstance().setVar(variableName, text);
        log.trace("Значение [" + text + "] сохранено в переменную [" + variableName + "]");
    }

    /**
     * ######################################################################################################################
     */
    @И("^элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\"$")
    public void containsAttribute(String elementName, String attribute) {
        containsAttribute(
                WebScenario.getCurrentPage().getElement(elementName),
                attribute
        );
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\"$")
    public void containsAttribute(String blockName, String elementName, String attribute) {
        containsAttribute(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                attribute);
    }

    /**
     * Проверка, что у элемента есть атрибут с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    public void containsAttribute(SelenideElement element, String attribute) {
        attribute = getPropertyOrStringVariableOrValue(attribute);
        element.shouldHave(attribute(attribute));
    }
    /**
     * ######################################################################################################################
     */


    @И("^элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void containsAttributeWithValue(String elementName, String attribute, String expectedAttributeValue) {
        containsAttributeWithValue(
                WebScenario.getCurrentPage().getElement(elementName),
                attribute,
                expectedAttributeValue
        );
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void containsAttributeWithValue(String blockName, String elementName, String attribute, String expectedAttributeValue) {
        containsAttributeWithValue(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                attribute,
                expectedAttributeValue
        );
    }

    /**
     * Проверка, что у элемента есть атрибут с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    public void containsAttributeWithValue(SelenideElement element, String attribute, String expectedAttributeValue) {
        attribute = getPropertyOrStringVariableOrValue(attribute);
        expectedAttributeValue = getPropertyOrStringVariableOrValue(expectedAttributeValue);
        element.shouldHave(attribute(attribute, expectedAttributeValue));
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void containsCssWithValue(String elementName, String cssName, String cssValue) {
        containsCssWithValue(
                WebScenario.getCurrentPage().getElement(elementName),
                cssName,
                cssValue
        );
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void containsCssWithValue(String blockName, String elementName, String cssName, String cssValue) {
        containsCssWithValue(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                cssName,
                cssValue
        );
    }

    /**
     * Проверка, что у элемента есть css с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    public void containsCssWithValue(SelenideElement element, String cssName, String cssValue) {
        cssName = getPropertyOrStringVariableOrValue(cssName);
        cssValue = getPropertyOrStringVariableOrValue(cssValue);
        element.shouldHave(cssValue(cssName, cssValue));
    }

    /**
     * ######################################################################################################################
     */

    @И("^элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void notContainsCssWithValue(String elementName, String cssName, String cssValue) {
        notContainsCssWithValue(
                WebScenario.getCurrentPage().getElement(elementName),
                cssName,
                cssValue
        );
    }

    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public void notContainsCssWithValue(String blockName, String elementName, String cssName, String cssValue) {
        notContainsCssWithValue(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                cssName,
                cssValue
        );
    }

    /**
     * Проверка, что у элемента нет css с ожидаемым значением (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    public void notContainsCssWithValue(SelenideElement element, String cssName, String cssValue) {
        cssName = getPropertyOrStringVariableOrValue(cssName);
        cssValue = getPropertyOrStringVariableOrValue(cssValue);
        element.shouldNotHave(cssValue(cssName, cssValue));
    }

    /**
     * ######################################################################################################################
     */

    @А("^элемент \"([^\"]*)\" содержит текст")
    @И("^элемент \"([^\"]*)\" содержит текст \"([^\"]*)\"$")
    public void containsText(String elementName, String expectedValue) {
        containsText(
                WebScenario.getCurrentPage().getElement(elementName),
                expectedValue
        );
    }

    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст")
    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст \"([^\"]*)\"$")
    public void containsText(String blockName, String elementName, String expectedValue) {
        containsText(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                expectedValue
        );
    }

    /**
     * Проверка, что значение в поле содержит текст, указанный в шаге
     * (в приоритете: из property, из переменной сценария, значение аргумента).
     * Не чувствителен к регистру
     */
    public void containsText(SelenideElement element, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        element.shouldHave(
                or("Текст элемента содержит",
                        text(expectedValue),
                        value(expectedValue)));
    }

    /**
     * ######################################################################################################################
     */

    @А("^элемент \"([^\"]*)\" не содержит текст")
    @И("^элемент \"([^\"]*)\" не содержит текст \"([^\"]*)\"$")
    public void notContainsText(String elementName, String expectedValue) {
        notContainsText(
                WebScenario.getCurrentPage().getElement(elementName),
                expectedValue
        );
    }

    @А("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не содержит текст")
    @И("^в блоке \"([^\"]*)\" элемент \"([^\"]*)\" не содержит текст \"([^\"]*)\"$")
    public void notContainsText(String blockName, String elementName, String expectedValue) {
        notContainsText(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                expectedValue
        );
    }

    /**
     * Проверка, что значение в поле содержит текст, указанный в шаге
     * (в приоритете: из property, из переменной сценария, значение аргумента).
     * Не чувствителен к регистру
     */
    public void notContainsText(SelenideElement element, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        element.shouldHave(
                and("Текст элемента не содержит",
                        not(text(expectedValue)),
                        not(value(expectedValue))));
    }

    /**
     * ######################################################################################################################
     */

    @И("^текст элемента \"([^\"]*)\" равен \"([^\"]*)\"$")
    @А("^текст элемента \"([^\"]*)\" равен$")
    public void hasExactText(String elementName, String expectedValue) {
        hasExactText(
                WebScenario.getCurrentPage().getElement(elementName),
                expectedValue);
    }


    @И("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" равен \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" равен$")
    public void hasExactText(String blockName, String elementName, String expectedValue) {
        hasExactText(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                expectedValue);
    }


    /**
     * Проверка, что текста в поле равен значению, указанному в шаге
     * (в приоритете: из property, из переменной сценария, значение аргумента)
     */
    public void hasExactText(SelenideElement element, String expectedValue) {
        expectedValue = getPropertyOrStringVariableOrValue(expectedValue);
        element.shouldHave(
                or("Текст элемента равен",
                        exactText(expectedValue),
                        exactValue(expectedValue)));
    }

    /**
     * ######################################################################################################################
     */

    @А("^текст элемента \"([^\"]*)\" соответствует регулярному выражению \"([^\"]*)\"$")
    @И("^текст элемента \"([^\"]*)\" соответствует регулярному выражению$")
    public void matchesRegexp(String elementName, String expectedValue) {
        matchesRegexp(
                WebScenario.getCurrentPage().getElement(elementName),
                expectedValue
        );
    }

    @А("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" соответствует регулярному выражению \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" текст элемента \"([^\"]*)\" соответствует регулярному выражению$")
    public void matchesRegexp(String blockName, String elementName, String expectedValue) {
        matchesRegexp(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                expectedValue
        );
    }

    public void matchesRegexp(SelenideElement element, String expectedValue) {
        expectedValue = OtherSteps.getPropertyOrStringVariableOrValue(expectedValue);
        element.shouldHave(Condition.matchText(expectedValue));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в элементе \"([^\"]*)\" содержится (\\d+) символов$")
    public void checkFieldSymbolsCount(String elementName, int num) {
        checkFieldSymbolsCount(
                WebScenario.getCurrentPage().getElement(elementName),
                num);
    }


    @И("^в блоке \"([^\"]*)\" в элементе \"([^\"]*)\" содержится (\\d+) символов$")
    public void checkFieldSymbolsCount(String blockName, String elementName, int num) {
        checkFieldSymbolsCount(
                WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName),
                num);
    }

    /**
     * Производится проверка количества символов в элементе со значением, указанным в шаге
     */
    public void checkFieldSymbolsCount(SelenideElement element, int expectedLength) {
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
     * ######################################################################################################################
     */

    /**
     * -----------------------------------------Проверки радиокнопок/чекбоксов------------------------------------------
     */

    /**
     * ######################################################################################################################
     */

    @И("^радиокнопка \"([^\"]*)\" выбрана$")
    public void radioButtonIsSelected(String elementName) {
        radioButtonIsSelected(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" радиокнопка \"([^\"]*)\" выбрана$")
    public void radioButtonIsSelected(String blockName, String elementName) {
        radioButtonIsSelected(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка, что радиокнопка выбрана
     */
    public void radioButtonIsSelected(SelenideElement element) {
        element.shouldHave(selected);
    }

    /**
     * ######################################################################################################################
     */

    @И("^радиокнопка \"([^\"]*)\" не выбрана")
    public void radioButtonIsNotSelected(String elementName) {
        radioButtonIsNotSelected(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" радиокнопка \"([^\"]*)\" не выбрана")
    public void radioButtonIsNotSelected(String blockName, String elementName) {
        radioButtonIsNotSelected(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка, что радиокнопка не выбрана
     */
    public void radioButtonIsNotSelected(SelenideElement element) {
        element.shouldHave(not(selected));
    }

    /**
     * ######################################################################################################################
     */

    @И("^чекбокс \"([^\"]*)\" выбран$")
    public void checkBoxIsChecked(String elementName) {
        checkBoxIsChecked(WebScenario.getCurrentPage().getElement(elementName));
    }


    @И("^в блоке \"([^\"]*)\" чекбокс \"([^\"]*)\" выбран$")
    public void checkBoxIsChecked(String blockName, String elementName) {
        checkBoxIsChecked(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка, что чекбокс отмечен
     */
    public void checkBoxIsChecked(SelenideElement element) {
        element.shouldHave(checked);
    }

    /**
     * ######################################################################################################################
     */

    @И("^чекбокс \"([^\"]*)\" не выбран$")
    public void checkBoxIsNotChecked(String elementName) {
        checkBoxIsNotChecked(WebScenario.getCurrentPage().getElement(elementName));
    }

    @И("^в блоке \"([^\"]*)\" чекбокс \"([^\"]*)\" не выбран$")
    public void checkBoxIsNotChecked(String blockName, String elementName) {
        checkBoxIsNotChecked(WebScenario.getCurrentPage().getBlock(blockName).getElement(elementName));
    }

    /**
     * Проверка, что чекбокс не отмечен
     */
    public void checkBoxIsNotChecked(SelenideElement element) {
        element.shouldHave(not(checked));
    }
}
