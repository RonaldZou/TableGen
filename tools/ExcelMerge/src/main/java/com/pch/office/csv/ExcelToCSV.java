package com.pch.office.csv;

import com.pch.office.FileRecord;
import com.pch.office.Utils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * csv处理类  同excel中同名sheet会自动合并，检查sn是否重复，检查field是否重复定义
 * 特殊处理：同excel中name_*会变成name生成，_符号占用做这种特殊处理，name和name_*都存在只会生成name
 * 目前采用自定义的方式进行excel读写操作
 * 可以考虑换成opencsv中CSVWriter
 * TODO：如何检测sheet同名的问题
 */
public class ExcelToCSV {

    /** 配置表支持类型 */
    private static List<String> dataType = new ArrayList<>();

    static {
        String[] array = {"int", "string", "String", "float", "boolean", "bool","short", "byte", "long", "double","vector3d","vector2d"};
        for (String type : array) {
            dataType.add(type);
            dataType.add(type + "[]");
            dataType.add(type + "[][]");
        }
    }

    private String csvOutPath;
    private String excelPath;
    private String isAll;

    private Map<String, String> nameMap = new HashMap<>();

    private ExcelToCSV(String csvOutPath, String excelPath, String isAll) {
        this.csvOutPath = csvOutPath;
        this.excelPath = excelPath;
        this.isAll = isAll;
    }

