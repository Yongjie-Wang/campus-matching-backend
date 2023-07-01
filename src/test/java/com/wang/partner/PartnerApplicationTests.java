package com.wang.partner;

import com.wang.partner.mapper.TeamMapper;
import com.wang.partner.utils.RandomUsernameGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.wang.partner.contant.RedisConstant.ADD_EXPIRE_TIME;


@SpringBootTest
class PartnerApplicationTests {
    @Autowired
    RedisTemplate redisTemplate;
    @Resource
    private TeamMapper teamMapper;


    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String s4 = "abc";
        String s5 = s4.replace("a", "m");
        System.out.println(s4);
        System.out.println(s5);
    }


    @Test
    void contextLoads() {
        List<Long> mainUserList = Arrays.asList(13L, 23L, 33L);
        mainUserList.forEach(x -> {
            String redisKey = String.format("wang:user:recommend:%s", x);
            ValueOperations valueOperations = redisTemplate.opsForValue();
            try {

                valueOperations.set(redisKey, "fdfjalskdf", 3000000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {

            }
        });

    }

    @Test
    void bubble() {
        String a = 1 + "";
        int[] arr = {1, 4, 5, 2, 3, 7, 5, 3, 2, 7, 9};
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] > arr[j + 1]) {
                    int t = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = t;
                }
            }

        }
        System.out.println(Arrays.toString(arr));
    }

    @Test
    void generateCode() {
        String randomNumber = RandomStringUtils.randomNumeric(6);
        System.out.println("验证码：" + randomNumber.substring(0, 6));
    }

    @Test
    void randomName() {
        String s = RandomUsernameGenerator.generateUsername();
        System.out.println(s);
    }

    /**
     * 测试存放userId和时间错
     */
    @Test
    void getSession() {
        //假设userId
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
        String key = format + "onlineUser";
        String userId = 1 + "";
        long maxTime = System.currentTimeMillis();
        long minTime = System.currentTimeMillis() - 60 * 10;
        redisTemplate.opsForZSet().add(key, userId, maxTime);
        redisTemplate.expire(key, 86400, TimeUnit.SECONDS);
        Set set = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, minTime, maxTime);
        redisTemplate.opsForZSet().add(key, 4 + "", maxTime + ADD_EXPIRE_TIME);
        set.forEach(System.out::println);
    }

    /**
     * 测试续期
     */
    @Test
    void testAddExpiire() {
        //假设userId
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
        String key = format + "onlineUser";
        String userId = 1 + "";
        long maxTime = System.currentTimeMillis() / 1000;
        Instant plus = Instant.ofEpochSecond(maxTime).plus(Duration.ofMinutes(10));
        int i = 1000;
        long newMaxTime = plus.toEpochMilli() / i;

        long minTime = System.currentTimeMillis() - 60 * 10;
        redisTemplate.opsForZSet().add(key, 1 + "", newMaxTime);
        redisTemplate.opsForZSet().add(key, 2 + "", newMaxTime);
        redisTemplate.opsForZSet().add(key, 3 + "", newMaxTime);
    }

    /**
     * 测试获取在线用户id
     */
    @Test
    void testAddExpiire1() {
        //假设userId
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
        String key = format + "onlineUser";
        String userId = 8 + "";

        long minTime = System.currentTimeMillis() / 1000;
        Instant tenMinutesLater = Instant.ofEpochSecond(minTime).plus(Duration.ofMinutes(10));
// 将新时间转换成时间戳格式
        long maxTime = tenMinutesLater.toEpochMilli() / 1000;
        Set set = redisTemplate.opsForZSet().reverseRangeByScore(key, minTime, maxTime);
        set.forEach(System.out::println);

    }

    /**
     * 测试在线id和所有id进行比较
     */
    @Test
    void testIntergerAndLong() {
        System.out.println("ehkafhsdkjf");


    }


}
