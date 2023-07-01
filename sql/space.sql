create table space
(
    id         bigint                              not null comment 'id',
    userId     int                                 null comment '用户id',
    post       text                                null comment '文章',
    createTime datetime  default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime timestamp default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   bigint    default 0                 not null comment '逻辑删除',
    images     varchar(512)                        null comment '推文图片',
    bage       bigint    default 0                 null comment '点赞数量',
    primary key (id)
);