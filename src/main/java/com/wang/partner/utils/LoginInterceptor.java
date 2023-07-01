package com.wang.partner.utils;

import com.wang.partner.model.domain.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoginInterceptor implements HandlerInterceptor {

    private RedisTemplate redisTemplate;

    public LoginInterceptor(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("userLoginState");


        if (user != null) {
            //续期
            Long id = user.getId();
            String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
            String key = format + "onlineUser";
            long minTime = System.currentTimeMillis() / 1000;
            Instant tenMinutesLater = Instant.ofEpochSecond(minTime).plus(Duration.ofMinutes(10));
            // 将新时间转换成时间戳格式
            long maxTime = tenMinutesLater.toEpochMilli() / 1000;
            try {
                redisTemplate.opsForZSet().add(key, id + "", maxTime);

            } catch (Exception e) {
                System.out.println("出异常" + e.toString());
                return true;
            }
        }
        return true;
    }
}