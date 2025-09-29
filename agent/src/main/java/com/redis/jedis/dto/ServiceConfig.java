package com.redis.jedis.dto;

/**
 * 服务配置DTO
 * Agent节点管理的服务配置信息
 */
public class ServiceConfig {
    
    private String name; // 服务名称
    private String jarName; // JAR文件名
    private String jarPath; // JAR文件路径
    private String startCommand; // 启动命令
    private String stopCommand; // 停止命令
    private String healthCheckUrl; // 健康检查URL
    private int startupTimeout; // 启动超时时间（秒）
    private int shutdownTimeout; // 停止超时时间（秒）
    private boolean autoStart; // 是否自动启动
    private int port; // 服务端口
    
    public ServiceConfig() {
    }
    
    public ServiceConfig(String name, String jarName, String jarPath) {
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
    
    public int getStartupTimeout() {
        return startupTimeout;
    }
    
    public void setStartupTimeout(int startupTimeout) {
        this.startupTimeout = startupTimeout;
    }
    
    public int getShutdownTimeout() {
        return shutdownTimeout;
    }
    
    public void setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }
    
    public boolean isAutoStart() {
        return autoStart;
    }
    
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
}
