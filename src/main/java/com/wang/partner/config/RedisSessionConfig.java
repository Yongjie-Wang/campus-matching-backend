package com.wang.partner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

//@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 86400)
//@Configuration
public class RedisSessionConfig {
//        @Bean
//        public LettuceConnectionFactory connectionFactory() {
//            return new LettuceConnectionFactory();
//        }

}
