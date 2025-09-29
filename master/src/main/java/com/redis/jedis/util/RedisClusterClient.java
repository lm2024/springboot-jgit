package com.redis.jedis.util;

import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Set;

public class RedisClusterClient implements RedisClient {
    private final JedisCluster delegate;

    public RedisClusterClient(JedisCluster delegate) {
        this.delegate = delegate;
    }

    @Override
    public String get(String key) { return delegate.get(key); }

    @Override
    public void setex(String key, int seconds, String value) { delegate.setex(key, seconds, value); }

    @Override
    public void expire(String key, int seconds) { delegate.expire(key, seconds); }

    @Override
    public void sadd(String key, String member) { delegate.sadd(key, member); }

    @Override
    public Set<String> smembers(String key) { return delegate.smembers(key); }

    @Override
    public void lpush(String key, String value) { delegate.lpush(key, value); }

    @Override
    public void ltrim(String key, int start, int end) { delegate.ltrim(key, start, end); }

    @Override
    public List<String> lrange(String key, int start, int end) { return delegate.lrange(key, start, end); }

    @Override
    public String rpop(String key) { return delegate.rpop(key); }

    @Override
    public void publish(String channel, String message) { delegate.publish(channel, message); }

    @Override
    public Set<String> keys(String pattern) { return delegate.keys(pattern); }

    @Override
    public void del(String key) { delegate.del(key); }
}


