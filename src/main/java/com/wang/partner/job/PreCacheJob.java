package com.wang.partner.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
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
    private List<Long> mainUserList = Arrays.asList(1L);

    // 每天执行，预热推荐用户
    @Scheduled(cron = "12 29 19 * * * ")   //自己设置时间测试
    public void doCacheRecommendUser() {
//        建锁
        RLock lock = redissonClient.getLock("shayu:precachejob:docache:lock");

        try {
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getLock:"+Thread.currentThread().getId());
                //查数据库
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                Page<User> userPage = userService.page(new Page<>(1,20),queryWrapper);
                String redisKey = String.format("shayu:user:recommend:%s",mainUserList);
                ValueOperations valueOperations = redisTemplate.opsForValue();
                //写缓存,30s过期
                try {
                    valueOperations.set(redisKey,userPage,3000000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    log.error("redis set key error",e);
                }
            }

        } catch (Exception e){
            log.error("doCacheRecommendUser error",e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock: "+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}