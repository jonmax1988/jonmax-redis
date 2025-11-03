package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(new LoginInterceptor()).excludePathPatterns(
               "/swagger-ui.html",
               "/swagger-resources/**",
               "/webjars/**",
               "/v2/api-docs",
               "/v3/api-docs",
               "/v3/api-docs/**",
               "/doc.html",
               "/favicon.ico",

               "/voucher/**",
               "/upload/**",
               "/shop/**",
               "/shop-type/**",
               "/blog/hot",
               "/user/code",
               "/user/login"
       ).order(1);
       registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}
