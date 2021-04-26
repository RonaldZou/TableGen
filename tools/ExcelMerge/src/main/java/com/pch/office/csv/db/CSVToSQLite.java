package com.pch.office.csv.db;

import com.opencsv.CSVReader;
import com.pch.office.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * @author pch
 * 生成sqlite
 */
public class CSVToSQLite {

    private String csvPath;

    public CSVToSQLite(String csvPath) {
        this.csvPath = csvPath;
        File file = new File("config.db");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 生成SQLite操作
     */
    public void genDB() {
        // 统计所有数据
        File[] allFiles = Utils.getFilesInMergeDir(csvPath, (dir, name) -> name.endsWith("csv"));
        class GenFile extends RecursiveTask<Map<String, List<Object>>> {
            private static final int THRESHOLD = 40;
            private final int low, high;

            private GenFile(int low, int high) {
                this.low = low;
                this.high = high;
            }

            @Override
            protected Map<String, List<Object>> compute() {
                if (high - low < THRESHOLD) {
                    Map<String, List<Object>> map = new HashMap<>();
                    for (int i = low; i <= high; i++) {
                        File file = allFiles[i];
                        genSQLData(file, map);
                    }
                    return map;
                } else {
                    int middle = low + THRESHOLD;
                    GenFile leftTask = new GenFile(low, middle - 1);
                    GenFile rightTask = new GenFile(middle, high);
                    rightTask.fork();
                    Map<String, List<Object>> left = leftTask.compute();
                    Map<String, List<Object>> right = rightTask.join();
                    left.putAll(right);
                    return left;
                }
            }
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        var sqlData = forkJoinPool.invoke(new GenFile(0, allFiles.length - 1));
        forkJoinPool.shutdown();
        // 把所有数据写入到SQLite
        writeDataToDB(sqlData);
    }

    /**
     * 写db，先写到缓存，最后提交，这样就加快了写入速度，提速是数量级的提升
     * @param sqlData 缓存的sql语句
     */
    @SuppressWarnings("unchecked")
    private void writeDataToDB(Map<String, List<Object>> sqlData) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:config.db");
            // 设置成手动提交
            connection.setAutoCommit(false);
            for (Map.Entry<String, List<Object>> c : sqlData.entrySet()) {
                List<Object> allSql = c.getValue();
                Statement ps = connection.createStatement();
                ps.execute((String) allSql.get(0));
                System.out.println(c.getKey() + "建表完成！！！！！！！！");
                // 每个sheet内容采用预编译的方式
                PreparedStatement pps = connection.prepareStatement((String) allSql.get(1));
                for (var insetData : (List<List<String>>) allSql.get(2)) {
                    for (int i = 0; i < insetData.size(); i++) {
                        pps.setObject(i + 1, insetData.get(i));
                    }
                    pps.addBatch();
                }
                pps.executeBatch();
                System.out.println(c.getKey() + "插入数据完成！！！！！！！！");
            }
            // 提交
            connection.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 根据文件内容生成sql语句
     * @param file 文件
     * @param map sql缓存
     */
    private void genSQLData(File file, Map<String, List<Object>> map) {
        try {
            int in = file.getName().lastIndexOf(".");
            String csvName = file.getName().substring(0, in);
            String tableName = "Conf" + csvName;
            StringBuilder createTable = new StringBuilder();
            createTable.append("CREATE TABLE " + "`").append(tableName).append("` (");
            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> allLines = new ArrayList<>();
            String[] next;
            while ((next = csvReader.readNext()) != null) {
                allLines.add(next);
            }
            List<String> csList = Arrays.asList(allLines.get(0));
            List<Integer> indexList = new ArrayList<>();
            for (int i = 0; i < csList.size(); i++) {
                if (csList.get(i).contains("c")) {
                    indexList.add(i);
                }
            }
            if (indexList.isEmpty()) {
                return;
            }
            List<String> typeList = Arrays.asList(allLines.get(1));
            List<String> nameList = Arrays.asList(allLines.get(2));
            String typeName;
            String fieldName;
            List<String> set = new ArrayList<>();

            StringBuilder insertTable = new StringBuilder();
            insertTable.append("INSERT INTO ").append(tableName).append(" (");

            StringJoiner stringJoiner = new StringJoiner(", ");
            for (int i : indexList) {
                typeName = typeList.get(i);
                fieldName = nameList.get(i).strip().toLowerCase();
                if (!set.contains(fieldName)) {
                    set.add(fieldName);
                } else {
                    System.err.println("-------重复字段---------" + tableName + " " + fieldName);
                    System.exit(1);
                }
                stringJoiner.add("`" + fieldName + "`");
                if (typeName.contains("vector") || typeName.contains("[]") || typeName.equalsIgnoreCase("string")) {
                    typeName = "text";
                    createTable.append("`").append(fieldName).append("` ").append(typeName).append(fieldName.equalsIgnoreCase("sn")
                            ? " NOT NULL," : " DEFAULT '',");
                } else if (typeName.equalsIgnoreCase("bool") || typeName.equalsIgnoreCase("boolean")) {
                    typeName = "int";
                    createTable.append("`").append(fieldName).append("` ").append(typeName).append(" DEFAULT '0',");
                } else {
                    createTable.append("`").append(fieldName).append("` ").append(typeName).append(fieldName.equalsIgnoreCase("sn")
                            ? " NOT NULL," : " DEFAULT '0',");
                }
            }
            if (!set.contains("sn")) {
                return;
            }
            insertTable.append(stringJoiner.toString());
            insertTable.append(") values (");
            String s = "?, ".repeat(indexList.size() - 1);
            s += "? )";
            insertTable.append(s);

            createTable.append("PRIMARY KEY (`sn`)");
            createTable.append(")");
            List<Object> list = new ArrayList<>();
            list.add(createTable.toString());
            list.add(insertTable.toString());

            List<List<String>> allInsertSql = new ArrayList<>();
            list.add(allInsertSql);

            map.put(tableName, list);
            A:for (int i = 4; i < allLines.size(); i++) {
                var strArray = allLines.get(i);
                List<String> data = new ArrayList<>();
                for (int j : indexList) {
                    String str = strArray[j];
                    if (j == 0) {
                        if (str == null || str.isBlank()) {
                            continue A;
                        }
                    }
                    if (str == null || str.isBlank()) {
                        str = "";
                    }
                    try {
                         data.add(getDefaultValue(typeList.get(j), str));
                    }catch (Exception e) {
                        System.err.println("-------类型错误---------" + tableName + " 第"+ (j + 1) +"列");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                allInsertSql.add(data);
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获得默认值
     * @param typeName 字段类型名
     * @param value 字段值
     * @return String
     */
    private String getDefaultValue(String typeName, String value) {
        // 数组类型，非空会进行base64转化
        if (typeName.contains("[]")) {
            if (value.isBlank()) {
                return "";
            }
            return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
        } else if (typeName.contains("vector") || typeName.equalsIgnoreCase("string")) {
            if (value.isBlank()) {
                return "";
            }
            return value;
        } else if (typeName.equalsIgnoreCase("bool") || typeName.equalsIgnoreCase("boolean")) {
            if (value.isBlank()) {
                return "0";
            }
            return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1") ? "1" : "0";
        } else {
            if (value.isBlank()) {
                return "0";
            }
            // 数值类型检查
            if (typeName.equalsIgnoreCase("int")) {
                // 坑爹的隐藏字符
                value = value.replace("\u202D","").replace("\u202C","");
                return Integer.valueOf(value).toString();
            } else if (typeName.equalsIgnoreCase("long")) {
                return Long.valueOf(value).toString();
            } else if (typeName.equalsIgnoreCase("byte")) {
                return Byte.valueOf(value).toString();
            }
            return value;
        }
    }
}
