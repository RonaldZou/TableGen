package com.pch.office.csv.gen;

import com.opencsv.CSVReader;
import com.pch.office.Utils;

import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * @author pch
 * 根据csv生成lua
 */
public class CSVToLua extends CSVToAbstract {

    public CSVToLua(String csvOutPath, String luaPath, String tempPath) {
        super(csvOutPath, luaPath, tempPath);
        recordName = "clientRecord.txt";
    }

    @Override
    public void toFile(File file) {
        try {
            Map<String, Object> rootMap = new HashMap<>();
            FileReader reader = new FileReader(file);
            CSVReader csvReader = new CSVReader(reader);
            String[] csList = csvReader.readNext();
            List<Integer> csIndex = new ArrayList<>();
            for (int i = 0; i < csList.length; i++) {
                if (csList[i].contains("c")) {
                    csIndex.add(i);
                }
            }
            if (csIndex.isEmpty()) {
                return;
            }
            var csvName = file.getName().substring(0, file.getName().lastIndexOf("."));
            var luaName = "Conf" + csvName;
            rootMap.put("entityName", luaName);
            Set<Map<String, String>> props = new LinkedHashSet<>();
            String[] typeList = csvReader.readNext();
            String[] nameList = csvReader.readNext();
            for (int i : csIndex) {
                Map<String, String> infoMap = new LinkedHashMap<>();
                if (typeList[i].isBlank()) {
                    continue;
                }
                infoMap.put("type", typeList[i].strip());
                infoMap.put("name", nameList[i]);
                props.add(infoMap);
            }
            csvReader.close();
            rootMap.put("fields", props);
            rootMap.put("excelName", nameMap.getOrDefault(luaName, "@-@!"));
            writeFile(Utils.getPath(filePath + luaName + ".lua"), "clientConf.ftl", rootMap);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
