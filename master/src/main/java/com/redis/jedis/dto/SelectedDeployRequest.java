package com.redis.jedis.dto;

import java.util.List;

/**
 * 选定服务部署请求DTO
 */
public class SelectedDeployRequest {
    
    private List<String> serviceNames; // 要部署的服务名称列表
    private boolean forceDeploy; // 是否强制部署
    private String deployMode; // 部署模式：parallel(并行) 或 sequential(串行)
    private String operator; // 操作人
    
    public SelectedDeployRequest() {
    }
    
    public SelectedDeployRequest(List<String> serviceNames, boolean forceDeploy, String deployMode) {
        this.serviceNames = serviceNames;
        this.forceDeploy = forceDeploy;
        this.deployMode = deployMode;
    }
    
    // Getters and Setters
    public List<String> getServiceNames() {
        return serviceNames;
    }
    
    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
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
