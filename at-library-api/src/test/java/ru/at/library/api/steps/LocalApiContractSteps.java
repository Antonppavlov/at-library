package ru.at.library.api.steps;

import com.sun.net.httpserver.HttpServer;
import io.cucumber.java.After;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Тогда;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Локальные фикстуры для проверки API step-definition без внешних сервисов.
 */
public class LocalApiContractSteps {

    private HttpServer proxyServer;

    @Дано("^подготовлен локальный (JSON|XML|YAML)-ответ \"([^\"]+)\":$")
    public void prepareLocalResponse(String format, String responseVariable, String body) {
        CoreScenario scenario = CoreScenario.getInstance();
        scenario.setVar(
                responseVariable,
                new ResponseBuilder()
                        .setStatusCode(200)
                        .setContentType(contentType(format))
                        .setBody(body)
                        .build()
        );
        scenario.setVar(responseVariable + "_body", body);
    }

    @Дано("^запущен локальный HTTP proxy с адресом в \"([^\"]+)\" и портом в \"([^\"]+)\"$")
    public void startLocalProxy(String hostVariable, String portVariable) throws IOException {
        startProxy("proxy-ok", hostVariable, portVariable);
    }

    @Дано("^запущен локальный HTTP proxy с пустым ответом, адрес в \"([^\"]+)\" и порт в \"([^\"]+)\"$")
    public void startEmptyLocalProxy(String hostVariable, String portVariable) throws IOException {
        startProxy("", hostVariable, portVariable);
    }

    @Тогда("^системное свойство \"([^\"]+)\" равно \"([^\"]*)\"$")
    public void systemPropertyEquals(String name, String expected) {
        String resolved = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(expected);
        String actual = System.getProperty(name);
        if (!resolved.equals(actual)) {
            throw new AssertionError(String.format("Системное свойство %s: ожидалось '%s', получено '%s'", name, resolved, actual));
        }
    }

    @Тогда("^системное свойство \"([^\"]+)\" не задано$")
    public void systemPropertyAbsent(String name) {
        if (System.getProperty(name) != null) {
            throw new AssertionError(String.format("Системное свойство %s должно быть не задано, а равно '%s'", name, System.getProperty(name)));
        }
    }

    @Тогда("^RestAssured использует proxy с адресом \"([^\"]+)\" и портом \"([^\"]+)\"$")
    public void restAssuredUsesProxy(String hostVariable, String portVariable) {
        String host = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(hostVariable);
        int port = Integer.parseInt(PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(portVariable));
        if (RestAssured.proxy == null || !host.equals(RestAssured.proxy.getHost()) || port != RestAssured.proxy.getPort()) {
            throw new AssertionError("RestAssured.proxy не совпадает с ожидаемым " + host + ":" + port + ", фактически: " + RestAssured.proxy);
        }
    }

    @Тогда("^RestAssured не использует proxy$")
    public void restAssuredWithoutProxy() {
        if (RestAssured.proxy != null) {
            throw new AssertionError("RestAssured.proxy должен быть сброшен, а равен: " + RestAssured.proxy);
        }
    }

    @After("@local-api-proxy")
    public void stopLocalProxy() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        RestAssured.proxy = null;

        if (proxyServer != null) {
            proxyServer.stop(0);
            proxyServer = null;
        }
    }

    private void startProxy(String responseBody, String hostVariable, String portVariable) throws IOException {
        proxyServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proxyServer.createContext("/", exchange -> {
            byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
            if (response.length == 0) {
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(200, response.length);
                exchange.getResponseBody().write(response);
            }
            exchange.close();
        });
        proxyServer.start();

        CoreScenario scenario = CoreScenario.getInstance();
        scenario.setVar(hostVariable, "127.0.0.1");
        scenario.setVar(portVariable, String.valueOf(proxyServer.getAddress().getPort()));
    }

    private ContentType contentType(String format) {
        return switch (format) {
            case "JSON" -> ContentType.JSON;
            case "XML" -> ContentType.XML;
            case "YAML" -> ContentType.TEXT;
            default -> throw new IllegalArgumentException("Неизвестный формат ответа: " + format);
        };
    }
}
