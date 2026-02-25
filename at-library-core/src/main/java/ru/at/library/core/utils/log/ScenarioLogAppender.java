package ru.at.library.core.utils.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Log4j2 плагин-аппендер, собирающий логи текущего потока в буфер.
 * Буфер инициализируется в начале сценария и считывается в конце
 * для прикладывания в Allure и записи в отдельный файл.
 * <p>
 * <b>Рекомендуемый способ подключения</b> — через log4j2.xml потребителя:
 * <pre>{@code
 * <Configuration packages="ru.at.library.core.utils.log">
 *   <Appenders>
 *     <ScenarioLog name="ScenarioLogAppender">
 *       <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%t] %c{1} - %m%n"/>
 *     </ScenarioLog>
 *   </Appenders>
 *   <Loggers>
 *     <Root level="INFO">
 *       <AppenderRef ref="ScenarioLogAppender" level="TRACE"/>
 *     </Root>
 *   </Loggers>
 * </Configuration>
 * }</pre>
 * <p>
 * Если аппендер не сконфигурирован в XML, {@link #installIfNeeded()} выполнит
 * программную инициализацию (фолбэк с ограниченной функциональностью).
 */
@Plugin(name = "ScenarioLog", category = Core.CATEGORY_NAME,
        elementType = "appender", printObject = true)
public class ScenarioLogAppender extends AbstractAppender {

    /**
     * StatusLogger — внутренний логгер Log4j, доступный даже во время инициализации конфигурации.
     * Используется в {@link #createAppender} (фабрике, вызываемой до завершения init Log4j).
     */
    private static final StatusLogger STATUS_LOG = StatusLogger.getLogger();

    /** Буфер логов для текущего потока выполнения (один поток = один сценарий). */
    private static final ThreadLocal<StringBuilder> SCENARIO_LOG = new ThreadLocal<>();

    private static volatile ScenarioLogAppender INSTANCE;

    protected ScenarioLogAppender(String name,
                                  Filter filter,
                                  Layout<? extends Serializable> layout,
                                  boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
    }

    // ── Фабрика для XML-конфигурации ──────────────────────────────────

    /**
     * Создаёт экземпляр аппендера из XML-конфигурации log4j2.
     */
    @PluginFactory
    public static ScenarioLogAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") Filter filter) {

        if (name == null) {
            STATUS_LOG.error("Не указано имя для ScenarioLogAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.newBuilder()
                    .withCharset(StandardCharsets.UTF_8)
                    .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%t] %c{1} - %m%n")
                    .build();
        }

        ScenarioLogAppender appender = new ScenarioLogAppender(name, filter, layout, true);
        INSTANCE = appender;
        return appender;
    }

    // ── Программная инициализация (фолбэк) ───────────────────────────

    /**
     * Программная инициализация — используется только если аппендер
     * НЕ сконфигурирован в log4j2.xml потребителя.
     * <p>
     * ОГРАНИЧЕНИЕ: программный {@code addAppender()} может не перехватывать
     * события, пришедшие через additivity от дочерних LoggerConfig'ов.
     * Для полного сбора логов используйте конфигурацию через XML.
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
                        .withCharset(StandardCharsets.UTF_8)
                        .withPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p [%t] %c{1} - %m%n")
                        .build();

                ScenarioLogAppender appender = new ScenarioLogAppender(
                        "ScenarioLogAppender", null, layout, true
                );
                appender.start();
                config.addAppender(appender);

                // Подключаемся к root и ко всем именованным LoggerConfig'ам
                config.getRootLogger().addAppender(appender, org.apache.logging.log4j.Level.TRACE, null);
                for (LoggerConfig loggerConfig : config.getLoggers().values()) {
                    loggerConfig.addAppender(appender, org.apache.logging.log4j.Level.TRACE, null);
                }

                ctx.updateLoggers();
                INSTANCE = appender;
            } catch (Exception e) {
                STATUS_LOG.error("Не удалось инициализировать ScenarioLogAppender", e);
            }
        }
    }

    // ── API для управления сбором логов ───────────────────────────────

    /**
     * Вызывается в начале сценария — очищает и инициализирует буфер логов.
     */
    public static void startScenarioLogging() {
        SCENARIO_LOG.set(new StringBuilder());
    }

    /**
     * Возвращает накопленный лог текущего сценария и очищает буфер.
     */
    public static String getAndClearScenarioLog() {
        StringBuilder sb = SCENARIO_LOG.get();
        if (sb == null) {
            return null;
        }
        String result = sb.toString();
        SCENARIO_LOG.remove();
        return result;
    }

    // ── Основной метод аппендера ──────────────────────────────────────

    @Override
    public void append(LogEvent event) {
        StringBuilder sb = SCENARIO_LOG.get();
        if (sb == null) {
            return; // логгирование вне контекста сценария — пропускаем
        }
        try {
            Layout<? extends Serializable> layout = getLayout();
            if (layout != null) {
                sb.append(layout.toSerializable(event));
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
