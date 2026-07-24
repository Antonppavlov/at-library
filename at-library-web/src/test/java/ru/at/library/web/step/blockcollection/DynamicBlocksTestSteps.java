package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.WebScenario;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static com.codeborne.selenide.Selenide.$;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.findCorePageByRegExpInElement;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.findCorePageByTextContainInElement;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.findCorePageByTextInElement;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.findCorePageByVisibleElement;
import static ru.at.library.web.step.blockcollection.BlocksCollectionOtherMethod.getBlockListWithComplexCondition;

/**
 * Локальная страница для воспроизведения перерисовки DOM без внешних сервисов.
 */
public class DynamicBlocksTestSteps {

    @И("^открыта стабильная тестовая страница со списками блоков$")
    public void openStableBlocksPage() {
        Selenide.open("about:blank");
        Selenide.executeJavaScript("""
                document.body.style.margin = '0';
                document.body.dataset.clickCount = '0';

                window.__dynamicBlockMarkup = (className, index, name) => `
                    <div class="${className}"
                         data-index="${index}"
                         style="position:absolute;left:${(index - 1) * 200}px;top:0;width:180px;height:130px;display:block"
                         onclick="document.body.dataset.clicked = 'block-' + this.dataset.index;
                           document.body.dataset.clickCount =
                             String(Number(document.body.dataset.clickCount || 0) + 1)">
                      <span class="block-name" title="Префикс ${name} суффикс">${name}</span>
                      <button class="block-action"
                              data-code="action-${index}"
                              onclick="event.stopPropagation();
                                document.body.dataset.clicked = 'button-${index}';
                                document.body.dataset.clickCount =
                                  String(Number(document.body.dataset.clickCount || 0) + 1)">
                        Выбрать ${index}
                      </button>
                      <input class="block-input"
                             value="старое значение ${index}"
                             onclick="event.stopPropagation()">
                      <span class="block-description"
                            title="Префикс title-token-${index} суффикс"
                            onmouseover="document.body.dataset.hovered = '${index}'">
                        Описание ${name}
                      </span>
                      <span class="hidden-marker" style="display:none">Скрыто ${index}</span>
                      <img class="block-image"
                           alt="image-${index}"
                           src="data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==">
                    </div>`;

                document.body.innerHTML = `
                  <main>
                    <div id="blocks"
                         style="position:relative;margin-top:20px;width:600px;height:130px">
                      ${window.__dynamicBlockMarkup('dynamic-block', 1, 'Первый блок')}
                      ${window.__dynamicBlockMarkup('dynamic-block', 2, 'Второй блок')}
                      ${window.__dynamicBlockMarkup('dynamic-block', 3, 'Третий блок')}
                    </div>
                    <section id="dynamic-blocks-container"
                             style="position:relative;margin-top:40px;width:400px;height:130px">
                      <div id="nested-blocks"
                           style="position:relative;width:400px;height:130px">
                        ${window.__dynamicBlockMarkup('nested-dynamic-block', 1, 'Вложенный первый')}
                        ${window.__dynamicBlockMarkup('nested-dynamic-block', 2, 'Вложенный второй')}
                      </div>
                    </section>
                  </main>`;
                """);
    }

    @И("^открыта тестовая страница с перерисовкой списка во время scrollIntoView$")
    public void openPageWithRerenderDuringScroll() {
        openStableBlocksPage();
        Selenide.executeJavaScript("""
                window.__rerenderOnScroll = true;
                document.body.dataset.rerenders = '0';
                const originalScrollIntoView = Element.prototype.scrollIntoView;

                Element.prototype.scrollIntoView = function(options) {
                  if (window.__rerenderOnScroll && this.closest('#blocks')) {
                    window.__rerenderOnScroll = false;
                    const container = document.getElementById('blocks');
                    container.innerHTML =
                      window.__dynamicBlockMarkup('dynamic-block', 1, 'Первый блок') +
                      window.__dynamicBlockMarkup('dynamic-block', 2, 'Второй блок') +
                      window.__dynamicBlockMarkup('dynamic-block', 3, 'Третий блок');
                    document.body.dataset.rerenders = '1';
                  }
                  if (originalScrollIntoView) {
                    originalScrollIntoView.call(this, options);
                  }
                };
                """);
    }

