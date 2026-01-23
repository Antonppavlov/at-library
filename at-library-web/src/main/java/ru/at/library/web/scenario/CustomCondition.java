package ru.at.library.web.scenario;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.WebElementsCondition;

import java.util.Arrays;

/**
 * Набор дополнительных условий и сравнений, используемых в шагах для проверки размеров коллекций
 * и простых числовых сравнений ("равно", "больше", "не равно" и т.п.).
 */
public class CustomCondition {
    public enum Comparison {
        equal("равно"),
        not_equal("не равно"),
        more("больше"),
        less("меньше"),
        more_or_equal("больше или равно"),
        less_or_equal("меньше или равно");

        public String name;

        Comparison(String name) {
        this.name = name;
        }

        @Override
        public String toString() {
        return name;
        }

        public static Comparison fromString(String text) {
        for (Comparison comparison : Comparison.values()) {
            if (comparison.name.equalsIgnoreCase(text)) {
                return comparison;
            }
        }
        throw new RuntimeException("Передаваемое условие: +" + text +
               "\nОтсутствует в списке возможных: " + Arrays.toString(values()));
        }
    }

    public static WebElementsCondition getElementsCollectionSizeCondition(Comparison comparison, int expectedSize) {
        WebElementsCondition collectionCondition;

        switch (comparison) {
            case equal:
                collectionCondition = CollectionCondition.size(expectedSize);
                break;
            case not_equal:
                collectionCondition = CollectionCondition.sizeNotEqual(expectedSize);
                break;
            case more:
                collectionCondition = CollectionCondition.sizeGreaterThan(expectedSize);
                break;
            case less:
                collectionCondition = CollectionCondition.sizeLessThan(expectedSize);
                break;
            case more_or_equal:
                collectionCondition = CollectionCondition.sizeGreaterThanOrEqual(expectedSize);
                break;
            case less_or_equal:
                collectionCondition = CollectionCondition.sizeLessThanOrEqual(expectedSize);
                break;
            default:
                throw new IllegalArgumentException("Не реализована проверка размера ElementsCollection: " + comparison.toString());
        }
        return collectionCondition;
    }

    public static boolean comparisonInt(Comparison comparison, int actual, int comparedNumber) {
        boolean conditionSuccessful = false;
     
        switch (comparison) {
            case equal: {
                conditionSuccessful = actual == comparedNumber;
                break;
            }
            case not_equal:{
                conditionSuccessful = actual != comparedNumber;
                break;
            }
            case more:{
                conditionSuccessful = actual > comparedNumber;
                break;
            }
            case less:{
                conditionSuccessful = actual < comparedNumber;
                break;
            }
            case more_or_equal:{
                conditionSuccessful = actual >= comparedNumber;
                break;
            }
            case less_or_equal:{
                conditionSuccessful = actual <= comparedNumber;
                break;
            }
        }
        return conditionSuccessful;
    }
}
