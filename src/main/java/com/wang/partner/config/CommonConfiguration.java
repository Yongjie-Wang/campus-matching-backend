package com.wang.partner.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

@Configuration
public class CommonConfiguration {
    /**
     * 文件上传配置，在application配置文件中设置不起作用！
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 单个文件最大
//        factory.setMaxFileSize("10240KB"); // KB,MB
        factory.setMaxFileSize(DataSize.ofBytes(10240000*5)); // KB,MB

        // 设置总上传数据总大小
        factory.setMaxFileSize(DataSize.ofBytes(1024000*5)); // KB,MB
        return factory.createMultipartConfig();
    }
}