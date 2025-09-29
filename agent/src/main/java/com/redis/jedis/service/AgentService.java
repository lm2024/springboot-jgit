package com.redis.jedis.service;

import com.redis.jedis.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.alibaba.fastjson.JSON;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

/**
 * Agent服务
 * 负责Agent节点的核心功能
 */
@Service
public class AgentService {
    
    @Value("${node.id}")
    private String nodeId;
    
    @Value("${node.name}")
    private String nodeName;
    
    @Autowired
    private SystemInfoService systemInfoService;
    
    @Autowired
    private ServiceManagementService serviceManagementService;
    
    @Autowired
    private TaskExecutionService taskExecutionService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 获取节点状态
     */
    public StatusReport getNodeStatus() {
        StatusReport report = new StatusReport();
        report.setNodeId(nodeId);
        report.setTimestamp(System.currentTimeMillis());
        report.setCpuUsage(systemInfoService.getCpuUsage());
        report.setMemoryUsage(systemInfoService.getMemoryUsage());
        report.setServiceStatus(getServiceStatus());
        report.setLastDeployTime(getLastDeployTime());
        report.setLastDeployVersion(getLastDeployVersion());
        return report;
    }
    
    /**
     * 上报状态
     */
    public void reportStatus() {
        StatusReport report = getNodeStatus();
        // 1) 写入节点状态
        String statusKey = "node:status:" + nodeId;
        stringRedisTemplate.opsForValue().set(statusKey, JSON.toJSONString(report));
        // 2) 维护节点列表
        stringRedisTemplate.opsForSet().add("node:list", nodeId);
        // 3) 写入健康状态（简单映射为整体节点健康）
        Map<String, Object> health = new HashMap<>();
        health.put("nodeId", nodeId);
        health.put("serviceName", "node");
        health.put("status", "HEALTHY");
        health.put("checkTime", System.currentTimeMillis());
        String healthKey = "health:status:" + nodeId;
        stringRedisTemplate.opsForValue().set(healthKey, JSON.toJSONString(health));
    }

    /**
     * 定时上报（默认每30秒），可通过 agent application.yml 的 monitor.report-interval 调整后续扩展
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 5000)
    public void periodicReport() {
        try {
            reportStatus();
        } catch (Exception e) {
            System.err.println("定时上报失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行部署任务
     */
    public void executeDeployTask(DeployTask task) {
        // TODO: 执行部署任务
        // 这里先空实现，后续实现
    }
    
    /**
     * 启动服务
     */
    public void startService(String serviceName) {
        serviceManagementService.startService(serviceName);
    }
    
    /**
     * 停止服务
     */
    public void stopService(String serviceName) {
        serviceManagementService.stopService(serviceName);
    }
    
    /**
     * 重启服务
     */
    public void restartService(String serviceName) {
        serviceManagementService.restartService(serviceName);
    }
    
    /**
     * 获取服务状态
     */
    public ServiceStatus getServiceStatus(String serviceName) {
        return serviceManagementService.getServiceStatus(serviceName);
    }
    
    /**
     * 获取所有服务状态
     */
    public List<ServiceStatus> getAllServices() {
        return serviceManagementService.getAllServices();
    }
    
    /**
     * 健康检查
     */
    public boolean healthCheck() {
        // TODO: 实现健康检查逻辑
        // 这里先返回true，后续实现
        return true;
    }
    
    private String getServiceStatus() {
        // TODO: 获取服务状态
        // 这里先返回RUNNING，后续实现
        return "RUNNING";
    }
    
    private long getLastDeployTime() {
        // TODO: 获取最近部署时间
        // 这里先返回当前时间，后续实现
        return System.currentTimeMillis();
    }
    
    private String getLastDeployVersion() {
        // TODO: 获取最近部署版本
        // 这里先返回默认版本，后续实现
        return "v" + System.currentTimeMillis();
    }
}
