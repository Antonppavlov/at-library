/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.at.library.core.utils.helpers;

import com.google.common.collect.Maps;
import groovy.lang.GroovyShell;
import lombok.extern.log4j.Log4j2;
import ru.at.library.core.cucumber.api.CoreScenario;

import java.math.BigDecimal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static ru.at.library.core.utils.helpers.PropertyLoader.loadProperty;

/**
 * Реализация хранилища переменных, заданных пользователем, внутри тестовых сценариев
 */
@Log4j2
public class ScopedVariables {

    public static final String CURVE_BRACES_PATTERN = "\\{([^{}]+)\\}";
    private Map<String, Object> variables = Maps.newHashMap();

    /**
     * Проверяет заданную строку на возможность подставновки параметров.
     * В случае нахождения параметра в строке заменяет его значение на значение из properties или хранилища переменных.
     * Пример: в файле property есть запись: 'prop.value = test'
     * При обработке строки: 'some_{prop.value}' будет получена строка: 'some_test'
     *
     * @param inputString       заданная строка
     * @return                  новая строка
     */
    public static String resolveVars(String inputString) {
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }
        log.debug(format("Проверка строки %s на возможность подстановки параметров", inputString));
        Pattern p = Pattern.compile(CURVE_BRACES_PATTERN);
        Matcher m = p.matcher(inputString);
        String newString = "";
        while (m.find()) {
            String varName = m.group(1);
            String value = loadProperty(varName, (String) CoreScenario.getInstance().tryGetVar(varName));
            if (value == null) {
                log.debug(format("Значение %s не было найдено ни в properties, ни в environment переменной", varName));
                newString = m.replaceFirst("__[__" + m.group(1) + "__]__");
            } else {
                newString = m.replaceFirst(value);
            }
            m = p.matcher(newString);
        }
        newString = newString.replaceAll("__\\[__", "{").replaceAll("__\\]__", "}");
        if (newString.isEmpty()) {
            newString = inputString;
        } else {
            log.debug(format("Найден параметр для подстановки. Новое значение строки %s = %s", inputString, newString));
        }
        return newString;
    }

    /**
     * @param expression java/groovy-код, который будет выполнен
     *                   Компилирует и выполняет в рантайме переданный на вход java/groovy-код.
     *                   Предварительно загружает в память все переменные,
     *                   т.е. на вход в строковом аргументе могут быть переданы переменные из "variables"
     */
    public Object evaluate(String expression) {
        GroovyShell shell = new GroovyShell();
        variables.entrySet().forEach(e -> {
            try {
                shell.setVariable(e.getKey(), new BigDecimal(e.getValue().toString()));
            } catch (NumberFormatException exp) {
                shell.setVariable(e.getKey(), e.getValue());
            }
        });
        return shell.evaluate(expression);
    }


    public void put(String name, Object value) {
        variables.put(name, value);
    }

    public Object get(String name) {
        return variables.get(name);
    }

    public void clear() {
        variables.clear();
    }

    public Object remove(String key) {
        return variables.remove(key);
    }

}
