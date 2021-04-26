package com.pch.test;

import com.pch.office.Utils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author pch
 */
public class TestXLSM {
    public static void main(String[] a) {
        try {
            String path = "C:\\Users\\admin\\Desktop\\my\\demo.xlsm";
            String path1 = "C:\\Users\\admin\\Desktop\\my\\dem1.xlsm";
            Files.copy(Paths.get(path), Paths.get(path1), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalArgumentException("不支持的格式！！！！！");
        }
    }
}