    @И("^открыта тестовая страница с отложенной перерисовкой вложенного списка блоков$")
    public void openPageWithDelayedNestedBlocks() {
        openStableBlocksPage();
        Selenide.executeJavaScript("""
                const nested = document.getElementById('nested-blocks');
                nested.innerHTML =
                  window.__dynamicBlockMarkup('nested-dynamic-block', 1, 'Вложенная загрузка');

                window.setTimeout(() => {
                  nested.innerHTML =
                    window.__dynamicBlockMarkup('nested-dynamic-block', 1, 'Вложенный первый') +
                    window.__dynamicBlockMarkup('nested-dynamic-block', 2, 'Вложенный готов');
                }, 250);
                """);
    }

    @И("^открыта тестовая страница с постоянно перерисовываемым списком блоков$")
    public void openPageWithRerenderingBlocks() {
        Selenide.open("about:blank");
        Selenide.executeJavaScript("""
                document.body.innerHTML = '<main><div id="blocks"></div></main>';
                const container = document.getElementById('blocks');
                let renderNumber = 0;

                window.__dynamicBlocksTimer = window.setInterval(() => {
                    renderNumber += 1;
                    container.innerHTML =
                        '<div class="dynamic-block">' +
                        '<span class="block-name">Загрузка ' + renderNumber + '</span>' +
                        '<button class="block-action">Выбрать</button>' +
                        '</div>';

                    if (renderNumber >= 80) {
                        window.clearInterval(window.__dynamicBlocksTimer);
                        container.innerHTML =
                            '<div class="dynamic-block">' +
                            '<span class="block-name">Готовый блок</span>' +
                            '<button class="block-action" ' +
                            'onclick="document.body.dataset.clicked = \\'true\\'">Выбрать</button>' +
                            '</div>';
                    }
                }, 10);
                """);
    }

    @И("^открыта тестовая страница с отложенным вторым блоком$")
    public void openPageWithDelayedSecondBlock() {
        Selenide.open("about:blank");
        Selenide.executeJavaScript("""
                document.body.innerHTML =
                    '<main><div id="blocks">' +
                    '<div class="dynamic-block">' +
                    '<span class="block-name">Первый блок</span>' +
                    '<button class="block-action">Выбрать</button>' +
                    '</div>' +
                    '</div></main>';

                window.setTimeout(() => {
                    document.getElementById('blocks').insertAdjacentHTML(
                        'beforeend',
                        '<div class="dynamic-block">' +
                        '<span class="block-name">Второй блок</span>' +
                        '<button class="block-action" ' +
                        'onclick="document.body.dataset.clicked = \\'true\\'">Выбрать</button>' +
                        '</div>'
                    );
                }, 300);
                """);
    }

    @И("^открыта тестовая страница с добавлением второго блока после первой проверки$")
    public void openPageWithSecondBlockAddedAfterFirstCheck() {
        openStableBlocksPage();
        Selenide.executeJavaScript("""
                const blocks = document.getElementById('blocks');
                blocks.querySelectorAll('.dynamic-block:not(:first-child)')
                  .forEach(element => element.remove());

                const originalScrollIntoView = Element.prototype.scrollIntoView;
                window.__appendSecondBlockOnScroll = true;
                Element.prototype.scrollIntoView = function(options) {
                  if (window.__appendSecondBlockOnScroll && this.closest('#blocks')) {
                    window.__appendSecondBlockOnScroll = false;
                    blocks.insertAdjacentHTML(
                      'beforeend',
                      window.__dynamicBlockMarkup(
                        'dynamic-block',
                        2,
                        'Второй блок'
                      )
                    );
                  }
                  if (originalScrollIntoView) {
                    originalScrollIntoView.call(this, options);
                  }
                };
                """);
    }

