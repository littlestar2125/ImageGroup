package com.ig.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties;

    static {
        properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = Config.class.getClassLoader().getResourceAsStream("application.properties");
        // 使用properties对象加载输入流
        try {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Config() {
    }

    public static String getProperties(String key) {
        return (String) properties.get(key);
    }
}
