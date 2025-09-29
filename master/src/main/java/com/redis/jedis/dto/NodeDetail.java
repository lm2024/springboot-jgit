package com.redis.jedis.dto;

import java.util.List;
import java.util.Map;

/**
 * 节点详情DTO
 */
public class NodeDetail {
    
    private String nodeId; // 节点ID
    private String nodeName; // 节点名称
    private String serviceStatus; // 服务状态
    private double cpuUsage; // CPU使用率
    private double memoryUsage; // 内存使用率
    private String healthStatus; // 健康状态
    private long lastHeartbeat; // 最后心跳时间
    private long lastDeployTime; // 最近部署时间
    private String lastDeployVersion; // 最近部署版本
    private long uptime; // 服务运行时间
    private String jarPath; // 当前JAR文件路径
    private long jarFileSize; // JAR文件大小
    private long jarFileTime; // JAR文件修改时间
    private List<ServiceInfo> services; // 节点上的服务列表
    private Map<String, Object> systemInfo; // 系统信息
    
    public NodeDetail() {
    }
    
    public NodeDetail(String nodeId, String nodeName) {
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
    
    public List<ServiceInfo> getServices() {
        return services;
    }
    
    public void setServices(List<ServiceInfo> services) {
        this.services = services;
    }
    
    public Map<String, Object> getSystemInfo() {
        return systemInfo;
    }
    
    public void setSystemInfo(Map<String, Object> systemInfo) {
        this.systemInfo = systemInfo;
    }
}