    @И("^нажатие на динамический блок зарегистрировано$")
    public void dynamicBlockClickWasRegistered() {
        $("body").shouldHave(Condition.attribute("data-clicked", "true"));
    }

    @И("^зарегистрировано действие \"([^\"]*)\" ровно один раз$")
    public void dynamicActionWasRegisteredOnce(String action) {
        $("body")
                .shouldHave(Condition.attribute("data-clicked", action))
                .shouldHave(Condition.attribute("data-click-count", "1"));
    }

    @И("^список был перерисован во время прокрутки$")
    public void listWasRerenderedDuringScroll() {
        $("body").shouldHave(Condition.attribute("data-rerenders", "1"));
    }

    @И("^наведение зарегистрировано для блока (\\d+)$")
    public void hoverWasRegistered(int blockNumber) {
        $("body").shouldHave(Condition.attribute("data-hovered", String.valueOf(blockNumber)));
    }

    @И("^в поле блока (\\d+) установлено значение \"([^\"]*)\"$")
    public void blockInputHasValue(int blockNumber, String expectedValue) {
        $("#blocks > .dynamic-block", blockNumber - 1)
                .$(".block-input")
                .shouldHave(Condition.value(expectedValue));
    }

    @И("^в поле вложенного блока (\\d+) установлено значение \"([^\"]*)\"$")
    public void nestedBlockInputHasValue(int blockNumber, String expectedValue) {
        $("#nested-blocks > .nested-dynamic-block", blockNumber - 1)
                .$(".block-input")
                .shouldHave(Condition.value(expectedValue));
    }

    @И("^snapshot списка остаётся рабочим после полной замены DOM$")
    public void snapshotRemainsUsableAfterDomReplacement(DataTable conditions) {
        List<CorePage> snapshot = WebScenario.getCurrentPage().getBlocksList("Динамические блоки");

        Selenide.executeJavaScript("""
                document.getElementById('blocks').innerHTML =
                  window.__dynamicBlockMarkup('dynamic-block', 1, 'Снимок первый') +
                  window.__dynamicBlockMarkup('dynamic-block', 2, 'Снимок второй') +
                  window.__dynamicBlockMarkup('dynamic-block', 3, 'Снимок третий');
                """);

        assertFound(
                findCorePageByVisibleElement(snapshot, "Кнопка динамического блока"),
                "Поиск видимого элемента"
        );
        assertFound(
                findCorePageByTextInElement(
                        snapshot,
                        "Название динамического блока",
                        "Снимок второй"
                ),
                "Поиск по точному тексту"
        );
        assertFound(
                findCorePageByTextContainInElement(
                        snapshot,
                        "Название динамического блока",
                        "второй"
                ),
                "Поиск по части текста"
        );
        assertFound(
                findCorePageByRegExpInElement(
                        snapshot,
                        "Название динамического блока",
                        "Снимок\\s+второй"
                ),
                "Поиск по регулярному выражению"
        );

        List<CorePage> filtered = getBlockListWithComplexCondition(snapshot, conditions);
        if (filtered.size() != 1) {
            throw new AssertionError(
                    "Snapshot-фильтр должен вернуть один блок, получено: " + filtered.size()
            );
        }
    }

