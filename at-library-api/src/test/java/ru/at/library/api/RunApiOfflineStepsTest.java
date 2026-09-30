package ru.at.library.api;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Оффлайн-контракты всех шагов API-модуля: без интернета и без публичного Petstore.
 * <p>
 * Ответы для проверок строятся в памяти ({@code ResponseBuilder}), а шаги отправки запросов работают
 * с локальным HTTP/HTTPS-сервером. Отрицательные случаи проверяются шагом
 * «выполнение шага завершается ошибкой, содержащей …».
 */
@CucumberOptions(
        monochrome = true,
        plugin = {"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"},
        glue = {"ru"},
        features = {"src/test/resources/offline"}
)
public class RunApiOfflineStepsTest extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
