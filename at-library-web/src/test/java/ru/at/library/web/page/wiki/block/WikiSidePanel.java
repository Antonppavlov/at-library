package ru.at.library.web.page.wiki.block;

import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

import java.util.List;

/**
 * Блок боковой панели Википедии, содержащий список навигационных блоков.
 */
@Name("Боковая панель")
public class WikiSidePanel extends CorePage {

    @Name("Список блоков навигации")
    @FindBy(css = "li")
    public List<WikiNavItem> navBlocks;
}
