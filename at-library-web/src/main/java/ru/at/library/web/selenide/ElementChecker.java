package ru.at.library.web.selenide;

import io.qameta.allure.Allure;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ElementChecker {

    /**
     * Проверка списка объектов с интерфейсом {@link IElementCheck} на соответствие условию {@link com.codeborne.selenide.Condition},
     * заданному в объекте, с указанным таймаутом.
     *
     * @param elementCheckList список объектов {@link IElementCheck} для проверки
     * @param timeOutInMillis  таймаут в миллисекундах
     * @return модифицированный список объектов {@link IElementCheck} с проставленными статусами {@link IElementCheck#getStatus()}
     */
    public static List<IElementCheck> checkElements(List<IElementCheck> elementCheckList, long timeOutInMillis) {
        List<IElementCheck> resultCheckList = new ArrayList<>(elementCheckList);
        for (IElementCheck elementCheck : resultCheckList) {
            try {
                // В Selenide 7.x Condition реализует WebElementCondition,
                // поэтому используем стандартный shouldHave с явным таймаутом вместо element.is(condition)
                elementCheck.getElement().shouldHave(
                        elementCheck.getCondition(),
                        java.time.Duration.ofMillis(timeOutInMillis)
                );
                elementCheck.setStatus(true);
            } catch (Throwable e) {
                elementCheck.setStatus(false);
            }
        }
        return resultCheckList;
    }

    /**
     * Преобразование списка объектов с интерфейсом {@link IElementCheck} в строку
     *
     * @param elementCheckList  список объектов с интерфейсом {@link IElementCheck} для преобразования в строку
     *
     * @return список объектов с интерфейсом {@link IElementCheck} преобразованный к объекту типа {@link String}
     */
    public static String elementCheckListAsString(List<IElementCheck> elementCheckList) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (IElementCheck elementCheck:elementCheckList) {
            sb.append(elementCheck.toString());
            index++;
            if (index == elementCheckList.size()) break;
            sb.append("\r\n\r\n");
        }
        return sb.toString();
    }

    /**
     * Преобразование списка объектов с интерфейсом {@link IElementCheck} и отрицательным результатом проверки {@link IElementCheck#getStatus()} в строку
     *
     * @param elementCheckList  список объектов с интерфейсом {@link IElementCheck} для преобразования в строку
     *
     * @return список объектов с интерфейсом {@link IElementCheck} и отрицательным результатом проверки {@link IElementCheck#getStatus()} преобразованный к объекту типа {@link String}
     */
    public static List<IElementCheck> getFailedCheckList(List<IElementCheck> elementCheckList) {
        return elementCheckList.stream().filter(r -> !r.getStatus()).collect(toList());
    }

    /**
     * Преобразование списка объектов с интерфейсом {@link IElementCheck} и положительным результатом проверки {@link IElementCheck#getStatus()} в строку
     *
     * @param elementCheckList  список объектов с интерфейсом {@link IElementCheck} для преобразования в строку
     *
     * @return список объектов с интерфейсом {@link IElementCheck} и положительным результатом проверки {@link IElementCheck#getStatus()} преобразованный к объекту типа {@link String}
     */
    public static List<IElementCheck> getPassedCheckList(List<IElementCheck> elementCheckList) {
        return elementCheckList.stream().filter(IElementCheck::getStatus).collect(toList());
    }

    public static void attachCheckListResults(String message, List<IElementCheck> checkList, boolean status) {
        List<IElementCheck> checkListWithStatus = status ? getPassedCheckList(checkList) : getFailedCheckList(checkList);
        if (!checkListWithStatus.isEmpty()) {
            Allure.getLifecycle().addAttachment(String.format("%s: %d из %d", message, checkListWithStatus.size(), checkList.size()), "text/html", ".txt",
                    elementCheckListAsString(checkListWithStatus).getBytes(StandardCharsets.UTF_8));
        }
    }

}
