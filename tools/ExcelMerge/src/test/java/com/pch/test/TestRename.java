package com.pch.test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author pch
 */
public class TestRename {
    public static void main(String[] a) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, "C:\\Users\\admin\\Desktop\\my\\ConfAchievementValidator.java");
        System.out.println(result == 0 ? "编译成功" : "编译失败");
        Class c = findClass( "com.kwai.clover.gencheck.configcheck.validator.ConfAchievementValidator");
        System.err.println(Arrays.stream(c.getDeclaredMethods()).map(p->p.getName()).collect(Collectors.joining("--")));
    }

    public static Class<?> findClass(String packageName) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        //获取路径classes的路径
        //这里得到的就是com.company.base.controller包的全名
        try {
            URL classes = new URL("file:///" + "C:\\Users\\admin\\Desktop\\my\\classes\\");
            ClassLoader custom = new URLClassLoader(new URL[]{classes}, systemClassLoader);
            Class<?> clazz = custom.loadClass(packageName);
            return clazz;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
