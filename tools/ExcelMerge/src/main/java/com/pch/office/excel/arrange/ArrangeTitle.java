package com.pch.office.excel.arrange;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.pch.office.Utils.*;

/**
 * @author pch
 * Model文件title改类型、描述、cs标记、批注信息修改，以及在最后修改增加列，会自动同步到分表，缺点：不支持删除字段，改字段名
 */
public class ArrangeTitle implements ITitle {

    /**
     * 整理title
     */
    public void arrangeTitle(String path) {
        readExcelTitle(path);
        var fileName = path.substring(StringUtils.lastOrdinalIndexOf(path, "/", 2) + 1, path.length() - 1);
        var str = getFilesInMergeDir(path, fileName);
        for (var name : str) {
            var workbook = getReadWorkBook(name);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                var sheetName = sheet.getSheetName();
                if (!sheetName.contains("|")) {
                    continue;
                }
                var tMap = ArrangeTitle.titleMap.get(sheetName);
                var csRow = sheet.getRow(CS);
                var typeRow = sheet.getRow(TYPE);
                var nameRow = sheet.getRow(NAME);
                var desRow = sheet.getRow(DES);
                List<String> list = new ArrayList<>();
                Cell tempCell;
                for (var j = 0; j < nameRow.getPhysicalNumberOfCells(); j++) {
                    var cell = nameRow.getCell(j);
                    var s = getCellValueStr(cell);
                    var t = tMap.get(s);
                    if (t == null) {
                        continue;
                    }
                    list.add(s);
                    if (!t.cs.isBlank()) {
                        tempCell = csRow.getCell(j);
                        if (!Objects.equals(getCellValueStr(tempCell), t.cs)) {
                            tempCell.setCellValue(t.cs);
                        }
                    }
                    if (!t.type.isBlank()) {
                        tempCell = typeRow.getCell(j);
                        if (!Objects.equals(getCellValueStr(tempCell), t.type)) {
                            tempCell.setCellValue(t.type);
                        }
                    }
                    if (!t.des.isBlank()) {
                        tempCell = desRow.getCell(j);
                        if (!Objects.equals(getCellValueStr(tempCell), t.des)) {
                            tempCell.setCellValue(t.des);
                        }
                    }
                    tempCell = desRow.getCell(j);
                    if (tempCell == null) {
                        continue;
                    }
                    tempCell.removeCellComment();
                    setComment(t, sheet, tempCell);
                }
                for (var d : tMap.entrySet()) {
                    if (!list.contains(d.getKey())) {
                        var v = d.getValue();
                        int k = nameRow.getPhysicalNumberOfCells();
                        csRow.createCell(k).setCellValue(v.cs);
                        typeRow.createCell(k).setCellValue(v.type);
                        nameRow.createCell(k).setCellValue(d.getKey());
                        tempCell = desRow.createCell(k);
                        tempCell.setCellValue(v.des);
                        setComment(v, sheet, tempCell);
                    }
                }
            }
            try (var fo = new FileOutputStream(name)) {
                workbook.write(fo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setComment(Title v, Sheet sheet, Cell desCell) {
        if (v.comment != null) {
            var draw = sheet.createDrawingPatriarch();
            var comment = draw.createCellComment(v.comment.getClientAnchor());
            comment.setString(v.comment.getString());
            comment.setAuthor(v.comment.getAuthor());
            desCell.setCellComment(comment);
        }
    }
}
