package ru.at.library.web.selenide;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import ru.at.library.web.scenario.CorePage;
import ru.at.library.web.scenario.annotations.Hidden;
import ru.at.library.web.scenario.annotations.Mandatory;
import ru.at.library.web.scenario.annotations.Optional;

import java.lang.reflect.Field;
import java.util.List;

public class PageElement {
    private Object element;
    private String name;
    private ElementType type;
    private ElementMode mode;

    public PageElement(Object element, String name, ElementType type, ElementMode mode) {
        this.element = element;
        this.name = name;
        this.type = type;
        this.mode = mode;
    }

    public Object getElement() {
        return element;
    }

    public void setElement(Object element) {
        this.element = element;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ElementType getType() {
        return type;
    }

    public void setType(ElementType type) {
        this.type = type;
    }

    public ElementMode getMode() {
        return mode;
    }

    public void setMode(ElementMode mode) {
        this.mode = mode;
    }

    public boolean checkMode(List<ElementMode> expectedModes) {
        boolean result = false;
        for (ElementMode expectedMode:expectedModes) {
            result |= this.mode.equals(expectedMode);
        }
        return result;
    }

    public enum ElementType {
        SELENIDE_ELEMENT,
        ELEMENTS_COLLECTION,
        CORE_PAGE,
        LIST_CORE_PAGE;

        public static ElementType getType(Object obj) {
            ElementType type = null;
            if (obj instanceof SelenideElement) {
                type = SELENIDE_ELEMENT;
            } else if (obj instanceof ElementsCollection) {
                type = ELEMENTS_COLLECTION;
            } else if (obj instanceof CorePage) {
                type = CORE_PAGE;
            } else if (obj instanceof List) {
                type = LIST_CORE_PAGE;
            }
            return type;
        }
    }

    public enum ElementMode {
        MANDATORY,
        PRIMARY,
        OPTIONAL,
        HIDDEN;

        public static ElementMode getMode(Field field) {
            ElementMode elementMode;
            if (field.getAnnotation(Mandatory.class) != null) {
                elementMode = MANDATORY;
            } else if (field.getAnnotation(Optional.class) != null) {
                elementMode = OPTIONAL;
            } else if (field.getAnnotation(Hidden.class) != null) {
                elementMode = HIDDEN;
            } else elementMode = PRIMARY;
            return elementMode;
        }
    }

}