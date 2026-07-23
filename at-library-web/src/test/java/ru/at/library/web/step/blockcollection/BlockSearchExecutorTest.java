package ru.at.library.web.step.blockcollection;

import org.openqa.selenium.StaleElementReferenceException;
import org.testng.annotations.Test;
import ru.at.library.web.scenario.CorePage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertThrows;

public class BlockSearchExecutorTest {

    @Test
    public void retriesCurrentOperationAfterStaleElement() {
        CorePage block = new TestBlock();
        AtomicInteger calls = new AtomicInteger();

        List<CorePage> result = BlockSearchExecutor.filterInSnapshot(
                List.of(block),
                ignored -> {
                    if (calls.getAndIncrement() == 0) {
                        throw new StaleElementReferenceException("test rerender");
                    }
                    return true;
                },
                "Блок не найден"
        );

        assertEquals(calls.get(), 2);
        assertEquals(result.size(), 1);
        assertSame(result.get(0), block);
    }

    @Test
    public void restartsWholeSnapshotAfterStaleInLaterBlock() {
        CorePage firstBlock = new TestBlock();
        CorePage secondBlock = new TestBlock();
        AtomicInteger firstBlockCalls = new AtomicInteger();
        AtomicInteger secondBlockCalls = new AtomicInteger();

        List<CorePage> result = BlockSearchExecutor.filterInSnapshot(
                List.of(firstBlock, secondBlock),
                block -> {
                    if (block == firstBlock) {
                        firstBlockCalls.incrementAndGet();
                        return true;
                    }
                    if (secondBlockCalls.getAndIncrement() == 0) {
                        throw new StaleElementReferenceException("test rerender");
                    }
                    return true;
                },
                "Блоки не найдены"
        );

        assertEquals(firstBlockCalls.get(), 2);
        assertEquals(secondBlockCalls.get(), 2);
        assertEquals(result, List.of(firstBlock, secondBlock));
    }

    @Test
    public void returnsAllMatchingBlocksInOriginalOrder() {
        CorePage firstBlock = new TestBlock();
        CorePage skippedBlock = new TestBlock();
        CorePage thirdBlock = new TestBlock();

        List<CorePage> result = BlockSearchExecutor.filterInSnapshot(
                List.of(firstBlock, skippedBlock, thirdBlock),
                block -> block != skippedBlock,
                "Блоки не найдены"
        );

        assertEquals(result, List.of(firstBlock, thirdBlock));
    }

    @Test
    public void doesNotRetryUnexpectedProgrammingError() {
        AtomicInteger calls = new AtomicInteger();

        assertThrows(
                IllegalStateException.class,
                () -> BlockSearchExecutor.filterInSnapshot(
                        List.of(new TestBlock()),
                        block -> {
                            calls.incrementAndGet();
                            throw new IllegalStateException("test error");
                        },
                        "Блок не найден"
                )
        );

        assertEquals(calls.get(), 1);
    }

    @Test
    public void rejectsInvalidBlockNumbersBeforeReadingContext() {
        assertThrows(
                IllegalArgumentException.class,
                () -> BlockSearchExecutor.awaitBlockByNumber(null, 0, "test")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> BlockSearchExecutor.awaitBlockRoot(null, -1, null, "test")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> BlockSearchExecutor.awaitElementInBlock(null, 0, "element", null, "test")
        );
    }

    private static final class TestBlock extends CorePage {
    }
}
