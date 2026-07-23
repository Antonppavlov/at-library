package ru.at.library.web;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Проверяет, что каждый пример из каталога blockcollection однозначно
 * сопоставляется с Cucumber step-definition. Браузер при этом не запускается.
 */
@CucumberOptions(
        dryRun = true,
        monochrome = true,
        features = "src/test/resources/features/blockcollection/blockcollection_steps_catalog.feature",
        glue = {"ru"}
)
public class RunBlockCollectionStepCatalogTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
