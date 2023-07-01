package com.wang.partner.contant;

/**
 * 用户常量
 *
 * @author yupi
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";
    String USER_RECOMMEND_TAGS = "wang:user:recommend:";

    //  ------- 权限 --------

    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;

    String SALT = "WANG";
    String EMAIL_SAVE_CODE_KEY="email:code:";

}
