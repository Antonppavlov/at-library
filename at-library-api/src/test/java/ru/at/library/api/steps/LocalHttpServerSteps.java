package ru.at.library.api.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import io.cucumber.java.After;
import io.cucumber.java.ru.Дано;
import ru.at.library.api.steps.request.SendRequestSteps;
import ru.at.library.core.cucumber.api.CoreScenario;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Локальный детерминированный HTTP/HTTPS-сервер для оффлайн-тестов шагов отправки запросов.
 * Заменяет публичный Petstore там, где нужен предсказуемый ответ, и позволяет увидеть,
 * что именно получил сервер (метод, query, заголовки, cookies, form, multipart, body).
 * <p>
 * Адреса эндпоинтов публикуются в переменные сценария (значения можно передавать в шаги как адрес).
 */
public class LocalHttpServerSteps {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String BASIC_USER = "user";
    private static final String BASIC_PASSWORD = "pass";
    private static final String BEARER_TOKEN = "token-123";

    private static Path keyStoreFile;

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private HttpServer http;
    private HttpsServer https;
    private int originalRetries = -1;

    // =======================================================================
    // ЖИЗНЕННЫЙ ЦИКЛ
    // =======================================================================

    @Дано("^запущен локальный HTTP сервер$")
    public void startHttp() throws IOException {
        http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.setExecutor(Executors.newCachedThreadPool(daemonThreads()));
        registerContexts(http);
        http.start();
        publish("http://127.0.0.1:" + http.getAddress().getPort(), "local");
    }

    @Дано("^запущен локальный HTTPS сервер с самоподписанным сертификатом$")
    public void startHttps() throws Exception {
        SSLContext sslContext = sslContext();
        https = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        https.setHttpsConfigurator(new HttpsConfigurator(sslContext));
        https.setExecutor(Executors.newCachedThreadPool(daemonThreads()));
        registerContexts(https);
        https.start();
        publish("https://127.0.0.1:" + https.getAddress().getPort(), "local_https");
    }

    @Дано("^количество повторов запроса равно (\\d+)$")
    public void setRetries(int retries) {
        if (originalRetries < 0) {
            originalRetries = SendRequestSteps.requestRetries;
        }
        SendRequestSteps.requestRetries = retries;
    }

    @After
    public void stop() {
        if (http != null) {
            http.stop(0);
        }
        if (https != null) {
            https.stop(0);
        }
        if (originalRetries >= 0) {
            SendRequestSteps.requestRetries = originalRetries;
        }
    }

    private void publish(String base, String prefix) {
        CoreScenario scenario = CoreScenario.getInstance();
        scenario.setVar(prefix + "_echo", base + "/echo");
        scenario.setVar(prefix + "_echo_by_id", base + "/echo/items/{id}");
        scenario.setVar(prefix + "_status", base + "/status/{code}");
        scenario.setVar(prefix + "_flaky", base + "/flaky/{key}/{failures}");
        scenario.setVar(prefix + "_poll", base + "/poll/{key}/{ready_after}");
        scenario.setVar(prefix + "_cookies", base + "/cookies");
        scenario.setVar(prefix + "_headers", base + "/headers");
        scenario.setVar(prefix + "_basic", base + "/secure/basic");
        scenario.setVar(prefix + "_basic_no_challenge", base + "/secure/basic-no-challenge");
        scenario.setVar(prefix + "_bearer", base + "/secure/bearer");
        scenario.setVar(prefix + "_text", base + "/text");
        scenario.setVar(prefix + "_flaky_twice", base + "/flaky/twice/2");
        scenario.setVar(prefix + "_poll_ready_on_3", base + "/poll/ready3/3");
    }

    private void registerContexts(com.sun.net.httpserver.HttpServer server) {
        server.createContext("/echo", safe(this::echo));
        server.createContext("/status/", safe(this::status));
        server.createContext("/flaky/", safe(this::flaky));
        server.createContext("/poll/", safe(this::poll));
        server.createContext("/cookies", safe(this::cookies));
        server.createContext("/headers", safe(this::headers));
        server.createContext("/secure/basic", safe(ex -> basic(ex, true)));
        server.createContext("/secure/basic-no-challenge", safe(ex -> basic(ex, false)));
        server.createContext("/secure/bearer", safe(this::bearer));
        server.createContext("/text", safe(ex -> respond(ex, 200, "text/plain; charset=UTF-8", "plain-text-body")));
    }

