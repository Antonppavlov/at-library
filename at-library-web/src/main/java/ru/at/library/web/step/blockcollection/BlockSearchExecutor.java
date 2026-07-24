package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.ElementNotFound;
import io.qameta.allure.Allure;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import ru.at.library.web.scenario.CorePage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.codeborne.selenide.CheckResult.Verdict.ACCEPT;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.blockListToString;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.scrollToElementCenter;

/**
 * Повторно получает список блоков и выполняет операцию до успеха
 * или до истечения общего таймаута Selenide.
 */
final class BlockSearchExecutor {

    private BlockSearchExecutor() {
    }

    static CorePage findInContext(BlockListContext context,
                                  String elementName,
                                  WebElementCondition condition,
                                  String notFoundMessage) {
        return findInContext(context, elementName, condition, null, notFoundMessage);
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
                notFoundMessage
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
                null,
                notFoundMessage
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
                notFoundMessage
        );
    }

    static List<CorePage> filterInContext(BlockListContext context,
                                          Predicate<CorePage> predicate,
                                          String notFoundMessage) {
        return filter(
                context::freshBlocks,
                predicate,
                null,
                notFoundMessage
        );
    }

    static List<CorePage> filterInSnapshot(List<CorePage> blocks,
                                           Predicate<CorePage> predicate,
                                           String notFoundMessage) {
        return filter(
                () -> blocks,
                predicate,
                null,
                notFoundMessage
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
                notFoundMessage
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
                null,
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
                null,
                notFoundMessage
        );
    }

    private static CorePage find(Supplier<List<CorePage>> blocksSupplier,
                                 String elementName,
                                 WebElementCondition condition,
                                 Consumer<CorePage> onMatched,
                                 String notFoundMessage) {
        return retry(
                blocksSupplier,
                blocks -> {
                    for (int index = 0; index < blocks.size(); index++) {
                        int blockNumber = index + 1;
                        CorePage block = blocks.get(index);
                        TargetCheck targetCheck = checkElement(
                                blockNumber,
                                blocks.size(),
                                "элемент '" + elementName + "'",
                                condition,
                                () -> block.getElement(elementName),
                                "Действие с найденным блоком №" + blockNumber,
                                onMatched == null
                                        ? null
                                        : element -> onMatched.accept(block)
                        );

                        if (targetCheck.state() == BlockState.RETRY) {
                            return Attempt.again(
                                    targetCheck.actualState() + " в блоке №" + blockNumber
                            );
                        }
                        if (targetCheck.state() == BlockState.MATCHED) {
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
                notFoundMessage
        );
    }

    private static List<CorePage> filter(Supplier<List<CorePage>> blocksSupplier,
                                         Predicate<CorePage> predicate,
                                         Consumer<List<CorePage>> onMatched,
                                         String notFoundMessage) {
        return retry(
                blocksSupplier,
                blocks -> {
                    List<CorePage> matchedBlocks = new ArrayList<>();

                    for (int index = 0; index < blocks.size(); index++) {
                        int blockNumber = index + 1;
                        CorePage block = blocks.get(index);
                        BlockState state = checkPredicate(
                                blockNumber,
                                blocks.size(),
                                "сложные условия",
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

                    if (runAction(
                            "Действие с найденными блоками: " + matchedBlocks.size(),
                            onMatched,
                            matchedBlocks
                    ) == BlockState.RETRY) {
                        return Attempt.again("DOM обновился при действии с найденными блоками");
                    }
                    return Attempt.done(
                            matchedBlocks,
                            "найдено блоков: " + matchedBlocks.size() + " из " + blocks.size()
                    );
                },
                notFoundMessage
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
                    TargetCheck targetCheck = checkElement(
                            blockNumber,
                            blocks.size(),
                            targetDescription,
                            condition,
                            () -> targetProvider.apply(block),
                            "Действие с " + targetDescription +
                                    " в блоке №" + blockNumber,
                            onMatched
                    );

                    if (targetCheck.state() == BlockState.MATCHED) {
                        return Attempt.done(
                                block,
                                "условие выполнено в блоке №" + blockNumber
                        );
                    }

                    return Attempt.again(targetCheck.state() == BlockState.RETRY
                            ? targetCheck.actualState() + " в блоке №" + blockNumber
                            : targetDescription + " пока не соответствует условию");
                },
                notFoundMessage
        );
    }

    private static TargetCheck checkElement(int blockNumber,
                                            int totalBlocks,
                                            String targetDescription,
                                            WebElementCondition condition,
                                            Supplier<SelenideElement> elementSupplier,
                                            String actionDescription,
                                            Consumer<SelenideElement> onMatched) {
        String stepTitle = "Блок №" + blockNumber + " из " + totalBlocks +
                " — " + targetDescription;

        return Allure.step(stepTitle, step -> {
            TargetCheck targetCheck = null;
            try {
                targetCheck = evaluateElement(elementSupplier, condition);
                if (targetCheck.matched()
                        && runAction(
                        actionDescription,
                        onMatched,
                        targetCheck.element()
                ) == BlockState.RETRY) {
                    targetCheck = TargetCheck.retry(
                            "DOM обновился во время действия"
                    );
                }
                BlockAllureReport.finishStep(
                        step,
                        stepTitle,
                        targetCheck.state().description,
                        condition.toString(),
                        targetCheck.actualState()
                );
                return targetCheck;
            } catch (RuntimeException | AssertionError error) {
                BlockAllureReport.finishStep(
                        step,
                        stepTitle,
                        "ОШИБКА",
                        condition.toString(),
                        targetCheck == null
                                ? "не удалось получить состояние элемента"
                                : targetCheck.actualState()
                );
                BlockAllureReport.addError(step, error);
                throw error;
            }
        });
    }

    static TargetCheck evaluateElement(Supplier<SelenideElement> elementSupplier,
                                       WebElementCondition condition) {
        return evaluateElement(elementSupplier, condition, true);
    }

    static TargetCheck evaluateElement(Supplier<SelenideElement> elementSupplier,
                                       WebElementCondition condition,
                                       boolean requiresExistingElement) {
        SelenideElement element;
        WebElement webElement;

        try {
            element = elementSupplier.get();
            webElement = element.toWebElement();
        } catch (ElementNotFound | NoSuchElementException error) {
            boolean matched = !requiresExistingElement
                    && condition.missingElementSatisfiesCondition();
            return matched
                    ? TargetCheck.matched(null, "элемент не существует")
                    : TargetCheck.notMatched(null, "элемент не существует");
        } catch (StaleElementReferenceException | IndexOutOfBoundsException error) {
            return TargetCheck.retry();
        }

        try {
            if (requiresExistingElement) {
                scrollToElementCenter(webElement);
            }
            CheckResult checkResult = condition.check(
                    WebDriverRunner.driver(),
                    webElement
            );
            BlockAllureReport.ElementState elementState =
                    BlockAllureReport.inspect(webElement);
            if (elementState.domUpdated()) {
                return TargetCheck.retry();
            }

            String actualState = describeActualState(checkResult, elementState);
            return checkResult.verdict() == ACCEPT
                    ? TargetCheck.matched(element, actualState)
                    : TargetCheck.notMatched(element, actualState);
        } catch (StaleElementReferenceException
                 | NoSuchElementException
                 | ElementNotFound error) {
            return TargetCheck.retry();
        }
    }

    private static BlockState checkPredicate(int blockNumber,
                                             int totalBlocks,
                                             String expectation,
                                             Supplier<Boolean> operation) {
        String stepTitle = "Блок №" + blockNumber + " из " + totalBlocks;
        return Allure.step(stepTitle, step -> {
            try {
                boolean matched = BlockAllureReport.withoutSelenideSteps(operation);
                BlockState state = matched
                        ? BlockState.MATCHED
                        : BlockState.NOT_MATCHED;
                BlockAllureReport.finishStep(
                        step,
                        stepTitle,
                        state.description,
                        expectation,
                        matched ? "условия выполнены" : "условия не выполнены"
                );
                return state;
            } catch (StaleElementReferenceException | NoSuchElementException | ElementNotFound error) {
                BlockAllureReport.finishStep(
                        step,
                        stepTitle,
                        BlockState.RETRY.description,
                        expectation,
                        "DOM обновился"
                );
                return BlockState.RETRY;
            }
        });
    }

    private static <T> BlockState runAction(String description,
                                            Consumer<T> action,
                                            T target) {
        if (action == null) {
            return BlockState.MATCHED;
        }
        return runAction(description, () -> action.accept(target));
    }

    private static BlockState runAction(String description,
                                        Runnable action) {
        return Allure.step(description, step -> {
            try {
                BlockAllureReport.withoutSelenideSteps(action);
                step.name(description + " — ВЫПОЛНЕНО");
                return BlockState.MATCHED;
            } catch (StaleElementReferenceException | NoSuchElementException | ElementNotFound error) {
                step.name(description + " — DOM ОБНОВИЛСЯ, повторяем попытку");
                return BlockState.RETRY;
            } catch (RuntimeException | AssertionError error) {
                step.name(description + " — ОШИБКА");
                BlockAllureReport.addError(step, error);
                throw error;
            }
        });
    }

    private static <T> T retry(Supplier<List<CorePage>> blocksSupplier,
                               Function<List<CorePage>, Attempt<T>> operation,
                               String notFoundMessage) {
        BlockPollingDeadline deadline = BlockPollingDeadline.fromSelenideConfiguration();
        List<CorePage> lastBlocks = List.of();

        while (deadline.tryNextAttempt()) {
            int attemptNumber = deadline.attempts();
            List<CorePage> blocks;

            try {
                blocks = BlockAllureReport.withoutSelenideSteps(
                        () -> new ArrayList<>(blocksSupplier.get())
                );
                lastBlocks = blocks;
            } catch (StaleElementReferenceException | NoSuchElementException | ElementNotFound error) {
                Allure.step("Попытка №" + attemptNumber +
                        " — список обновился до проверки");
                deadline.pauseBeforeNextAttempt();
                continue;
            }

            List<CorePage> currentBlocks = blocks;
            Attempt<T> attempt;
            try {
                attempt = Allure.step("Попытка №" + attemptNumber, step -> {
                    step.parameter("Блоков в списке", currentBlocks.size());
                    try {
                        Attempt<T> result = operation.apply(currentBlocks);
                        step.name("Попытка №" + attemptNumber + " — " + result.description());
                        return result;
                    } catch (StaleElementReferenceException | NoSuchElementException | ElementNotFound error) {
                        step.name("Попытка №" + attemptNumber +
                                " — DOM обновился во время проверки");
                        return Attempt.again("DOM обновился во время проверки");
                    }
                });
            } catch (RuntimeException | AssertionError error) {
                BlockAllureReport.attachFailureScreenshot();
                throw error;
            }

            if (attempt.value() != null) {
                return attempt.value();
            }
            deadline.pauseBeforeNextAttempt();
        }

        BlockAllureReport.attachFailureScreenshot();

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

    private static String describeActualState(CheckResult checkResult,
                                              BlockAllureReport.ElementState elementState) {
        Object actualValue = checkResult.actualValue();
        return actualValue == null
                ? elementState.description()
                : "значение условия: " + actualValue +
                "; состояние элемента: " + elementState.description();
    }

    private enum BlockState {
        MATCHED("СООТВЕТСТВУЕТ"),
        NOT_MATCHED("НЕ СООТВЕТСТВУЕТ"),
        RETRY("DOM обновился, повторяем попытку");

        private final String description;

        BlockState(String description) {
            this.description = description;
        }
    }

    record TargetCheck(BlockState state,
                       SelenideElement element,
                       String actualState) {

        private static TargetCheck matched(SelenideElement element, String actualState) {
            return new TargetCheck(BlockState.MATCHED, element, actualState);
        }

        private static TargetCheck notMatched(SelenideElement element, String actualState) {
            return new TargetCheck(BlockState.NOT_MATCHED, element, actualState);
        }

        private static TargetCheck retry() {
            return new TargetCheck(BlockState.RETRY, null, "DOM обновился");
        }

        private static TargetCheck retry(String actualState) {
            return new TargetCheck(BlockState.RETRY, null, actualState);
        }

        boolean matched() {
            return state == BlockState.MATCHED;
        }

        boolean needsRetry() {
            return state == BlockState.RETRY;
        }
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
