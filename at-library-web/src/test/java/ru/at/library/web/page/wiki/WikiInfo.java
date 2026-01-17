package ru.at.library.web.page.wiki;

import com.codeborne.selenide.ElementsCollection;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.annotations.Name;

@Name("Сведения о странице")
public class WikiInfo extends WikipediaPage {

    @Name("Сведения - ключ")
    @FindBy(css = "[class=\"wikitable mw-page-info\"] tr td:nth-child(1)")
    public ElementsCollection wikiInfoKeyList;

    @Name("Сведения - значение")
    @FindBy(css = "[class=\"wikitable mw-page-info\"] tr td:nth-child(2)")
    public ElementsCollection wikiInfoValueList;

}
