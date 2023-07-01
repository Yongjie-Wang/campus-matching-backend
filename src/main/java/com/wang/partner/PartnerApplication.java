package com.wang.partner;

import com.wang.partner.controller.TeamChatEndpoint;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@MapperScan("com.wang.partner.mapper")
@EnableScheduling
public class PartnerApplication {
    public static void main(String[] args) {
        //获取应用程序上下文
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(PartnerApplication.class, args);
        //通过set方法在TeamChatEndpoint注入configurableApplicationContext（单例)
        TeamChatEndpoint.setApplicationContext(configurableApplicationContext);
    }

}
