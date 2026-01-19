package ru.at.library.api.steps.request;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.Cookie;
import io.restassured.http.Method;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSender;
import io.restassured.specification.RequestSpecification;
import lombok.extern.log4j.Log4j2;
import org.apache.http.params.CoreConnectionPNames;
import ru.at.library.api.helpers.Utils;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;
import ru.at.library.core.utils.helpers.ScopedVariables;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static org.hamcrest.Matchers.is;

/**
 * Шаги по формированию и отправке HTTP-запросов.
 * Новая линейка шагов ориентирована на краткие и понятные формулировки,
 * но сохраняет всю гибкость старой реализации (таблица параметров, polling, проверка ответа).
 */
@Log4j2
public class SendRequestSteps {

    private static final int DEFAULT_TIMEOUT = PropertyLoader.loadPropertyInt("http.timeout", 10);
    private static final String HTTP_METHOD_PATTERN = "((?:GET|PUT|POST|DELETE|HEAD|TRACE|OPTIONS|PATCH))";

    /**
     * Включить логирование HTTP-запросов/ответов RestAssured в консоль.
     * Можно отключить через системное свойство: -Dapi.http.log.disable=true
     */
    private static final boolean LOG_HTTP = !parseBoolean(getProperty("api.http.log.disable", "false"));

    /**
     * Количество попыток при простом повторении запросов (не polling).
     */
    public static int requestRetries = Integer.parseInt(getProperty("request.retries", "1"));

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    // =======================================================================
    // БАЗОВЫЕ ШАГИ ОТПРАВКИ HTTP-ЗАПРОСОВ
    // =======================================================================

    /**
     * Отправка HTTP-запроса без таблицы параметров. Ответ сохраняется в переменную.
     * Пример:
     *  И отправлен HTTP GET запрос на URL "https://example.com" и ответ сохранён в переменную "response"
     */
    @И("^отправлен HTTP " + HTTP_METHOD_PATTERN + " запрос на URL \"([^\"]+)\" и ответ сохранён в переменную \"([^\"]+)\"$")
    public void sendHttpRequestAndSave(String method,
                                       String address,
                                       String responseVar) {
        Response response = sendRequest(method, address, null);
        saveResponse(responseVar, response);
    }

    /**
     * Отправка HTTP-запроса с таблицей параметров (HEADER, PARAMETER, BODY, FILE и т.п.). Ответ сохраняется в переменную.
     * Пример:
     *  И отправлен HTTP GET запрос на URL "https://example.com" с параметрами запроса и ответ сохранён в переменную "response"
     *    | HEADER    | Accept       | application/json |
     *    | PARAMETER | status       | available        |
     */
    @И("^отправлен HTTP " + HTTP_METHOD_PATTERN + " запрос на URL \"([^\"]+)\" с параметрами запроса и ответ сохранён в переменную \"([^\"]+)\"$")
    public void sendHttpRequestWithParamsAndSave(String method,
                                                 String address,
                                                 String responseVar,
                                                 DataTable paramsTable) {
        Response response = sendRequest(method, address, paramsTable);
        saveResponse(responseVar, response);
    }

    /**
     * Отправка HTTP-запроса без таблицы параметров с ожиданием кода ответа и сохранением ответа.
     * Пример:
     *  И отправлен HTTP GET запрос на URL "https://example.com" и ожидается код ответа 200, а ответ сохранён в переменную "response"
     */
    @И("^отправлен HTTP " + HTTP_METHOD_PATTERN + " запрос на URL \"([^\"]+)\" и ожидается код ответа (\\d+), а ответ сохранён в переменную \"([^\"]+)\"$")
    public void sendHttpRequestExpectStatusAndSave(String method,
                                                   String address,
                                                   int expectedStatus,
                                                   String responseVar) {
        Response response = sendWithRetries(method, address, null, expectedStatus);
        saveResponse(responseVar, response);
    }

