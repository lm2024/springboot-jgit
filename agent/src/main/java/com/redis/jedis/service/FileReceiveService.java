package com.redis.jedis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 文件接收服务
 * 负责接收Master节点分发的文件
 */
@Service
public class FileReceiveService {
    
    @Value("${node.id}")
    private String nodeId;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    /**
     * 从共享存储复制文件
     */
    public String copyFromSharedStorage(String sharedPath) {
        System.out.println("FileReceive[" + nodeId + "]: 从共享存储复制文件: " + sharedPath);
        
        try {
            // TODO: 实现从共享存储复制文件逻辑
            // 这里先返回空字符串，后续实现
            System.out.println("FileReceive[" + nodeId + "]: 共享存储复制功能暂未实现");
            return "";
        } catch (Exception e) {
            System.err.println("FileReceive[" + nodeId + "]: 从共享存储复制文件失败: " + e.getMessage());
            throw new RuntimeException("从共享存储复制文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 从HTTP下载文件
     */
    public String downloadFromHttp(String downloadUrl) {
        System.out.println("FileReceive[" + nodeId + "]: 从HTTP下载文件: " + downloadUrl);
        
        try {
            // TODO: 实现从HTTP下载文件逻辑
            // 这里先返回空字符串，后续实现
            System.out.println("FileReceive[" + nodeId + "]: HTTP下载功能暂未实现");
            return "";
        } catch (Exception e) {
            System.err.println("FileReceive[" + nodeId + "]: 从HTTP下载文件失败: " + e.getMessage());
            throw new RuntimeException("从HTTP下载文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件（通用方法）
     */
    public void downloadFile(String sourceUrl, String targetPath) throws Exception {
        System.out.println("FileReceive[" + nodeId + "]: 下载文件: " + sourceUrl + " -> " + targetPath);
        
        if (sourceUrl.startsWith("http://") || sourceUrl.startsWith("https://")) {
            downloadFromHttpUrl(sourceUrl, targetPath);
        } else {
            // 假设是本地文件路径，直接复制
            Files.copy(Paths.get(sourceUrl), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("FileReceive[" + nodeId + "]: 本地文件复制完成");
        }
    }
    
    /**
     * 从HTTP URL下载文件
     */
    private void downloadFromHttpUrl(String url, String targetPath) throws Exception {
        System.out.println("FileReceive[" + nodeId + "]: 开始HTTP下载: " + url);
        
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(targetPath)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            System.out.println("FileReceive[" + nodeId + "]: HTTP下载完成，总大小: " + totalBytes + " bytes");
        }
    }
    
    /**
     * 从Redis接收文件
     */
    public void receiveFromRedis(String redisKey, String targetPath) throws Exception {
        System.out.println("FileReceive[" + nodeId + "]: 从Redis接收文件: " + redisKey + " -> " + targetPath);
        
        try {
            // 从Redis获取文件数据（Base64编码）
            String fileData = stringRedisTemplate.opsForValue().get(redisKey);
            if (fileData == null) {
                throw new RuntimeException("Redis中没有找到文件数据: " + redisKey);
            }
            
            // 解码并写入文件
            byte[] decodedData = Base64.getDecoder().decode(fileData);
            Files.write(Paths.get(targetPath), decodedData);
            
            System.out.println("FileReceive[" + nodeId + "]: 从Redis接收文件完成，大小: " + decodedData.length + " bytes");
            
            // 清理Redis中的临时数据
            stringRedisTemplate.delete(redisKey);
            
        } catch (Exception e) {
            System.err.println("FileReceive[" + nodeId + "]: 从Redis接收文件失败: " + e.getMessage());
            throw new RuntimeException("从Redis接收文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证文件完整性
     */
    public boolean validateFile(String filePath) {
        System.out.println("FileReceive[" + nodeId + "]: 验证文件完整性: " + filePath);
        
        try {
            // 检查文件是否存在
            if (!Files.exists(Paths.get(filePath))) {
                System.err.println("FileReceive[" + nodeId + "]: 文件不存在: " + filePath);
                return false;
            }
            
            // 检查文件大小
            long fileSize = Files.size(Paths.get(filePath));
            if (fileSize == 0) {
                System.err.println("FileReceive[" + nodeId + "]: 文件大小为0: " + filePath);
                return false;
            }
            
            System.out.println("FileReceive[" + nodeId + "]: 文件验证通过，大小: " + fileSize + " bytes");
            return true;
            
        } catch (Exception e) {
            System.err.println("FileReceive[" + nodeId + "]: 文件验证失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取文件校验和
     */
    public String getFileChecksum(String filePath) {
        System.out.println("FileReceive[" + nodeId + "]: 计算文件校验和: " + filePath);
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (FileInputStream fis = new FileInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    md.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            
            String checksum = sb.toString();
            System.out.println("FileReceive[" + nodeId + "]: 文件校验和: " + checksum);
            return checksum;
            
        } catch (Exception e) {
            System.err.println("FileReceive[" + nodeId + "]: 计算文件校验和失败: " + e.getMessage());
            return "";
        }
    }
}
