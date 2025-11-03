package com.hmdp.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    //注入StringRedisTemplate
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        // 尝试从Redis缓存当中获取，按约定拼接Key
        String key = RedisConstants.CACHE_SHOP_TYPE_KEY;

        // 通过key获取Redis中的值
        List<String> cachedList = stringRedisTemplate.opsForList().range(key, 0, -1);

        // 判断，如果存在就直接返回
//        if (cachedList != null && !cachedList.isEmpty()) {
//            // 将Redis中的字符串列表转换为ShopType对象列表
//            List<ShopType> resultList = cachedList.stream()
//                    .map(json -> JSONUtil.toBean(json, ShopType.class))
//                    .collect(Collectors.toList());
//            return Result.ok(resultList);
//        }
        //传统写法

//        if(cachedList !=null && !cachedList.isEmpty()){
//            List<ShopType> resultList = new ArrayList<>();
//            for (String json : cachedList) {
//                ShopType shopType  = JSONUtil.toBean(json, ShopType.class);
//                resultList.add(shopType );
//            }
//            return Result.ok(resultList);
//        }
        //流写法
        if(cachedList !=null && !cachedList.isEmpty()){
            List<ShopType> resultList = cachedList.stream()
                    .map(json -> JSONUtil.toBean(json, ShopType.class))
                    .collect(Collectors.toList());
                    return Result.ok(resultList);
        }

        // Redis中不存在就去数据库中查询
        List<ShopType> shopTypes = query().orderByAsc("sort").list();

        // 如果数据库中不存在就返回错误信息
        if (shopTypes == null || shopTypes.isEmpty()) {
            return Result.fail("未查到任何信息");
        }

        // 将每个ShopType对象转为JSON字符串列表
        List<String> shopTypeJsonList = shopTypes.stream()
                .map(JSONUtil::toJsonStr)
                .collect(Collectors.toList());

        // 存在，缓存到redis，并设置过期时间
        stringRedisTemplate.opsForList().rightPushAll(key, shopTypeJsonList);
        stringRedisTemplate.expire(key, RedisConstants.CACHE_SHOP_TYPE_TTL, TimeUnit.HOURS);

        // 返回给前端
        return Result.ok(shopTypes);
    }
}
