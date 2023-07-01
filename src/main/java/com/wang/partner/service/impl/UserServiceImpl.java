package com.wang.partner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.partner.common.BaseResponse;
import com.wang.partner.common.ErrorCode;
import com.wang.partner.common.ResultUtils;
import com.wang.partner.contant.SystemConstant;
import com.wang.partner.exception.BusinessException;
import com.wang.partner.mapper.UserMapper;
import com.wang.partner.model.domain.EmailCode;
import com.wang.partner.model.domain.User;
import com.wang.partner.model.domain.request.UserUpdateRequest;
import com.wang.partner.model.domain.vo.TagVo;
import com.wang.partner.service.UserService;
import com.wang.partner.utils.AlgorithmUtils;
import com.wang.partner.utils.RandomUsernameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.wang.partner.contant.UserConstant.*;

/**
 * 用户服务实现类
 *
 * @author yupi
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String from;


    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String email, String code) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, email, code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (code.length() != 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码格式不对");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 验证code
        String emailKey = EMAIL_SAVE_CODE_KEY + email;
        queryWrapper = new QueryWrapper<>();
        EmailCode emailCode = (EmailCode) redisTemplate.opsForValue().get(emailKey);
        if (!code.equals(emailCode.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        //星球编号自增
        long countUser = this.count() + 1;
        String planetCode = countUser + "";
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        user.setEmail(email);
        user.setAvatarUrl("https://pic2.imgdb.cn/item/64570d2e0d2dde5777d012cb.png");
        String generateUsername = RandomUsernameGenerator.generateUsername();
        user.setUsername(generateUsername);
        user.setProfile("人和代码一个能跑就行！！！");
        user.setTags("[\"Java\",\"大一\",\"女\"]");
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户不存在");

        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return getUsersBySql(tagNameList);
//        通过sql查询
//        return getUsersBySql(tagNameList);
//        内存查询
    }


    //更新用户
    @Override
    public int updateUser(UserUpdateRequest user, User loginUser) {
        //获取登入用户id
        Long id = user.getId();
        String redisCommendKey = USER_RECOMMEND_TAGS + id;
        //判断用户id是否异常
        if (id < 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登入信息异常");
        }
        //判断时否登入及可修改的权限
        if (!isAdmin(loginUser) && !id.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "权限不足");
        }
        //判断数据库是否含有此用户
        User queryUser = userMapper.selectById(id);
        if (queryUser == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登入信息异常");
        }
//        BeanUtils.copyProperties(user,queryUser);
        BeanUtils.copyProperties(user, queryUser);
        //执行更新操作
        int i = userMapper.updateById(queryUser);
        if (i != -1) {
            redisTemplate.delete(redisCommendKey);
        }
        return i;
    }

    //获取登入用户
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"request == null");
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN,"为登入");
        }
        return (User) userObj;
    }

    //鉴权
    @Override
    public boolean isAdmin(HttpServletRequest request) {

        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    //鉴权重载
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 推荐匹配用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags")
                .select("id", "tags");
        List<User> userList = this.list(queryWrapper);
        Long id = loginUser.getId();
        queryWrapper.eq("id", id);
        User one = this.getOne(queryWrapper);
        String tags = one.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 存储用户信息的列表，每个Map对象存储一个用户的相关信息
        List<Pair<String, String>> list1 = null;
        List<Map<String, Object>> list = new ArrayList<>();
        // 依次计算当前用户和所有用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签的 或当前用户为自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("distance", distance);
            list.add(userMap);
        }
        //按编辑距离由小到大排序
        List<Map<String, Object>> topUserMapList = list.stream()
                .sorted(Comparator.comparingLong(userMap -> (Long) userMap.get("distance")))
                .limit(num)
                .collect(Collectors.toList());
        //有顺序的userId列表
        List<Long> userIdList = topUserMapList.stream()
                .map(userMap -> (Long) userMap.get("id"))
                .collect(Collectors.toList());

        //根据id查询user完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));

        // 因为上面查询打乱了顺序，这里根据上面有序的userID列表赋值
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        List<String> onlineUserIdS = getOnlineUserIdS();
        List<Long> collect = onlineUserIdS.stream().map(Long::valueOf).collect(Collectors.toList());
        finalUserList.forEach(x->{
            if(collect.contains(x.getId())){
                x.setIsOnline(1);
            }
        });
        return finalUserList;
    }

    @Override
    public Boolean sendCode(EmailCode emailCode) {
        // 判断emailCode对象是否为空
        //空，抛异常
        String email = emailCode.getEmail();
        if (!isEmailValid(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断邮箱是否存在
        User user = userMapper.searchUserByEmail(emailCode);
        if (user != null && user.getId() > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
        }
        String subject = "伙伴匹配系统";
        String code = "";
        if (StringUtils.isNotEmpty(email)) {
            //发送一个四位数的验证码,把验证码变成String类型
            String randomNumber = RandomStringUtils.randomNumeric(6);
            code = randomNumber.substring(0, 6);
            String text = "【伙伴匹配系统】您好，您的验证码为：" + code + "，请在5分钟内使用";
            log.info("验证码为：" + code);
            //发送短信
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(email);
            message.setSubject(subject);
            message.setText(text);
            //发送邮件
            javaMailSender.send(message);
            EmailCode userSendMessage = new EmailCode();
            userSendMessage.setEmail(email);
            userSendMessage.setCode(code);
            // 作为唯一标识
            String redisKey = EMAIL_SAVE_CODE_KEY + email;
            // 写缓存
            ValueOperations valueOperations = redisTemplate.opsForValue();
            try {
                valueOperations.set(redisKey, userSendMessage, 300, TimeUnit.SECONDS);
                EmailCode sendMessage = (EmailCode) valueOperations.get(redisKey);
                log.info(sendMessage.toString());
                System.out.println(sendMessage);
                return true;
            } catch (Exception e) {
                log.error("redis set key error", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缓存失败!");
            }
        }
        return false;
    }


    //内存查询
    private List<User> getUsersByMemory(List<String> tagNameList) {
        List<User> list = this.list(null);
        Gson gson = new Gson();
        return list.stream().filter(u -> {
            String tags = u.getTags();
            if (StringUtils.isEmpty(tags)) {
                return false;
            }
            Set<String> set = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            set = Optional.ofNullable(set).orElse(new HashSet<>());
            for (String tag : tagNameList
            ) {
                if (!set.contains(tag)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    //    sql查询方法
    @Deprecated
    private List<User> getUsersBySql(List<String> tagNameList) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        for (String tag : tagNameList
        ) {
            queryWrapper.like(User::getTags, tag);
        }
        List<User> list = this.list(queryWrapper);
        List<User> userLists = list.stream().map(this::getSafetyUser).collect(Collectors.toList());
        return userLists;
    }

    public static boolean isEmailValid(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(regex);
    }

    @Override
    public TagVo getTags(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        TagVo tagVo = new TagVo();
        String redisKey = USER_LOGIN_STATE;
        User user = (User) request.getSession().getAttribute(redisKey);
        User currentUser = this.getById(user.getId());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String tags = currentUser.getTags();
        Gson gson = new Gson();
        List<String> oldTags = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());


        tagVo.setOldTags(oldTags);
        tagVo.setRecommendTags(oldTags);
        return tagVo;
    }

    /**
     * 本地
     *
     * @param file
     * @return
     */
    @Override
    public BaseResponse uploadUserImg(MultipartFile file, User user) {
        if (file.isEmpty() || file == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long userId = user.getId();
        LocalDate now = LocalDate.now();
        String suffix = now.format(DateTimeFormatter.ofPattern("yyyy/MM/"));
        // 根据Windows和Linux配置不同的头像 保存路径
        String OSName = System.getProperty("os.name");
        // window://user/yes/
        //linux:/www/root/www/06/13/
        String profilesPath = OSName.toLowerCase().startsWith("win") ? SystemConstant.WINDOWS_PROFILES_PATH
                : SystemConstant.LINUX_PROFILES_PATH_PRO + suffix;

        if (!file.isEmpty()) {

            //保存在本地的地址(c://user/1233.jpg)
            String fileName = profilesPath + System.currentTimeMillis() + file.getOriginalFilename();
            //保存数据库和前端的地址(http:/8080/wwwContent/06/13/1.png)
            //String avatarAddress =fileName.replace(profilesPath,LINUX_PROFILES_PATH_Begin);

            System.out.println("文件地址" + fileName);

            // 磁盘保存
            BufferedOutputStream out = null;
            try {
                File folder = new File(profilesPath);
                if (!folder.exists())
                    folder.mkdirs();
                out = new BufferedOutputStream(new FileOutputStream(fileName));
                // 写入新文件
                out.write(file.getBytes());
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "上传失败");
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            user.setAvatarUrl(fileName);
            //保存数据库
            boolean b = this.updateById(user);
            if (!b) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存失败");
            }
            return ResultUtils.success(fileName);
        } else {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "上传失败");
        }

    }

    @Override
    public Page<User> recommendUsers(Long id, long pageNum, long pageSize) {

        //设置redisKey,并从redis取
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        String redisKey = USER_RECOMMEND_TAGS + id;
        //  Page<User> page = (Page<User>) valueOperations.get(redisKey);
        Page<User> page = null;
//        //对取出来的数据进行判空，成功则返回
//        if (!Objects.isNull(page)) {
//            return ResultUtils.success(page);
//        }
        //不成功则从数据库查询，并存进redis中，方便下次从redis中取
        //取出Zset中与现在时间错相差十分钟的id
        List<String> idList = getOnlineUserIdS();
        //上面将将id转为Long未生效
        List<Long> reaIdList = idList.stream().map(Long::valueOf).collect(Collectors.toList());
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        //与所有用户集合进行合并
        wrapper.notIn("id", id);
        page = page(new Page<>(pageNum, pageSize), wrapper);
        //内容
        List<User> allUsers = page.getRecords();
        allUsers.forEach(x -> {
            if (reaIdList.contains(x.getId())) {
                x.setIsOnline(1);
            }
        });
        List<User> sortedUsers = allUsers.stream()
                .sorted((o1, o2) -> o2.getIsOnline() - o1.getIsOnline())
                .collect(Collectors.toList());
        //重构
        //page = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        page.setRecords(sortedUsers);
        //存redis
   /*     try {
            valueOperations.set(redisKey, page, 60*10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("redisKey错误");
        }*/
        return page;
    }

    /**
     * 获取在线用户id（zset）
     *
     * @return
     */
    private List<String> getOnlineUserIdS() {
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:"));
        String key = format + "onlineUser";

        long minTime = System.currentTimeMillis() / 1000;
        Instant tenMinutesLater = Instant.ofEpochSecond(minTime).plus(Duration.ofMinutes(10));
        Instant minTimeUser = Instant.ofEpochSecond(minTime).minus(Duration.ofMinutes(10));
// 将新时间转换成时间戳格式
        minTime = minTimeUser.toEpochMilli() / 1000;
        long maxTime = tenMinutesLater.toEpochMilli() / 1000;
        //获取idlist和响应list<User>
        List<String> idList = (List<String>) redisTemplate.opsForZSet()
                .reverseRangeByScore(key, minTime, maxTime).stream()
                .collect(Collectors.toList());
        return idList;
    }

}




