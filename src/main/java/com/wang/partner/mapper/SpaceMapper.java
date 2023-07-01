package com.wang.partner.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.model.domain.Space;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.partner.model.domain.vo.SpaceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author 24017
* @description 针对表【space】的数据库操作Mapper
* @createDate 2023-06-29 20:46:51
* @Entity com.wang.partner.model.domain.Space
*/
public interface SpaceMapper extends BaseMapper<Space> {
    IPage<SpaceVO> selectAllFollowPosts(Page page,@Param("ids") List<Integer> ids);

    IPage<SpaceVO> selectAllPostsById(Page page,@Param("set") Set set);
}




