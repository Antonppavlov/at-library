package ru.at.library.web;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Контракты для известных дефектов. Запускаются отдельно и должны стать зелёными,
 * когда переход в новую вкладку начнёт нажимать на элемент, а шаги двойного
 * нажатия начнут выполнять doubleClick.
 */
@CucumberOptions(
        monochrome = true,
        plugin = {"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        glue = {"ru"},
        features = {"src/test/resources/local-contract/web_known_gap_contract.feature"}
)
public class RunWebKnownGapContractIT extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
