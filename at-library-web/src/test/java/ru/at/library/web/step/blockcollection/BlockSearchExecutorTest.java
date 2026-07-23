package ru.at.library.web.step.blockcollection;

import org.openqa.selenium.StaleElementReferenceException;
import org.testng.annotations.Test;
import ru.at.library.web.scenario.CorePage;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

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

    private static final class TestBlock extends CorePage {
    }
}
