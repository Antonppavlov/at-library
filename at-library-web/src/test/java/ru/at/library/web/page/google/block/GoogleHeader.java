package ru.at.library.web.page.google.block;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

@Name("Google Header")
public class GoogleHeader extends CorePage {

    @Name("Кнопка Почта")
    @FindBy(css = "a[href*='mail.google.']")
    public ElementsCollection googleAppsList;

    @Name("Кнопка Картинки")
    @FindBy(css = "a[href*='imghp']")
    public SelenideElement otherAppsBtn;

}
