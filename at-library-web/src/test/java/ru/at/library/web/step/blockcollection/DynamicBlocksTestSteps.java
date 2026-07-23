package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.ru.И;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.WebScenario;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private void assertFound(CorePage block, String operation) {
        if (block == null) {
            throw new AssertionError(operation + " не вернул блок");
        }
    }
}
