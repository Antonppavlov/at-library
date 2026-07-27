package ru.at.library.web;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Проверяет, что примеры использования публичных Cucumber-шагов web-модуля
 * однозначно сопоставляются со step-definition. Браузер и hooks не запускаются.
 */
@CucumberOptions(
        dryRun = true,
        monochrome = true,
        tags = "@unit or @manual",
        features = "src/test/resources/features",
        glue = {"ru"}
)
public class RunWebStepCatalogTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
