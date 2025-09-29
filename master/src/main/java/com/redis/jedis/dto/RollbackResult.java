package com.redis.jedis.dto;

import java.util.List;

/**
 * 回滚结果DTO
 */
public class RollbackResult {
    
    private int totalServices; // 总服务数
    private int successCount; // 成功回滚数
    private int failedCount; // 失败回滚数
    private List<RollbackDetail> rollbackDetails; // 回滚详情
    private String status; // 回滚状态
    private long startTime; // 开始时间
    private String operator; // 操作人
    
    public RollbackResult() {
        this.startTime = System.currentTimeMillis();
    }
    
    public RollbackResult(int totalServices, int successCount, int failedCount, List<RollbackDetail> rollbackDetails, String status) {
        this.totalServices = totalServices;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.rollbackDetails = rollbackDetails;
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
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }
    
    public int getFailedCount() {
        return failedCount;
    }
    
    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }
    
    public List<RollbackDetail> getRollbackDetails() {
        return rollbackDetails;
    }
    
    public void setRollbackDetails(List<RollbackDetail> rollbackDetails) {
        this.rollbackDetails = rollbackDetails;
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
