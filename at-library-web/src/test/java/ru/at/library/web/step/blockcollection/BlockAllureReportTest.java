package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Allure;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

@Test(singleThreaded = true)
public class BlockAllureReportTest {

    private static final String DEFAULT_LISTENER_NAME = "AllureSelenide";
    private static final String CUSTOM_LISTENER_NAME = "custom-allure-listener";
    private static final String OTHER_LISTENER_NAME = "other-selenide-listener";

    private LogEventListener previousDefaultListener;
    private LogEventListener previousCustomListener;
    private LogEventListener previousOtherListener;
    private String previousConfiguredNames;

    @BeforeMethod
    public void preserveListenersAndProperty() {
        previousDefaultListener = SelenideLogger.removeListener(DEFAULT_LISTENER_NAME);
        previousCustomListener = SelenideLogger.removeListener(CUSTOM_LISTENER_NAME);
        previousOtherListener = SelenideLogger.removeListener(OTHER_LISTENER_NAME);
        previousConfiguredNames =
                System.getProperty(BlockAllureReport.LISTENER_NAMES_PROPERTY);
    }

    @AfterMethod
    public void restoreListenersAndProperty() {
        SelenideLogger.removeListener(DEFAULT_LISTENER_NAME);
        SelenideLogger.removeListener(CUSTOM_LISTENER_NAME);
        SelenideLogger.removeListener(OTHER_LISTENER_NAME);
        restore(DEFAULT_LISTENER_NAME, previousDefaultListener);
        restore(CUSTOM_LISTENER_NAME, previousCustomListener);
        restore(OTHER_LISTENER_NAME, previousOtherListener);

        if (previousConfiguredNames == null) {
            System.clearProperty(BlockAllureReport.LISTENER_NAMES_PROPERTY);
        } else {
            System.setProperty(
                    BlockAllureReport.LISTENER_NAMES_PROPERTY,
                    previousConfiguredNames
            );
        }
    }

    @Test
    public void suppressesTechnicalEventsAndRestoresListener() {
        RecordingListener listener = register(DEFAULT_LISTENER_NAME);

        BlockAllureReport.withoutSelenideSteps(
                () -> emitSelenideEvent("внутри blockcollection")
        );

        assertEquals(listener.events(), 0);
        assertTrue(SelenideLogger.hasListener(DEFAULT_LISTENER_NAME));

        emitSelenideEvent("после blockcollection");
        assertEquals(listener.events(), 2);
    }

    @Test
    public void restoresListenerAfterExceptionAndNestedScope() {
        RecordingListener listener = register(DEFAULT_LISTENER_NAME);

        assertThrows(IllegalStateException.class, () ->
                BlockAllureReport.withoutSelenideSteps(() ->
                        BlockAllureReport.withoutSelenideSteps(() -> {
                            emitSelenideEvent("вложенный scope");
                            throw new IllegalStateException("test");
                        })
                )
        );

        assertEquals(listener.events(), 0);
        assertTrue(SelenideLogger.hasListener(DEFAULT_LISTENER_NAME));

        emitSelenideEvent("после исключения");
        assertEquals(listener.events(), 2);
    }

    @Test
    public void supportsConfiguredListenerName() {
        System.setProperty(
                BlockAllureReport.LISTENER_NAMES_PROPERTY,
                CUSTOM_LISTENER_NAME
        );
        RecordingListener listener = register(CUSTOM_LISTENER_NAME);

        BlockAllureReport.withoutSelenideSteps(
                () -> emitSelenideEvent("custom listener")
        );

        assertEquals(listener.events(), 0);
        assertTrue(SelenideLogger.hasListener(CUSTOM_LISTENER_NAME));

        emitSelenideEvent("после custom scope");
        assertEquals(listener.events(), 2);
    }

