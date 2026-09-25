package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.А;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.entities.BlockListStepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.CustomCondition;
import ru.at.library.web.scenario.IStepResult;
import ru.at.library.web.scenario.WebScenario;
import ru.at.library.web.step.blockcollection.helper.BlockAllureReport;
import ru.at.library.web.step.blockcollection.helper.BlockCheckSupport;
import ru.at.library.web.step.blockcollection.helper.BlockConditions;
import ru.at.library.web.step.blockcollection.helper.BlockListContext;
import ru.at.library.web.step.blockcollection.helper.BlockSearchExecutor;

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
import static ru.at.library.web.step.blockcollection.helper.BlockCheckSupport.createBlockListContext;
import static ru.at.library.web.step.blockcollection.helper.BlockCheckSupport.describeBlockTargetState;
import static ru.at.library.web.step.blockcollection.helper.BlockCheckSupport.forEachBlock;
import static ru.at.library.web.step.blockcollection.helper.BlockCheckSupport.forEachBlockWithoutResult;
import static ru.at.library.web.step.blockcollection.helper.BlocksCollectionOtherMethod.getBlockListWithCheckingTheQuantity;
import static ru.at.library.web.step.blockcollection.helper.BlocksCollectionOtherMethod.getBlockListWithComplexCondition;
import static ru.at.library.web.step.blockcollection.helper.BlocksCollectionOtherMethod.getSelenideCondition;
import static ru.at.library.web.step.blockcollection.helper.BlocksCollectionOtherMethod.methodCheckHasCssInBlockList;
import static ru.at.library.web.step.blockcollection.helper.BlocksCollectionOtherMethod.methodCheckNotHasCssInBlockList;
import static ru.at.library.web.step.blockcollection.helper.BlocksCollectionOtherMethod.shouldHaveTextMatches;
import static ru.at.library.web.step.selenideelement.SelenideElementCheckSteps.inBounds;

/**
 * Все шаги-проверки для работы со списками блоков (List&lt;CorePage&gt;), построенные поверх
 * {@link BlockListContext} и общего движка поиска/ожидания в пакете
 * {@code ru.at.library.web.step.blockcollection.helper}.
 * <p>
 * Раньше этот класс был разбит на пять по "области действия" (весь список / каждый блок / любой
 * блок / N блоков / конкретный блок), но границы между ними на практике оказались нечёткими —
 * например, методы с "AnyBlock" в имени жили в классе "SpecificBlock". Поэтому шаги снова в одном
 * файле, но сгруппированы по разделам (см. комментарии-баннеры ниже) в порядке от простого к
 * сложному:
 * <ol>
 *     <li>Список блоков в целом (виден ли, сколько блоков, совпадает ли с таблицей);</li>
 *     <li>Расположение списка (ряды, ширина контейнера);</li>
 *     <li>Поиск одного блока по условию — "любой из блоков" / "где ... текст равен ...";</li>
 *     <li>Проверка каждого блока по очереди;</li>
 *     <li>Проверка, что ровно N блоков удовлетворяют условию;</li>
 *     <li>Проверка конкретного блока по номеру.</li>
 * </ol>
 * Шаги "в блоке ..." и без него объединены в один метод на каждую проверку (см. пакетный
 * Javadoc/CLAUDE.md — "Technique A"): короткий вариант шага получает фиктивную пустую
 * захватывающую группу {@code ()} в начале regex, чтобы число групп совпадало с "блочным"
 * вариантом — это сохраняет автодополнение IntelliJ для обеих формулировок.
 */
public class BlocksCollectionCheckSteps {

    /*
     * =================================================================================================
     * 1. СПИСОК БЛОКОВ В ЦЕЛОМ — виден ли список, сколько в нём блоков, совпадает ли с таблицей
     * =================================================================================================
     */

    @И("^()список блоков \"([^\"]*)\" отображается на странице$")
    @А("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" отображается на странице$")
    public IStepResult listBlockVisible(String blockName, String blockListName) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
        forEachBlockWithoutResult(
                blockListContext,
                null,
                "блок отображается на странице",
                CorePage::isAppeared
        );
        return new BlockListStepResult(blockListContext.getBlocks());
    }

