package ru.at.library.web.page.wiki;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Hidden;
import ru.at.library.web.scenario.annotations.Name;

@Name("Википедия")
public class WikipediaPage extends CorePage {

    @Name("Заголовок")
    @FindBy(css = "[class=\"main-top-left\"] h1")
    public SelenideElement pageHeader;

    @Name("Список ссылок")
    @FindBy(css = "[id=\"mw-panel\"] li")
    public ElementsCollection linkList;

    @Name("Сведения о странице")
    @FindBy(css = "#t-info")
    public SelenideElement wikiInfoButton;

    @Name("Инструменты")
    @FindBy(css = "[id=\"p-tb\"] li")
    public ElementsCollection listToolsLink;

    @Hidden
    @Name("Нет списка")
    @FindBy(css = "[id=\"not-spisok\"] li")
    public ElementsCollection notList;

    @Name("Поиск")
    @FindBy(css = "input#searchInput")
    public SelenideElement searchInput;

    @Name("Платформа сайта")
    @FindBy(css = "#footer-poweredbyico")
    public SelenideElement platformButton;

    @Name("Заявление о куки")
    @FindBy(css = "#footer-places-cookiestatement>a")
    public SelenideElement aboutCookiesBtn;
}
