package com.redis.jedis.service;

import com.alibaba.fastjson.JSON;
import com.redis.jedis.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.redis.jedis.util.RedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis缓存服务
 * 负责Redis数据的存储、获取和过期时间管理
 */
@Service
public class RedisCacheService {
    
    @Autowired
    private RedisClient redisClient;
    
    // 监控数据缓存时间（60天）
    private static final int MONITOR_CACHE_TTL = 5184000;
    
    // 任务数据缓存时间（7天）
    private static final int TASK_CACHE_TTL = 604800;
    
    // 队列数据缓存时间（1小时）
    private static final int QUEUE_CACHE_TTL = 3600;
    
    // 部署历史缓存时间（180天）
    private static final int DEPLOY_HISTORY_TTL = 15552000;
    
    /**
     * 存储节点状态，自动设置过期时间
     */
    public void setNodeStatus(String nodeId, NodeStatus status) {
        try {
            String key = "node:status:" + nodeId;
            redisClient.setex(key, MONITOR_CACHE_TTL, JSON.toJSONString(status));
            
            // 更新节点列表
            redisClient.sadd("node:list", nodeId);
            redisClient.expire("node:list", MONITOR_CACHE_TTL);
            
        } catch (Exception e) {
            throw new RuntimeException("存储节点状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取节点状态，自动刷新过期时间
     */
    public NodeStatus getNodeStatus(String nodeId) {
        try {
            String key = "node:status:" + nodeId;
            String value = redisClient.get(key);
            
            if (value != null) {
                // 刷新过期时间
                redisClient.expire(key, MONITOR_CACHE_TTL);
                return JSON.parseObject(value, NodeStatus.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取节点状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有节点状态
     */
    public List<NodeStatus> getAllNodeStatus() {
        try {
            List<NodeStatus> statusList = new ArrayList<>();
            Set<String> nodeIds = redisClient.smembers("node:list");
            
            for (String nodeId : nodeIds) {
                NodeStatus status = getNodeStatus(nodeId);
                if (status != null) {
                    statusList.add(status);
                }
            }
            
            return statusList;
        } catch (Exception e) {
            throw new RuntimeException("获取所有节点状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 存储节点详情，自动设置过期时间
     */
    public void setNodeDetail(String nodeId, NodeDetail detail) {
        try {
            String key = "node:detail:" + nodeId;
            redisClient.setex(key, MONITOR_CACHE_TTL, JSON.toJSONString(detail));
        } catch (Exception e) {
            throw new RuntimeException("存储节点详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取节点详情，自动刷新过期时间
     */
    public NodeDetail getNodeDetail(String nodeId) {
        try {
            String key = "node:detail:" + nodeId;
            String value = redisClient.get(key);
            
            if (value != null) {
                // 刷新过期时间
                redisClient.expire(key, MONITOR_CACHE_TTL);
                return JSON.parseObject(value, NodeDetail.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取节点详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 存储健康状态，自动设置过期时间
     */
    public void setHealthStatus(String nodeId, HealthStatus health) {
        try {
            String key = "health:status:" + nodeId;
            redisClient.setex(key, MONITOR_CACHE_TTL, JSON.toJSONString(health));
        } catch (Exception e) {
            throw new RuntimeException("存储健康状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取健康状态，自动刷新过期时间
     */
    public HealthStatus getHealthStatus(String nodeId) {
        try {
            String key = "health:status:" + nodeId;
            String value = redisClient.get(key);
            
            if (value != null) {
                // 刷新过期时间
                redisClient.expire(key, MONITOR_CACHE_TTL);
                return JSON.parseObject(value, HealthStatus.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取健康状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有健康状态
     */
    public List<HealthStatus> getAllHealthStatus() {
        try {
            List<HealthStatus> healthList = new ArrayList<>();
            Set<String> nodeIds = redisClient.smembers("node:list");
            
            for (String nodeId : nodeIds) {
                HealthStatus health = getHealthStatus(nodeId);
                if (health != null) {
                    healthList.add(health);
                }
            }
            
            return healthList;
        } catch (Exception e) {
            throw new RuntimeException("获取所有健康状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 存储部署信息，自动设置过期时间
     */
    public void setDeployInfo(String nodeId, String serviceName, DeployInfo deployInfo) {
        try {
            String key = "service:deploy:" + nodeId + ":" + serviceName;
            redisClient.setex(key, MONITOR_CACHE_TTL, JSON.toJSONString(deployInfo));
            
            // 更新部署历史
            String historyKey = "deploy:history:" + nodeId + ":" + serviceName;
            redisClient.lpush(historyKey, JSON.toJSONString(deployInfo));
            redisClient.expire(historyKey, DEPLOY_HISTORY_TTL);
            
            // 限制历史记录数量（保留最近50条）
            redisClient.ltrim(historyKey, 0, 49);
            
        } catch (Exception e) {
            throw new RuntimeException("存储部署信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取部署信息，自动刷新过期时间
     */
    public DeployInfo getDeployInfo(String nodeId, String serviceName) {
        try {
            String key = "service:deploy:" + nodeId + ":" + serviceName;
            String value = redisClient.get(key);
            
            if (value != null) {
                // 刷新过期时间
                redisClient.expire(key, MONITOR_CACHE_TTL);
                return JSON.parseObject(value, DeployInfo.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取部署信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取部署历史
     */
    public List<DeployHistory> getDeployHistory(String nodeId, String serviceName) {
        try {
            String key = "deploy:history:" + nodeId + ":" + serviceName;
            List<String> historyList = redisClient.lrange(key, 0, -1);
            
            List<DeployHistory> history = new ArrayList<>();
            for (String item : historyList) {
                DeployInfo info = JSON.parseObject(item, DeployInfo.class);
                DeployHistory historyItem = new DeployHistory();
                historyItem.setServiceName(serviceName);
                historyItem.setDeployTime(info.getDeployTime());
                historyItem.setVersion(info.getVersion());
                historyItem.setStatus(info.getStatus());
                history.add(historyItem);
            }
            
            return history;
        } catch (Exception e) {
            throw new RuntimeException("获取部署历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送消息到队列，自动设置过期时间
     */
    public void sendToQueue(String queueName, Object message) {
        try {
            String key = "queue:" + queueName;
            redisClient.lpush(key, JSON.toJSONString(message));
            redisClient.expire(key, QUEUE_CACHE_TTL);
        } catch (Exception e) {
            throw new RuntimeException("发送消息到队列失败: " + e.getMessage());
        }
    }
    
    /**
     * 从队列获取消息，自动刷新过期时间
     */
    public String getFromQueue(String queueName) {
        try {
            String key = "queue:" + queueName;
            String message = redisClient.rpop(key);
            
            if (message != null) {
                // 刷新过期时间
                redisClient.expire(key, QUEUE_CACHE_TTL);
            }
            return message;
        } catch (Exception e) {
            throw new RuntimeException("从队列获取消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 发布消息到频道
     */
    public void publishMessage(String channel, Object message) {
        try {
            redisClient.publish(channel, JSON.toJSONString(message));
        } catch (Exception e) {
            throw new RuntimeException("发布消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期数据
     */
    public void cleanupExpiredData() {
        try {
            // Redis会自动清理过期的key，这里可以添加额外的清理逻辑
            System.out.println("清理过期数据完成");
        } catch (Exception e) {
            System.err.println("清理过期数据失败: " + e.getMessage());
        }
    }
}

