package ru.at.library.web.step.blockcollection;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebElementCondition;

import java.util.regex.Pattern;

/**
 * Общие условия для элементов внутри блоков.
 */
final class BlockConditions {

    private BlockConditions() {
    }

    static WebElementCondition clickable() {
        return Condition.and("кликабелен", Condition.visible, Condition.enabled);
    }

    static WebElementCondition textEquals(String expectedText) {
        return Condition.or("текст элемента равен",
                Condition.exactText(expectedText),
                Condition.exactValue(expectedText),
                Condition.attribute("title", expectedText)
        );
    }

    static WebElementCondition textContains(String expectedText) {
        return Condition.or("текст элемента содержит",
                Condition.text(expectedText),
                Condition.value(expectedText),
                Condition.attributeMatching(
                        "title",
                        ".*" + Pattern.quote(expectedText) + ".*"
                )
        );
    }

    static WebElementCondition textMatches(String expectedText) {
        return Condition.or("текст элемента соответствует регулярному выражению",
                Condition.matchText(expectedText),
                Condition.attributeMatching("value", expectedText),
                Condition.attributeMatching("title", expectedText)
        );
    }
}
