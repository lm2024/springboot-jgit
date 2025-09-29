package com.redis.jedis.dto;

/**
 * 任务结果DTO
 * Agent节点向Master节点返回的任务执行结果
 */
public class TaskResult {
    
    private String taskId; // 任务ID
    private boolean success; // 是否成功
    private String message; // 结果消息
    private long timestamp; // 时间戳
    private String nodeId; // 节点ID
    private String serviceName; // 服务名称
    private String action; // 操作类型
    
    public TaskResult() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public TaskResult(String taskId, boolean success, String message) {
        this.taskId = taskId;
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
}