    @И("^()в списке блоков \"([^\"]*)\" количество блоков (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" количество блоков (равно|не равно|больше|меньше|больше или равно|меньше или равно) (\\d+)$")
    public IStepResult checkBlockListSize(String blockName, String blockListName, String condition, int expectedCountBlock) {
        CustomCondition.Comparison comparison = CustomCondition.Comparison.fromString(condition);

        List<CorePage> blocksList = BlockListContext.isAbsent(blockName)
                ? getBlockListWithCheckingTheQuantity(blockListName, comparison, expectedCountBlock)
                : getBlockListWithCheckingTheQuantity(blockName, blockListName, comparison, expectedCountBlock);

        return new BlockListStepResult(blocksList);
    }

    /**
     * NB: в исходном коде до-рефакторинга варианты "без блока"/"с блоком" отличались не только
     * контекстом, но и способом сообщения об ошибке (без блока — через hamcrestAssert с пустой
     * строкой, с блоком — через прямой throw). Сохраняем оба поведения как есть, привязав их
     * к наличию имени блока.
     * <p>
     * Для каждой строки {@code conditionsTable} ожидается формат:
     * | индекс блока | имя элемента | текстовое условие | ожидаемое значение/регулярка |
     */
    @SuppressWarnings("deprecation")
    @И("^()список блоков \"([^\"]*)\" соответствует списку$")
    @А("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" соответствует списку$")
    public void blockListMatchesList(String blockName, String blockListName, DataTable conditionsTable) {
        boolean noBlockGiven = BlockListContext.isAbsent(blockName);
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);

        List<List<String>> conditionsRows = conditionsTable.asLists();
        List<CorePage> blocksList = blockListContext.getBlocks();
        String contextDescription = blockListContext.describe();
        String resultMessageTemplate = "%s\nБлок с индексом %d: элемент '%s' не соответствует условию: %s '%s'\nФактический web-элемент: %s\n";
        StringBuilder resultMessageBuilder = new StringBuilder();

        for (int rowIndex = 0; rowIndex < conditionsRows.size(); rowIndex++) {
            List<String> conditionRow = conditionsRows.get(rowIndex);
            int blockNumber = Integer.parseInt(conditionRow.get(0));
            int blockIndex = blockNumber - 1;
            if (blockIndex < 0 || blockIndex >= blocksList.size()) {
                throw new IllegalArgumentException(
                        "Некорректный номер блока в таблице условий: " + blockNumber +
                                " (строка №" + (rowIndex + 1) + ")" +
                                "\nНомер блока должен быть от 1 до " + blocksList.size() +
                                "\n" + contextDescription
                );
            }
            String elementName = conditionRow.get(1);
            String textCondition = conditionRow.get(2);
            String expectedText = resolveVars(getPropertyOrStringVariableOrValue(conditionRow.get(3)));

            SelenideElement element = blocksList.get(blockIndex).getElement(elementName);
            String stepTitle = "Проверка №" + (rowIndex + 1) + " из " + conditionsRows.size() +
                    " — блок №" + (blockIndex + 1) +
                    " — элемент '" + elementName + "'";
            String expectation = textCondition + " '" + expectedText + "'";
            boolean matched = Allure.step(stepTitle, step -> {
                try {
                    BlockAllureReport.withoutSelenideSteps(
                            () -> {
                                element.shouldHave(
                                        getSelenideCondition(textCondition, expectedText)
                                );
                            }
                    );
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "ВЫПОЛНЕНО",
                            expectation,
                            BlockAllureReport.elementState(element)
                    );
                    return true;
                } catch (AssertionError error) {
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "НЕ ВЫПОЛНЕНО",
                            expectation,
                            BlockAllureReport.elementState(element)
                    );
                    BlockAllureReport.addError(step, error);
                    return false;
                }
            });

            if (!matched) {
                int indexForMessage = noBlockGiven ? blockIndex + 1 : blockIndex;
                resultMessageBuilder
                        .append(String.format(resultMessageTemplate,
                                contextDescription,
                                indexForMessage,
                                elementName,
                                textCondition,
                                expectedText,
                                blocksList.get(blockIndex).getSelf().toString()));
            }
        }

