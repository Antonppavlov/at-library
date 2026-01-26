package ru.at.library.core.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.То;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertNotNull;
import static ru.at.library.core.utils.helpers.PropertyLoader.*;

/**
 * Набор общих шагов под api/web/mob</>
 */
@Log4j2
public class OtherSteps {

    private static CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Локальное перемещение файла
     */
    @И("^перемещение файла из \"([^\"]*)\" в \"([^\"]*)\"$")
    public static void localMoveFiles(String pathFile, String pathMoveFile) throws IOException {
        pathFile = loadValueFromFileOrPropertyOrVariableOrDefault(pathFile);
        pathFile = loadValueFromFileOrVariableOrDefault(pathFile);

        pathMoveFile = loadValueFromFileOrPropertyOrVariableOrDefault(pathMoveFile);
        pathMoveFile = loadValueFromFileOrVariableOrDefault(pathMoveFile);

        Path temp = Files.move(Paths.get(pathFile), Paths.get(pathMoveFile));

        assertNotNull(temp, "Ошибка перемещения файла: " + pathFile);
    }

    /**
     * <p style="color: green; font-size: 1.5em">
     *
     * @return Возвращает каталог "Downloads" в домашней директории
     */
    public static File getDownloadsDir() {
        String homeDir = System.getProperty("user.home");
        return new File(homeDir + "/Downloads");
    }

    /**
     * <p style="color: green; font-size: 1.5em">
     *
     * @param filesToDelete массив файлов
     *                      Удаляет файлы, переданные в метод
     */
    public static void deleteFiles(File[] filesToDelete) {
        for (File file : filesToDelete) {
            file.delete();
        }
    }

    /**
     * <p style="color: green; font-size: 1.5em">
     *
     * @param maxValueInRange максимальная граница диапазона генерации случайных чисел
     *                        Возвращает случайное число от нуля до maxValueInRange
     */
    public static int getRandom(int maxValueInRange) {
        return (int) (Math.random() * maxValueInRange);
    }

    /**
     * Возвращает последовательность случайных символов переданных алфавита и длины
     * Принимает на вход варианты языков 'ru' и 'en'
     * Для других входных параметров возвращает латинские символы (en)
     *
     * @param length длина последовательности
     * @param lang   варианты языков 'ru' или 'en'
     */
    public static String getRandCharSequence(int length, String lang) {
        if (lang.equals("кириллице")) {
            lang = "ru";
        } else {
            lang = "en";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char symbol = charGenerator(lang);
            builder.append(symbol);
        }
        return builder.toString();
    }

    /**
     * Возвращает случайный символ переданного алфавита
     *
     * @param lang варианты языков 'ru' или 'en'
     */
    public static char charGenerator(String lang) {
        Random random = new Random();
        if (lang.equals("ru")) {
            return (char) (1072 + random.nextInt(32));
        } else {
            return (char) (97 + random.nextInt(26));
        }
    }

    /**
     * Проверка на соответствие строки паттерну
     *
     * @param pattern шаблон для проверки
     * @param str     строка для проверки
     */
    public static boolean isTextMatches(String str, String pattern) {
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(str);
        return m.matches();
    }

    /**
     * Возвращает локатор для поиска по нормализованному (без учета регистра) тексту
     *
     * @param expectedText
     */
    public static String getTranslateNormalizeSpaceText(String expectedText) {
        StringBuilder text = new StringBuilder();
        text.append("//*[contains(translate(normalize-space(text()), ");
        text.append("'ABCDEFGHIJKLMNOPQRSTUVWXYZАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ', ");
        text.append("'abcdefghijklmnopqrstuvwxyzабвгдеёжзийклмнопрстуфхчшщъыьэюя'), '");
        text.append(expectedText.toLowerCase());
        text.append("')]");
        return text.toString();
    }

