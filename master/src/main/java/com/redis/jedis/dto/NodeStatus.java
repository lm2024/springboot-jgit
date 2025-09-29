package com.redis.jedis.dto;

/**
 * 节点状态DTO
 * Master节点展示的节点状态信息
 */
public class NodeStatus {
    
    private String nodeId; // 节点ID
    private String nodeName; // 节点名称
    private String serviceStatus; // 服务状态
    private double cpuUsage; // CPU使用率
    private double memoryUsage; // 内存使用率
    private String healthStatus; // 健康状态
    private long lastHeartbeat; // 最后心跳时间
    private long lastDeployTime; // 最近部署时间
    private String lastDeployVersion; // 最近部署版本
    private long uptime; // 服务运行时间（毫秒）
    private String jarPath; // 当前JAR文件路径
    private long jarFileSize; // JAR文件大小
    private long jarFileTime; // JAR文件修改时间
    
    public NodeStatus() {
    }
    
    public NodeStatus(String nodeId, String nodeName) {
        this.nodeId = nodeId;
        this.nodeName = nodeName;
    }
    
    // Getters and Setters
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public String getServiceStatus() {
        return serviceStatus;
    }
    
    public void setServiceStatus(String serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
    
    public double getCpuUsage() {
        return cpuUsage;
    }
    
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    
    public double getMemoryUsage() {
        return memoryUsage;
    }
    
    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    
    public String getHealthStatus() {
        return healthStatus;
    }
    
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
    
    public long getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
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
    
    public long getJarFileTime() {
        return jarFileTime;
    }
    
    public void setJarFileTime(long jarFileTime) {
        this.jarFileTime = jarFileTime;
    }
}
