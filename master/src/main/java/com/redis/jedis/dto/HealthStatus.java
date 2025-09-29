package com.redis.jedis.dto;

/**
 * 健康状态DTO
 */
public class HealthStatus {
    
    private String nodeId; // 节点ID
    private String serviceName; // 服务名称
    private String status; // 健康状态：HEALTHY/UNHEALTHY/UNKNOWN
    private long checkTime; // 检查时间
    private String message; // 状态消息
    private long lastDeployTime; // 最近部署时间
    private String lastDeployVersion; // 最近部署版本
    private long uptime; // 服务运行时间
    
    public HealthStatus() {
        this.checkTime = System.currentTimeMillis();
    }
    
    public HealthStatus(String nodeId, String serviceName, String status) {
        this.nodeId = nodeId;
        this.serviceName = serviceName;
        this.status = status;
        this.checkTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getCheckTime() {
        return checkTime;
    }
    
    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getLastDeployTime() {
        return lastDeployTime;
    }
    
    public void setLastDeployTime(long lastDeployTime) {
        this.lastDeployTime = lastDeployTime;
    }
    
    public String getLastDeployVersion() {
        return lastDeployVersion;
    }
    
    public void setLastDeployVersion(String lastDeployVersion) {
        this.lastDeployVersion = lastDeployVersion;
    }
    
    public long getUptime() {
        return uptime;
    }
    
    public void setUptime(long uptime) {
        this.uptime = uptime;
    }
}
