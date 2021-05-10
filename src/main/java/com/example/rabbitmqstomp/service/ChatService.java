package com.example.rabbitmqstomp.service;

import com.example.rabbitmqstomp.model.ChatMessage;
import com.example.rabbitmqstomp.util.JsonUtil;
import com.example.rabbitmqstomp.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Principal;

/**
 * @Author: hejiyuan
 * @Date: 2021/4/26 21:14
 * @Description: 消息处理，根据消息的格式来决定发送到哪个 topic
 */
@Service
public class ChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
    
    @Autowired private RedisUtils redisUtils;
    @Autowired private RabbitTemplate rabbitTemplate;

    @Value("${redis.routingKey.msgToAll}")
    private String msgToAll;
    @Value("${redis.routingKey.msgAlone}")
    private String msgAlone;
    

    /**
     * 添加用户，并将添加用户的消息广播
     * @param chatMessage
     * @param userPrincipal
     */
    public void addUser(ChatMessage chatMessage, Principal userPrincipal) {
        try {
            redisUtils.set(chatMessage.getSender(), userPrincipal.getName());
            // 发送登录消息给所有人
            rabbitTemplate.convertAndSend("topicWebSocketExchange","topic.public", JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 群发消息
     * @param chatMessage
     */
    public void sendMessage(ChatMessage chatMessage) {
        try {
            System.out.println("---------------群发消息------------");
            rabbitTemplate.convertAndSend("topicWebSocketExchange", msgToAll, JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 私发消息
     * @param chatMessage
     */
    public void sendMessageAlone(ChatMessage chatMessage) {
        try {
            System.out.println("---------------单发消息----------");
            rabbitTemplate.convertAndSend("topicWebSocketExchange", msgAlone, JsonUtil.parseObjToJson(chatMessage));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
}