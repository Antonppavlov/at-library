package ru.at.library.web.page.wiki.block;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

import java.util.List;

/**
 * Блок боковой панели Википедии, содержащий список навигационных блоков.
 */
@Name("Боковая панель")
public class WikiSidePanel extends CorePage {

    // См. WikipediaPage.navBlocks: та же причина избегать "li" без уточнения —
    // #p-lang внутри #mw-panel содержит ~340 скрытых li, на которых PRIMARY-проверка
    // видимости ждёт полный таймаут на каждом и вызывает многоминутное зависание.
    @Name("Список блоков навигации")
    @FindBy(css = "#p-navigation li, #p-participation li, #p-coll-print_export li, #p-wikibase-otherprojects li")
    public List<WikiNavItem> navBlocks;

    /**
     * Первая ссылка боковой панели ("Заглавная страница") — стабильный одиночный
     * элемент внутри блока для тестов действий/проверок над одним элементом блока.
     */
    @Name("Ссылка Заглавная страница")
    @FindBy(css = "#p-navigation li:nth-of-type(1) a")
    public SelenideElement homeLink;

    /**
     * Все ссылки боковой панели — стабильная коллекция элементов внутри блока
     * для тестов ElementsCollection-шагов, ограниченных блоком. Тот же набор
     * портлетов, что и navBlocks — см. комментарий выше.
     */
    @Name("Ссылки панели")
    @FindBy(css = "#p-navigation li a, #p-participation li a, #p-coll-print_export li a, #p-wikibase-otherprojects li a")
    public ElementsCollection panelLinks;
}
