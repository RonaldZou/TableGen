package com.pch.office.excel.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.pch.office.excel.merge.PoiMergeManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.pch.office.Utils.delFolder;
import static com.pch.office.Utils.getModelFile;
import static com.pch.office.excel.easyexcel.EasyManager.getTempDir;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * 第一步：读取Model
 */
public class EasyFirst extends EasyAbstract{

    public static void main(String[] args) {
        easyFirst();
    }

    private static void easyFirst() {
        String tempDir = getTempDir();
        delFolder(tempDir);
        String outPath = "../Floder/";
        List<String> files = PoiMergeManager.findNeedMergeExcel(outPath);
        if (files.isEmpty()) {
            return;
        }
        for (String fileDir : files) {
            EasyExcel.read(getModelFile(outPath + fileDir + "/"), new ModelListener(fileDir)).doReadAll();
            System.out.println(fileDir + "正在进行合表！！！");
        }
        try (var w = Files.newBufferedWriter(Paths.get(tempDir + "files.txt"), CREATE, WRITE, TRUNCATE_EXISTING)) {
            for (var v : files) {
                w.write(v);
                w.newLine();
            }
            w.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
