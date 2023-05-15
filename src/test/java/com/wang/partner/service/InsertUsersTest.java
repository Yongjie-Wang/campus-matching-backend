package com.wang.partner.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wang.partner.mapper.UserMapper;
import com.wang.partner.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 循环插入用户
     */
    @Test
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 20;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("xiaowang");
            user.setUserAccount("xiaowang");
            user.setAvatarUrl("https://pic2.imgdb.cn/item/6437c9a60d2dde57775708a2.gif");
            user.setProfile("一条咸鱼");
            user.setGender(0);
            user.setUserPassword("b0dd3697a192885d7c055db46155b26a");
            user.setPhone("123456789108");
            user.setEmail("303378464@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("123");
            user.setTags("[\"Java\",\"C++\",\"Python\"]");
            userList.add(user);
        }
        userService.saveBatch(userList, 5);

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
        int batchSize = 50000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数。（鱼皮这里直接取了个值，会有问题,我这里随便写的）
        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("假沙鱼");
                user.setUserAccount("yusha");
                user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
                user.setProfile("一条咸鱼");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("shayu-yusha@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("931");
                user.setTags("[]");
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
}
