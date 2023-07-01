package com.wang.partner.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.common.BaseResponse;
import com.wang.partner.common.ErrorCode;
import com.wang.partner.common.ResultUtils;
import com.wang.partner.exception.BusinessException;
import com.wang.partner.model.domain.Space;
import com.wang.partner.model.domain.User;
import com.wang.partner.model.domain.vo.LikedVO;
import com.wang.partner.model.domain.vo.SpaceVO;
import com.wang.partner.service.SpaceService;
import com.wang.partner.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wang.partner.contant.RedisConstant.SPACE_LIKED_POSTS;
import static com.wang.partner.contant.RedisConstant.SPACE_POST_LIKED;

@RestController
@RequestMapping("/space")
@Api(tags = "日记层")
@Slf4j
public class SpaceController {
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private RedisTemplate redisTemplate;


    @PostMapping("/add")
    @ApiOperation("添加推文")
    public BaseResponse<Long> addPost(HttpServletRequest request, @RequestBody Map<String, Object> req) {
        //通过sql
//        return addPostBySql(request, req);
        //通过redis
        return addPostByRedis(request, req);
    }


    @GetMapping("/select")
    @ApiOperation("查看推文")
    public BaseResponse<IPage<SpaceVO>> selectPosts(HttpServletRequest request, Page page) {
        User loginUser = userService.getLoginUser(request);
        IPage<SpaceVO> spaces = spaceService.selectPosts(loginUser, page);
        String redisKey = SPACE_LIKED_POSTS + loginUser.getId();
        String redisUsers = SPACE_POST_LIKED + loginUser.getId();
        Set<String> members = redisTemplate.opsForSet().members(redisKey);
        Set<Long> collect = members.stream().map(Long::valueOf).collect(Collectors.toSet());
        spaces.getRecords().stream().forEach(
                x -> {
                    if (collect.contains(x.getId())) {
                        x.setIsBadge(true);
                    }
                    Long size = redisTemplate.opsForSet().size(SPACE_POST_LIKED + x.getId());
                    x.setBage(size);
                }
        );
        return ResultUtils.success(spaces);
    }

    @PostMapping("/liked")
    @ApiOperation("文章点赞")
    public BaseResponse<Boolean> likePost(HttpServletRequest request,
                                          @RequestBody LikedVO likedVO) {
        //通过redis实现
        return likePostByRedis(request, likedVO);
        //通过sql实现（）
//        return likePostBySql(request, likedVO);

    }

    private BaseResponse<Boolean> likePostBySql(HttpServletRequest request, LikedVO likedVO) {
        User loginUser = userService.getLoginUser(request);
        Boolean isOk = spaceService.liked(loginUser.getId(), likedVO.getLiked(), Long.valueOf(likedVO.getId()));
        if (!isOk) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(isOk);
    }

    private BaseResponse<Boolean> likePostByRedis(HttpServletRequest request, LikedVO likedVO) {
        User loginUser = userService.getLoginUser(request);
        Boolean isOk = spaceService.liked(loginUser.getId(), likedVO.getLiked(), Long.valueOf(likedVO.getId()));
        if (!isOk) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResultUtils.success(isOk);
    }

    private BaseResponse<Long> addPostBySql(HttpServletRequest request, Map<String, Object> req) {
        String context = (String) req.get("context");
        User loginUser = userService.getLoginUser(request);
        int id = Math.toIntExact(loginUser.getId());
        Space space = new Space();
        space.setUserId(id);
        space.setPost(context);
        boolean save = spaceService.save(space);
        return ResultUtils.success(space.getId());
    }

    private BaseResponse<Long> addPostByRedis(HttpServletRequest request, Map<String, Object> req) {
        //新增推文逻辑
        //发布推文，找到所有关注我的人为将我的博客id进行推入，并且保存时间
        String context = (String) req.get("context");
        User loginUser = userService.getLoginUser(request);
        int id = Math.toIntExact(loginUser.getId());
        Space space = new Space();
        space.setUserId(id);
        space.setPost(context);
        boolean save = spaceService.save(space);
        if (save) {
            //找我该作者的粉丝id集合
            spaceService.pushFens(id, space.getId());
            //保存到zet中
        }
        return ResultUtils.success(space.getId());
    }


}
