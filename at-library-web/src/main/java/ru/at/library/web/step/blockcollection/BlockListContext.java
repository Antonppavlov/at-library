package ru.at.library.web.step.blockcollection;

import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.WebScenario;

import java.util.List;

import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.getBlockListWithCheckingTheQuantity;

/**
 * Источник списка блоков: сохранённый снимок для обычных проверок
 * и координаты списка для повторного чтения DOM во время retry.
 */
class BlockListContext {

    private final List<CorePage> snapshot;
    private final String listName;
    private final String containerName;

    private BlockListContext(List<CorePage> snapshot, String listName, String containerName) {
        this.snapshot = snapshot;
        this.listName = listName;
        this.containerName = containerName;
    }

    static BlockListContext snapshot(String listName) {
        List<CorePage> blocks =
                getBlockListWithCheckingTheQuantity(listName, CustomCondition.Comparison.more, 0);
        return new BlockListContext(blocks, listName, null);
    }

    static BlockListContext snapshotInBlock(String blockName, String listName) {
        List<CorePage> blocks =
                getBlockListWithCheckingTheQuantity(blockName, listName, CustomCondition.Comparison.more, 0);
        return new BlockListContext(blocks, listName, blockName);
    }

    /**
     * Создаёт только описание источника. Сам список впервые получается уже
     * внутри polling-попытки, поэтому ошибка перерисовки не выйдет за deadline.
     */
    static BlockListContext live(String listName) {
        return new BlockListContext(List.of(), listName, null);
    }

    static BlockListContext liveInBlock(String blockName, String listName) {
        return new BlockListContext(List.of(), listName, blockName);
    }

    List<CorePage> getBlocks() {
        return snapshot;
    }

    List<CorePage> freshBlocks() {
        return loadBlocks(listName, containerName);
    }

    String describe() {
        return describe(listName, containerName);
    }

    static String describe(String listName, String containerName) {
        StringBuilder description = new StringBuilder()
                .append("Текущая страница: '")
                .append(WebScenario.getCurrentPage().getName())
                .append("'");

        if (containerName != null) {
            description.append("\nБлок-контейнер: '")
                    .append(containerName)
                    .append("'");
        }

        return description.append("\nСписок блоков: '")
                .append(listName)
                .append("'")
                .toString();
    }

    private static List<CorePage> loadBlocks(String listName, String containerName) {
        CorePage owner = containerName == null
                ? WebScenario.getCurrentPage()
                : WebScenario.getCurrentPage().getBlock(containerName);
        return owner.getBlocksList(listName);
    }
}
