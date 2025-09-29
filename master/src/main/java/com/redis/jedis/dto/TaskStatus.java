package com.redis.jedis.dto;

import java.util.List;
import java.util.Map;

/**
 * 任务状态DTO
 * 增强的任务状态，包含详细失败信息
 */
public class TaskStatus {
    
    private String taskId; // 任务ID
    private String serviceName; // 服务名称
    private String status; // 任务状态：PENDING/RUNNING/COMPLETED/FAILED
    private String errorMessage; // 错误信息
    private String errorCode; // 错误代码
    private String errorDetails; // 错误详情
    private long startTime; // 开始时间
    private long endTime; // 结束时间
    private int progress; // 进度百分比
    private List<String> targetNodes; // 目标节点
    private Map<String, String> nodeResults; // 各节点执行结果
    
    public TaskStatus() {
    }
    
    public TaskStatus(String taskId, String serviceName, String status) {
        this.taskId = taskId;
        this.serviceName = serviceName;
        this.status = status;
    }
    
    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    public List<String> getTargetNodes() {
        return targetNodes;
    }
    
    public void setTargetNodes(List<String> targetNodes) {
        this.targetNodes = targetNodes;
    }
    
    public Map<String, String> getNodeResults() {
        return nodeResults;
    }
    
    public void setNodeResults(Map<String, String> nodeResults) {
        this.nodeResults = nodeResults;
    }
}
