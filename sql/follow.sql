create table follow
(
    id         int auto_increment
        primary key,
    userId     int                                null comment '用户id',
    followId   int                                null comment '关注者id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '逻辑删除'
)
    comment '用户关注关系表';