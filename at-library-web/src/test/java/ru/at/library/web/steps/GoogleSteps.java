package ru.at.library.web.steps;

import io.cucumber.java.ru.И;
import ru.at.library.web.WebActionSteps;

public class GoogleSteps {

    @И("создание пользователя с ФИО: \"([^\"]*)\" телефон: \"([^\"]*)\" email: \"([^\"]*)\"")
    public void loginSystem(String fio, String password, String email) {
        WebActionSteps webActionSteps = new WebActionSteps();
        webActionSteps.loadPage("BCS demo аккаунт");

        webActionSteps.setFieldValue("ФИО", fio);
        webActionSteps.setFieldValue("Номер телефона", password);
        webActionSteps.setFieldValue("Email", email);
        webActionSteps.clickOnElement("Открыть счет");
    }

}
