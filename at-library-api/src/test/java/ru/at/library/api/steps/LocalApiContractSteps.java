package ru.at.library.api.steps;

import com.sun.net.httpserver.HttpServer;
import io.cucumber.java.After;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Тогда;
import io.restassured.RestAssured;
import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import ru.at.library.api.steps.response.JsonResponseSteps;
import ru.at.library.core.cucumber.api.CoreScenario;

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
        proxyServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        proxyServer.createContext("/", exchange -> {
            byte[] response = "proxy-ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        proxyServer.start();

        CoreScenario scenario = CoreScenario.getInstance();
        scenario.setVar(hostVariable, "127.0.0.1");
        scenario.setVar(portVariable, String.valueOf(proxyServer.getAddress().getPort()));
    }

    @Тогда("^шаг проверки непустого массива в ответе \"([^\"]+)\" по jsonPath \"([^\"]+)\" отклоняет пустой массив$")
    public void emptyJsonArrayIsRejected(String responseVariable, String jsonPath) {
        try {
            new JsonResponseSteps().arraySizeNotNull(responseVariable, jsonPath);
        } catch (AssertionError expected) {
            return;
        }
        throw new AssertionError(String.format(
                "Шаг проверки непустого массива принял пустой массив по jsonPath '%s'",
                jsonPath
        ));
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

    private ContentType contentType(String format) {
        return switch (format) {
            case "JSON" -> ContentType.JSON;
            case "XML" -> ContentType.XML;
            case "YAML" -> ContentType.TEXT;
            default -> throw new IllegalArgumentException("Неизвестный формат ответа: " + format);
        };
    }
}
