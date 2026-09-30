package ru.at.library.api;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.at.library.api.steps.ApiStepRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Мета-тест: у каждого публичного шага API-модуля должен быть хотя бы один настоящий сценарий,
 * в котором шаг вызывается как обычная строка feature (а не только внутри DocString отрицательной проверки).
 * <p>
 * Добавили шаг в {@code ru.at.library.api.steps} и не написали на него тест — этот тест упадёт
 * и перечислит шаги без покрытия.
 */
public class ApiStepCoverageTest {

    private static final List<String> FEATURE_DIRECTORIES = List.of(
            "src/test/resources/features",
            "src/test/resources/local-contract",
            "src/test/resources/offline"
    );

    /** Ключевые слова русского Gherkin, с которых начинается строка шага. */
    private static final Pattern STEP_KEYWORD = Pattern.compile("^(?:Дано|Когда|Тогда|И|Но|Пусть|Если|\\*)\\s+(.*)$");

    @Test(description = "Реестр шагов не пуст (иначе сканирование классов сломалось и покрытие проверять нечем)")
    public void registryFindsSteps() {
        Assert.assertTrue(ApiStepRegistry.all().size() >= 60,
                "Найдено слишком мало шагов: " + ApiStepRegistry.all().size());
    }

    @Test(description = "Каждый публичный шаг вызывается хотя бы в одном выполняемом feature")
    public void everyPublicStepHasARealScenario() throws IOException {
        List<String> stepLines = collectStepLines();
        Assert.assertFalse(stepLines.isEmpty(), "Не найдено ни одной строки шага в " + FEATURE_DIRECTORIES);

        List<String> uncovered = new ArrayList<>();
        for (ApiStepRegistry.StepDef step : ApiStepRegistry.all()) {
            boolean covered = stepLines.stream().anyMatch(line -> step.pattern().matcher(line).matches());
            if (!covered) {
                uncovered.add(step.title());
            }
        }
        Assert.assertTrue(uncovered.isEmpty(),
                "Шаги без сценария в " + FEATURE_DIRECTORIES + " (" + uncovered.size() + "):\n  " + String.join("\n  ", uncovered));
    }

    private static List<String> collectStepLines() throws IOException {
        List<String> result = new ArrayList<>();
        for (String directory : FEATURE_DIRECTORIES) {
            Path root = Path.of(System.getProperty("user.dir")).resolve(directory);
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".feature"))::iterator) {
                    for (String raw : Files.readAllLines(file)) {
                        String line = raw.strip();
                        if (line.startsWith("#")) {
                            continue;
                        }
                        var matcher = STEP_KEYWORD.matcher(line);
                        if (matcher.matches()) {
                            result.add(matcher.group(1));
                        }
                    }
                }
            }
        }
        return result;
    }
}
