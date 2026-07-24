package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.logevents.LogEventListener;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Формирует компактные данные для Allure-шагов при работе со списками блоков
 * и временно скрывает технические события Selenide внутри polling-циклов.
 * <p>
 * Реестр listeners в Selenide является {@link ThreadLocal}, поэтому снятие и
 * восстановление listener-а не влияет на параллельно выполняющиеся тесты.
 * Нестандартные имена регистрации Allure-listener можно перечислить через
 * {@value #LISTENER_NAMES_PROPERTY}, разделяя их запятыми.
 */
final class BlockAllureReport {

    static final String LISTENER_NAMES_PROPERTY =
            "at.library.blockcollection.selenide-listener-names";

    private static final List<String> DEFAULT_ALLURE_LISTENER_NAMES = List.of(
            "AllureSelenide",
            "allure",
            "allure-selenide",
            "allureSelenide",
            "AllureSelenideSberbank"
    );
    private static final int MAX_STATE_LENGTH = 180;
    private static final String ELEMENT_STATE_SCRIPT = """
            const element = arguments[0];
            const style = window.getComputedStyle(element);
            const rect = element.getBoundingClientRect();
            return {
              visible: style.display !== 'none'
                && style.visibility !== 'hidden'
                && style.opacity !== '0'
                && rect.width > 0
                && rect.height > 0,
              text: element.innerText ?? element.textContent ?? '',
              value: 'value' in element ? element.value : '',
              title: element.getAttribute('title') ?? ''
            };
            """;

    private BlockAllureReport() {
    }

    static <T> T withoutSelenideSteps(Supplier<T> operation) {
        List<RemovedListener> removedListeners = removeAllureListeners();
        try {
            return operation.get();
        } finally {
            for (RemovedListener removedListener : removedListeners) {
                SelenideLogger.addListener(
                        removedListener.name(),
                        removedListener.listener()
                );
            }
        }
    }

    static void withoutSelenideSteps(Runnable operation) {
        withoutSelenideSteps(() -> {
            operation.run();
            return null;
        });
    }

    static String elementState(SelenideElement element) {
        return inspect(element).description();
    }

    static ElementState inspect(SelenideElement element) {
        try {
            return inspect(element.toWebElement());
        } catch (StaleElementReferenceException error) {
            return ElementState.updatedDom();
        } catch (ElementNotFound | NoSuchElementException | IndexOutOfBoundsException error) {
            return ElementState.missing();
        } catch (RuntimeException error) {
            return ElementState.unavailable(error);
        }
    }

    static ElementState inspect(WebElement element) {
        try {
            Object rawState = WebDriverRunner.driver()
                    .executeJavaScript(ELEMENT_STATE_SCRIPT, element);
            if (!(rawState instanceof Map<?, ?> state)) {
                return ElementState.unavailable(null);
            }

            List<String> description = new ArrayList<>();
            description.add(Boolean.TRUE.equals(state.get("visible"))
                    ? "видим"
                    : "скрыт");
            addValue(description, "text", stateValue(state, "text"));
            addValue(description, "value", stateValue(state, "value"));
            addValue(description, "title", stateValue(state, "title"));
            return ElementState.available(
                    compact(String.join(", ", description))
            );
        } catch (StaleElementReferenceException | NoSuchElementException | ElementNotFound error) {
            return ElementState.updatedDom();
        } catch (RuntimeException error) {
            return ElementState.unavailable(error);
        }
    }

    static void finishStep(Allure.StepContext step,
                           String title,
                           String status,
                           String expected,
                           String actual) {
        step.name(title + " — " + status);
        addParameter(step, "Ожидается", expected);
        addParameter(step, "Фактически", actual);
    }

    static void addError(Allure.StepContext step, Throwable error) {
        String errorMessage = error.getMessage();
        addParameter(
                step,
                "Ошибка",
                error.getClass().getSimpleName() +
                        (errorMessage == null || errorMessage.isBlank()
                                ? ""
                                : ": " + errorMessage)
        );
    }

    /**
     * Добавляет один обычный viewport-снимок только при конечной ошибке.
     * В отличие от full-page screenshot этот вызов не использует общий
     * synchronized lock и не сериализует параллельные тесты.
     */
    static void attachFailureScreenshot() {
        try {
            if (!WebDriverRunner.hasWebDriverStarted()
                    || !(WebDriverRunner.getWebDriver() instanceof TakesScreenshot screenshotDriver)) {
                return;
            }

            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    "Состояние страницы при ошибке блока",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );
        } catch (RuntimeException ignored) {
            // Диагностика не должна маскировать исходную ошибку.
        }
    }

    static String compact(String value) {
        if (value == null) {
            return "<нет значения>";
        }

        String normalized = value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();

        return normalized.length() <= MAX_STATE_LENGTH
                ? normalized
                : normalized.substring(0, MAX_STATE_LENGTH - 1) + "…";
    }

    private static void addParameter(Allure.StepContext step,
                                     String name,
                                     String value) {
        if (value != null && !value.isBlank()) {
            step.parameter(name, compact(value));
        }
    }

    private static void addValue(List<String> state, String name, String value) {
        if (value != null && !value.isBlank()) {
            state.add(name + "='" + compact(value) + "'");
        }
    }

    private static String stateValue(Map<?, ?> state, String name) {
        Object value = state.get(name);
        return value == null ? null : String.valueOf(value);
    }

    private static List<RemovedListener> removeAllureListeners() {
        List<RemovedListener> removedListeners = new ArrayList<>();
        for (String listenerName : configuredListenerNames()) {
            if (!SelenideLogger.hasListener(listenerName)) {
                continue;
            }

            LogEventListener listener = SelenideLogger.removeListener(listenerName);
            if (listener != null) {
                removedListeners.add(new RemovedListener(listenerName, listener));
            }
        }
        return removedListeners;
    }

    private static Set<String> configuredListenerNames() {
        Set<String> listenerNames = new LinkedHashSet<>(DEFAULT_ALLURE_LISTENER_NAMES);
        String configuredNames = System.getProperty(LISTENER_NAMES_PROPERTY, "");
        for (String configuredName : configuredNames.split(",")) {
            String listenerName = configuredName.trim();
            if (!listenerName.isEmpty()) {
                listenerNames.add(listenerName);
            }
        }
        return listenerNames;
    }

    private record RemovedListener(String name, LogEventListener listener) {
    }

    record ElementState(boolean domUpdated, String description) {

        private static ElementState available(String description) {
            return new ElementState(false, description);
        }

        private static ElementState missing() {
            return new ElementState(false, "элемент не существует");
        }

        private static ElementState updatedDom() {
            return new ElementState(true, "DOM обновился");
        }

        private static ElementState unavailable(RuntimeException error) {
            String reason = error == null
                    ? "неизвестный формат ответа"
                    : error.getClass().getSimpleName();
            return new ElementState(
                    false,
                    "состояние недоступно: " + reason
            );
        }
    }
}