    /**
     * <p style="color: green; font-size: 1.5em">
     *
     * @return Возвращает значение из property файла, если отсутствует, то из пользовательских переменных,
     * если и оно отсутствует, то возвращает значение переданной на вход переменной
     */
    public synchronized static String getPropertyOrStringVariableOrValue(String propertyNameOrVariableNameOrValue) {
        String propertyValue = tryLoadProperty(propertyNameOrVariableNameOrValue);
        String variableValue = null;
        try {
            variableValue = (String) CoreScenario.getInstance().tryGetVar(propertyNameOrVariableNameOrValue);
        } catch (ClassCastException ignored) {}

        boolean propertyCheck = checkResult(propertyValue, "Переменная " + propertyNameOrVariableNameOrValue + " из property файла");
        boolean variableCheck = checkResult(variableValue, "Переменная сценария " + propertyNameOrVariableNameOrValue);

        return propertyCheck ? propertyValue : (variableCheck ? variableValue : propertyNameOrVariableNameOrValue);
    }

    public synchronized static List<String> getPropertyOrStringVariableOrValue(List<String> textTable) {
        List<String> list = new ArrayList<>();
        for (String text : textTable) {
            list.add(getPropertyOrStringVariableOrValue(text));
        }
        return list;
    }

    public static boolean checkResult(String result, String message) {
        if (isNull(result)) {
            log.trace(message + " не найдена");
            return false;
        }
        log.trace(message + " = " + result);
        return true;
    }

    /**
     * Устанавливается значение переменной в хранилище переменных.
     * Один из кейсов: установка login пользователя
     *
     * @param variableName имя переменной
     * @param value        значение переменной
     */
    @То("^установлено значение переменной \"([^\"]*)\" равным$")
    @И("^установлено значение переменной \"([^\"]*)\" равным \"(.*)\"$")
    public void setVariable(String variableName, String value) {
        value = getPropertyOrValue(value);
        coreScenario.setVar(variableName, value);
    }

