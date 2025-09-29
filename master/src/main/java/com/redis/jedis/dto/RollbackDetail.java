package com.redis.jedis.dto;

/**
 * 回滚详情DTO
 */
public class RollbackDetail {
    
    private String serviceName; // 服务名称
    private String taskId; // 任务ID
    private boolean success; // 是否成功
    private String errorMessage; // 错误信息
    private String fromVersion; // 从版本
    private String toVersion; // 到版本
    private long rollbackTime; // 回滚时间
    
    public RollbackDetail() {
        this.rollbackTime = System.currentTimeMillis();
    }
    
    public RollbackDetail(String serviceName, String taskId, boolean success) {
        this.serviceName = serviceName;
        this.taskId = taskId;
        this.success = success;
        this.rollbackTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
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
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getFromVersion() {
        return fromVersion;
    }
    
    public void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion;
    }
    
    public String getToVersion() {
        return toVersion;
    }
    
    public void setToVersion(String toVersion) {
        this.toVersion = toVersion;
    }
    
    public long getRollbackTime() {
        return rollbackTime;
    }
    
    public void setRollbackTime(long rollbackTime) {
        this.rollbackTime = rollbackTime;
    }
}
