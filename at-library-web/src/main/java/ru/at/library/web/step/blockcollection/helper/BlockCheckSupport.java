package ru.at.library.web.step.blockcollection.helper;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import ru.at.library.web.entities.BlockListStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.IStepResult;

import java.util.List;
import java.util.function.Consumer;

/**
 * Общие для всех {@code BlocksCollection*Steps}-классов проверок примитивы: получение "снимка"
 * списка блоков и постановка результата каждого блока в отдельный Allure-подшаг.
 * <p>
 * Выделен из бывшего единого {@code BlocksCollectionCheckSteps} (1288 строк), чтобы конкретные
 * группы шагов ({@code BlocksCollectionListSteps}, {@code BlocksCollectionEachBlockSteps},
 * {@code BlocksCollectionAnyBlockSteps}, {@code BlocksCollectionCountSteps},
 * {@code BlocksCollectionSpecificBlockSteps}) могли переиспользовать одну и ту же логику, не
 * копируя её. Хелперы, используемые только одной группой шагов, остались приватными в
 * соответствующем классе — здесь только то, что реально нужно более чем одной группе.
 */
public final class BlockCheckSupport {

    private BlockCheckSupport() {
    }

    /**
     * Создаёт "снимок" списка блоков: на текущей странице, если {@code blockName} не задан
     * (null или пустая строка — короткая форма шага даёт "" через фиктивную regex-группу),
     * иначе — внутри блока-контейнера {@code blockName}.
     * <p>
     * Снимок берётся один раз и дальше не обновляется — используется там, где каждому блоку
     * достаточно собственного Selenide-ожидания ({@code should*}), а не повторного перечитывания
     * всего списка при перерисовке DOM. Для операций, которые должны переживать перерисовку
     * списка во время поиска, используется "живой" источник {@link BlockListContext#live}.
     */
    @Step("Создаём контекст списка блоков '{blockListName}'")
    public static BlockListContext createBlockListContext(String blockName, String blockListName) {
        return BlockListContext.snapshot(blockName, blockListName);
    }

    public static IStepResult forEachBlock(BlockListContext blockListContext,
                                           String key,
                                           String expectation,
                                           Consumer<CorePage> checker) {
        List<CorePage> blocks = blockListContext.getBlocks();
        forEachBlockWithReport(blocks, key, expectation, checker);
        return new BlockListStepResult(blocks, key);
    }

    public static void forEachBlockWithoutResult(BlockListContext blockListContext,
                                                 String elementName,
                                                 String expectation,
                                                 Consumer<CorePage> checker) {
        forEachBlockWithReport(
                blockListContext.getBlocks(),
                elementName,
                expectation,
                checker
        );
    }

    private static void forEachBlockWithReport(List<CorePage> blocks,
                                               String elementName,
                                               String expectation,
                                               Consumer<CorePage> checker) {
        for (int index = 0; index < blocks.size(); index++) {
            int blockNumber = index + 1;
            CorePage block = blocks.get(index);
            String stepTitle = "Блок №" + blockNumber + " из " + blocks.size() +
                    (elementName == null ? "" : " — элемент '" + elementName + "'");
            Allure.step(stepTitle, step -> {
                try {
                    BlockAllureReport.withoutSelenideSteps(() -> checker.accept(block));
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "ВЫПОЛНЕНО",
                            expectation,
                            describeBlockTargetState(block, elementName)
                    );
                } catch (RuntimeException | AssertionError error) {
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "НЕ ВЫПОЛНЕНО",
                            expectation,
                            describeBlockTargetState(block, elementName)
                    );
                    BlockAllureReport.addError(step, error);
                    BlockAllureReport.attachFailureScreenshot();
                    throw error;
                }
            });
        }
    }

    public static String describeBlockTargetState(CorePage block, String elementName) {
        try {
            SelenideElement target = elementName == null
                    ? block.getSelf()
                    : block.getElement(elementName);
            return BlockAllureReport.elementState(target);
        } catch (RuntimeException | AssertionError error) {
            return "состояние недоступно: " + error.getClass().getSimpleName();
        }
    }
}
