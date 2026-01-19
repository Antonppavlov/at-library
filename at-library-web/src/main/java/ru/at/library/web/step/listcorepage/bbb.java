package ru.at.library.web.step.listcorepage;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.Point;
import org.testng.Assert;
import ru.at.library.core.cucumber.api.CorePage;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.steps.OtherSteps;
import ru.at.library.web.core.CustomCondition;
import ru.at.library.web.core.IStepResult;
import ru.at.library.web.entities.BlockListStepResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Condition.not;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.utils.helpers.ScopedVariables.resolveVars;
import static ru.at.library.web.step.listcorepage.ListCorePageOtherMethod.*;
import static ru.at.library.web.step.selenideelement.SelenideElementCheckSteps.inBounds;

@Log4j2
public class ListCorePageCheckSteps {

    private final CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * -----------------------------------------------------------------------------------------------------------------
     * ---------------------------------------------Проверки списка блоков----------------------------------------------
     * -----------------------------------------------------------------------------------------------------------------
     */


    @И("^список блоков \"([^\"]*)\" отображается на странице$")
    public IStepResult listBlockVisible(String blockListName) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage corePage : blocksList) {
            corePage.isAppeared();
        }

        return new BlockListStepResult(blocksList);
    }

    @И("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" отображается на странице$")
    public IStepResult listBlockVisible(String blockName, String blockListName) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage corePage : blocksList) {
            corePage.isAppeared();
        }

        return new BlockListStepResult(blocksList);
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

    @SuppressWarnings("deprecation")
    @И("^список блоков \"([^\"]*)\" соответствует списку$")
    public void blockListMatchesList(String blockListName, DataTable conditionsTable) {
        List<List<String>> conditionsRows = conditionsTable.asLists();
        List<CorePage> blocksList = getBlockListWithCheckingTheQuantity(
                blockListName,
                CustomCondition.Comparison.equal,
                Integer.parseInt(conditionsRows.get(conditionsRows.size() - 1).get(0))
        );
        int blockIndex;
        String elementName, expectedText, textCondition;
        String resultMessageTemplate = "В блоке %d элемент %s не соответствует условию: %s %s\n%s\n";
        StringBuilder resultMessage = new StringBuilder();
        List<SelenideElement> checkedElements = new ArrayList<>();
        for (List<String> conditionRow : conditionsRows) {
            blockIndex = Integer.parseInt(conditionRow.get(0)) - 1;
            elementName = conditionRow.get(1);
            textCondition = conditionRow.get(2);
            expectedText = resolveVars(getPropertyOrStringVariableOrValue(conditionRow.get(3)));
            SelenideElement element = blocksList.get(blockIndex).getElement(elementName);
            checkedElements.add(element);
            if (!element.is(getSelenideCondition(textCondition, expectedText))) {
                resultMessage
                        .append(String.format(resultMessageTemplate, blockIndex + 1, elementName, textCondition, expectedText, blocksList.get(blockIndex).getSelf().toString()))
                        .append("\n");
            }
        }
        this.coreScenario.getAssertionHelper().hamcrestAssert(resultMessage.toString(), resultMessage.toString(), isEmptyString());
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" соответствует списку$")
    public void blockListMatchesList(String blockName, String blockListName, DataTable conditionsTable) {
        List<List<String>> conditionsRows = conditionsTable.asLists();
        List<CorePage> blocksList = getBlockListWithCheckingTheQuantity(
                blockName,
                blockListName,
                CustomCondition.Comparison.equal,
                Integer.parseInt(conditionsRows.get(conditionsRows.size() - 1).get(0))
        );
        int blockIndex;
        String elementName, expectedText, textCondition;
        String resultMessageTemplate = "В блоке %d элемент %s не соответствует условию: %s %s\n%s\n";
        StringBuilder resultMessage = new StringBuilder();
        List<SelenideElement> checkedElements = new ArrayList<>();
        for (List<String> conditionRow : conditionsRows) {
            blockIndex = Integer.parseInt(conditionRow.get(0)) - 1;
            elementName = conditionRow.get(1);
            textCondition = conditionRow.get(2);
            expectedText = resolveVars(getPropertyOrStringVariableOrValue(conditionRow.get(3)));
            SelenideElement element = blocksList.get(blockIndex).getElement(elementName);
            checkedElements.add(element);
            if (!element.is(getSelenideCondition(textCondition, expectedText))) {
                resultMessage
                        .append(String.format(resultMessageTemplate, blockIndex, elementName, textCondition, expectedText, blocksList.get(blockIndex).getSelf().toString()))
                        .append("\n");
            }
        }
        if (!resultMessage.toString().isEmpty()) {
            throw new AssertionError(resultMessage.toString());
        }
    }

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" блоки расположены по (\\d+) в ряд$")
    public IStepResult checkBlockListRowsFormat(String blockListName, int elementsInRow) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

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
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" блоки расположены по (\\d+) в ряд$")
    public IStepResult checkBlockListRowsFormat(String blockName, String blockListName, int elementsInRow) {
        List<CorePage> blocksList = getBlockListWithCheckingTheQuantity(
                blockName,
                blockListName,
                CustomCondition.Comparison.more,
                0
        );

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

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" (\\d+) блок содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkBlockListForBlockWithCss(String blockListName, int blockIndex, String cssName, String cssValue) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        block.getSelf().shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" (\\d+) блок содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkBlockListForBlockWithCss(String blockName, String blockListName, int blockIndex, String cssName, String cssValue) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        block.getSelf().shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(block);
    }

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^список блоков \"([^\"]*)\" расположен по ширине элемента \"([^\"]*)\"$")
    public void checkBlockListElementsInWidthOfElement(String blockListName, String elementOuter) {
        int index = 0;

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        SelenideElement element = this.coreScenario.getCurrentPage().getElement(elementOuter);
        int elementLeftBound = element.getLocation().x;
        int elementRightBound = elementLeftBound + element.getSize().width;

        for (CorePage block : blocksList) {
            index++;
            int blockLeftBoundX = block.getSelf().getLocation().x;
            int blockRightBound = blockLeftBoundX + block.getSelf().getSize().width;

            assertTrue((blockLeftBoundX >= elementLeftBound) && (blockRightBound <= elementRightBound),
                    String.format("%d блок расположен не по ширене элемента " + elementOuter, index));
        }
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" список блоков \"([^\"]*)\" расположен по ширине элемента \"([^\"]*)\"$")
    public void checkBlockListElementsInWidthOfElement(String blockName, String blockListName, String elementOuter) {
        int index = 0;

        List<CorePage> blocksList = getBlockListWithCheckingTheQuantity(
                blockName,
                blockListName,
                CustomCondition.Comparison.more,
                0
        );

        SelenideElement element = this.coreScenario.getCurrentPage().getBlock(blockName).getElement(elementOuter);
        int elementLeftBound = element.getLocation().x;
        int elementRightBound = elementLeftBound + element.getSize().width;

        for (CorePage block : blocksList) {
            index++;
            int blockLeftBoundX = block.getSelf().getLocation().x;
            int blockRightBound = blockLeftBoundX + block.getSelf().getSize().width;

            assertTrue((blockLeftBoundX >= elementLeftBound) && (blockRightBound <= elementRightBound),
                    String.format("%d блок расположен не по ширене элемента " + elementOuter, index));
        }
    }

    /**
     * -----------------------------------------------В КАЖДОМ------------------------------------------------
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockListName, String elementVisible) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            block.getElement(elementVisible).shouldHave(Condition.visible);
        }
        return new BlockListStepResult(blocksList, elementVisible);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockName, String blockListName, String elementVisible) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            block.getElement(elementVisible).shouldHave(Condition.visible);
        }
        return new BlockListStepResult(blocksList, elementVisible);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не отображается на странице$")
    public IStepResult elementNotVisibleInBlockList(String blockListName, String elementHidden) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            block.getElement(elementHidden).shouldNot(Condition.visible);
        }
        return new BlockListStepResult(blocksList, elementHidden);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не отображается на странице$")
    public IStepResult elementNotVisibleInBlockList(String blockName, String blockListName, String elementHidden) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            block.getElement(elementHidden).shouldNot(Condition.visible);
        }
        return new BlockListStepResult(blocksList, elementHidden);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" является изображением и отображается на странице$")
    public void checkImageInBlockList(String blockListName, String elementImageLoaded) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            SelenideElement element = block.getElement(elementImageLoaded);
            element
                    .shouldHave(Condition.image)
                    .shouldHave(Condition.visible);
        }
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" является изображением и отображается на странице$")
    public void checkImageInBlockList(String blockName, String blockListName, String elementImageLoaded) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            SelenideElement element = block.getElement(elementImageLoaded);
            element
                    .shouldHave(Condition.image)
                    .shouldHave(Condition.visible);
        }
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockListName, String elementName, String regExp) {
        regExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            shouldHaveTextMatches(block, elementName, regExp);
        }
        return new BlockListStepResult(blocksList, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockName, String blockListName, String elementName, String regExp) {
        regExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            shouldHaveTextMatches(block, elementName, regExp);
        }
        return new BlockListStepResult(blocksList, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке в элементе \"([^\"]*)\" текст не равен \"([^\"]*)\"$")
    public IStepResult checkNotTextInBlockListMatches(String blockListName, String elementName, String regExp) {
        regExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            block.getElement(elementName).shouldNot(Condition.and("Проверка что TextMatches элемента",
                    Condition.attribute("value", regExp),
                    Condition.attribute("title", regExp),
                    Condition.text(regExp)
            ));
        }
        return new BlockListStepResult(blocksList, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке в элементе \"([^\"]*)\" текст не равен \"([^\"]*)\"$")
    public IStepResult checkNotTextInBlockListMatches(String blockName, String blockListName, String elementName, String regExp) {
        regExp = OtherSteps.getPropertyOrStringVariableOrValue(regExp);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            block.getElement(elementName).shouldNot(Condition.and("Проверка что TextMatches элемента",
                    Condition.attribute("value", regExp),
                    Condition.attribute("title", regExp),
                    Condition.text(regExp)
            ));
        }
        return new BlockListStepResult(blocksList, elementName);
    }
    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkCssInBlockList(String blockListName, String elementName, String cssName, String cssValue) {
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            methodCheckHasCssInBlockList(block, elementName, cssName, cssValue);
        }

        return new BlockListStepResult(this.coreScenario.getCurrentPage().getBlocksList(blockListName), elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkCssInBlockList(String blockName, String blockListName, String elementName, String cssName, String cssValue) {
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            methodCheckHasCssInBlockList(block, elementName, cssName, cssValue);
        }

        return new BlockListStepResult(this.coreScenario.getCurrentPage().getBlocksList(blockListName), elementName);
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkHasNotCssInBlockList(String blockListName, String elementName, String cssName, String cssValue) {
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            methodCheckNotHasCssInBlockList(block, elementName, cssName, cssValue);
        }

        return new BlockListStepResult(this.coreScenario.getCurrentPage().getBlocksList(blockListName), elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в каждом блоке элемент \"([^\"]*)\" не содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkHasNotCssInBlockList(String blockName, String blockListName, String elementName, String cssName, String cssValue) {
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        for (CorePage block : blocksList) {
            methodCheckNotHasCssInBlockList(block, elementName, cssName, cssValue);
        }

        return new BlockListStepResult(this.coreScenario.getCurrentPage().getBlocksList(blockListName), elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" каждый из блоков соответствует условиям$")
    public IStepResult everyBlockInBlockListMatchesComplexCondition(String blockListName, DataTable conditionsTable) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        List<List<String>> conditionsRows = conditionsTable.asLists();
        int counter = 0;
        for (CorePage block : blocksList) {
            counter++;
            for (List<String> conditionsRow : conditionsRows) {
                String elementName = conditionsRow.get(0);
                String textCondition = conditionsRow.get(1);
                String expectedText = resolveVars(getPropertyOrStringVariableOrValue(conditionsRow.get(2)));

                if (counter == 0) {
                    block.getElement(elementName).shouldHave(getSelenideCondition(textCondition, expectedText));
                } else {
                    block.getElement(elementName).shouldHave(getSelenideCondition(textCondition, expectedText), Duration.ZERO);
                }
            }
        }
        return new BlockListStepResult(blocksList, conditionsRows.stream().map(conditionsRow -> conditionsRow.get(0)).collect(Collectors.toList()));
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" каждый из блоков соответствует условиям$")
    public IStepResult everyBlockInBlockListMatchesComplexCondition(String blockName, String blockListName, DataTable conditionsTable) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        List<List<String>> conditionsRows = conditionsTable.asLists();
        int counter = 0;
        for (CorePage block : blocksList) {
            counter++;
            for (List<String> conditionsRow : conditionsRows) {
                String elementName = conditionsRow.get(0);
                String textCondition = conditionsRow.get(1);
                String expectedText = resolveVars(getPropertyOrStringVariableOrValue(conditionsRow.get(2)));

                if (counter == 0) {
                    block.getElement(elementName).shouldHave(getSelenideCondition(textCondition, expectedText));
                } else {
                    block.getElement(elementName).shouldHave(getSelenideCondition(textCondition, expectedText), Duration.ZERO);
                }
            }
        }
        return new BlockListStepResult(blocksList, conditionsRows.stream().map(conditionsRow -> conditionsRow.get(0)).collect(Collectors.toList()));
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
     * @param blockListName   Название списка блоков
     * @param conditionsTable Список проверяемых условий в блоке
     *                        пример:
     *                        |<Название элемента 1>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     *                        ...
     *                        |<Название элемента N>|(текст равен|текст содержит|текст в формате|отображается на странице|не отображается на странице|не существует на странице|изображение загрузилось)|<Имя переменной/Имя свойства/Ожидаемый текст/Регулярное выражение>|
     */
    @И("^в списке блоков \"([^\"]*)\" любой из блоков соответствует условиям$")
    public IStepResult checkBlockListForComplexCondition(String blockListName, DataTable conditionsTable) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> resultList = getBlockListWithComplexCondition(blocksList, conditionsTable);

        return new BlockListStepResult(resultList,
                conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
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
        List<CorePage> blocksList;
        AssertionError error = new AssertionError();
        List<CorePage> resultList;
        long time = System.currentTimeMillis() + Configuration.timeout;

        do {
            blocksList = this.coreScenario.getCurrentPage().getBlock(blockName).getBlocksList(blockListName);
            try {
                resultList = getBlockListWithComplexCondition(blocksList, conditionsTable);
                if (resultList.size() > 0) {
                    return new BlockListStepResult(resultList,
                            conditionsTable.asLists().stream().map(conditionRow -> conditionRow.get(0)).collect(Collectors.toList()));
                }
            } catch (AssertionError e) {
                error = e;
            }
        } while (time > System.currentTimeMillis());
        throw error;
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст равен \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockListName, String elementName, String expectedText) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePage = findCorePageByTextInElement(blocksList, elementName, expectedText);
        return new BlockListStepResult(corePage, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст равен \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePage = findCorePageByTextInElement(blocksList, elementName, expectedText);
        return new BlockListStepResult(corePage, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkByRegExpInElementInAnyBlock(String blockListName, String elementName, String expectedText) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePage = findCorePageByRegExpInElement(blocksList, elementName, expectedText);
        return new BlockListStepResult(corePage, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkByRegExpInElementInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePage = findCorePageByRegExpInElement(blocksList, elementName, expectedText);
        return new BlockListStepResult(corePage, elementName);
    }


    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит$")
    @И("^в списке блоков \"([^\"]*)\" в любом из блоков в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    public IStepResult checkContainTextInAnyBlock(String blockListName, String elementName, String expectedText) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePage = findCorePageByTextContainInElement(blocksList, elementName, expectedText);
        return new BlockListStepResult(corePage, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков \"([^\"]*)\" в элементе \"([^\"]*)\" текст содержит$")
    @То("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в любом из блоков \"([^\"]*)\" в элементе \"([^\"]*)\" текст содержит \"([^\"]*)\"$")
    public IStepResult checkContainTextInAnyBlock(String blockName, String blockListName, String elementName, String expectedText) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePage = findCorePageByTextContainInElement(blocksList, elementName, expectedText);
        return new BlockListStepResult(corePage, elementName);
    }

    /**
     * -----------------------------------------------В КАКОМ-ТО КОЛИЧЕСТВЕ------------------------------------------------
     */

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockListName, int blockNumber, String elementName, String regExp) {
        regExp = getPropertyOrStringVariableOrValue(regExp);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> filterListBlock = new ArrayList<>();

        for (CorePage block : blocksList) {
            if (checkTextMatches(block, elementName, regExp)) {
                filterListBlock.add(block);
            }
        }

        Assert.assertEquals(filterListBlock.size(), blockNumber,
                "Условия поиска: " +
                        "\nЭлемент '" + elementName + "' содержит текст в формате : '" + regExp + "'" +
                        "\nОжидаемое количество блоков: " + blockNumber +
                        "\nАктуальное количество блоков: " + filterListBlock.size()
        );

        return new BlockListStepResult(filterListBlock, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInBlockListMatches(String blockName, String blockListName, int blockNumber, String elementName, String regExp) {
        regExp = getPropertyOrStringVariableOrValue(regExp);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> filterListBlock = new ArrayList<>();

        for (CorePage block : blocksList) {
            if (checkTextMatches(block, elementName, regExp)) {
                filterListBlock.add(block);
            }
        }

        Assert.assertEquals(filterListBlock.size(), blockNumber,
                "Условия поиска: " +
                        "\nЭлемент '" + elementName + "' содержит текст в формате : '" + regExp + "'" +
                        "\nОжидаемое количество блоков: " + blockNumber +
                        "\nАктуальное количество блоков: " + filterListBlock.size()
        );

        return new BlockListStepResult(filterListBlock, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockListName, int blockNumber, String elementName) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> filterListBlock = new ArrayList<>();

        for (CorePage block : blocksList) {
            if (block.getElement(elementName).is(Condition.visible)) {
                filterListBlock.add(block);
            }
        }

        Assert.assertEquals(filterListBlock.size(), blockNumber,
                "Условия поиска: " +
                        "\nЭлемент '" + elementName + "' отображается в блоке" +
                        "\nОжидаемое количество блоков: " + blockNumber +
                        "\nАктуальное количество блоков: " + filterListBlock.size()
        );

        return new BlockListStepResult(filterListBlock, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоках элемент \"([^\"]*)\" отображается на странице$")
    public IStepResult elementVisibleInBlockList(String blockName, String blockListName, int blockNumber, String elementName) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        List<CorePage> filterListBlock = new ArrayList<>();

        for (CorePage block : blocksList) {
            if (block.getElement(elementName).is(Condition.visible)) {
                filterListBlock.add(block);
            }
        }

        Assert.assertEquals(filterListBlock.size(), blockNumber,
                "Условия поиска: " +
                        "\nЭлемент '" + elementName + "' отображается в блоке" +
                        "\nОжидаемое количество блоков: " + blockNumber +
                        "\nАктуальное количество блоков: " + filterListBlock.size()
        );

        return new BlockListStepResult(filterListBlock, elementName);
    }


    /**
     * -----------------------------------------------В КОНКРЕТНОМ------------------------------------------------
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult elementDisplayedInBlockWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        corePageByTextInElement.getElement(elementNameSearch).shouldBe(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult elementDisplayedInBlockWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        corePageByTextInElement.getElement(elementNameSearch).shouldBe(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" не отображается$")
    public IStepResult elementNotDisplayedInBlockWhereTextEquals(String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        corePageByTextInElement.getElement(elementNameSearch).shouldNot(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" не отображается$")
    public IStepResult elementNotDisplayedInBlockWhereTextEquals(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        corePageByTextInElement.getElement(elementNameSearch).shouldBe(not(Condition.visible));
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult checkTextInAnyBlockMatches(String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByRegExpInElement(blocksList, elementNameSearch, expectedTextSearch);
        corePageByTextInElement.getElement(elementNameSearch).shouldBe(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" отображается$")
    public IStepResult checkTextInAnyBlockMatches(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String expectedElementVisible) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByRegExpInElement(blocksList, elementNameSearch, expectedTextSearch);
        corePageByTextInElement.getElement(elementNameSearch).shouldBe(Condition.visible);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, expectedElementVisible);
    }
    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);
        expectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        shouldHaveTextMatches(corePageByTextInElement, elementNameFind, expectedTextFind);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlock(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);
        expectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        shouldHaveTextMatches(corePageByTextInElement, elementNameFind, expectedTextFind);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlockMatches1(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);
        expectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByRegExpInElement(blocksList, elementNameSearch, expectedTextSearch);

        SelenideElement element = corePageByTextInElement.getElement(elementNameFind);
        element.shouldHave(Condition.matchText(getPropertyOrStringVariableOrValue(expectedTextFind)), Duration.ZERO);

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\" элемент \"([^\"]*)\" содержит текст в формате \"([^\"]*)\"$")
    public IStepResult checkTextInAnyBlockMatches1(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String expectedTextFind) {
        expectedTextSearch = getPropertyOrStringVariableOrValue(expectedTextSearch);
        expectedTextFind = getPropertyOrStringVariableOrValue(expectedTextFind);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByRegExpInElement(blocksList, elementNameSearch, expectedTextSearch);

        SelenideElement element = corePageByTextInElement.getElement(elementNameFind);
        element.shouldHave(Condition.matchText(getPropertyOrStringVariableOrValue(expectedTextFind)), Duration.ZERO);

        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"")
    public IStepResult checkCssInAnyBlock(String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String cssName, String cssValue) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);
        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = corePageByTextInElement.getElement(elementNameFind);
        element.shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"")
    public IStepResult checkCssInAnyBlock(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String elementNameFind, String cssName, String cssValue) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);
        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = corePageByTextInElement.getElement(elementNameFind);
        element.shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch, elementNameFind);
    }

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" блок расположен (в|вне) видимой части браузера")
    public IStepResult checkBlockWithTextInElementInBounds(String blockListName, String elementNameSearch, String expectedTextSearch, String boundsCondition) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        inBounds(corePageByTextInElement.getSelf(), boundsCondition);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" блок расположен (в|вне) видимой части браузера")
    public IStepResult checkBlockWithTextInElementInBounds(String blockName, String blockListName, String elementNameSearch, String expectedTextSearch, String boundsCondition) {
        expectedTextSearch = OtherSteps.getPropertyOrStringVariableOrValue(expectedTextSearch);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementNameSearch, expectedTextSearch);

        inBounds(corePageByTextInElement.getSelf(), boundsCondition);
        return new BlockListStepResult(corePageByTextInElement, elementNameSearch);
    }

    /**
     * ######################################################################################################################
     */

    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" координаты (\\d+) блока соответствуют: x=(\\d+); y=(\\d+)$")
    public IStepResult checkBlockListItemCoordinates(String blockListName, int blockIndex, int x, int y) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);
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
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);
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
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);
        SelenideElement element = block.getElement(elementName);

        this.coreScenario.getEnvironment().setVar(varName, element.shouldBe(Condition.visible).getText());

        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке текст элемента \"([^\"]*)\" сохранен в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextForNthBlockFromBlockList(String blockName, String blockListName, int blockIndex, String elementName, String varName) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);
        SelenideElement element = block.getElement(elementName);

        this.coreScenario.getEnvironment().setVar(varName, element.shouldBe(Condition.visible).getText());

        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListMatchesText(String blockListName, int blockIndex, String elementName, String expectedText) {
        expectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        shouldHaveTextMatches(block, elementName, expectedText);

        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст соответствует регулярному выражению \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListMatchesText(String blockName, String blockListName, int blockIndex, String elementName, String expectedText) {
        expectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);

        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        shouldHaveTextMatches(block, elementName, expectedText);

        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForText(String blockListName, int blockIndex, String elementName, String conditionString, String expectedText) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        Condition condition = null;
        expectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);
        switch (conditionString) {
            case "равен":
                condition = Condition.exactText(expectedText);
                break;
            case "содержит":
                condition = Condition.text(expectedText);
                break;
        }

        block.getElement(elementName).should(condition);
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке в элементе \"([^\"]*)\" текст (равен|содержит) \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForText(String blockName, String blockListName, int blockIndex, String elementName, String conditionString, String expectedText) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        Condition condition = null;
        expectedText = OtherSteps.getPropertyOrStringVariableOrValue(expectedText);
        switch (conditionString) {
            case "равен":
                condition = Condition.exactText(expectedText);
                break;
            case "содержит":
                condition = Condition.text(expectedText);
                break;
        }

        block.getElement(elementName).should(condition);
        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForCss(String blockListName, int blockIndex, String elementName, String cssName, String cssValue) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит css \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForCss(String blockName, String blockListName, int blockIndex, String elementName, String cssName, String cssValue) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        cssName = OtherSteps.getPropertyOrStringVariableOrValue(cssName);
        cssValue = OtherSteps.getPropertyOrStringVariableOrValue(cssValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.cssValue(cssName, cssValue));
        return new BlockListStepResult(block, elementName);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForAttribute(String blockListName, int blockIndex, String elementName, String attributeName, String attributeValue) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        attributeName = OtherSteps.getPropertyOrStringVariableOrValue(attributeName);
        attributeValue = OtherSteps.getPropertyOrStringVariableOrValue(attributeValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.attributeMatching(attributeName, attributeValue));
        return new BlockListStepResult(block, elementName);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" в (\\d+) блоке элемент \"([^\"]*)\" содержит атрибут \"([^\"]*)\" со значением \"([^\"]*)\"$")
    public IStepResult checkElementInBlockListForAttribute(String blockName, String blockListName, int blockIndex, String elementName, String attributeName, String attributeValue) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        attributeName = OtherSteps.getPropertyOrStringVariableOrValue(attributeName);
        attributeValue = OtherSteps.getPropertyOrStringVariableOrValue(attributeValue);

        SelenideElement element = block.getElement(elementName);
        element.shouldHave(Condition.attributeMatching(attributeName, attributeValue));
        return new BlockListStepResult(block, elementName);
    }


    /**
     * ######################################################################################################################
     */


    @SuppressWarnings("deprecation")
    @И("^в списке блоков \"([^\"]*)\" (\\d+) блок расположен (в|вне) видимой части браузера$")
    public IStepResult checkBlockListItemInBounds(String blockListName, int blockIndex, String boundsCondition) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        inBounds(block.getSelf(), boundsCondition);
        return new BlockListStepResult(block);
    }

    @SuppressWarnings("deprecation")
    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" (\\d+) блок расположен (в|вне) видимой части браузера$")
    public IStepResult checkBlockListItemInBounds(String blockName, String blockListName, int blockIndex, String boundsCondition) {
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage block = blocksList.get(blockIndex - 1);

        inBounds(block.getSelf(), boundsCondition);
        return new BlockListStepResult(block);
    }

    /**
     * ######################################################################################################################
     */

    @И("^в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено сохранение текста элемента \"([^\"]*)\" в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextToVarInBlockListWhereTextEquals(String blockListName, String elementToCheckText, String expectedText, String elementToSaveText, String varName) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementToCheckText, expectedText);

        SelenideElement element = corePageByTextInElement.getElement(elementToSaveText);
        CoreScenario.getInstance().getEnvironment().setVar(varName, element.getText());
        return new BlockListStepResult(corePageByTextInElement, elementToCheckText, elementToSaveText);
    }

    @И("^в блоке \"([^\"]*)\" в списке блоков \"([^\"]*)\" где в элементе \"([^\"]*)\" текст равен \"([^\"]*)\" выполнено сохранение текста элемента \"([^\"]*)\" в переменную \"([^\"]*)\"$")
    public IStepResult saveElementTextToVarInBlockListWhereTextEquals(String blockName, String blockListName, String elementToCheckText, String expectedText, String elementToSaveText, String varName) {
        expectedText = getPropertyOrStringVariableOrValue(expectedText);
        List<CorePage> blocksList =
                getBlockListWithCheckingTheQuantity(blockName, blockListName, CustomCondition.Comparison.more, 0);

        CorePage corePageByTextInElement = findCorePageByTextInElement(blocksList, elementToCheckText, expectedText);

        SelenideElement element = corePageByTextInElement.getElement(elementToSaveText);
        CoreScenario.getInstance().getEnvironment().setVar(varName, element.getText());
        return new BlockListStepResult(corePageByTextInElement, elementToCheckText, elementToSaveText);
    }

    /**
     * ######################################################################################################################
     */

}
