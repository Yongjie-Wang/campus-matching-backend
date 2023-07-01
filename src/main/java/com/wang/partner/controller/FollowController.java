package com.wang.partner.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.common.BaseResponse;
import com.wang.partner.common.ResultUtils;
import com.wang.partner.model.domain.Follow;
import com.wang.partner.model.domain.User;
import com.wang.partner.service.FollowService;
import com.wang.partner.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.wang.partner.contant.UserConstant.USER_LOGIN_STATE;

@RequestMapping("/follow")
@RestController
@Slf4j
public class FollowController {
    @Resource
    private  UserService userService;
    @Resource
    private FollowService followService;

    @GetMapping("/commonFollow")
    public BaseResponse<IPage<User>> commonFollow(HttpServletRequest request, Page page, Long otherUserId) {

        User attribute =(User) request.getSession().getAttribute(USER_LOGIN_STATE);
        Long meId = attribute.getId();
       IPage<User>iPage= followService.selectCommonFollow(page, meId,otherUserId);
        return ResultUtils.success(iPage);
    }
    @GetMapping("/objects")
    public BaseResponse<IPage<User>> followedObjects(HttpServletRequest request, Page page) {

        User attribute =(User) request.getSession().getAttribute(USER_LOGIN_STATE);
        Long meId = attribute.getId();
        List<Integer> collect = followService.list(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getUserId, meId)).stream().map(Follow::getFollowId).collect(Collectors.toList());
        Page page1 = userService.page(page, Wrappers.<User>lambdaQuery().in(User::getId, collect));
        return ResultUtils.success(page1);
    }
}
