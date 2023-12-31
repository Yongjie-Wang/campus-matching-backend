# 伙伴匹配系统
## 项目介绍

该校园匹配平台是一个基于 Vue 3 + Spring Boot 2 的移动端网站，旨在为校园内的用户提供一个交流和组队的平台。用户可以通过该平台进行用户管理、按标签检索用户、推荐相似用户、好友单聊、队伍群聊、发送邮件以及统计在线用户等操作。

## 工作内容的重点亮点

1. 分布式 Session 管理：使用 Redis 实现分布式 Session，解决集群间登录态同步问题，提升系统的可扩展性和稳定性。
2. 内存优化和缓存：使用 Hash 存储用户信息，节约内存，并通过 Redis 缓存高频访问的用户信息列表，加速接口响应时长。
3. 并发编程和性能优化：使用 CompletableFuture 和自定义线程池提高批量导入数据库的性能，采用分布式锁保证多机部署时定时任务不会重复执行。
4. 数据处理和算法应用：使用 Java 8 Stream API 和 Lambda 表达式简化集合处理，使用编辑距离算法实现标签匹配，优化算法运算过程中的内存占用。
5. 容器化部署和自动化构建：自主编写 Dockerfile，并通过第三方容器托管平台实现自动化镜像构建及容器部署，提高部署上线效率。
6. 接口文档生成和注释补充：使用 Knife4j + Swagger 自动生成后端接口文档，并通过编写注解补充接口注释，提高文档的准确性和可维护性。
7. 实时通信和状态管理：使用 WebSocket 实现好友单聊和多人群聊功能，利用 Redis 记录用户在线状态和聊天记录，保证消息的准确性和实时性。同时，基于推模式实现关注 Feed 流，保证了新点评消息的及时可达，并减少用户访问的等待时间。使用 Redis Set 数据结构实现用户关注和共同关注功能，提升性能并避免数据丢失。

通过以上亮点，该校园匹配平台实现了高性能、高可用性和用户友好的功能，为校园用户提供了便捷的交流和组队平台。

## 效果图
![修改用户信息页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-16-54.png)
![单聊页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-17-34.png)
![群聊页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-32-07.png)
![搜索页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-32-28.png)
![个人空间页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-35-00.png)
![在线用户页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-16-03.png)
![公开队伍界面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-16-12.png)
![私密队伍界面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-16-22.png)
![用户信息页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-16-30.png)
![关注对象页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-16-39.png)
![登入页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/login.png)
![注册页面](https://gitlab.com/Yongjie-Wang/partner-matching-backend/-/raw/main/imgs/Snipaste_2023-07-02_11-14-38.png)
