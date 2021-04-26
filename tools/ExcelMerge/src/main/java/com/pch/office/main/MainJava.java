package com.pch.office.main;

import com.pch.office.csv.gen.CSVToJava;

/**
 * @author pch
 * csv生成java启动类
 */
public class MainJava {
    public static void main(String[] args) {
        String javaPath;
        String csvPath;
        String tempPath;
        if (args.length > 0) {
            csvPath = args[0];
            javaPath = args[1];
            tempPath = args[2];
        } else {
            javaPath = "C:\\sandbox\\trunk\\code\\server\\sandbox\\common\\src\\gen\\java\\com\\kwai\\clover\\common\\config\\";
            csvPath = "C:\\sandbox\\trunk\\code\\server\\sandbox\\data\\csv\\";
            tempPath = "C:\\sandbox\\trunk\\public\\config\\gen\\config\\";
        }
        new CSVToJava(csvPath, javaPath, tempPath).genFiles();
    }
}
