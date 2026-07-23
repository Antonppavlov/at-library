package ru.at.library.web.page.dynamic;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Name;

import java.util.List;

@Name("Страница с динамическими блоками")
public class DynamicBlocksPage extends CorePage {

    @Name("Область динамических блоков")
    @FindBy(css = "#blocks")
    public SelenideElement area;

    @Name("Динамические блоки")
    @FindBy(css = "#blocks > .dynamic-block")
    public List<DynamicBlock> blocks;

    @Name("Контейнер динамических блоков")
    @FindBy(css = "#dynamic-blocks-container")
    public DynamicBlocksContainer container;
}
