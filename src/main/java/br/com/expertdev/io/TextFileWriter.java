package br.com.expertdev.io;

import java.io.IOException;

public interface TextFileWriter {

    void write(String fileName, String content) throws IOException;
}

