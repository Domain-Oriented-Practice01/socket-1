package com.example.rabbitmqstomp.controller;

import com.example.rabbitmqstomp.model.ChatMessage;
import com.example.rabbitmqstomp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.security.Principal;

@RestController
public class ChatController {
    @Resource private ChatService chatService;
    
    @MessageMapping("/chat/addUser")
    public void addUser(@Payload ChatMessage chatMessage,
                        Principal userPrincipal) {
        chatService.addUser(chatMessage, userPrincipal);
    }
    
    @MessageMapping("/chat/sendMessageAll")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        chatService.sendMessage(chatMessage);
    }

    @MessageMapping("/chat/sendMessageAlone")
    public void sendMessageAlone(@Payload ChatMessage chatMessage) {
        chatService.sendMessageAlone(chatMessage);
    }
    
}