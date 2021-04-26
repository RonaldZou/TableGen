package com.pch.office.excel.arrange;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.pch.office.Utils.*;

/**
 * @author pch
 * 重新生成分表 能够实现按Model的顺序排列内容，支持增删字段，并且保留原表数据格式
 */
public class RenewalTitle {
    private static Map<String, Map<Integer, List<CellData>>> titleMap = new HashMap<>();
    private static Map<String, List<Integer>> widthMap = new HashMap<>();

    private static class CellData {
        private String content;
        private Comment comment;
        private CellStyle cellStyle;

        private CellData(String content, CellStyle cellStyle, Comment comment) {
            this.content = content;
            this.cellStyle = cellStyle;
            this.comment = comment;
        }
    }

    /**
     * @param path 文件夹路径
     *             读取Model文件title
     */
    private static void readExcelTitle(String path) {
        var readFile = getModelFile(path);
        var readWorkbook = getReadWorkBook(readFile);
        for (int i = 0; i < readWorkbook.getNumberOfSheets(); i++) {
            var sheet = readWorkbook.getSheetAt(i);
            var sheetName = sheet.getSheetName();
            if (!sheetName.contains("|")) {
                continue;
            }
            var cols = sheet.getRow(NAME).getPhysicalNumberOfCells();
            var map = titleMap.computeIfAbsent(sheetName, n -> new HashMap<>());
            var tempWidthMap = widthMap.computeIfAbsent(sheetName, n -> new ArrayList<>());
            for (var n = 0; n < CONT; n++) {
                var row = sheet.getRow(n);
                var mapTemp = map.computeIfAbsent(n, k -> new ArrayList<>());
                for (int j = 0; j < cols; j++) {
                    var cell = row.getCell(j);
                    if (cell == null) {
                        mapTemp.add(new CellData("", null, null));
                        continue;
                    }
                    var val = getCellValueStr(cell);
                    if (n == NAME) {
                        tempWidthMap.add(sheet.getColumnWidth(j));
                    }
                    mapTemp.add(new CellData(val, cell.getCellStyle(), cell.getCellComment()));
                }
            }
        }
    }

    /**
     * @param path 文件夹路径
     *             重新覆写文件（会重新生成excel）
     */
    public static void renewalFile(String path) {
        readExcelTitle(path);
        var fileName = path.substring(StringUtils.lastOrdinalIndexOf(path, "/", 2) + 1, path.length() - 1);
        var str = getFilesInMergeDir(path, fileName);
        var c = new CountDownLatch(str.length);
        for (var name : str) {
            new Thread(() -> {
                readWriteFile(name);
                c.countDown();
            }).start();
        }
        try {
            c.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileOld 要整理文件file
     *                方法写的太长了，有时间优化一下
     */
    private static void readWriteFile(File fileOld) {
        var tempPath = fileOld.getPath();
        int dotIndex = tempPath.lastIndexOf('.');
        var fileNew = tempPath.substring(0, dotIndex) + "temp" + tempPath.substring(dotIndex);
        var writeWorkbook = createWorkBook(fileNew);
        int modelSheetNum = writeWorkbook.getNumberOfSheets();
        if (modelSheetNum > 0) {
            for (int i = 0; i < modelSheetNum; i++) {
                writeWorkbook.removeSheetAt(i);
            }
        }
        var workbook = getReadWorkBook(fileOld);
        int setIndex = 0;
        for (var i = 0; i < workbook.getNumberOfSheets(); i++) {
            var readSheet = workbook.getSheetAt(i);
            var readSheetName = readSheet.getSheetName();
            if (!titleMap.containsKey(readSheetName)) {
                continue;
            }
            var compareRow = readSheet.getRow(NAME);
            var tempCompareRow = new ArrayList<>();
            for (var j = 0; j < compareRow.getPhysicalNumberOfCells(); j++) {
                tempCompareRow.add(getCellValueStr(compareRow.getCell(j)));
            }
            var resultIndex = new ArrayList<Integer>();
            var rowMap = titleMap.get(readSheetName);
            var titleRow = rowMap.get(NAME);
            for (var s : titleRow) {
                resultIndex.add(tempCompareRow.indexOf(s.content));
            }
            var cols = titleRow.size();
            var writeSheet = writeWorkbook.createSheet(readSheetName);
            var modelWidth = widthMap.get(readSheetName);
            // 读写Title
            for (var j = 0; j < CONT; j++) {
                var row = writeSheet.createRow(j);
                var listCell = rowMap.get(j);
                // 设置行高
                row.setHeight(readSheet.getRow(j).getHeight());
                var readSheetRow = readSheet.getRow(j);
                for (var k = 0; k < cols; k++) {
                    var cell = row.createCell(k);
                    var index = resultIndex.get(k);
                    var d = listCell.get(k);
                    // 设置列宽
                    if (j == 0) {
                        writeSheet.setColumnWidth(k, index > -1 ? readSheet.getColumnWidth(index) : modelWidth.get(k));
                    }
                    // 设置comment
                    Optional.ofNullable(d.comment).ifPresent(p -> {
                        var draw = writeSheet.createDrawingPatriarch();
                        var comment = draw.createCellComment(p.getClientAnchor());
                        comment.setString(p.getString());
                        comment.setAuthor(p.getAuthor());
                        cell.setCellComment(comment);
                    });
                    // 设置cellStyle
                    CellStyle style;
                    if (index > -1) {
                        // 设置style
                        var readCell = readSheetRow.getCell(index);
                        // 策划协议注释配置
                        if (readCell == null) {
                            continue;
                        }
                        style = readCell.getCellStyle();
                    } else {
                        style = d.cellStyle;
                    }
                    if (!d.content.isBlank()) {
                        if (style != null && setIndex < CELL_STYLE_MAX) {
                            // 每个workBook最多定义64000个cellStyle
                            var cellStyle = writeWorkbook.createCellStyle();
                            cell.setCellStyle(cellStyle);
                            cell.getCellStyle().cloneStyleFrom(style);
                            setIndex++;
                        }
                        cell.setCellValue(d.content);
                    }
                }
            }
            // 读写内容
            for (var j = CONT; j < readSheet.getPhysicalNumberOfRows(); j++) {
                var readRow = readSheet.getRow(j);
                if (readRow == null) {
                    continue;
                }
                var writeRow = writeSheet.createRow(j);
                writeRow.setHeight(readRow.getHeight());
                for (var k = 0; k < cols; k++) {
                    var tempIndex = resultIndex.get(k);
                    if (tempIndex < 0) {
                        continue;
                    }
                    var readCell = readRow.getCell(tempIndex);
                    var temp = getCellValueStr(readCell);
                    if (temp.isBlank()) {
                        continue;
                    }
                    Cell c = writeRow.createCell(k);
                    if (k < 1) {
                        XSSFCellStyle rStyle = (XSSFCellStyle)readCell.getCellStyle();
                        if (rStyle != null) {
                            // 每个workBook最多定义64000个cellStyle
                            var cellStyle = writeWorkbook.createCellStyle();
                            c.setCellStyle(cellStyle);
                            c.getCellStyle().cloneStyleFrom(rStyle);
                            setIndex++;
                        }
                    }
                    c.setCellValue(temp);
                }
            }
        }
        try (var fo = new FileOutputStream(fileNew)) {
            writeWorkbook.write(fo);
            writeWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileOld.delete()) {
            File f = new File(fileNew);
            var path = fileOld.getPath();
            if (!f.renameTo(new File(tempPath))) {
                System.err.println(path + " 重命名失败");
            }
        }
    }

}
