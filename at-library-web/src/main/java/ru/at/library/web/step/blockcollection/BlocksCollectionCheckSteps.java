package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.extern.log4j.Log4j2;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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

@Log4j2
public class BlocksCollectionCheckSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

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
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        WebElementCondition condition = Condition.cssValue(resolvedCssName, resolvedCssValue);
        CorePage block = waitUntilBlockByNumberSelfMeetsCondition(
                BlockListContext.live(blockListName),
                blockIndex,
                condition
        );
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" (\\d+) блок содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkBlockListForBlockWithCss(String blockName, String blockListName, int blockIndex, String cssName, String cssValue) {
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        WebElementCondition condition = Condition.cssValue(resolvedCssName, resolvedCssValue);
        CorePage block = waitUntilBlockByNumberSelfMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex,
                condition
        );
        return new BlockListStepResult(block);
    }



    /**
     * ######################################################################################################################
     */

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
     * @param blockListName Название списка блоков
     * @param conditionsTable  Список проверяемых условий в блоке
     *                         пример:
     *                         |<Название элемента 1>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     *                         ...
     *                         |<Название элемента N>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     */

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

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.live(blockListName),
                elementName,
                BlockConditions.textEquals(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст равен \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                elementName,
                BlockConditions.textEquals(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkByRegExpInElementInAnyBlock(String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.live(blockListName),
                elementName,
                BlockConditions.textMatches(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkByRegExpInElementInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                elementName,
                BlockConditions.textMatches(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }



    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    public IStepResult checkContainTextInAnyBlock(String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.live(blockListName),
                elementName,
                BlockConditions.textContains(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }

    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    public IStepResult checkContainTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                elementName,
                BlockConditions.textContains(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
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
        Point expectedCoordinates = new Point(x, y);

        CorePage block = waitForBlockByNumber(
                BlockListContext.live(blockListName),
                blockIndex
        );
        Point actualCoordinates = block.getSelf().getLocation();
        this.coreScenario.getAssertionHelper().hamcrestAssert(
                String.format("Координаты %d блока списка блоков %s не соответствуют ожидаемым\n" +
                                "Фактические координаты: x=%d; y=%d\n" +
                                "Ожидаемые координаты: x=%d; y=%d",
                        blockIndex, blockListName,
                        actualCoordinates.x, actualCoordinates.y,
                        expectedCoordinates.x, expectedCoordinates.y),
                actualCoordinates,
                is(equalTo(expectedCoordinates))
        );
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" координаты (\\d+) блока соответствуют: x=(\\d+); y=(\\d+)$")
    public IStepResult checkBlockListItemCoordinates(String blockName, String blockListName, int blockIndex, int x, int y) {
        Point expectedCoordinates = new Point(x, y);

        CorePage block = waitForBlockByNumber(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex
        );
        Point actualCoordinates = block.getSelf().getLocation();
        this.coreScenario.getAssertionHelper().hamcrestAssert(
                String.format("Координаты %d блока списка блоков %s не соответствуют ожидаемым\n" +
                                "Фактические координаты: x=%d; y=%d\n" +
                                "Ожидаемые координаты: x=%d; y=%d",
                        blockIndex, blockListName,
                        actualCoordinates.x, actualCoordinates.y,
                        expectedCoordinates.x, expectedCoordinates.y),
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
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.live(blockListName),
                blockIndex,
                elementName,
                Condition.visible,
                element -> this.coreScenario.getEnvironment().setVar(varName, element.getText())
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextForNthBlockFromBlockList(String blockName, String blockListName, int blockIndex, String elementName, String varName) {
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex,
                elementName,
                Condition.visible,
                element -> this.coreScenario.getEnvironment().setVar(varName, element.getText())
        );
        return new BlockListStepResult(block, elementName);
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListMatchesText(String blockListName, int blockIndex, String elementName, String expectedText) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilTextMatchesInBlockByNumber(
                BlockListContext.live(blockListName),
                blockIndex,
                elementName,
                resolvedExpectedText
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListMatchesText(String blockName, String blockListName, int blockIndex, String elementName, String expectedText) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilTextMatchesInBlockByNumber(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex,
                elementName,
                resolvedExpectedText
        );
        return new BlockListStepResult(block, elementName);
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForText(String blockListName, int blockIndex, String elementName, String conditionString, String expectedText) {
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

        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.live(blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForText(String blockName, String blockListName, int blockIndex, String elementName, String conditionString, String expectedText) {
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

        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForCss(String blockListName, int blockIndex, String elementName, String cssName, String cssValue) {
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        WebElementCondition condition = Condition.cssValue(resolvedCssName, resolvedCssValue);
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.live(blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForCss(String blockName, String blockListName, int blockIndex, String elementName, String cssName, String cssValue) {
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        WebElementCondition condition = Condition.cssValue(resolvedCssName, resolvedCssValue);
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForAttribute(String blockListName, int blockIndex, String elementName, String attributeName, String attributeValue) {
        String resolvedAttributeName = OtherSteps.getPropertyOrStringVariableOrValue(attributeName);
        String resolvedAttributeValue = OtherSteps.getPropertyOrStringVariableOrValue(attributeValue);

        WebElementCondition condition = Condition.attributeMatching(resolvedAttributeName, resolvedAttributeValue);
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.live(blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForAttribute(String blockName, String blockListName, int blockIndex, String elementName, String attributeName, String attributeValue) {
        String resolvedAttributeName = OtherSteps.getPropertyOrStringVariableOrValue(attributeName);
        String resolvedAttributeValue = OtherSteps.getPropertyOrStringVariableOrValue(attributeValue);

        WebElementCondition condition = Condition.attributeMatching(resolvedAttributeName, resolvedAttributeValue);
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }




    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]+)\" (\\d+) блок расположен (в|вне) видимой части браузера$")
    public IStepResult checkBlockListItemInBounds(String blockListName, int blockIndex, String boundsCondition) {
        CorePage block = waitForBlockByNumber(
                BlockListContext.live(blockListName),
                blockIndex
        );
        inBounds(block.getSelf(), boundsCondition);
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]+)\" в списке блоков \"([^\"]+)\" (\\d+) блок расположен (в|вне) видимой части браузера$")
    public IStepResult checkBlockListItemInBounds(String blockName, String blockListName, int blockIndex, String boundsCondition) {
        CorePage block = waitForBlockByNumber(
                BlockListContext.liveInBlock(blockName, blockListName),
                blockIndex
        );
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

    // Вспомогательные методы для сокращения дублирования

    /**
     * Создаёт контекст для списка блоков, объявленного на текущей странице (без родительского блока).
     * Используется во всех шагах вида "в списке блоков <имя> ...".
     */
    @Step("Создаём контекст списка блоков '{blockListName}' на текущей странице")
    private BlockListContext createBlockListContextFromList(String blockListName) {
        return BlockListContext.snapshot(blockListName);
    }

    /**
     * Создаёт контекст для списка блоков, который находится внутри родительского блока.
     * Используется во всех шагах вида "в блоке <имя блока> в списке блоков <имя списка> ...".
     */
    @Step("Создаём контекст списка блоков '{blockListName}' внутри блока-контейнера '{blockName}'")
    private BlockListContext createBlockListContextFromBlock(String blockName, String blockListName) {
        return BlockListContext.snapshotInBlock(blockName, blockListName);
    }

    /**
     * Применяет переданную проверку ко всем блокам в {@link BlockListContext} и возвращает результат
     * с ключом (обычно это имя проверяемого элемента).
     */
    @Step("Выполняем проверку для каждого блока списка, результирующий ключ '{key}'")
    private IStepResult forEachBlock(BlockListContext blockListContext, String key, Consumer<CorePage> checker) {
        List<CorePage> blocks = blockListContext.getBlocks();
        forEachBlockWithReport(blocks, checker);
        return new BlockListStepResult(blocks, key);
    }

    /**
     * Удобный вариант forEach без формирования {@link IStepResult}, когда шаг ничего не возвращает наружу.
     */
    @Step("Выполняем действие для каждого блока списка без формирования результата")
    private void forEachBlock(BlockListContext blockListContext, Consumer<CorePage> checker) {
        forEachBlockWithReport(blockListContext.getBlocks(), checker);
    }

    private void forEachBlockWithReport(List<CorePage> blocks, Consumer<CorePage> checker) {
        for (int index = 0; index < blocks.size(); index++) {
            int blockNumber = index + 1;
            CorePage block = blocks.get(index);
            Allure.step(
                    "Блок №" + blockNumber + " из " + blocks.size(),
                    () -> checker.accept(block)
            );
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
    @Step("Проверяем, что количество блоков, удовлетворяющих условию, равно {expectedCount}")
    private IStepResult assertBlocksCountMatching(BlockListContext blockListContext,
                                                  int expectedCount,
                                                  String key,
                                                  Predicate<CorePage> predicate,
                                                  String failureHeader) {
        List<CorePage> blocks = blockListContext.getBlocks();
        List<CorePage> matches = Allure.step("Сопоставляем блоки с условием", step -> {
            List<CorePage> result = new ArrayList<>();
            for (CorePage block : blocks) {
                if (predicate.test(block)) {
                    result.add(block);
                }
            }
            step.name("Условию соответствует блоков: " + result.size() + " из " + blocks.size());
            return result;
        });

        String messageOnFailure = blockListContext.describe() +
                "\n" + failureHeader +
                "\nОжидаемое количество блоков, удовлетворяющих условию: " + expectedCount +
                "\nФактическое количество таких блоков: " + matches.size() +
                "\nОбщее количество блоков в списке: " + blocks.size();

        Assert.assertEquals(matches.size(), expectedCount, messageOnFailure);

        return new BlockListStepResult(matches, key);
    }

    @Step("Ожидаем появления {blockIndex}-го блока")
    private CorePage waitForBlockByNumber(BlockListContext context, int blockIndex) {
        return BlockSearchExecutor.awaitBlockByNumber(
                context,
                blockIndex,
                "Не удалось дождаться блока №" + blockIndex + "\n" + context.describe()
        );
    }

    @Step("Ожидаем, пока элемент '{elementName}' в блоке №{blockIndex} удовлетворит условию")
    private CorePage waitUntilElementInBlockByNumberMeetsCondition(BlockListContext context,
                                                                   int blockIndex,
                                                                   String elementName,
                                                                   WebElementCondition condition) {
        return waitUntilElementInBlockByNumberMeetsCondition(
                context,
                blockIndex,
                elementName,
                condition,
                element -> {
                }
        );
    }

    private CorePage waitUntilElementInBlockByNumberMeetsCondition(BlockListContext context,
                                                                   int blockIndex,
                                                                   String elementName,
                                                                   WebElementCondition condition,
                                                                   Consumer<SelenideElement> onMatched) {
        return BlockSearchExecutor.awaitElementInBlock(
                context,
                blockIndex,
                elementName,
                condition,
                onMatched,
                "Элемент '" + elementName + "' в блоке №" + blockIndex +
                        " не удовлетворил условию '" + condition + "'\n" + context.describe()
        );
    }

    @Step("Ожидаем, пока корневой элемент блока №{blockIndex} удовлетворит условию")
    private CorePage waitUntilBlockByNumberSelfMeetsCondition(BlockListContext context,
                                                              int blockIndex,
                                                              WebElementCondition condition) {
        return BlockSearchExecutor.awaitBlockRoot(
                context,
                blockIndex,
                condition,
                "Блок №" + blockIndex + " не удовлетворил условию '" + condition +
                        "'\n" + context.describe()
        );
    }

    @Step("Ожидаем соответствия текста элемента '{elementName}' регулярному выражению в блоке №{blockIndex}")
    private CorePage waitUntilTextMatchesInBlockByNumber(BlockListContext context,
                                                         int blockIndex,
                                                         String elementName,
                                                         String regExp) {
        return BlockSearchExecutor.awaitElementInBlock(
                context,
                blockIndex,
                elementName,
                BlockConditions.textMatches(regExp),
                "Текст элемента '" + elementName + "' в блоке №" + blockIndex +
                        " не соответствует выражению '" + regExp + "'\n" + context.describe()
        );
    }

    @Step("Ожидаем блок, в котором элемент '{elementName}' удовлетворяет условию")
    private CorePage waitUntilAnyBlockElementMeetsCondition(BlockListContext context,
                                                            String elementName,
                                                            WebElementCondition condition) {
        return BlockSearchExecutor.findInContext(
                context,
                elementName,
                condition,
                "Не найден блок, в котором элемент '" + elementName +
                        "' удовлетворяет условию '" + condition + "'\n" + context.describe()
        );
    }

    /**
     * Строит детальное сообщение о несоответствии элементов ожиданиям, заданным в {@link DataTable}.
     * <p>
     * Для каждой строки таблицы ожидается формат:
     * | индекс блока | имя элемента | текстовое условие | ожидаемое значение/регулярка |
     */
    @Step("Формируем сообщение о несоответствии списка блоков ожидаемым условиям из таблицы")
    private String buildBlockListMatchesListMessage(BlockListContext blockListContext,
                                                    DataTable conditionsTable,
                                                    boolean useOneBasedIndexInMessage) {
        List<List<String>> conditionsRows = conditionsTable.asLists();
        List<CorePage> blocksList = blockListContext.getBlocks();
        String contextDescription = blockListContext.describe();
        String resultMessageTemplate = "%s\nБлок с индексом %d: элемент '%s' не соответствует условию: %s '%s'\\nФактический web-элемент: %s\\n";
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
                                contextDescription,
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

    @Step("Проверяем, что блоки списка расположены по {elementsInRow} в ряд")
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

    @Step("Проверяем, что все блоки списка расположены по ширине элемента '{elementOuterName}'")
    private void checkBlockListElementsInWidthOfElement(List<CorePage> blocksList, SelenideElement outerElement, String elementOuterName) {
        int index = 0;
        int elementLeftBound = outerElement.getLocation().x;
        int elementRightBound = elementLeftBound + outerElement.getSize().width;

        for (CorePage block : blocksList) {
            index++;
            int blockLeftBoundX = block.getSelf().getLocation().x;
            int blockRightBound = blockLeftBoundX + block.getSelf().getSize().width;

            assertTrue((blockLeftBoundX >= elementLeftBound) && (blockRightBound <= elementRightBound),
                    String.format("%d блок расположен не по ширине элемента '%s'", index, elementOuterName));
        }
    }

    /**
     * Проверка, что каждый блок списка удовлетворяет всем условиям из таблицы.
     * Для каждого блока и строки таблицы выполняется полноценное ожидание через {@code shouldHave}.
     */
    @Step("Проверяем, что каждый блок списка удовлетворяет сложным условиям из таблицы")
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
    @Step("Ищем блоки в списке, удовлетворяющие сложным условиям из таблицы")
    private IStepResult checkBlockListForComplexCondition(BlockListContext blockListContext,
                                                          DataTable conditionsTable) {
        List<CorePage> resultList =
                getBlockListWithComplexCondition(blockListContext, conditionsTable);

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }

    // Helpers for "конкретный блок, где текст ..." and "в любом блоке где текст ..." patterns

    private CorePage waitUntilBlockWhereTextEquals(BlockListContext initialContext,
                                                   String elementNameSearch,
                                                   String resolvedExpectedText,
                                                   Consumer<CorePage> onMatched) {
        return BlockSearchExecutor.findInContext(
                initialContext,
                elementNameSearch,
                BlockConditions.textEquals(resolvedExpectedText),
                onMatched,
                "Во всех блоках в элементах " + elementNameSearch +
                        " не найден текст:" + resolvedExpectedText
        );
    }

    @Step("Ищем блок, где в элементе '{elementNameSearch}' текст равен '{expectedTextSearch}', и проверяем отображение элемента '{expectedElementVisible}'")
    private IStepResult elementDisplayedInBlockWhereTextEquals(BlockListContext blockListContext,
                                                               String elementNameSearch,
                                                               String expectedTextSearch,
                                                               String expectedElementVisible) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage corePageByTextInElement =
                waitUntilBlockWhereTextEquals(
                        blockListContext,
                        elementNameSearch,
                        resolvedExpectedText,
                        block -> block.getElement(expectedElementVisible).shouldBe(Condition.visible)
                );

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    @Step("Ищем блок, где в элементе '{elementNameSearch}' текст равен '{expectedTextSearch}', и проверяем, что элемент '{expectedElementVisible}' не отображается")
    private IStepResult elementNotDisplayedInBlockWhereTextEquals(BlockListContext blockListContext,
                                                                  String elementNameSearch,
                                                                  String expectedTextSearch,
                                                                  String expectedElementVisible) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage corePageByTextInElement =
                waitUntilBlockWhereTextEquals(
                        blockListContext,
                        elementNameSearch,
                        resolvedExpectedText,
                        block -> block.getElement(expectedElementVisible).shouldNot(Condition.visible)
                );

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    @Step("Проверяем, что в каком-либо блоке текст элемента '{elementNameSearch}' соответствует регулярному выражению '{expectedTextSearch}'")
    private IStepResult checkTextInAnyBlockMatches(BlockListContext blockListContext,
                                                   String elementNameSearch,
                                                   String expectedTextSearch,
                                                   String expectedElementVisible) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        CorePage corePageByTextInElement = BlockSearchExecutor.findInContext(
                blockListContext,
                elementNameSearch,
                BlockConditions.textMatches(resolvedExpectedText),
                block -> block.getElement(expectedElementVisible).shouldBe(Condition.visible),
                "Во всех блоках в элементах " + elementNameSearch +
                        " не найден текст по выражению:" + resolvedExpectedText
        );

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    @Step("Проверяем текст элемента '{elementNameFind}' в блоке, где элемент '{elementNameSearch}' имеет ожидаемый текст '{expectedTextSearch}'")
    private IStepResult checkTextInAnyBlock(BlockListContext blockListContext,
                                            String elementNameSearch,
                                            String expectedTextSearch,
                                            String elementNameFind,
                                            String expectedTextFind) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedExpectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        CorePage corePageByTextInElement =
                waitUntilBlockWhereTextEquals(
                        blockListContext,
                        elementNameSearch,
                        resolvedExpectedText,
                        block -> shouldHaveTextMatches(block, elementNameFind, resolvedExpectedTextFind)
                );

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    @Step("Проверяем по регулярным выражениям текст элемента '{elementNameFind}' в блоке, найденном по элементу '{elementNameSearch}'")
    private IStepResult checkTextInAnyBlockMatches1(BlockListContext blockListContext,
                                                    String elementNameSearch,
                                                    String expectedTextSearch,
                                                    String elementNameFind,
                                                    String expectedTextFind) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedExpectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        CorePage corePageByTextInElement = BlockSearchExecutor.findInContext(
                blockListContext,
                elementNameSearch,
                BlockConditions.textMatches(resolvedExpectedText),
                block -> block.getElement(elementNameFind)
                        .shouldHave(Condition.matchText(resolvedExpectedTextFind), Duration.ZERO),
                "Во всех блоках в элементах " + elementNameSearch +
                        " не найден текст по выражению:" + resolvedExpectedText
        );

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    @Step("Проверяем css '{cssName}={cssValue}' элемента '{elementNameFind}' в блоке, найденном по тексту элемента '{elementNameSearch}'")
    private IStepResult checkCssInAnyBlock(BlockListContext blockListContext,
                                           String elementNameSearch,
                                           String expectedTextSearch,
                                           String elementNameFind,
                                           String cssName,
                                           String cssValue) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        CorePage block = waitUntilBlockWhereTextEquals(
                blockListContext,
                elementNameSearch,
                resolvedExpectedText,
                matchedBlock -> matchedBlock.getElement(elementNameFind)
                        .shouldHave(Condition.cssValue(resolvedCssName, resolvedCssValue))
        );
        return new BlockListStepResult(block, elementNameSearch, elementNameFind);
    }

    @Step("Проверяем, что блок с текстом элемента '{elementNameSearch}' = '{expectedTextSearch}' находится '{boundsCondition}' видимой области")
    private IStepResult checkBlockWithTextInElementInBounds(BlockListContext blockListContext,
                                                            String elementNameSearch,
                                                            String expectedTextSearch,
                                                            String boundsCondition) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        CorePage corePageByTextInElement =
                waitUntilBlockWhereTextEquals(
                        blockListContext,
                        elementNameSearch,
                        resolvedExpectedText,
                        block -> inBounds(block.getSelf(), boundsCondition)
                );

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch);
    }

    @Step("Сохраняем текст элемента '{elementToSaveText}' из блока, где элемент '{elementToCheckText}' имеет текст '{expectedText}', в переменную '{varName}'")
    private IStepResult saveElementTextToVarInBlockListWhereTextEquals(BlockListContext blockListContext,
                                                                       String elementToCheckText,
                                                                       String expectedText,
                                                                       String elementToSaveText,
                                                                       String varName) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage corePageByTextInElement =
                waitUntilBlockWhereTextEquals(
                        blockListContext,
                        elementToCheckText,
                        resolvedExpectedText,
                        block -> {
                            String text = block.getElement(elementToSaveText).getText();
                            CoreScenario.getInstance().getEnvironment().setVar(varName, text);
                        }
                );

        return new BlockListStepResult(corePageByTextInElement, elementToCheckText, elementToSaveText);
    }

}
