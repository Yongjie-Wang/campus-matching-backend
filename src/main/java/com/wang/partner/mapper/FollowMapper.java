package com.wang.partner.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.model.domain.Follow;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.partner.model.domain.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 24017
* @description 针对表【follow(用户关注关系表)】的数据库操作Mapper
* @createDate 2023-06-22 21:14:46
* @Entity com.wang.partner.model.domain.Follow
*/
public interface FollowMapper extends BaseMapper<Follow> {

    IPage<User> selectCommonFollow(Page page, @Param("me") Long meId, @Param("other") Long otherUserId);
}




