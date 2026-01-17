package ru.at.library.web.step.other;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.java.ru.И;
import org.openqa.selenium.Keys;
import ru.at.library.web.scenario.WebScenario;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.testng.Assert.*;
import static ru.at.library.core.steps.OtherSteps.*;
import static ru.at.library.core.utils.helpers.PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault;

/**
 * Общие шаги
 */
public class OtherSteps {


    /**
     * Эмулирует нажатие клавиш на клавиатуре
     */
    @И("^выполнено нажатие на клавиатуре \"([^\"]*)\"$")
    public void pushButtonOnKeyboard(String buttonName) {
        switchTo().activeElement()
                .sendKeys(getKeyOrCharacter(buttonName));
    }

    /**
     * <p style="color: green; font-size: 1.5em">
     *
     * @param keyNames название клавиши
     *                 Эмулирует нажатие сочетания клавиш на клавиатуре.
     *                 Допустим, чтобы эмулировать нажатие на Ctrl+A, в таблице должны быть следующие значения
     *                 | CONTROL |
     *                 | a       |
     */
    @И("^выполнено нажатие на сочетание клавиш из таблицы$")
    public void pressKeyCombination(List<String> keyNames) {
        Iterable<CharSequence> listKeys = keyNames.stream()
                .map(this::getKeyOrCharacter)
                .collect(Collectors.toList());
        String combination = Keys.chord(listKeys);
        switchTo().activeElement().sendKeys(combination);
    }


    /**
     * Выполняется запуск js-скрипта с указанием в js.executeScript его логики
     * Скрипт можно передать как аргумент метода или значение из properties
     */
    @И("^выполнен js-скрипт \"([^\"]*)\"")
    public void executeJsScript(String script) {
        script = getPropertyOrStringVariableOrValue(script);
        Selenide.executeJavaScript(script);
    }


    /**
     * Выполняется поиск нужного файла в папке /Downloads
     * Поиск осуществляется по содержанию ожидаемого текста в названии файла. Можно передавать регулярное выражение.
     * После выполнения проверки файл удаляется
     */
    @И("^файл по пути \"([^\"]*)\" выгрузился в поле \"([^\"]*)\"$")
    public void uploadFile(String path, String fieldName) {
        path = getPropertyOrStringVariableOrValue(path);

        WebScenario.getCurrentPage()
                .getElement(fieldName)
                .uploadFile(new File(path));
    }

    /**
     * Выполняется нажатие на кнопку и подгружается указанный файл
     * Селектор кнопки должны быть строго на input элемента
     * Можно указать путь до файла. Например, src/test/resources/example.pdf
     */
    @И("^выполнено нажатие на кнопку \"([^\"]*)\" и загружен файл \"([^\"]*)\"$")
    public void clickOnButtonAndUploadFile(String buttonName, String fileName) {
        String file = loadValueFromFileOrPropertyOrVariableOrDefault(fileName);
        File attachmentFile = new File(file);
        WebScenario.getCurrentPage().getElement(buttonName).uploadFile(attachmentFile);
    }

    /**
     * Выполняется поиск нужного файла в папке /Downloads
     * Поиск осуществляется по содержанию ожидаемого текста в названии файла. Можно передавать регулярное выражение.
     * После выполнения проверки файл удаляется
     */
    @И("^файл \"([^\"]*)\" загрузился в папку /Downloads$")
    public void testFileDownloaded(String fileName) {
        File downloads = getDownloadsDir();
        File[] expectedFiles = downloads.listFiles((files, file) -> file.contains(fileName));
        assertNotNull(expectedFiles, "Ошибка поиска файла");
        assertNotEquals(expectedFiles.length, 0, "Файл не загрузился");
        assertEquals(expectedFiles.length, 1, String.format("В папке присутствуют более одного файла с одинаковым названием, содержащим текст [%s]", fileName));
        deleteFiles(expectedFiles);
    }

    /**
     * Проверка, что на странице не отображаются редактируемые элементы, такие как:
     * -input
     * -textarea
     */
    @И("^открыта read-only форма$")
    public void openReadOnlyForm() {
        int inputsCount = getDisplayedElementsByCss("input").size();
        assertEquals(inputsCount, 0, "Форма не read-only. Количество input-полей: " + inputsCount);
        int textareasCount = getDisplayedElementsByCss("textarea").size();
        assertEquals(textareasCount, 0, "Форма не read-only. Количество элементов textarea: " + textareasCount);
    }

    private List<SelenideElement> getDisplayedElementsByCss(String cssSelector) {
        return $$(cssSelector).stream()
                .filter(SelenideElement::isDisplayed)
                .collect(Collectors.toList());
    }

    private CharSequence getKeyOrCharacter(String key) {
        try {
            return Keys.valueOf(key.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return key;
        }
    }

}
