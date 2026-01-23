package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Point;
import org.testng.Assert;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.entities.BlockListStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.IStepResult;
import ru.at.library.web.scenario.WebScenario;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.utils.helpers.ScopedVariables.resolveVars;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.*;
import static ru.at.library.web.step.selenideelement.SelenideElementCheckSteps.inBounds;

public class BlocksCollectionCheckSteps {

    private static final Logger log = LogManager.getLogger(BlocksCollectionCheckSteps.class);

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    // Вспомогательные методы для сокращения дублирования

    /**
     * Создаёт контекст для списка блоков, объявленного на текущей странице (без родительского блока).
     * Используется во всех шагах вида "в списке блоков &lt;имя&gt; ...".
     */
    private BlockListContext createBlockListContextFromList(String blockListName) {
        return BlockListContext.fromList(blockListName);
    }

    /**
     * Создаёт контекст для списка блоков, который находится внутри родительского блока.
     * Используется во всех шагах вида "в блоке &lt;имя блока&gt; в списке блоков &lt;имя списка&gt; ...".
     */
    private BlockListContext createBlockListContextFromBlock(String blockName, String blockListName) {
        return BlockListContext.fromBlock(blockName, blockListName);
    }

    /**
     * Применяет переданную проверку ко всем блокам в {@link BlockListContext} и возвращает результат
     * с ключом (обычно это имя проверяемого элемента).
     */
    private IStepResult forEachBlock(BlockListContext blockListContext, String key, Consumer<CorePage> checker) {
        List<CorePage> blocks = blockListContext.getBlocks();
        for (CorePage block : blocks) {
            checker.accept(block);
        }
        return new BlockListStepResult(blocks, key);
    }

    /**
     * Удобный вариант forEach без формирования {@link IStepResult}, когда шаг ничего не возвращает наружу.
     */
    private void forEachBlock(BlockListContext blockListContext, Consumer<CorePage> checker) {
        for (CorePage block : blockListContext.getBlocks()) {
            checker.accept(block);
        }
    }

    /**
     * Helper: проверка, что РОВНО expectedCount блоков удовлетворяют предикату.
     * Возвращает {@link BlockListStepResult} с этими блоками.
     * <p>
     * Важно: сам предикат не должен кидать проверочные исключения наружу —
     * если нужна "долгая" проверка с ожиданием, внутри предиката следует
     * вызвать {@code should*} и перехватить {@link AssertionError},
     * возвращая {@code false} в случае неуспеха.
     */
    private IStepResult assertBlocksCountMatching(BlockListContext blockListContext,
                                                  int expectedCount,
                                                  String key,
                                                  Predicate<CorePage> predicate,
                                                  String failureHeader) {
        List<CorePage> matches = blockListContext.getBlocks().stream()
                .filter(predicate)
                .collect(Collectors.toList());

        Assert.assertEquals(matches.size(), expectedCount,
                failureHeader +
                        "\nОжидаемое количество блоков: " + expectedCount +
                        "\nАктуальное количество блоков: " + matches.size());

        return new BlockListStepResult(matches, key);
    }

    /**
     * Helper: проверка условия "в любом блоке" через функцию-поисковик.
     * Используется в шагах вида "в любом из блоков ...", чтобы переиспользовать
     * {@code findCorePageBy*} из {@link BlocksCollectionOtherMethod}.
     */
    private IStepResult checkAnyBlock(BlockListContext blockListContext,
                                      String key,
                                      Function<List<CorePage>, CorePage> finder) {
        CorePage block = finder.apply(blockListContext.getBlocks());
        return new BlockListStepResult(block, key);
    }

/**
 * Шаги-проверки для работы с коллекциями блоков (List<CorePage>),
 * построенные поверх "живых" списков блоков (BlocksCollection) и {@link BlockListContext}.
 *
 * Основные принципы:
 * <ul>
 *     <li>Все ожидания делаются через {@code should*}/{@code shouldHave} Selenide, что гарантирует использование
 *     стандартного таймаута {@code Configuration.timeout}, как и для одиночных {@code SelenideElement}.</li>
 *     <li>Для выборок "любой блок" / "в N блоках" логика вынесена в приватные helper-методы, а Cucumber-шаги
 *     являются тонкими обёртками над ними.</li>
 *     <li>Получение списка блоков всегда делается через {@link BlockListContext}, чтобы использовать единый
 *     механизм ожиданий размера коллекции.</li>
 * </ul>
 */
    /**
     * -----------------------------------------------------------------------------------------------------------------
     * ---------------------------------------------Проверки списка блоков----------------------------------------------
     * -----------------------------------------------------------------------------------------------------------------
     */


