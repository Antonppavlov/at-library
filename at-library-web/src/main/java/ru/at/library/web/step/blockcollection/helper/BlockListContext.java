package ru.at.library.web.step.blockcollection.helper;

import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.WebScenario;

import java.util.List;

import static ru.at.library.web.step.blockcollection.helper.BlocksCollectionOtherMethod.getBlockListWithCheckingTheQuantity;

/**
 * Источник списка блоков: сохранённый снимок для обычных проверок
 * и координаты списка для повторного чтения DOM во время retry.
 */
public class BlockListContext {

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

    /**
     * Снимок списка блоков на текущей странице, если {@code blockName} не задан
     * (null или пустая строка — короткая форма шага "в блоке ..." даёт "" через
     * фиктивную regex-группу), иначе — снимок внутри блока-контейнера {@code blockName}.
     */
    public static BlockListContext snapshot(String blockName, String listName) {
        if (isAbsent(blockName)) {
            return snapshot(listName);
        }
        List<CorePage> blocks =
                getBlockListWithCheckingTheQuantity(blockName, listName, CustomCondition.Comparison.more, 0);
        return new BlockListContext(blocks, listName, blockName);
    }

    /**
     * Создаёт только описание источника. Сам список впервые получается уже
     * внутри polling-попытки, поэтому ошибка перерисовки не выйдет за deadline.
     */
    public static BlockListContext live(String listName) {
        return new BlockListContext(List.of(), listName, null);
    }

    /**
     * "Живой" источник списка блоков на текущей странице, если {@code blockName} не задан
     * (null или пустая строка), иначе — внутри блока-контейнера {@code blockName}.
     */
    public static BlockListContext live(String blockName, String listName) {
        return isAbsent(blockName)
                ? live(listName)
                : new BlockListContext(List.of(), listName, blockName);
    }

    /**
     * Единственное место, где решается, задано ли имя блока-контейнера
     * (короткая форма шага "в блоке ..." даёт пустую строку через фиктивную regex-группу).
     * Используется также напрямую из step-классов пакета, чтобы не дублировать эту проверку.
     */
    public static boolean isAbsent(String blockName) {
        return blockName == null || blockName.isEmpty();
    }

    public List<CorePage> getBlocks() {
        return snapshot;
    }

    List<CorePage> freshBlocks() {
        CorePage owner = containerName == null
                ? WebScenario.getCurrentPage()
                : WebScenario.getCurrentPage().getBlock(containerName);
        return owner.getBlocksList(listName);
    }

    public String describe() {
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
}
