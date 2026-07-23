package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import com.codeborne.selenide.ex.ElementNotFound;
import io.qameta.allure.Allure;
import org.openqa.selenium.StaleElementReferenceException;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.step.browser.BrowserSteps;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.blockListToString;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.scrollToElementCenter;

/**
 * Повторно получает список блоков и выполняет операцию до успеха
 * или до истечения общего таймаута Selenide.
 */
final class BlockSearchExecutor {

    private static final Consumer<SelenideElement> NO_ELEMENT_ACTION = element -> {
    };
    private static final Consumer<CorePage> NO_BLOCK_ACTION = block -> {
    };
    private static final Consumer<List<CorePage>> NO_BLOCKS_ACTION = blocks -> {
    };

    private BlockSearchExecutor() {
    }

    static CorePage findInContext(BlockListContext context,
                                  String elementName,
                                  WebElementCondition condition,
                                  String notFoundMessage) {
        return findInContext(context, elementName, condition, NO_BLOCK_ACTION, notFoundMessage);
    }

    static CorePage findInContext(BlockListContext context,
                                  String elementName,
                                  WebElementCondition condition,
                                  Consumer<CorePage> onMatched,
                                  String notFoundMessage) {
        return find(
                context::freshBlocks,
                elementName,
                condition,
                onMatched,
                notFoundMessage,
                false
        );
    }

    static CorePage findInSnapshot(List<CorePage> blocks,
                                   String elementName,
                                   WebElementCondition condition,
                                   String notFoundMessage) {
        return find(
                () -> blocks,
                elementName,
                condition,
                NO_BLOCK_ACTION,
                notFoundMessage,
                true
        );
    }

    static List<CorePage> filterInContext(BlockListContext context,
                                          Predicate<CorePage> predicate,
                                          Consumer<List<CorePage>> onMatched,
                                          String notFoundMessage) {
        return filter(
                context::freshBlocks,
                predicate,
                onMatched,
                notFoundMessage,
                false
        );
    }

    static List<CorePage> filterInSnapshot(List<CorePage> blocks,
                                           Predicate<CorePage> predicate,
                                           String notFoundMessage) {
        return filter(
                () -> blocks,
                predicate,
                NO_BLOCKS_ACTION,
                notFoundMessage,
                true
        );
    }

    static CorePage awaitBlockByNumber(BlockListContext context,
                                       int blockNumber,
                                       String notFoundMessage) {
        validateBlockNumber(blockNumber);
        return retry(
                context::freshBlocks,
                blocks -> blocks.size() < blockNumber
                        ? Attempt.again("ожидаем блок №" + blockNumber +
                        ", сейчас блоков: " + blocks.size())
                        : Attempt.done(
                                blocks.get(blockNumber - 1),
                                "блок №" + blockNumber + " доступен"
                        ),
                notFoundMessage,
                false
        );
    }

    static CorePage awaitElementInBlock(BlockListContext context,
                                        int blockNumber,
                                        String elementName,
                                        WebElementCondition condition,
                                        Consumer<SelenideElement> onMatched,
                                        String notFoundMessage) {
        return awaitTargetInBlock(
                context,
                blockNumber,
                "элемент '" + elementName + "'",
                block -> block.getElement(elementName),
                condition,
                onMatched,
                notFoundMessage
        );
    }

    static CorePage awaitElementInBlock(BlockListContext context,
                                        int blockNumber,
                                        String elementName,
                                        WebElementCondition condition,
                                        String notFoundMessage) {
        return awaitElementInBlock(
                context,
                blockNumber,
                elementName,
                condition,
                NO_ELEMENT_ACTION,
                notFoundMessage
        );
    }

    static CorePage awaitBlockRoot(BlockListContext context,
                                   int blockNumber,
                                   WebElementCondition condition,
                                   Consumer<SelenideElement> onMatched,
                                   String notFoundMessage) {
        return awaitTargetInBlock(
                context,
                blockNumber,
                "корневой элемент блока",
                CorePage::getSelf,
                condition,
                onMatched,
                notFoundMessage
        );
    }

