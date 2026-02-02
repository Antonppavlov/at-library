package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.SelenideElement;
import io.cucumber.datatable.DataTable;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;

import java.util.List;

import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.*;

/**
 * Вспомогательный класс для работы со списком блоков в шагах.
 * Инкапсулирует получение/ожидание списка блоков и типовые операции поиска.
 *
 * ВАЖНО: контекст хранит снимок списка блоков на момент создания. Это повторяет
 * исходное (стабильное) поведение библиотеки и исключает неожиданные изменения
 * порядка/состава блоков во время выполнения сложных шагов.
 */
class BlockListContext {

    private final List<CorePage> blocks;
    private final String listName;
    private final String containerName; // может быть null

    private BlockListContext(List<CorePage> blocks, String listName, String containerName) {
        this.blocks = blocks;
        this.listName = listName;
        this.containerName = containerName;
    }

    static BlockListContext fromList(String listName) {
        List<CorePage> blocks =
                getBlockListWithCheckingTheQuantity(listName, CustomCondition.Comparison.more, 0);
        return new BlockListContext(blocks, listName, null);
    }

    static BlockListContext fromBlock(String blockName, String listName) {
        List<CorePage> blocks =
                getBlockListWithCheckingTheQuantity(blockName, listName, CustomCondition.Comparison.more, 0);
        return new BlockListContext(blocks, listName, blockName);
    }

    List<CorePage> getBlocks() {
        return blocks;
    }

    String getListName() {
        return listName;
    }

    String getContainerName() {
        return containerName;
    }

    /**
     * Возвращает блок по его порядковому номеру в списке.
     *
     * @param blockNumber номер блока, начиная с 1 (как его видит пользователь в шагах)
     * @return {@link CorePage} с указанным номером
     * @throws IllegalArgumentException если номер выходит за пределы списка
     */
    CorePage getBlockByNumber(int blockNumber) {
        int zeroBasedIndex = blockNumber - 1;
        if (blockNumber < 1 || zeroBasedIndex >= blocks.size()) {
            throw new IllegalArgumentException(String.format(
                    "Индекс блока должен быть в диапазоне [1..%d], получено: %d",
                    blocks.size(), blockNumber));
        }
        return blocks.get(zeroBasedIndex);
    }

    CorePage findByTextEquals(String elementName, String expectedText) {
        return findCorePageByTextInElement(blocks, elementName, expectedText);
    }

    CorePage findByTextContains(String elementName, String expectedText) {
        return findCorePageByTextContainInElement(blocks, elementName, expectedText);
    }

    CorePage findByRegExp(String elementName, String expectedRegExp) {
        return findCorePageByRegExpInElement(blocks, elementName, expectedRegExp);
    }

    CorePage findByVisibleElement(String elementName) {
        return findCorePageByVisibleElement(blocks, elementName);
    }

    List<CorePage> filterByConditions(DataTable conditionsTable) {
        return getBlockListWithComplexCondition(blocks, conditionsTable);
    }

    SelenideElement element(CorePage block, String elementName) {
        return block.getElement(elementName);
    }
}
