package com.example.rabbitmqstomp.config.interceptor;

import com.example.rabbitmqstomp.model.UserPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class GetHeaderParamInterceptor extends ChannelInterceptorAdapter {

    /**
     * 处理Stomp消息帧，增加消息的用户标识
     * @param message
     * @param channel
     * @return
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 给用户设置自定义的标识
            accessor.setUser(new UserPrincipal(accessor.getSessionId()));
        }
        return message;
    }
}