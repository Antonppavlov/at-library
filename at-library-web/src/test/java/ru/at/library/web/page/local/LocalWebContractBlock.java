package ru.at.library.web.page.local;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;
import ru.at.library.web.scenario.annotations.Optional;

@Name("Локальный тестовый блок")
public class LocalWebContractBlock extends CorePage {

    @Name("Маркер блока")
    @FindBy(css = ".block-marker")
    public SelenideElement marker;

    @Optional
    @Name("Элементы")
    @FindBy(css = ".block-list .block-item")
    public ElementsCollection elements;

    @Optional
    @Name("Поле")
    @FindBy(css = ".block-field")
    public SelenideElement field;

    @Optional
    @Name("Элемент")
    @FindBy(css = ".block-element")
    public SelenideElement element;

    @Optional
    @Name("Кнопка")
    @FindBy(css = ".block-button")
    public SelenideElement button;

    @Optional
    @Name("Изображение")
    @FindBy(css = ".block-image")
    public SelenideElement image;

    @Optional
    @Name("Радио")
    @FindBy(css = ".block-radio")
    public SelenideElement radio;

    @Optional
    @Name("Чекбокс")
    @FindBy(css = ".block-checkbox")
    public SelenideElement checkbox;

    @Optional
    @Name("Ссылка")
    @FindBy(css = ".block-link")
    public SelenideElement link;

    @Optional
    @Name("Дочерний")
    @FindBy(css = ".child-block")
    public LocalWebContractChildBlock child;
}
