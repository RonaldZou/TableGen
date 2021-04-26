package com.pch.office.main;

import com.pch.office.csv.db.CSVToSQLite;

/**
 * @author pch
 */
public class MainSQLite {
    public static void main(String[] args) {
        String csvPath;
        if (args.length > 0) {
            csvPath = args[0];
        } else {
            csvPath = "C:\\sandbox\\trunk\\public\\config\\data\\csv\\";
        }
        new CSVToSQLite(csvPath).genDB();
    }
}