    /**
     * 生成csv
     */
    private void genCSV() {
        var allFiles = Utils.getFilesInMergeDir(excelPath, (dir, name) -> Utils.checkFileNameSuffix(name));
        File javaDir = new File(csvOutPath);
        if (!javaDir.exists()) {
            javaDir.mkdir();
        }
        if (isAll.isBlank()) {
            FileRecord.parallelTask(allFiles, excelPath, this::toCSV, FileRecord.INDEX_NAME);
        } else {
            for (File file : allFiles) {
                toCSV(file);
            }
        }
        try {
            Path path = Paths.get(excelPath + "gen/config/record.txt");
            Map<String, String> oldNameMap = FileRecord.readRecordFile(path);
            var bw = Files.newBufferedWriter(path, CREATE, TRUNCATE_EXISTING, WRITE);
            oldNameMap.putAll(nameMap);
            for (var file : oldNameMap.entrySet()) {
                bw.write(file.getKey() + "=" + file.getValue());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转化成csv
     * @param inputFile excel文件
     */
    private void toCSV(File inputFile) {
        try {
            Workbook wBook = Utils.getReadWorkBook(inputFile);
            int index = wBook.getNumberOfSheets();
            Set<String> excelName = new HashSet<>();
            Map<Integer, String> tempMap = new HashMap<>();
            for (int k = 0; k < index; k++) {
                Sheet sheet = wBook.getSheetAt(k);
                String sheetName = sheet.getSheetName();
                if (Utils.isNotSheet(sheetName)) {
                    continue;
                }
                String[] sheetNames = sheetName.split("\\|");
                if (!Utils.isClassName(sheetNames[1])) {
                    throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + "  sheet名（*|name）配置错误！ name部分必须以字母或下划线开头，只能包含字母、数字、下划线");
                }
                tempMap.put(k, sheetNames[1]);
            }
            Map<Integer, String> resultMap = new HashMap<>();
            tempMap.forEach((k, v) -> {
                if (!v.contains("_")) {
                    resultMap.put(k, v);
                }
            });
            tempMap.forEach((k, v) -> {
                int i = v.indexOf("_") > 0 ? v.indexOf("_") : v.length();
                String key = v.substring(0, i);
                if (!resultMap.containsValue(key)) {
                    resultMap.put(k, key);
                } else {
                    resultMap.put(k, v);
                }
            });
            Map<String, Set<String>> snCacheMap = new HashMap<>();
            List<Integer> csIndex = new ArrayList<>();
            for (var map : resultMap.entrySet()) {
                Sheet sheet = wBook.getSheetAt(map.getKey());
                String sheetName = map.getValue();
                String fileName = sheetName + ".csv";
                nameMap.put("Conf" + sheetName, inputFile.getName());
                boolean isSame = excelName.contains(fileName);
                FileOutputStream fos = new FileOutputStream(csvOutPath + fileName, isSame);
                excelName.add(fileName);
                if (!isSame) {
                    // 插入bom
                    byte[] bytes = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
                    fos.write(bytes);
                }
                int rowNums = sheet.getLastRowNum() + 1;
                if (rowNums < 4) {
                    continue;
                }
                int colNums = sheet.getRow(2).getLastCellNum();
                // 定义的字段名集合
                Set<String> nameSet = new HashSet<>();
                if (!isSame) {
                    csIndex = new ArrayList<>();
                }

                LOOP:for (int i = isSame ? 4 : 0; i < rowNums; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }
                    StringJoiner data = new StringJoiner(",");
                    for (int j = 0; j < colNums; j++) {
                        Cell cell = row.getCell(j);
                        if (cell == null) {
                            if (j == 0) {
                                if (i == 0) {
                                    throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + " ☢配置首列不能为null");
                                }
                                continue LOOP;
                            }
                        }
                        // 下面的检查  有时间优化下  代码乱死
                        String result;
                        try {
                            result = Optional.ofNullable(Utils.getCellValueStr(cell).stripTrailing()).orElse("");
                        } catch (Exception e) {
                            throw new IllegalArgumentException(inputFile.getName() + " -> " + sheetName + " 行 = " + (i + 1) + " 列 = " + (j + 1) + "内容识别不出来， 重新保存试一下");
                        }
                        // 检测cs列
                        if (i == 0) {
                            if (result.contains("c") || result.contains("s")) {
                                csIndex.add(j);
                            } else {
                                continue;
                            }
                        } else {
                            // 过滤出不是cs的列
                            if (!csIndex.contains(j)) {
                                continue;
                            }
                            // 检测第二行的数据类型是否是支持类型
                            if (i == 1) {
                                if (!dataType.contains(result.strip())) {
                                    throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + "  ☢不支持数据类型: " + result);
                                }
                            } else if (i == 2) {
                                // 判断字段名不能为空
                                if (result.isBlank()) {
                                    throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + " ☢配置中存在重复字段:" + result);
                                }
                                // 判断字段名是否重复
                                if (nameSet.contains(result)) {
                                    throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + " ☢配置中存在重复字段:" + result);
                                }
                                nameSet.add(result);
                            }
                        }
                        // 第一列的数据检测
                        if (j == 0) {
                            // sn列数据重复检测
                            if (i >= 4) {
                                if (result.isBlank()) {
                                    continue LOOP;
                                }
                                Set<String> set = snCacheMap.computeIfAbsent(fileName, key -> new HashSet<>());
                                if (set.contains(result)) {
                                    throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + "  ☢配置中存在sn重复: " + result);
                                }
                                set.add(result);
                            } else if (i < 3) {
                                // 第一列数据不能为空
                                if (result.isBlank()) {
                                    throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + " ☢配置首列不能为null");
                                }
                                // 第一列数据类型名必须为sn
                                if (i == 2) {
                                    if (!result.equals("sn")) {
                                        throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + " ☢配置首列必须为sn列");
                                    }
                                } else if (i == 0) {
                                    if (!result.contains("s") && !result.contains("c")) {
                                        throw new IllegalArgumentException(inputFile.getName() + " " + sheetName + " ☢配置首列必须为sn列，必须配置cs");
                                    }
                                }
                            }
                        }
                        // 解决\的问题
                        result = result.replaceAll("\\\\n", "<br>");
                        // 解决文本中有“”的问题
                        result = result.replaceAll("\"", "\"\"");
                        // 用双引号包裹
                        result = "\"" + result + "\"";
                        data.add(result);
                    }
                    fos.write(data.toString().getBytes(StandardCharsets.UTF_8));
                    fos.write("\n".getBytes(StandardCharsets.UTF_8));
                }
                fos.flush();
                fos.close();
                System.out.println(fileName + "---生成完成！！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        String input;
        String excel;
        String flag = "";
        if (args.length == 2) {
            input = args[0];
            excel = args[1];
        } else if (args.length == 3) {
            input = args[0];
            excel = args[1];
            flag = args[2];
        } else {
            input = "C:\\sandbox\\trunk\\public\\config\\data\\csv\\";
            excel = "C:\\sandbox\\trunk\\public\\config\\";
        }
        new ExcelToCSV(input, excel, flag).genCSV();
    }
}
