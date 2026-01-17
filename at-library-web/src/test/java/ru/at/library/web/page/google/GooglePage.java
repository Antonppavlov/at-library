package ru.at.library.web.page.google;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.page.google.block.GoogleHeader;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

@Name("Google")
public class GooglePage extends CorePage {

    @Name("Поиск")
    @FindBy(css = "[title=\"Поиск\"]")
    public SelenideElement searchInput;

    @Name("Кнопка Почта")
    @FindBy(css = "header [aria-label*=\"Почта\"]")
    public SelenideElement menuBtn;

    @Name("Google Header")
    @FindBy(css = "header")
    public GoogleHeader googleHeader;
}
