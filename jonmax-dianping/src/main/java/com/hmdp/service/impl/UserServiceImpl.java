package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验手机号
        boolean flag=RegexUtils.isPhoneInvalid(phone);
        if(flag){
            //如果不符合，返回错误信息
           return Result.fail("手机号填写错误，请重新填写。");
        }
        //符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //保存到session
        //session.setAttribute("code",code);
        //替换方案保存到Redis当中
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
        //发送短信验证码
        log.debug("发送短信验证码成功，验证码:{}"+code);
        //返回OK
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1 校验手机号,falg为true校验不通过
        String phone = loginForm.getPhone();
        boolean pFlag = RegexUtils.isPhoneInvalid(phone);
        if(pFlag){
            return Result.fail("手机号输入错误，请重新输入。");
        }
        // 2 校验验证码
       // Object cacheCode = session.getAttribute("code");
        //改成从Redis当中取验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();

//        if(cacheCode == null || !cacheCode.toString().equals(code)){
        if(cacheCode == null || !cacheCode.equals(code)){
            return Result.fail("验证码错误，请重新输入。");
        }
        //3 检查用户是否存在，不存在则创建用户
        User user = query().eq("phone", phone).one();
        if(user == null){
            //创建用户
            user=createUserWithPhone(phone);
        }
        //保存用户信息到session，
        //session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        /**
         *  保存用户到redis中
         *  1 通过UUID生成token
         *  2 将userDTO转化为HashMap存入redis
         * */
        String token = UUID.randomUUID().toString(false);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        String tokenKey=LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);
        //设置有效期时长
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);
        //return Result.ok();
        return  Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        //通过手机号创建新用户
        User user=new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        save(user);
        return user;
    }

}
