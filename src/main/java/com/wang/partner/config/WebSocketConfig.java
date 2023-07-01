package com.wang.partner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
 

/**
 * @author 22603
 */
@Configuration
public class WebSocketConfig {
    //注入ServerEndPointExporter bean对象来自动扫描@ServerEndPoint注解
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}