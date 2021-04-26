package com.pch.office.excel.merge;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.pch.office.Utils.*;

/**
 * @author pch
 * 有序排列的类（支持按模板的第一列做排序操作）
 */
class PoiOrderMerge extends AbstractPoiMerge {

    private EMergeType mergeType;

    private Map<String, TreeMap<Integer, List<String>>> tempData = new HashMap<>();

    PoiOrderMerge(EMergeType mergeType) {
        this.mergeType = mergeType;
    }

    /**
     * 在创建TreeMap对象时，确定是升序还是降序
     */
    private TreeMap<Integer, List<String>> createTreeMap(EMergeType orderType) {
        if (orderType == EMergeType.DES) {
            return new TreeMap<>((a, b) -> b - a);
        } else {
            return new TreeMap<>();
        }
    }

    @Override
    protected void readExcel(File fileName) {
        Workbook workbook = getReadWorkBook(fileName);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            var readSheet = workbook.getSheetAt(i);
            var readSheetName = readSheet.getSheetName();
            if (!compareTitleRow.containsKey(readSheetName)) {
                continue;
            }
            var compareRow = readSheet.getRow(NAME);
            var tempCompareRow = new ArrayList<String>();
            for (int j = 0; j < compareRow.getPhysicalNumberOfCells(); j++) {
                tempCompareRow.add(getCellValueStr(compareRow.getCell(j)));
            }
            var resultIndex = new ArrayList<Integer>();
            var titleRow = this.compareTitleRow.get(readSheetName);
            for (String s : titleRow) {
                resultIndex.add(tempCompareRow.indexOf(s));
            }
            var titleSize = titleRow.size();
            var rowContent = tempData.computeIfAbsent(readSheetName, k -> createTreeMap(mergeType));
            LOOP:for (int j = CONT; j < readSheet.getPhysicalNumberOfRows(); j++) {
                var readRow = readSheet.getRow(j);
                if (readRow == null) {
                    continue;
                }
                var content = new ArrayList<String>();
                for (int k = 0; k < titleSize; k++) {
                    var tempIndex = resultIndex.get(k);
                    if (tempIndex < 0) {
                        if (k == 0) {
                            continue LOOP;
                        }
                        // 必须插入一个空串，用来占位
                        content.add("");
                        continue;
                    }
                    var cellStr = getCellValueStr(readRow.getCell(tempIndex));
                    if (k == 0 && cellStr.isBlank()) {
                        continue LOOP;
                    }
                    content.add(cellStr);
                }
                rowContent.put(Integer.valueOf(content.get(0)), content);
            }
        }
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void beforeWriteExcel(String filePath) {
        tempData.forEach((k, v) -> {
            Sheet sheet1 = writeWorkbook.getSheet(k);
            var typeList = typeTitleRow.get(k);
            for (List<String> list : v.values()) {
                var row = sheet1.createRow(sheet1.getLastRowNum() + 1);
                for (int j = 0; j < list.size(); j++) {
                    var temp = list.get(j);
                    // 占位的空串，不写入到cell
                    if (temp.isBlank()) {
                        continue;
                    }
                    setCellValueByType(row.createCell(j), typeList.get(j), temp);
                }
            }
        });
    }
}