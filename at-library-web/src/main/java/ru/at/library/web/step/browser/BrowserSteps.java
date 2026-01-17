/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.at.library.web.step.browser;

import com.assertthat.selenium_shutterbug.core.Capture;
import com.assertthat.selenium_shutterbug.core.Shutterbug;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import io.cucumber.java.ru.И;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.web.scenario.WebScenario;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.url;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.utils.helpers.PropertyLoader.getPropertyOrValue;
import static ru.at.library.core.utils.helpers.PropertyLoader.loadValueFromFileOrVariableOrDefault;
import static ru.at.library.core.utils.helpers.ScopedVariables.resolveVars;

/**
 * Шаги браузера
 */
@Log4j2
public class BrowserSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Выполняется переход по заданной ссылке,
     *
     * @param address Ссылка
     */
    @И("^совершен переход по ссылке \"([^\"]*)\"$")
    public void openUrl(String address) {
        String url = resolveVars(getPropertyOrStringVariableOrValue(address));
        open(url);
        log.trace("Url = " + url);
    }

    /**
     * Выполняется переход по заданной ссылке в новом окне,
     *
     * @param address Ссылка
     */
    @И("^совершен переход по ссылке \"([^\"]*)\" в новой вкладке$")
    public void openUrlNewTab(String address) {
        String url = resolveVars(getPropertyOrStringVariableOrValue(address));

        ((JavascriptExecutor) WebDriverRunner.getWebDriver())
                .executeScript("window.open('" + url + "','_blank');");

        int numberThisTab = WebDriverRunner.getWebDriver().getWindowHandles().size() - 1;
        Selenide.switchTo().window(numberThisTab);
    }

    /**
     * Проверка, что текущий URL совпадает с ожидаемым
     *
     * @param expectedURL ожидаемый URL
     */
    @И("^текущий URL равен \"([^\"]*)\"$")
    public void checkEqualsCurrentURL(String expectedURL) {
        expectedURL = formALinkExpectedURL(expectedURL);
        String currentUrl = "";
        int sleepTime = 100;
        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            currentUrl = url();
            if (currentUrl.toLowerCase().equals(expectedURL.toLowerCase())) {
                return;
            }
            sleep(sleepTime);
        }

        takeScreenshot();
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                "Текущий URL не совпадает с ожидаемым",
                currentUrl,
                is(equalToIgnoringCase(expectedURL))
        );
    }

    /**
     * Проверка, что текущий URL содержит с ожидаемым
     *
     * @param expectedURL ожидаемый URL
     */
    @И("^текущий URL содержит \"([^\"]*)\"$")
    public void checkContainsStringURL(String expectedURL) {
        expectedURL = formALinkExpectedURL(expectedURL);
        String currentUrl = "";
        int sleepTime = 100;
        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            currentUrl = url();
            if (currentUrl.toLowerCase().contains(expectedURL.toLowerCase())) {
                return;
            }
            sleep(sleepTime);
        }

        takeScreenshot();
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                "Текущий URL не содержит ожидаемую строку",
                currentUrl,
                containsString(expectedURL)
        );
    }

    /**
     * Проверка, что текущий URL не совпадает с ожидаемым
     *
     * @param expectedURL URL с которым происходит сравнение
     */
    @И("^текущий URL не равен \"([^\"]*)\"$")
    public void checkCurrentURLIsNotEquals(String expectedURL) {
        expectedURL = formALinkExpectedURL(expectedURL);
        String currentUrl = "";
        int sleepTime = 100;

        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            currentUrl = url();
            if (currentUrl.equals(expectedURL)) {
                sleep(sleepTime);
            }
        }
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                "Текущий URL совпадает с ожидаемым",
                currentUrl,
                not(equalToIgnoringCase(expectedURL))
        );
    }

    /**
     * Переключение на следующую вкладку браузера
     */
    @И("^выполнено переключение на следующую вкладку$")
    public void switchToTheNextTab() {
        String nextWindowHandle = nextWindowHandle();
        getWebDriver().switchTo().window(nextWindowHandle);
        log.trace("Текущая вкладка " + nextWindowHandle);
    }

    /**
     * Выполняется обновление страницы
     */
    @И("^выполнено обновление текущей страницы$")
    public void refreshPage() {
        refresh();
    }

    /**
     * Выполняется обновление страницы
     *
     * @param secondString        общее время обновление страницы
     * @param allTimeSecondString как часто будет происходить обноволение
     */
    @И("^выполнено обновление текущей страницы каждые \"([^\"]*)\" секунд в течении \"([^\"]*)\" секунд$")
    public void refreshPage(String secondString, String allTimeSecondString) {
        int second = Integer.parseInt(secondString);
        int allTimeSecond = Integer.parseInt(allTimeSecondString);
        for (int i = 0; i < allTimeSecond; i += second) {
            sleep(second * 1000);
            refresh();
        }
    }

    /**
     * Производится закрытие текущей вкладки и возвращает на первую
     */
    @И("^выполнено закрытие текущей вкладки$")
    public void closeCurrentTab() {
        getWebDriver().close();
        if (WebDriverRunner.getWebDriver().getWindowHandles().size() > 0) {
            Selenide.switchTo().window(0);
        }
    }

    /**
     * Производится нажатие кнопки вперед в браузере
     */
    @И("^нажатие кнопки вперед в браузере$")
    public void forward() {
        WebDriverRunner.getWebDriver().navigate().forward();
    }

    /**
     * Производится нажатие назад в браузере
     */
    @И("^нажатие кнопки назад в браузере$")
    public void back() {
        WebDriverRunner.getWebDriver().navigate().back();
    }

    /**
     * Переключение на вкладку браузера с заголовком
     *
     * @param title заголовок вкладки
     */
    @И("^выполнено переключение на вкладку с заголовком \"([^\"]*)\"$")
    public void switchToTheTabWithTitle(String title) {
        title = getPropertyOrStringVariableOrValue(title);
        try {
            switchTo().window(title);
        } catch (Exception exception) {
            exception.printStackTrace();
            checkPageTitleEquals(title);
        }
    }

    /**
     * Переключение на фрейм с именем
     *
     * @param frameName имя/id фрейма
     */
    @И("^выполнено переключение на фрейм с (?:именем|id) \"([^\"]*)\"$")
    public void switchToFrameWithName(String frameName) {
        frameName = getPropertyOrStringVariableOrValue(frameName);
        try {
            switchTo().frame(frameName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Переключение на основной фрейм страницы
     */
    @И("^выполнено переключение основной на фрейм страницы$")
    public void switchToDefaultFrame() {
        try {
            switchTo().defaultContent();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Производится сравнение заголовка страницы со значением, указанным в шаге
     *
     * @param expectedTitle ожидаемый заголовок текущей вкладки
     */
    @И("^заголовок страницы равен \"([^\"]*)\"$")
    public void checkPageTitleEquals(String expectedTitle) {
        expectedTitle = getPropertyOrStringVariableOrValue(expectedTitle);
        String actualTitle = "";
        int sleepTime = 100;
        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            actualTitle = title();
            if (actualTitle.toLowerCase().equals(expectedTitle.toLowerCase())) {
                return;
            }
            sleep(sleepTime);
        }
        takeScreenshot();
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("Заголовок страницы не совпадает с ожидаемым значением. Ожидаемый результат: %s, текущий результат: %s", expectedTitle, actualTitle),
                expectedTitle,
                equalToIgnoringCase(actualTitle)
        );
    }

    /**
     * Производится сохранение заголовка страницы в переменную
     *
     * @param variableName имя переменной
     */
    @И("^заголовок страницы сохранен в переменную \"([^\"]*)\"$")
    public void savePageTitleToVariable(String variableName) {
        String titleName = title();
        coreScenario.setVar(variableName, titleName);
        log.trace("Значение заголовка страницы [" + titleName + "] сохранено в переменную [" + variableName + "]");
    }

    /**
     * Устанавливает размеры окна браузера
     *
     * @param widthString  ширина
     * @param heightString высота
     */
    @И("^установлен размер окна браузера \"([^\"]*)\" х \"([^\"]*)\"$")
    public void setBrowserWindowSize(String widthString, String heightString) {
        int width = Integer.parseInt(getPropertyOrStringVariableOrValue(widthString));
        int height = Integer.parseInt(getPropertyOrStringVariableOrValue(heightString));
        setBrowserWindowSize(width, height);
    }

    /**
     * Устанавливает ширину окна браузера
     *
     * @param widthString ширина окна
     */
    @И("^установлена ширина окна браузера \"([^\"]*)\"$")
    public void setBrowserWindowWidth(String widthString) {
        int width = Integer.parseInt(getPropertyOrStringVariableOrValue(widthString));
        setBrowserWindowSize(width, null);
    }

    /**
     * Устанавливает высоту окна браузера
     *
     * @param heightString высота окна
     */
    @И("^установлена высота окна браузера \"([^\"]*)\"$")
    public void setBrowserWindowHeight(String heightString) {
        int height = Integer.parseInt(getPropertyOrStringVariableOrValue(heightString));
        setBrowserWindowSize(null, height);
    }

    /**
     * Устанавливает размеры окна браузера
     *
     * @param width  ширина окна (если null, то ширина не меняется)
     * @param height высота окна (если null, то высота не меняется)
     */
    public void setBrowserWindowSize(Integer width, Integer height) {
        WebDriver.Window browserWindow = getWebDriver().manage().window();
        if (width == null) {
            width = browserWindow.getSize().width;
        }
        if (height == null) {
            height = browserWindow.getSize().height;
        }
        browserWindow.setSize(new Dimension(width, height));
        log.trace("Установлены размеры окна браузера: ширина " + width + " высота" + height);
    }

    /**
     * Разворачивает окно с браузером на весь экран
     */
    @И("^окно развернуто на весь экран$")
    public void expandWindowToFullScreen() {
        getWebDriver().manage().window().maximize();
    }

    /**
     * Выполняется переход в начало страницы
     */
    @И("^совершен переход в начало страницы$")
    public void scrollUP() {
        Actions actions = new Actions(WebDriverRunner.getWebDriver());
        actions.keyDown(Keys.CONTROL).sendKeys(new CharSequence[]{Keys.HOME}).build().perform();
        actions.keyUp(Keys.CONTROL).perform();
    }

    /**
     * Выполняется переход в конец страницы
     */
    @И("^совершен переход в конец страницы$")
    public void scrollDown() {
        Actions actions = new Actions(getWebDriver());
        actions.keyDown(Keys.CONTROL).sendKeys(Keys.END).build().perform();
        actions.keyUp(Keys.CONTROL).perform();
    }

    /**
     * Метод осуществляет снятие скриншота и прикрепление его к cucumber отчету.
     */
    @И("^снят скриншот текущей страницы$")
    public synchronized static void takeScreenshot() {
        String attachmentName = "screenshot";
        try {
            attachmentName = WebScenario.getCurrentPage().getName();
        } catch (Exception ignored) {
            try {
                attachmentName = "screenshot_" + WebDriverRunner.url();
            } catch (Exception ignoredUrl) {
                // оставим attachmentName по умолчанию
            }
        }
        final String finalName = attachmentName;
        getScreenshotBytes().ifPresent(bytes ->
                CoreScenario.getInstance().getScenario().attach(bytes, "image/png", finalName)
        );
    }

    /**
     * Удалить все cookies
     */
    @И("^cookies приложения очищены$")
    public void deleteCookies() {
        int sleepTime = 100;
        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            sleep(sleepTime);
            if (getWebDriver().manage().getCookies().size() != 0) {
                clearBrowserCookies();
            } else {
                return;
            }
        }
    }

    /**
     * Поиск cookie по имени.
     * Сохранение cookie в переменную для дальнейшего использования
     *
     * @param cookieName   имя cookie
     * @param variableName имя переменной
     */
    @И("^содержимое cookie с именем \"([^\"]*)\" сохранена в переменную \"([^\"]*)\"$")
    public void saveCookieToVar(String cookieName, String variableName) {
        int sleepTime = 100;
        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            Cookie cookie = getWebDriver().manage().getCookieNamed(cookieName);
            if (cookie != null) {
                coreScenario.setVar(variableName, cookie.getValue());
                return;
            }
            sleep(sleepTime);
        }
        fail("Cookie c именем: " + cookieName + " не найдена");
    }

    /**
     * проверка cookie по имени.
     * Сохранение cookie в переменную для дальнейшего использования
     *
     * @param cookieName          имя cookie
     * @param expectedCookieValue предполагаемое содержимое cookie
     */
    @И("^содержимое cookie с именем \"([^\"]*)\" равно \"([^\"]*)\"$")
    public void checkValueCookie(String cookieName, String expectedCookieValue) {
        cookieName = getPropertyOrStringVariableOrValue(cookieName);
        expectedCookieValue = getPropertyOrStringVariableOrValue(expectedCookieValue);

        int sleepTime = 100;
        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            Cookie cookie = getWebDriver().manage().getCookieNamed(cookieName);
            if (cookie.getValue().equals(expectedCookieValue)) {
                return;
            }
            sleep(sleepTime);
        }

        takeScreenshot();
        fail("Содержимое cookie c именем: " + cookieName + " : " + getWebDriver().manage().getCookieNamed(cookieName).getValue());
    }

    /**
     * Сохраняем все cookies в переменную для дальнейшего использования
     *
     * @param variableName имя переменной
     */
    @И("^cookies сохранены в переменную \"([^\"]*)\"$")
    public void saveAllCookies(String variableName) {
        coreScenario.setVar(
                variableName,
                getWebDriver().manage().getCookies()
        );
    }

    /**
     * Находим cookie по имени и подменяем ее значение.
     * Имя cookie и домен не меняются
     *
     * @param cookieName  имя cookie
     * @param cookieValue значение cookie
     */
    @И("^добавлена cookie с именем \"([^\"]*)\" и значением \"([^\"]*)\"$")
    public void replaceCookie(String cookieName, String cookieValue) {
        cookieName = getPropertyOrStringVariableOrValue(cookieName);
        cookieValue = getPropertyOrStringVariableOrValue(cookieValue);
        getWebDriver().manage().addCookie(new Cookie(cookieName, cookieValue));
    }

    /**
     * Проверка что cookie нет на странице
     *
     * @param cookieName имя cookie
     */
    @И("^на странице нет cookie с именем \"([^\"]*)\"$")
    public void notCookie(String cookieName) {
        cookieName = getPropertyOrStringVariableOrValue(cookieName);
        int sleepTime = 100;
        Cookie cookie = null;
        for (int time = 0; time < Configuration.timeout; time += sleepTime) {
            cookie = getWebDriver().manage().getCookieNamed(cookieName);
            if (cookie != null) {
                sleep(sleepTime);
            }
        }
        assertNull(cookie, "Cookie: " + cookie + " найдена");
    }

    /**
     * Очистка сессионного хранилища
     */
    @И("^выполнена очистка сессионного хранилища")
    public void clearSessionStorage() {
        Selenide.clearBrowserLocalStorage();
        executeJavaScript("sessionStorage.clear();");
    }

    /**
     * Из сессионного хранилища удаляется элемент
     *
     * @param key поле с данным ключем будет удалено из sessionStorage
     */
    @И("^из сессионного хранилища удаляется элемент с ключом \"([^\"]*)\"$")
    public static void removeSessionStorageKey(String key) {
        key = getPropertyOrStringVariableOrValue(key);
        executeJavaScript(String.format("sessionStorage.removeItem('%s');", key));
    }

    /**
     * Метод переключается на следующую вкладку
     */
    private String nextWindowHandle() {
        String currentWindowHandle = getWebDriver().getWindowHandle();
        Set<String> windowHandles = getWebDriver().getWindowHandles();
        windowHandles.remove(currentWindowHandle);

        return windowHandles.iterator().next();
    }

    /**
     * Метод проверяет если ли в передавемом url текст http 
     * Если данного текста нет то метод выполняет конкатенатицию строк Configuration.baseUrl и передаваемого expectedURL
     *
     * @param expectedURL URL с которым происходит провпрка и конкатенатиция
     */
    private String formALinkExpectedURL(String expectedURL) {
        expectedURL = getPropertyOrValue(expectedURL);
        String propertyUrl = getPropertyOrValue(expectedURL);
        if (!propertyUrl.contains("http")) {
            propertyUrl = Configuration.baseUrl + propertyUrl;
        }
        String variableUrl = loadValueFromFileOrVariableOrDefault(expectedURL);

        if (variableUrl.contains("http")) {
            expectedURL = variableUrl;
        } else {
            expectedURL = propertyUrl;
        }

        return expectedURL;
    }

    /**
     * Получание скриншота побайтно
     */
    public synchronized static Optional<byte[]> getScreenshotBytes() {
        try {
            return WebDriverRunner.hasWebDriverStarted() ?
                    Optional.ofNullable(Shutterbug.shootPage(WebDriverRunner.getWebDriver(), Capture.FULL).getBytes()) :
                    Optional.empty();
        } catch (WebDriverException | IOException e) {
            log.warn("Could not get screen shot: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
