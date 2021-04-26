package com.pch.office.excel.easyexcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pch.office.Utils.CONT;
import static com.pch.office.excel.easyexcel.EasyManager.TXT;
import static com.pch.office.Utils.isNotSheet;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * 读取分表内容监听
 */
public class ContentListener extends AnalysisEventListener<Map<Integer, String>> {

    private String outPath;

    private Map<String, List<List<String>>> excelContend = new HashMap<>();

    ContentListener(String outPath) {
        this.outPath = outPath;
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        String sheetName = context.readSheetHolder().getSheetName();
        if (isNotSheet(sheetName)) {
            return;
        }
        if (headMap.isEmpty()) {
            return;
        }
        excelContend.computeIfAbsent(sheetName, p -> new ArrayList<>()).add(new ArrayList<>(headMap.values()));
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        String sheetName = context.readSheetHolder().getSheetName();
        if (isNotSheet(sheetName)) {
            return;
        }
        excelContend.computeIfAbsent(sheetName, p -> new ArrayList<>()).add(new ArrayList<>(data.values()));
    }

    /**
     * 读完一个sheet回调
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        String sheetName = context.readSheetHolder().getSheetName();
        if (isNotSheet(sheetName)) {
            return;
        }
        List<List<String>> data = excelContend.remove(sheetName);
        if (data.size() < CONT) {
            throw new IllegalArgumentException(sheetName + " 行数小于4，请检查配置表！！！");
        }
        int key = sheetName.lastIndexOf("|");
        String path = EasyManager.getTempDir() + outPath + "/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (var w = Files.newBufferedWriter(Paths.get(path + sheetName.substring(key + 1) + TXT), CREATE, WRITE, APPEND)) {
            for (var d : data.subList(CONT, data.size())) {
                w.write(JSON.toJSONString(d));
                w.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        exception.printStackTrace();
        if (exception instanceof ExcelDataConvertException) {
            String fileName = context.readWorkbookHolder().getFile().getName();
            String sheetName = context.readSheetHolder().getSheetName();
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException) exception;
            System.err.println(fileName + "-->" + sheetName + "的第" + excelDataConvertException.getRowIndex() + "行，第" + excelDataConvertException.getColumnIndex() + "列解析异常");
        }
        System.err.println("--------检查数据，如果数据没有问题，请重新执行一次！！----------");
        System.exit(1);
    }

}