    static CorePage awaitBlockRoot(BlockListContext context,
                                   int blockNumber,
                                   WebElementCondition condition,
                                   String notFoundMessage) {
        return awaitBlockRoot(
                context,
                blockNumber,
                condition,
                NO_ELEMENT_ACTION,
                notFoundMessage
        );
    }

    private static CorePage find(Supplier<List<CorePage>> blocksSupplier,
                                 String elementName,
                                 WebElementCondition condition,
                                 Consumer<CorePage> onMatched,
                                 String notFoundMessage,
                                 boolean takeScreenshotOnFailure) {
        return retry(
                blocksSupplier,
                blocks -> {
                    for (int index = 0; index < blocks.size(); index++) {
                        int blockNumber = index + 1;
                        CorePage block = blocks.get(index);
                        BlockState state = checkBlock(
                                blockNumber,
                                () -> {
                                    SelenideElement element = block.getElement(elementName);
                                    if (!scrollToElementCenter(element) || !element.is(condition)) {
                                        return false;
                                    }
                                    onMatched.accept(block);
                                    return true;
                                }
                        );

                        if (state == BlockState.RETRY) {
                            return Attempt.again("DOM обновился при проверке блока №" + blockNumber);
                        }
                        if (state == BlockState.MATCHED) {
                            return Attempt.done(
                                    block,
                                    "найден блок №" + blockNumber + " из " + blocks.size()
                            );
                        }
                    }

                    return Attempt.again(blocks.isEmpty()
                            ? "список пока пуст"
                            : "проверено блоков: " + blocks.size() + ", совпадений нет");
                },
                notFoundMessage,
                takeScreenshotOnFailure
        );
    }

    private static List<CorePage> filter(Supplier<List<CorePage>> blocksSupplier,
                                         Predicate<CorePage> predicate,
                                         Consumer<List<CorePage>> onMatched,
                                         String notFoundMessage,
                                         boolean takeScreenshotOnFailure) {
        return retry(
                blocksSupplier,
                blocks -> {
                    List<CorePage> matchedBlocks = new ArrayList<>();

                    for (int index = 0; index < blocks.size(); index++) {
                        int blockNumber = index + 1;
                        CorePage block = blocks.get(index);
                        BlockState state = checkBlock(
                                blockNumber,
                                () -> predicate.test(block)
                        );

                        if (state == BlockState.RETRY) {
                            return Attempt.again("DOM обновился при проверке блока №" + blockNumber);
                        }
                        if (state == BlockState.MATCHED) {
                            matchedBlocks.add(block);
                        }
                    }

                    if (matchedBlocks.isEmpty()) {
                        return Attempt.again(blocks.isEmpty()
                                ? "список пока пуст"
                                : "проверено блоков: " + blocks.size() + ", совпадений нет");
                    }

                    onMatched.accept(matchedBlocks);
                    return Attempt.done(
                            matchedBlocks,
                            "найдено блоков: " + matchedBlocks.size() + " из " + blocks.size()
                    );
                },
                notFoundMessage,
                takeScreenshotOnFailure
        );
    }

    private static CorePage awaitTargetInBlock(BlockListContext context,
                                               int blockNumber,
                                               String targetDescription,
                                               Function<CorePage, SelenideElement> targetProvider,
                                               WebElementCondition condition,
                                               Consumer<SelenideElement> onMatched,
                                               String notFoundMessage) {
        validateBlockNumber(blockNumber);
        return retry(
                context::freshBlocks,
                blocks -> {
                    if (blocks.size() < blockNumber) {
                        return Attempt.again("ожидаем блок №" + blockNumber +
                                ", сейчас блоков: " + blocks.size());
                    }

                    CorePage block = blocks.get(blockNumber - 1);
                    BlockState state = checkBlock(
                            blockNumber,
                            () -> {
                                SelenideElement target = targetProvider.apply(block);
                                if (!scrollToElementCenter(target) || !target.is(condition)) {
                                    return false;
                                }
                                onMatched.accept(target);
                                return true;
                            }
                    );

                    return state == BlockState.MATCHED
                            ? Attempt.done(
                            block,
                            "условие выполнено в блоке №" + blockNumber
                    )
                            : Attempt.again(state == BlockState.RETRY
                            ? "DOM обновился при работе с блоком №" + blockNumber
                            : targetDescription + " пока не соответствует условию");
                },
                notFoundMessage,
                false
        );
    }

