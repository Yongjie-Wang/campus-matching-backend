package com.wang.partner.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.wang.partner.mapper.UserMapper;
import com.wang.partner.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 循环插入用户
     */
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 500;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("咕噜咕力123"+i);
            user.setUserAccount("xiaoben"+i);
            user.setAvatarUrl("http://124.221.242.250:8008/wp-content/uploads/2023/06/6437c9a60d2dde57775708a2.gif");
            user.setProfile("书里总爱写喜出望外的傍晚");
            user.setGender(i%2);
            user.setUserPassword("b0dd3697a192885d7c055db46155b26a");
            user.setPhone("1387892387923"+i);
            user.setEmail("30333464"+i+"@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("112234"+i);
            user.setTags(i%2==0?"[\"C++\",\"大一\",\"男\"]":"[\"大数据\",\"大四\",\"男\"]");
            userList.add(user);
        }
        userService.saveBatch(userList, 50000);
        stopWatch.stop();
        System.out.println("total time:" + stopWatch.getLastTaskTimeMillis());

    }

    @Test
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000000;
        // 分十组
        int j = 0;
        //批量插入数据的大小
        int batchSize = 500;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。（鱼皮这里直接取了个值，会有问题,我这里随便写的）
        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("张老板");
                user.setUserAccount("wangxiao"+i);
                user.setAvatarUrl("https://pic2.imgdb.cn/item/6437c9a60d2dde57775708a2.gif");
                user.setProfile("书里总爱写喜出望外的傍晚");
                user.setGender(i%2);
                user.setUserPassword("b0dd3697a192885d7c055db46155b26a");
                user.setPhone("13437f88"+i);
                user.setEmail("303w378464@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("11223234"+i);
                user.setTags(i%2==0?"[\"可视化\",\"大一\",\"男\"]":"[\"前端\",\"大一\",\"男\"]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());

    }

    @Test
    public void doDeleteUser() {
        QueryWrapper<User> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("username", "假沙鱼");
        int delete = userMapper.delete(objectQueryWrapper);

    }

    @Test
    public void doCount() {
        Long aLong = userMapper.selectCount(null);
        log.info("total counts:" + aLong);
    }
    @Test
    @Scheduled(initialDelay = 1000,fixedRate = 1000)
    public void testSchedule() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i <2000 ; i++) {
            System.out.println("wangyongjie"+i);
        }
        stopWatch.stop();
        long lastTaskTimeMillis = stopWatch.getLastTaskTimeMillis();
        System.out.println("******执行时间："+lastTaskTimeMillis);
    }
    @Test
    public  void testStopWatch() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 需要计时的代码
        try {
            Thread.sleep(2000); // 模拟任务执行
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            stopWatch.stop();
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            System.out.println("任务执行时间：" + totalTimeMillis + "ms");
        }
    }

}
