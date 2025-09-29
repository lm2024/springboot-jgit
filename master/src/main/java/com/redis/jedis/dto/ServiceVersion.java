package com.redis.jedis.dto;

/**
 * 服务版本信息DTO
 */
public class ServiceVersion {
    
    private String version; // 版本号
    private String jarPath; // JAR文件路径
    private long fileSize; // 文件大小
    private String checksum; // 文件校验和
    private long createTime; // 创建时间
    private boolean isCurrent; // 是否为当前版本
    private String description; // 版本描述
    
    public ServiceVersion() {
    }
    
    public ServiceVersion(String version, String jarPath) {
        this.version = version;
        this.jarPath = jarPath;
        this.createTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getJarPath() {
        return jarPath;
    }
    
    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public boolean isCurrent() {
        return isCurrent;
    }
    
    public void setCurrent(boolean current) {
        isCurrent = current;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
