package com.redis.jedis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Master节点启动类
 * 负责监控、部署管理、文件分发
 */
@SpringBootApplication
public class MasterApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MasterApplication.class, args);
    }
}
