package com.wang.partner.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.partner.model.domain.Follow;
import com.wang.partner.model.domain.Space;
import com.wang.partner.model.domain.User;
import com.wang.partner.model.domain.vo.SpaceVO;
import com.wang.partner.service.FollowService;
import com.wang.partner.service.SpaceService;
import com.wang.partner.mapper.SpaceMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wang.partner.contant.RedisConstant.*;

/**
 * @author 24017
 * @description 针对表【space】的数据库操作Service实现
 * @createDate 2023-06-29 20:46:51
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {
    @Resource
    private FollowService followService;
    @Resource
    private RedisTemplate redisTemplate;


    @Override
    public IPage<SpaceVO> selectPosts(User loginUser, Page page) {
        //通过当前用户查询所关注的人的id
//        return getSpaceVOIPageBySql(loginUser, page);
        //通过当前用户查询所关注的人的id
        return getSpaceVOIPageByRedis(loginUser, page);
    }


    @Override
    public Boolean liked(Long id, Boolean liked, Long id1) {
        //通过sql实现（点赞数量没实现）
//        return bySql(liked, id1);
        //通过redis实现
        return byRedis(id, liked, id1);
    }

    @Override
    public void pushFens(int id, Long id1) {
        //通过id获取粉丝id集合
        List<Follow> list = followService.list(Wrappers.<Follow>lambdaQuery().eq(Follow::getFollowId, id));
        List<Integer> collect = list.stream().map(Follow::getUserId).collect(Collectors.toList());
        collect.add(id);
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        //为每个id建立zet
        collect.forEach(x -> {
            Boolean add = zSetOperations.add(SPACE_PUSH_USER + x, id1, System.currentTimeMillis());
        });
    }


    private boolean bySql(Boolean liked, Long id1) {
        boolean update = false;
        //判断是否点赞
        if (!liked) {
            //进行点赞
            update = this.update(Wrappers.<Space>lambdaUpdate().setSql("bage = bage + 1").eq(Space::getId, id1));

        } else {
            update = this.update(Wrappers.<Space>lambdaUpdate().setSql("bage = bage -1").eq(Space::getId, id1));
            //取消点赞
        }
        return update;
    }

    private boolean byRedis(Long id, Boolean liked, Long id1) {
        boolean update = false;
        String redisKey = SPACE_POST_LIKED + id1;
        String redisUserKey = SPACE_LIKED_POSTS + id;
        SetOperations setOperations = redisTemplate.opsForSet();
        update = !liked ? setOperations.add(redisKey, id) == 1 : setOperations.remove(redisKey, id) == 1;
        update = !liked ? setOperations.add(redisUserKey, id1.toString()) == 1 : setOperations.remove(redisUserKey, id1.toString()) == 1;
        return update;
    }

    private IPage<SpaceVO> getSpaceVOIPageBySql(User loginUser, Page page) {
        //关注了那些人
        List<Integer> ids = followService.list(Wrappers
                .<Follow>lambdaQuery().select(Follow::getFollowId)
                .eq(Follow::getUserId, loginUser.getId())).stream().map(Follow::getFollowId).collect(Collectors.toList());
        ids.add(Math.toIntExact(loginUser.getId()));
        IPage<SpaceVO> spaceVOIPage = baseMapper.selectAllFollowPosts(page, ids);
        return spaceVOIPage;
    }

    private IPage<SpaceVO> getSpaceVOIPageByRedis(User loginUser, Page page) {
        String redisKey = SPACE_PUSH_USER + loginUser.getId();
        Set set = redisTemplate.opsForZSet().reverseRangeByScore(redisKey, 0, System.currentTimeMillis(), 0, 10);
        if (set == null || set.isEmpty()) {
            return page.setRecords(null);
        }
        //
        IPage<SpaceVO> spaceVOIPage = baseMapper.selectAllPostsById(page, set);
        return spaceVOIPage;
    }
}




