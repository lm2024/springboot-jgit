package com.redis.jedis.dto;

import java.util.List;

/**
 * 部署任务DTO
 * Master节点发送给Agent节点的部署任务
 */
public class DeployTask {
    
    private String taskId; // 任务ID
    private String nodeId; // 目标节点ID
    private String serviceName; // 服务名称
    private String action; // 操作类型：START/STOP/RESTART/DEPLOY/ROLLBACK
    private String distributionMethod; // 分发方式：shared-storage, http
    private String filePath; // 文件路径（共享存储路径或HTTP下载URL）
    private long fileSize; // 文件大小
    private String fileChecksum; // 文件校验和
    private List<String> targetNodes; // 目标节点列表
    private long createTime; // 创建时间
    private String operator; // 操作人
    
    public DeployTask() {
        this.createTime = System.currentTimeMillis();
    }
    
    public DeployTask(String taskId, String nodeId, String serviceName, String action) {
        this.taskId = taskId;
        this.nodeId = nodeId;
        this.serviceName = serviceName;
        this.action = action;
        this.createTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
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
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDistributionMethod() {
        return distributionMethod;
    }
    
    public void setDistributionMethod(String distributionMethod) {
        this.distributionMethod = distributionMethod;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileChecksum() {
        return fileChecksum;
    }
    
    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }
    
    public List<String> getTargetNodes() {
        return targetNodes;
    }
    
    public void setTargetNodes(List<String> targetNodes) {
        this.targetNodes = targetNodes;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
}
