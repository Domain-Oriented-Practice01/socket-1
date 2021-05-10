package com.example.rabbitmqstomp.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author: hejiyuan
 * @Date: 2021/4/27 10:21
 * @Description: redis工具方法
 */
@Component
public class RedisUtils {
    @Autowired private RedisTemplate redisTemplate;

    /**
     * 保存用户标识信息，有效期为一天
     * @param key
     * @param value
     */
    public void set(String key, String value){
        redisTemplate.opsForValue().set(key, value, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    /**
     * 获取key对应的值
     * @param key
     * @return
     */
    public String get(String key){
        String value = (String) redisTemplate.opsForValue().get(key);
        return value;
    }

    /**
     * 删除key
     * @param key
     */
    public void delete(String key){
        redisTemplate.delete(key);
    }
    
    /**
     * 获取所有登录用户信息
     * @return
     */
    public String findAllUsers(){
        Set<String> users = redisTemplate.keys("*");
        Iterator<String> userIterator = users.iterator();
        String content = "[";
        while(userIterator.hasNext())
            content += userIterator.next() + ",";
        content += "]";
        return content;
    }
}