    // =======================================================================
    // ЭНДПОИНТЫ
    // =======================================================================

    /** Возвращает JSON с тем, что сервер получил от клиента. */
    private void echo(HttpExchange ex) throws IOException {
        byte[] body = readAll(ex.getRequestBody());
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("method", ex.getRequestMethod());
        out.put("path", ex.getRequestURI().getPath());
        out.put("query", parseParams(ex.getRequestURI().getRawQuery()));

        Map<String, String> headers = new LinkedHashMap<>();
        ex.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(Locale.ROOT), String.join(", ", values)));
        out.put("headers", headers);
        out.put("cookies", parseCookies(ex.getRequestHeaders().getFirst("Cookie")));
        out.put("contentType", contentType);

        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")) {
            out.put("form", parseParams(new String(body, StandardCharsets.UTF_8)));
        }
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/")) {
            out.put("multipart", parseMultipart(body, contentType));
        } else {
            out.put("body", new String(body, StandardCharsets.UTF_8));
        }
        respondJson(ex, 200, out);
    }

    private void status(HttpExchange ex) throws IOException {
        String[] segments = ex.getRequestURI().getPath().split("/");
        int code = Integer.parseInt(segments[segments.length - 1]);
        respondJson(ex, code, Map.of("status", code));
    }

    /** Первые {failures} обращений по ключу отвечают 503, затем 200. */
    private void flaky(HttpExchange ex) throws IOException {
        String[] segments = ex.getRequestURI().getPath().split("/");
        String key = segments[2];
        int failures = Integer.parseInt(segments[3]);
        int attempt = counters.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        ex.getResponseHeaders().add("X-Attempt", String.valueOf(attempt));
        boolean failing = attempt <= failures;
        respondJson(ex, failing ? 503 : 200, ordered("attempt", attempt, "state", failing ? "FAILING" : "OK"));
    }

    /** Всегда 200, но состояние (заголовок, cookie, тело) становится READY только с {ready_after}-го обращения. */
    private void poll(HttpExchange ex) throws IOException {
        String[] segments = ex.getRequestURI().getPath().split("/");
        String key = segments[2];
        int readyAfter = Integer.parseInt(segments[3]);
        int attempt = counters.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        String state = attempt >= readyAfter ? "READY" : "PENDING";
        ex.getResponseHeaders().add("X-State", state);
        ex.getResponseHeaders().add("Set-Cookie", "state=" + state + "; Path=/");
        respondJson(ex, 200, ordered("attempt", attempt, "state", state));
    }

    private void cookies(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("Set-Cookie", "session=abc123; Path=/");
        ex.getResponseHeaders().add("Set-Cookie", "theme=dark; Path=/");
        respondJson(ex, 200, Map.of("cookies", "set"));
    }

    private void headers(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().add("X-Custom", "custom-value");
        ex.getResponseHeaders().add("X-Request-Id", "req-42");
        respondJson(ex, 200, Map.of("ok", true));
    }

    private void basic(HttpExchange ex, boolean sendChallenge) throws IOException {
        String expected = "Basic " + Base64.getEncoder()
                .encodeToString((BASIC_USER + ":" + BASIC_PASSWORD).getBytes(StandardCharsets.UTF_8));
        if (expected.equals(ex.getRequestHeaders().getFirst("Authorization"))) {
            respondJson(ex, 200, Map.of("authenticated", true, "scheme", "basic"));
            return;
        }
        if (sendChallenge) {
            ex.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"test\"");
        }
        respondJson(ex, 401, Map.of("authenticated", false));
    }

    private void bearer(HttpExchange ex) throws IOException {
        if (("Bearer " + BEARER_TOKEN).equals(ex.getRequestHeaders().getFirst("Authorization"))) {
            respondJson(ex, 200, Map.of("authenticated", true, "scheme", "bearer"));
            return;
        }
        respondJson(ex, 401, Map.of("authenticated", false));
    }

    // =======================================================================
    // ВСПОМОГАТЕЛЬНОЕ
    // =======================================================================

    private static Map<String, Object> ordered(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private static HttpHandler safe(HttpHandler handler) {
        return ex -> {
            try {
                handler.handle(ex);
            } catch (Exception e) {
                byte[] error = ("handler error: " + e).getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(500, error.length);
                ex.getResponseBody().write(error);
            } finally {
                ex.close();
            }
        };
    }

    private static void respondJson(HttpExchange ex, int status, Object payload) throws IOException {
        try {
            respond(ex, status, "application/json", JSON.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new IOException(e);
        }
    }

    private static void respond(HttpExchange ex, int status, String contentType, String body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", contentType);
        boolean noBody = "HEAD".equals(ex.getRequestMethod()) || status == 204 || status == 304 || status < 200;
        if (noBody) {
            ex.sendResponseHeaders(status, -1);
            return;
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
    }

    private static byte[] readAll(InputStream in) throws IOException {
        return in.readAllBytes();
    }

    private static Map<String, List<String>> parseParams(String raw) {
        Map<String, List<String>> params = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) {
            return params;
        }
        for (String pair : raw.split("&")) {
            int eq = pair.indexOf('=');
            String key = URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return params;
    }

    private static Map<String, String> parseCookies(String header) {
        Map<String, String> cookies = new LinkedHashMap<>();
        if (header == null) {
            return cookies;
        }
        for (String pair : header.split(";")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                cookies.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
            }
        }
        return cookies;
    }

    /** Простейший разбор multipart/form-data: достаточно для текстовых частей и небольших файлов. */
    private static List<Map<String, String>> parseMultipart(byte[] body, String contentType) {
        List<Map<String, String>> parts = new ArrayList<>();
        int idx = contentType.indexOf("boundary=");
        if (idx < 0) {
            return parts;
        }
        String boundary = contentType.substring(idx + "boundary=".length()).replace("\"", "").split(";")[0].trim();
        String raw = new String(body, StandardCharsets.ISO_8859_1);
        for (String chunk : raw.split(java.util.regex.Pattern.quote("--" + boundary))) {
            String part = chunk.startsWith("\r\n") ? chunk.substring(2) : chunk;
            if (part.isBlank() || part.startsWith("--")) {
                continue;
            }
            int headersEnd = part.indexOf("\r\n\r\n");
            if (headersEnd < 0) {
                continue;
            }
            String headerBlock = part.substring(0, headersEnd);
            String content = part.substring(headersEnd + 4);
            if (content.endsWith("\r\n")) {
                content = content.substring(0, content.length() - 2);
            }
            Map<String, String> info = new LinkedHashMap<>();
            for (String line : headerBlock.split("\r\n")) {
                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.startsWith("content-disposition:")) {
                    info.put("name", attribute(line, "name"));
                    String fileName = attribute(line, "filename");
                    if (fileName != null) {
                        info.put("filename", fileName);
                    }
                } else if (lower.startsWith("content-type:")) {
                    info.put("contentType", line.substring("content-type:".length()).trim());
                }
            }
            info.put("content", new String(content.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            parts.add(info);
        }
        return parts;
    }

    private static String attribute(String headerLine, String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("[; ]" + name + "=\"([^\"]*)\"").matcher(headerLine);
        return m.find() ? m.group(1) : null;
    }

    private static java.util.concurrent.ThreadFactory daemonThreads() {
        return runnable -> {
            Thread thread = new Thread(runnable, "local-http-server");
            thread.setDaemon(true);
            return thread;
        };
    }

    // =======================================================================
    // HTTPS: самоподписанный сертификат создаётся штатным keytool один раз на JVM
    // =======================================================================

    private static synchronized SSLContext sslContext() throws Exception {
        if (keyStoreFile == null) {
            Path dir = Files.createTempDirectory("at-library-api-tls");
            Path file = dir.resolve("keystore.p12");
            String keytool = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();
            Process process = new ProcessBuilder(keytool, "-genkeypair",
                    "-alias", "local", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
                    "-dname", "CN=localhost", "-ext", "san=ip:127.0.0.1,dns:localhost",
                    "-storetype", "PKCS12", "-keystore", file.toString(),
                    "-storepass", "changeit", "-keypass", "changeit")
                    .redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            if (process.waitFor() != 0) {
                throw new IllegalStateException("keytool завершился с ошибкой: " + output);
            }
            file.toFile().deleteOnExit();
            dir.toFile().deleteOnExit();
            keyStoreFile = file;
        }
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(keyStoreFile)) {
            keyStore.load(in, "changeit".toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "changeit".toCharArray());
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), null, null);
        return context;
    }
}
