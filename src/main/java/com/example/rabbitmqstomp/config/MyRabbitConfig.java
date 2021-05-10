package com.example.rabbitmqstomp.config;

import com.example.rabbitmqstomp.service.SendService;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author: hejiyuan
 * @Date: 2021/4/26 21:23
 * @Description: rabbitmq配置
 */
@Configuration
public class MyRabbitConfig {
    @Resource private SendService sendService;

    //群发队列绑定键
    @Value("${redis.routingKey.msgToAll}")
    private String msgTopicKey;
    //私聊队列绑定键
    @Value("${redis.routingKey.msgAlone}")
    private String msgTopicPrivateKey;

    //群发队列
    @Bean
    public Queue topicQueue() {
        return new Queue("topicQueue",true);
    }
    //私聊队列
    @Bean
    public Queue topicPrivateQueue(){
        return new Queue("topicPrivateQueue",true);
    }
    
    @Bean
    TopicExchange exchange() {
        return new TopicExchange("topicWebSocketExchange",true,false);
    }
    
    @Bean
    Binding bindingExchangeMessage() {
        return BindingBuilder.bind(topicQueue())
                .to(exchange())
                .with(msgTopicKey);
    }

    @Bean
    Binding bindingPrivateExchangeMessage() {
        return BindingBuilder.bind(topicPrivateQueue())
                .to(exchange())
                .with(msgTopicPrivateKey);
    }
    
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("127.0.0.1", 5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPublisherConfirms(true); // 必须要设置
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("ConfirmCallback:     "+"相关数据："+correlationData);
                System.out.println("ConfirmCallback:     "+"确认情况："+ack);
                System.out.println("ConfirmCallback:     "+"原因："+cause);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("ReturnCallback:     "+"消息："+message);
                System.out.println("ReturnCallback:     "+"回应码："+replyCode);
                System.out.println("ReturnCallback:     "+"回应信息："+replyText);
                System.out.println("ReturnCallback:     "+"交换机："+exchange);
                System.out.println("ReturnCallback:     "+"路由键："+routingKey);
            }
        });
        return rabbitTemplate;
    }

    /**
     * 接受消息的监听，这个监听会接受消息队列的消息
     * 针对消费者配置
     * @return
     */
    @Bean
    public SimpleMessageListenerContainer messageContainer() {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(topicQueue(),topicPrivateQueue());
        container.setExposeListenerChannel(true);
        container.setMaxConcurrentConsumers(100);
        container.setConcurrentConsumers(100);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
        container.setMessageListener(new ChannelAwareMessageListener() {
            public void onMessage(Message message, com.rabbitmq.client.Channel channel) throws Exception {
                byte[] body = message.getBody();
                String msg = new String(body);
                System.out.println("rabbitmq收到消息 : " +msg);
                Boolean sendToWebsocket = sendService.sendMsg(msg);
                if (sendToWebsocket){
                    System.out.println("消息处理成功！ 已经推送到websocket！");
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), true); //确认消息成功消费
                }
            }
        });
        return container;
    }
}
