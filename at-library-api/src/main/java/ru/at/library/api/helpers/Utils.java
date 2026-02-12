package ru.at.library.api.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.at.library.core.cucumber.api.CoreScenario;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.at.library.core.utils.helpers.PropertyLoader.loadProperty;

/**
 * Вспомогательные методы для валидации и обработки JSON/XML/URL-параметров и подстановки переменных в JSON.
 */
@Log4j2
public class Utils {

    public static final String CURVE_BRACES_PATTERN = "\\{([^{}]+)\\}";
    private static final SoftAssert sa = new SoftAssert();

    /**
     * Проверяет, является ли переданная в качестве аргумента строка соответствующей переданному формату, или,
     * если формат не передан, определяет формат.
     *
     * @param checkingValueString строка для валидации
     * @param expectedTextFormat  формат из множества или {@code null} для автоопределения
     * @return определённый или ожидаемый формат
     */
    public static TextFormat defineOrCheckDataFormat(String checkingValueString, TextFormat expectedTextFormat) {
        if (expectedTextFormat != null) {
            // Формат явно задан вызывающим кодом, просто возвращаем его без дополнительной проверки
            return expectedTextFormat;
        }

        if (isJSONValid(checkingValueString)) {
            return TextFormat.JSON;
        }
        if (isXMLValid(checkingValueString)) {
            return TextFormat.XML;
        }
        if (isParamsValid(checkingValueString)) {
            return TextFormat.PARAMS;
        }

        sa.fail("Нечитаемый формат данных");
        return expectedTextFormat;
    }

    /**
     * @param jsonInString - строка для валидации
     * @return Проверяет, является ли переданная в качестве аргумента строка валидным JSON
     */
    public static boolean isJSONValid(String jsonInString) {
        if (jsonInString.isEmpty()) {
            return false;
        }
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

/**
     * @param xmlInString - строка для валидации
     * @return Проверяет, является ли переданная в качестве аргумента строка валидным XML
     */
    public static boolean isXMLValid(String xmlInString) {
        if (xmlInString == null || xmlInString.isEmpty()) {
            return false;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            try (InputStream is = new ByteArrayInputStream(xmlInString.getBytes())) {
                db.parse(is);
            }
            return true;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return false;
        }
    }

    /**
     * @param paramsInString - строка для валидации
     * @return Проверяет, является ли переданная в качестве аргумента строка валидным URL Params (param1=value1&param2=value2)
     */
    public static boolean isParamsValid(String paramsInString) {

        return paramsInString.matches("((.+=.*)&)*(.+=.*)");
    }

    /**
     * @param xmlin - xml-поток для преобразования
     * @return Преобразует InputStream, содержащий xml, в объектное представление
     */
    public static Document readXml(InputStream xmlin) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(xmlin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param doc       - объектное представление xml
     * @param xpathExpr - xpath для поиска
     * @return Производит поиск в xml по переданному xpath, возвращает найденный узел
     */
    public static NodeList filterNodesByXPath(Document doc, String xpathExpr) {
        try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expr = xpath.compile(xpathExpr);
            Object eval = expr.evaluate(doc, XPathConstants.NODESET);
            return (NodeList) eval;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Кодирование UTF-8 строки в URL
     */
    public static String urlEncodeUTF8(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * @param inputJsonAsString заданная строка
     * @return новая строка
     * Производит поиск параметров в переданном строкой json.
     * В случае нахождения параметра - заменяет его значение на значение из properties или хранилища переменных
     */
    public static String resolveJsonVars(String inputJsonAsString) {
        if (isJSONValid(inputJsonAsString)) return inputJsonAsString;
        Pattern p = Pattern.compile(CURVE_BRACES_PATTERN);
        Matcher m = p.matcher(inputJsonAsString);
        String newString = "";
        while (m.find()) {
            String varName = m.group(1);
            String value = loadProperty(varName, (String) CoreScenario.getInstance().tryGetVar(varName));
            if (value == null) {
                log.trace(
                        "Значение " + varName +
                                " не было найдено ни в properties, ни в environment переменной");
            }
            newString = m.replaceFirst(value);
            if (isJSONValid(newString)) return newString;
            m = p.matcher(newString);
        }
        if (newString.isEmpty()) {
            newString = inputJsonAsString;
        }
        return newString;
    }

}
