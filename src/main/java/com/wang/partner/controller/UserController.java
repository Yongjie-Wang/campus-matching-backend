package com.wang.partner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.wang.partner.common.BaseResponse;
import com.wang.partner.common.ErrorCode;
import com.wang.partner.common.ResultUtils;
import com.wang.partner.exception.BusinessException;
import com.wang.partner.model.domain.User;
import com.wang.partner.model.domain.request.UserLoginRequest;
import com.wang.partner.model.domain.request.UserRegisterRequest;
import com.wang.partner.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.wang.partner.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author yupi
 */
//@CrossOrigin(origins = {"http://localhost:3000"})
@RestController
@RequestMapping("/user")
@Api(tags = "用户控制层")
@Slf4j
public class UserController {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation("用户注册接口")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    @ApiOperation("用户登入接口")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    @ApiOperation("用户注销接口")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    @ApiOperation("获取用户信息接口")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();

        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    @ApiOperation("用户查询接口")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/delete")
    @ApiOperation("用户删除接口")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 通过标签搜素用户
     */
    @GetMapping("/search/tags")
    @ApiOperation("标签查询用户接口")
    public BaseResponse<List<User>> deleteUser(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        userList.forEach(new Consumer<User>() {
            @Override
            public void accept(User user) {
                System.out.println("user" + user);
            }
        });
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    @ApiOperation("更新用户接口")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
//        验证参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 主页推荐
     */
    @GetMapping("/recommend")
    @ApiOperation("用户推荐接口")
    public BaseResponse<Page<User>> recommendUsers(long pageNum, long pageSize, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("shayu:user:recommend:%s", loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //        如果有缓存，直接读取
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
//        无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
//        存缓存
        try {
            valueOperations.set(redisKey, userPage, 3600, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set key error ", e);
        }
        return ResultUtils.success(userPage);
    }


}
