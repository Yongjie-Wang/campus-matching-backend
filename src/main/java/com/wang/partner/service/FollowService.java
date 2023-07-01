package com.wang.partner.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.model.domain.Follow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.partner.model.domain.User;

import java.util.List;

/**
* @author 24017
* @description 针对表【follow(用户关注关系表)】的数据库操作Service
* @createDate 2023-06-22 21:14:46
*/
public interface FollowService extends IService<Follow> {

    IPage<User> selectCommonFollow(Page page, Long meId, Long otherUserId);
}
