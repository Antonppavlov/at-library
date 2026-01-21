package ru.at.library.web.selenide;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;

public class ElementCheck implements IElementCheck {
    private final String name;
    private final SelenideElement element;
    private final WebElementCondition condition;
    private final String message;
    private boolean status;

    public ElementCheck(String name, SelenideElement element, WebElementCondition condition, String message) {
        this.name = name;
        this.element = element;
        this.condition = condition;
        this.message = message;
        this.status = false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SelenideElement getElement() {
        return element;
    }

    @Override
    public WebElementCondition getCondition() {
        return condition;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public boolean getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return  "Элемент: " + this.name + "\n" +
                "Локатор: " + this.element.getSearchCriteria() + "\n" +
                "Проверка: " + this.message + "\n" +
                "Результат проверки: " + (this.status ? "Успешно пройдена" : "Не пройдена");
    }

}
