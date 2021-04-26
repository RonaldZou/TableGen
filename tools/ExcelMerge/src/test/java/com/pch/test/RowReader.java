package com.pch.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类名称：RowReader
 * 类描述：Excel行处理
 */
public class RowReader {

    private static Map<String, List<String[]>> map = new HashMap<>();


    private Integer curRow = -1;

    public void getRows(String sheetName, int curRow, String[] row) {
        if (this.curRow != curRow) {
            map.computeIfAbsent(sheetName, p -> new ArrayList<>()).add(row);
            this.curRow = curRow;
        }

    }
}