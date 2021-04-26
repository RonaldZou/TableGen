package com.pch.office.excel.arrange;

import java.util.Objects;

/**
 * @author pch
 * 整理title类型
 */
public enum ETitleType {
    /**
     * 原文件上修改
     */
    ARRANGE("整理Title"),
    /**
     * 重新生成文件
     */
    RENEWAL("重新生成"),
    /**
     * Model增加Sheet
     */
    ADD_SHEET("增加Sheet")
    ;

    private String des;

    ETitleType(String des) {
        this.des = des;
    }

    public String getDes() {
        return des;
    }

    public static ETitleType getETitleType(String des) {
        for (ETitleType i : ETitleType.values()) {
            if (Objects.equals(i.getDes(), des)) {
                return i;
            }
        }
        throw new IllegalArgumentException("error");
    }

    public static String[] getArray() {
        String[] strings = new String[ETitleType.values().length];
        int k = 0;
        for (ETitleType i : ETitleType.values()) {
            strings[k++] = i.getDes();
        }
        return strings;
    }
}
