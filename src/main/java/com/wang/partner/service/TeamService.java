package com.wang.partner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.partner.model.domain.Team;
import com.wang.partner.model.domain.User;

/**
 * <p>
 * 队伍 服务类
 * </p>
 *
 * @author wang
 * @since 2023-05-06
 */
public interface TeamService extends IService<Team> {
    long addTeam(Team team, User loginUser);
}
