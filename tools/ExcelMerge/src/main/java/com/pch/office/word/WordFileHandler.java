package com.pch.office.word;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author pch
 * 读取word文档（主要为了处理屏蔽字文档，即doc-txt）
 */
public class WordFileHandler {

    private static int MAX_LENGTH = 15;

    private static void execOpr(String shieldPath, String outPath) {
        Set<String> allWords = new LinkedHashSet<>(5000);
        // 即使使用段落的方式也很蛋疼  那如何区分这一段是title 没有统一的格式
        // 所以有些时候要实现自动化是很困难的（实际因素往往不是按照一定规则来的）
        // 能做的就是尽量减少人员操作的复杂度
        try (FileInputStream fis = new FileInputStream(new File(shieldPath))) {
            XWPFDocument doc = new XWPFDocument(fis);
            doc.getParagraphs().stream().map(XWPFParagraph::getText).filter(p -> !p.isBlank()).map(String::strip)
                    .forEach(p -> {
                        if (p.length() > MAX_LENGTH) {
                            allWords.addAll(Arrays.stream(p.split("、")).map(k -> k.strip().split("，"))
                                    .flatMap(Arrays::stream).filter(k -> !k.isBlank()).map(String::strip)
                                    .collect(Collectors.toSet()));
                        } else {
                            // 输出被过滤出去的
                            System.out.println(p);
                        }
                    });
            BufferedWriter bw = Files.newBufferedWriter(Paths.get(outPath), CREATE, WRITE, TRUNCATE_EXISTING);
            for (var word : allWords) {
                bw.write(word);
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String shieldPath;
        String outPath;
        if (args.length > 0) {
            WordFileHandler.MAX_LENGTH = Integer.parseInt(args[0]);
            shieldPath = args[1];
            outPath = args[2];
        } else {
            shieldPath = "C:\\Users\\admin\\Desktop\\my\\shieldTest\\1.docx";
            outPath = "C:\\Users\\admin\\Desktop\\my\\shieldTest\\shield.txt";
        }
        WordFileHandler.execOpr(shieldPath, outPath);
    }
}
