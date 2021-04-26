package com.pch.office.excel.easyexcel;

import org.apache.poi.openxml4j.util.ZipSecureFile;

/**
 * 抽象类 具备某种能力
 * @author pch
 */
public abstract class EasyAbstract {

    static {
        ZipSecureFile.setMinInflateRatio(-1.0d);
    }
}
