package com.redis.jedis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Agent节点启动类
 * 负责接收任务、管理服务、状态上报
 */
@SpringBootApplication
public class AgentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
