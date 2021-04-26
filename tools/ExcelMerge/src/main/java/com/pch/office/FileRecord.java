package com.pch.office;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * 记录文件上一次的操作记录
 */
public class FileRecord {
    /** 记录的临时文件名 */
    public final static String INDEX_NAME = "serverRecord.txt";

    /**
     * 读记录
     * @param path path
     * @return Map<String, String>
     */
    public static Map<String, String> readRecordFile(Path path) {
        if (!Files.exists(path)) {
            return Collections.emptyMap();
        }
        try {
            return Files.lines(path).filter(p -> !p.isBlank()).map(p -> p.trim().split("=")).filter(p -> p.length == 2)
                    .collect(Collectors.toMap(p -> p[0], p -> p[1]));
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * 读记录
     * @param configPath 记录文件路径
     * @return Map<String, String>
     */
    public static Map<String, String> readRecordFile(String configPath) {
        Path path = Paths.get(configPath);
        return readRecordFile(path);
    }

    /**
     * 写记录
     * @param configPath 配置路径
     * @param allFiles 文件数组
     * @param recordName 记录文件名
     */
    private static void writeRecordFile(String configPath, File[] allFiles, String recordName) {
        try (var bw = Files.newBufferedWriter(Paths.get(configPath + recordName), CREATE, TRUNCATE_EXISTING, WRITE)) {
            for (var file : allFiles) {
                String fileModifyTime = Files.getLastModifiedTime(file.toPath()).toString();
                bw.write(file.getName() + "=" + fileModifyTime);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 并行执行任务
     * @param allFiles 所有文件
     * @param recordPath 记录路径
     * @param consumer 执行方法
     * @param recordName 记录文件名
     */
    public static void parallelTask(File[] allFiles, String recordPath, Consumer<File> consumer, String recordName) {
        var record = readRecordFile(recordPath + recordName);
        class GenFile extends RecursiveAction {
            private static final int THRESHOLD = 40;
            private final int low, high;

            private GenFile(int low, int high) {
                this.low = low;
                this.high = high;
            }

            @Override
            protected void compute() {
                if (high - low < THRESHOLD) {
                    for (int i = low; i <= high; i++) {
                        File file = allFiles[i];
                        try {
                            String fileModifyTime = Files.getLastModifiedTime(file.toPath()).toString();
                            if (!record.containsKey(file.getName()) || !Objects.equals(record.get(file.getName()), fileModifyTime)) {
                                consumer.accept(file);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    int middle = low + THRESHOLD;
                    GenFile leftTask = new GenFile(low, middle - 1);
                    GenFile rightTask = new GenFile(middle, high);
                    invokeAll(leftTask, rightTask);
                }
            }
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.invoke(new GenFile(0, allFiles.length - 1));
        forkJoinPool.shutdown();
        writeRecordFile(recordPath, allFiles, recordName);
    }

}
