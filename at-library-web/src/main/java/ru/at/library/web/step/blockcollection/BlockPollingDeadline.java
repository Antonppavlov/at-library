package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;

import java.util.concurrent.TimeUnit;

/**
 * Единый ограничитель polling-циклов для операций со списками блоков.
 *
 * Ограничение одновременно по времени и числу попыток защищает от вечного
 * цикла даже при некорректном pollingInterval. Для измерения времени
 * используется монотонный System.nanoTime().
 */
final class BlockPollingDeadline {

    private static final long MIN_POLLING_MS = 50L;

    private final long timeoutMs;
    private final long pollingMs;
    private final long startedNanos;
    private final long timeoutNanos;
    private final int maxAttempts;
    private int attempts;

    private BlockPollingDeadline(long timeoutMs, long pollingMs) {
        this.timeoutMs = Math.max(0L, timeoutMs);
        this.pollingMs = Math.max(MIN_POLLING_MS, pollingMs);
        this.startedNanos = System.nanoTime();
        this.timeoutNanos = TimeUnit.MILLISECONDS.toNanos(this.timeoutMs);

        long calculatedAttempts = this.timeoutMs / this.pollingMs + 2L;
        this.maxAttempts = calculatedAttempts >= Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : Math.max(1, (int) calculatedAttempts);
    }

    static BlockPollingDeadline fromSelenideConfiguration() {
        return of(Configuration.timeout, Configuration.pollingInterval);
    }

    static BlockPollingDeadline of(long timeoutMs, long pollingMs) {
        return new BlockPollingDeadline(timeoutMs, pollingMs);
    }

    /**
     * Разрешает хотя бы одну попытку даже при timeout=0, затем строго
     * ограничивает выполнение общим временем и максимальным числом проходов.
     */
    boolean tryNextAttempt() {
        if (attempts >= maxAttempts) {
            return false;
        }
        if (attempts > 0 && isExpired()) {
            return false;
        }
        attempts++;
        return true;
    }

    void pauseBeforeNextAttempt() {
        long remainingNanos = remainingNanos();
        if (remainingNanos <= 0L) {
            return;
        }

        long remainingMs = Math.max(1L, TimeUnit.NANOSECONDS.toMillis(remainingNanos));
        Selenide.sleep(Math.min(pollingMs, remainingMs));
    }

    long timeoutMs() {
        return timeoutMs;
    }

    int attempts() {
        return attempts;
    }

    private boolean isExpired() {
        return System.nanoTime() - startedNanos >= timeoutNanos;
    }

    private long remainingNanos() {
        return timeoutNanos - (System.nanoTime() - startedNanos);
    }
}
