package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class CacheClient {
    private  static final ExecutorService CACHE_REBUILD_EXECUTOR= Executors.newFixedThreadPool(10);
    private final StringRedisTemplate stringRedisTemplate;
    private boolean tryGetLock(String key) {
        Boolean falg = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(falg);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,timeUnit);
    }

    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit timeUnit) {
        RedisData redisData=new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <R,ID> R queryWithPassThrough(String keyPrefix,  ID id, Class<R> type,
                                             Function<ID,R> dbFallback,
                                             Long time, TimeUnit timeUnit) {
        // 拼接key
        String key = keyPrefix + id;
        //通过key 获取redis中的对象
        String json = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(json)) {
            //存在直接返回
            return JSONUtil.toBean(json, type);
        }
        //判断命中是否是空值，空值就去查缓存的空值
        if (json != null) {
            return null;
        }
        //存在根据id 去数据库中查询
        R r = dbFallback.apply(id);
        if (r == null) {
            //将shop的null值，以空值方式写入Redis
            stringRedisTemplate.opsForValue().set(key, "", time, timeUnit);
            //返回错误信息
            return null;
        }
        //存在就写入redis缓存
        this.set(key,r,time,timeUnit);
        //返回结果
        return r;
    }
    public <R, ID> R queryWithLogicalExpire(String keyPrefix,String LockKeyPrefix,ID id,Class<R> type,
                                            Function<ID,R> dbFallback,
                                            Long time, TimeUnit timeUnit) {
        // 拼接key
        String key = keyPrefix + id;
        //1 通过key 获取redis中的对象
        String json = stringRedisTemplate.opsForValue().get(key);
        //2 判断是否存在
        if (StrUtil.isBlank(json)) {
            //3 不存在,直接返回
            return null;
        }
        //4 命中存在，把JSON反序列化
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        //JSONObject data = (JSONObject)redisData.getData();
        R r = JSONUtil.toBean((JSONObject)redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        //5 判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            //5.1 未过期直接返回店铺信息
            return r;
        }
        //5.2 过期需要重建缓存
        //6 缓存重建
        //6.1 获取互斥锁
        String lockKey=LockKeyPrefix+id;
        boolean isLock = tryGetLock(lockKey);
        //6.2 判断是否获取锁成功
        if(isLock){
            //6.3 成功，开启独立线程，重建缓存
            try {
                CACHE_REBUILD_EXECUTOR.submit(()-> {
                    R r1= dbFallback.apply(id);
                    this.setWithLogicalExpire(key,r1,time,timeUnit);
                });
            }catch (Exception e){
                throw  new RuntimeException(e);
            }finally {
                unLock(lockKey);
            }
        }
        //6.4 返回过期缓存（成功/失败）
        return r;
    }
}
