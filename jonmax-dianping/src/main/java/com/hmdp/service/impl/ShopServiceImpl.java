package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate ;
    @Override
    public Result queryById(Long id) {
        // 拼接key
        String key = RedisConstants.CACHE_SHOP_KEY+id;
        //通过key 获取redisz中的对象
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断是否存在
        if(StrUtil.isNotBlank(shopJson)){
            //存在直接返回
            Shop shop= JSONUtil.toBean(shopJson,Shop.class);
            return Result.ok(shop);
        }
        //存在根据id 去数据库中查询
        Shop shop = getById(id);
        if(shop == null){
            //不存在就报错
            return Result.fail("商户信息不存在");
        }
        //存在就写入redis缓存
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop));
        //返回结果
        return Result.ok(shop);
    }
}
