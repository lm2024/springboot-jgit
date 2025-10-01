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
import java.util.concurrent.TimeUnit;

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
            System.out.println("Agent[" + nodeId + "]: 开始定时状态上报...");
            reportStatus();
            System.out.println("Agent[" + nodeId + "]: 状态上报完成");
        } catch (Exception e) {
            System.err.println("Agent[" + nodeId + "]: 定时上报失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 定时检查任务队列（每5秒检查一次）
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 10000)
    public void checkTaskQueue() {
        try {
            String queueKey = "task:queue:" + nodeId;
            System.out.println("Agent[" + nodeId + "]: 检查任务队列: " + queueKey);
            
            // 从Redis队列中获取任务（阻塞式，超时1秒）
            String taskJson = stringRedisTemplate.opsForList().rightPop(queueKey, 1, TimeUnit.SECONDS);
            
            if (taskJson != null && !taskJson.trim().isEmpty()) {
                System.out.println("Agent[" + nodeId + "]: 收到新任务: " + taskJson);
                
                try {
                    DeployTask task = JSON.parseObject(taskJson, DeployTask.class);
                    System.out.println("Agent[" + nodeId + "]: 解析任务成功:");
                    System.out.println("  - 任务ID: " + task.getTaskId());
                    System.out.println("  - 服务名: " + task.getServiceName());
                    System.out.println("  - 操作: " + task.getAction());
                    System.out.println("  - 文件路径: " + task.getFilePath());
                    System.out.println("  - 操作员: " + task.getOperator());
                    
                    // 执行任务
                    executeDeployTask(task);
                    
                } catch (Exception e) {
                    System.err.println("Agent[" + nodeId + "]: 处理任务失败: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // 没有任务时不打印日志，避免日志过多
                // System.out.println("Agent[" + nodeId + "]: 队列中没有新任务");
            }
            
        } catch (Exception e) {
            System.err.println("Agent[" + nodeId + "]: 检查任务队列失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 执行部署任务
     */
    public void executeDeployTask(DeployTask task) {
        System.out.println("Agent[" + nodeId + "]: === 开始执行部署任务 ===");
        System.out.println("Agent[" + nodeId + "]: 任务详情:");
        System.out.println("  - 任务ID: " + task.getTaskId());
        System.out.println("  - 服务名: " + task.getServiceName());
        System.out.println("  - 操作: " + task.getAction());
        System.out.println("  - 分发方式: " + task.getDistributionMethod());
        System.out.println("  - 文件路径: " + task.getFilePath());
        System.out.println("  - 操作员: " + task.getOperator());
        
        try {
            // 更新任务状态为执行中
            updateTaskStatus(task.getTaskId(), "RUNNING", "开始执行部署任务", null);
            
            // 根据操作类型执行不同的任务
            switch (task.getAction()) {
                case "DEPLOY":
                    System.out.println("Agent[" + nodeId + "]: 执行部署操作...");
                    taskExecutionService.executeDeployTask(task);
                    break;
                case "ROLLBACK":
                    System.out.println("Agent[" + nodeId + "]: 执行回滚操作...");
                    taskExecutionService.executeRollbackTask(task);
                    break;
                case "START":
                    System.out.println("Agent[" + nodeId + "]: 执行启动操作...");
                    taskExecutionService.executeStartTask(task);
                    break;
                case "STOP":
                    System.out.println("Agent[" + nodeId + "]: 执行停止操作...");
                    taskExecutionService.executeStopTask(task);
                    break;
                case "RESTART":
                    System.out.println("Agent[" + nodeId + "]: 执行重启操作...");
                    taskExecutionService.executeRestartTask(task);
                    break;
                default:
                    throw new RuntimeException("不支持的操作类型: " + task.getAction());
            }
            
            // 更新任务状态为成功
            updateTaskStatus(task.getTaskId(), "SUCCESS", "任务执行成功", null);
            System.out.println("Agent[" + nodeId + "]: === 部署任务执行成功 ===");
            
        } catch (Exception e) {
            System.err.println("Agent[" + nodeId + "]: === 部署任务执行失败 ===");
            System.err.println("Agent[" + nodeId + "]: 错误详情: " + e.getMessage());
            e.printStackTrace();
            
            // 更新任务状态为失败
            updateTaskStatus(task.getTaskId(), "FAILED", "任务执行失败", e.getMessage());
        }
    }
    
    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, String status, String message, String errorMessage) {
        try {
            System.out.println("Agent[" + nodeId + "]: 更新任务状态: " + taskId + " -> " + status);
            
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("taskId", taskId);
            statusUpdate.put("nodeId", nodeId);
            statusUpdate.put("status", status);
            statusUpdate.put("message", message);
            statusUpdate.put("errorMessage", errorMessage);
            statusUpdate.put("updateTime", System.currentTimeMillis());
            
            String statusKey = "task:status:" + taskId + ":" + nodeId;
            stringRedisTemplate.opsForValue().set(statusKey, JSON.toJSONString(statusUpdate));
            
            System.out.println("Agent[" + nodeId + "]: 任务状态更新完成");
            
        } catch (Exception e) {
            System.err.println("Agent[" + nodeId + "]: 更新任务状态失败: " + e.getMessage());
            e.printStackTrace();
        }
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
