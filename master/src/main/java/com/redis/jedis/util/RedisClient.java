package com.redis.jedis.util;

import java.util.List;
import java.util.Set;

public interface RedisClient {
    String get(String key);
    void set(String key, String value);
    void setex(String key, int seconds, String value);
    void expire(String key, int seconds);
    void sadd(String key, String member);
    Set<String> smembers(String key);
    void lpush(String key, String value);
    void ltrim(String key, int start, int end);
    List<String> lrange(String key, int start, int end);
    String rpop(String key);
    void publish(String channel, String message);
    Set<String> keys(String pattern);
    void del(String key);
}


