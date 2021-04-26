package com.pch.office.relace;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static com.pch.office.Utils.*;

/**
 * 替换内容工具
 * 实现通过传入：模板xlsx路径 and 输出xlsx路径 实现自动替换
 * 模板xlsx：第一列 代表旧的数据 第二列 代表新的数据  第三列以后 代表要替换的列
 * 输出xlsx：会输出第三列以后替换好的内容 没有被被替换过的cell会设置成黄色
 * 支持替换数据有下面分隔符：逗号、分隔、冒号、竖杠，并且对没有替换过的内容进行染色
 * @author pch
 */
public class ReplaceTool {

    /** 分隔字符 */
    private static final char[] SPILT = {',', ';', ':', '|'};

    /**
     * 是否是分隔字符
     * @param c 字符
     * @return boolean
     */
    private static boolean isSplitChar(char c) {
        for (char sp : SPILT) {
            if (c == sp) {
                return true;
            }
        }
        return false;
    }

    /**
     * 替换
     * @param fileName   模板路径
     * @param resultName 输出路径
     */
    private static void replace(String fileName, String resultName) {
        Workbook writeWorkbook = createWorkBook(resultName);
        Workbook workbook = getReadWorkBook(new File(fileName));
        int sheetNum = workbook.getNumberOfSheets();
        Map<String, String> replaceMap = new HashMap<>();
        List<List<String>> contendList = new ArrayList<>();
        for (int i = 0; i < sheetNum; i++) {
            Sheet readSheet = workbook.getSheetAt(i);
            for (int r = 0; r < readSheet.getPhysicalNumberOfRows(); r++) {
                Row rows = readSheet.getRow(r);
                if (rows == null) {
                    System.err.println("出现空行 行数 = " + (r + 1));
                    System.exit(1);
                }
                int cols = rows.getLastCellNum();
                // 必须有内容列
                if (cols < 1) {
                    System.err.println("前两列不能为空 行数 = " + (r + 1));
                    System.exit(1);
                }
                replaceMap.put(getCellValueStr(rows.getCell(0)), getCellValueStr(rows.getCell(1)));
                List<String> list = new ArrayList<>();
                for (int c = 2; c <= cols; c++) {
                    list.add(getCellValueStr(rows.getCell(c)));
                }
                contendList.add(list);
            }
            Sheet sheet = writeWorkbook.createSheet(readSheet.getSheetName());
            for (int row = 0; row < contendList.size(); row++) {
                List<String> columns = contendList.get(row);
                var curRow = sheet.createRow(row);
                for (int col = 0; col < columns.size(); col++) {
                    String strings = columns.get(col);
                    if (strings.isBlank()) {
                        continue;
                    }
                    StringBuilder builder = new StringBuilder();
                    int index = 0;
                    char[] chars = strings.toCharArray();
                    for (int l = 0; l < chars.length; l++) {
                        char c = chars[l];
                        if (isSplitChar(c)) {
                            if (l > index) {
                                String temp = strings.substring(index, l);
                                builder.append(replaceMap.getOrDefault(temp, temp));
                            }
                            index = l + 1;
                            if (index <= chars.length) {
                                builder.append(c);
                            }
                        }
                        if (l == chars.length - 1 && index <= l) {
                            String temp = strings.substring(index);
                            if (temp.length() == 1 && isSplitChar(c)) {
                                builder.append(c);
                            } else {
                                builder.append(replaceMap.getOrDefault(temp, temp));
                            }
                        }
                    }
                    Cell cell = curRow.createCell(col);
                    String con = builder.toString();
                    if (!con.isBlank() && con.equals(strings)) {
                        var cellStyle = writeWorkbook.createCellStyle();
                        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(cellStyle);
                    }
                    cell.setCellValue(con);
                }
            }
        }
        try (FileOutputStream fos = new FileOutputStream(resultName)) {
            writeWorkbook.write(fos);
            writeWorkbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String replacePath;
        String resultPath;
        if (args.length == 2) {
            replacePath = args[0];
            resultPath = args[1];
        } else {
            replacePath = "C:\\Users\\admin\\Desktop\\my\\test\\A.xlsx";
            resultPath = "C:\\Users\\admin\\Desktop\\my\\test\\B.xlsx";
        }
        replace(replacePath, resultPath);
    }

}
