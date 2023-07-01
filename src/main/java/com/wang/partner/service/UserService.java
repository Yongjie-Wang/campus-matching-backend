package com.wang.partner.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.partner.common.BaseResponse;
import com.wang.partner.model.domain.EmailCode;
import com.wang.partner.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wang.partner.model.domain.request.UserUpdateRequest;
import com.wang.partner.model.domain.vo.TagVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author yupi
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String email ,String code);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    List<User> searchUsersByTags(List<String> tagNameList);


    int updateUser(UserUpdateRequest user, User loginUser);

    User getLoginUser(HttpServletRequest request);
    /**
     * 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);
    boolean isAdmin(User loginUser);

    List<User> matchUsers(long num, User user);

    Boolean sendCode(EmailCode emailCode);
    TagVo getTags(  HttpServletRequest request);

    BaseResponse uploadUserImg(MultipartFile file,User user);

    Page<User> recommendUsers(Long id,long pageNum, long pageSize);
}
