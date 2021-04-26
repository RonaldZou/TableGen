package com.pch.office.excel.arrange;

import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pch.office.Utils.*;

/**
 * @author pch
 * Model文件增加sheet，同步更新到分表
 */
public class AddSheet implements ITitle {

    /**
     * 增加sheet
     * @param path 文件夹路径
     */
    public void addSheetOpr(String path) {
        readExcelTitle(path);
        var fileName = path.substring(StringUtils.lastOrdinalIndexOf(path, "/", 2) + 1, path.length() - 1);
        var str = getFilesInMergeDir(path, fileName);
        for (var name : str) {
            var workbook = getReadWorkBook(name);
            Set<String> sheetNameList = new HashSet<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                var sheet = workbook.getSheetAt(i);
                var sheetName = sheet.getSheetName();
                if (!sheetName.contains("|")) {
                    continue;
                }
                sheetNameList.add(sheetName);
            }
            List<String> addSheet = titleMap.keySet().stream().filter(p -> !sheetNameList.contains(p)).collect(Collectors.toList());
            if (addSheet.isEmpty()) {
                continue;
            }
            for (var j : addSheet) {
                var sheet = workbook.createSheet(j);
                LinkedHashMap<String, Title> cont = titleMap.get(j);
                var csRow = sheet.createRow(CS);
                var typeRow = sheet.createRow(TYPE);
                var nameRow = sheet.createRow(NAME);
                var desRow = sheet.createRow(DES);
                int i = 0;
                for (var d : cont.entrySet()) {
                    var nameCell = nameRow.createCell(i);
                    var csCell = csRow.createCell(i);
                    var typeCell = typeRow.createCell(i);
                    var desCell = desRow.createCell(i);
                    nameCell.setCellValue(d.getKey());
                    Title title = d.getValue();
                    csCell.setCellValue(title.cs);
                    typeCell.setCellValue(title.type);
                    desCell.setCellValue(title.des);
                    var oldComment = title.comment;
                    if (oldComment != null) {
                        var draw = sheet.createDrawingPatriarch();
                        var comment = draw.createCellComment(oldComment.getClientAnchor());
                        comment.setString(oldComment.getString());
                        comment.setAuthor(oldComment.getAuthor());
                        desCell.setCellComment(comment);
                    }
                    i++;
                }
            }
            try (var fo = new FileOutputStream(name)) {
                workbook.write(fo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
