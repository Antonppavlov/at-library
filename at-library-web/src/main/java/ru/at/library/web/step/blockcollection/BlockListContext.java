package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.SelenideElement;
import io.cucumber.datatable.DataTable;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;

import java.util.List;
import java.util.function.Supplier;

import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.*;

/**
 * Вспомогательный класс для работы со списком блоков в шагах.
 * Инкапсулирует получение/ожидание списка блоков и типовые операции поиска.
 *
 * "Живой" контекст: фактический список блоков берётся из DOM при каждом обращении,
 * а не кэшируется единожды на момент создания контекста.
 */
class BlockListContext {

    private final Supplier<List<CorePage>> blocksSupplier;
    private final String listName;
    private final String containerName; // может быть null

    private BlockListContext(Supplier<List<CorePage>> blocksSupplier, String listName, String containerName) {
        this.blocksSupplier = blocksSupplier;
        this.listName = listName;
        this.containerName = containerName;
    }

    static BlockListContext fromList(String listName) {
        return new BlockListContext(
                () -> getBlockListWithCheckingTheQuantity(listName, CustomCondition.Comparison.more, 0),
                listName,
                null
        );
    }

    static BlockListContext fromBlock(String blockName, String listName) {
        return new BlockListContext(
                () -> getBlockListWithCheckingTheQuantity(blockName, listName, CustomCondition.Comparison.more, 0),
                listName,
                blockName
        );
    }

    List<CorePage> getBlocks() {
        return blocksSupplier.get();
    }

    String getListName() {
        return listName;
    }

    String getContainerName() {
        return containerName;
    }

    CorePage nthBlock(int oneBasedIndex) {
        List<CorePage> currentBlocks = getBlocks();
        int zeroBasedIndex = oneBasedIndex - 1;
        if (oneBasedIndex < 1 || zeroBasedIndex >= currentBlocks.size()) {
            throw new IllegalArgumentException(String.format(
                    "Индекс блока должен быть в диапазоне [1..%d], получено: %d",
                    currentBlocks.size(), oneBasedIndex));
        }
        return currentBlocks.get(zeroBasedIndex);
    }

    CorePage findByTextEquals(String elementName, String expectedText) {
        return findCorePageByTextInElement(getBlocks(), elementName, expectedText);
    }

    CorePage findByTextContains(String elementName, String expectedText) {
        return findCorePageByTextContainInElement(getBlocks(), elementName, expectedText);
    }

    CorePage findByRegExp(String elementName, String expectedRegExp) {
        return findCorePageByRegExpInElement(getBlocks(), elementName, expectedRegExp);
    }

    CorePage findByVisibleElement(String elementName) {
        return findCorePageByVisibleElement(getBlocks(), elementName);
    }

    List<CorePage> filterByConditions(DataTable conditionsTable) {
        return getBlockListWithComplexCondition(getBlocks(), conditionsTable);
    }

    SelenideElement element(CorePage block, String elementName) {
        return block.getElement(elementName);
    }
}