    /**
     * Отправка HTTP-запроса с таблицей параметров, ожиданием кода ответа и сохранением ответа.
     * Пример:
     *  И отправлен HTTP GET запрос на URL "https://example.com" с параметрами запроса и ожидается код ответа 200, а ответ сохранён в переменную "response"
     */
    @И("^отправлен HTTP " + HTTP_METHOD_PATTERN + " запрос на URL \"([^\"]+)\" с параметрами запроса и ожидается код ответа (\\d+), а ответ сохранён в переменную \"([^\"]+)\"$")
    public void sendHttpRequestWithParamsExpectStatusAndSave(String method,
                                                             String address,
                                                             int expectedStatus,
                                                             String responseVar,
                                                             DataTable paramsTable) {
        Response response = sendWithRetries(method, address, paramsTable, expectedStatus);
        saveResponse(responseVar, response);
    }

    // =======================================================================
    // POLLING (ПОВТОРНЫЕ ЗАПРОСЫ В ТЕЧЕНИЕ ВРЕМЕНИ)
    // =======================================================================

    /**
     * Периодическая отправка HTTP-запроса без таблицы параметров до получения нужного кода ответа или истечения таймаута.
     * Пример:
     *  И в течение 30 секунд каждые 5 секунд отправляется HTTP GET запрос на URL "https://example.com" и ожидается код ответа 200, а ответ сохранён в переменную "response"
     */
    @И("^в течение (\\d+) секунд каждые (\\d+) секунд отправляется HTTP " + HTTP_METHOD_PATTERN + " запрос на URL \"([^\"]+)\" и ожидается код ответа (\\d+), а ответ сохранён в переменную \"([^\"]+)\"$")
    public void pollHttpRequestAndSave(int timeoutSec,
                                       int periodSec,
                                       String method,
                                       String address,
                                       int expectedStatus,
                                       String responseVar) {
        Response response = pollWithParams(timeoutSec, periodSec, method, address, null, null, expectedStatus);
        saveResponse(responseVar, response);
    }

    /**
     * Периодическая отправка HTTP-запроса с таблицей параметров до получения нужного кода ответа или истечения таймаута.
     */
    @И("^в течение (\\d+) секунд каждые (\\d+) секунд отправляется HTTP " + HTTP_METHOD_PATTERN + " запрос на URL \"([^\"]+)\" с параметрами запроса и ожидается код ответа (\\d+), а ответ сохранён в переменную \"([^\"]+)\"$")
    public void pollHttpRequestWithParamsAndSave(int timeoutSec,
                                                 int periodSec,
                                                 String method,
                                                 String address,
                                                 int expectedStatus,
                                                 String responseVar,
                                                 DataTable paramsTable) {
        Response response = pollWithParams(timeoutSec, periodSec, method, address, paramsTable, null, expectedStatus);
        saveResponse(responseVar, response);
    }

    /**
     * Периодическая отправка HTTP-запроса с таблицей параметров до получения нужного кода ответа и совпадения параметров ответа по таблице.
     * Таблица делится строкой с "RESPONSE" в первом столбце на параметры запроса и параметры ответа.
     */
    @И("^в течение (\\d+) секунд каждые (\\d+) секунд отправляется HTTP " + HTTP_METHOD_PATTERN + " запрос на URL \"([^\"]+)\" с параметрами запроса и ожидается код ответа (\\d+) и параметры ответа по таблице, а ответ сохранён в переменную \"([^\"]+)\"$")
    public void pollHttpRequestWithParamsAndResponseCheck(int timeoutSec,
                                                          int periodSec,
                                                          String method,
                                                          String address,
                                                          int expectedStatus,
                                                          String responseVar,
                                                          DataTable dataTable) {
        DataTable requestParams = dataTable;
        DataTable responseParams = null;
        int responseDividerIndex = dataTable.column(0).indexOf("RESPONSE");
        if (responseDividerIndex != -1) {
            responseParams = dataTable.subTable(responseDividerIndex + 1, 0, dataTable.height(), dataTable.width());
            requestParams = dataTable.subTable(0, 0, responseDividerIndex, dataTable.width());
        }
        Response response = pollWithParams(timeoutSec, periodSec, method, address, requestParams, responseParams, expectedStatus);
        saveResponse(responseVar, response);
    }

