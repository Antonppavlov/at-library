package ru.at.library.core.steps;

import io.cucumber.java.ru.И;
import org.apache.commons.lang3.RandomStringUtils;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;
import ru.at.library.core.utils.helpers.PropertyLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static ru.at.library.core.steps.OtherSteps.getPropertyOrStringVariableOrValue;
import static ru.at.library.core.steps.OtherSteps.getRandCharSequence;

/**
 * Шаги генерации тестовых данных
 */
@Log4j2
public class DataGenerationSteps {

    private static CoreScenario coreScenario = CoreScenario.getInstance();

    /**
     * Конкатенация строк
     */
    @И("конкатенация строк \"([^\"]*)\" и \"([^\"]*)\" и сохранено в переменную \"([^\"]*)\"$")
    public void concatenationString(String text1, String text2, String varName) {
        text1 = getPropertyOrStringVariableOrValue(text1);
        text2 = getPropertyOrStringVariableOrValue(text2);
        String text = text1 + text2;
        coreScenario.setVar(varName, text);
        log.trace("Строка равна: " + text);
    }

    /**
     * Генерация последовательности латинских или кириллических букв задаваемой длины
     */
    @И("^генерация (\\d+) случайных символов на ((?:кириллице|латинице)) и сохранено в переменную \"([^\"]*)\"$")
    public void setRandomCharSequence(int seqLength, String lang, String varName) {
        String charSeq = getRandCharSequence(seqLength, lang);
        coreScenario.setVar(varName, charSeq);
        log.trace("Строка случайных символов равна: " + charSeq);
    }

    /**
     * Генерация последовательности цифр задаваемой длины и сохранение этого значения в переменную
     */
    @И("^генерация случайного числа из (\\d+) (?:цифр|цифры) и сохранение в переменную \"([^\"]*)\"$")
    public void randomNumSequence(int seqLength, String varName) {
        String numSeq = RandomStringUtils.randomNumeric(seqLength);
        coreScenario.setVar(varName, numSeq);
        log.trace("Случайное число равно: " + numSeq);
    }

    /**
     * Создает случайную строку, которая находится между включающим минимумом и максимум </ p>
     */
    @И("^генерация случайного числа в диапазоне от (\\d+) до (\\d+) и сохранение в переменную \"([^\"]*)\"$")
    public void rRandomNumSequence(int min, int max, String varName) {
        max -= min;
        long number = (long) (Math.random() * ++max) + min;
        String numSeq = String.valueOf(number);
        ;
        coreScenario.setVar(varName, numSeq);
        log.trace("Случайное число равно: " + numSeq);
    }

    /**
     * Генерация случайного boolean и сохранение в переменную
     */
    @И("^генерация случайного boolean и сохранение в переменную \"([^\"]*)\"$")
    public void randomBoolean(String varName) {
        String randomString = String.valueOf(nextBoolean());
        coreScenario.setVar(varName, randomString);
        log.trace("Случайное boolean равно: " + randomString);
    }

    /**
     * Выбрано случайное знание из списка и сохранено в переменную
     */
    @И("^сохранено в переменную \"([^\"]*)\" случайное значение из списка:$")
    public void randomStingInList(String varName, List<String> list) {
        int random = new Random().nextInt(list.size());
        String randomString = list.get(random);
        coreScenario.setVar(varName, randomString);
        log.trace("Строка равна: " + randomString);
    }

    @И("^переменная \"([^\"]+)\" содержит base64 кодирование, декодирована в pdf и сохранена по пути \"([^\"]+)\" с именем \"([^\"]+)\" в формате \"([^\"]+)\"$")
    public void saveBase64ToPdf(String encodeBytes, String path, String fName, String fFormat) throws IOException {
        String base64Code = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(encodeBytes);
        String fileName = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(fName);
        String fileFormat = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(fFormat);
        String baseDir = PropertyLoader.loadValueFromFileOrPropertyOrVariableOrDefault(path);

        byte[] decodedBytes = Base64.getDecoder().decode(base64Code);

        File dir = new File(baseDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Не удалось создать директорию для сохранения файла: " + baseDir);
        }

        File file = new File(dir, fileName + "." + fileFormat);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
            fos.flush();
        }
    }
}
