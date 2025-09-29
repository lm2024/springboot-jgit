package com.redis.jedis.dto;

/**
 * 服务状态DTO
 * Agent节点管理的服务状态信息
 */
public class ServiceStatus {
    
    private String serviceName; // 服务名称
    private boolean running; // 是否运行
    private String pid; // 进程ID
    private long uptime; // 运行时间
    private double cpuUsage; // CPU使用率
    private double memoryUsage; // 内存使用率
    private String status; // 状态描述
    private long lastDeployTime; // 最近部署时间
    private String lastDeployVersion; // 最近部署版本
    
    public ServiceStatus() {
    }
    
    public ServiceStatus(String serviceName, boolean running) {
        this.serviceName = serviceName;
        this.running = running;
    }
    
    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void setRunning(boolean running) {
        this.running = running;
    }
    
    public String getPid() {
        return pid;
    }
    
    public void setPid(String pid) {
        this.pid = pid;
    }
    
    public long getUptime() {
        return uptime;
    }
    
    public void setUptime(long uptime) {
        this.uptime = uptime;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
}
