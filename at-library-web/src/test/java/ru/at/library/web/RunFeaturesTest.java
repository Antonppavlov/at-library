package ru.at.library.web;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        monochrome = true,
        plugin = {"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        tags = "@unit",
        features = "src/test/resources/features",
        glue = {"ru"}
)
public class RunFeaturesTest extends AbstractTestNGCucumberTests {
}
