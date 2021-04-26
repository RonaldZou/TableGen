package com.pch.office.excel.easyexcel;

import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.util.List;

import static com.pch.office.Utils.MODEL;
import static com.pch.office.Utils.getFilesInMergeDir;

/**
 * @author pch
 * 第二步：读取分表内容
 */
public class EasyTwo  extends EasyAbstract {
    public static void main(String[] args) {
        easyTwo();
    }

    private static void easyTwo() {
        String outPath = "../Floder/";
        List<String> files = EasyManager.getFiles();
        if (files.isEmpty()) {
            return;
        }
        for (String fileDir : files) {
            File[] str = getFilesInMergeDir(outPath + fileDir, fileDir);
            for (var s : str) {
                String name = s.getName();
                if (!name.equals(MODEL)) {
                    EasyExcel.read(s, new ContentListener(fileDir)).doReadAll();
                }
            }
        }
    }
}
