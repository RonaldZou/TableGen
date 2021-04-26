package com.pch.office.csv.gen;

import com.opencsv.CSVReader;
import com.pch.office.Utils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * @author pch
 * 根据csv生成Java
 */
public class CSVToJava extends CSVToAbstract {
    public CSVToJava(String csvOutPath, String javaPath, String tempPath) {
        super(csvOutPath, javaPath, tempPath);
    }

    @Override
    public void toFile(File file) {
        try {
            var rootMap = new HashMap<String, Object>();
            FileReader reader = new FileReader(file);
            CSVReader csvReader = new CSVReader(reader);
            String[] csList = csvReader.readNext();
            List<Integer> csIndex = new ArrayList<>();
            for (int i = 0; i < csList.length; i++) {
                if (csList[i].contains("s")) {
                    csIndex.add(i);
                }
            }
            if (csIndex.isEmpty()) {
                return;
            }
            int dynamicFieldCount = 0;
            Map<String, Map<String, String>> dynamicInfo = new LinkedHashMap<>();
            String csvName = file.getName().substring(0, file.getName().lastIndexOf("."));
            String javaName = "Conf" + csvName;
            rootMap.put("entityName", javaName);
            rootMap.put("csvName", csvName);
            Set<Map<String, String>> props = new LinkedHashSet<>();
            String[] typeList = csvReader.readNext();
            String[] nameList = csvReader.readNext();
            String[] desList = csvReader.readNext();
            for (int i : csIndex) {
                try {
                    Map<String, String> infoMap = new LinkedHashMap<>();
                    var paramType = getStrType(typeList[i]);
                    infoMap.put("type", paramType);
                    if (Objects.equals(nameList[i], "sn")) {
                        rootMap.put("idType", paramType.equalsIgnoreCase("int") ? "Integer" : paramType);
                    }
                    infoMap.put("name", nameList[i]);
                    String note = StringUtils.stripEnd(desList[i].strip(), "\n");
                    infoMap.put("note", note.replaceAll("\n", "\n\t "));
                    infoMap.put("kNote", note.replaceAll("\n", "\n\t\t "));
                    if (csList[i].contains("i")) {
                        infoMap.put("index", "");
                    }
                    props.add(infoMap);
                    if (csList[i].contains("d")) {
                        dynamicInfo.put(nameList[i], infoMap);
                        if (!Objects.equals(nameList[i], "sn")) {
                            infoMap.put("unitBit", "0x" + Integer.toHexString((int) Math.pow(2, dynamicFieldCount)));
                            dynamicFieldCount++;
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException(file.getName() + " col = " + i + "数据有问题！！！");
                }
            }
            csvReader.close();
            rootMap.put("indexs", genJavaConfIndex(props));
            rootMap.put("properties", props);
            rootMap.put("params", getParams(props));
            rootMap.put("packageName", "com.kwai.clover.common.config");
            if (!rootMap.containsKey("idType")) {
                throw new IllegalArgumentException(javaName + " 必须包含sn属性！！！！");
            }
            rootMap.put("excelName", nameMap.getOrDefault(javaName, "@-@!"));
            writeFile(Utils.getPath(filePath + javaName + ".java"), "ExcelToJava.ftl", rootMap);

            var dynamicRootMap = new HashMap<String, Object>();
            dynamicRootMap.put("entityName", javaName);
            dynamicRootMap.put("packageName", "com.kwai.clover.common.config");
            Set<Map<String, String>> danymicFields = new LinkedHashSet<>();
            //字段名
            dynamicRootMap.put("danymicFields", danymicFields);
            dynamicInfo.remove("sn");
            if (dynamicInfo.isEmpty()) {
                return;
            }
            //遍历字段
            for (Map.Entry<String, Map<String, String>> entity : dynamicInfo.entrySet()) {
                //Java实体信息
                danymicFields.add(entity.getValue());
            }
            Set<Map<String, String>> confFields = new LinkedHashSet<>();
            //字段名
            dynamicRootMap.put("confFields", confFields);
            for (Map<String, String> entity : props) {
                if (dynamicInfo.containsKey(entity.get("name"))) {
                    continue;
                }
                confFields.add(entity);
            }
            writeFile(Utils.getPath(filePath + "Danymic" + javaName + ".java"), "ExcelToJavaDanymic.ftl", dynamicRootMap);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 支持string
     * @param strType 数据类型
     * @return String
     */
    private String getStrType(String strType) {
        if (strType.contains("string")) {
            return strType.replace("string", "String");
        }
        return strType;
    }

    private List<Map<String, String>> getParams(Set<Map<String, String>> props) {
        List<Map<String, String>> params = new ArrayList<>();
        for (var f : props) {
            String type = f.get("type");
            String name = f.get("name");
            Map<String, String> pa = new HashMap<>();
            params.add(pa);
            pa.put("name", name);
            if (type.equalsIgnoreCase("int")) {
                pa.put("value", "data.getIntValue(\"" + name + "\")");
            } else if (type.equalsIgnoreCase("boolean")) {
                pa.put("value", "data.getBooleanValue(\"" + name + "\")");
            } else if (type.equalsIgnoreCase("double")) {
                pa.put("value", "data.getDoubleValue(\"" + name + "\")");
            } else if (type.equalsIgnoreCase("long")) {
                pa.put("value", "data.getLongValue(\"" + name + "\")");
            } else if (type.equalsIgnoreCase("float")) {
                pa.put("value", "data.getFloatValue(\"" + name + "\")");
            } else if (type.startsWith("float[")) {
                List<String> splits = parseSplit(type);
                if (splits.size() == 1) {
                    f.put("type", "float[]");
                    pa.put("value", "parseFloatArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
                } else {
                    f.put("type", "float[][]");
                    pa.put("value", "parseFloatArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\", \"" + splits.get(1) + "\")");
                }
            } else if (type.startsWith("double[")) {
                List<String> splits = parseSplit(type);
                if (splits.size() == 1) {
                    f.put("type", "double[]");
                    pa.put("value", "parseDoubleArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
                } else {
                    f.put("type", "double[][]");
                    pa.put("value", "parseDoubleArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\", \"" + splits.get(1) + "\")");
                }
            } else if (type.startsWith("String[")) {
                List<String> splits = parseSplit(type);
                if (splits.size() == 1) {
                    f.put("type", "String[]");
                    pa.put("value", "parseStringArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
                } else {
                    f.put("type", "String[][]");
                    pa.put("value", "parseStringArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\", \"" + splits.get(1) + "\")");
                }
            } else if (type.startsWith("int[")) {
                List<String> splits = parseSplit(type);
                if (splits.size() == 1) {
                    f.put("type", "int[]");
                    pa.put("value", "parseIntArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
                } else {
                    f.put("type", "int[][]");
                    pa.put("value", "parseIntArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\", \"" + splits.get(1) + "\")");
                }
            } else if (type.startsWith("boolean[")) {
                List<String> splits = parseSplit(type);
                if (splits.size() == 1) {
                    f.put("type", "boolean[]");
                    pa.put("value", "parseBoolArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
                } else {
                    f.put("type", "boolean[][]");
                    pa.put("value", "parseBoolArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\", \"" + splits.get(1) + "\")");
                }
            } else if (type.startsWith("long[")) {
                List<String> splits = parseSplit(type);
                if (splits.size() == 1) {
                    f.put("type", "long[]");
                    pa.put("value", "parseLongArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
                } else {
                    f.put("type", "long[][]");
                    pa.put("value", "parseLongArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\", \"" + splits.get(1) + "\")");
                }
            } else if ("vector2d".equalsIgnoreCase(type)) {
                f.put("type", "com.kwai.clover.common.support.Vector2D");
                pa.put("value", "com.kwai.clover.common.support.VectorUtils.parseVector2d(data.getString(\"" + name + "\"))");
            } else if (type.startsWith("vector2d[")) {
                List<String> splits = parseSplit("String[]");
                f.put("type", "com.kwai.clover.common.support.Vector2D[]");
                pa.put("value", "com.kwai.clover.common.support.VectorUtils.parseVector2dArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
            } else if ("vector3d".equalsIgnoreCase(type)) {
                f.put("type", "com.kwai.clover.common.support.Vector3D");
                pa.put("value", "com.kwai.clover.common.support.VectorUtils.parseVector3d(data.getString(\"" + name + "\"))");
            } else if (type.startsWith("vector3d[")) {
                List<String> splits = parseSplit("String[]");
                f.put("type", "com.kwai.clover.common.support.Vector3D[]");
                pa.put("value", "com.kwai.clover.common.support.VectorUtils.parseVector3dArray(data.getString(\"" + name + "\"), \"" + splits.get(0) + "\")");
            } else {
                pa.put("value", "data.getString(\"" + name + "\")");
            }
        }
        return params;
    }

    private static String[] defaultSplit = {",", ";"};

    private List<String> parseSplit(String type) {
        List<String> splits = new ArrayList<>();
        int firstIndex = type.indexOf("[") + 1;
        int defaultIndex = 0;
        if (type.charAt(firstIndex) == ']') {
            splits.add(defaultSplit[defaultIndex]);
        } else {
            splits.add(convertToString(type.charAt(firstIndex)));
        }
        if (Objects.equals(splits.get(0), defaultSplit[0])) {
            ++defaultIndex;
        }
        firstIndex = type.indexOf("[", firstIndex) + 1;
        if (firstIndex == 0) {
            return splits;
        }
        if (type.charAt(firstIndex) == ']') {
            splits.add(0, defaultSplit[defaultIndex]);
        } else {
            splits.add(convertToString(type.charAt(firstIndex)));
        }
        return splits;
    }

    private String convertToString(char c) {
        StringBuilder result = new StringBuilder();
        if (c == '.' || c == '|') {
            result.append("\\\\");
        }
        result.append(c);
        return result.toString();
    }

    private static class FieldInfo {
        private String type;
        private String name;

        private FieldInfo(Map<String, String> info) {
            this.type = info.get("type");
            this.name = info.get("name");
        }
    }

    private static class TestCombine {
        private List<FieldInfo> list;
        private List<List<FieldInfo>> results = new ArrayList<>();

        private TestCombine(List<FieldInfo> list) {
            this.list = list;
        }

        private List<List<FieldInfo>> getCombine(int num) {
            results.clear();
            int[] result = new int[num];
            _getCombine(0, result, num, num);
            return results;
        }

        private void _getCombine(int start, int[] result, int count, int num) {
            for (int i = start; i < list.size() + 1 - count; ++i) {
                result[count - 1] = i;
                if (count - 1 == 0) {
                    List<FieldInfo> ret = new ArrayList<>();
                    for (int j = num - 1; j >= 0; --j) {
                        ret.add(list.get(result[j]));
                    }
                    results.add(ret);
                } else {
                    _getCombine(i + 1, result, count - 1, num);
                }
            }
        }
    }

    private Set<Map<String, String>> genJavaConfIndex(Set<Map<String, String>> f) {
        Set<Map<String, String>> indexs = new LinkedHashSet<>();
        // 抽出需要建立索引的字段
        List<FieldInfo> indexFields = new ArrayList<>();
        for (Map<String, String> entry : f) {
            // 该项不索引
            if (!entry.containsKey("index")) {
                continue;
            }
            // sn不索引
            if (Objects.equals(entry.get("name"), "sn")) {
                continue;
            }
            String dataType = entry.get("type");
            if (dataType.equalsIgnoreCase("String")
                    || dataType.equalsIgnoreCase("int")
                    || dataType.equalsIgnoreCase("long")) {
                indexFields.add(new FieldInfo(entry));
            } else {
                System.err.println("警告：Index只支持String/int/long类型 !");
            }
        }
        if (indexFields.isEmpty()) {
            return indexs;
        }
        if (indexFields.size() <= 3) {
            // 取出全部组合
            for (int i = 1; i <= indexFields.size(); ++i) {
                // n个里面取i个的全部组合
                TestCombine combines = new TestCombine(indexFields);
                genIndexs(indexs, combines.getCombine(i));
            }
        } else {
            System.err.println("警告：Index组合键太多，最多只支持3个及以内的组合，将只生成单键索引");
            // 只生成单键索引
            genIndexs(indexs, new TestCombine(indexFields).getCombine(1));
        }
        return indexs;
    }

    private static void genIndexs(Set<Map<String, String>> indexs, List<List<FieldInfo>> combines) {
        for (List<FieldInfo> combine : combines) {
            Map<String, String> index = new HashMap<>();
            index.put("name", genIndexName(combine));
            index.put("paramsWithType", genIndexParamsWithType(combine));
            index.put("params", genIndexParams(combine));
            index.put("paramsInit", genIndexParamsInit(combine));
            indexs.add(index);
        }
    }

    private static String genIndexName(List<FieldInfo> combine) {
        StringJoiner sb = new StringJoiner("And");
        for (FieldInfo field : combine) {
            sb.add(Utils.toUpperCaseFirstOne(field.name));
        }
        return sb.toString();
    }

    private static String genIndexParamsWithType(List<FieldInfo> combine) {
        StringJoiner sb = new StringJoiner(", ");
        for (FieldInfo field : combine) {
            sb.add(field.type + " " + field.name);
        }
        return sb.toString();
    }

    private static String genIndexParams(List<FieldInfo> combine) {
        StringJoiner sb = new StringJoiner(", ");
        for (FieldInfo field : combine) {
            sb.add(field.name);
        }
        return sb.toString();
    }

    private static String genIndexParamsInit(List<FieldInfo> combine) {
        StringJoiner sb = new StringJoiner(", ");
        for (FieldInfo field : combine) {
            if (field.type.equalsIgnoreCase("String")) {
                sb.add("conf.getString(\"" + field.name + "\")");
            } else if (field.type.equalsIgnoreCase("int")) {
                sb.add("conf.getIntValue(\"" + field.name + "\")");
            } else if (field.type.equalsIgnoreCase("long")) {
                sb.add("conf.getLongValue(\"" + field.name + "\")");
            }
        }
        return sb.toString();
    }
}
