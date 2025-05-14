package org.chomookun.fintics.shell.tax;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class KisUsCommandTest {

    @Tag("manual")
    @Test
    public void test() throws IOException {
        String baseDir = System.getProperty("user.home") + File.separator;
        String pdfInputFilePath = baseDir + "tax-kis.pdf";
        String excelOutputFilePath = baseDir + "tax-kis.xlsx";
        KisUsCommand kisCommand = new KisUsCommand();
        kisCommand.kisUs(pdfInputFilePath, excelOutputFilePath);
    }

}