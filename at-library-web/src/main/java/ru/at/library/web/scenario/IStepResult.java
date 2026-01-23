package ru.at.library.web.scenario;

import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

/**
 * Результат выполнения шага, который может предоставить список элементов для выделения на скриншоте.
 * <p>Step‑классы возвращают реализации этого интерфейса, а инфраструктура отчётности берёт из них
 * {@link WebElement}-ы и подсвечивает их на кадрах страницы.</p>
 */
public interface IStepResult {
    Optional<List<WebElement>> getWebElements();
}
