package com.redis.jedis.dto;

import java.util.List;

/**
 * 批量部署结果DTO
 */
public class BatchDeployResult {
    
    private int totalServices; // 总服务数
    private List<String> taskIds; // 任务ID列表
    private String status; // 部署状态
    private long startTime; // 开始时间
    private String operator; // 操作人
    
    public BatchDeployResult() {
        this.startTime = System.currentTimeMillis();
    }
    
    public BatchDeployResult(int totalServices, List<String> taskIds, String status) {
        this.totalServices = totalServices;
        this.taskIds = taskIds;
        this.status = status;
        this.startTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public int getTotalServices() {
        return totalServices;
    }
    
    public void setTotalServices(int totalServices) {
        this.totalServices = totalServices;
    }
    
    public List<String> getTaskIds() {
        return taskIds;
    }
    
    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
}
