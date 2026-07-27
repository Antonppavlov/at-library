package ru.at.library.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Отдельный контракт известного дефекта проверки непустого JSON-массива.
 */
@CucumberOptions(
        monochrome = true,
        plugin = {"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        glue = {"ru"},
        features = {"src/test/resources/local-contract/api_known_gap_contract.feature"}
)
public class RunApiKnownGapContractIT extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
