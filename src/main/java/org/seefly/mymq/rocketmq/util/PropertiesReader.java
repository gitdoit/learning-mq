package org.seefly.mymq.rocketmq.util;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Properties;

/**
 * @author liujianxin
 * @date 2019-04-24 10:52
 */
public class PropertiesReader {
    private final Properties properties;

    public static PropertiesReader build(String filename){
        return new PropertiesReader(filename);
    }

    private PropertiesReader(String filename) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:"+filename);
        properties = new Properties();
        try {
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  String read(String key){
        return properties.getProperty(key);
    }
}
