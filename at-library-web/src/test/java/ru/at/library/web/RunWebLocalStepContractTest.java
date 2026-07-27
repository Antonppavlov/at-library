package ru.at.library.web;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        monochrome = true,
        plugin = {"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        glue = {"ru"},
        features = {"src/test/resources/local-contract/web_steps_local_contract.feature"}
)
public class RunWebLocalStepContractTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
