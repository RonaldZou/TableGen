package com.pch.office.main;

import com.pch.office.csv.gen.CSVToLua;

/**
 * @author pch
 */
public class MainLua {
    public static void main(String[] args) {
        String luaPath;
        String csvPath;
        String configPath;
        if (args.length == 3) {
            luaPath = args[0];
            csvPath = args[1];
            configPath = args[2];
        } else {
            luaPath = "C:\\sandbox\\trunk\\public\\config\\";
            csvPath = "C:\\sandbox\\trunk\\public\\config\\data\\csv\\";
            configPath = "C:\\sandbox\\trunk\\public\\config\\";
        }
        new CSVToLua(luaPath, csvPath, configPath).genFiles();
    }

}
