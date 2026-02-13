package ru.at.library.core.utils.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.Map;

/**
 * Дополнительный аппендер Log4j2, который собирает логи текущего потока
 * в буфер. Буфер инициализируется в начале сценария и считывается в конце
 * для прикладывания в Allure.
 */
public class ScenarioLogAppender extends AbstractAppender {

    private static final Logger LOG = LogManager.getLogger(ScenarioLogAppender.class);

    /**
     * Буфер логов для текущего потока выполнения (один поток = один сценарий).
     */
    private static final ThreadLocal<StringBuilder> SCENARIO_LOG = new ThreadLocal<>();

    /**
     * Счетчик событий для диагностики.
     */
    private static final ThreadLocal<Integer> EVENT_COUNT = new ThreadLocal<>();

    private static volatile ScenarioLogAppender INSTANCE;

    protected ScenarioLogAppender(String name,
                                  Filter filter,
                                  Layout<? extends Serializable> layout,
                                  boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    /**
     * Инициализация и подключение аппендера ко всем логгерам приложения.
     * Вызывается один раз при первом обращении к CoreInitialSetup.
     */
    public static void installIfNeeded() {
        if (INSTANCE != null) {
            return;
        }
        synchronized (ScenarioLogAppender.class) {
            if (INSTANCE != null) {
                return;
            }
            try {
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                Configuration config = ctx.getConfiguration();

                Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                        .withConfiguration(config)
                        .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%t] %c{1} - %m%n")
                        .build();

                ScenarioLogAppender appender = new ScenarioLogAppender(
                        "ScenarioLogAppender",
                        null,
                        layout,
                        true
                );
                appender.start();

                config.addAppender(appender);

                // Подключаемся к root-логгеру с уровнем TRACE, чтобы получать все логи
                config.getRootLogger().addAppender(appender, org.apache.logging.log4j.Level.TRACE, null);

                // Явно подключаемся ко всем существующим логгерам тоже
                int connectedLoggers = 0;
                for (Map.Entry<String, LoggerConfig> entry : config.getLoggers().entrySet()) {
                    LoggerConfig loggerConfig = entry.getValue();
                    loggerConfig.addAppender(appender, org.apache.logging.log4j.Level.TRACE, null);
                    connectedLoggers++;
                    LOG.debug("ScenarioLogAppender: подключен к логгеру: {}", entry.getKey());
                }

                ctx.updateLoggers();
                INSTANCE = appender;
                LOG.info("ScenarioLogAppender успешно инициализирован и подключен к root + {} логгерам", connectedLoggers);
            } catch (Exception e) {
                // Не должны ломать запуск тестов, если что-то пошло не так
                LOG.error("Не удалось инициализировать ScenarioLogAppender", e);
            }
        }
    }

    /**
     * Вызывается в начале сценария – очищает и инициализирует буфер логов.
     */
    public static void startScenarioLogging() {
        SCENARIO_LOG.set(new StringBuilder());
        EVENT_COUNT.set(0);
        LOG.debug("ScenarioLogAppender: начат сбор логов для потока {}", Thread.currentThread().getName());
    }

    /**
     * Возвращает накопленный лог текущего сценария и очищает буфер.
     */
    public static String getAndClearScenarioLog() {
        StringBuilder sb = SCENARIO_LOG.get();
        Integer eventCount = EVENT_COUNT.get();
        if (sb == null) {
            LOG.warn("ScenarioLogAppender: буфер логов отсутствует для потока {}", Thread.currentThread().getName());
            return null;
        }
        String result = sb.toString();
        int logLength = result.length();
        int events = eventCount != null ? eventCount : 0;
        SCENARIO_LOG.remove();
        EVENT_COUNT.remove();
        LOG.info("ScenarioLogAppender: собрано {} символов логов ({} событий) для потока {}", logLength, events, Thread.currentThread().getName());
        return result;
    }

    @Override
    public void append(LogEvent event) {
        StringBuilder sb = SCENARIO_LOG.get();
        if (sb == null) {
            return; // логгирование вне контекста сценария – пропускаем
        }
        try {
            // Инкрементируем счетчик событий
            Integer count = EVENT_COUNT.get();
            EVENT_COUNT.set(count != null ? count + 1 : 1);

            Layout<? extends Serializable> layout = getLayout();
            if (layout != null) {
                Serializable serializable = layout.toSerializable(event);
                sb.append(serializable);
            } else {
                sb.append(event.getMessage().getFormattedMessage()).append("\n");
            }
        } catch (Exception e) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(e);
            }
        }
    }
}