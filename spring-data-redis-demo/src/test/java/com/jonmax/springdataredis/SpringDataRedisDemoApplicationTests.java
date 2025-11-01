package com.jonmax.springdataredis;

import com.jonmax.springdataredis.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@SpringBootTest
class SpringDataRedisDemoApplicationTests {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    @Test
    void testString() {
        //临时解决方案
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.opsForValue().set("name","囧麦");
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println("name = " + name);

    }
    @Test
    void testUser(){
        redisTemplate.opsForValue().set("user:1",new User("jon",20));
        User user = (User) redisTemplate.opsForValue().get("user:1");
        System.out.println("user = " + user);
    }

}
