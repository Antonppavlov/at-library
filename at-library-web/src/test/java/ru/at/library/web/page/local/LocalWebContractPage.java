package ru.at.library.web.page.local;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;
import ru.at.library.web.scenario.annotations.Optional;

@Name("Страница")
public class LocalWebContractPage extends CorePage {

    @Name("Маркер страницы")
    @FindBy(css = "#page-marker")
    public SelenideElement pageMarker;

    @Optional
    @Name("Элементы")
    @FindBy(css = "#page-list .page-item")
    public ElementsCollection elements;

    @Optional
    @Name("Поле")
    @FindBy(css = "#page-field")
    public SelenideElement field;

    @Optional
    @Name("Элемент")
    @FindBy(css = "#page-element")
    public SelenideElement element;

    @Optional
    @Name("Изображение")
    @FindBy(css = "#page-image")
    public SelenideElement image;

    @Optional
    @Name("Радио")
    @FindBy(css = "#page-radio")
    public SelenideElement radio;

    @Optional
    @Name("Чекбокс")
    @FindBy(css = "#page-checkbox")
    public SelenideElement checkbox;

    @Optional
    @Name("Файл")
    @FindBy(css = "#page-file")
    public SelenideElement file;

    @Optional
    @Name("Скачать")
    @FindBy(css = "#page-upload-button")
    public SelenideElement uploadButton;

    @Optional
    @Name("Блок")
    @FindBy(css = "#contract-block")
    public LocalWebContractBlock block;
}