        String resultMessage = resultMessageBuilder.toString();
        if (!resultMessage.isEmpty()) {
            BlockAllureReport.attachFailureScreenshot();
        }
        if (noBlockGiven) {
            CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(resultMessage, resultMessage, isEmptyString());
        } else if (!resultMessage.isEmpty()) {
            throw new AssertionError(resultMessage);
        }
    }

    /*
     * =================================================================================================
     * 2. РАСПОЛОЖЕНИЕ СПИСКА — раскладка по рядам, ширина контейнера
     * =================================================================================================
     */

    @SuppressWarnings("deprecation")
    @И("^()список блоков \"([^\"]*)\" блоки расположены по (\\d+) в ряд$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" блоки расположены по (\\d+) в ряд$")
    public IStepResult checkBlockListRowsFormat(String blockName, String blockListName, int elementsInRow) {
        if (elementsInRow < 1) {
            throw new IllegalArgumentException(
                    "Количество блоков в ряду должно быть больше нуля"
            );
        }

        List<CorePage> blocksList = createBlockListContext(blockName, blockListName).getBlocks();
        Point previousElementLocation = null;
        Point currentRowStart = null;

        for (int index = 0; index < blocksList.size(); index++) {
            int blockNumber = index + 1;
            int rowNumber = index / elementsInRow + 1;
            boolean startsRow = index % elementsInRow == 0;

            Point previousLocation = previousElementLocation;
            Point rowStart = currentRowStart;
            String expectation = startsRow
                    ? (rowStart == null
                    ? "начинает строку №" + rowNumber
                    : "начинает строку №" + rowNumber + " ниже предыдущей строки")
                    : "находится правее блока №" + (blockNumber - 1) +
                    " в строке №" + rowNumber;
            String stepTitle = "Блок №" + blockNumber + " из " + blocksList.size();

            Point location = Allure.step(stepTitle, step -> {
                Point actualLocation = null;
                try {
                    actualLocation = BlockAllureReport.withoutSelenideSteps(
                            () -> blocksList.get(blockNumber - 1)
                                    .getSelf()
                                    .getRect()
                                    .getPoint()
                    );
                    if (startsRow && rowStart != null) {
                        assertTrue(
                                actualLocation.y > rowStart.y,
                                blockNumber + " блок должен начинать новую строку"
                        );
                    } else if (!startsRow) {
                        assertTrue(
                                actualLocation.x > previousLocation.x,
                                blockNumber + " блок должен быть правее блока №" +
                                        (blockNumber - 1)
                        );
                        assertEquals(
                                actualLocation.y,
                                rowStart.y,
                                blockNumber + " блок должен находиться в строке №" + rowNumber
                        );
                    }
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "ВЫПОЛНЕНО",
                            expectation,
                            coordinates(actualLocation)
                    );
                    return actualLocation;
                } catch (RuntimeException | AssertionError error) {
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "НЕ ВЫПОЛНЕНО",
                            expectation,
                            actualLocation == null
                                    ? "координаты недоступны"
                                    : coordinates(actualLocation)
                    );
                    BlockAllureReport.addError(step, error);
                    BlockAllureReport.attachFailureScreenshot();
                    throw error;
                }
            });

            if (startsRow) {
                currentRowStart = location;
            }
            previousElementLocation = location;
        }
        return new BlockListStepResult(blocksList);
    }

    @SuppressWarnings("deprecation")
    @И("^()список блоков \"([^\"]*)\" расположен по ширине элемента \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" расположен по ширине элемента \"([^\"]*)\"$")
    public void checkBlockListElementsInWidthOfElement(String blockName, String blockListName, String elementOuter) {
        List<CorePage> blocksList = createBlockListContext(blockName, blockListName).getBlocks();
        CorePage owner = BlockListContext.isAbsent(blockName)
                ? WebScenario.getCurrentPage()
                : WebScenario.getCurrentPage().getBlock(blockName);
        SelenideElement outerElement = owner.getElement(elementOuter);

        String outerStepTitle = "Границы элемента '" + elementOuter + "'";
        Rectangle outerRect = Allure.step(outerStepTitle, step -> {
            try {
                Rectangle rect = BlockAllureReport.withoutSelenideSteps(
                        outerElement::getRect
                );
                BlockAllureReport.finishStep(
                        step,
                        outerStepTitle,
                        "ПОЛУЧЕНЫ",
                        "элемент доступен",
                        "x=" + rect.x + ", y=" + rect.y + ", width=" + rect.width + ", height=" + rect.height
                );
                return rect;
            } catch (RuntimeException | AssertionError error) {
                BlockAllureReport.finishStep(
                        step,
                        outerStepTitle,
                        "НЕДОСТУПНЫ",
                        "элемент доступен",
                        "границы недоступны"
                );
                BlockAllureReport.addError(step, error);
                BlockAllureReport.attachFailureScreenshot();
                throw error;
            }
        });
        int elementLeftBound = outerRect.x;
        int elementRightBound = elementLeftBound + outerRect.width;

        for (int index = 0; index < blocksList.size(); index++) {
            int blockNumber = index + 1;
            CorePage block = blocksList.get(index);
            String expectation = "границы находятся внутри элемента '" +
                    elementOuter + "' [" + elementLeftBound + ", " +
                    elementRightBound + "]";
            String stepTitle = "Блок №" + blockNumber + " из " + blocksList.size();

            Allure.step(stepTitle, step -> {
                String actualState = "границы недоступны";
                try {
                    Rectangle blockRect = BlockAllureReport.withoutSelenideSteps(
                            () -> block.getSelf().getRect()
                    );
                    int blockLeftBound = blockRect.x;
                    int blockRightBound = blockLeftBound + blockRect.width;
                    actualState = "границы [" + blockLeftBound + ", " +
                            blockRightBound + "]";

                    assertTrue(
                            blockLeftBound >= elementLeftBound
                                    && blockRightBound <= elementRightBound,
                            blockNumber + " блок расположен не по ширине элемента '" +
                                    elementOuter + "'"
                    );
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "ВЫПОЛНЕНО",
                            expectation,
                            actualState
                    );
                } catch (RuntimeException | AssertionError error) {
                    BlockAllureReport.finishStep(
                            step,
                            stepTitle,
                            "НЕ ВЫПОЛНЕНО",
                            expectation,
                            actualState
                    );
                    BlockAllureReport.addError(step, error);
                    BlockAllureReport.attachFailureScreenshot();
                    throw error;
                }
            });
        }
    }

    /*
     * =================================================================================================
     * 3. ПОИСК ОДНОГО БЛОКА ПО УСЛОВИЮ — "любой из блоков ..." / "где в элементе ... текст равен ..."
     *    Обе формулировки в итоге делают одно и то же: ищут среди блоков список первый подходящий
     *    и либо проверяют его же, либо проверяют/используют другой элемент внутри него.
     * =================================================================================================
     */

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
    @И("^()в списке блоков \"([^\"]*)\" любой из блоков соответствует условиям$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" любой из блоков соответствует условиям$")
    public IStepResult checkBlockListForComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
        List<CorePage> resultList =
                getBlockListWithComplexCondition(blockListContext, conditionsTable);

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
    }

    @И("^()в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст равен \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст равен \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.live(blockName, blockListName),
                elementName,
                BlockConditions.textEquals(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkByRegExpInElementInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.live(blockName, blockListName),
                elementName,
                BlockConditions.textMatches(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    public IStepResult checkContainTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = getPropertyOrStringVariableOrValue(expectedText);

        CorePage block = waitUntilAnyBlockElementMeetsCondition(
                BlockListContext.live(blockName, blockListName),
                elementName,
                BlockConditions.textContains(resolvedExpectedText)
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult elementDisplayedInBlockWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" не отображается$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" не отображается$")
    public IStepResult elementNotDisplayedInBlockWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult checkTextInAnyBlockMatches(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextFormatInAnyBlockFoundByRegExp(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"")
    public IStepResult checkCssInAnyBlock(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String cssName, String cssValue) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @SuppressWarnings("deprecation")
    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" блок расположен (в|вне) видимой части браузера")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" блок расположен (в|вне) видимой части браузера")
    public IStepResult checkBlockWithTextInElementInBounds(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String boundsCondition) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @И("^()в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено сохранение текста элемента \"([^\"]*)\" в переменную \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено сохранение текста элемента \"([^\"]*)\" в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextToVarInBlockListWhereTextEquals(String blockName, String blockListName, String elementToCheckText, String expectedText, String elementToSaveText, String varName) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    /*
     * =================================================================================================
     * 4. КАЖДЫЙ БЛОК — одно и то же условие проверяется по очереди на всех блоках списка
     * =================================================================================================
     */

    @И("^()в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" отображается на странице$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockName, String blockListName, String elementVisible) {
        return forEachBlock(
                createBlockListContext(blockName, blockListName),
                elementVisible,
                "элемент '" + elementVisible + "' отображается на странице",
                block -> block.getElement(elementVisible).shouldHave(Condition.visible));
    }

    @И("^()в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не отображается на странице$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не отображается на странице$")
    public IStepResult elementNotVisibleInBlockList(String blockName, String blockListName, String elementHidden) {
        return forEachBlock(
                createBlockListContext(blockName, blockListName),
                elementHidden,
                "элемент '" + elementHidden + "' не отображается на странице",
                block -> block.getElement(elementHidden).shouldNot(Condition.visible));
    }

    @И("^()в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" является изображением и отображается на странице$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" является изображением и отображается на странице$")
    public void checkImageInBlockList(String blockName, String blockListName, String elementImageLoaded) {
        forEachBlockWithoutResult(
                createBlockListContext(blockName, blockListName),
                elementImageLoaded,
                "элемент '" + elementImageLoaded + "' является загруженным видимым изображением",
                block -> block.getElement(elementImageLoaded)
                        .shouldHave(Condition.image)
                        .shouldHave(Condition.visible));
    }

    @И("^()в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockName, String blockListName, String elementName, String regExp) {
        String resolvedRegExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);
        return forEachBlock(
                createBlockListContext(blockName, blockListName),
                elementName,
                "текст элемента '" + elementName + "' соответствует '" + resolvedRegExp + "'",
                block -> shouldHaveTextMatches(block, elementName, resolvedRegExp));
    }

    @И("^()в списке блоков \"([^\"]*)\" в каждом блоке в элементе \"([^\"]*)\" текст не равен \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке в элементе \"([^\"]*)\" текст не равен \"([^\"]*)\"$")
    public IStepResult checkNotTextInBlockListMatches(String blockName, String blockListName, String elementName, String expectedText) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);
        return forEachBlock(
                createBlockListContext(blockName, blockListName),
                elementName,
                "текст элемента '" + elementName + "' не равен '" + resolvedExpectedText + "'",
                block -> block.getElement(elementName)
                        .shouldNotHave(BlockConditions.textEquals(resolvedExpectedText)));
    }

    @И("^()в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkCssInBlockList(String blockName, String blockListName, String elementName, String cssName, String cssValue) {
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        return forEachBlock(
                createBlockListContext(blockName, blockListName),
                elementName,
                "элемент '" + elementName + "' содержит css '" +
                        resolvedCssName + "=" + resolvedCssValue + "'",
                block -> methodCheckHasCssInBlockList(
                        block,
                        elementName,
                        cssName,
                        resolvedCssValue
                ));
    }

    @И("^()в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkHasNotCssInBlockList(String blockName, String blockListName, String elementName, String cssName, String cssValue) {
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        return forEachBlock(
                createBlockListContext(blockName, blockListName),
                elementName,
                "элемент '" + elementName + "' не содержит css '" +
                        resolvedCssName + "=" + resolvedCssValue + "'",
                block -> methodCheckNotHasCssInBlockList(
                        block,
                        elementName,
                        cssName,
                        resolvedCssValue
                ));
    }

    /**
     * Проверка, что каждый блок списка удовлетворяет всем условиям из таблицы.
     * Для каждого блока и строки таблицы выполняется полноценное ожидание через {@code shouldHave}.
     */
    @И("^()в списке блоков \"([^\"]*)\" каждый из блоков соответствует условиям$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" каждый из блоков соответствует условиям$")
    public IStepResult everyBlockInBlockListMatchesComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
        List<CorePage> blocksList = blockListContext.getBlocks();
        List<List<String>> conditionsRows = conditionsTable.asLists();

        forEachBlockWithoutResult(
                blockListContext,
                null,
                "каждый элемент блока удовлетворяет условиям из таблицы",
                block -> {
                    for (int index = 0; index < conditionsRows.size(); index++) {
                        int conditionNumber = index + 1;
                        List<String> conditionsRow = conditionsRows.get(index);
                        String elementName = conditionsRow.get(0);
                        String textCondition = conditionsRow.get(1);
                        String expectedText = resolveVars(
                                getPropertyOrStringVariableOrValue(conditionsRow.get(2))
                        );
                        SelenideElement element = block.getElement(elementName);
                        String stepTitle = "Условие №" + conditionNumber + " из " +
                                conditionsRows.size() + " — элемент '" + elementName + "'";
                        String expectation = textCondition + " '" + expectedText + "'";

                        Allure.step(stepTitle, step -> {
                            try {
                                element.shouldHave(
                                        getSelenideCondition(textCondition, expectedText)
                                );
                                BlockAllureReport.finishStep(
                                        step,
                                        stepTitle,
                                        "ВЫПОЛНЕНО",
                                        expectation,
                                        BlockAllureReport.elementState(element)
                                );
                            } catch (RuntimeException | AssertionError error) {
                                BlockAllureReport.finishStep(
                                        step,
                                        stepTitle,
                                        "НЕ ВЫПОЛНЕНО",
                                        expectation,
                                        BlockAllureReport.elementState(element)
                                );
                                BlockAllureReport.addError(step, error);
                                throw error;
                            }
                        });
                    }
                }
        );

        return new BlockListStepResult(blocksList,
                conditionsRows.stream().map(conditionsRow -> conditionsRow.get(0)).collect(Collectors.toList()));
    }

    /*
     * =================================================================================================
     * 5. РОВНО N БЛОКОВ — в отличие от раздела "каждый блок", здесь допускаются блоки,
     *    не удовлетворяющие условию, лишь бы ровно N блоков ему соответствовали
     * =================================================================================================
     */

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockName, String blockListName, int blockNumber, String elementName, String regExp) {
        String resolvedRegExp = getPropertyOrStringVariableOrValue(regExp);
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" отображается на странице$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockName, String blockListName, int blockNumber, String elementName) {
        BlockListContext blockListContext = createBlockListContext(blockName, blockListName);
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

    /*
     * =================================================================================================
     * 6. КОНКРЕТНЫЙ БЛОК ПО НОМЕРУ — "N блок ...", "в N блоке ...", "координаты N блока ..."
     * =================================================================================================
     */

    @SuppressWarnings("deprecation")
    @И("^()в списке блоков \"([^\"]*)\" (\\d+) блок содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" (\\d+) блок содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkBlockListForBlockWithCss(String blockName, String blockListName, int blockIndex, String cssName, String cssValue) {
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        WebElementCondition condition = Condition.cssValue(resolvedCssName, resolvedCssValue);
        BlockListContext context = BlockListContext.live(blockName, blockListName);
        CorePage block = BlockSearchExecutor.awaitBlockRoot(
                context,
                blockIndex,
                condition,
                "Блок №" + blockIndex + " не удовлетворил условию '" + condition +
                        "'\n" + context.describe()
        );
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^()в списке блоков \"([^\"]*)\" координаты (\\d+) блока соответствуют: x=(\\d+); y=(\\d+)$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" координаты (\\d+) блока соответствуют: x=(\\d+); y=(\\d+)$")
    public IStepResult checkBlockListItemCoordinates(String blockName, String blockListName, int blockIndex, int x, int y) {
        Point expectedCoordinates = new Point(x, y);

        CorePage block = waitForBlockByNumber(
                BlockListContext.live(blockName, blockListName),
                blockIndex
        );
        Point actualCoordinates = block.getSelf().getLocation();
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
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

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоке текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextForNthBlockFromBlockList(String blockName, String blockListName, int blockIndex, String elementName, String varName) {
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.live(blockName, blockListName),
                blockIndex,
                elementName,
                Condition.visible,
                element -> CoreScenario.getInstance().getEnvironment().setVar(varName, element.getText())
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListMatchesText(String blockName, String blockListName, int blockIndex, String elementName, String expectedText) {
        String resolvedExpectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);

        BlockListContext context = BlockListContext.live(blockName, blockListName);
        CorePage block = BlockSearchExecutor.awaitElementInBlock(
                context,
                blockIndex,
                elementName,
                BlockConditions.textMatches(resolvedExpectedText),
                "Текст элемента '" + elementName + "' в блоке №" + blockIndex +
                        " не соответствует выражению '" + resolvedExpectedText + "'\n" + context.describe()
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
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
                BlockListContext.live(blockName, blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForCss(String blockName, String blockListName, int blockIndex, String elementName, String cssName, String cssValue) {
        String resolvedCssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        String resolvedCssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        WebElementCondition condition = Condition.cssValue(resolvedCssName, resolvedCssValue);
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.live(blockName, blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }

    @И("^()в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    @А("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForAttribute(String blockName, String blockListName, int blockIndex, String elementName, String attributeName, String attributeValue) {
        String resolvedAttributeName = OtherSteps.getPropertyOrStringVariableOrValue(attributeName);
        String resolvedAttributeValue = OtherSteps.getPropertyOrStringVariableOrValue(attributeValue);

        WebElementCondition condition = Condition.attributeMatching(resolvedAttributeName, resolvedAttributeValue);
        CorePage block = waitUntilElementInBlockByNumberMeetsCondition(
                BlockListContext.live(blockName, blockListName),
                blockIndex,
                elementName,
                condition
        );
        return new BlockListStepResult(block, elementName);
    }

    @SuppressWarnings("deprecation")
    @И("^()в списке блоков \"([^\"]+)\" (\\d+) блок расположен (в|вне) видимой части браузера$")
    @А("^в блоке \"([^\"]+)\" в списке блоков \"([^\"]+)\" (\\d+) блок расположен (в|вне) видимой части браузера$")
    public IStepResult checkBlockListItemInBounds(String blockName, String blockListName, int blockIndex, String boundsCondition) {
        CorePage block = waitForBlockByNumber(
                BlockListContext.live(blockName, blockListName),
                blockIndex
        );
        inBounds(block.getSelf(), boundsCondition);
        return new BlockListStepResult(block);
    }

    /*
     * =================================================================================================
     * Вспомогательные методы, общие для нескольких шагов выше
     * =================================================================================================
     */

    private String coordinates(Point point) {
        return "x=" + point.x + ", y=" + point.y;
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
            String expectation = BlockAllureReport.compact(
                    failureHeader.replace('\n', ' ')
            );
            for (int index = 0; index < blocks.size(); index++) {
                int blockNumber = index + 1;
                CorePage block = blocks.get(index);
                String blockStepTitle = "Блок №" + blockNumber + " из " + blocks.size() +
                        " — элемент '" + key + "'";
                boolean matched = Allure.step(blockStepTitle, blockStep -> {
                    try {
                        boolean resultMatched = BlockAllureReport.withoutSelenideSteps(
                                () -> predicate.test(block)
                        );
                        BlockAllureReport.finishStep(
                                blockStep,
                                blockStepTitle,
                                resultMatched ? "СООТВЕТСТВУЕТ" : "НЕ СООТВЕТСТВУЕТ",
                                expectation,
                                describeBlockTargetState(block, key)
                        );
                        return resultMatched;
                    } catch (RuntimeException | AssertionError error) {
                        BlockAllureReport.finishStep(
                                blockStep,
                                blockStepTitle,
                                "ОШИБКА ПРОВЕРКИ",
                                expectation,
                                describeBlockTargetState(block, key)
                        );
                        BlockAllureReport.addError(blockStep, error);
                        BlockAllureReport.attachFailureScreenshot();
                        throw error;
                    }
                });

                if (matched) {
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

        if (matches.size() != expectedCount) {
            BlockAllureReport.attachFailureScreenshot();
        }
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
}
