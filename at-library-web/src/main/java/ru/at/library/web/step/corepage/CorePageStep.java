package ru.at.library.web.step.corepage;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.ru.И;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.JavascriptExecutor;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.WebScenario;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.url;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.utils.helpers.ScopedVariables.resolveVars;

/**
 * Шаги с  CorePage
 */
@Log4j2
public class CorePageStep {

    /**
     * Для работы с элементами страниц используется WebScenario (страницы),
     * а для переменных/ассертов по-прежнему CoreScenario.
     */

    /**
     * Проверка того, что все элементы, которые описаны в классе страницы с аннотацией @Name,
     * но без аннотации @Optional появились на странице
     *
     * @param nameOfPage название страница|блок|форма|вкладка
     */
    @И("^(?:страница|форма|вкладка) \"([^\"]*)\" загрузилась$")
    public void loadPage(String nameOfPage) {
        CorePage page = WebScenario.getPage(nameOfPage);
        WebScenario.setCurrentPage(page);
        WebScenario.getCurrentPage().isAppeared();
    }

    /**
     * Проверка того, что все элементы, которые описаны в классе страницы с аннотацией @Name,
     * но без аннотации @Optional появились в блоке
     *
     * @param nameOfPage название блока
     */
    @И("^блок \"([^\"]*)\" загрузился$")
    public void loadBlock(String nameOfPage) {
        CorePage page = WebScenario.getCurrentPage().getBlock(nameOfPage);
        WebScenario.setCurrentPage(page);
        WebScenario.getCurrentPage().isAppeared();
    }

    /**
     * Проверка того, что все элементы, которые описаны в классе страницы с аннотацией @Name,
     * но без аннотации @Optional, не появились на странице
     *
     * @param nameOfPage название страница|форма|вкладка
     */
    @И("^(?:страница|форма|вкладка) \"([^\"]*)\" не загрузилась$")
    public void loadPageDisappeared(String nameOfPage) {
        WebScenario.setCurrentPage(WebScenario.getPage(nameOfPage));
        WebScenario.getCurrentPage().isDisappeared();
    }

    /**
     * Выполняется объявление заданной страницы текущей без проверки загрузки элементов
     *
     * @param pageName  название страницы
     */
    @И("^совершен переход на страницу \"([^\"]*)\"$")
    public void setCurrentPage(String pageName) {
        CorePage page = WebScenario.getPage(pageName);
        WebScenario.setCurrentPage(page);
    }

    /**
     * Выполняется переход по заданной ссылке.
     * Шаг содержит проверку, что после перехода загружена заданная страница.
     *
     * @param nameOfPage название блока
     * @param urlOrName  url ссылки
     */
    @И("^совершен переход на страницу \"([^\"]*)\" по ссылке \"([^\"]*)\"$")
    public void goToSelectedPageByLink(String nameOfPage, String urlOrName) {
        String url = resolveVars(getPropertyOrStringVariableOrValue(urlOrName));
        log.trace(" url = " + url);
        open(url);
        loadPage(nameOfPage);
    }

    /**
     * Выполняется переход по заданной ссылке в новой вкладке.
     * Шаг содержит проверку, что после перехода загружена заданная страница.
     * Ссылка может передаваться как строка, так и как ключ из properties
     *
     * @param nameOfPage название блока
     * @param urlOrName  url ссылки
     */
    @И("^совершен переход на страницу \"([^\"]*)\" в новой вкладке по ссылке \"([^\"]*)\"$")
    public void goToSelectedPageByLinkNewTab(String nameOfPage, String urlOrName) {
        String url = resolveVars(getPropertyOrStringVariableOrValue(urlOrName));
        log.trace(" url = " + url);
        ((JavascriptExecutor) WebDriverRunner.getWebDriver())
                .executeScript("window.open('" + url + "','_blank');");
        int numberThisTab = WebDriverRunner.getWebDriver().getWindowHandles().size() - 1;
        Selenide.switchTo().window(numberThisTab);
        loadPage(nameOfPage);
    }

    /**
     * Переход на страницу по клику и проверка, что страница загружена
     *
     * @param nameOfPage  название блока
     * @param elementName имя элемента после нажатия на которых происходит переход
     */
    @И("^выполнен переход на страницу \"([^\"]*)\" после нажатия на (?:кнопку|ссылку|поле|чекбокс|радиокнопу|текст|элемент) \"([^\"]*)\"$")
    public void urlClickAndCheckRedirection(String nameOfPage, String elementName) {
        WebScenario.getCurrentPage().getElement(elementName).click();
        loadPage(nameOfPage);
        log.trace(" url = " + url());
    }

    /**
     * Проверка того, что все основные и обязательные элементы текущей страницы отображаются
     */
    @И("^все элементы текущей страницы отображаются$")
    public void pageAppeared() {
        WebScenario.setCurrentPage(
                WebScenario.getPage(WebScenario.getCurrentPage().getName())
        );
        WebScenario.getCurrentPage().checkPrimary(true);
    }

    /**
     * Проверка отображения всех основных элементов блока (всех кроме Optional и Hidden)
     *
     * @param blockName имя блока для проверки
     */
    @И("^блок \"([^\"]*)\" отображается на странице$")
    public void blockAppeared(String blockName) {
        CorePage block = WebScenario.getCurrentPage().getBlock(blockName);
        blockAppeared(block);
    }

    /**
     * Проверка того, что блок исчез/стал невидимым
     *
     * @param blockName имя блока для проверки
     */
    @И("^блок \"([^\"]*)\" не отображается на странице$")
    public void blockDisappeared(String blockName) {
        CorePage block = WebScenario.getCurrentPage().getBlock(blockName);
        block.isDisappeared();
    }

    /**
     * Блок это не совсем Selenide|Selenium элемент.
     * <p>
     * Шагом *не отображается* - проверить нельзя. Кидает ошибку что не смог найти селектор.
     */
    @SuppressWarnings("deprecation")
    @И("^блок \"([^\"]*)\" не присутствует в DOM$")
    public void blockDoesntExist(String blockName) {
        WebScenario.getCurrentPage().getBlock(blockName).getSelf().shouldHave(Condition.not(Condition.exist));
    }

    /**
     * Проверка отображения всех основных элементов дочернего блока (всех кроме Optional и Hidden) в родительском блоке
     *
     * @param parentBlockName имя родительского блока, в котором расположен дочерний блок
     * @param childBlockName  имя дочернего блока для проверки
     */
    @И("^в блоке \"([^\"]*)\" блок \"([^\"]*)\" отображается на странице$")
    public void blockAppeared(String parentBlockName, String childBlockName) {
        CorePage block = WebScenario.getCurrentPage().getBlock(parentBlockName).getBlock(childBlockName);
        blockAppeared(block);
    }

    /**
     * Проверка отображения всех основных элементов блока (всех кроме Optional и Hidden)
     *
     * @param block блок для проверки
     */
    public void blockAppeared(CorePage block) {
        block.checkPrimary(true);
    }
}
