package ru.at.library.web.selenide;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;

public interface IElementCheck {
    String getName();
    SelenideElement getElement();
    WebElementCondition getCondition();
    String getMessage();
    void setStatus(boolean status);
    boolean getStatus();
    String toString();
}
