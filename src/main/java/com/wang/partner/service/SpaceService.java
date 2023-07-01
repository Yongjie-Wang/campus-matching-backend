package com.wang.partner.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.model.domain.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.partner.model.domain.User;
import com.wang.partner.model.domain.vo.SpaceVO;

/**
* @author 24017
* @description 针对表【space】的数据库操作Service
* @createDate 2023-06-29 20:46:51
*/
public interface SpaceService extends IService<Space> {

    IPage<SpaceVO> selectPosts(User loginUser, Page page);

    Boolean liked(Long id, Boolean liked, Long id1);

    void pushFens(int id, Long id1);
}
