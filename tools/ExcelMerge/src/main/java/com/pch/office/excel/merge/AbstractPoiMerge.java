package com.pch.office.excel.merge;

import com.pch.office.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pch.office.Utils.*;

/**
 * @author pch
 * poi处理Excel基类
 */
abstract class AbstractPoiMerge {

    Map<String, List<String>> compareTitleRow = new HashMap<>();

    Map<String, List<String>> typeTitleRow = new HashMap<>();

    Workbook writeWorkbook;

    private Workbook getWriteWorkBook(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return createWorkBook(fileName);
    }

    private void beforeReadExcel(String path, String outPath) {
        writeWorkbook = getWriteWorkBook(outPath);
        File readFile = getModelFile(path);
        Workbook readWorkbook = getReadWorkBook(readFile);
        for (int i = 0; i < readWorkbook.getNumberOfSheets(); i++) {
            var sheet = readWorkbook.getSheetAt(i);
            var sheetName = sheet.getSheetName();
            var cols = sheet.getRow(NAME).getPhysicalNumberOfCells();
            var writeSheet = writeWorkbook.createSheet(sheetName);
            for (int k = 0; k < CONT; k++) {
                var row = sheet.getRow(k);
                var rowName = new ArrayList<String>();
                var writeRow = writeSheet.createRow(k);
                for (int j = 0; j < cols; j++) {
                    var cell = row.getCell(j);
                    var tempCell = cell == null ? "" : Utils.getCellValueStr(cell);
                    rowName.add(tempCell);
                    writeRow.createCell(j).setCellValue(tempCell);
                }
                if (k == NAME) {
                    compareTitleRow.putIfAbsent(sheetName, rowName);
                } else if (k == TYPE) {
                    typeTitleRow.put(sheetName, rowName);
                }
            }
        }
    }

    protected abstract void readExcel(File fileName);

    private String readAllExcel(String path, String out) {
        String fileName = path.substring(StringUtils.lastOrdinalIndexOf(path, "/", 2) + 1, path.length() - 1);
        String outPath = out + fileName + SUFFIX;
        beforeReadExcel(path, outPath);
        File[] str = getFilesInMergeDir(path,fileName);
        for (File name : str) {
            readExcel(name);
        }
        return outPath;
    }

    protected void beforeWriteExcel(String filePath) {
    }

    private void writeExcel(String filePath) {
        beforeWriteExcel(filePath);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            writeWorkbook.write(fos);
            writeWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void performMerge(String path, String out) {
        writeExcel(readAllExcel(path, out));
    }

}
