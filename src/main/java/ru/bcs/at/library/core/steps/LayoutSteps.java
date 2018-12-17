package ru.bcs.at.library.core.steps;

import com.galenframework.api.Galen;
import com.galenframework.reports.model.LayoutReport;
import cucumber.api.java.ru.Тогда;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import ru.bcs.at.library.core.cucumber.api.CoreScenario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static org.junit.Assert.fail;
import static ru.bcs.at.library.core.core.helpers.PropertyLoader.loadSystemPropertyOrDefault;

public class LayoutSteps {
    public static final String SPECS_DIR_PATH = loadSystemPropertyOrDefault("specsDir",
            System.getProperty("user.dir") + "/src/test/resources/specs/");
    public static final String IMG_DIFF_PATH = loadSystemPropertyOrDefault("imgDiff",
            System.getProperty("user.dir") + "/build/results-img/");
    private CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Шаг проверяет, что текущая страница соответствует описанным в .spec файле требованиям
     *
     * @param spec - Название galen спецификации .spec, где описан ожидаемый дизайн страницы
     *             По умолчанию ожидается, что .spec файлы находятся по пути /src/test/resources/specs.
     *             Этот путь можно переопределить, задав системную переменную specsDir
     */

    @Тогда("(?:страница соответствует|соответствует|блок соответствует) ожидаемой спецификации \"([^\"]*)\"")
    public void compareCurrentPageWithBase(String spec) {
        checkLayoutAccordingToSpec(spec, null);
    }

    /**
     * Шаг проверяет, что текущая страница соответствует описанным в .spec файле требованиям
     *
     * @param spec - Название galen спецификации .spec, где описан ожидаемый дизайн страницы
     *             По умолчанию ожидается, что .spec файлы находятся по пути /src/test/resources/specs.
     *             Этот путь можно переопределить, задав системную переменную specsDir
     * @param tag  - название тэга в galen спецификации (например @on desktop),
     *             для которого описан дизайн конкретных элементов.
     */
    @Тогда("(?:страница соответствует|соответствует|блок соответствует) спецификации \"([^\"]*)\" для экрана \"(\\D+)\"")
    public void compareCurrentPageWithBase(String spec, String tag) {
        List<String> tags = new ArrayList<>();
        tags.add(tag);
        checkLayoutAccordingToSpec(spec, tags);
    }

    @SneakyThrows
    /**
     * Проверяет соответствие текущей страницы ее описанию в .spec файле.
     * Скриншоты с расходениями в дизайне сохраняются в /build/results-img/ и прикрепояются к cucumber отчету
     * Путь /build/results-img/ можно переопределить, задав системную переменную imgDiff
     */
    private void checkLayoutAccordingToSpec(String spec, List<String> tags) {
        LayoutReport report = Galen.checkLayout(getWebDriver(), SPECS_DIR_PATH + spec, tags);
        report.getFileStorage().copyAllFilesTo(new File(IMG_DIFF_PATH));
        if (report.errors() > 0) {
            embedScreenshotAndFail(report);
        }
    }

    private void embedScreenshotAndFail(LayoutReport report) {
        Map<String, File> screenshots = report.getFileStorage().getFiles();
        screenshots.forEach((key, value) -> {
            if (key.contains("map") || key.contains("expected") || key.contains("actual")) {
                coreScenario.write(key);
                embedFileToReport(value, "image/png");
            }
        });
        fail(report.getValidationErrorResults().toString());
    }

    /**
     * Прикрепляет файл к текущему сценарию в cucumber отчете
     *
     * @param fileName - название файла
     * @param mimeType - тип файла
     */
    @SneakyThrows
    public static void embedFileToReport(File fileName, String mimeType) {
        CoreScenario.getInstance().getScenario()
                .embed(FileUtils.readFileToByteArray(fileName), mimeType);
    }
}