    @И("^нулевой номер блока отклоняется без ожидания таймаута$")
    public void zeroBlockNumberIsRejectedImmediately() {
        long startedNanos = System.nanoTime();
        IllegalArgumentException expectedFailure = null;

        try {
            new BlocksCollectionActionSteps().clickOnElementBlockInBlockList(
                    "Динамические блоки",
                    0,
                    "Кнопка динамического блока"
            );
        } catch (IllegalArgumentException error) {
            expectedFailure = error;
        }

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
        if (expectedFailure == null) {
            throw new AssertionError("Нулевой номер блока неожиданно принят");
        }
        if (!expectedFailure.getMessage().contains("начинаться с 1")) {
            throw new AssertionError("Ошибка не объясняет допустимую нумерацию", expectedFailure);
        }
        if (elapsedMs > 300L) {
            throw new AssertionError(
                    "Проверка номера блока выполнялась слишком долго: " + elapsedMs + " мс",
                    expectedFailure
            );
        }
    }

    @И("^проверка \"текст не равен\" дожидается изменения равного текста$")
    public void notEqualCheckWaitsForEqualTextToChange() {
        long originalTimeout = Configuration.timeout;
        Boolean textChanged;

        try {
            Configuration.timeout = 2_000L;
            Selenide.executeJavaScript("""
                    document.body.dataset.notEqualReady = 'false';
                    window.setTimeout(() => {
                      const name = document.querySelectorAll(
                        '#blocks > .dynamic-block .block-name'
                      )[1];
                      name.textContent = 'Изменённый второй блок';
                      name.title = 'Префикс Изменённый второй блок суффикс';
                      document.body.dataset.notEqualReady = 'true';
                    }, 800);
                    """);

            new BlocksCollectionCheckSteps().checkNotTextInBlockListMatches(
                    "Динамические блоки",
                    "Название динамического блока",
                    "Второй блок"
            );

            textChanged = Selenide.executeJavaScript(
                    "return document.body.dataset.notEqualReady === 'true';"
            );
        } finally {
            Configuration.timeout = originalTimeout;
        }

        if (!Boolean.TRUE.equals(textChanged)) {
            throw new AssertionError(
                    "Проверка завершилась до того, как равный текст действительно изменился"
            );
        }
    }

    @И("^условие \"текст не содержит\" учитывает подстроку в атрибуте title$")
    public void notContainsCheckUsesTitleSubstring(DataTable conditions) {
        List<CorePage> matches = getBlockListWithComplexCondition(
                BlockListContext.live("Динамические блоки"),
                conditions
        );

        if (matches.size() != 2) {
            throw new AssertionError(
                    "Условие должно исключить только блок с title-token-1, найдено: " +
                            matches.size()
            );
        }
        for (CorePage match : matches) {
            String title = match.getElement("Описание динамического блока")
                    .getAttribute("title");
            if (title != null && title.contains("title-token-1")) {
                throw new AssertionError("В результат попал блок с запрещённой подстрокой: " + title);
            }
        }
    }

    @И("^поиск отсутствующего динамического блока завершается по общему таймауту$")
    public void missingDynamicBlockSearchStopsAtDeadline() {
        long originalTimeout = Configuration.timeout;
        long startedNanos = System.nanoTime();
        AssertionError expectedFailure = null;

        try {
            Configuration.timeout = 500L;
            new BlocksCollectionActionSteps().clickButtonInBlockListWhereTextEquals(
                    "Динамические блоки",
                    "Название динамического блока",
                    "Отсутствующий блок",
                    "Кнопка динамического блока"
            );
        } catch (AssertionError error) {
            expectedFailure = error;
        } finally {
            Configuration.timeout = originalTimeout;
        }

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
        if (expectedFailure == null) {
            throw new AssertionError("Поиск отсутствующего блока неожиданно завершился успешно");
        }
        if (!expectedFailure.getMessage().contains("Timeout: 500 мс")) {
            throw new AssertionError("Ошибка не содержит настроенный общий таймаут", expectedFailure);
        }
        if (elapsedMs > 2_000L) {
            throw new AssertionError(
                    "Поиск с таймаутом 500 мс выполнялся слишком долго: " + elapsedMs + " мс",
                    expectedFailure
            );
        }
    }

