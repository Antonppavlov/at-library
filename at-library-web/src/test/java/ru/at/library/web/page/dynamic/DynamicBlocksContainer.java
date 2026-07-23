package ru.at.library.web.page.dynamic;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

import java.util.List;

@Name("Контейнер динамических блоков")
public class DynamicBlocksContainer extends CorePage {

    @Name("Область вложенных динамических блоков")
    @FindBy(css = "#nested-blocks")
    public SelenideElement area;

    @Name("Вложенные динамические блоки")
    @FindBy(css = "#nested-blocks > .nested-dynamic-block")
    public List<DynamicBlock> blocks;
}
