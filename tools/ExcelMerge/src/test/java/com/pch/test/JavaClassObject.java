package com.pch.test;
import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
public class JavaClassObject extends SimpleJavaFileObject {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public JavaClassObject(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    public byte[] getBytes() {
        return outputStream.toByteArray();
    }

    //编译时候会调用openOutputStream获取输出流,并写数据
    @Override
    public OutputStream openOutputStream() throws IOException {
        return outputStream;
    }
}