    @Test
    public void keepsUnrelatedSelenideListenerEnabled() {
        RecordingListener allureListener = register(DEFAULT_LISTENER_NAME);
        RecordingListener otherListener = register(OTHER_LISTENER_NAME);

        BlockAllureReport.withoutSelenideSteps(
                () -> emitSelenideEvent("внутри blockcollection")
        );

        assertEquals(allureListener.events(), 0);
        assertEquals(otherListener.events(), 2);
    }

    @Test
    public void keepsStepNameShortAndWritesDetailsToParameters() {
        RecordingStepContext step = new RecordingStepContext();

        BlockAllureReport.finishStep(
                step,
                "Блок №2 из 5 — элемент 'Название'",
                "НЕ СООТВЕТСТВУЕТ",
                "текст равен 'Ожидаемый текст'",
                "видим, text='Фактический текст'"
        );
        BlockAllureReport.addError(
                step,
                new AssertionError("Текст не совпал")
        );

        assertEquals(
                step.name(),
                "Блок №2 из 5 — элемент 'Название' — НЕ СООТВЕТСТВУЕТ"
        );
        assertEquals(
                step.parameters().get("Ожидается"),
                "текст равен 'Ожидаемый текст'"
        );
        assertEquals(
                step.parameters().get("Фактически"),
                "видим, text='Фактический текст'"
        );
        assertEquals(
                step.parameters().get("Ошибка"),
                "AssertionError: Текст не совпал"
        );
    }

    @Test(timeOut = 15_000L)
    public void suppressionDoesNotAffectParallelThread() throws Exception {
        RecordingListener currentThreadListener = register(DEFAULT_LISTENER_NAME);
        CountDownLatch workerReady = new CountDownLatch(1);
        CountDownLatch runWorkerEvent = new CountDownLatch(1);
        ExecutorService executor = Executors.newSingleThreadExecutor(
                Thread.ofPlatform()
                        .daemon()
                        .name("block-allure-listener-test-", 0L)
                        .factory()
        );

        try {
            Future<Integer> workerEvents = executor.submit(() -> {
                RecordingListener workerListener = register(DEFAULT_LISTENER_NAME);
                workerReady.countDown();
                assertTrue(runWorkerEvent.await(5L, TimeUnit.SECONDS));
                emitSelenideEvent("параллельный поток");
                SelenideLogger.removeListener(DEFAULT_LISTENER_NAME);
                return workerListener.events();
            });

            assertTrue(workerReady.await(5L, TimeUnit.SECONDS));
            BlockAllureReport.withoutSelenideSteps(() -> {
                runWorkerEvent.countDown();
                emitSelenideEvent("текущий поток");
            });

            assertEquals(workerEvents.get(5L, TimeUnit.SECONDS).intValue(), 2);
            assertEquals(currentThreadListener.events(), 0);
            assertTrue(SelenideLogger.hasListener(DEFAULT_LISTENER_NAME));
        } finally {
            executor.shutdownNow();
        }
    }

    private RecordingListener register(String name) {
        RecordingListener listener = new RecordingListener();
        SelenideLogger.addListener(name, listener);
        return listener;
    }

    private void restore(String name, LogEventListener listener) {
        if (listener != null) {
            SelenideLogger.addListener(name, listener);
        }
    }

    private void emitSelenideEvent(String subject) {
        SelenideLogger.run("test", subject, () -> {
        });
    }

    private static class RecordingListener implements LogEventListener {

        private final AtomicInteger events = new AtomicInteger();

        @Override
        public void afterEvent(LogEvent currentLog) {
            events.incrementAndGet();
        }

        @Override
        public void beforeEvent(LogEvent currentLog) {
            events.incrementAndGet();
        }

        private int events() {
            return events.get();
        }
    }

    private static class RecordingStepContext implements Allure.StepContext {

        private final Map<String, String> parameters = new LinkedHashMap<>();
        private String name;

        @Override
        public void name(String name) {
            this.name = name;
        }

        @Override
        public <T> T parameter(String name, T value) {
            parameters.put(name, String.valueOf(value));
            return value;
        }

        private String name() {
            return name;
        }

        private Map<String, String> parameters() {
            return parameters;
        }
    }
}
