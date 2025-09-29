package com.redis.jedis.service;

import com.alibaba.fastjson.JSON;
import com.redis.jedis.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.redis.jedis.util.RedisClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 部署服务
 * 负责服务部署管理功能
 */
@Service
public class DeployService {
    
    @Autowired
    private RedisClient jedisCluster;
    
    @Autowired
    private FileDistributionService fileDistributionService;
    
    @Autowired
    private TaskManagementService taskManagementService;
    
    @Autowired
    private RollbackService rollbackService;
    
    @Autowired
    private GitService gitService;
    
    @Autowired
    private BuildService buildService;
    
    @Autowired
    private DirectoryService directoryService;
    
    @Autowired
    private LogService logService;
    
    /**
     * 从Git构建JAR包
     */
    public String buildFromGit(String gitUrl, String branch, String projectName, String buildCommand) {
        try {
            // 克隆或更新代码
            String projectPath = gitService.cloneOrPull(gitUrl, branch);
            
            // 执行Maven构建
            BuildService.BuildResult buildResult = buildService.build(projectPath, buildCommand);
            
            if (!buildResult.isSuccess()) {
                throw new RuntimeException("构建失败: " + buildResult.getErrorMessage());
            }
            
            // 复制JAR到分发目录
            String jarPath = buildResult.getJarPath();
            if (jarPath == null) {
                throw new RuntimeException("未找到生成的JAR文件");
            }
            
            String distributePath = directoryService.getProjectDistributePath(projectName);
            String targetJarPath = Paths.get(distributePath, Paths.get(jarPath).getFileName().toString()).toString();
            
            Files.copy(Paths.get(jarPath), Paths.get(targetJarPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // 记录构建日志
            logBuildInfo(projectName, gitUrl, branch, targetJarPath, buildResult.getDuration());
            logService.logDeploy(projectName, "BUILD_FROM_GIT", 
                String.format("Git: %s:%s, JAR: %s, Duration: %dms", gitUrl, branch, targetJarPath, buildResult.getDuration()));
            
            return targetJarPath;
            
        } catch (Exception e) {
            throw new RuntimeException("从Git构建失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传JAR包
     */
    public String uploadJar(MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                throw new RuntimeException("上传文件为空");
            }
            
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".jar")) {
                throw new RuntimeException("文件必须是JAR格式");
            }
            
            // 保存文件
            String jarPath = fileDistributionService.saveUploadedFile(file);
            
            // 记录上传日志
            logUploadInfo(originalFilename, jarPath, file.getSize());
            logService.logDeploy("UPLOAD", "UPLOAD_JAR", 
                String.format("File: %s, Path: %s, Size: %d bytes", originalFilename, jarPath, file.getSize()));
            
            return jarPath;
            
        } catch (Exception e) {
            throw new RuntimeException("JAR包上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 部署服务
     */
    public String deployService(DeployRequest request) {
        try {
            // 验证请求
            validateDeployRequest(request);
            
            // 检查JAR文件是否存在
            if (!new java.io.File(request.getJarPath()).exists()) {
                throw new RuntimeException("JAR文件不存在: " + request.getJarPath());
            }
            
            // 选择分发方式
            long fileSize = new java.io.File(request.getJarPath()).length();
            String distributionMethod = fileDistributionService.selectDistributionMethod(fileSize);
            
            // 分发文件
            String distributionResult = fileDistributionService.distributeFile(
                request.getJarPath(), 
                request.getTargetNodes(), 
                distributionMethod
            );
            
            // 创建部署任务
            String taskId = createDeployTask(request, distributionResult, distributionMethod);
            
            // 发送任务到Agent节点
            sendTaskToAgents(taskId, request.getTargetNodes());
            
            return taskId;
            
        } catch (Exception e) {
            throw new RuntimeException("部署失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务状态
     */
    public TaskStatus getTaskStatus(String taskId) {
        try {
            return taskManagementService.getTaskStatus(taskId);
        } catch (Exception e) {
            throw new RuntimeException("获取任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取服务列表
     */
    public List<ServiceInfo> getAvailableServices() {
        try {
            // TODO: 从配置获取服务列表
            // 这里先返回示例数据，后续实现
            List<ServiceInfo> services = new ArrayList<>();
            
            ServiceInfo service1 = new ServiceInfo();
            service1.setName("service-a");
            service1.setJarName("service-a.jar");
            service1.setJarPath("/opt/services/service-a.jar");
            service1.setDescription("服务A");
            services.add(service1);
            
            ServiceInfo service2 = new ServiceInfo();
            service2.setName("service-b");
            service2.setJarName("service-b.jar");
            service2.setJarPath("/opt/services/service-b.jar");
            service2.setDescription("服务B");
            services.add(service2);
            
            return services;
            
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量部署所有服务
     */
    public BatchDeployResult deployAllServices(BatchDeployRequest request) {
        try {
            List<ServiceInfo> services = getAvailableServices();
            List<String> taskIds = new ArrayList<>();
            
            for (ServiceInfo service : services) {
                try {
                    // 检查JAR文件是否存在
                    if (!new java.io.File(service.getJarPath()).exists()) {
                        continue; // 跳过不存在的服务
                    }
                    
                    // 创建部署请求
                    DeployRequest deployRequest = new DeployRequest();
                    deployRequest.setServiceName(service.getName());
                    deployRequest.setJarPath(service.getJarPath());
                    deployRequest.setTargetNodes(service.getTargetNodes());
                    deployRequest.setForceDeploy(request.isForceDeploy());
                    deployRequest.setDeployMode(request.getDeployMode());
                    deployRequest.setOperator(request.getOperator());
                    
                    // 部署服务
                    String taskId = deployService(deployRequest);
                    taskIds.add(taskId);
                    
                } catch (Exception e) {
                    // 记录错误但继续处理其他服务
                    System.err.println("部署服务 " + service.getName() + " 失败: " + e.getMessage());
                }
            }
            
            BatchDeployResult result = new BatchDeployResult();
            result.setTotalServices(services.size());
            result.setTaskIds(taskIds);
            result.setStatus("批量部署任务已创建");
            result.setOperator(request.getOperator());
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("批量部署失败: " + e.getMessage());
        }
    }
    
    /**
     * 部署选定服务
     */
    public BatchDeployResult deploySelectedServices(SelectedDeployRequest request) {
        try {
            List<String> serviceNames = request.getServiceNames();
            List<String> taskIds = new ArrayList<>();
            
            for (String serviceName : serviceNames) {
                try {
                    // 查找服务配置
                    ServiceInfo service = findServiceByName(serviceName);
                    if (service == null) {
                        continue; // 跳过不存在的服务
                    }
                    
                    // 检查JAR文件是否存在
                    if (!new java.io.File(service.getJarPath()).exists()) {
                        continue; // 跳过不存在的服务
                    }
                    
                    // 创建部署请求
                    DeployRequest deployRequest = new DeployRequest();
                    deployRequest.setServiceName(service.getName());
                    deployRequest.setJarPath(service.getJarPath());
                    deployRequest.setTargetNodes(service.getTargetNodes());
                    deployRequest.setForceDeploy(request.isForceDeploy());
                    deployRequest.setDeployMode(request.getDeployMode());
                    deployRequest.setOperator(request.getOperator());
                    
                    // 部署服务
                    String taskId = deployService(deployRequest);
                    taskIds.add(taskId);
                    
                } catch (Exception e) {
                    // 记录错误但继续处理其他服务
                    System.err.println("部署服务 " + serviceName + " 失败: " + e.getMessage());
                }
            }
            
            BatchDeployResult result = new BatchDeployResult();
            result.setTotalServices(serviceNames.size());
            result.setTaskIds(taskIds);
            result.setStatus("选定服务部署任务已创建");
            result.setOperator(request.getOperator());
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("选定服务部署失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取批量部署状态
     */
    public List<TaskStatus> getBatchDeployStatus(List<String> taskIds) {
        try {
            List<TaskStatus> statusList = new ArrayList<>();
            for (String taskId : taskIds) {
                TaskStatus status = getTaskStatus(taskId);
                if (status != null) {
                    statusList.add(status);
                }
            }
            return statusList;
        } catch (Exception e) {
            throw new RuntimeException("获取批量部署状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量回滚所有服务
     */
    public RollbackResult rollbackAllServices(RollbackRequest request) {
        try {
            List<ServiceInfo> services = getAvailableServices();
            List<RollbackDetail> rollbackDetails = new ArrayList<>();
            
            for (ServiceInfo service : services) {
                try {
                    RollbackDetail detail = rollbackService(service, request);
                    rollbackDetails.add(detail);
                } catch (Exception e) {
                    RollbackDetail detail = new RollbackDetail();
                    detail.setServiceName(service.getName());
                    detail.setSuccess(false);
                    detail.setErrorMessage(e.getMessage());
                    rollbackDetails.add(detail);
                }
            }
            
            RollbackResult result = new RollbackResult();
            result.setTotalServices(services.size());
            result.setSuccessCount(rollbackDetails.stream().mapToInt(d -> d.isSuccess() ? 1 : 0).sum());
            result.setFailedCount(rollbackDetails.stream().mapToInt(d -> d.isSuccess() ? 0 : 1).sum());
            result.setRollbackDetails(rollbackDetails);
            result.setStatus("批量回滚任务已创建");
            result.setOperator(request.getOperator());
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("批量回滚失败: " + e.getMessage());
        }
    }
    
    /**
     * 回滚选定服务
     */
    public RollbackResult rollbackSelectedServices(SelectedRollbackRequest request) {
        try {
            List<String> serviceNames = request.getServiceNames();
            List<RollbackDetail> rollbackDetails = new ArrayList<>();
            
            for (String serviceName : serviceNames) {
                try {
                    ServiceInfo service = findServiceByName(serviceName);
                    if (service == null) {
                        RollbackDetail detail = new RollbackDetail();
                        detail.setServiceName(serviceName);
                        detail.setSuccess(false);
                        detail.setErrorMessage("服务不存在");
                        rollbackDetails.add(detail);
                        continue;
                    }
                    
                    RollbackDetail detail = rollbackService(service, request);
                    rollbackDetails.add(detail);
                    
                } catch (Exception e) {
                    RollbackDetail detail = new RollbackDetail();
                    detail.setServiceName(serviceName);
                    detail.setSuccess(false);
                    detail.setErrorMessage(e.getMessage());
                    rollbackDetails.add(detail);
                }
            }
            
            RollbackResult result = new RollbackResult();
            result.setTotalServices(serviceNames.size());
            result.setSuccessCount(rollbackDetails.stream().mapToInt(d -> d.isSuccess() ? 1 : 0).sum());
            result.setFailedCount(rollbackDetails.stream().mapToInt(d -> d.isSuccess() ? 0 : 1).sum());
            result.setRollbackDetails(rollbackDetails);
            result.setStatus("选定服务回滚任务已创建");
            result.setOperator(request.getOperator());
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("选定服务回滚失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取回滚历史
     */
    public List<RollbackHistory> getRollbackHistory(String serviceName) {
        try {
            // TODO: 从Redis获取回滚历史
            // 这里先返回空列表，后续实现
            return new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("获取回滚历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证部署请求
     */
    private void validateDeployRequest(DeployRequest request) {
        if (request.getServiceName() == null || request.getServiceName().trim().isEmpty()) {
            throw new RuntimeException("服务名称不能为空");
        }
        if (request.getJarPath() == null || request.getJarPath().trim().isEmpty()) {
            throw new RuntimeException("JAR文件路径不能为空");
        }
        if (request.getTargetNodes() == null || request.getTargetNodes().isEmpty()) {
            throw new RuntimeException("目标节点不能为空");
        }
    }
    
    /**
     * 创建部署任务
     */
    private String createDeployTask(DeployRequest request, String distributionResult, String distributionMethod) {
        String taskId = "deploy_" + request.getServiceName() + "_" + System.currentTimeMillis();
        
        DeployTask task = new DeployTask();
        task.setTaskId(taskId);
        task.setServiceName(request.getServiceName());
        task.setAction("DEPLOY");
        task.setDistributionMethod(distributionMethod);
        task.setFilePath(distributionResult);
        task.setTargetNodes(request.getTargetNodes());
        task.setOperator(request.getOperator());
        
        // 存储任务到Redis
        taskManagementService.storeTask(task);
        
        return taskId;
    }
    
    /**
     * 发送任务到Agent节点
     */
    private void sendTaskToAgents(String taskId, List<String> targetNodes) {
        try {
            DeployTask task = taskManagementService.getTask(taskId);
            if (task != null) {
                for (String nodeId : targetNodes) {
                    String taskJson = JSON.toJSONString(task);
                    jedisCluster.lpush("task:queue:" + nodeId, taskJson);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("发送任务到Agent节点失败: " + e.getMessage());
        }
    }
    
    /**
     * 回滚服务
     */
    private RollbackDetail rollbackService(ServiceInfo service, Object request) {
        return rollbackService.rollbackService(service, request);
    }
    
    /**
     * 查找服务配置
     */
    private ServiceInfo findServiceByName(String serviceName) {
        List<ServiceInfo> services = getAvailableServices();
        return services.stream()
                .filter(service -> service.getName().equals(serviceName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 记录上传日志
     */
    private void logUploadInfo(String originalFilename, String jarPath, long fileSize) {
        System.out.println("文件上传成功: " + originalFilename + " -> " + jarPath + " (大小: " + fileSize + " bytes)");
    }
    
    /**
     * 记录构建日志
     */
    private void logBuildInfo(String projectName, String gitUrl, String branch, String jarPath, long duration) {
        System.out.println("Git构建成功: " + projectName + " (" + gitUrl + ":" + branch + ") -> " + jarPath + " (耗时: " + duration + "ms)");
    }
}