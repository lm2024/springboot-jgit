package com.redis.jedis.dto;

/**
 * 批量部署请求DTO
 */
public class BatchDeployRequest {
    
    private boolean forceDeploy; // 是否强制部署
    private String deployMode; // 部署模式：parallel(并行) 或 sequential(串行)
    private String operator; // 操作人
    
    public BatchDeployRequest() {
    }
    
    public BatchDeployRequest(boolean forceDeploy, String deployMode) {
        this.forceDeploy = forceDeploy;
        this.deployMode = deployMode;
    }
    
    // Getters and Setters
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
