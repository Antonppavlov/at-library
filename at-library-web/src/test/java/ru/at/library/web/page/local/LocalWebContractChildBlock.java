package ru.at.library.web.page.local;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

@Name("Локальный дочерний блок")
public class LocalWebContractChildBlock extends CorePage {

    @Name("Маркер дочернего блока")
    @FindBy(css = ".child-marker")
    public SelenideElement marker;
}
