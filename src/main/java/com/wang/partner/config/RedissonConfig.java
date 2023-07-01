package com.wang.partner.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 */
@Configuration
@Data

public class RedissonConfig {

//    private String host = "124.221.242.250";
        private String host="localhost";

    private String port = "6379";

    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        //  使用单个Redis，没有开集群 useClusterServers()  设置地址和使用库
//        config.useSingleServer().setAddress(redisAddress).setDatabase(3).setPassword("abc123");
            config.useSingleServer().setAddress(redisAddress).setDatabase(1);
        // 2. 创建实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}

