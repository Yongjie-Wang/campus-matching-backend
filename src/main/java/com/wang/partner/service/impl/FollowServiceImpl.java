package com.wang.partner.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.partner.model.domain.Follow;
import com.wang.partner.model.domain.User;
import com.wang.partner.service.FollowService;
import com.wang.partner.mapper.FollowMapper;
import com.wang.partner.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 24017
 * @description 针对表【follow(用户关注关系表)】的数据库操作Service实现
 * @createDate 2023-06-22 21:14:46
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
        implements FollowService {
    @Resource
    private UserService userService;

    @Override
    public  IPage<User> selectCommonFollow(Page page ,Long meId, Long otherUserId) {
      IPage<User> userList = baseMapper.selectCommonFollow( page,meId, otherUserId);
        List<Long> collect = userList.getRecords().stream().map(User::getFollowId).collect(Collectors.toList());
        List<User> userList1 = userService.listByIds(collect);
        return (IPage) page.setRecords(userList1);
    }
}




