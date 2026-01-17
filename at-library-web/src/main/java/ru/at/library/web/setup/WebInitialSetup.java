package ru.at.library.web.setup;

import com.codeborne.selenide.Browsers;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.google.common.base.Strings;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.at.library.core.setup.CoreInitialSetup;
import ru.at.library.core.utils.helpers.PropertyLoader;
import ru.at.library.web.scenario.WebScenario;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Configuration.browser;
import static ru.at.library.core.setup.CoreInitialSetup.getScenarioId;
import static ru.at.library.core.utils.helpers.PropertyLoader.tryLoadProperty;

/**
 * Web-специфичная начальная настройка: запуск и остановка WebDriver'а.
 * Выполняется только для сценариев с тегом @web.
 */
@Log4j2
public class WebInitialSetup {

    @Before(order = 600, value = "@web")
    @Step("Запуск браузера для web-сценария")
    public void startWebDriver(Scenario scenario) throws Exception {
        configureBaseUrl();
        WebScenario.initPages();
        startUITest(scenario, CoreInitialSetup.scenarioNumber);
    }

    /**
     * После web-сценария: закрытие WebDriver'а.
     */
    @After(value = "@web")
    @Step("Закрытие браузера для web-сценария")
    public void stopWebDriver(Scenario scenario) {
        Selenide.closeWebDriver();
        log.info("Драйвер успешно остановлен для сценария [{}]", scenario.getName());
    }

    /**
     * Настройка базового URL для Selenide на основе system property "baseURI" или значения из properties.
     */
    private void configureBaseUrl() {
        String baseUri = System.getProperty("baseURI", tryLoadProperty("baseURI"));
        Configuration.baseUrl = baseUri;
        log.debug("[WEB] Configuration.baseUrl='{}'", baseUri);
    }

    /**
     * Создание и запуск WebDriver для UI-тестов (локально или удалённо).
     */
    @Step("Запуск UI теста")
    private void startUITest(Scenario scenario, int testNumber) throws Exception {
        if (Strings.isNullOrEmpty(Configuration.remote)) {
            initLocalStart(scenario);
        } else {
            initRemoteStart(scenario, testNumber);
        }
    }

    @Step("Запуск теста локально")
    private void initLocalStart(Scenario scenario) {
        log.info(String.format("%s: ОС: %s", getScenarioId(scenario), System.getProperty("os.name")));
        log.info(String.format("%s: локальный бразуер: %s", getScenarioId(scenario), browser));

        if (browser.equals(Browsers.CHROME)) {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            ChromeOptions chromeOptions = new ChromeOptions();
            enrichWithChromeArgumentsFromProperties(capabilities, chromeOptions, "local");

            // Начиная с Selenide 7 browserCapabilities настраиваются через статическое поле Configuration.
            if (Configuration.browserCapabilities == null) {
                Configuration.browserCapabilities = capabilities;
            } else {
                Configuration.browserCapabilities.merge(capabilities);
            }
        }
    }

    @Step("Запуск теста удаленно")
    private void initRemoteStart(Scenario scenario, int testNumber) throws Exception {
        log.info(String.format("%s: удаленная машина: %s", getScenarioId(scenario), Configuration.remote));
        log.info(String.format("%s: браузер: %s", getScenarioId(scenario), Configuration.browser));

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setBrowserName(Configuration.browser);
        if (System.getProperty("version") != null && (!System.getProperty("version").isEmpty())) {
            capabilities.setVersion(System.getProperty("version"));
        }
        capabilities.setCapability("enableVNC",
                Boolean.parseBoolean(System.getProperty("enableVNC", "false"))
        );
        capabilities.setCapability("enableVideo",
                Boolean.parseBoolean(System.getProperty("enableVideo", "false"))
        );
        capabilities.setCapability("name", "[" + testNumber + "]" + scenario.getName());
        capabilities.setCapability("screenResolution", "1900x1080x24");
        capabilities.setCapability("browserstack.timezone", "Moscow");
        capabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);

        if (browser.equals(Browsers.CHROME)) {
            ChromeOptions chromeOptions = new ChromeOptions();
            if (System.getProperty("disableChromeFileViewer", "true").equals("true")) {
                chromeOptions.setExperimentalOption("prefs", new HashMap<String, Object>() {
                    {
                        put("profile.default_content_settings.popups", 0);
                        put("download.prompt_for_download", false);
                        put("download.directory_upgrade", true);
                        put("safebrowsing.enabled", false);
                        put("plugins.always_open_pdf_externally", true);
                        put("plugins.plugins_disabled", new ArrayList<String>() {
                            {
                                add("Chrome PDF Viewer");
                            }
                        });
                    }
                });
            }
            enrichWithChromeArgumentsFromProperties(capabilities, chromeOptions, "remote");
        }
        WebDriverRunner.setWebDriver(new RemoteWebDriver(
                URI.create(Configuration.remote).toURL(),
                capabilities
        ));
    }

    /**
     * Добавляет аргументы запуска для chrome указанные в property-файле в формате
     * chrome.arguments.(local|remote|all).%NAME%=%VALUE%
     */
    private DesiredCapabilities enrichWithChromeArgumentsFromProperties(DesiredCapabilities capabilities,
                                                                        ChromeOptions chromeOptions,
                                                                        String scope) {
        HashMap<String, String> arguments = PropertyLoader.loadPropertiesMatchesByRegex(
                "^chrome\\.arguments\\.(" + scope + "|all)");
        if (!arguments.isEmpty()) {
            for (Map.Entry<String, String> argument : arguments.entrySet()) {
                chromeOptions.addArguments(argument.getValue());
            }
        }
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        return capabilities;
    }
}