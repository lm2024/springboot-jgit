package com.redis.jedis.service;

import com.alibaba.fastjson.JSON;
import com.redis.jedis.dto.DeployTask;
import com.redis.jedis.dto.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.redis.jedis.util.RedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 任务管理服务
 * 负责任务的生命周期管理
 */
@Service
public class TaskManagementService {
    
    @Autowired
    private RedisClient jedisCluster;
    
    private static final String TASK_PREFIX = "task:deploy:";
    private static final String TASK_STATUS_PREFIX = "task:status:";
    private static final int TASK_TTL = 3600; // 1小时
    
    /**
     * 存储任务
     */
    public void storeTask(DeployTask task) {
        try {
            String taskKey = TASK_PREFIX + task.getTaskId();
            String taskJson = JSON.toJSONString(task);
            jedisCluster.setex(taskKey, TASK_TTL, taskJson);
            
            // 创建初始任务状态
            TaskStatus status = new TaskStatus();
            status.setTaskId(task.getTaskId());
            status.setServiceName(task.getServiceName());
            status.setStatus("PENDING");
            status.setStartTime(System.currentTimeMillis());
            status.setProgress(0);
            status.setTargetNodes(task.getTargetNodes());
            
            storeTaskStatus(status);
            
        } catch (Exception e) {
            throw new RuntimeException("存储任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务
     */
    public DeployTask getTask(String taskId) {
        try {
            String taskKey = TASK_PREFIX + taskId;
            String taskJson = jedisCluster.get(taskKey);
            if (taskJson != null) {
                return JSON.parseObject(taskJson, DeployTask.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 存储任务状态
     */
    public void storeTaskStatus(TaskStatus status) {
        try {
            String statusKey = TASK_STATUS_PREFIX + status.getTaskId();
            String statusJson = JSON.toJSONString(status);
            jedisCluster.setex(statusKey, TASK_TTL, statusJson);
        } catch (Exception e) {
            throw new RuntimeException("存储任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务状态
     */
    public TaskStatus getTaskStatus(String taskId) {
        try {
            String statusKey = TASK_STATUS_PREFIX + taskId;
            String statusJson = jedisCluster.get(statusKey);
            if (statusJson != null) {
                return JSON.parseObject(statusJson, TaskStatus.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("获取任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新任务状态
     */
    public void updateTaskStatus(String taskId, String status, int progress) {
        try {
            TaskStatus taskStatus = getTaskStatus(taskId);
            if (taskStatus != null) {
                taskStatus.setStatus(status);
                taskStatus.setProgress(progress);
                if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                    taskStatus.setEndTime(System.currentTimeMillis());
                }
                storeTaskStatus(taskStatus);
            }
        } catch (Exception e) {
            throw new RuntimeException("更新任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新任务错误信息
     */
    public void updateTaskError(String taskId, String errorMessage, String errorCode) {
        try {
            TaskStatus taskStatus = getTaskStatus(taskId);
            if (taskStatus != null) {
                taskStatus.setStatus("FAILED");
                taskStatus.setErrorMessage(errorMessage);
                taskStatus.setErrorCode(errorCode);
                taskStatus.setEndTime(System.currentTimeMillis());
                storeTaskStatus(taskStatus);
            }
        } catch (Exception e) {
            throw new RuntimeException("更新任务错误信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有任务
     */
    public List<DeployTask> getAllTasks() {
        try {
            List<DeployTask> tasks = new ArrayList<>();
            Set<String> keys = jedisCluster.keys(TASK_PREFIX + "*");
            
            for (String key : keys) {
                String taskJson = jedisCluster.get(key);
                if (taskJson != null) {
                    DeployTask task = JSON.parseObject(taskJson, DeployTask.class);
                    tasks.add(task);
                }
            }
            
            return tasks;
        } catch (Exception e) {
            throw new RuntimeException("获取所有任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有任务状态
     */
    public List<TaskStatus> getAllTaskStatus() {
        try {
            List<TaskStatus> statusList = new ArrayList<>();
            Set<String> keys = jedisCluster.keys(TASK_STATUS_PREFIX + "*");
            
            for (String key : keys) {
                String statusJson = jedisCluster.get(key);
                if (statusJson != null) {
                    TaskStatus status = JSON.parseObject(statusJson, TaskStatus.class);
                    statusList.add(status);
                }
            }
            
            return statusList;
        } catch (Exception e) {
            throw new RuntimeException("获取所有任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除任务
     */
    public void deleteTask(String taskId) {
        try {
            String taskKey = TASK_PREFIX + taskId;
            String statusKey = TASK_STATUS_PREFIX + taskId;
            jedisCluster.del(taskKey);
            jedisCluster.del(statusKey);
        } catch (Exception e) {
            throw new RuntimeException("删除任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期任务
     */
    public void cleanupExpiredTasks() {
        try {
            // Redis会自动清理过期的key，这里可以添加额外的清理逻辑
            System.out.println("清理过期任务完成");
        } catch (Exception e) {
            System.err.println("清理过期任务失败: " + e.getMessage());
        }
    }
}
