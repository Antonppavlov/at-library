package ru.at.library.web.step.selenideelement;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.ru.А;
import io.cucumber.java.ru.И;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.WebScenario;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;

import static com.codeborne.selenide.Selenide.*;
import static ru.at.library.core.steps.OtherSteps.*;

/**
 * Действия с SelenideElement.
 *
 * Шаги "в блоке ..." и без него объединены в один метод на каждое действие: короткий
 * вариант шага получает фиктивную пустую захватывающую группу {@code ()} в начале
 * regex, чтобы количество групп совпадало с "блочным" вариантом. Это сохраняет
 * подсказки/навигацию IntelliJ для обеих формулировок шага (в отличие от одиночной
 * аннотации с {@code (?:...)?}) и исключает делегирование между двумя разными
 * Cucumber-шагами.
 */
@Log4j2
public class SelenideElementActionSteps {

    @И("^()выполнено нажатие на (?:кнопку|элемент) \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" выполнено нажатие на (?:кнопку|элемент) \"([^\"]*)\"$")
    public void clickOnElement(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).click();
    }

    @И("^()выполнено нажатие c ховером на (?:кнопку|элемент) \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" выполнено нажатие c ховером на (?:кнопку|элемент) \"([^\"]*)\"$")
    public void clickOnElementWithHover(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).hover().click();
    }

    @И("^()выполнен ховер на элемент \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" выполнен ховер на элемент \"([^\"]*)\"$")
    public void elementHover(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).hover();
    }

    @И("^()выполнено нажатие на (?:кнопку|элемент) \"([^\"]*)\" и переход на новую вкладку$")
    @И("^в блоке \"([^\"]*)\" выполнено нажатие на (?:кнопку|элемент) \"([^\"]*)\" и переход на новую вкладку$")
    public void clickOnElementAndSwitchToNewTab(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).clear();
        Selenide.switchTo().window(WebDriverRunner.getWebDriver().getWindowHandles().size() - 1);
    }

    @SuppressWarnings("deprecation")
    @И("^()выполнено нажатие на элемент с текстом \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" выполнено нажатие на элемент с текстом \"([^\"]*)\"$")
    public void clickingElementWithText(String blockName, String text) {
        // Блочный вариант шага принимается для совместимости текста (blockName не используется),
        // но не влияет на поиск: элемент с нужным текстом всегда ищется по всему документу.
        String resolved = getPropertyOrStringVariableOrValue(text);
        Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved))).click();
    }

    /**
     * Устанавливается значение (в приоритете: из property, из переменной сценария, значение аргумента) в заданное поле.
     * Перед использованием поле нужно очистить
     */
    @А("^()в поле \"([^\"]*)\" введено значение$")
    @И("^()в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" введено значение$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" введено значение \"([^\"]*)\"$")
    public String setFieldValue(String blockName, String elementName, String value) {
        value = getPropertyOrStringVariableOrValue(value);
        SelenideElement element = resolveOwner(blockName).getElement(elementName);
        element.shouldHave(Condition.visible);
        element.setValue(value);
        return value;
    }

    /**
     * Набирается значение посимвольно (в приоритете: из property, из переменной сценария, значение аргумента) в заданное поле.
     */
    @А("^()в поле \"([^\"]*)\" посимвольно набирается значение$")
    @И("^()в поле \"([^\"]*)\" посимвольно набирается значение \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" посимвольно набирается значение$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" посимвольно набирается значение \"([^\"]*)\"$")
    public void sendKeysCharacterByCharacter(String blockName, String elementName, String value) {
        value = getPropertyOrStringVariableOrValue(value);
        SelenideElement element = resolveOwner(blockName).getElement(elementName);
        element.shouldHave(Condition.visible);
        for (char character : value.toCharArray()) {
            element.sendKeys(String.valueOf(character));
            sleep(100);
        }
    }

    /**
     * Добавление строки (в приоритете: из property, из переменной сценария, значение аргумента) в поле к уже заполненой строке
     */
    @А("^()в поле \"([^\"]*)\" дописывается значение$")
    @И("^()в поле \"([^\"]*)\" дописывается значение \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" дописывается значение$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" дописывается значение \"([^\"]*)\"$")
    public void valueIsAppended(String blockName, String elementName, String value) {
        value = getPropertyOrStringVariableOrValue(value);
        SelenideElement element = resolveOwner(blockName).getElement(elementName);
        String oldValue = element.getValue();
        if (oldValue == null || oldValue.isEmpty()) {
            oldValue = element.getText();
        }
        element.setValue("");
        element.setValue(oldValue + value);
    }

    /**
     * Ввод в поле текущей даты в заданном формате.
     * При неверном формате используется dd.MM.yyyy
     */
    @И("^()в поле \"([^\"]*)\" набирается текущая дата в формате \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" набирается текущая дата в формате \"([^\"]*)\"$")
    public void currentDateIsTypedInTheFormat(String blockName, String elementName, String dateFormat) {
        SelenideElement element = resolveOwner(blockName).getElement(elementName);

        long date = System.currentTimeMillis();
        String currentStringDate;
        try {
            currentStringDate = new SimpleDateFormat(dateFormat).format(date);
        } catch (IllegalArgumentException ex) {
            currentStringDate = new SimpleDateFormat("dd.MM.yyyy").format(date);
            log.trace("Неверный формат даты. Будет использоваться значание по умолчанию в формате dd.MM.yyyy");
        }
        element.setValue("");
        element.setValue(currentStringDate);
        log.trace("Текущая дата " + currentStringDate);
    }

    /**
     * Ввод в поле указанного текста (в приоритете: из property, из переменной сценария, значение аргумента),
     * используя буфер обмена и клавиши SHIFT + INSERT
     */
    @И("^()в поле \"([^\"]*)\" с помощью горячих клавиш вставлено значение \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" с помощью горячих клавиш вставлено значение \"([^\"]*)\"$")
    public void pasteValueToTextField(String blockName, String elementName, String value) {
        value = getPropertyOrStringVariableOrValue(value);
        SelenideElement element = resolveOwner(blockName).getElement(elementName);

        ClipboardOwner clipboardOwner = (clipboard, contents) -> {
        };
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(value);
        clipboard.setContents(stringSelection, clipboardOwner);
        element.sendKeys(Keys.chord(Keys.SHIFT, Keys.INSERT));
    }

    /**
     * Очищается заданное поле
     */
    @И("^()очищено поле \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" очищено поле \"([^\"]*)\"$")
    public void cleanInput(String blockName, String elementName) {
        SelenideElement element = resolveOwner(blockName).getElement(elementName);
        element.clear();

        if (element.is(Condition.not(Condition.empty))) {
            element.sendKeys(Keys.chord(Keys.CONTROL + "a" + Keys.BACK_SPACE));
        }

        if (element.is(Condition.not(Condition.empty)) && element.getValue() != null) {
            for (int i = 0; i < element.getValue().length(); ++i) {
                element.sendKeys(Keys.BACK_SPACE);
            }
        }
    }

    @И("^()в поле \"([^\"]*)\" введено \"([^\"]*)\" случайных символов на (кириллице|латинице)$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" введено \"([^\"]*)\" случайных символов на (кириллице|латинице)$")
    public void setRandomCharSequence(String blockName, String elementName, String seqLengthString, String lang) {
        setRandomCharSequence(resolveOwner(blockName).getElement(elementName), seqLengthString, lang);
    }

    /**
     * Ввод в поле случайной последовательности латинских или кириллических букв задаваемой длины
     */
    public String setRandomCharSequence(SelenideElement element, String seqLengthString, String lang) {
        int seqLength = Integer.parseInt(seqLengthString);

        String charSeq = getRandCharSequence(seqLength, lang);
        element.setValue(charSeq);
        log.trace("Строка случайных символов равна :" + charSeq);

        return charSeq;
    }

    /**
     * Ввод в поле случайной последовательности латинских или кириллических букв задаваемой длины и сохранение этого значения в переменную
     */
    @И("^()в поле \"([^\"]*)\" введено \"([^\"]*)\" случайных символов на (кириллице|латинице) и сохранено в переменную \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" введено \"([^\"]*)\" случайных символов на (кириллице|латинице) и сохранено в переменную \"([^\"]*)\"$")
    public void setRandomCharSequenceAndSaveToVar(String blockName, String elementName, String seqLengthString, String lang, String varName) {
        String charSeq = setRandomCharSequence(resolveOwner(blockName).getElement(elementName), seqLengthString, lang);
        CoreScenario.getInstance().setVar(varName, charSeq);
    }

    @И("^()в поле \"([^\"]*)\" введено случайное число из \"([^\"]*)\" (?:цифр|цифры)$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" введено случайное число из \"([^\"]*)\" (?:цифр|цифры)$")
    public void inputRandomNumSequence(String blockName, String elementName, String seqLengthString) {
        inputRandomNumSequence(resolveOwner(blockName).getElement(elementName), seqLengthString);
    }

    /**
     * Ввод в поле случайной последовательности цифр задаваемой длины
     *
     * @return сгенерированное число
     */
    public String inputRandomNumSequence(SelenideElement element, String seqLengthString) {
        seqLengthString = getPropertyOrStringVariableOrValue(seqLengthString);

        String randomNumeric = randomNumSequence(seqLengthString);
        element.shouldHave(Condition.visible);
        element.setValue(randomNumeric);
        return randomNumeric;
    }

    @Step("Генерация случайного числа из '{seqLengthString}' цифр")
    public String randomNumSequence(String seqLengthString) {
        int seqLength = Integer.parseInt(seqLengthString);
        return getRandNumSequence(seqLength);
    }

    /**
     * Ввод в поле случайной последовательности цифр задаваемой длины и сохранение этого значения в переменную
     */
    @И("^()в поле \"([^\"]*)\" введено случайное число из (\\d+) (?:цифр|цифры) и сохранено в переменную \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" в поле \"([^\"]*)\" введено случайное число из (\\d+) (?:цифр|цифры) и сохранено в переменную \"([^\"]*)\"$")
    public void inputAndSetRandomNumSequence(String blockName, String elementName, int seqLengthString, String varName) {
        String value = inputRandomNumSequence(resolveOwner(blockName).getElement(elementName), String.valueOf(seqLengthString));
        CoreScenario.getInstance().setVar(varName, value);
    }

    /**
     * Скроллит экран до нужного элемента, имеющегося на странице, но видимого только в нижней/верхней части страницы.
     */
    @И("^()страница прокручена до элемента \"([^\"]*)\"")
    @И("^в блоке \"([^\"]*)\" страница прокручена до элемента \"([^\"]*)\"")
    public void scrollPageToElement(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).scrollTo();
    }

    /**
     * Прокручивает страницу к указанному элементу.
     * Selenide ожидает появления элемента в пределах настроенного timeout.
     */
    @И("^()страница прокручена до появления элемента \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" страница прокручена до появления элемента \"([^\"]*)\"$")
    public void scrollWhileElemNotFoundOnPage(String blockName, String elementName) {
        resolveOwner(blockName).getElement(elementName).scrollTo();
    }

    /**
     * Скроллит страницу вниз до появления элемента с текстом из property файла, из переменной сценария или указанному в шаге каждую секунду.
     * Если достигнут футер страницы и элемент не найден - выбрасывается exception.
     */
    @SuppressWarnings("deprecation")
    @И("^()страница прокручена до появления элемента с текстом \"([^\"]*)\"$")
    @И("^в блоке \"([^\"]*)\" страница прокручена до появления элемента с текстом \"([^\"]*)\"$")
    public void scrollWhileElemWithTextNotFoundOnPage(String blockName, String expectedValue) {
        // Блочный вариант шага принимается для совместимости текста (blockName не используется),
        // но не влияет на поиск: элемент с нужным текстом всегда ищется по всему документу,
        // т.к. getSelf() блоков может быть не инициализирован корректно в текущей реализации.
        String resolved = getPropertyOrStringVariableOrValue(expectedValue);
        SelenideElement element = Selenide.$(By.xpath(getTranslateNormalizeSpaceText(resolved)));
        ((JavascriptExecutor) WebDriverRunner.getWebDriver()).executeScript("arguments[0].scrollIntoView();", element);
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
