package com.redis.jedis.dto;

/**
 * 部署历史DTO
 */
public class DeployHistory {
    
    private String serviceName; // 服务名称
    private String version; // 版本号
    private long deployTime; // 部署时间
    private String status; // 部署状态
    private String operator; // 操作人
    private String nodeId; // 节点ID
    private String jarPath; // JAR文件路径
    private long jarFileSize; // 文件大小
    
    public DeployHistory() {
    }
    
    public DeployHistory(String serviceName, String version, long deployTime) {
        this.serviceName = serviceName;
        this.version = version;
        this.deployTime = deployTime;
    }
    
    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public long getDeployTime() {
        return deployTime;
    }
    
    public void setDeployTime(long deployTime) {
        this.deployTime = deployTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getJarPath() {
        return jarPath;
    }
    
    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }
    
    public long getJarFileSize() {
        return jarFileSize;
    }
    
    public void setJarFileSize(long jarFileSize) {
        this.jarFileSize = jarFileSize;
    }
}

