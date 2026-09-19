package ru.at.library.web.page.wiki;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.page.wiki.block.WikiNavItem;
import ru.at.library.web.page.wiki.block.WikiSidePanel;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Hidden;
import ru.at.library.web.scenario.annotations.Name;

@Name("Википедия")
public class WikipediaPage extends CorePage {

    @Name("Заголовок")
    @FindBy(css = "[class=\"main-top-left\"] h1")
    public SelenideElement pageHeader;

    // Намеренно НЕ "#mw-panel li": внутри #mw-panel есть портлет #p-lang (список
    // интервики-языков) с ~350 li, из которых видимо только ~10 — остальные скрыты
    // в свёрнутом vector-menu-content и их набор/размер меняется асинхронно после
    // первой отрисовки (сворачивание происходит через JS уже после загрузки DOM).
    // Из-за этого случайный индекс, посчитанный по .size() ДО сворачивания, переставал
    // совпадать с фактическим списком на момент клика — "выполнено нажатие на случайный
    // элемент" падал с IndexOutOfBounds. Берём только портлеты, где все li гарантированно
    // видимы и стабильны.
    @Name("Список ссылок")
    @FindBy(css = "#p-navigation li, #p-participation li, #p-coll-print_export li, #p-wikibase-otherprojects li")
    public ElementsCollection linkList;

    // Тестовый список блоков для ListCorePage* шагов (каждый li оборачивается в WikiNavItem).
    // См. комментарий у "Список ссылок" выше: та же причина избегать "#mw-panel li" —
    // скрытые/асинхронно сворачиваемые li портлета #p-lang вызывали и многоминутное
    // зависание PRIMARY-проверки видимости (checkPrimary), и нестабильные индексы.
    @Name("Список блоков навигации")
    @FindBy(css = "#p-navigation li, #p-participation li, #p-coll-print_export li, #p-wikibase-otherprojects li")
    public java.util.List<WikiNavItem> navBlocks;

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

    // Боковая панель как отдельный блок, внутри которого также есть список блоков навигации
    @Name("Боковая панель")
    @FindBy(css = "#mw-panel")
    public WikiSidePanel sidePanel;
}
