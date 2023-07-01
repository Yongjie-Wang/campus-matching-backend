package com.wang.partner.contant;

public class RedisConstant {
    //过期时间
    public static final int ADD_EXPIRE_TIME= 10*60;
    //当前队伍聊天记录
    public static final String TEAM_CHAT_RECORDS="team:chat:records:";
    //当前博文那些点赞用户集合
    public static final String  SPACE_POST_LIKED ="space:liked:";
    //用户点赞的发文id集合
    public static final String  SPACE_LIKED_POSTS="space:like_posts:";
    //发文推送用户
    public static final String  SPACE_PUSH_USER="space:push:user:";
}
