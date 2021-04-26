package com.pch.office.excel.arrange;

import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.pch.office.Utils.*;

/**
 * @author pch
 * 读取Model的Title内容
 */
interface ITitle {

    Map<String, LinkedHashMap<String, Title>> titleMap = new HashMap<>();

    class Title {
        String cs;
        String type;
        String des;
        Comment comment;

        Title(String cs, String type, String des, Comment comment) {
            this.cs = cs;
            this.type = type;
            this.des = des;
            this.comment = comment;
        }
    }

    /**
     * 读取Model文件title
     */
    default void readExcelTitle(String path) {
        var readFile = getModelFile(path);
        var readWorkbook = getReadWorkBook(readFile);
        for (int i = 0; i < readWorkbook.getNumberOfSheets(); i++) {
            Sheet sheet = readWorkbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            if (!sheetName.contains("|")) {
                continue;
            }
            var csRow = sheet.getRow(CS);
            var typeRow = sheet.getRow(TYPE);
            var nameRow = sheet.getRow(NAME);
            var desRow = sheet.getRow(DES);
            var map = titleMap.computeIfAbsent(sheetName, n -> new LinkedHashMap<>());
            for (int j = 0; j < nameRow.getPhysicalNumberOfCells(); j++) {
                var desC = desRow.getCell(j);
                map.put(getCellValueStr(nameRow.getCell(j)), new Title(getCellValueStr(csRow.getCell(j)),
                        getCellValueStr(typeRow.getCell(j)), getCellValueStr(desC), desC == null ? null : desC.getCellComment()));
            }
        }
    }
}
