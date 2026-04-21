package br.com.expertdev.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class DefaultTextFileWriter implements TextFileWriter {

    public void write(String fileName, String content) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8)) {
            osw.write(content == null ? "" : content);
        }
    }
}

