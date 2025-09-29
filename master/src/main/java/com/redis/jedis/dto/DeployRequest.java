package com.redis.jedis.dto;

import java.util.List;

/**
 * 部署请求DTO
 */
public class DeployRequest {
    
    private String serviceName; // 服务名称
    private String jarPath; // JAR文件路径
    private List<String> targetNodes; // 目标节点列表
    private boolean forceDeploy; // 是否强制部署
    private String deployMode; // 部署模式：parallel(并行) 或 sequential(串行)
    private String operator; // 操作人
    
    public DeployRequest() {
    }
    
    public DeployRequest(String serviceName, String jarPath, List<String> targetNodes) {
        this.serviceName = serviceName;
        this.jarPath = jarPath;
        this.targetNodes = targetNodes;
    }
    
    // Getters and Setters
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
    
    public boolean isForceDeploy() {
        return forceDeploy;
    }
    
    public void setForceDeploy(boolean forceDeploy) {
        this.forceDeploy = forceDeploy;
    }
    
    public String getDeployMode() {
        return deployMode;
    }
    
    public void setDeployMode(String deployMode) {
        this.deployMode = deployMode;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
}
