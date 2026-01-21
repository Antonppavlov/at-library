package ru.at.library.web.page.wiki.block;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

/**
 * Тестовый блок для демонстрации работы шагов ListCorePage*.
 * Представляет одну строку навигационного меню Википедии.
 */
@Name("Навигационный блок")
public class WikiNavItem extends CorePage {

    @Name("Название ссылки")
    @FindBy(css = "a")
    public SelenideElement linkTitle;
}
