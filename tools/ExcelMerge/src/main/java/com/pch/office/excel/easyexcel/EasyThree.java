package com.pch.office.excel.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.fastjson.JSON;
import com.pch.office.FileRecord;
import com.pch.office.excel.merge.PoiMergeManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pch.office.Utils.SUFFIX;
import static com.pch.office.Utils.delFolder;
import static com.pch.office.excel.easyexcel.EasyManager.*;

/**
 * @author pch
 * 第三步：进行合表
 */
public class EasyThree extends EasyAbstract  {
    public static void main(String[] args) {
        easyThree();
    }

    private static void easyThree() {
        String outPath = "../";
        List<String> files = EasyManager.getFiles();
        if (files.isEmpty()) {
            return;
        }
        String tempRootDir = getTempDir();
        for (String file : files) {
            try {
                String path = outPath + file + SUFFIX;
                Files.deleteIfExists(Paths.get(path));
                String tempDir = tempRootDir + file;
                File[] fs = new File(tempDir).listFiles();
                if (fs == null) {
                    return;
                }
                Map<String, String> map = FileRecord.readRecordFile(tempDir + "/" + SHEET_NAME + TXT);
                ExcelWriter excelWriter = EasyExcel.write(path).build();
                for (int i = 0; i < fs.length; i++) {
                    String name = fs[i].getName();
                    name = name.substring(0, name.length() - 4);
                    if (name.equals(SHEET_NAME)) {
                        continue;
                    }
                    WriteSheet sheet = EasyExcel.writerSheet(i, map.get(name)).build();
                    WriteTable table = EasyExcel.writerTable().build();
                    table.setAutomaticMergeHead(false);
                    excelWriter.write(Files.lines(Paths.get(tempDir + "/" + name + TXT))
                            .map(p -> JSON.parseArray(p, String.class)).collect(Collectors.toList()), sheet, table);
                }
                excelWriter.finish();
                System.out.println(file + "合表完成！！！");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        delFolder(tempRootDir);
        PoiMergeManager.updateIndex(outPath + "Floder/");
    }
}
