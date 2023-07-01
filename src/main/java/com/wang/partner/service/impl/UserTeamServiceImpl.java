package com.wang.partner.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.partner.mapper.UserTeamMapper;
import com.wang.partner.model.domain.UserTeam;
import com.wang.partner.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户队伍关系 服务实现类
 * </p>
 *
 * @author wang
 * @since 2023-05-06
 */
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam> implements UserTeamService {

}
