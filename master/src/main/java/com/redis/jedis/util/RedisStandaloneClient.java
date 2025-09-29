package com.redis.jedis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Set;

public class RedisStandaloneClient implements RedisClient {
    private final JedisPool pool;

    public RedisStandaloneClient(String host, int port, String password, int maxActive, int maxIdle, int minIdle, long maxWait) {
        JedisPoolConfig cfg = new JedisPoolConfig();
        cfg.setMaxTotal(maxActive);
        cfg.setMaxIdle(maxIdle);
        cfg.setMinIdle(minIdle);
        cfg.setMaxWaitMillis(maxWait);
        if (password != null && !password.isEmpty()) {
            this.pool = new JedisPool(cfg, host, port, 2000, password);
        } else {
            this.pool = new JedisPool(cfg, host, port);
        }
    }

    private <T> T withJedis(RedisCallback<T> cb) {
        try (Jedis jedis = pool.getResource()) {
            return cb.doInRedis(jedis);
        }
    }

    @Override
    public String get(String key) { return withJedis(j -> j.get(key)); }

    @Override
    public void setex(String key, int seconds, String value) { withJedis(j -> { j.setex(key, seconds, value); return null; }); }

    @Override
    public void expire(String key, int seconds) { withJedis(j -> { j.expire(key, seconds); return null; }); }

    @Override
    public void sadd(String key, String member) { withJedis(j -> { j.sadd(key, member); return null; }); }

    @Override
    public Set<String> smembers(String key) { return withJedis(j -> j.smembers(key)); }

    @Override
    public void lpush(String key, String value) { withJedis(j -> { j.lpush(key, value); return null; }); }

    @Override
    public void ltrim(String key, int start, int end) { withJedis(j -> { j.ltrim(key, start, end); return null; }); }

    @Override
    public List<String> lrange(String key, int start, int end) { return withJedis(j -> j.lrange(key, start, end)); }

    @Override
    public String rpop(String key) { return withJedis(j -> j.rpop(key)); }

    @Override
    public void publish(String channel, String message) { withJedis(j -> { j.publish(channel, message); return null; }); }

    private interface RedisCallback<T> { T doInRedis(Jedis jedis); }

    @Override
    public Set<String> keys(String pattern) { return withJedis(j -> j.keys(pattern)); }

    @Override
    public void del(String key) { withJedis(j -> { j.del(key); return null; }); }
}


