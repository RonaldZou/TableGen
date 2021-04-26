package com.pch.office;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author pch
 * Excel处理中用到的一些公用工具方法
 */
public class Utils {
    /** excel后缀 */
    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";
    private static final String XLSM = "xlsm";
    /** 模板文件名 */
    public static final String MODEL = "Model";
    /** 重新生成excel后缀 */
    public static final String SUFFIX = ".xlsx";
    /** cs行index */
    public static final int CS = 0;
    /** 字段类型行index */
    public static final int TYPE = 1;
    /** 唯一名行index */
    public static final int NAME = 2;
    /** 描述行index */
    public static final int DES = 3;
    /** 内容开始行index */
    public static final int CONT = 4;
    /** 编码格式 */
    public static final String UTF_8 = "UTF-8";
    /** 每个workBook最多定义64000个cellStyle */
    public static final int CELL_STYLE_MAX = 64000;

    /**
     * 判断一个字符是否是汉字
     * 注意：中文汉字的编码范围：[\u4e00-\u9fa5]
     */
    public static boolean isChinese(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }

    /**
     * 是否全是数字
     * @param string 字符串
     * @return boolean
     */
    public static boolean isNumber(String string) {
        return string.matches("[0-9]+");
    }

    /**
     * 是否含有数字
     * @param string 字符串
     * @return boolean
     */
    public static boolean hasNumber(String string) {
        return !string.matches(".*\\d+.*");
    }

    /**
     * 检查文件名（是excel文件并且不是临时文件）
     * @param name 字符串
     * @return boolean
     */
    public static boolean checkFileNameSuffix(String name) {
        return !name.contains("~$") && (name.endsWith(XLS) || name.endsWith(XLSX) || name.endsWith(XLSM));
    }

    /**
     * 是否是sheet
     * @param sheetName sheet名
     * @return boolean
     */
    public static boolean isNotSheet(String sheetName) {
        return !sheetName.contains("|");
    }

    /**
     * 检查类名
     * @param string 字符串
     * @return boolean
     */
    public static boolean isClassName(String string) {
        return string.substring(0, 1).matches("[a-zA-Z_]") && string.matches("[_0-9a-zA-Z]*");
    }

    /**
     * 首字母大写
     * @param s 字符串
     * @return String
     */
    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return s.substring(0, 1).toUpperCase().concat(s.substring(1));
        }
    }

    /**
     * 获得路径
     * @param path 路径
     * @return Path
     */
    public static Path getPath(String path) {
        // 只适用windows
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return Paths.get(path);
    }

    /**
     * 获得该路径下的文件夹名
     * @param path 路径
     * @return List<String>
     */
    public static List<String> getDirNamesInPath(String path) {
        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return list;
        }
        if (!file.isDirectory()) {
            return list;
        }
        File[] files = file.listFiles();
        if (files == null) {
            return list;
        }
        for (File file1 : files) {
            if (file1.isDirectory()) {
                list.add(file1.getName());
            }
        }
        return list;
    }

    /**
     * 获得文件夹下的所有文件
     * @param path 路径
     * @param filter 过滤器
     * @return File[]
     */
    public static File[] getFilesInMergeDir(String path, FilenameFilter filter) {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("路径不存在！！！！！");
        }
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("输入的不是目录！！！！！");
        }
        return file.listFiles(filter);
    }

    /**
     * 分表文件夹下的所有符合条件的文件
     * @param path 路径
     * @param dirName 文件夹名
     * @return File[]
     */
    public static File[] getFilesInMergeDir(String path, String dirName) {
        return getFilesInMergeDir(path, (dir, name) -> {
            if (checkFileNameSuffix(name)) {
                for (var str : name.split("_")) {
                    if (str.equals(dirName)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    /**
     * 删除文件或文件夹
     * @param path path
     */
    public static void delFolder(String path) {
        File file = new File(path);
        if (file.exists()) {
            delFolder(file);
        }
    }

    /**
     * 删除文件或文件夹
     * delete只能删除文件或者空文件夹，所以对于文件夹先清空，再删除
     * @param file File
     */
    private static void delFolder(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (File f : files) {
                    delFolder(f);
                }
                file.delete();
            }
        }
    }

    /**
     * 获得cell的内容
     * @param cell cell
     * @return String
     */
    public static String getCellValueStr(Cell cell) {
        String cellValue = "";
        if (cell == null) {
            return cellValue;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                cellValue = NumberToTextConverter.toText(cell.getNumericCellValue());
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                try {
                    cellValue = cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    cellValue = NumberToTextConverter.toText(cell.getNumericCellValue());
                    if (Objects.equals(cellValue, "0")) {
                        cellValue = "0";
                    }
                }
                break;
//            case BLANK:
//                cellValue = "";
//                break;
//            case ERROR:
//                cellValue = "非法字符";
//                break;
            default:
//                cellValue = "";
                break;
        }
        return cellValue;
    }

    /**
     * 获得读Workbook对象
     * @param file 文件对象
     * @return Workbook
     */
    public static Workbook getReadWorkBook(File file) {
        Workbook workbook = null;
        try (InputStream stream = new FileInputStream(file)) {
            ZipSecureFile.setMinInflateRatio(-1d);
            workbook = WorkbookFactory.create(stream);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.err.println("注意：-----error------" + file.getName() + " 该文件出现错误，如果是总表请重新合表！");
            System.err.println("可能的解决方案：打开" + file.getName() + "变动一下，重新保存一下即可！");
            System.exit(1);
        }
        return workbook;
    }

    /**
     * 复制一个xlsm模板文件
     * @param fileName 文件名
     */
    private static void copyXLXMModelFile(String fileName) {
        try {
            String path = System.getProperty("user.dir") + "/config/Temp_Model.xlsm";
            Files.copy(Paths.get(path), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalArgumentException("xlsm模板文件复制失败！！！！！");
        }
    }

    /**
     * 创建Workbook对象
     * @param fileName 文件名
     * @return Workbook
     */
    public static Workbook createWorkBook(String fileName) {
        Workbook workbook;
        if (fileName.endsWith(XLS)) {
            workbook = new HSSFWorkbook();
        } else if (fileName.endsWith(XLSX)) {
            workbook = new XSSFWorkbook();
        } else if (fileName.endsWith(XLSM)) {
            copyXLXMModelFile(fileName);
            workbook = getReadWorkBook(new File(fileName));
        } else {
            throw new IllegalArgumentException("不支持的格式！！！！！");
        }
        return workbook;
    }

    /**
     * 获得Model文件
     * @param path 路径
     * @return File
     */
    public static File getModelFile(String path) {
        String preName = path + MODEL + ".";
        File file = new File(preName + XLSM);
        if (file.exists()) {
            return file;
        }
        file = new File(preName + XLSX);
        if (file.exists()) {
            return file;
        }
        file = new File(preName + XLS);
        if (file.exists()) {
            return file;
        }
        throw new IllegalArgumentException("not found Model file!!!!!");
    }

    /**
     * 保证总表cell的内容格式
     */
    public static void setCellValueByType(Cell c, String type, String value) {
        switch (type) {
            case "int":
                c.setCellValue(Integer.parseInt(value));
                break;
            case "long":
                c.setCellValue(Long.parseLong(value));
                break;
            case "float":
                c.setCellValue(Float.parseFloat(value));
                break;
            case "double":
                c.setCellValue(Double.parseDouble(value));
                break;
            default:
                c.setCellValue(value);
        }
    }
}
