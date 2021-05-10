package com.example.rabbitmqstomp.service;

import com.alibaba.fastjson.JSONObject;
import com.example.rabbitmqstomp.model.ChatMessage;
import com.example.rabbitmqstomp.model.MessageType;
import com.example.rabbitmqstomp.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

/**
 * @Author: hejiyuan
 * @Date: 2021/4/27 11:28
 * @Description: 服务端转发消息
 */
@Service
public class SendService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
    @Autowired private SimpMessageSendingOperations simpMessageSendingOperations;
    @Autowired private RedisUtils redisUtils;
    
    public Boolean sendMsg(String msg) {
        try {
            JSONObject msgJson = JSONObject.parseObject(msg);
            String sender = msgJson.getString("sender");
            String content = msgJson.getString("content");
            String type = msgJson.getString("type");
            String to = msgJson.getString("to");
            if(type.equals(MessageType.SYSTEM.name())){
                switch (content){
                    case "refresh":
                        String result = redisUtils.findAllUsers();
                        String receiver = redisUtils.get(to);
                        simpMessageSendingOperations.convertAndSendToUser(receiver,"/topic/msg", new ChatMessage(sender, result, MessageType.SYSTEM, to));
                        break;
                }
            }else{
                if (to.equals("all") && type.equals(MessageType.CHAT.name())){
                    simpMessageSendingOperations.convertAndSend("/topic/public", msgJson);
                }else if (to.equals("all") && type.equals(MessageType.JOIN.name())) {
                    simpMessageSendingOperations.convertAndSend("/topic/public", msgJson);
                }else if (!to.equals("all") && type.equals(MessageType.CHAT.name())){
                    String receiver = redisUtils.get(to);
                    simpMessageSendingOperations.convertAndSendToUser(receiver,"/topic/msg", msgJson);
                }else{
                    redisUtils.delete(sender);
                    simpMessageSendingOperations.convertAndSend("/topic/public", new ChatMessage(sender, content, MessageType.LEAVE, to));
                }
            }
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage(),"发送了错误的消息！");
            return false;
        }
        return true;
    }
}
