package ru.at.library.web;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Проверяет, что все шаги в @unit-сценариях web-модуля однозначно сопоставляются
 * со step-definition (нет неоднозначных/непокрытых формулировок). Браузер и hooks
 * не запускаются — быстрая проверка регулярок без реального прогона.
 */
@CucumberOptions(
        dryRun = true,
        monochrome = true,
        tags = "@unit",
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
