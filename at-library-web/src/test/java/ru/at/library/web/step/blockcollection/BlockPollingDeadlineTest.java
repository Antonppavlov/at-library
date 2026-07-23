package ru.at.library.web.step.blockcollection;

import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BlockPollingDeadlineTest {

    @Test
    public void zeroTimeoutAllowsOnlyOneAttempt() {
        BlockPollingDeadline deadline = BlockPollingDeadline.of(0L, 0L);

        assertTrue(deadline.tryNextAttempt());
        assertFalse(deadline.tryNextAttempt());
        assertEquals(deadline.attempts(), 1);
        assertEquals(deadline.timeoutMs(), 0L);
    }

    @Test
    public void attemptCountIsBoundedEvenWithoutPauses() {
        BlockPollingDeadline deadline = BlockPollingDeadline.of(60_000L, 1L);

        while (deadline.tryNextAttempt()) {
            // Имитируем ошибочный polling без пауз: защита по числу попыток
            // всё равно должна завершить цикл.
        }

        assertEquals(deadline.attempts(), 1_202);
    }

    @Test
    public void negativeTimeoutIsNormalizedToZero() {
        BlockPollingDeadline deadline = BlockPollingDeadline.of(-1L, 50L);

        assertTrue(deadline.tryNextAttempt());
        assertFalse(deadline.tryNextAttempt());
        assertEquals(deadline.timeoutMs(), 0L);
    }

    @Test
    public void hugeTimeoutDoesNotOverflowNanoseconds() {
        BlockPollingDeadline deadline = BlockPollingDeadline.of(Long.MAX_VALUE, 50L);

        assertTrue(deadline.tryNextAttempt());
        assertEquals(deadline.timeoutMs(), Long.MAX_VALUE);
    }

    @Test
    public void pollingLongerThanTimeoutDoesNotOversleep() {
        BlockPollingDeadline deadline = BlockPollingDeadline.of(120L, 5_000L);
        long startedNanos = System.nanoTime();

        assertTrue(deadline.tryNextAttempt());
        deadline.pauseBeforeNextAttempt();
        assertFalse(deadline.tryNextAttempt());

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
        assertTrue(elapsedMs < 2_000L, "Пауза превысила общий deadline: " + elapsedMs + " мс");
    }
}
