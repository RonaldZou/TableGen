package com.pch.office.csv.gen;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import static com.pch.office.FileRecord.*;
import static com.pch.office.Utils.UTF_8;
import static com.pch.office.Utils.getFilesInMergeDir;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * csv转化基类
 */
public abstract class CSVToAbstract {
    /** csv路径 */
    private String csvPath;
    /** freemarker配置对象 */
    private Configuration configuration;
    /** 所有csv文件 */
    private File[] allFiles;
    /** 输出文件路径 */
    String filePath;
    /** excel名对应记录 */
    Map<String, String> nameMap;
    /** 记录名 */
    String recordName = INDEX_NAME;

    CSVToAbstract(String csvOutPath, String filePath, String tempPath) {
        this.filePath = filePath;
        this.csvPath = csvOutPath;
        configuration = new Configuration();
        try {
            configuration.setDirectoryForTemplateLoading(new File(tempPath));
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setEncoding(Locale.getDefault(), UTF_8);
            nameMap = readRecordFile(tempPath + "record.txt");
            File javaDir = new File(filePath);
            if (!javaDir.exists()) {
                javaDir.mkdir();
            }
            allFiles = getFilesInMergeDir(csvPath, (dir, name) -> name.endsWith("csv"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void genFiles() {
        parallelTask(allFiles, csvPath, this::toFile, recordName);
    }

    public abstract void toFile(File file);

    void writeFile(Path path, String tempName, Object rootMap) {
        try {
            OutputStream outputStream = Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING, WRITE);
            Writer out = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            Template temp = configuration.getTemplate(tempName, UTF_8);
            temp.setEncoding(UTF_8);
            temp.process(rootMap, out);
            out.flush();
            out.close();
            outputStream.close();
            System.out.println(path.getFileName() + "---生成完成!!!!");
        } catch (Exception e) {
            System.err.println();
            System.err.println();
            System.err.println(path + "生成出错了！！！！！！！！！");
            System.err.println();
            System.err.println();
            System.exit(1);
        }
    }
}
