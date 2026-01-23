package ru.at.library.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.testng.annotations.DataProvider;
import ru.at.library.core.setup.CoreInitialSetup;

public class RunFeaturesTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        Object[][] scenarios = super.scenarios();
        CoreInitialSetup.totalScenarios = scenarios.length;
        return scenarios;
    }
}
