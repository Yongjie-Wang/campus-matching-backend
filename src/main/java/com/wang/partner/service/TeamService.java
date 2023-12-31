package com.wang.partner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.partner.model.domain.Team;
import com.wang.partner.model.domain.User;
import com.wang.partner.model.domain.request.TeamQueryRequest;
import com.wang.partner.model.domain.request.TeamJoinRequest;
import com.wang.partner.model.domain.request.TeamQuitRequest;
import com.wang.partner.model.domain.request.TeamUpdateRequest;
import com.wang.partner.model.domain.vo.TeamUserVO;

import java.util.List;

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

    List<TeamUserVO> listTeams(TeamQueryRequest teamQueryRequest, boolean isAdmin);

    Boolean updateTeam(TeamUpdateRequest team, User loginUser);

    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    Boolean quitTeam(TeamQuitRequest teamJoinRequest, User loginUser);

    boolean deleteTeam(long id, User loginUser);
}
