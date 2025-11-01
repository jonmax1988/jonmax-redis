package com.jonmax.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JonMaxJedisConnetctFactory {
    private static final JedisPool JEDIS_POOL;
    static {
        //配置链接池
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(8);
        jedisPoolConfig.setMaxIdle(8);
        jedisPoolConfig.setMinIdle(0);
        jedisPoolConfig.setMaxWaitMillis(1000);
        //创建链接池对象
        JEDIS_POOL=new JedisPool(jedisPoolConfig,"172.18.100.100",6379,1000,"Kfcs@1234");
    }
    public static Jedis getJedis(){
        return  JEDIS_POOL.getResource();
    }
}
