package com.wang.partner.config;

import com.wang.partner.utils.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private RedisTemplate redisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(redisTemplate));

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //那些接口需要配置跨域
        registry.addMapping("/**")
                //允许那些url跨域
//                .allowedOrigins("http://124.221.242.250:9001")
                .allowedOrigins("http://localhost:3000")
                //是否允许携带cookie
                .allowCredentials(true)
                //允许那些请求方式
                .allowedMethods("GET","POST","HEAD","PUT","DELETE","OPTIONS")
                //cookie失效时间，-1指浏览器关闭失效,单位（s）
                .maxAge(3600);

    }
}