    @И("^поиск блока с повторными попытками не создаёт технических Selenide-шагов$")
    public void retrySearchDoesNotEmitTechnicalSelenideSteps() {
        assertNoTechnicalSelenideEvents(() ->
                new BlocksCollectionActionSteps().clickButtonInBlockListWhereTextEquals(
                        "Динамические блоки",
                        "Название динамического блока",
                        "Второй блок",
                        "Кнопка динамического блока"
                )
        );
        assertSearchReportStructure(false);
    }

    @И("^поиск блока при перерисовке DOM не создаёт технических Selenide-шагов$")
    public void staleRetryDoesNotEmitTechnicalSelenideSteps() {
        assertNoTechnicalSelenideEvents(() ->
                new BlocksCollectionActionSteps().clickButtonInBlockListWhereTextEquals(
                        "Динамические блоки",
                        "Название динамического блока",
                        "Второй блок",
                        "Кнопка динамического блока"
                )
        );
        assertSearchReportStructure(true);
    }

    @И("^проверка каждого блока не создаёт технических Selenide-шагов$")
    public void everyBlockCheckDoesNotEmitTechnicalSelenideSteps() {
        assertNoTechnicalSelenideEvents(() ->
                new BlocksCollectionCheckSteps().checkNotTextInBlockListMatches(
                        "Динамические блоки",
                        "Название динамического блока",
                        "Отсутствующий текст"
                )
        );
        assertEveryBlockReportStructure();
    }

    private void assertNoTechnicalSelenideEvents(Runnable blockOperation) {
        String listenerName = "AllureSelenide";
        LogEventListener previousListener =
                SelenideLogger.removeListener(listenerName);
        RecordingListener listener = new RecordingListener();
        SelenideLogger.addListener(listenerName, listener);
        try {
            blockOperation.run();
            if (listener.events() != 0) {
                throw new AssertionError(
                        "Внутри blockcollection зарегистрировано технических Selenide-событий: " +
                                listener.events()
                );
            }

            SelenideLogger.run("control", "после blockcollection", () -> {
            });
            if (listener.events() != 2) {
                throw new AssertionError(
                        "Selenide listener не восстановлен после blockcollection"
                );
            }
        } finally {
            SelenideLogger.removeListener(listenerName);
            if (previousListener != null) {
                SelenideLogger.addListener(listenerName, previousListener);
            }
        }
    }

    private void assertSearchReportStructure(boolean requireDomUpdated) {
        List<StepResult> roots = currentAllureSteps();
        List<StepResult> attempts = findSteps(
                roots,
                step -> step.getName().startsWith("Попытка №")
        );
        if (attempts.size() < 2) {
            throw new AssertionError(
                    "Ожидалось минимум две попытки поиска, фактически: " +
                            attempts.size() + "\nAllure-шаги: " + stepNames(roots)
            );
        }

        List<StepResult> reportedBlocks = new ArrayList<>();
        for (StepResult attempt : attempts) {
            List<StepResult> blocks = attempt.getSteps().stream()
                    .filter(step -> step.getName().startsWith("Блок №"))
                    .toList();
            boolean listUpdatedBeforeCheck =
                    attempt.getName().contains("список обновился до проверки");
            if ((!listUpdatedBeforeCheck && blocks.isEmpty())
                    || blocks.stream().anyMatch(step ->
                    step.getStatus() != Status.PASSED
                            || !hasParameter(step, "Ожидается")
                            || !hasParameter(step, "Фактически"))) {
                throw new AssertionError(
                    "Каждая попытка должна содержать успешные детальные шаги блоков" +
                                "\nПопытка: " + attempt.getName() +
                                "\nAllure-шаги: " + stepNames(roots)
                );
            }
            reportedBlocks.addAll(blocks);
        }

        List<StepResult> actions = findSteps(
                reportedBlocks,
                step -> step.getName().startsWith("Действие с найденным блоком №")
                        && step.getName().endsWith("— ВЫПОЛНЕНО")
        );
        if (actions.isEmpty()) {
            throw new AssertionError(
                    "В успешном блоке отсутствует вложенное действие" +
                            "\nAllure-шаги: " + stepNames(roots)
            );
        }
        if (requireDomUpdated && findSteps(
                attempts,
                step -> step.getName().startsWith("Блок №")
                        && step.getName().contains("DOM обновился")
        ).isEmpty()) {
            throw new AssertionError(
                    "В отчёте отсутствует повтор после обновления DOM" +
                            "\nAllure-шаги: " + stepNames(roots)
            );
        }
        assertNoFailedAllureSteps(roots);
    }

