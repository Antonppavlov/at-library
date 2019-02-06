/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p style="color: green; font-size: 1.5em">
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p style="color: green; font-size: 1.5em">
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.bcs.at.library.core.steps;

import cucumber.api.java.ru.Если;
import cucumber.api.java.ru.И;
import cucumber.api.java.ru.Когда;
import cucumber.api.java.ru.Тогда;
import lombok.extern.log4j.Log4j2;
import org.hamcrest.Matchers;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import ru.bcs.at.library.core.cucumber.api.CoreScenario;

import java.util.Set;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.codeborne.selenide.WebDriverRunner.url;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static ru.bcs.at.library.core.cucumber.ScopedVariables.resolveVars;
import static ru.bcs.at.library.core.steps.WebSteps.getPropertyOrStringVariableOrValue;

/**
 * <h1 style="color: green; font-size: 2.2em">Браузер шаги</h1>
 *
 * @author Anton Pavlov
 */
@Log4j2
public class BrowserSteps {

    private CoreScenario coreScenario = CoreScenario.getInstance();


    /**
     * <p style="color: green; font-size: 1.5em">Выполняется переход по заданной ссылке,
     *
     * @param address Ссылка берется из property / переменной по ключу, если такая переменная не найдена,
     *                то берется переданное значение
     *                при этом все ключи переменных в фигурных скобках
     *                меняются на их значения из хранилища coreScenario</p>
     */
    @Когда("^совершен переход по ссылке \"([^\"]*)\"$")
    public void goToUrl(String address) {
        String url = resolveVars(getPropertyOrStringVariableOrValue(address));
        open(url);
        coreScenario.write("Url = " + url);
    }

    /**
     * <p style="color: green; font-size: 1.5em">Проверка, что текущий URL совпадает с ожидаемым
     *
     * @param url (берется из property / переменной, если такая переменная не найдена,
     *            то берется переданное значение)</p>
     */
    @Тогда("^текущий URL равен \"([^\"]*)\"$")
    public void checkCurrentURL(String url) {
        String currentUrl = url();
        String expectedUrl = resolveVars(getPropertyOrStringVariableOrValue(url));
        assertThat("Текущий URL не совпадает с ожидаемым", currentUrl, is(expectedUrl));
    }

    /**
     * <p style="color: green; font-size: 1.5em">Проверка, что текущий URL не совпадает с ожидаемым
     *
     * @param url (берется из property / переменной, если такая переменная не найдена,
     *            то берется переданное значение)</p>
     */
    @Тогда("^текущий URL не равен \"([^\"]*)\"$")
    public void checkCurrentURLIsNotEquals(String url) {
        String currentUrl = url();
        String expectedUrl = resolveVars(getPropertyOrStringVariableOrValue(url));
        assertThat("Текущий URL совпадает с ожидаемым", currentUrl, Matchers.not(expectedUrl));
    }

    /**
     * <p style="color: green; font-size: 1.5em">Переключение на следующую вкладку браузера</p>
     */
    @Когда("выполнено переключение на следующую вкладку")
    public void switchToTheNextTab() {
        String nextWindowHandle = nextWindowHandle();
        getWebDriver().switchTo().window(nextWindowHandle);
        coreScenario.write("Текущая вкладка " + nextWindowHandle);
    }

    /**
     * <p style="color: green; font-size: 1.5em">Выполняется обновление страницы</p>
     */
    @И("^выполнено обновление текущей страницы$")
    public void refreshPage() {
        refresh();
    }

    /**
     * <p style="color: green; font-size: 1.5em">Производится закрытие текущей вкладки</p>
     */
    @И("выполнено закрытие текущей вкладки")
    public void closeCurrentTab() {
        getWebDriver().close();
    }

    /**
     * <p style="color: green; font-size: 1.5em">Переключение на вкладку браузера с заголовком</p>
     *
     * @param title заголовок вкладки
     */
    @Когда("^выполнено переключение на вкладку с заголовком \"([^\"]*)\"$")
    public void switchToTheTabWithTitle(String title) {
        switchTo().window(title);
        checkPageTitle(title);
    }

    /**
     * <p style="color: green; font-size: 1.5em">Производится сравнение заголовка страницы со значением, указанным в шаге
     * (в приоритете: из property, из переменной сценария, значение аргумента)</p>
     *
     * @param pageTitleName ожидаемый заголовок текущей вкладки
     */
    @Тогда("^заголовок страницы равен \"([^\"]*)\"$")
    public void checkPageTitle(String pageTitleName) {
        pageTitleName = getPropertyOrStringVariableOrValue(pageTitleName);
        String currentTitle = getWebDriver().getTitle().trim();
        assertThat(String.format("Заголовок страницы не совпадает с ожидаемым значением. Ожидаемый результат: %s, текущий результат: %s", pageTitleName, currentTitle),
                pageTitleName, equalToIgnoringCase(currentTitle));
    }

