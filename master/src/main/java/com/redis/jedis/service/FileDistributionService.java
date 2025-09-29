package com.redis.jedis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 文件分发服务
 * 负责文件分发策略选择和执行
 */
@Service
public class FileDistributionService {
    
    @Value("${deploy.jar-shared-dir}")
    private String sharedDir;
    
    @Value("${deploy.jar-deploy-dir}")
    private String deployDir;
    
    @Value("${deploy.file-transfer.max-file-size:524288000}")
    private long maxFileSize; // 500MB
    
    @Value("${deploy.file-transfer.use-shared-dir:true}")
    private boolean useSharedDir;
    
    /**
     * 选择文件分发方式
     * 根据文件大小选择合适的分发方式
     */
    public String selectDistributionMethod(long fileSize) {
        if (fileSize < 52428800) { // 50MB以下
            return "redis";
        } else if (fileSize < maxFileSize && useSharedDir) { // 50MB-500MB，使用共享存储
            return "shared-storage";
        } else { // 500MB以上，使用HTTP
            return "http";
        }
    }
    
    /**
     * 分发文件到目标节点
     */
    public String distributeFile(String filePath, List<String> targetNodes, String method) {
        try {
            switch (method) {
                case "shared-storage":
                    return distributeToSharedStorage(filePath, targetNodes);
                case "http":
                    return distributeViaHttp(filePath, targetNodes);
                case "redis":
                    return distributeViaRedis(filePath, targetNodes);
                default:
                    throw new IllegalArgumentException("不支持的分发方式: " + method);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件分发失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过共享存储分发文件
     */
    private String distributeToSharedStorage(String filePath, List<String> targetNodes) {
        try {
            File sourceFile = new File(filePath);
            if (!sourceFile.exists()) {
                throw new RuntimeException("源文件不存在: " + filePath);
            }
            
            // 确保共享目录存在
            Path sharedPath = Paths.get(sharedDir);
            if (!Files.exists(sharedPath)) {
                Files.createDirectories(sharedPath);
            }
            
            // 复制文件到共享存储
            String fileName = sourceFile.getName();
            Path targetPath = sharedPath.resolve(fileName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 通过Redis通知Agent节点文件已准备就绪
            notifyAgentsFileReady(targetPath.toString(), targetNodes);
            
            return targetPath.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("共享存储分发失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过HTTP分发文件
     */
    private String distributeViaHttp(String filePath, List<String> targetNodes) {
        try {
            File sourceFile = new File(filePath);
            if (!sourceFile.exists()) {
                throw new RuntimeException("源文件不存在: " + filePath);
            }
            
            // 确保部署目录存在
            Path deployPath = Paths.get(deployDir);
            if (!Files.exists(deployPath)) {
                Files.createDirectories(deployPath);
            }
            
            // 复制文件到部署目录
            String fileName = sourceFile.getName();
            Path targetPath = deployPath.resolve(fileName);
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 生成HTTP下载URL
            String downloadUrl = generateDownloadUrl(fileName);
            
            // 通过Redis通知Agent节点下载URL
            notifyAgentsDownloadUrl(downloadUrl, targetNodes);
            
            return downloadUrl;
            
        } catch (IOException e) {
            throw new RuntimeException("HTTP分发失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过Redis分发文件（小文件）
     */
    private String distributeViaRedis(String filePath, List<String> targetNodes) {
        try {
            File sourceFile = new File(filePath);
            if (!sourceFile.exists()) {
                throw new RuntimeException("源文件不存在: " + filePath);
            }
            
            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(sourceFile.toPath());
            
            // 通过Redis发送文件内容到Agent节点
            sendFileToAgents(fileContent, sourceFile.getName(), targetNodes);
            
            return "redis://" + sourceFile.getName();
            
        } catch (IOException e) {
            throw new RuntimeException("Redis分发失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存上传的文件
     */
    public String saveUploadedFile(MultipartFile file) {
        try {
            // 确保部署目录存在
            Path deployPath = Paths.get(deployDir);
            if (!Files.exists(deployPath)) {
                Files.createDirectories(deployPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileName = generateUniqueFileName(originalFilename);
            Path targetPath = deployPath.resolve(fileName);
            
            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return targetPath.toString();
            
        } catch (IOException e) {
            throw new RuntimeException("保存上传文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 通知Agent节点文件已准备就绪
     */
    private void notifyAgentsFileReady(String filePath, List<String> targetNodes) {
        // TODO: 通过Redis通知Agent节点
        // 这里先空实现，后续实现
    }
    
    /**
     * 通知Agent节点下载URL
     */
    private void notifyAgentsDownloadUrl(String downloadUrl, List<String> targetNodes) {
        // TODO: 通过Redis通知Agent节点
        // 这里先空实现，后续实现
    }
    
    /**
     * 发送文件到Agent节点
     */
    private void sendFileToAgents(byte[] fileContent, String fileName, List<String> targetNodes) {
        // TODO: 通过Redis发送文件内容到Agent节点
        // 这里先空实现，后续实现
    }
    
    /**
     * 生成下载URL
     */
    private String generateDownloadUrl(String fileName) {
        // TODO: 生成HTTP下载URL
        // 这里先返回空字符串，后续实现
        return "http://localhost:8080/download/" + fileName;
    }
    
    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        long timestamp = System.currentTimeMillis();
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        return originalFilename.substring(0, lastDotIndex > 0 ? lastDotIndex : originalFilename.length()) 
               + "_" + timestamp + extension;
    }
}