    // =======================================================================
    // НИЗКОУРОВНЕВЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =======================================================================

    private Response sendWithRetries(String method,
                                     String address,
                                     DataTable params,
                                     int expectedStatus) {
        Response lastResponse = null;
        for (int attempt = 1; attempt <= requestRetries; attempt++) {
            lastResponse = sendRequest(method, address, params);
            int actualStatus = lastResponse.statusCode();
            log.debug("HTTP {} {} -> статус {} (попытка {}/{})", method, address, actualStatus, attempt, requestRetries);
            if (actualStatus == expectedStatus) {
                return lastResponse;
            }
        }
        if (lastResponse == null) {
            throw new AssertionError(String.format("Не удалось отправить HTTP %s запрос на %s: нет ни одного ответа", method, address));
        }
        throw new AssertionError(String.format(
                "Ожидался статус %d для HTTP %s %s после %d попыток, фактически %d",
                expectedStatus, method, address, requestRetries, lastResponse.statusCode()));
    }

    private Response pollWithParams(int timeoutSec,
                                    int periodSec,
                                    String method,
                                    String address,
                                    DataTable requestParams,
                                    DataTable expectedResponseParams,
                                    int expectedStatus) {
        long startTime = System.currentTimeMillis();
        long deadline = startTime + timeoutSec * 1000L;
        AssertionError lastError = null;
        Response lastResponse = null;

        while (System.currentTimeMillis() < deadline) {
            lastResponse = sendRequest(method, address, requestParams);
            int actualStatus = lastResponse.statusCode();
            log.debug("HTTP {} {} -> статус {} (polling)", method, address, actualStatus);

            if (actualStatus == expectedStatus) {
                try {
                    checkResponseByParams(lastResponse, expectedResponseParams);
                    return lastResponse;
                } catch (AssertionError e) {
                    lastError = e;
                    log.debug("Параметры ответа не совпали с ожиданиями, продолжаем polling: {}", e.getMessage());
                }
            }

            long next = System.currentTimeMillis() + periodSec * 1000L;
            if (next > deadline) {
                break;
            }
            try {
                Thread.sleep(periodSec * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Ожидание между попытками polling было прервано", e);
            }
        }

        if (lastResponse == null) {
            throw new AssertionError(String.format("Не удалось получить ответ для HTTP %s %s в течение %d секунд", method, address, timeoutSec));
        }
        if (lastResponse.statusCode() != expectedStatus) {
            throw new AssertionError(String.format(
                    "Ожидался статус %d для HTTP %s %s в течение %d секунд, фактически %d",
                    expectedStatus, method, address, timeoutSec, lastResponse.statusCode()));
        }
        if (lastError != null) {
            throw lastError;
        }
        // До сюда мы доходить не должны, но на всякий случай.
        return lastResponse;
    }

    /**
     * Отправка http-запроса.
     */
    private Response sendRequest(String method, String address, DataTable dataTable) {
        address = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(address);
        configureRestAssuredIfNeeded();

        RequestSender request = createRequest(dataTable);
        Response response = request.request(Method.valueOf(method), address);
        if (LOG_HTTP) {
            // Логируем ответ сразу после выполнения запроса
            response.then().log().all();
        }
        return response;
    }

    /**
     * Базовая настройка RestAssured (таймауты, JSON-конфигурация, SSL),
     * включается только если системное свойство relaxedHTTPSValidation=true.
     */
    private void configureRestAssuredIfNeeded() {
        if (!parseBoolean(getProperty("relaxedHTTPSValidation", "false"))) {
            return;
        }
        RestAssured.config =
                RestAssuredConfig.newConfig()
                        .sslConfig(new SSLConfig().allowAllHostnames())
                        .jsonConfig(JsonConfig.jsonConfig()
                                .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL))
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, DEFAULT_TIMEOUT * 1000)
                                .setParam(CoreConnectionPNames.SO_TIMEOUT, DEFAULT_TIMEOUT * 1000)
                        );
    }

    /**
     * Создание запроса из таблицы параметров.
     * Content-Type при необходимости должен быть указан в качестве HEADER.
     */
    private RequestSender createRequest(DataTable dataTable) {
        String body = null;
        RequestSpecification request = RestAssured.given();

        if (dataTable != null) {
            for (List<String> requestParam : dataTable.asLists()) {
                String type = requestParam.get(0);

                String name = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(requestParam.get(1));
                String value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(requestParam.get(2));
                value = PropertyLoader.loadValueFromFileOrVariableOrDefault(value);

                switch (type.toUpperCase()) {
                    case "BASIC_AUTHENTICATION": {
                        request.auth().basic(name, value);
                        break;
                    }
                    case "RELAXED_HTTPS": {
                        request.relaxedHTTPSValidation();
                        break;
                    }
                    case "ACCESS_TOKEN": {
                        request.header(name, "Bearer " + value.replace("\"", ""));
                        break;
                    }
                    case "PARAMETER": {
                        request.queryParam(name, value);
                        break;
                    }
                    case "MULTIPART": {
                        request.multiPart(name, value);
                        break;
                    }
                    case "FORM_PARAMETER": {
                        request.formParam(name, value);
                        break;
                    }
                    case "PATH_PARAMETER": {
                        request.pathParam(name, value);
                        break;
                    }
                    case "HEADER": {
                        request.header(name, value);
                        break;
                    }
                    case "COOKIES": {
                        Cookie myCookie = new Cookie.Builder(name, value).build();
                        request.cookie(myCookie);
                        break;
                    }
                    case "BODY": {
                        value = checkBody(value);
                        body = Utils.resolveJsonVars(value);
                        request.body(body);
                        break;
                    }
                    case "FILE": {
                        String filePath = PropertyLoader.loadProperty(value, ScopedVariables.resolveVars(value));
                        request.multiPart("file", new File(filePath), name);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(format("Некорректно задан тип %s для параметра запроса %s ", type, name));
                    }
                }
            }
            if (body != null) {
                log.trace("Тело запроса:\n" + body);
            }
        }

        if (LOG_HTTP) {
            // Логируем сформированный запрос при выполнении
            request = request.log().all();
        }

        return request;
    }

    /**
     * Проверка параметров ответа по таблице (HEADER/COOKIES/BODY).
     */
    private void checkResponseByParams(Response response, DataTable dataTable) {
        if (dataTable == null) {
            return;
        }
        StringBuilder errorMessage = new StringBuilder();
        for (List<String> responseParam : dataTable.asLists()) {
            String type = responseParam.get(0);

            String name = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(responseParam.get(1));
            String value = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(responseParam.get(2));
            value = PropertyLoader.loadValueFromFileOrVariableOrDefault(value);

            try {
                switch (type.toUpperCase()) {
                    case "HEADER": {
                        response.then().header(name, value);
                        break;
                    }
                    case "COOKIES": {
                        response.then().cookie(name, value);
                        break;
                    }
                    case "BODY": {
                        response.then().body(is(value));
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException(format("Некорректно задан тип %s для параметра ответа %s ", type, name));
                    }
                }
            } catch (AssertionError e) {
                errorMessage.append(e.getMessage()).append("\n");
            }
        }
        if (errorMessage.length() > 0) {
            throw new AssertionError(errorMessage.toString());
        }
    }

    /**
     * Загрузка тела запроса: либо значение напрямую, либо содержимое resource-файла.
     */
    private String checkBody(String value) {
        URL url = PropertyLoader.class.getClassLoader().getResource(value);
        if (url != null) {
            try {
                value = Resources.toString(url, Charsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(format("Ошибка чтения файла ресурса: %s", url.getPath()), e);
            }
        }
        return value;
    }

    /**
     * Сохранение Response в хранилище переменных.
     */
    private void saveResponse(String variableName, Response response) {
        coreScenario.setVar(variableName, response);
    }
}