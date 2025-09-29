package com.redis.jedis.dto;

import java.util.List;

/**
 * 选定服务回滚请求DTO
 */
public class SelectedRollbackRequest {
    
    private List<String> serviceNames; // 要回滚的服务名称列表
    private String targetVersion; // 目标版本
    private boolean forceRollback; // 是否强制回滚
    private String rollbackReason; // 回滚原因
    private String operator; // 操作人
    
    public SelectedRollbackRequest() {
    }
    
    public SelectedRollbackRequest(List<String> serviceNames, String targetVersion, boolean forceRollback, String rollbackReason) {
        this.serviceNames = serviceNames;
        this.targetVersion = targetVersion;
        this.forceRollback = forceRollback;
        this.rollbackReason = rollbackReason;
    }
    
    // Getters and Setters
    public List<String> getServiceNames() {
        return serviceNames;
    }
    
    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }
    
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