    private static BlockState checkBlock(int blockNumber,
                                         Supplier<Boolean> operation) {
        return Allure.step("Блок №" + blockNumber, step -> {
            try {
                if (!operation.get()) {
                    step.name("Блок №" + blockNumber + " — условие не выполнено");
                    return BlockState.NOT_MATCHED;
                }

                step.name("Блок №" + blockNumber + " — условие выполнено");
                return BlockState.MATCHED;
            } catch (StaleElementReferenceException | ElementNotFound error) {
                step.name("Блок №" + blockNumber + " — DOM обновился, повторим");
                return BlockState.RETRY;
            }
        });
    }

    private static <T> T retry(Supplier<List<CorePage>> blocksSupplier,
                               Function<List<CorePage>, Attempt<T>> operation,
                               String notFoundMessage,
                               boolean takeScreenshotOnFailure) {
        BlockPollingDeadline deadline = BlockPollingDeadline.fromSelenideConfiguration();
        List<CorePage> lastBlocks = List.of();

        while (deadline.tryNextAttempt()) {
            int attemptNumber = deadline.attempts();
            List<CorePage> blocks;

            try {
                blocks = new ArrayList<>(blocksSupplier.get());
                lastBlocks = blocks;
            } catch (StaleElementReferenceException | ElementNotFound error) {
                Allure.step("Попытка №" + attemptNumber +
                        " — список обновился до проверки");
                deadline.pauseBeforeNextAttempt();
                continue;
            }

            List<CorePage> currentBlocks = blocks;
            Attempt<T> attempt = Allure.step("Попытка №" + attemptNumber, step -> {
                try {
                    Attempt<T> result = operation.apply(currentBlocks);
                    step.name("Попытка №" + attemptNumber + " — " + result.description());
                    return result;
                } catch (StaleElementReferenceException | ElementNotFound error) {
                    step.name("Попытка №" + attemptNumber +
                            " — DOM обновился во время проверки");
                    return Attempt.again("DOM обновился во время проверки");
                }
            });

            if (attempt.value() != null) {
                return attempt.value();
            }
            deadline.pauseBeforeNextAttempt();
        }

        if (takeScreenshotOnFailure) {
            BrowserSteps.takeScreenshot();
        }

        throw new AssertionError(
                notFoundMessage +
                        "\nКоличество попыток: " + deadline.attempts() +
                        "\nTimeout: " + deadline.timeoutMs() + " мс" +
                        "\nРазмер блоков: " + lastBlocks.size() +
                        "\nСодержимое блоков: " + safelyDescribe(lastBlocks)
        );
    }

    private static void validateBlockNumber(int blockNumber) {
        if (blockNumber < 1) {
            throw new IllegalArgumentException(
                    "Индекс блока должен начинаться с 1, получено: " + blockNumber
            );
        }
    }

    private static String safelyDescribe(List<CorePage> blocks) {
        try {
            return blockListToString(blocks);
        } catch (RuntimeException | AssertionError error) {
            return "<не удалось получить описание: " +
                    error.getClass().getSimpleName() + ">";
        }
    }

    private enum BlockState {
        MATCHED,
        NOT_MATCHED,
        RETRY
    }

    private record Attempt<T>(T value, String description) {

        private static <T> Attempt<T> done(T value, String description) {
            return new Attempt<>(value, description);
        }

        private static <T> Attempt<T> again(String description) {
            return new Attempt<>(null, description);
        }
    }
}
