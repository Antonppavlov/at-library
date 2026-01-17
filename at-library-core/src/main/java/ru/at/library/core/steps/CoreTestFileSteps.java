package ru.at.library.core.steps;

import io.cucumber.java.Before;
import io.cucumber.java.ru.И;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Вспомогательные шаги и хуки для модульных сценариев ядра.
 * На прод-проекты влияние минимально: шаг помечен тегом @file-move
 * и используется только в тестовых сценариях библиотеки.
 */
public class CoreTestFileSteps {

    private static final String SOURCE_PATH = "/tmp/at_library_core_move_source.txt";
    private static final String DEST_PATH = "/tmp/at_library_core_move_dest.txt";

    @Before("@file-move")
    public void prepareTestFiles() throws IOException {
        File src = new File(SOURCE_PATH);
        if (!src.exists()) {
            File parent = src.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileWriter fw = new FileWriter(src)) {
                fw.write("test");
            }
        }
        File dest = new File(DEST_PATH);
        if (dest.exists()) {
            // очистим целевой файл, чтобы проверка Files.move прошла
            dest.delete();
        }
        System.setProperty("core.test.move.source", SOURCE_PATH);
        System.setProperty("core.test.move.dest", DEST_PATH);
    }

    @И("^подготовлены test-файлы для шага перемещения файла$")
    public void dummyStepForReadability() {
        // Ничего не делаем: вся подготовка уже выполнена в @Before("@file-move")
    }
}
