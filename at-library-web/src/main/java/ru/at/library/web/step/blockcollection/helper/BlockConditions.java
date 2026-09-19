package ru.at.library.web.step.blockcollection.helper;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebElementCondition;

import java.util.regex.Pattern;

/**
 * Общие условия для элементов внутри блоков.
 */
public final class BlockConditions {

    private BlockConditions() {
    }

    public static WebElementCondition clickable() {
        return Condition.and("кликабелен", Condition.visible, Condition.enabled);
    }

    public static WebElementCondition textEquals(String expectedText) {
        return Condition.or("текст элемента равен",
                Condition.exactText(expectedText),
                Condition.exactValue(expectedText),
                Condition.attribute("title", expectedText)
        );
    }

    public static WebElementCondition textContains(String expectedText) {
        return Condition.or("текст элемента содержит",
                Condition.text(expectedText),
                Condition.value(expectedText),
                Condition.attributeMatching(
                        "title",
                        ".*" + Pattern.quote(expectedText) + ".*"
                )
        );
    }

    public static WebElementCondition textMatches(String expectedText) {
        return Condition.or("текст элемента соответствует регулярному выражению",
                Condition.matchText(expectedText),
                Condition.attributeMatching("value", expectedText),
                Condition.attributeMatching("title", expectedText)
        );
    }
}
