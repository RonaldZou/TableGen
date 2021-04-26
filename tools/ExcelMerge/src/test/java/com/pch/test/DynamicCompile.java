package com.pch.test;

import javax.tools.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DynamicCompile {
    private URLClassLoader parentClassLoader;
    private String classpath;

    public DynamicCompile() {
        this.parentClassLoader = (URLClassLoader) this.getClass().getClassLoader();
        this.buildClassPath();// 存在动态安装的问题，需要动态编译类路径
    }

    private void buildClassPath() {
        this.classpath = null;
        StringBuilder sb = new StringBuilder();
        for (URL url : this.parentClassLoader.getURLs()) {
            String p = url.getFile();
            sb.append(p).append(File.pathSeparator); //路径分割符linux为:window系统为;
        }
        this.classpath = sb.toString();
    }

    /**
     * 编译出类
     *
     * @param fullClassName 全路径的类名
     * @param javaCode      java代码
     * @return 目标类
     */
    public Class<?> compileToClass(String fullClassName, String javaCode) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));

        List<JavaFileObject> jfiles = new ArrayList<>();
        jfiles.add(new CharSequenceJavaFileObject(fullClassName, javaCode));

        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(this.classpath);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);
        boolean success = task.call();

        if (success) {
            JavaClassObject jco = fileManager.getJavaClassObject();
            DynamicClassLoader dynamicClassLoader = new DynamicClassLoader(this.parentClassLoader);
            //加载至内存
            return dynamicClassLoader.loadClass(fullClassName, jco);
        } else {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                String error = compileError(diagnostic);
                throw new RuntimeException(error);
            }
            throw new RuntimeException("compile error");
        }
    }

    private String compileError(Diagnostic diagnostic) {
        StringBuilder res = new StringBuilder();
        res.append("LineNumber:[").append(diagnostic.getLineNumber()).append("]\n");
        res.append("ColumnNumber:[").append(diagnostic.getColumnNumber()).append("]\n");
        res.append("Message:[").append(diagnostic.getMessage(null)).append("]\n");
        return res.toString();
    }
}
