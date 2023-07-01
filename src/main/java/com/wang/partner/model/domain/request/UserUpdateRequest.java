package com.wang.partner.model.domain.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

@Data
public class UserUpdateRequest {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;


    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;


    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 标签列表
     */
    private String tags;



    @TableField(exist = false)
    private static final long serialVersionUID = -2597942582389955790L;

}
