package com.wang.partner.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.model.domain.User;
import com.wang.partner.service.UserService;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: shayu
 * @date: 2022/12/11
 * @ClassName: yupao-backend01
 * @Description:        数据预热
 */

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    // 重点用户
    private List<Long> mainUserList = Arrays.asList(1L,2L,4L);
    // 每天执行，预热推荐用户
    @Scheduled(cron="0 0 1 * * ?")
    public void doCacheRecommendUser(){
        //锁名
        String redissonLock="wang:preCacheJob:doCache:lock";
            //通过redisson建锁
        RLock lock = redissonClient.getLock(redissonLock);
        try {
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                //表示枪锁线程
                System.out.println("LOCK:"+Thread.currentThread().getId());
                //模拟用户登入
                //查询数据库
                Page<User> page = userService.page(new Page<>(1, 20), null);
                mainUserList.forEach(x->{
                    String redisKey="wang:preCache:doCache:"+x;
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(redisKey,page,18*60*60,TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error");
                    }
                });
            }
        } catch (Exception e) {
            log.error("doCacheByDay:error");
        }finally {
            if(lock.isHeldByCurrentThread()){
                System.out.println("unLock:"+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}