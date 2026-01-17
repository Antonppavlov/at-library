package ru.at.library.web.scenario;

import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс который используется для полчения списка объектов {@link WebElement} для выделения на скриншоте страницы
 */
public interface IStepResult {
    Optional<List<WebElement>> getWebElements();
}