    private void assertEveryBlockReportStructure() {
        List<StepResult> roots = currentAllureSteps();
        List<StepResult> blockSteps = findSteps(
                roots,
                step -> step.getName().startsWith("Блок №")
        );
        if (blockSteps.size() != 3
                || blockSteps.stream().anyMatch(step ->
                step.getStatus() != Status.PASSED
                        || !hasParameter(step, "Ожидается")
                        || !hasParameter(step, "Фактически")
                        || !step.getName().endsWith("— ВЫПОЛНЕНО"))) {
            throw new AssertionError(
                    "Ожидались три детальных Allure-шага по блокам: " +
                            stepNames(blockSteps)
            );
        }
        assertNoFailedAllureSteps(roots);
    }

    private boolean hasParameter(StepResult step, String parameterName) {
        return step.getParameters().stream()
                .anyMatch(parameter -> parameterName.equals(parameter.getName())
                        && parameter.getValue() != null
                        && !parameter.getValue().isBlank());
    }

    private List<StepResult> currentAllureSteps() {
        String currentStep = Allure.getLifecycle()
                .getCurrentTestCaseOrStep()
                .orElseThrow(() -> new AssertionError(
                        "Не найден текущий Allure-шаг"
                ));
        List<StepResult> steps = new ArrayList<>();
        Allure.getLifecycle().updateStep(
                currentStep,
                result -> steps.addAll(result.getSteps())
        );
        return steps;
    }

    private List<StepResult> findSteps(List<StepResult> roots,
                                       Predicate<StepResult> predicate) {
        List<StepResult> result = new ArrayList<>();
        collectSteps(roots, predicate, result);
        return result;
    }

    private void collectSteps(List<StepResult> steps,
                              Predicate<StepResult> predicate,
                              List<StepResult> result) {
        for (StepResult step : steps) {
            if (predicate.test(step)) {
                result.add(step);
            }
            collectSteps(step.getSteps(), predicate, result);
        }
    }

    private List<String> stepNames(List<StepResult> steps) {
        List<String> names = new ArrayList<>();
        collectStepNames(steps, names);
        return names;
    }

    private void assertNoFailedAllureSteps(List<StepResult> roots) {
        List<StepResult> failed = findSteps(
                roots,
                step -> step.getStatus() == Status.FAILED
                        || step.getStatus() == Status.BROKEN
        );
        if (!failed.isEmpty()) {
            throw new AssertionError(
                    "В успешном цикле есть failed/broken Allure-шаги: " +
                            stepNames(failed)
            );
        }
    }

    private void collectStepNames(List<StepResult> steps, List<String> names) {
        for (StepResult step : steps) {
            names.add(step.getName());
            collectStepNames(step.getSteps(), names);
        }
    }

    private void assertFound(CorePage block, String operation) {
        if (block == null) {
            throw new AssertionError(operation + " не вернул блок");
        }
    }

    private static class RecordingListener implements LogEventListener {

        private final AtomicInteger events = new AtomicInteger();

        @Override
        public void afterEvent(LogEvent currentLog) {
            events.incrementAndGet();
        }

        @Override
        public void beforeEvent(LogEvent currentLog) {
            events.incrementAndGet();
        }

        private int events() {
            return events.get();
        }
    }
}