    /**
     * Проверка равенства двух переменных из хранилища
     *
     * @param firstVariableName  первая переменная
     * @param secondVariableName вторая переменная
     */
    @И("^значения в переменных \"([^\"]*)\" и \"([^\"]*)\" совпадают$")
    public void compareTwoVariables(String firstVariableName, String secondVariableName) {
        String firstValueToCompare = coreScenario.getVar(firstVariableName).toString();
        String secondValueToCompare = coreScenario.getVar(secondVariableName).toString();
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("Значения в переменных [%s] и [%s] не совпадают", firstVariableName, secondVariableName),
                firstValueToCompare,
                is(equalTo(secondValueToCompare))
        );
    }

    /**
     * Проверка неравенства двух переменных из хранилища
     *
     * @param firstVariableName  первая переменная
     * @param secondVariableName вторая переменная
     */
    @И("^значения в переменных \"([^\"]*)\" и \"([^\"]*)\" не совпадают$")
    public void checkingTwoVariablesAreNotEquals(String firstVariableName, String secondVariableName) {
        String firstValueToCompare = coreScenario.getVar(firstVariableName).toString();
        String secondValueToCompare = coreScenario.getVar(secondVariableName).toString();
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("Значения в переменных [%s] и [%s] совпадают", firstVariableName, secondVariableName),
                firstValueToCompare,
                not(equalTo(secondValueToCompare))
        );
    }

    /**
     * Проверка равенства переменной
     *
     * @param variableName          переменная
     * @param expectedValueVariable ожидаемое содержимое
     */
    @То("^значение переменной \"([^\"]*)\" равно$")
    @И("^значение переменной \"([^\"]*)\" равно \"([^\"]*)\"$")
    public void checkVariable(String variableName, String expectedValueVariable) {
        expectedValueVariable = getPropertyOrStringVariableOrValue(expectedValueVariable);
        String valueVariable = coreScenario.getVar(variableName).toString();
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                String.format("Значения в переменных [%s] и [%s] не совпадают", valueVariable, expectedValueVariable),
                valueVariable,
                is(equalTo(expectedValueVariable))
        );
    }

    /**
     * Проверка выражения на истинность
     *
     * @param expression выражение из property, из переменной сценария или значение аргумента
     *                   Например, string1.equals(string2)
     *                   OR string.equals("string")
     *                   Любое Java-выражение, возвращающие boolean
     */
    @И("^верно, что \"([^\"]*)\"$")
    public void expressionExpression(String expression) {
        coreScenario.getVars().evaluate("assert(" + expression + ")");
    }

    /**
     * Сохранено значение из property файла в переменную
     *
     * @param propertyVariableName ключ в файле properties
     * @param variableName         имя переменной
     *                             Значение заданной переменной из properties сохраняется в переменную в coreScenario
     *                             для дальнейшего использования
     */
    @И("^сохранено значение \"([^\"]*)\" из property файла в переменную \"([^\"]*)\"$")
    public void saveValueToVar(String propertyVariableName, String variableName) {
        propertyVariableName = loadProperty(propertyVariableName);
        coreScenario.setVar(variableName, propertyVariableName);
        log.trace("Значение сохраненной переменной " + propertyVariableName);
    }

    /**
     * Проверка совпадения значения из переменной и значения из property
     */
    @И("^значения из переменной \"([^\"]*)\" и из property файла \"([^\"]*)\" совпадают$")
    public void checkIfValueFromVariableEqualPropertyVariable(String envVariable, String propertyVariable) {
        String envVariableValue = (String) coreScenario.getVar(envVariable);
        String propertyVariableValue = loadProperty(propertyVariable);
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(
                "Переменные " + envVariable + " и " + propertyVariable + " не совпадают",
                envVariableValue,
                is(equalToIgnoringCase(propertyVariableValue))
        );
    }

    /**
     * Выполняется чтение файла с шаблоном и заполнение его значениями из таблицы
     */
    @И("^шаблон \"([^\"]*)\" заполнен данными из таблицы и сохранён в переменную \"([^\"]*)\"$")
    public void fillTemplate(String templateName, String varName, DataTable table) {
        String template = getPropertyOrValue(templateName);
        template = loadValueFromFileOrVariableOrDefault(template);
        boolean error = false;
        for (List<String> list : table.asLists()) {
            String regexp = loadValueFromFileOrPropertyOrVariableOrDefault(list.get(0));
            String replacement = loadValueFromFileOrPropertyOrVariableOrDefault(list.get(1));
            if (template.contains(regexp)) {
                template = template.replaceAll(regexp, replacement);
            } else {
                log.trace("В шаблоне не найден элемент " + regexp);
                error = true;
            }
        }
        if (error)
            throw new RuntimeException("В шаблоне не найдены требуемые регулярные выражения");
        coreScenario.setVar(varName, template);
    }

