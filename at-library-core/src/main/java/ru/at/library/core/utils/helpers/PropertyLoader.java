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

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.at.library.core.cucumber.api.CoreScenario;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для получения свойств
 */
public class PropertyLoader {

    private static final Logger log = LogManager.getLogger(PropertyLoader.class);

    public static String PROPERTIES_FILE = System.getProperty("properties", "application.properties");
    private static final Properties PROPERTIES = getPropertiesInstance();
    private static final Properties PROFILE_PROPERTIES = getProfilePropertiesInstance();

    private PropertyLoader() {
    }

    /**
     * Возвращает значение системного свойства
     * (из доступных для данной JVM) по его названию,
     * в случае, если оно не найдено, вернется значение по умолчанию
     *
     * @param propertyName название свойства
     * @param defaultValue значение по умолчанию
     * @return значение свойства по названию или значение по умолчанию
     */
    public static String loadSystemPropertyOrDefault(String propertyName, String defaultValue) {
        String propValue = System.getProperty(propertyName);
        return propValue != null ? propValue : defaultValue;
    }

    /**
     * Возвращает Integer значение системного свойства
     * (из доступных для данной JVM) по его названию,
     * в случае, если оно не найдено, вернется значение по умолчанию
     *
     * @param propertyName название свойства
     * @param defaultValue Integer значение по умолчанию
     * @return Integer значение свойства по названию или значение по умолчанию
     */
    public static Integer loadSystemPropertyOrDefault(String propertyName, Integer defaultValue) {
        try {
            return Integer.valueOf(System.getProperty(propertyName, defaultValue.toString()).trim());
        } catch (NumberFormatException ex) {
            log.error("Could not parse value to Integer ", ex.getMessage());
            return defaultValue;
        }
    }

    /**
     * Возвращает Boolean значение системного свойства
     * (из доступных для данной JVM) по его названию,
     * в случае, если оно не найдено, вернется значение по умолчанию
     *
     * @param propertyName название свойства
     * @param defaultValue Boolean значение по умолчанию
     * @return Integer значение свойства по названию или значение по умолчанию
     */
    public static Boolean loadSystemPropertyOrDefault(String propertyName, Boolean defaultValue) {
        String def = defaultValue.toString();
        String property = loadProperty(propertyName, def);
        return Boolean.parseBoolean(property.trim());
    }

    /**
     * Возвращает свойство по его названию из property-файла
     *
     * @param propertyName название свойства
     * @return значение свойства, в случае, если значение не найдено,
     * будет выброшено исключение
     */
    public static String loadProperty(String propertyName) {
        String value = tryLoadProperty(propertyName);
        if (null == value) {
            throw new IllegalArgumentException("В файле properties не найдено значение по ключу: " + propertyName);
        }
        return value;
    }

    /**
     * Возвращает значение свойства из property-файла по его названию,
     * если значение не найдено, возвращает это же значение в качестве значения по умолчанию
     *
     * @param propertyNameOrValue название свойства/значение по умолчанию
     * @return значение по ключу value, если значение не найдено,
     * вернется value
     */
    public static String getPropertyOrValue(String propertyNameOrValue) {
        return loadProperty(propertyNameOrValue, propertyNameOrValue);
    }

    /**
     * Возвращает значение свойства из property-файла по его названию,
     * Если ничего не найдено, возвращает значение по умолчанию
     *
     * @param propertyName название свойства
     * @param defaultValue значение по умолчанию
     * @return значение свойства
     */
    public static String loadProperty(String propertyName, String defaultValue) {
        String value = tryLoadProperty(propertyName);
        return value != null ? value : defaultValue;
    }

