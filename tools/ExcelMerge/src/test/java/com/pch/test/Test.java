package com.pch.test;

/**
 * @author pch
 */
public class Test {
    public static void main(String[] args) throws Exception {
        DynamicCompile dynamicCompile = new DynamicCompile();
        String code = "package com.hsc.study.com.hsc.test;\n" +
                "\n" +
                "public class Test {\n" +
                "    @Override\n" +
                "    public String toString() {\n" +
                "        return \"test\"\n" + ";" +
                "    }\n" +
                "}\n";
        Class<?> classz = dynamicCompile.compileToClass("com.hsc.study.Test", code);
        System.out.println(classz.newInstance());
    }
}
