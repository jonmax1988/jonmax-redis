package com.jonmax;


import com.jonmax.utils.JonMaxJedisConnetctFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private Jedis jedis;

    @BeforeEach
    void setUp() {
        //建立连接
        //jedis =new Jedis("172.18.100.100",6379);
        jedis= JonMaxJedisConnetctFactory.getJedis();
        //设置密码
        jedis.auth("Kfcs@1234");
        //选择库
        jedis.select(1);
    }

    @Test
    public void testJedisString(){
        String keyName = jedis.set("name", "Lee立哥");
        System.out.println("keyName = " + keyName);
        String name = jedis.get("name");
        System.out.println("name = " + name);
    }

    @Test
    public void testJedisHash(){
        jedis.hset("user:1","name","jon");
        jedis.hset("user:1","age","18");
        Map<String, String> map = jedis.hgetAll("user:1");
        System.out.println(map);
    }

    @AfterEach
    void afterAll() {
        if(jedis != null){
            jedis.close();
        }
    }
}
