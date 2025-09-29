package com.redis.jedis.dto;

/**
 * 回滚请求DTO
 */
public class RollbackRequest {
    
    private String targetVersion; // 目标版本（可选，默认回滚到上一个版本）
    private boolean forceRollback; // 是否强制回滚
    private String rollbackReason; // 回滚原因
    private String operator; // 操作人
    
    public RollbackRequest() {
    }
    
    public RollbackRequest(String targetVersion, boolean forceRollback, String rollbackReason) {
        this.targetVersion = targetVersion;
        this.forceRollback = forceRollback;
        this.rollbackReason = rollbackReason;
    }
    
    // Getters and Setters
    public String getTargetVersion() {
        return targetVersion;
    }
    
    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }
    
    public boolean isForceRollback() {
        return forceRollback;
    }
    
    public void setForceRollback(boolean forceRollback) {
        this.forceRollback = forceRollback;
    }
    
    public String getRollbackReason() {
        return rollbackReason;
    }
    
    public void setRollbackReason(String rollbackReason) {
        this.rollbackReason = rollbackReason;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
}
