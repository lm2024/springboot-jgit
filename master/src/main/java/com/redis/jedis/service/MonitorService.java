package com.redis.jedis.service;

import com.redis.jedis.dto.NodeStatus;
import com.redis.jedis.dto.NodeDetail;
import com.redis.jedis.dto.HealthStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 监控服务
 * 负责节点状态监控功能
 */
@Service
public class MonitorService {
    
    @Autowired
    private RedisCacheService redisCacheService;
    
    /**
     * 获取所有节点状态
     */
    public List<NodeStatus> getAllNodes() {
        try {
            return redisCacheService.getAllNodeStatus();
        } catch (Exception e) {
            throw new RuntimeException("获取所有节点状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个节点详情
     */
    public NodeDetail getNodeDetail(String nodeId) {
        try {
            return redisCacheService.getNodeDetail(nodeId);
        } catch (Exception e) {
            throw new RuntimeException("获取节点详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取节点健康状态
     */
    public HealthStatus getNodeHealth(String nodeId) {
        try {
            return redisCacheService.getHealthStatus(nodeId);
        } catch (Exception e) {
            throw new RuntimeException("获取节点健康状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有节点健康状态
     */
    public List<HealthStatus> getAllNodeHealth() {
        try {
            return redisCacheService.getAllHealthStatus();
        } catch (Exception e) {
            throw new RuntimeException("获取所有节点健康状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新所有节点状态
     */
    public void refreshAllNodeStatus() {
        try {
            // 清理过期数据
            redisCacheService.cleanupExpiredData();
            System.out.println("节点状态刷新完成");
        } catch (Exception e) {
            throw new RuntimeException("刷新节点状态失败: " + e.getMessage());
        }
    }
}
