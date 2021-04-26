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

import static com.pch.office.Utils.isNotSheet;
import static com.pch.office.excel.easyexcel.EasyManager.*;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * 读取Model内容监听
 */
public class ModelListener extends AnalysisEventListener<Map<Integer, String>> {
    private String outPath;
    private Map<String, List<List<String>>> excelContend = new HashMap<>();

    ModelListener(String outPath) {
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
        String path = EasyManager.getTempDir() + outPath + "/";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        int index = sheetName.lastIndexOf("|");
        String sName = sheetName.substring(index + 1);
        try (var w = Files.newBufferedWriter(Paths.get(path + sName + TXT), CREATE, WRITE, TRUNCATE_EXISTING)) {
            List<List<String>> data = excelContend.get(sheetName);
            for (var d : data) {
                w.write(JSON.toJSONString(d));
                w.newLine();
            }
            w.flush();
            var q = Files.newBufferedWriter(Paths.get(path + SHEET_NAME + TXT), CREATE, WRITE, APPEND);
            q.write(sName + "=" + sheetName);
            q.newLine();
            q.flush();
            q.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            excelContend.remove(sheetName);
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
