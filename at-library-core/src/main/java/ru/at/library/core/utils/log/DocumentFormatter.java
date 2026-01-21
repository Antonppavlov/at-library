package ru.at.library.core.utils.log;

import lombok.extern.log4j.Log4j2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;


/**
 * Преобразователь документов для просмотра
 */
@Log4j2
public class DocumentFormatter {

    private static final String CONTENT_TYPE_XML = "text/xml";
    private static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * @param input строка, содержащая json
     * @return выровненный для просмотра документ или текст ошибки
     * преобразования
     * преобразование json к "красивому" виду
     */
    private static String createPrettyPrintJson(String input) {
        if (input.isEmpty()) {
            return ""; //**QUIT**
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            JsonElement obj = new JsonParser().parse(input);
            return gson.toJson(obj); //**QUIT**
        } catch (JsonParseException ex) {
            return "Error pretty printing json:\n" + ex.getMessage();
        }
    }

    /**
     * @param input строка, содержащая xml
     * @return выровненный для просмотра документ или текст ошибки
     * преобразования
     * преобразование xml к "красивому" виду
     */
    private static String createPrettyPrintXml(String input) {
        Source xmlInput = new StreamSource(new StringReader(input));
        StringWriter stringWriter = new StringWriter();
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlInput, new StreamResult(stringWriter));

            return stringWriter.toString().trim();
        } catch (Exception ex) {
            return "Error pretty printing xml:\n" + ex.getMessage();
        }
    }

    /**
     * @param input    строка, содержащая xml
     * @param contType тип ответа
     * @return выровненный для просмотра документ или текст ошибки
     * преобразования
     * преобразование документа к "красивому" виду
     */
    public static String createPrettyPrint(String input, String contType) {
        if (contType == null) {
            return input;
        }
        String typeOnly = contType.split(";")[0];
        switch (typeOnly) {
            case CONTENT_TYPE_XML:
                return createPrettyPrintXml(input);
            case CONTENT_TYPE_JSON:
                return createPrettyPrintJson(input);
            default:
                return input;
        }
    }
}

