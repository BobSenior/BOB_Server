package com.bob_senior.bob_server.service;


import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class RedisService {
    private final RedisTemplate redisTemplate;
    private final ValueOperations<String, Object> valueOperations;

    @Autowired
    public RedisService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        valueOperations = this.redisTemplate.opsForValue();
    }

    // refresh 토큰 저장하기
    public Boolean setRedisRefreshToken(int userIdx, String refreshToken, Long expireTime){
        String key = "refresh" + userIdx;
        valueOperations.set(key, refreshToken);
        return redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
    }

    // refresh 토큰 가져오기
    public String getRedisRefreshToken(int userIdx){
        String key = "refresh" + userIdx;
        String refreshToken = (String) valueOperations.get(key);
        // 해당 userIdx로 발급받은 refresh 토큰이 없다면 null 리턴
        return refreshToken;
    }
}
