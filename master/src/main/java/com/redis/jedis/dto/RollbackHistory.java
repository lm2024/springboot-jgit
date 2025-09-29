package com.redis.jedis.dto;

/**
 * 回滚历史DTO
 */
public class RollbackHistory {
    
    private String serviceName; // 服务名称
    private String fromVersion; // 从版本
    private String toVersion; // 到版本
    private String reason; // 回滚原因
    private long rollbackTime; // 回滚时间
    private String operator; // 操作人
    private String status; // 回滚状态
    
    public RollbackHistory() {
    }
    
    public RollbackHistory(String serviceName, String fromVersion, String toVersion, String reason, long rollbackTime) {
        this.serviceName = serviceName;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.reason = reason;
        this.rollbackTime = rollbackTime;
    }
    
    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public long getRollbackTime() {
        return rollbackTime;
    }
    
    public void setRollbackTime(long rollbackTime) {
        this.rollbackTime = rollbackTime;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
