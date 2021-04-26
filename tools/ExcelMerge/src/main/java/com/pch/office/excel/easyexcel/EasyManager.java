package com.pch.office.excel.easyexcel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * @author pch
 */
class EasyManager {

    static String SHEET_NAME = "sheetName";
    static String TXT = ".txt";

    /**
     * 获得所有需要合表的文件名
     * @return List<String>
     */
    static List<String> getFiles() {
        String path = getTempDir() + "/files.txt";
        Path pa = Paths.get(path);
        if (!Files.exists(pa)) {
            return Collections.emptyList();
        }
        try {
            return Files.readAllLines(pa);
        } catch (IOException w) {
            throw new IllegalArgumentException("---------读取files.txt出错-------------");
        }
    }

    /**
     * 临时文件夹路径
     * @return String
     */
    static String getTempDir() {
        return System.getProperty("user.dir") + "/config/dir/";
    }
}
