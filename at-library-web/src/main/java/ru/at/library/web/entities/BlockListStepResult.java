package ru.at.library.web.entities;

import org.openqa.selenium.WebElement;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.IStepResult;

import java.util.*;

import static ru.at.library.web.scenario.CastToWebElements.getBlockListAsWebElementsList;
import static ru.at.library.web.scenario.CastToWebElements.tryGetWebElement;

/**
 * Класс используемый для выделения элементов на скриншоте страницы в результате выполения
 * шагов с проверкой списка блоков или блока {@link CorePage}
 * <p>
 * Если в результате выполнения cucumber шага в котором есть взаимодействие с блоком/списком блоков
 * необходимо выбелить на скриншоте страницы определенные элементы - этот шаг должен возвращать объект
 * реализующий интерфейс {@link IStepResult}
 */
public class BlockListStepResult implements IStepResult {

    private final List<CorePage> blockList = new ArrayList<>();
    private final Set<String> blockElements = new HashSet<>();

    /**
     * Выделение на скриншоте страницы списка блоков {@link CorePage} (без внутренних элементов)
     *
     * @param blockList список блоков {@link CorePage} для выделения на скриншоте страницы
     */
    public BlockListStepResult(List<CorePage> blockList) {
        this.blockList.addAll(blockList);
    }

    /**
     * Выделение на скриншоте страницы блока {@link CorePage} (без внутренних элементов)
     *
     * @param block блок {@link CorePage} для выделения на скриншоте страницы
     */
    public BlockListStepResult(CorePage block) {
        this.blockList.add(block);
    }

    /**
     * Выделение на скриншоте страницы списка блоков {@link CorePage} с заданными списком внутренних элементами
     *
     * @param blockList     список блоков {@link CorePage} для выделения на скриншоте страницы
     * @param elementsNames список имен элементов для выделения в каждом блоке
     */
    public BlockListStepResult(List<CorePage> blockList, List<String> elementsNames) {
        this(blockList);
        this.blockElements.addAll(elementsNames);
    }

    /**
     * Выделение на скриншоте страницы списка блоков {@link CorePage} с одним или несколькими внутренними элементами
     *
     * @param blockList     список блоков {@link CorePage} для выделения на скриншоте страницы
     * @param elementsNames один или несколько имен элементов для выделения в каждом блоке
     */
    public BlockListStepResult(List<CorePage> blockList, String... elementsNames) {
        this(blockList);
        this.blockElements.addAll(Arrays.asList(elementsNames));
    }

    /**
     * Выделение на скриншоте страницы блока {@link CorePage} с одним или несколькими внутренними элементами
     *
     * @param block         блок {@link CorePage} для выделения на скриншоте страницы
     * @param elementsNames один или несколько имен элементов для выделения в блоке
     */
    public BlockListStepResult(CorePage block, String... elementsNames) {
        this(block);
        this.blockElements.addAll(Arrays.asList(elementsNames));
    }

    @Override
    public Optional<List<WebElement>> getWebElements() {
        List<WebElement> resultList = new ArrayList<>();
        getBlockListAsWebElementsList(this.blockList).ifPresent(list -> {
            resultList.addAll(list);
            blockList.forEach(block ->
                    blockElements.forEach(elementName ->
                            tryGetWebElement(block.getElement(elementName)).ifPresent(resultList::add)));
        });
        return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList);
    }
}
