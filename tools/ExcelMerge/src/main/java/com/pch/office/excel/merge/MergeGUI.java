package com.pch.office.excel.merge;

import com.pch.office.Utils;
import com.pch.office.excel.arrange.AddSheet;
import com.pch.office.excel.arrange.ArrangeTitle;
import com.pch.office.excel.arrange.ETitleType;
import com.pch.office.excel.arrange.RenewalTitle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author pch
 * 简单的UI 蛋疼的GUI
 */
public class MergeGUI {
    /*** 需要合并的文件 名字需要与文件夹名一致 增加合并文件只需要在这里添加即可**/
    private List<String> mergeFiles;
    /*** 合并类型 **/
    private EMergeType mergeType = EMergeType.NO;
    /*** 整理Title类型 **/
    private ETitleType titleType = ETitleType.ARRANGE;
    /*** 已选中的文件 **/
    private Set<String> choiceFiles = new HashSet<>();

    MergeGUI(String srcPath, String outPath) {
        mergeFiles = Utils.getDirNamesInPath(srcPath);
        init(srcPath, outPath);
    }

    private void init(String srcPath, String outPath) {
        JFrame frame = new JFrame("pch");
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        frame.setLayout(layout);
        frame.setBounds(420, 300, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        JPanel panel1 = new JPanel();
//        panel1.setBorder(BorderFactory.createTitledBorder("选择要合并的文件夹"));
//        mergeFiles.forEach(p -> {
//            JCheckBox jcb = new JCheckBox(p);
//            panel1.add(jcb);
//            jcb.addActionListener(e -> choiceFileOpr(p));
//        });
//        layout.setConstraints(panel1, constraints);
//        frame.add(panel1);

        JPanel panel2 = new JPanel();
        panel2.setBorder(BorderFactory.createTitledBorder("分表整理"));
        JLabel userLabel = new JLabel("选择分表:");

        @SuppressWarnings("unchecked")
        JComboBox comboBox = new JComboBox(mergeFiles.toArray());
        comboBox.addActionListener(e -> {
            Object object = ((JComboBox) e.getSource()).getSelectedItem();
            if (object == null) {
                object = mergeFiles.get(0);
            }
            choiceFiles.clear();
            choiceFiles.add(object.toString());
        });
        panel2.add(userLabel);
        panel2.add(comboBox);


        JLabel userLabel1 = new JLabel("    选择整理方式:");

        @SuppressWarnings("unchecked")
        JComboBox comboBoxTitle = new JComboBox(ETitleType.getArray());
        comboBoxTitle.addActionListener(e -> {
            Object object = ((JComboBox) e.getSource()).getSelectedItem();
            if (object == null) {
                object = ETitleType.ARRANGE.getDes();
            }
            titleType = ETitleType.getETitleType(object.toString());
        });
        panel2.add(userLabel1);
        panel2.add(comboBoxTitle);
        JLabel userLabel2 = new JLabel("    ");
        panel2.add(userLabel2);
        JButton jButton2 = new JButton("执行");
        jButton2.addActionListener(e -> performTitleOpr(srcPath));
        panel2.add(jButton2);

//        @SuppressWarnings("unchecked")
//        JComboBox comboBox = new JComboBox(EMergeType.getArray());
//        comboBox.addActionListener(e -> {
//            Object object = ((JComboBox) e.getSource()).getSelectedItem();
//            if (object == null) {
//                object = EMergeType.NO.getDes();
//            }
//            mergeType = EMergeType.getEMergeType(object.toString());
//        });
//        panel2.add(comboBox);
//        JButton jButton = new JButton("合表");
//        jButton.addActionListener(e -> performMergeOpr(srcPath, outPath));
//        panel2.add(jButton, -1);

        layout.setConstraints(panel2, constraints);
        frame.add(panel2);
        JPanel panel3 = new JPanel();
        panel3.setBorder(BorderFactory.createTitledBorder("使用规则"));
        JTextArea showMsg = new JTextArea();
        showMsg.setFont(new Font("monospaced", Font.PLAIN, 13));
        showMsg.setBackground(Color.BLUE);
        showMsg.setColumns(59);
        showMsg.setRows(11);
        showMsg.setEnabled(false);
        showMsg.setText("首先选中要操作的文件\n" +
                " 1-整理Title：修改Model中cs、type、批注、描述、在最后添加新列时使用，会直接在分表上修改\n" +
                " 2-重新生成：能够做到\"整理Title\"所做的、并且支持添加、删除字段，会重新生成分表，同时保证\n" +
                " 原来的格式不会被改变（缺点：会丢掉颜色信息，建议使用“整理Title”）\n" +
                " 3-增加Sheet：同步在分表上增加sheet\n" +
                " 修改批注尽量在model上修改，重新生成时，会同步批注信息（批注一定要加在desc行）\n"
                /**"2、执行合表 \n" +
                " 默认：按照读取分表文件的顺序合表\n" +
                " 升序：sn升序合表\n" +
                " 降序：sn降序合表 "**/);
        panel3.add(showMsg);
        layout.setConstraints(panel3, constraints);
        frame.add(panel3);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
        });
    }

    private void choiceFileOpr(String name) {
        if (!choiceFiles.remove(name)) {
            choiceFiles.add(name);
        }
    }

    private void performMergeOpr(String srcPath, String outPath) {
        if (choiceFiles.isEmpty()) {
            JOptionPane.showMessageDialog(null, "请选择要合并的文件!");
            return;
        }
        // 为了保证表都合并完成在弹提示 使用了闭锁
        CountDownLatch countDownLatch = new CountDownLatch(choiceFiles.size());
        choiceFiles.forEach(p -> new Thread(() -> {
            PoiMergeManager.runMergeTask(mergeType, srcPath + p, outPath);
            countDownLatch.countDown();
        }).start());
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "合并成功!");
    }

    private void performTitleOpr(String srcPath) {
        if (choiceFiles.isEmpty()) {
            JOptionPane.showMessageDialog(null, "请选择要整理的文件!");
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(choiceFiles.size());
        // 直接创建线程异步执行，其实应该主线程跑一个线程，其他的创建线程异步执行fork/join框架的思路就是这样
        choiceFiles.forEach(p -> new Thread(() -> {
            String path = srcPath + p + "/";
            if (titleType == ETitleType.ARRANGE) {
                new ArrangeTitle().arrangeTitle(path);
            } else if (titleType == ETitleType.RENEWAL) {
                RenewalTitle.renewalFile(path);
            } else {
                new AddSheet().addSheetOpr(path);
            }
            countDownLatch.countDown();
        }).start());
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, "整理完成!");
    }
}
