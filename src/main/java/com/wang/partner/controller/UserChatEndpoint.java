package com.wang.partner.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.partner.common.ErrorCode;
import com.wang.partner.config.GetHttpSessionConfigurator;
import com.wang.partner.exception.BusinessException;
import com.wang.partner.model.domain.Message;
import com.wang.partner.model.domain.User;
import com.wang.partner.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.wang.partner.contant.UserConstant.USER_LOGIN_STATE;


/**
 * @author 22603
 */
@ServerEndpoint(value = "/chat",configurator = GetHttpSessionConfigurator.class)
@Component
@Slf4j
public class UserChatEndpoint {



    /**
     * 用来存储每个用户客户端对象的ChatEndpoint对象
     */
    private static Map<String, UserChatEndpoint> onlineUsers = new ConcurrentHashMap<>();
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
            String resultMessage = MessageUtils.getMessage(false, username,null,date, data);
            //发送数据
            log.info("websocket消息: 异常:" + resultMessage);
            UserChatEndpoint userChatEndpoint = onlineUsers.get(toName);
            if (userChatEndpoint ==null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"该用户不在线!");
            }
            //判断二者是否单身
            if(onlineStateMap.get(username).equals(onlineStateMap.get(toName))){
//                onlineStateMap.put(username,username+toName);
//                onlineStateMap.put(toName,username+toName);
                onlineUsers.get(toName).session.getBasicRemote().sendText(resultMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //关闭
    @OnClose
    public void onClose(Session session) {

        User attribute =(User)httpSession.getAttribute(USER_LOGIN_STATE);
        String username = attribute.getUsername();
        //从容器中删除指定的用户
        onlineUsers.remove(username);
        log.info("websocket连接已关闭！");
    }}

