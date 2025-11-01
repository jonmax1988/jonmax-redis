package com.jonmax.springdataredis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jonmax.springdataredis.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@SpringBootTest
class RedisStringTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    void testString() {
        //临时解决方案
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new StringRedisSerializer());
        stringRedisTemplate.opsForValue().set("name","囧麦s");
        Object name = stringRedisTemplate.opsForValue().get("name");
        System.out.println("name = " + name);

    }

  private static final ObjectMapper mapper=new ObjectMapper();
    @Test
    void testUser() throws JsonProcessingException {
    User user =new User("max",18);
        String userString = mapper.writeValueAsString(user);
        stringRedisTemplate.opsForValue().set("user:2",userString);
        String s = stringRedisTemplate.opsForValue().get("user:2");
        System.out.println("s = " + s);
        System.out.println("mapper.readValue(s,User.class) = " + mapper.readValue(s, User.class));
//        redisTemplate.opsForValue().set("user:1",new User("jon",20));
//        User user = (User) redisTemplate.opsForValue().get("user:1");
//        System.out.println("user = " + user);
    }
    @Test
    void  testHash(){
        stringRedisTemplate.opsForHash().put("user:3","name","dd");
        stringRedisTemplate.opsForHash().put("user:3","age","19");
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries("user:3");
        System.out.println("entries = " + entries);
    }

}
