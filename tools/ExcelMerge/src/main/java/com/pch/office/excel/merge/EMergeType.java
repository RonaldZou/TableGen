package com.pch.office.excel.merge;

import java.util.Objects;

/**
 * @author pch
 * 合表排序类型，升、降、无
 */
public enum EMergeType {
    NO("默认"),
    ASC("升序"),
    DES("降序");

    private String des;

    EMergeType(String des) {
        this.des = des;
    }

    String getDes() {
        return des;
    }

    static EMergeType getEMergeType(String des) {
        for (EMergeType i : EMergeType.values()) {
            if (Objects.equals(i.getDes(), des)) {
                return i;
            }
        }
        throw new IllegalArgumentException("error");
    }

    static String[] getArray() {
        String[] strings = new String[EMergeType.values().length];
        int k = 0;
        for (EMergeType i : EMergeType.values()) {
            strings[k++] = i.getDes();
        }
        return strings;
    }
}