    /**
     * Возвращает значение свойства типа Integer из property-файла по названию,
     * если ничего не найдено, возвращает значение по умолчанию
     *
     * @param propertyName название свойства
     * @param defaultValue значение по умолчанию
     * @return значение свойства типа Integer или значение по умолчанию
     */
    public static Integer loadPropertyInt(String propertyName, Integer defaultValue) {
        String value = tryLoadProperty(propertyName);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Вспомогательный метод, возвращает значение свойства по имени.
     * Сначала поиск в System переменным,
     * затем в property-файле, если указано системное свойство "profile"
     * Если ничего не найдено, поиск в properties
     *
     * @param propertyName название свойства
     * @return значение свойства
     */
    public static String tryLoadProperty(String propertyName) {
        String value = null;
        if (!Strings.isNullOrEmpty(propertyName)) {
            String systemProperty = loadSystemPropertyOrDefault(propertyName, propertyName);
            if (!propertyName.equals(systemProperty)) return systemProperty;

            value = PROFILE_PROPERTIES.getProperty(propertyName);
            if (null == value) {
                value = PROPERTIES.getProperty(propertyName);
            }
        }
        return value;
    }


    /**
     * Вспомогательный метод, возвращает значение свойства по имени.
     */
    public static String loadValueFromFileOrVariableOrDefault(String valueToFind) {
        String pathAsString = StringUtils.EMPTY;
        try {
            Path path = Paths.get(System.getProperty("user.dir", "."))
                    .resolve(valueToFind);
            pathAsString = path.toString();
            String fileValue = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            log.warn("Значение из файла " + valueToFind + " = " + fileValue);
            return fileValue;
        } catch (IOException | InvalidPathException e) {
            log.warn("Значение не найдено по пути " + pathAsString);
        }
        // Попытка загрузить значение как ресурс из classpath (src/test/resources, src/main/resources и т.п.)
        String resourcePath = valueToFind.startsWith("/") ? valueToFind.substring(1) : valueToFind;
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                is = PropertyLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            }
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    String fileValue = readAll(reader);
                    log.warn("Значение из classpath ресурса " + valueToFind + " = " + fileValue);
                    return fileValue;
                }
            }
        } catch (IOException e) {
            log.warn("Ошибка чтения classpath ресурса " + valueToFind, e);
        }
        if (CoreScenario.getInstance().tryGetVar(valueToFind) != null) {
            Object var = CoreScenario.getInstance().getVar(valueToFind);
//            TODO нужно зарефакторить проблема с тем что невозможно вернуть Response
//            if (var instanceof Response) {
//                return ((Response) var).getBody().asString();
//            }
            return (String) var;
        }
        log.warn("Значение не найдено в хранилище. Будет исользовано значение по умолчанию " + valueToFind);
        return valueToFind;
    }

    /**
     * Получает значение из properties, файла по переданному пути, значение из хранилища переменных или как String аргумент
     * Используется для получение body.json api шагах, либо для получения script.js в ui шагах
     *
     * @param valueToFind - ключ к значению в properties, путь к файлу c нужным значением, значение как String
     * @return значение как String
     */
    public static String loadValueFromFileOrPropertyOrVariableOrDefault(String valueToFind) {
        String pathAsString = StringUtils.EMPTY;
        String propertyValue = tryLoadProperty(valueToFind);
        if (StringUtils.isNotBlank(propertyValue)) {
            log.warn("Значение переменной: " + valueToFind + " из " + PROPERTIES_FILE + " = " + propertyValue);
            return propertyValue;
        }
        try {
            Path path = Paths.get(System.getProperty("user.dir", "."))
                    .resolve(valueToFind);
            pathAsString = path.toString();
            String fileValue = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            log.warn("Значение из файла " + valueToFind + " = " + fileValue);
            return fileValue;
        } catch (IOException | InvalidPathException e) {
            log.warn("Значение не найдено по пути: " + pathAsString);
        }
        // Попытка загрузить значение как ресурс из classpath (src/test/resources, src/main/resources и т.п.)
        String resourcePath = valueToFind.startsWith("/") ? valueToFind.substring(1) : valueToFind;
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                is = PropertyLoader.class.getClassLoader().getResourceAsStream(resourcePath);
            }
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    String fileValue = readAll(reader);
                    log.warn("Значение из classpath ресурса " + valueToFind + " = " + fileValue);
                    return fileValue;
                }
            }
        } catch (IOException e) {
            log.warn("Ошибка чтения classpath ресурса " + valueToFind, e);
        }
        if (CoreScenario.getInstance().tryGetVar(valueToFind) != null) {
            Object var = CoreScenario.getInstance().getVar(valueToFind);
            //TODO нужно зарефакторить проблема с тем что невозможно вернуть Response
//            if (var instanceof Response) {
//                return ((Response) var).getBody().asString();
//            }
            return (String) var;
        }
        log.warn("Значение не найдено в хранилище. Будет исользовано значение по умолчанию " + valueToFind);
        return valueToFind;
    }

    /**
     * Циклически подставляет параметры из properties, содержимое файла по переданному пути,
     * значение из хранилища переменных или как String аргумент
     *
     * @param processingValue - строка, содержащая в фигурных скобках ключи к значению в properties, переменные сценариев,
     *                        названия путей к файлам c нужным значением, значения как строки. Пример:
     *                        123{var_name} 456{prop_name} 789{file_path_from_project_root}
     * @return значение как String после всевозможных замен
     */
    public static String cycleSubstitutionFromFileOrPropertyOrVariable(String processingValue) {
        String savedValue;
        do {
            savedValue = processingValue;
            List<String> matches = getMatchesByRegex(processingValue, "\\{[^\\s{}]+}");
            if (matches.size() == 0) {
                return processingValue;
            }
            for (String match : matches) {
                String oldValue = match.substring(1, match.length() - 1);
                String newValue = loadValueFromFileOrPropertyOrVariableOrDefault(oldValue);
                if (!oldValue.equals(newValue)) {
                    processingValue = processingValue.replace(match, newValue);
                }
            }
        } while (!processingValue.equals(savedValue));

        return processingValue;
    }

    /**
     * Возвращает набор свойств из property-файла по соответствию имени регулярному выражению
     *
     * @param regex регулярное выражение
     * @return набор свойств
     */
    public static HashMap<String, String> loadPropertiesMatchesByRegex(String regex) {
        HashMap<String, String> properties = new HashMap<>();
        for (Enumeration<?> e = PROPERTIES.propertyNames(); e.hasMoreElements(); ) {
            String name = (String) e.nextElement();
            if (!getMatchesByRegex(name, regex).isEmpty()) {
                String value = PROPERTIES.getProperty(name);
                properties.put(name, value);
            }
        }
        return properties;
    }

    /**
     * Вспомогательный метод, возвращает свойства из файла properties
     *
     * @return свойства из файла properties
     */
    private static Properties getPropertiesInstance() {
        Properties instance = new Properties();

        // Имя файла без возможного ведущего слеша (значение берётся из -Dproperties)
        String fileName = PROPERTIES_FILE.startsWith("/")
                ? PROPERTIES_FILE.substring(1)
                : PROPERTIES_FILE;

        // 1) Пробуем загрузить через context classloader (видит test/main ресурсы модулей)
        InputStream resourceStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName);

        // 2) Fallback на старый механизм через PROPERTIES_FILE относительно PropertyLoader.class
        if (resourceStream == null) {
            resourceStream = PropertyLoader.class.getResourceAsStream(PROPERTIES_FILE);
        }

        // 3) Если до сих пор не нашли, делаем файловый поиск, начиная от user.dir и поднимаясь вверх,
        //    проверяя как корень, так и стандартную папку src/test/resources на каждом уровне.
        if (resourceStream == null) {
            Path dir = Paths.get(System.getProperty("user.dir", "."));
            while (dir != null) {
                // a) файл лежит прямо в текущем каталоге
                Path direct = dir.resolve(fileName);
                if (Files.exists(direct)) {
                    try {
                        log.warn("Не найден {} в classpath, но найден файл по пути '{}'. Будет использован он.", PROPERTIES_FILE, direct);
                        resourceStream = Files.newInputStream(direct);
                    } catch (IOException e) {
                        log.warn("Не удалось открыть файл properties по пути '{}'. Будут использованы пустые свойства.", direct, e);
                    }
                    break;
                }
                // b) файл лежит под src/test/resources относительно текущего каталога
                Path testResources = dir.resolve("src").resolve("test").resolve("resources").resolve(fileName);
                if (Files.exists(testResources)) {
                    try {
                        log.warn("Не найден {} в classpath, но найден файл по пути '{}'. Будет использован он.", PROPERTIES_FILE, testResources);
                        resourceStream = Files.newInputStream(testResources);
                    } catch (IOException e) {
                        log.warn("Не удалось открыть файл properties по пути '{}'. Будут использованы пустые свойства.", testResources, e);
                    }
                    break;
                }
                dir = dir.getParent();
            }
        }

        if (resourceStream == null) {
            log.warn("Не найден файл properties ({}). Свойства будут пустыми.", PROPERTIES_FILE);
            return instance;
        }

        try (InputStreamReader inputStream = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
            instance.load(inputStream);
        } catch (IOException e) {
            log.warn("Ошибка чтения файла properties '{}'. Свойства будут пустыми.", PROPERTIES_FILE, e);
        }

        return instance;
    }

    /**
     * Вспомогательный метод, возвращает свойства из кастомного properties по пути
     * из системного свойства "profile"
     *
     * @return прочитанные свойства из кастомного файла properties, если свойство "profile" указано, иначе пустой объект
     */
    private static Properties getProfilePropertiesInstance() {
        Properties instance = new Properties();
        String profile = System.getProperty("profile", "");
        if (!Strings.isNullOrEmpty(profile)) {
            String path = Paths.get(profile, PROPERTIES_FILE).toString();
            URL url = PropertyLoader.class.getClassLoader().getResource(path);
            if (url == null) {
                log.warn("Не найден profile properties по пути '{}'. Профиль '{}' будет проигнорирован.", path, profile);
                return instance;
            }
            try (InputStream resourceStream = url.openStream();
                 InputStreamReader inputStream = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
                instance.load(inputStream);
            } catch (IOException e) {
                log.warn("Ошибка чтения profile properties по пути '{}'. Профиль '{}' будет проигнорирован.", path, profile, e);
            }
        }
        return instance;
    }

    /**
     * @param inputString - строка для поиска соответствий регулярному выражению
     * @param regex       - регулярное выражение для поиска
     * @return Возращает список соответствий по регулярному выражению
     */
    public static List<String> getMatchesByRegex(String inputString, String regex) {
        List<String> result = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(inputString);
        while (m.find()) {
            result.add(m.group(0));
        }
        return result;
    }

    private static String readAll(InputStreamReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[4096];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, read);
        }
        return sb.toString();
    }

    /**
     * Загружает обязательное строковое свойство (system/profile/application.properties).
     * Если свойство не найдено или пустое, выбрасывается IllegalArgumentException.
     */
    public static String requireNonEmptyProperty(String propertyName) {
        String value = tryLoadProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Не найдено обязательное свойство '" + propertyName + "' в system/properties");
        }
        return value;
    }

    /**
     * Загружает свойство как URI и валидирует его формат.
     * Удобно для baseURI, endpoint'ов и других URL-подобных свойств.
     */
    public static java.net.URI loadUriProperty(String propertyName) {
        String raw = loadProperty(propertyName);
        try {
            return new java.net.URI(raw);
        } catch (java.net.URISyntaxException e) {
            throw new IllegalArgumentException("Свойство '" + propertyName + "' не является корректным URI: " + raw, e);
        }
    }

}

