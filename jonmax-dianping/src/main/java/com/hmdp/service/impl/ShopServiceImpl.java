package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        //缓存穿透，无法解决热点Key 问题
        //Shop shop = queryShopWithPassThrough(id);

        //用互斥锁，解决缓存击穿
        //Shop shop = qureyShopWithMutex(id);

        //逻辑过期，解决缓存击穿问题
        Shop shop = queryShopWithLogicalExpire(id);

        if (shop == null) {
            return Result.fail("此店铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("id不能为空");
        }
        //更新数据库
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    private boolean tryGetLock(String key) {
        Boolean falg = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(falg);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 可能存在缓存击穿击穿
     *
     * @param id
     * @return
     */
    public Shop qureyShopWithMutex(Long id) {
        // 拼接key
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        //通过key 获取redis中的对象
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //存在直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //判断命中是否是空值，空值就去查缓存的空值
        if (shopJson != null) {
            return null;
        }

        /**
         * 实现缓存重建
         */
        // 4 缓存重建
        //4.1 获取控制锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean lockFlag = tryGetLock(lockKey);
            //4.2 判断获取锁是否成功
            if (!lockFlag) {
                //4.3 锁获取失败，休眠，稍后重试
                Thread.sleep(50);
                return qureyShopWithMutex(id);
            }

            //4.4 获取锁成功，再根据id 去数据库中查询
            shop = getById(id);
            //模拟重建时延时
            Thread.sleep(200);
            if (shop == null) {
                //将shop的null值，以空值方式写入Redis
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                //返回错误信息
                return null;
            }
            //存在就写入redis缓存
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //4.5 释放互斥锁
            unLock(lockKey);
        }
        //返回结果
        return shop;
    }

    /**
     * 可能存在缓存击穿击穿
     *
     * @param id
     * @return
     */
    public Shop queryShopWithPassThrough(Long id) {
        // 拼接key
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        //通过key 获取redis中的对象
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            //存在直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //判断命中是否是空值，空值就去查缓存的空值
        if (shopJson != null) {
            return null;
        }
        //存在根据id 去数据库中查询
        Shop shop = getById(id);
        if (shop == null) {
            //将shop的null值，以空值方式写入Redis
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            //返回错误信息
            return null;
        }
        //存在就写入redis缓存
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //返回结果
        return shop;
    }
    public void saveShop2Redis(Long id , Long expireSeconds){
        //查询店铺信息
        Shop shop= getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(redisData));
    }

    public Shop queryShopWithLogicalExpire(Long id) {
        // 拼接key
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        //1 通过key 获取redis中的对象
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2 判断是否存在
        if (StrUtil.isBlank(shopJson)) {
            //3 不存在,直接返回
            return null;
        }
        //4 命中存在，把JSON反序列化
        //5 判断是否过期
           //5.1 未过期直接返回店铺信息
           //5.2 过期需要重建缓存
        //6 缓存重建
          //6.1 获取互斥锁
         //6.2 判断是否获取锁成功
        //6.3 成功，开启独立线程，重建缓存
        //6.4 返回过期缓存（成功/失败）
        
        return shop;
    }
}