    /**
     * <p style="color: green; font-size: 1.5em">Производится сохранение заголовка страницы в переменную</p>
     *
     * @param variableName имя переменной
     */
    @И("^заголовок страницы сохранен в переменную \"([^\"]*)\"$")
    public void savePageTitleToVariable(String variableName) {
        String titleName = getWebDriver().getTitle().trim();
        coreScenario.setVar(variableName, titleName);
        coreScenario.write("Значение заголовка страницы [" + titleName + "] сохранено в переменную [" + variableName + "]");
    }

    /**
     * <p style="color: green; font-size: 1.5em">Устанавливает размеры окна браузера</p>
     *
     * @param width  ширина
     * @param height высота
     */
    @И("^установлено разрешение экрана (\\d+) х (\\d+)$")
    public void setBrowserWindowSize(int width, int height) {
        getWebDriver().manage().window().setSize(new Dimension(width, height));
        coreScenario.write("Установлены размеры окна браузера: ширина " + width + " высота" + height);
    }

    /**
     * <p style="color: green; font-size: 1.5em">Разворачивает окно с браузером на весь экран</p>
     */
    @Если("^окно развернуто на весь экран$")
    public void expandWindowToFullScreen() {
        getWebDriver().manage().window().maximize();
    }


    /**
     * <p style="color: green; font-size: 1.5em">Выполняется переход в конец страницы</p>
     */
    @И("^совершен переход в конец страницы$")
    public void scrollDown() {
        Actions actions = new Actions(getWebDriver());
        actions.keyDown(Keys.CONTROL).sendKeys(Keys.END).build().perform();
        actions.keyUp(Keys.CONTROL).perform();
    }


    /**
     * <p style="color: green; font-size: 1.5em">Метод осуществляет снятие скриншота и прикрепление его к cucumber отчету.</p>
     */
    @И("^снят скриншот текущей страницы$")
    public void takeScreenshot() {
        final byte[] screenshot = ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.BYTES);
        CoreScenario.getInstance().getScenario().embed(screenshot, "image/png");
    }

    /**
     * <p style="color: green; font-size: 1.5em">Удалить все cookies</p>
     */
    @Когда("^cookies приложения очищены$")
    public void deleteCookies() {
        clearBrowserCookies();
    }

    /**
     * <p style="color: green; font-size: 1.5em">Поиск cookie по имени.
     * Сохранение cookie в переменную для дальнейшего использования</p>
     *
     * @param nameCookie   имя cookie
     * @param variableName имя переменной
     */
    @Когда("^cookie с именем \"([^\"]*)\" сохранена в переменную \"([^\"]*)\"$")
    public void saveCookieToVar(String nameCookie, String variableName) {
        String cookieName = resolveVars(nameCookie);
        Cookie var = getWebDriver().manage().getCookieNamed(cookieName);
        coreScenario.setVar(variableName, var);
    }

    /**
     * <p style="color: green; font-size: 1.5em">Сохраняем все cookies в переменную для дальнейшего использования</p>
     *
     * @param variableName имя переменной
     */
    @Когда("^cookies сохранены в переменную \"([^\"]*)\"$")
    public void saveAllCookies(String variableName) {
        Set cookies = getWebDriver().manage().getCookies();
        coreScenario.setVar(variableName, cookies);
    }


    /**
     * <p style="color: green; font-size: 1.5em">Находим cookie по имени и подменяем ее значение.
     * Имя cookie и домен не меняются</p>
     *
     * @param cookieName  имя cookie
     * @param cookieValue значение cookie
     */
    @Когда("^добавлена cookie с именем \"([^\"]*)\" и значением \"([^\"]*)\"$")
    public void replaceCookie(String cookieName, String cookieValue) {
        String nameCookie = resolveVars(cookieName);
        String valueCookie = resolveVars(cookieValue);
        getWebDriver().manage().addCookie(new Cookie(nameCookie, valueCookie));
    }

    private String nextWindowHandle() {
        String currentWindowHandle = getWebDriver().getWindowHandle();
        Set<String> windowHandles = getWebDriver().getWindowHandles();
        windowHandles.remove(currentWindowHandle);

        return windowHandles.iterator().next();
    }
}