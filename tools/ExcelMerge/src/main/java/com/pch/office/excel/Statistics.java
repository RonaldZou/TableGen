package com.pch.office.excel;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static com.pch.office.Utils.*;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * 统计客户端配置中使用的中文汉字
 */
public class Statistics {
    public static void main(String[] args) {
        String excelPath;
        String outPath;
        if (args.length > 0) {
            excelPath = args[0];
            outPath = args[1];
        } else {
            excelPath = "C:/sandbox/trunk/public/config/";
            outPath = "./word.txt";
        }
        runStatistics(excelPath, outPath);
    }

    private static void runStatistics(String excelPath, String outPath) {
        var allFiles = getFilesInMergeDir(excelPath, (dir, name) -> checkFileNameSuffix(name));
        // 多线程进行统计
        class ReadFileTask extends RecursiveTask<Set<Character>> {
            private static final int THRESHOLD = 30;
            private final int low, high;

            private ReadFileTask(int low, int high) {
                this.low = low;
                this.high = high;
            }

            @Override
            protected Set<Character> compute() {
                if (high - low < THRESHOLD) {
                    Set<Character> result = new HashSet<>();
                    for (int q = low; q <= high; q++) {
                        var workbook = getReadWorkBook(allFiles[q]);
                        for (var i = 0; i < workbook.getNumberOfSheets(); i++) {
                            var readSheet = workbook.getSheetAt(i);
                            var readSheetName = readSheet.getSheetName();
                            if (!readSheetName.contains("|")) {
                                continue;
                            }
                            var nameRow = readSheet.getRow(NAME);
                            var typeRow = readSheet.getRow(TYPE);
                            var rowIndex = new ArrayList<Integer>();
                            // 统计客户端使用列的index
                            for (int j = 0; j < nameRow.getLastCellNum(); j++) {
                                var str = getCellValueStr(nameRow.getCell(j));
                                if (!str.isBlank() && str.contains("c")) {
                                    str = getCellValueStr(typeRow.getCell(j));
                                    if (!str.isBlank() && str.contains("String")) {
                                        rowIndex.add(j);
                                    }
                                }
                            }
                            // 只遍历客户端用到的cell
                            for (var j = CONT; j <= readSheet.getLastRowNum(); j++) {
                                var readRow = readSheet.getRow(j);
                                if (readRow == null) {
                                    continue;
                                }
                                if (getCellValueStr(readRow.getCell(0)).isBlank()) {
                                    continue;
                                }
                                for (var k = 0; k < rowIndex.size(); k++) {
                                    var cell = readRow.getCell(k);
                                    if (cell == null) {
                                        continue;
                                    }
                                    var temp = getCellValueStr(cell);
                                    if (temp.isBlank()) {
                                        continue;
                                    }
                                    char[] cArray = temp.toCharArray();
                                    for (var c : cArray) {
                                        if (isChinese(c)) {
                                            result.add(c);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return result;
                } else {
                    var middle = low + THRESHOLD;
                    var leftTask = new ReadFileTask(low, middle - 1);
                    var rightTask = new ReadFileTask(middle, high);
                    rightTask.fork();
                    var left = leftTask.compute();
                    var right = rightTask.join();
                    left.addAll(right);
                    return left;
                }
            }
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Set<Character> result = forkJoinPool.invoke(new ReadFileTask(0, allFiles.length - 1));
        forkJoinPool.shutdown();
        try (var bw = Files.newBufferedWriter(Paths.get(outPath), CREATE, WRITE, TRUNCATE_EXISTING)) {
            for (var s : result) {
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
