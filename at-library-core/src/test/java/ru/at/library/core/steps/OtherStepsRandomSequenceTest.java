package ru.at.library.core.steps;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import static ru.at.library.core.steps.OtherSteps.getRandNumSequence;

public class OtherStepsRandomSequenceTest {

    @Test(timeOut = 5_000L)
    public void generatesNumericSequencesWithoutBlockingParallelThreads() throws Exception {
        int taskCount = 2_000;
        ExecutorService executor = Executors.newFixedThreadPool(
                64,
                Thread.ofPlatform()
                        .daemon()
                        .name("numeric-generator-", 0L)
                        .factory()
        );

        try {
            List<Callable<String>> tasks = new ArrayList<>(taskCount);
            for (int index = 0; index < taskCount; index++) {
                tasks.add(() -> getRandNumSequence(32));
            }

            List<Future<String>> results = executor.invokeAll(tasks, 3L, TimeUnit.SECONDS);
            for (Future<String> result : results) {
                assertFalse(result.isCancelled(), "Генерация не уложилась в общий таймаут");
                String value = result.get();
                assertEquals(value.length(), 32);
                assertTrue(value.chars().allMatch(Character::isDigit));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void supportsZeroLengthAndRejectsNegativeLength() {
        assertEquals(getRandNumSequence(0), "");
        assertThrows(IllegalArgumentException.class, () -> getRandNumSequence(-1));
    }
}