    @И("^список блоков \"([^\"]*)\" отображается на странице$")
    public IStepResult listBlockVisible(String blockListName) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        forEachBlock(blockListContext, corePage -> corePage.isAppeared());
        return new BlockListStepResult(blockListContext.getBlocks());
    }

    @И("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" отображается на странице$")
    public IStepResult listBlockVisible(String blockName, String blockListName) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        forEachBlock(blockListContext, corePage -> corePage.isAppeared());
        return new BlockListStepResult(blockListContext.getBlocks());
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" количество блоков (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)$")
    public IStepResult checkBlockListSize(String blockListName, String condition, int expectedCountBlock) {
        CustomCondition.Comparison comparison = CustomCondition.Comparison.fromString(condition);

        List<CorePage> blocksList = getBlockListWithCheckingTheQuantity(blockListName, comparison, expectedCountBlock);

        return new BlockListStepResult(blocksList);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" количество блоков (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)$")
    public IStepResult checkBlockListSize(String blockName, String blockListName, String condition, int expectedCountBlock) {
        CustomCondition.Comparison comparison = CustomCondition.Comparison.fromString(condition);

        List<CorePage> blocksList = getBlockListWithCheckingTheQuantity(blockName, blockListName, comparison, expectedCountBlock);

        return new BlockListStepResult(blocksList);
    }


    /**
     * ######################################################################################################################
     */

    /**
     * Строит детальное сообщение о несоответствии элементов ожиданиям, заданным в {@link DataTable}.
     * <p>
     * Для каждой строки таблицы ожидается формат:
     * | индекс блока | имя элемента | текстовое условие | ожидаемое значение/регулярка |
     */
    private String buildBlockListMatchesListMessage(BlockListContext blockListContext,
                                                    DataTable conditionsTable,
                                                    boolean useOneBasedIndexInMessage) {
        List<List<String>> conditionsRows = conditionsTable.asLists();
        List<CorePage> blocksList = blockListContext.getBlocks();
        String resultMessageTemplate = "В блоке %d элемент %s не соответствует условию: %s %s\\n%s\\n";
        StringBuilder resultMessage = new StringBuilder();

        for (List<String> conditionRow : conditionsRows) {
            int blockIndex = Integer.parseInt(conditionRow.get(0)) - 1;
            String elementName = conditionRow.get(1);
            String textCondition = conditionRow.get(2);
            String expectedText = resolveVars(getPropertyOrStringVariableOrValue(conditionRow.get(3)));

            SelenideElement element = blocksList.get(blockIndex).getElement(elementName);
            try {
                // Ждём выполнения текстового условия так же, как для одиночного SelenideElement
                element.shouldHave(getSelenideCondition(textCondition, expectedText));
            } catch (AssertionError e) {
                int indexForMessage = useOneBasedIndexInMessage ? blockIndex + 1 : blockIndex;
                resultMessage
                        .append(String.format(resultMessageTemplate,
                                indexForMessage,
                                elementName,
                                textCondition,
                                expectedText,
                                blocksList.get(blockIndex).getSelf().toString()))
                        .append("\\n");
            }
        }
        return resultMessage.toString();
    }

    @SuppressWarnings("deprecation")
    @И("^список блоков \"([^\"]*)\" соответствует списку$")
    public void blockListMatchesList(String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        String resultMessage = buildBlockListMatchesListMessage(blockListContext, conditionsTable, true);
        this.coreScenario.getAssertionHelper().hamcrestAssert(resultMessage, resultMessage, isEmptyString());
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" соответствует списку$")
    public void blockListMatchesList(String blockName, String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        String resultMessage = buildBlockListMatchesListMessage(blockListContext, conditionsTable, false);
        if (!resultMessage.isEmpty()) {
            throw new AssertionError(resultMessage);
        }
    }

    /**
     * ######################################################################################################################
     */

    private IStepResult checkBlockListRowsFormat(BlockListContext blockListContext, int elementsInRow) {
        List<CorePage> blocksList = blockListContext.getBlocks();

        int index = 0;
        int previousRowY = 0;
        while (index < blocksList.size()) {
            int currentRowY = blocksList.get(index).getSelf().getLocation().y;
            int previousElementX = blocksList.get(index).getSelf().getLocation().x;
            assertTrue(currentRowY > previousRowY, String.format("%d блок расположен в новой строке", index + 1));
            for (int i = 1; i < elementsInRow; ++i) {
                ++index;
                if (index == blocksList.size()) break;
                int currentElementX = blocksList.get(index).getSelf().getLocation().x;
                int currentElementY = blocksList.get(index).getSelf().getLocation().y;
                assertTrue(currentElementX > previousElementX, String.format("%d блок расположен правее %d блока", index + 1, index));
                assertEquals(currentRowY, currentElementY, String.format("%d блок расположен в одной строке с %d блоком", index + 1, index));
            }
            ++index;
            previousRowY = currentRowY;
        }
        return new BlockListStepResult(blocksList);
    }

    @SuppressWarnings("deprecation")
    @И("^список блоков \"([^\"]*)\" блоки расположены по (\\d+) в ряд$")
    public IStepResult checkBlockListRowsFormat(String blockListName, int elementsInRow) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkBlockListRowsFormat(blockListContext, elementsInRow);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" блоки расположены по (\\d+) в ряд$")
    public IStepResult checkBlockListRowsFormat(String blockName, String blockListName, int elementsInRow) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkBlockListRowsFormat(blockListContext, elementsInRow);
    }

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" (\\d+) блок содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkBlockListForBlockWithCss(String blockListName, int blockIndex, String cssName, String cssValue) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        block.getSelf().shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" (\\д+) блок содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkBlockListForBlockWithCss(String blockName, String blockListName, int blockIndex, String cssName, String cssValue) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        block.getSelf().shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(block);
    }

    /**
     * ######################################################################################################################
     */

    private void checkBlockListElementsInWidthOfElement(List<CorePage> blocksList, SelenideElement outerElement, String elementOuterName) {
        int index = 0;
        int elementLeftBound = outerElement.getLocation().x;
        int elementRightBound = elementLeftBound + outerElement.getSize().width;

        for (CorePage block : blocksList) {
            index++;
            int blockLeftBoundX = block.getSelf().getLocation().x;
            int blockRightBound = blockLeftBoundX + block.getSelf().getSize().width;

            assertTrue((blockLeftBoundX >= elementLeftBound) && (blockRightBound <= elementRightBound),
                    String.format("%d блок расположен не по ширене элемента " + elementOuterName, index));
        }
    }

    @SuppressWarnings("deprecation")
    @И("^список блоков \"([^\"]*)\" расположен по ширине элемента \"([^\"]*)\"$")
    public void checkBlockListElementsInWidthOfElement(String blockListName, String elementOuter) {
        List<CorePage> blocksList = createBlockListContextFromList(blockListName).getBlocks();
        SelenideElement outerElement = WebScenario.getCurrentPage().getElement(elementOuter);
        checkBlockListElementsInWidthOfElement(blocksList, outerElement, elementOuter);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" расположен по ширине элемента \"([^\"]*)\"$")
    public void checkBlockListElementsInWidthOfElement(String blockName, String blockListName, String elementOuter) {
        List<CorePage> blocksList = createBlockListContextFromBlock(blockName, blockListName).getBlocks();
        SelenideElement outerElement = WebScenario.getCurrentPage().getBlock(blockName).getElement(elementOuter);
        checkBlockListElementsInWidthOfElement(blocksList, outerElement, elementOuter);
    }

    /**
     * -----------------------------------------------В КАЖДОМ------------------------------------------------
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockListName, String elementVisible) {
        return forEachBlock(createBlockListContextFromList(blockListName), elementVisible,
                block -> block.getElement(elementVisible).shouldHave(Condition.visible));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockName, String blockListName, String elementVisible) {
        return forEachBlock(createBlockListContextFromBlock(blockName, blockListName), elementVisible,
                block -> block.getElement(elementVisible).shouldHave(Condition.visible));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не отображается на странице$")
    public IStepResult elementNotVisibleInBlockList(String blockListName, String elementHidden) {
        return forEachBlock(createBlockListContextFromList(blockListName), elementHidden,
                block -> block.getElement(elementHidden).shouldNot(Condition.visible));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не отображается на странице$")
    public IStepResult elementNotVisibleInBlockList(String blockName, String blockListName, String elementHidden) {
        return forEachBlock(createBlockListContextFromBlock(blockName, blockListName), elementHidden,
                block -> block.getElement(elementHidden).shouldNot(Condition.visible));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" является изображением и отображается на странице$")
    public void checkImageInBlockList(String blockListName, String elementImageLoaded) {
        forEachBlock(createBlockListContextFromList(blockListName),
                block -> block.getElement(elementImageLoaded)
                        .shouldHave(Condition.image)
                        .shouldHave(Condition.visible));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" является изображением и отображается на странице$")
    public void checkImageInBlockList(String blockName, String blockListName, String elementImageLoaded) {
        forEachBlock(createBlockListContextFromBlock(blockName, blockListName),
                block -> block.getElement(elementImageLoaded)
                        .shouldHave(Condition.image)
                        .shouldHave(Condition.visible));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockListName, String elementName, String regExp) {
        String resolvedRegExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);
        return forEachBlock(createBlockListContextFromList(blockListName), elementName,
                block -> shouldHaveTextMatches(block, elementName, resolvedRegExp));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockName, String blockListName, String elementName, String regExp) {
        String resolvedRegExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);
        return forEachBlock(createBlockListContextFromBlock(blockName, blockListName), elementName,
                block -> shouldHaveTextMatches(block, elementName, resolvedRegExp));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке в элементе \"([^\"]*)\" текст не равен \"([^\"]*)\"$")
    public IStepResult checkNotTextInBlockListMatches(String blockListName, String elementName, String regExp) {
        String resolvedRegExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);
        return forEachBlock(createBlockListContextFromList(blockListName), elementName,
                block -> block.getElement(elementName).shouldNot(Condition.and("Проверка что TextMatches элемента",
                        Condition.attribute("value", resolvedRegExp),
                        Condition.attribute("title", resolvedRegExp),
                        Condition.text(resolvedRegExp)
                )));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке в элементе \"([^\"]*)\" текст не равен \"([^\"]*)\"$")
    public IStepResult checkNotTextInBlockListMatches(String blockName, String blockListName, String elementName, String regExp) {
        String resolvedRegExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);
        return forEachBlock(createBlockListContextFromBlock(blockName, blockListName), elementName,
                block -> block.getElement(elementName).shouldNot(Condition.and("Проверка что TextMatches элемента",
                        Condition.attribute("value", resolvedRegExp),
                        Condition.attribute("title", resolvedRegExp),
                        Condition.text(resolvedRegExp)
                )));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkCssInBlockList(String blockListName, String elementName, String cssName, String cssValue) {
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        return forEachBlock(createBlockListContextFromList(blockListName), elementName,
                block -> methodCheckHasCssInBlockList(block, elementName, cssName, resolvedCssValue));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkCssInBlockList(String blockName, String blockListName, String elementName, String cssName, String cssValue) {
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        return forEachBlock(createBlockListContextFromBlock(blockName, blockListName), elementName,
                block -> methodCheckHasCssInBlockList(block, elementName, cssName, resolvedCssValue));
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkHasNotCssInBlockList(String blockListName, String elementName, String cssName, String cssValue) {
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        return forEachBlock(createBlockListContextFromList(blockListName), elementName,
                block -> methodCheckNotHasCssInBlockList(block, elementName, cssName, resolvedCssValue));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkHasNotCssInBlockList(String blockName, String blockListName, String elementName, String cssName, String cssValue) {
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        return forEachBlock(createBlockListContextFromBlock(blockName, blockListName), elementName,
                block -> methodCheckNotHasCssInBlockList(block, elementName, cssName, resolvedCssValue));
    }

    /**
     * ######################################################################################################################
     */

    /**
     * Проверка, что каждый блок списка удовлетворяет всем условиям из таблицы.
     * Для каждого блока и строки таблицы выполняется полноценное ожидание через {@code shouldHave}.
     */
    private IStepResult everyBlockInBlockListMatchesComplexCondition(BlockListContext blockListContext,
                                                                     DataTable conditionsTable) {
        List<CorePage> blocksList = blockListContext.getBlocks();
        List<List<String>> conditionsRows = conditionsTable.asLists();

        forEachBlock(blockListContext, block -> {
            for (List<String> conditionsRow : conditionsRows) {
                String elementName = conditionsRow.get(0);
                String textCondition = conditionsRow.get(1);
                String expectedText = resolveVars(getPropertyOrStringVariableOrValue(conditionsRow.get(2)));

                block.getElement(elementName).shouldHave(getSelenideCondition(textCondition, expectedText));
            }
        });

        return new BlockListStepResult(blocksList,
                conditionsRows.stream().map(conditionsRow -> conditionsRow.get(0)).collect(Collectors.toList()));
    }

    @И("^в списке блоков \"([^\"]*)\" каждый из блоков соответствует условиям$")
    public IStepResult everyBlockInBlockListMatchesComplexCondition(String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return everyBlockInBlockListMatchesComplexCondition(blockListContext, conditionsTable);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" каждый из блоков соответствует условиям$")
    public IStepResult everyBlockInBlockListMatchesComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return everyBlockInBlockListMatchesComplexCondition(blockListContext, conditionsTable);
    }

    /**
     * -----------------------------------------------В ЛЮБОМ ИЗ БЛОКОВ------------------------------------------------
     */

    /**
     * ######################################################################################################################
     */

    /**
     * Метод проверяет что в списке блоков есть блок, текст элемента(ов) которого соответствует условию conditionsTable
     *
     * @param blockListContext Название списка блоков
     * @param conditionsTable  Список проверяемых условий в блоке
     *                         пример:
     *                         |<Название элемента 1>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     *                         ...
     *                         |<Название элемента N>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     */
    private IStepResult checkBlockListForComplexCondition(BlockListContext blockListContext,
                                                          DataTable conditionsTable) {
        List<CorePage> resultList = blockListContext.filterByConditions(conditionsTable);

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }

    @И("^в списке блоков \"([^\"]*)\" любой из блоков соответствует условиям$")
    public IStepResult checkBlockListForComplexCondition(String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkBlockListForComplexCondition(blockListContext, conditionsTable);
    }

    /**
     * Метод проверяет что в списке блоков есть блок, текст элемента(ов) которого соответствует условию conditionsTable
     *
     * @param blockListName   Название списка блоков
     * @param conditionsTable Список проверяемых условий в блоке
     *                        пример:
     *                        |<Название элемента 1>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     *                        ...
     *                        |<Название элемента N>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     */
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" любой из блоков соответствует условиям$")
    public IStepResult checkBlockListForComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkBlockListForComplexCondition(blockListContext, conditionsTable);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст равен \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkAnyBlock(blockListContext, elementName,
                blocks -> findCorePageByTextInElement(blocks, elementName, resolvedExpectedText));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст равен \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkAnyBlock(blockListContext, elementName,
                blocks -> findCorePageByTextInElement(blocks, elementName, resolvedExpectedText));
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkByRegExpInElementInAnyBlock(String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkAnyBlock(blockListContext, elementName,
                blocks -> findCorePageByRegExpInElement(blocks, elementName, resolvedExpectedText));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkByRegExpInElementInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkAnyBlock(blockListContext, elementName,
                blocks -> findCorePageByRegExpInElement(blocks, elementName, resolvedExpectedText));
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит")
    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    public IStepResult checkContainTextInAnyBlock(String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkAnyBlock(blockListContext, elementName,
                blocks -> findCorePageByTextContainInElement(blocks, elementName, resolvedExpectedText));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков \"([^\"]*)\" в элементе \"([^\"]*)\" текст содержит")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков \"([^\"]*)\" в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    public IStepResult checkContainTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkAnyBlock(blockListContext, elementName,
                blocks -> findCorePageByTextContainInElement(blocks, elementName, resolvedExpectedText));
    }

    /**
     * -----------------------------------------------В КАКОМ-ТО КОЛИЧЕСТВЕ------------------------------------------------
     */

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockListName, int blockNumber, String elementName, String regExp) {
        String resolvedRegExp = getPropertyOrStringVariableOrValue(regExp);
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        String failureHeader = "Условия поиска:" +
                "\nЭлемент '" + elementName + "' содержит текст в формате : '" + regExp + "'";

        return assertBlocksCountMatching(blockListContext, blockNumber, elementName,
                block -> {
                    try {
                        shouldHaveTextMatches(block, elementName, resolvedRegExp);
                        return true;
                    } catch (AssertionError e) {
                        return false;
                    }
                },
                failureHeader);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockName, String blockListName, int blockNumber, String elementName, String regExp) {
        String resolvedRegExp = getPropertyOrStringVariableOrValue(regExp);
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        String failureHeader = "Условия поиска:" +
                "\nЭлемент '" + elementName + "' содержит текст в формате : '" + regExp + "'";

        return assertBlocksCountMatching(blockListContext, blockNumber, elementName,
                block -> {
                    try {
                        shouldHaveTextMatches(block, elementName, resolvedRegExp);
                        return true;
                    } catch (AssertionError e) {
                        return false;
                    }
                },
                failureHeader);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockListName, int blockNumber, String elementName) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        String failureHeader = "Условия поиска:" +
                "\nЭлемент '" + elementName + "' отображается в блоке";

        return assertBlocksCountMatching(blockListContext, blockNumber, elementName,
                block -> {
                    try {
                        block.getElement(elementName).shouldBe(Condition.visible);
                        return true;
                    } catch (AssertionError e) {
                        return false;
                    }
                },
                failureHeader);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockName, String blockListName, int blockNumber, String elementName) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        String failureHeader = "Условия поиска:" +
                "\nЭлемент '" + elementName + "' отображается в блоке";

        return assertBlocksCountMatching(blockListContext, blockNumber, elementName,
                block -> {
                    try {
                        block.getElement(elementName).shouldBe(Condition.visible);
                        return true;
                    } catch (AssertionError e) {
                        return false;
                    }
                },
                failureHeader);
    }


    /**
     * -----------------------------------------------В КОНКРЕТНОМ------------------------------------------------
     */

    // Helpers for "конкретный блок, где текст ..." and "в любом блоке где текст ..." patterns
    private IStepResult elementDisplayedInBlockWhereTextEquals(BlockListContext blockListContext,
                                                               String elementNameSearch,
                                                               String expectedTextSearch,
                                                               String expectedElementVisible) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage corePageByTextInElement =
                findCorePageByTextInElement(blockListContext.getBlocks(), elementNameSearch, resolvedExpectedText);

        corePageByTextInElement.getElement(elementNameSearch).shouldBe(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    private IStepResult elementNotDisplayedInBlockWhereTextEquals(BlockListContext blockListContext,
                                                                  String elementNameSearch,
                                                                  String expectedTextSearch,
                                                                  String expectedElementVisible) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage corePageByTextInElement =
                findCorePageByTextInElement(blockListContext.getBlocks(), elementNameSearch, resolvedExpectedText);

        corePageByTextInElement.getElement(elementNameSearch).shouldNot(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    private IStepResult checkTextInAnyBlockMatches(BlockListContext blockListContext,
                                                   String elementNameSearch,
                                                   String expectedTextSearch,
                                                   String expectedElementVisible) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage corePageByTextInElement =
                findCorePageByRegExpInElement(blockListContext.getBlocks(), elementNameSearch, resolvedExpectedText);

        corePageByTextInElement.getElement(elementNameSearch).shouldBe(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    private IStepResult checkTextInAnyBlock(BlockListContext blockListContext,
                                            String elementNameSearch,
                                            String expectedTextSearch,
                                            String elementNameFind,
                                            String expectedTextFind) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedExpectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        CorePage corePageByTextInElement =
                findCorePageByTextInElement(blockListContext.getBlocks(), elementNameSearch, resolvedExpectedText);

        shouldHaveTextMatches(corePageByTextInElement, elementNameFind, resolvedExpectedTextFind);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    private IStepResult checkTextInAnyBlockMatches1(BlockListContext blockListContext,
                                                    String elementNameSearch,
                                                    String expectedTextSearch,
                                                    String elementNameFind,
                                                    String expectedTextFind) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedExpectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        CorePage corePageByTextInElement =
                findCorePageByRegExpInElement(blockListContext.getBlocks(), elementNameSearch, resolvedExpectedText);

        SelenideElement element = corePageByTextInElement.getElement(elementNameFind);
        element.shouldHave(Condition.matchText(resolvedExpectedTextFind), Duration.ZERO);

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    private IStepResult checkCssInAnyBlock(BlockListContext blockListContext,
                                           String elementNameSearch,
                                           String expectedTextSearch,
                                           String elementNameFind,
                                           String cssName,
                                           String cssValue) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage block =
                findCorePageByTextInElement(blockListContext.getBlocks(), elementNameSearch, resolvedExpectedText);

        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = block.getElement(elementNameFind);
        element.shouldHave(Condition.cssValue(resolvedCssName, resolvedCssValue));
        return new BlockListStepResult(block, elementNameSearch, elementNameFind);
    }

    private IStepResult checkBlockWithTextInElementInBounds(BlockListContext blockListContext,
                                                            String elementNameSearch,
                                                            String expectedTextSearch,
                                                            String boundsCondition) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage corePageByTextInElement =
                findCorePageByTextInElement(blockListContext.getBlocks(), elementNameSearch, resolvedExpectedText);

        inBounds(corePageByTextInElement.getSelf(), boundsCondition);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch);
    }

    private IStepResult saveElementTextToVarInBlockListWhereTextEquals(BlockListContext blockListContext,
                                                                       String elementToCheckText,
                                                                       String expectedText,
                                                                       String elementToSaveText,
                                                                       String varName) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage corePageByTextInElement =
                findCorePageByTextInElement(blockListContext.getBlocks(), elementToCheckText, resolvedExpectedText);

        SelenideElement element = corePageByTextInElement.getElement(elementToSaveText);
        CoreScenario.getInstance().getEnvironment().setVar(varName, element.getText());
        return new BlockListStepResult(corePageByTextInElement, elementToCheckText, elementToSaveText);
    }

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult elementDisplayedInBlockWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return elementDisplayedInBlockWhereTextEquals(blockListContext, elementNameSearch, expectedTextSearch, expectedElementVisible);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult elementDisplayedInBlockWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return elementDisplayedInBlockWhereTextEquals(blockListContext, elementNameSearch, expectedTextSearch, expectedElementVisible);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" не отображается$")
    public IStepResult elementNotDisplayedInBlockWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return elementNotDisplayedInBlockWhereTextEquals(blockListContext, elementNameSearch, expectedTextSearch, expectedElementVisible);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" не отображается$")
    public IStepResult elementNotDisplayedInBlockWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return elementNotDisplayedInBlockWhereTextEquals(blockListContext, elementNameSearch, expectedTextSearch, expectedElementVisible);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult checkTextInAnyBlockMatches(String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkTextInAnyBlockMatches(blockListContext, elementNameSearch, expectedTextSearch, expectedElementVisible);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult checkTextInAnyBlockMatches(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkTextInAnyBlockMatches(blockListContext, elementNameSearch, expectedTextSearch, expectedElementVisible);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkTextInAnyBlock(blockListContext, elementNameSearch, expectedTextSearch, elementNameFind, expectedTextFind);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkTextInAnyBlock(blockListContext, elementNameSearch, expectedTextSearch, elementNameFind, expectedTextFind);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlockMatches1(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkTextInAnyBlockMatches1(blockListContext, elementNameSearch, expectedTextSearch, elementNameFind, expectedTextFind);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlockMatches1(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkTextInAnyBlockMatches1(blockListContext, elementNameSearch, expectedTextSearch, elementNameFind, expectedTextFind);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"")
    public IStepResult checkCssInAnyBlock(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String cssName, String cssValue) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkCssInAnyBlock(blockListContext, elementNameSearch, expectedTextSearch, elementNameFind, cssName, cssValue);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"")
    public IStepResult checkCssInAnyBlock(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String cssName, String cssValue) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkCssInAnyBlock(blockListContext, elementNameSearch, expectedTextSearch, elementNameFind, cssName, cssValue);
    }

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" блок расположен (в|вне) видимой части браузера")
    public IStepResult checkBlockWithTextInElementInBounds(String blockListName, String elementNameSearch, String expectedTextSearch, String boundsCondition) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return checkBlockWithTextInElementInBounds(blockListContext, elementNameSearch, expectedTextSearch, boundsCondition);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" блок расположен (в|вне) видимой части браузера")
    public IStepResult checkBlockWithTextInElementInBounds(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String boundsCondition) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return checkBlockWithTextInElementInBounds(blockListContext, elementNameSearch, expectedTextSearch, boundsCondition);
    }

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" координаты (\\d+) блока соответствуют: x=(\\d+); y=(\\d+)$")
    public IStepResult checkBlockListItemCoordinates(String blockListName, int blockIndex, int x, int y) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);
        Point actualCoordinates = block.getSelf().getLocation();
        Point expectedCoordinates = new Point(x, y);

        this.coreScenario.getAssertionHelper().hamcrestAssert(
                String.format("Координаты %d блока списка блоков %s не соответстуют ожидаемым\nФактические координты: x=%d; y=%d\nОжидаемые координаты: x=%d; y=%d",
                        blockIndex, blockListName, actualCoordinates.x, actualCoordinates.y, expectedCoordinates.x, expectedCoordinates.y),
                actualCoordinates,
                is(equalTo(expectedCoordinates))
        );
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" координаты (\\d+) блока соответствуют: x=(\\d+); y=(\\d+)$")
    public IStepResult checkBlockListItemCoordinates(String blockName, String blockListName, int blockIndex, int x, int y) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);
        Point actualCoordinates = block.getSelf().getLocation();
        Point expectedCoordinates = new Point(x, y);

        this.coreScenario.getAssertionHelper().hamcrestAssert(
                String.format("Координаты %d блока списка блоков %s не соответстуют ожидаемым\nФактические координты: x=%d; y=%d\nОжидаемые координаты: x=%d; y=%d",
                        blockIndex, blockListName, actualCoordinates.x, actualCoordinates.y, expectedCoordinates.x, expectedCoordinates.y),
                actualCoordinates,
                is(equalTo(expectedCoordinates))
        );
        return new BlockListStepResult(block);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextForNthBlockFromBlockList(String blockListName, int blockIndex, String elementName, String varName) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);
        SelenideElement element = block.getElement(elementName);

        this.coreScenario.getEnvironment().setVar(varName, element.shouldBe(Condition.visible).getText());

        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextForNthBlockFromBlockList(String blockName, String blockListName, int blockIndex, String elementName, String varName) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);
        SelenideElement element = block.getElement(elementName);

        this.coreScenario.getEnvironment().setVar(varName, element.shouldBe(Condition.visible).getText());

        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListMatchesText(String blockListName, int blockIndex, String elementName, String expectedText) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        shouldHaveTextMatches(block, elementName, resolvedExpectedText);
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListMatchesText(String blockName, String blockListName, int blockIndex, String elementName, String expectedText) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        shouldHaveTextMatches(block, elementName, resolvedExpectedText);
        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForText(String blockListName, int blockIndex, String elementName, String conditionString, String expectedText) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);
        WebElementCondition condition;
        switch (conditionString) {
            case "равен":
                condition = Condition.exactText(resolvedExpectedText);
                break;
            case "содержит":
                condition = Condition.text(resolvedExpectedText);
                break;
            default:
                throw new IllegalArgumentException("Неизвестное условие: " + conditionString);
        }

        block.getElement(elementName).should(condition);
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForText(String blockName, String blockListName, int blockIndex, String elementName, String conditionString, String expectedText) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);
        WebElementCondition condition;
        switch (conditionString) {
            case "равен":
                condition = Condition.exactText(resolvedExpectedText);
                break;
            case "содержит":
                condition = Condition.text(resolvedExpectedText);
                break;
            default:
                throw new IllegalArgumentException("Неизвестное условие: " + conditionString);
        }

        block.getElement(elementName).should(condition);
        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForCss(String blockListName, int blockIndex, String elementName, String cssName, String cssValue) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.cssValue(resolvedCssName, resolvedCssValue));
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForCss(String blockName, String blockListName, int blockIndex, String elementName, String cssName, String cssValue) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.cssValue(resolvedCssName, resolvedCssValue));
        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForAttribute(String blockListName, int blockIndex, String elementName, String attributeName, String attributeValue) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        String resolvedAttributeName = OtherSteps.getPropertyOrStringVariableOrValue(attributeName);
        String resolvedAttributeValue = OtherSteps.getPropertyOrStringVariableOrValue(attributeValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.attributeMatching(resolvedAttributeName, resolvedAttributeValue));
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForAttribute(String blockName, String blockListName, int blockIndex, String elementName, String attributeName, String attributeValue) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        String resolvedAttributeName = OtherSteps.getPropertyOrStringVariableOrValue(attributeName);
        String resolvedAttributeValue = OtherSteps.getPropertyOrStringVariableOrValue(attributeValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.attributeMatching(resolvedAttributeName, resolvedAttributeValue));
        return new BlockListStepResult(block, elementName);
    }


    /**
     * ######################################################################################################################
     */


    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" (\\d+) блок расположен (в|вне) видимой части браузера$")
    public IStepResult checkBlockListItemInBounds(String blockListName, int blockIndex, String boundsCondition) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        inBounds(block.getSelf(), boundsCondition);
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" (\\д+) блок расположен (в|вне) видимой части браузера$")
    public IStepResult checkBlockListItemInBounds(String blockName, String blockListName, int blockIndex, String boundsCondition) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        CorePage block = blockListContext.nthBlock(blockIndex);

        inBounds(block.getSelf(), boundsCondition);
        return new BlockListStepResult(block);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено сохранение текста элемента \"([^\"]*)\" в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextToVarInBlockListWhereTextEquals(String blockListName, String elementToCheckText, String expectedText, String elementToSaveText, String varName) {
        BlockListContext blockListContext = createBlockListContextFromList(blockListName);
        return saveElementTextToVarInBlockListWhereTextEquals(blockListContext, elementToCheckText, expectedText, elementToSaveText, varName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено сохранение текста элемента \"([^\"]*)\" в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextToVarInBlockListWhereTextEquals(String blockName, String blockListName, String elementToCheckText, String expectedText, String elementToSaveText, String varName) {
        BlockListContext blockListContext = createBlockListContextFromBlock(blockName, blockListName);
        return saveElementTextToVarInBlockListWhereTextEquals(blockListContext, elementToCheckText, expectedText, elementToSaveText, varName);
    }

    /**
     * ######################################################################################################################
     */

}
