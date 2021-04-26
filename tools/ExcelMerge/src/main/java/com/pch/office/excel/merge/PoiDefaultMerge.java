package com.pch.office.excel.merge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static com.pch.office.Utils.*;

/**
 * @author pch
 * 按读取表的顺序合并，不会排序
 */
class PoiDefaultMerge extends AbstractPoiMerge {

    @Override
    protected void readExcel(File fileName) {
        var workbook = getReadWorkBook(fileName);
        for (var i = 0; i < workbook.getNumberOfSheets(); i++) {
            var readSheet = workbook.getSheetAt(i);
            var readSheetName = readSheet.getSheetName();
            if (!compareTitleRow.containsKey(readSheetName)) {
                continue;
            }
            var compareRow = readSheet.getRow(NAME);
            var compareColsNum = compareRow.getPhysicalNumberOfCells();
            var tempCompareRow = new ArrayList<>();
            for (int j = 0; j < compareColsNum; j++) {
                tempCompareRow.add(getCellValueStr(compareRow.getCell(j)));
            }
            var resultIndex = new ArrayList<Integer>();
            var titleRow = this.compareTitleRow.get(readSheetName);
            for (var s : titleRow) {
                resultIndex.add(tempCompareRow.indexOf(s));
            }
            var titleColsNum = titleRow.size();
            var writeSheet = writeWorkbook.getSheet(readSheetName);
            var lastIndex = writeSheet.getLastRowNum() + 1;
            var typeList = typeTitleRow.get(readSheetName);
            for (var j = CONT; j < readSheet.getPhysicalNumberOfRows(); j++) {
                var readRow = readSheet.getRow(j);
                if (readRow == null) {
                    continue;
                }
                // 第一列为cell为null，直接跳过
                if (getCellValueStr(readRow.getCell(0)).isBlank()) {
                    continue;
                }
                var writeRow = writeSheet.createRow(lastIndex);
                for (var k = 0; k < titleColsNum; k++) {
                    var tempIndex = resultIndex.get(k);
                    if (tempIndex < 0) {
                        continue;
                    }
                    var cell = readRow.getCell(tempIndex);
                    if (cell == null) {
                        continue;
                    }
                    var temp = getCellValueStr(cell);
                    if (temp.isBlank()) {
                        continue;
                    }
                    try {
                        setCellValueByType(writeRow.createCell(k), typeList.get(k), temp);
                    } catch (Exception e) {
                        System.err.println("excelName ："+ fileName + "  sheetName: " + readSheetName + " colName: " +typeList.get(k));
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                lastIndex++;
            }
        }
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}