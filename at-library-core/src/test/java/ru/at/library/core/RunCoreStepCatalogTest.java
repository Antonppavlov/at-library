package ru.at.library.core;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Проверяет, что все публичные Cucumber-шаги core-модуля однозначно
 * сопоставляются со step-definition. Тела шагов и hooks не выполняются.
 */
@CucumberOptions(
        dryRun = true,
        monochrome = true,
        features = "src/test/resources/features/core_steps_catalog.feature",
        glue = {"ru"}
)
public class RunCoreStepCatalogTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
