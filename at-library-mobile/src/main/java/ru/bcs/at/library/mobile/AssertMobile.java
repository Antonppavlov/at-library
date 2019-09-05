package ru.bcs.at.library.mobile;

import org.junit.Assert;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import ru.bcs.at.library.core.cucumber.api.CoreScenario;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static ru.bcs.at.library.core.cucumber.api.CoreScenario.sleep;

public class AssertMobile {

    public static void expectedText(WebElement element, String expectedText, int second) {
        String actualText = null;
        for (int i = 0; i < second; i++) {
            actualText = element.getText();
            if (actualText.equals(expectedText)) {
                return;
            }
            sleep(1);
        }
        screenshot();
        Assert.assertEquals(expectedText, actualText);
    }

    public static void containsText(WebElement element, String containText, int second) {
        String actualText = null;
        for (int i = 0; i < second; i++) {
            actualText = element.getText();
            if (actualText.contains(containText)) {
                return;
            }
            sleep(1);
        }
        screenshot();
        Assert.fail("Текс: " + actualText + " не содержит: " + containText);
    }

    public static void display(WebElement element, boolean expectedDisplayed, int second) {
        boolean actualDisplayed = !expectedDisplayed;
        for (int i = 0; i < second; i++) {
            actualDisplayed = element.isDisplayed();
            if (actualDisplayed == expectedDisplayed) {
                return;
            }
            sleep(1);
        }
        screenshot();
        Assert.fail("Displayed элемента должен быть:" + expectedDisplayed);
    }


    private static void screenshot() {
        final byte[] screenshot = ((TakesScreenshot) getWebDriver()).getScreenshotAs(OutputType.BYTES);
        CoreScenario.getInstance().getScenario().embed(screenshot, "image/png");
    }
}