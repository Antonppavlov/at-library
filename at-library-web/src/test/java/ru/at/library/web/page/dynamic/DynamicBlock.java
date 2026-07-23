package ru.at.library.web.page.dynamic;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

@Name("Динамический блок")
public class DynamicBlock extends CorePage {

    @Name("Название динамического блока")
    @FindBy(css = ".block-name")
    public SelenideElement name;

    @Name("Кнопка динамического блока")
    @FindBy(css = ".block-action")
    public SelenideElement action;

    @Name("Поле динамического блока")
    @FindBy(css = ".block-input")
    public SelenideElement input;

    @Name("Описание динамического блока")
    @FindBy(css = ".block-description")
    public SelenideElement description;

    @Name("Скрытая метка динамического блока")
    @FindBy(css = ".hidden-marker")
    public SelenideElement hiddenMarker;

    @Name("Изображение динамического блока")
    @FindBy(css = ".block-image")
    public SelenideElement image;

    @Name("Отсутствующая метка динамического блока")
    @FindBy(css = ".optional-marker")
    public SelenideElement optionalMarker;
}
