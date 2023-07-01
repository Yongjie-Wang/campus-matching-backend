package com.wang.partner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.partner.model.domain.Team;
import com.wang.partner.model.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * 队伍 Mapper 接口
 * </p>
 *
 * @author wang
 * @since 2023-05-06
 */
@Mapper
public interface TeamMapper extends BaseMapper<Team> {
    @Select("select u.username,u.avatarUrl from user_team ut  join user u on ut.isDelete = u.isDelete and ut.userId = u.id\n" +
            "where ut.teamId =#{id}")
   List<User> selectUsernameByTeamId(int id);

}
