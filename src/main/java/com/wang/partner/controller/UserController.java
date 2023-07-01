package com.wang.partner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.common.BaseResponse;
import com.wang.partner.common.ErrorCode;
import com.wang.partner.common.ResultUtils;
import com.wang.partner.exception.BusinessException;
import com.wang.partner.model.domain.EmailCode;
import com.wang.partner.model.domain.Follow;
import com.wang.partner.model.domain.User;
import com.wang.partner.model.domain.request.UserLoginRequest;
import com.wang.partner.model.domain.request.UserRegisterRequest;
import com.wang.partner.model.domain.request.UserUpdateRequest;
import com.wang.partner.model.domain.vo.TagVo;
import com.wang.partner.service.FollowService;
import com.wang.partner.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
    private FollowService followService;
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
        String email = userRegisterRequest.getEmail();
        String code = userRegisterRequest.getCode();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, code)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, email, code);
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

    @PostMapping("/sendMessage")
    @ApiOperation("用户邮箱验证接口")
    public BaseResponse<Integer> userValidateCode(HttpServletRequest request, @RequestBody EmailCode emailCode) {
        if (emailCode == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Boolean flag = userService.sendCode(emailCode);
        return ResultUtils.success(0);
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
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
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
    public BaseResponse<Integer> updateUser(@RequestBody UserUpdateRequest user, HttpServletRequest request) {
//        验证参数是否为空
        if (null == user) {
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
        //通过request获取UserId
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        Page<User>  page=userService.recommendUsers(id,pageNum,pageSize);
        return ResultUtils.success(page);
    }

    @GetMapping("/match")
    @ApiOperation("用户匹配接口")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

    @GetMapping("/get/tags")
    public BaseResponse<TagVo> getTags(HttpServletRequest request) {

        TagVo tagVo = userService.getTags(request);
        log.info(tagVo.toString());
        return ResultUtils.success(tagVo);
    }


    @GetMapping("/selectUser/{id}")
    public BaseResponse<User> selectUser(HttpServletRequest request,@PathVariable("id") String id) {
        User loginUser = userService.getLoginUser(request);
        int i = Integer.parseInt(id);
        if(StringUtils.isBlank(id) || i<1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User otherUser = userService.getById(i);
        return ResultUtils.success(otherUser);
    }
    @GetMapping("/isFollowed/{id}")
    public BaseResponse<Boolean> isFollowed(HttpServletRequest request,@PathVariable("id") String id) {
        User loginUser = userService.getLoginUser(request);
        long count = selectUserFollow(id, loginUser);
        return ResultUtils.success(count > 0);
    }



    @GetMapping("/followeUser/{id}")
    public BaseResponse<Boolean> FollowedUser(HttpServletRequest request,@PathVariable("id") String id) {
        User loginUser = userService.getLoginUser(request);
        Long meId = loginUser.getId();
        long count = selectUserFollow(id, loginUser);
        if(count>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"重复关注");
        }
        Follow follow =new Follow();
        follow.setUserId(Math.toIntExact(meId));
        follow.setFollowId(Integer.parseInt(id));
        try {
            followService.save(follow);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"添加关注关系失败");
        }
        return ResultUtils.success(count > 0);
    }
    @GetMapping("/unFollowUser/{id}")
    public BaseResponse<Boolean> unFollowedUser(HttpServletRequest request,@PathVariable("id") String id) {
        User loginUser = userService.getLoginUser(request);
        Long meId = loginUser.getId();
        long count = selectUserFollow(id, loginUser);
        if(count<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"尚未关注");
        }
        Follow follow =new Follow();
        follow.setUserId(Math.toIntExact(meId));
        follow.setFollowId(Integer.parseInt(id));
        QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>(follow);
        try {
            followService.remove(followQueryWrapper);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"取消关注关系失败");
        }
        return ResultUtils.success(count > 0);
    }


    private long selectUserFollow(String id, User loginUser) {
        Long userId = loginUser.getId();
        int i = Integer.parseInt(id);
        QueryWrapper<Follow> eq = new QueryWrapper<Follow>().eq("userId", userId).eq("followId", i);

        long count = followService.count(eq);
        return count;
    }

}
