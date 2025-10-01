package com.redis.jedis.service;

import com.redis.jedis.dto.DeployTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;

/**
 * 任务执行服务
 * 负责执行Master节点发送的任务
 */
@Service
public class TaskExecutionService {
    
    @Value("${node.id}")
    private String nodeId;
    
    @Value("${agent.work-dir:/tmp/agent}")
    private String workDir;
    
    @Autowired
    private ServiceManagementService serviceManagementService;
    
    @Autowired
    private FileReceiveService fileReceiveService;
    
    /**
     * 执行部署任务
     */
    public void executeDeployTask(DeployTask task) {
        System.out.println("TaskExecution[" + nodeId + "]: === 开始执行部署任务 ===");
        System.out.println("TaskExecution[" + nodeId + "]: 服务名: " + task.getServiceName());
        System.out.println("TaskExecution[" + nodeId + "]: 文件路径: " + task.getFilePath());
        System.out.println("TaskExecution[" + nodeId + "]: 分发方式: " + task.getDistributionMethod());
        
        try {
            // 步骤1: 接收文件
            System.out.println("TaskExecution[" + nodeId + "]: 步骤1 - 接收JAR文件...");
            String localJarPath = receiveFile(task);
            System.out.println("TaskExecution[" + nodeId + "]: JAR文件接收完成: " + localJarPath);
            
            // 步骤2: 停止旧服务（如果存在）
            System.out.println("TaskExecution[" + nodeId + "]: 步骤2 - 停止旧服务...");
            stopServiceIfRunning(task.getServiceName());
            
            // 步骤3: 备份旧版本（如果存在）
            System.out.println("TaskExecution[" + nodeId + "]: 步骤3 - 备份旧版本...");
            backupOldVersion(task.getServiceName());
            
            // 步骤4: 部署新版本
            System.out.println("TaskExecution[" + nodeId + "]: 步骤4 - 部署新版本...");
            deployNewVersion(task.getServiceName(), localJarPath);
            
            // 步骤5: 启动新服务
            System.out.println("TaskExecution[" + nodeId + "]: 步骤5 - 启动新服务...");
            startService(task.getServiceName());
            
            // 步骤6: 健康检查
            System.out.println("TaskExecution[" + nodeId + "]: 步骤6 - 健康检查...");
            if (healthCheck(task.getServiceName())) {
                System.out.println("TaskExecution[" + nodeId + "]: === 部署任务执行成功 ===");
            } else {
                throw new RuntimeException("健康检查失败，服务可能未正常启动");
            }
            
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: === 部署任务执行失败 ===");
            System.err.println("TaskExecution[" + nodeId + "]: 错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("部署任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 接收文件
     */
    private String receiveFile(DeployTask task) throws Exception {
        System.out.println("TaskExecution[" + nodeId + "]: 开始接收文件...");
        System.out.println("TaskExecution[" + nodeId + "]: 源文件路径: " + task.getFilePath());
        System.out.println("TaskExecution[" + nodeId + "]: 分发方式: " + task.getDistributionMethod());
        
        // 确保工作目录存在
        Path workDirPath = Paths.get(workDir);
        Files.createDirectories(workDirPath);
        System.out.println("TaskExecution[" + nodeId + "]: 工作目录: " + workDirPath.toAbsolutePath());
        
        // 生成本地文件路径
        String fileName = Paths.get(task.getFilePath()).getFileName().toString();
        Path localFilePath = workDirPath.resolve(fileName);
        System.out.println("TaskExecution[" + nodeId + "]: 本地文件路径: " + localFilePath.toAbsolutePath());
        
        // 根据分发方式接收文件
        String distributionMethod = task.getDistributionMethod().toUpperCase();
        switch (distributionMethod) {
            case "DIRECT_COPY":
                System.out.println("TaskExecution[" + nodeId + "]: 使用直接复制方式...");
                Files.copy(Paths.get(task.getFilePath()), localFilePath, StandardCopyOption.REPLACE_EXISTING);
                break;
            case "HTTP_DOWNLOAD":
            case "HTTP":
                System.out.println("TaskExecution[" + nodeId + "]: 使用HTTP下载方式...");
                fileReceiveService.downloadFile(task.getFilePath(), localFilePath.toString());
                break;
            case "REDIS_TRANSFER":
            case "REDIS":
                System.out.println("TaskExecution[" + nodeId + "]: 使用Redis传输方式...");
                // 处理Redis路径格式：redis://filename -> filename
                String redisKey = task.getFilePath();
                if (redisKey.startsWith("redis://")) {
                    redisKey = redisKey.substring(8); // 移除 "redis://" 前缀
                }
                System.out.println("TaskExecution[" + nodeId + "]: Redis键名: " + redisKey);
                fileReceiveService.receiveFromRedis(redisKey, localFilePath.toString());
                break;
            default:
                throw new RuntimeException("不支持的分发方式: " + task.getDistributionMethod() + " (标准化后: " + distributionMethod + ")");
        }
        
        // 验证文件是否接收成功
        if (!Files.exists(localFilePath)) {
            throw new RuntimeException("文件接收失败，本地文件不存在: " + localFilePath);
        }
        
        long fileSize = Files.size(localFilePath);
        System.out.println("TaskExecution[" + nodeId + "]: 文件接收成功，大小: " + fileSize + " bytes");
        
        return localFilePath.toString();
    }
    
    /**
     * 停止服务（如果正在运行）
     */
    private void stopServiceIfRunning(String serviceName) {
        try {
            System.out.println("TaskExecution[" + nodeId + "]: 检查服务状态: " + serviceName);
            if (serviceManagementService.isServiceRunning(serviceName)) {
                System.out.println("TaskExecution[" + nodeId + "]: 服务正在运行，开始停止...");
                serviceManagementService.stopService(serviceName);
                
                // 等待服务停止
                int maxWait = 30; // 最多等待30秒
                for (int i = 0; i < maxWait; i++) {
                    if (!serviceManagementService.isServiceRunning(serviceName)) {
                        System.out.println("TaskExecution[" + nodeId + "]: 服务已停止");
                        return;
                    }
                    Thread.sleep(1000);
                    System.out.println("TaskExecution[" + nodeId + "]: 等待服务停止... (" + (i+1) + "/" + maxWait + ")");
                }
                
                System.err.println("TaskExecution[" + nodeId + "]: 警告: 服务停止超时，强制继续部署");
            } else {
                System.out.println("TaskExecution[" + nodeId + "]: 服务未运行，无需停止");
            }
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: 停止服务失败: " + e.getMessage());
            // 不抛出异常，继续部署流程
        }
    }
    
    /**
     * 备份旧版本
     */
    private void backupOldVersion(String serviceName) {
        try {
            System.out.println("TaskExecution[" + nodeId + "]: 开始备份旧版本...");
            
            Path servicePath = Paths.get(workDir, serviceName);
            if (Files.exists(servicePath)) {
                String backupName = serviceName + "_backup_" + System.currentTimeMillis();
                Path backupPath = Paths.get(workDir, backupName);
                
                System.out.println("TaskExecution[" + nodeId + "]: 备份路径: " + backupPath.toAbsolutePath());
                Files.move(servicePath, backupPath);
                System.out.println("TaskExecution[" + nodeId + "]: 旧版本备份完成");
            } else {
                System.out.println("TaskExecution[" + nodeId + "]: 没有旧版本需要备份");
            }
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: 备份旧版本失败: " + e.getMessage());
            // 不抛出异常，继续部署流程
        }
    }
    
    /**
     * 部署新版本
     */
    private void deployNewVersion(String serviceName, String jarPath) throws Exception {
        System.out.println("TaskExecution[" + nodeId + "]: 开始部署新版本...");
        
        // 创建服务目录
        Path servicePath = Paths.get(workDir, serviceName);
        Files.createDirectories(servicePath);
        System.out.println("TaskExecution[" + nodeId + "]: 服务目录: " + servicePath.toAbsolutePath());
        
        // 复制JAR文件到服务目录
        Path targetJarPath = servicePath.resolve(serviceName + ".jar");
        Files.copy(Paths.get(jarPath), targetJarPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("TaskExecution[" + nodeId + "]: JAR文件部署完成: " + targetJarPath.toAbsolutePath());
        
        // 创建启动脚本
        createStartScript(serviceName, servicePath, targetJarPath);
        
        System.out.println("TaskExecution[" + nodeId + "]: 新版本部署完成");
    }
    
    /**
     * 创建启动脚本
     */
    private void createStartScript(String serviceName, Path servicePath, Path jarPath) throws Exception {
        System.out.println("TaskExecution[" + nodeId + "]: 创建启动脚本...");
        
        Path startScriptPath = servicePath.resolve("start.sh");
        String scriptContent = "#!/bin/bash\n" +
                "cd " + servicePath.toAbsolutePath() + "\n" +
                "java -jar " + jarPath.getFileName() + " > " + serviceName + ".log 2>&1 &\n" +
                "echo $! > " + serviceName + ".pid\n";
        
        Files.write(startScriptPath, scriptContent.getBytes());
        
        // 设置执行权限
        startScriptPath.toFile().setExecutable(true);
        
        System.out.println("TaskExecution[" + nodeId + "]: 启动脚本创建完成: " + startScriptPath.toAbsolutePath());
    }
    
    /**
     * 启动服务
     */
    private void startService(String serviceName) throws Exception {
        System.out.println("TaskExecution[" + nodeId + "]: 开始启动服务: " + serviceName);
        serviceManagementService.startService(serviceName);
        
        // 等待服务启动
        int maxWait = 60; // 最多等待60秒
        for (int i = 0; i < maxWait; i++) {
            if (serviceManagementService.isServiceRunning(serviceName)) {
                System.out.println("TaskExecution[" + nodeId + "]: 服务启动成功");
                return;
            }
            Thread.sleep(1000);
            System.out.println("TaskExecution[" + nodeId + "]: 等待服务启动... (" + (i+1) + "/" + maxWait + ")");
        }
        
        throw new RuntimeException("服务启动超时");
    }
    
    /**
     * 健康检查
     */
    private boolean healthCheck(String serviceName) {
        try {
            System.out.println("TaskExecution[" + nodeId + "]: 开始健康检查: " + serviceName);
            
            // 检查进程是否存在
            if (!serviceManagementService.isServiceRunning(serviceName)) {
                System.err.println("TaskExecution[" + nodeId + "]: 健康检查失败: 服务进程不存在");
                return false;
            }
            
            // TODO: 这里可以添加更多健康检查逻辑，比如HTTP健康检查等
            
            System.out.println("TaskExecution[" + nodeId + "]: 健康检查通过");
            return true;
            
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: 健康检查异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 执行回滚任务
     */
    public void executeRollbackTask(DeployTask task) {
        System.out.println("TaskExecution[" + nodeId + "]: === 开始执行回滚任务 ===");
        System.out.println("TaskExecution[" + nodeId + "]: 服务名: " + task.getServiceName());
        
        try {
            // TODO: 实现回滚逻辑
            System.out.println("TaskExecution[" + nodeId + "]: 回滚功能暂未实现");
            throw new RuntimeException("回滚功能暂未实现");
            
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: 回滚任务执行失败: " + e.getMessage());
            throw new RuntimeException("回滚任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行启动任务
     */
    public void executeStartTask(DeployTask task) {
        System.out.println("TaskExecution[" + nodeId + "]: === 开始执行启动任务 ===");
        System.out.println("TaskExecution[" + nodeId + "]: 服务名: " + task.getServiceName());
        
        try {
            serviceManagementService.startService(task.getServiceName());
            System.out.println("TaskExecution[" + nodeId + "]: === 启动任务执行成功 ===");
            
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: 启动任务执行失败: " + e.getMessage());
            throw new RuntimeException("启动任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行停止任务
     */
    public void executeStopTask(DeployTask task) {
        System.out.println("TaskExecution[" + nodeId + "]: === 开始执行停止任务 ===");
        System.out.println("TaskExecution[" + nodeId + "]: 服务名: " + task.getServiceName());
        
        try {
            serviceManagementService.stopService(task.getServiceName());
            System.out.println("TaskExecution[" + nodeId + "]: === 停止任务执行成功 ===");
            
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: 停止任务执行失败: " + e.getMessage());
            throw new RuntimeException("停止任务执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行重启任务
     */
    public void executeRestartTask(DeployTask task) {
        System.out.println("TaskExecution[" + nodeId + "]: === 开始执行重启任务 ===");
        System.out.println("TaskExecution[" + nodeId + "]: 服务名: " + task.getServiceName());
        
        try {
            serviceManagementService.restartService(task.getServiceName());
            System.out.println("TaskExecution[" + nodeId + "]: === 重启任务执行成功 ===");
            
        } catch (Exception e) {
            System.err.println("TaskExecution[" + nodeId + "]: 重启任务执行失败: " + e.getMessage());
            throw new RuntimeException("重启任务执行失败: " + e.getMessage());
        }
    }
}
