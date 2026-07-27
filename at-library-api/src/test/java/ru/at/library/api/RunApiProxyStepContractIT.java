package ru.at.library.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Запускается отдельно от параллельного API-набора, потому что ProxySteps
 * изменяет глобальное состояние RestAssured и системные свойства JVM.
 */
@CucumberOptions(
        monochrome = true,
        plugin = {"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        glue = {"ru"},
        features = {"src/test/resources/local-contract/api_proxy_steps_local_contract.feature"}
)
public class RunApiProxyStepContractIT extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
