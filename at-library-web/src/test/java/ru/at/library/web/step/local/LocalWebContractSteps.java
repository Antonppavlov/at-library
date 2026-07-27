package ru.at.library.web.step.local;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.sun.net.httpserver.HttpServer;
import io.cucumber.java.After;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Тогда;
import ru.at.library.web.scenario.WebScenario;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Детерминированная локальная HTML-фикстура для Cucumber-контрактов web-шагов.
 */
public class LocalWebContractSteps {

    private HttpServer server;
    private long previousTimeout;
    private String previousUserHome;
    private Path temporaryHome;

    @Дано("^открыта локальная страница для проверки web-шагов$")
    public void openLocalContractPage() throws IOException {
        previousTimeout = Configuration.timeout;
        Configuration.timeout = 1_000;

        byte[] body = html().getBytes(StandardCharsets.UTF_8);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        Selenide.open("http://127.0.0.1:" + server.getAddress().getPort() + "/");
        WebScenario.setCurrentPage(WebScenario.getPage("Страница"));
    }

    @И("^локальная страница очищена$")
    public void clearLocalPage() {
        executeJavaScript("document.body.innerHTML = ''");
    }

    @И("^локальный блок скрыт$")
    public void hideLocalBlock() {
        executeJavaScript("document.getElementById('contract-block').style.display = 'none'");
    }

    @И("^локальный блок удален из DOM$")
    public void removeLocalBlock() {
        executeJavaScript("document.getElementById('contract-block').remove()");
    }

    @И("^локальные списки элементов скрыты$")
    public void hideLocalElementLists() {
        executeJavaScript("""
                document.getElementById('page-list').style.display = 'none';
                document.querySelector('.block-list').style.display = 'none';
                """);
    }

    @И("^локальные элементы с текстом скрыты$")
    public void hideLocalTextElements() {
        executeJavaScript("""
                document.getElementById('page-element').style.display = 'none';
                document.querySelector('.block-element').style.display = 'none';
                """);
    }

    @И("^элемент локального блока получил фокус$")
    public void focusBlockElement() {
        $(".block-element").click();
        $(".block-element").shouldHave(Condition.focused);
    }

    @И("^элемент локального блока перемещен за пределы экрана$")
    public void moveBlockElementOutsideViewport() {
        executeJavaScript("""
                const element = document.querySelector('.block-element');
                element.style.position = 'absolute';
                element.style.top = '5000px';
                """);
    }

    @И("^кнопка локального блока отключена$")
    public void disableBlockButton() {
        executeJavaScript("document.querySelector('.block-button').disabled = true");
    }

    @И("^поле локального блока заполнено$")
    public void fillBlockField() {
        $(".block-field").setValue("Заполнено");
    }

    @И("^локальные радио и чекбоксы сняты$")
    public void unselectLocalControls() {
        executeJavaScript("""
                document.getElementById('page-radio').checked = false;
                document.getElementById('page-checkbox').checked = false;
                document.querySelector('.block-radio').checked = false;
                document.querySelector('.block-checkbox').checked = false;
                """);
    }

    @И("^локальная форма переведена в read-only вид$")
    public void makeFormReadOnly() {
        executeJavaScript("""
                document.querySelectorAll('input, textarea')
                  .forEach(element => element.style.display = 'none');
                """);
    }

    @И("^подготовлен локальный файл загрузки$")
    public void prepareDownloadedFile() throws IOException {
        previousUserHome = System.getProperty("user.home");
        temporaryHome = Path.of("target", "local-web-contract-home").toAbsolutePath();
        Path downloads = temporaryHome.resolve("Downloads");
        Files.createDirectories(downloads);
        Files.writeString(
                downloads.resolve("at-library-contract-download.txt"),
                "local download contract",
                StandardCharsets.UTF_8
        );
        System.setProperty("user.home", temporaryHome.toString());
    }

    @Тогда("^в локальном списке (страницы|блока) зарегистрировано (\\d+) нажатия$")
    public void localListClickCount(String listOwner, int expectedCount) {
        Number actualCount = "страницы".equals(listOwner)
                ? executeJavaScript("return window.pageClicks || 0")
                : executeJavaScript("return window.blockClicks || 0");
        if (actualCount == null || actualCount.intValue() != expectedCount) {
            throw new AssertionError(String.format(
                    "Для списка %s ожидалось %d событий click, зарегистрировано %s",
                    listOwner,
                    expectedCount,
                    actualCount
            ));
        }
    }

    @After("@local-web-contract")
    public void cleanup() throws IOException {
        Configuration.timeout = previousTimeout == 0 ? Configuration.timeout : previousTimeout;

        if (previousUserHome != null) {
            System.setProperty("user.home", previousUserHome);
            previousUserHome = null;
        }
        if (temporaryHome != null) {
            Files.deleteIfExists(
                    temporaryHome.resolve("Downloads").resolve("at-library-contract-download.txt")
            );
            Files.deleteIfExists(temporaryHome.resolve("Downloads"));
            Files.deleteIfExists(temporaryHome);
            temporaryHome = null;
        }
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (WebDriverRunner.hasWebDriverStarted()) {
            while (WebDriverRunner.getWebDriver().getWindowHandles().size() > 1) {
                WebDriverRunner.getWebDriver().close();
                Selenide.switchTo().window(0);
            }
        }
    }

    private String html() {
        return """
                <!doctype html>
                <html lang="ru">
                <head><meta charset="UTF-8"><title>Локальный web-контракт</title></head>
                <body style="margin:0;min-height:6000px">
                  <div id="page-marker">Страница готова</div>
                  <select id="page-list" multiple size="2">
                    <option class="page-item" selected onclick="window.pageClicks=(window.pageClicks||0)+1">Первый</option>
                    <option class="page-item" onclick="window.pageClicks=(window.pageClicks||0)+1">Второй</option>
                  </select>
                  <input id="page-field" value="">
                  <button id="page-element" data-testid="value" style="display:block" disabled>Текст</button>
                  <img id="page-image" alt="pixel"
                       src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==">
                  <input id="page-radio" type="radio" checked>
                  <input id="page-checkbox" type="checkbox" checked>
                  <input id="page-file" type="file">
                  <input id="page-upload-button" type="file">

                  <section id="contract-block">
                    <div class="block-marker">Блок готов</div>
                    <select class="block-list" multiple size="2">
                      <option class="block-item" selected
                              onclick="window.blockClicks=(window.blockClicks||0)+1">Первый</option>
                      <option class="block-item"
                              onclick="window.blockClicks=(window.blockClicks||0)+1">Второй</option>
                    </select>
                    <input class="block-field" value="">
                    <button class="block-element" data-testid="value" style="display:block">Текст</button>
                    <button class="block-button">Кнопка</button>
                    <img class="block-image" alt="pixel"
                         src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==">
                    <input class="block-radio" type="radio" checked>
                    <input class="block-checkbox" type="checkbox" checked>
                    <a class="block-link" target="_blank" href="/new-tab">Ссылка</a>
                    <div class="child-block"><span class="child-marker">Дочерний блок готов</span></div>
                  </section>
                  <iframe name="frame" id="frame" srcdoc="<p>frame</p>"></iframe>
                </body>
                </html>
                """;
    }
}
