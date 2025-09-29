package com.redis.jedis.dto;

import java.util.List;

/**
 * 服务信息DTO
 */
public class ServiceInfo {
    
    private String name; // 服务名称
    private String jarName; // JAR文件名
    private String jarPath; // JAR文件路径
    private List<String> targetNodes; // 目标节点列表
    private String description; // 服务描述
    private String startCommand; // 启动命令
    private String stopCommand; // 停止命令
    private String healthCheckUrl; // 健康检查URL
    private int port; // 服务端口
    
    public ServiceInfo() {
    }
    
    public ServiceInfo(String name, String jarName, String jarPath) {
        this.name = name;
        this.jarName = jarName;
        this.jarPath = jarPath;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getJarName() {
        return jarName;
    }
    
    public void setJarName(String jarName) {
        this.jarName = jarName;
    }
    
    public String getJarPath() {
        return jarPath;
    }
    
    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }
    
    public List<String> getTargetNodes() {
        return targetNodes;
    }
    
    public void setTargetNodes(List<String> targetNodes) {
        this.targetNodes = targetNodes;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStartCommand() {
        return startCommand;
    }
    
    public void setStartCommand(String startCommand) {
        this.startCommand = startCommand;
    }
    
    public String getStopCommand() {
        return stopCommand;
    }
    
    public void setStopCommand(String stopCommand) {
        this.stopCommand = stopCommand;
    }
    
    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }
    
    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
}
