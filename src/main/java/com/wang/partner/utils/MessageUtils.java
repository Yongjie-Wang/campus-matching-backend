package com.wang.partner.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.partner.model.domain.ResultMessage;


public class MessageUtils {
    public static String getMessage(boolean isSystemMessage, String fromName,String userAvatar,  String date,Object message){
        try {
            ResultMessage result = new ResultMessage();
            result.setSystem(isSystemMessage);
            result.setUserAvatar(userAvatar);
            result.setMessage(message);
            result.setNowTime(date);

            if (fromName!=null){
                result.setFromName(fromName);
            }
            //把字符串转成json格式的字符串
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(result);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return null;
    }
}