//    /**
//     * Валидация что текст является email-ом
//     */
//    @И("^значение переменной \"([^\"]*)\" является email-ом$")
//    public void checkEmail(String variableName) throws AddressException {
//        String valueVariable = coreScenario.getVar(variableName).toString();
//        new InternetAddress(valueVariable)
//                .validate();
//    }

    /**
     * Валидация что текст является email-ом
     */
    @И("^длина строки переменной \"([^\"]*)\" ((?:больше|меньше|равна)) (\\d+)$")
    public void checkEmail(String variableName, String condition, int expectedLength) {
        int actualLength = coreScenario.getVar(variableName).toString().length();
        String message = "Длина строки переменной " + variableName + condition + " " + expectedLength;
        org.hamcrest.Matcher<Integer> matcher;
        switch (condition) {
            case "больше": {
                matcher = greaterThan(expectedLength);
                break;
            }
            case "меньше": {
                matcher = lessThan(expectedLength);
                break;
            }
            case "равна": {
                matcher = equalTo(expectedLength);
                break;
            }
            default:
                throw new IllegalArgumentException("Не реализована проверка длины строки переменной для условия: " + condition);
        }
        CoreScenario.getInstance().getAssertionHelper().hamcrestAssert(message, actualLength, matcher);
    }

    /**
     * Устанавливается значение текущей даты в хранилище переменных.
     *
     * @param variableName имя переменной
     * @param dateFormat   формат даты
     */
    @И("^установлено значение переменной \"([^\"]*)\" с текущей датой в формате \"([^\"]*)\"$")
    public void setCurrentDate(String variableName, String dateFormat) {
        long date = System.currentTimeMillis();
        setDate(date, variableName, dateFormat);
    }

    /**
     * Устанавливается значение текущей даты в хранилище переменных.
     *
     * @param variableName имя переменной
     * @param dateFormat   формат даты
     */
    @И("^установлено значение переменной \"([^\"]*)\" с текущей датой минус (\\d+) (?:час|часов) в формате \"([^\"]*)\"$")
    public void setMinusDate(String variableName, int hour, String dateFormat) {
        long time = new Date(System.currentTimeMillis() - hour * 1000 * 3600).getTime();
        setDate(time, variableName, dateFormat);
    }

    /**
     * Устанавливается значение текущей даты + часы в хранилище переменных.
     *
     * @param variableName имя переменной
     * @param dateFormat   формат даты
     */
    @И("^установлено значение переменной \"([^\"]*)\" с текущей датой плюс (\\d+) (?:час|часов) в формате \"([^\"]*)\"$")
    public void setPlusDate(String variableName, int hour, String dateFormat) {
        long time = new Date(System.currentTimeMillis() + hour * 1000 * 3600).getTime();
        setDate(time, variableName, dateFormat);
    }

    private void setDate(long date, String variableName, String dateFormat) {
        String currentStringDate;
        try {
            currentStringDate = new SimpleDateFormat(dateFormat).format(date);
        } catch (IllegalArgumentException ex) {
            currentStringDate = new SimpleDateFormat("dd.MM.yyyy").format(date);
            log.trace("Неверный формат даты. Будет использоваться значание по умолчанию в формате dd.MM.yyyy");
        }

        coreScenario.setVar(variableName, currentStringDate);
    }

    /**
     * Ожидание в течение заданного количества секунд
     *
     * @param seconds секунд
     */
    @И("^выполнено ожидание в течение (\\d+) (?:секунд|секунды)")
    public void waitForSeconds(long seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ожидание было прервано", e);
        }
    }

    /**
     * Написание автотеста в работе
     */
    @И("^ручной тест$")
    public void manualTest() {
        throw new io.cucumber.java.PendingException("написание автотеста в работе");
    }

    /**
     * Написание автотеста в работе
     */
    @И("^написание автотеста в работе$")
    public void pendingException() {
        throw new io.cucumber.java.PendingException("написание автотеста в работе");
    }

    /**
     * Написание автотеста в работе
     */
    @И("^написание автотеста в работе. Планируемая дата: \"([^\"]*)\"$")
    public void pendingException(String date) throws PendingException {
        throw new io.cucumber.java.PendingException("написание автотеста в работе. Планируемая дата: " + date);
    }

    /**
     * Написание автотеста в работе
     */
    @И("^проблема с поиском локатора: \"([^\"]*)\"$")
    public void pending(String date) {
        throw new io.cucumber.java.PendingException("написание автотеста в работе. Планируемая дата: " + date);
    }

    /**
     * Автотест реализован на старом фреймворке
     */
    @И("^автотест реализован на старом фреймворке$")
    public void oldFramework() {
        throw new io.cucumber.java.PendingException("автотест реализован на старом фреймворке");
    }

    /**
     * Автотест реализован на старом фреймворке
     */
    @И("^не актуальный тест в тестовой моделе")
    public void notActual() {
        throw new io.cucumber.java.PendingException("не актуальный тест в тестовой моделе");
    }
}
