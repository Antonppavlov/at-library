package ru.at.library.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Проверяет сопоставление всех публичных Cucumber-шагов API-модуля
 * со step-definition без выполнения HTTP-запросов и тел шагов.
 */
@CucumberOptions(
        dryRun = true,
        monochrome = true,
        features = "src/test/resources/step-catalog/api_steps_catalog.feature",
        glue = {"ru"}
)
public class RunApiStepCatalogTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
