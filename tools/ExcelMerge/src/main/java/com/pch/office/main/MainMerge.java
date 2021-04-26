package com.pch.office.main;

import com.pch.office.excel.merge.PoiMergeManager;

/**
 * @author pch
 * 启动类合表（jar包，必须放在指定目录下，这样就不用把path传递进去了）
 */
public class MainMerge {
    public static void main(String[] args) {
        String outPath;
        int mergeType;
        if (args.length > 0) {
            outPath = "../";
            mergeType = Integer.parseInt(args[0]);
        } else {
            outPath = "C:/sandbox/trunk/public/config/";
            mergeType = 2;
        }
        String srcPath = outPath + "Floder/";
        PoiMergeManager.mergeOpr(mergeType, srcPath, outPath);
    }
}
