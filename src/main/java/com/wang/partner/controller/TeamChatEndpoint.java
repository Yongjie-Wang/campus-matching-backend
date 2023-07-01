package com.wang.partner.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.partner.common.ErrorCode;
import com.wang.partner.config.GetHttpSessionConfigurator;
import com.wang.partner.exception.BusinessException;
import com.wang.partner.mapper.TeamMapper;
import com.wang.partner.model.domain.Message;
import com.wang.partner.model.domain.Team;
import com.wang.partner.model.domain.User;
import com.wang.partner.service.TeamService;
import com.wang.partner.service.UserService;
import com.wang.partner.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.index.PathBasedRedisIndexDefinition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wang.partner.contant.RedisConstant.TEAM_CHAT_RECORDS;
import static com.wang.partner.contant.UserConstant.USER_LOGIN_STATE;


/**
 * @author 22603
 */
@ServerEndpoint(value = "/team/chat",configurator = GetHttpSessionConfigurator.class)
@Component
@Slf4j
public class TeamChatEndpoint {
    //解决teamMapper注入为null逻辑
    private static ApplicationContext applicationContext;

    private  TeamMapper teamMapper;

    private RedisTemplate redisTemplate;

    //解决无法注入mapper问题
    public static void setApplicationContext(ApplicationContext applicationContext) {
        TeamChatEndpoint.applicationContext = applicationContext;
    }

    /**
     * 用来存储每个用户客户端对象的ChatEndpoint对象
     */
    private static Map<String, TeamChatEndpoint> onlineUsers = new ConcurrentHashMap<>();
    private static Map<String,String> onlineStateMap = new ConcurrentHashMap<>();


    /**
     * 声明session对象，通过对象可以发送消息给指定的用户
     */
    private Session session;

    /**
     * 声明HttpSession对象，我们之前在HttpSession对象中存储了用户名
     */
    private HttpSession httpSession;

    //连接建立
    @OnOpen
    public void onOpen(Session session, EndpointConfig config){
        this.session = session;
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        this.httpSession = httpSession;
        //存储登陆的对象
        User user =(User)httpSession.getAttribute(USER_LOGIN_STATE);
        Long id = user.getId();
        String username = user.getUsername();
        onlineUsers.put(username,this);
        onlineStateMap.put(username,"0");
        log.info("websocket连接已建立！");
    }

    //收到消息
    @OnMessage
    public void onMessage(Session session,String message){

        log.info("websocket消息: 收到客户端消息:" + message);
        if (StringUtils.isBlank(message)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //将数据转换成对象
        try {
            ObjectMapper mapper =new ObjectMapper();
            Message mess = mapper.readValue(message, Message.class);
            String toName = mess.getToName();
            String data = mess.getMessage();
            log.info("websocket消息: toName:" + toName);
            log.info("websocket消息: 数据:" + data);
            log.info("websocket消息: 异常:" + onlineUsers.toString());
            User attribute =(User)httpSession.getAttribute(USER_LOGIN_STATE);
            String username = attribute.getUsername();
            log.info("websocket消息: 用户:" + username);
            String date=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            //找出队伍中所有用户的用户名
            //通过applicationContext获知指定的bean
            teamMapper=applicationContext.getBean(TeamMapper.class);
//            redisTemplate=applicationContext.getBean(RedisTemplate.class);
            int teamId = Integer.parseInt(toName);
            List<User> userList = teamMapper.selectUsernameByTeamId(teamId);
            userList.forEach((x)->{
                 String resultMessage = MessageUtils.getMessage(false, username,attribute.getAvatarUrl(),date, data);
                sendMessageIfOnline(x.getUsername(), username, resultMessage, onlineUsers);
                //保存聊天记录
//                redisTemplate.opsForList().rightPush(TEAM_CHAT_RECORDS+teamId,message);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //关闭
    @OnClose
    public void onClose(Session session) {

        User attribute =(User)httpSession.getAttribute(USER_LOGIN_STATE);
        String username = attribute.getUsername();
        onlineStateMap.remove(username);
        //从容器中删除指定的用户
        onlineUsers.remove(username);
        log.info("websocket连接已关闭！");
    }

    /**
     * 发送消息逻辑
     * @param teamUsername
     * @param currentUsername
     * @param message
     * @param onlineUsers
     */
    private void sendMessageIfOnline(String teamUsername, String currentUsername, String message, Map<String, TeamChatEndpoint> onlineUsers) {
        TeamChatEndpoint chatEndpoint = onlineUsers.get(teamUsername);
        if (chatEndpoint == null) {
            // 如果用户不在线，则直接结束方法，不发送消息
            return;
        } else {
            // 如果用户在线，判断消息发送者是否为当前用户
            if (!teamUsername.equals(currentUsername)) {
                try {
                    // 发送消息
                    onlineUsers.get(teamUsername).session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}

