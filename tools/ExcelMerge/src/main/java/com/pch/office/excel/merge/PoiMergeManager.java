package com.pch.office.excel.merge;

import com.pch.office.FileRecord;
import com.pch.office.Utils;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * 一些static方法管理类
 */
public class PoiMergeManager {

    private static AbstractPoiMerge getInstance(EMergeType eMergeType) {
        AbstractPoiMerge abstractPoiOrder = null;
        switch (eMergeType) {
            case NO:
                abstractPoiOrder = new PoiDefaultMerge();
                break;
            case ASC:
            case DES:
                abstractPoiOrder = new PoiOrderMerge(eMergeType);
                break;
        }
        return abstractPoiOrder;
    }

    public static void updateIndex(String mergePath) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(mergePath))) {
            var write = Files.newBufferedWriter(Paths.get(mergePath + "index.txt"), CREATE, WRITE, TRUNCATE_EXISTING);
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    try {
                        var fileName = p.getFileName().toString();
                        var fileTime = Files.getLastModifiedTime(p);
                        write.write(fileName + "=" + fileTime.toString());
                        write.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            write.flush();
            write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> findNeedMergeExcel(String mergePath) {
        // 变动的判断 仅仅依靠时间 更好的是使用时间+length
        List<String> list = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(mergePath))) {
            Map<String, String> record = FileRecord.readRecordFile(mergePath + "index.txt");
            stream.forEach(p -> {
                if (Files.isDirectory(p)) {
                    try {
                        var fileName = p.getFileName().toString();
                        var fileTime = Files.getLastModifiedTime(p);
                        if (!record.containsKey(fileName)) {
                            list.add(fileName);
                        } else {
                            String timestamp = record.get(fileName);
                            if (!Objects.equals(timestamp, fileTime.toString())) {
                                list.add(fileName);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    static void runMergeTask(EMergeType mergeType, String srcPath, String outPath) {
        getInstance(mergeType).performMerge(srcPath + "/", outPath);
    }

    private static void autoPerformMergeOpr(String srcPath, String outPath) {
        performMergeOpr(findNeedMergeExcel(srcPath), srcPath, outPath);
        updateIndex(srcPath);
    }

    private static void performMergeOpr(List<String> list, String srcPath, String outPath) {
        if (list.size() <= 0) {
            return;
        }
        CountDownLatch count = new CountDownLatch(list.size());
        for (String string : list) {
            System.out.println(string + "表正在进行合表操作！请耐心等候！！！");
            new Thread(() -> {
                runMergeTask(EMergeType.NO, srcPath + string, outPath);
                count.countDown();
            }).start();
        }
        try {
            count.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergeOpr(int key, String srcPath, String outPath) {
        switch (key) {
            // 自动检测
            case 1:
                autoPerformMergeOpr(srcPath, outPath);
                break;
            // 手动选择
            case 2:
                try {
                    new MergeGUI(srcPath, outPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "合并出错，请查看error.log!");
                }
                break;
            // 全部合表
            case 3:
                performMergeOpr(Utils.getDirNamesInPath(srcPath), srcPath, outPath);
                break;
            default:
        }
    }
}
