package ru.at.library.web.scenario;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class CastToWebElements {

    /**
     * Преобразование списка объектов {@link CorePage} в список объектов {@link WebElement} для выдедения списка блоков на скриншоте страницы
     *
     * @param blockList список блоков {@link CorePage} для выделения на скриншоте страницы
     * @return список объектов {@link WebElement} в обертке {@link Optional#of(Object)} для выделения на скришоте страницы
     * или {@link Optional#empty()} в случае если список блоков blockList отсутствуют или пропал со страниы
     */
    public static Optional<List<WebElement>> getBlockListAsWebElementsList(List<CorePage> blockList) {
        if (blockList == null || blockList.isEmpty()) {
            return Optional.empty();
        }
        List<WebElement> webElements = new ArrayList<>();
        for (CorePage block : blockList) {
            tryGetWebElement(block.getSelf()).ifPresent(webElements::add);
        }
        if (webElements.isEmpty()) {
            return Optional.empty();
        } else return Optional.of(webElements);
    }

    /**
     * Преобразование объекта {@link ElementsCollection} в список объектов {@link WebElement} для выделения на скриншоте страницы
     *
     * @param elementsCollection коллекция элементов {@link ElementsCollection} для выделения на скриншоте страницы
     * @return список объектов {@link WebElement} в обертке {@link Optional#of(Object)} для выделения на скришоте страницы
     * или {@link Optional#empty()} в случае если список elementsCollection отсутствуют или пропал со страниы
     */
    public static Optional<List<WebElement>> getElementsCollectionAsElementsList(ElementsCollection elementsCollection) {
        if (elementsCollection == null) return Optional.empty();
        List<WebElement> webElements = new ArrayList<>();
        elementsCollection.filter(Condition.visible).forEach(element ->
                tryGetWebElement(element).ifPresent(webElements::add));
        return webElements.isEmpty() ? Optional.empty() : Optional.of(webElements);
    }

    /**
     * Преобразование объекта {@link SelenideElement} в объект {@link WebElement} для выделения на скриншоте страницы
     *
     * @param selenideElement элемент {@link SelenideElement} для выделения на скриншоте страницы
     * @return объект {@link WebElement} в обертке {@link Optional#of(Object)} для выделения на скришоте страницы
     * или {@link Optional#empty()} в случае если элемент selenideElement отсутствуют или пропал со страниы
     */
    public static Optional<WebElement> tryGetWebElement(SelenideElement selenideElement) {
        WebElement webElement;
        try {
            webElement = selenideElement.toWebElement();
        } catch (Exception | ElementNotFound e) {
            return Optional.empty();
        }
        return Optional.of(webElement);
    }

}
