package com.redis.jedis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import com.redis.jedis.util.RedisClient;
import com.redis.jedis.util.RedisClusterClient;
import com.redis.jedis.util.RedisStandaloneClient;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Redis集群配置类
 * 使用Jedis连接Redis集群
 */
@Configuration
public class JedisConfig {

    @Value("${redis.cluster.nodes:}")
    private String clusterNodesStr;

    @Value("${redis.cluster.password:}")
    private String password;

    @Value("${redis.cluster.timeout:5000}")
    private int timeout;

    // JedisCluster 读写超时（可与 timeout 一致）
    @Value("${redis.cluster.so-timeout:5000}")
    private int soTimeout;

    @Value("${redis.cluster.max-redirects:3}")
    private int maxRedirects;

    @Value("${redis.jedis.pool.max-active:20}")
    private int maxActive;

    @Value("${redis.jedis.pool.max-idle:10}")
    private int maxIdle;

    @Value("${redis.jedis.pool.min-idle:5}")
    private int minIdle;

    @Value("${redis.jedis.pool.max-wait:3000}")
    private long maxWait;

    @Value("${spring.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Bean
    public RedisClient jedisCommands() {
        // 如果配置了集群节点，则优先使用集群模式
        if (clusterNodesStr != null && !clusterNodesStr.trim().isEmpty()) {
            Set<HostAndPort> nodes = new HashSet<>();
            System.out.println("Redis cluster nodes configuration: " + clusterNodesStr);
            String[] nodeArray = clusterNodesStr.split(",");
            for (String node : nodeArray) {
                node = node.trim();
                System.out.println("Adding Redis node: " + node);
                String[] parts = node.split(":");
                nodes.add(new HostAndPort(parts[0], Integer.parseInt(parts[1])));
            }

            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(maxActive);
            poolConfig.setMaxIdle(maxIdle);
            poolConfig.setMinIdle(minIdle);
            poolConfig.setMaxWaitMillis(maxWait);
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);

            if (password != null && !password.trim().isEmpty()) {
                // 带密码的 JedisCluster 构造
                return new RedisClusterClient(new JedisCluster(nodes, timeout, soTimeout, maxRedirects, password, poolConfig));
            }
            // 无密码的 JedisCluster 构造
            return new RedisClusterClient(new JedisCluster(nodes, timeout, maxRedirects, poolConfig));
        }

        // 否则使用单机模式（JedisPooled 线程安全，且实现 JedisCommands）
        System.out.println("Using standalone Redis at " + redisHost + ":" + redisPort + ", db=" + redisDatabase);
        return new RedisStandaloneClient(redisHost, redisPort, redisPassword, maxActive, maxIdle, minIdle, maxWait);
    }
}