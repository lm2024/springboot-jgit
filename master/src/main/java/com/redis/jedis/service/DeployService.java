package com.redis.jedis.service;

import com.alibaba.fastjson.JSON;
import com.redis.jedis.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.redis.jedis.util.RedisClient;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private com.redis.jedis.config.ServicesConfig servicesConfig;

    /**
     * 获取GitService实例
     */
    public GitService getGitService() {
        return gitService;
    }

    /**
     * 根据配置的项目名称从Git构建JAR包
     */
    public String buildFromGitByProjectName(String projectName) {
        System.out.println("=== 开始构建项目: " + projectName + " ===");

        try {
            // 步骤1: 获取项目配置信息
            System.out.println("步骤1: 获取项目配置信息...");
            com.redis.jedis.config.GitRepositoryConfig.RepositoryInfo repoInfo = gitService
                    .getRepositoryInfo(projectName);
            if (repoInfo == null) {
                System.err.println("错误: 未找到项目配置: " + projectName);
                System.err.println("请检查application.yml中的git.repositories配置");
                throw new RuntimeException("未找到项目配置: " + projectName);
            }
            System.out.println("项目配置获取成功:");
            System.out.println("  - 项目名称: " + repoInfo.getName());
            System.out.println("  - Git URL: " + repoInfo.getUrl());
            System.out.println("  - 分支: " + repoInfo.getBranch());
            System.out.println("  - 构建命令: " + repoInfo.getBuildCommand());

            // 步骤2: 克隆或更新代码
            System.out.println("步骤2: 克隆或更新代码...");
            String projectPath = gitService.cloneOrPullByProjectName(projectName);
            System.out.println("代码下载成功，项目路径: " + projectPath);

            // 步骤3: 执行Maven构建
            System.out.println("步骤3: 执行Maven构建...");
            System.out.println("构建命令: " + repoInfo.getBuildCommand());
            BuildService.BuildResult buildResult = buildService.build(projectPath, repoInfo.getBuildCommand());

            System.out.println("构建结果:");
            System.out.println("  - 成功: " + buildResult.isSuccess());
            System.out.println("  - 退出码: " + buildResult.getExitCode());
            System.out.println("  - 耗时: " + buildResult.getDuration() + "ms");
            if (buildResult.getOutput() != null && !buildResult.getOutput().trim().isEmpty()) {
                System.out.println("  - 构建输出:");
                System.out.println(buildResult.getOutput());
            }

            if (!buildResult.isSuccess()) {
                System.err.println("Maven构建失败!");
                System.err.println("错误信息: " + buildResult.getErrorMessage());
                if (buildResult.getOutput() != null) {
                    System.err.println("详细输出: " + buildResult.getOutput());
                }
                throw new RuntimeException(
                        "Maven构建失败，退出码: " + buildResult.getExitCode() + ", 错误: " + buildResult.getErrorMessage());
            }

            // 步骤4: 检查生成的JAR文件
            System.out.println("步骤4: 检查生成的JAR文件...");
            String jarPath = buildResult.getJarPath();
            if (jarPath == null) {
                System.err.println("错误: 未找到生成的JAR文件");
                System.err.println("请检查Maven构建是否正确生成了JAR包");
                throw new RuntimeException("未找到生成的JAR文件");
            }
            System.out.println("找到JAR文件: " + jarPath);

            // 步骤5: 复制JAR到分发目录
            System.out.println("步骤5: 复制JAR到分发目录...");
            String distributePath = directoryService.getProjectDistributePath(projectName);
            System.out.println("分发目录: " + distributePath);

            String targetJarPath = java.nio.file.Paths
                    .get(distributePath, java.nio.file.Paths.get(jarPath).getFileName().toString()).toString();
            System.out.println("目标JAR路径: " + targetJarPath);

            // 确保分发目录存在
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(distributePath));

            Files.copy(java.nio.file.Paths.get(jarPath), java.nio.file.Paths.get(targetJarPath),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("JAR文件复制成功");

            // 步骤6: 更新服务配置中的JAR路径
            System.out.println("步骤6: 更新服务配置...");
            updateServiceJarPath(projectName, targetJarPath);

            // 记录构建日志
            logBuildInfo(projectName, repoInfo.getUrl(), repoInfo.getBranch(), targetJarPath,
                    buildResult.getDuration());
            logService.logDeploy(projectName, "BUILD_FROM_GIT",
                    String.format("Git: %s:%s, JAR: %s, Duration: %dms", repoInfo.getUrl(), repoInfo.getBranch(),
                            targetJarPath, buildResult.getDuration()));

            System.out.println("=== 项目构建完成: " + projectName + " ===");
            return targetJarPath;

        } catch (Exception e) {
            System.err.println("=== 项目构建失败: " + projectName + " ===");
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("从Git构建失败: " + e.getMessage(), e);
        }
    }

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
                    String.format("Git: %s:%s, JAR: %s, Duration: %dms", gitUrl, branch, targetJarPath,
                            buildResult.getDuration()));

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
                    distributionMethod);

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
        System.out.println("获取服务列表: 开始从配置文件读取服务信息...");

        try {
            // 检查servicesConfig是否为null
            if (servicesConfig == null) {
                System.err.println("获取服务列表: servicesConfig为null，请检查配置注入");
                return new ArrayList<>();
            }
            System.out.println("获取服务列表: servicesConfig注入成功");

            // 从配置文件获取服务列表
            List<com.redis.jedis.config.ServicesConfig.ServiceInfo> configServices = servicesConfig.getServices();
            System.out.println("获取服务列表: 从配置获取到 " + (configServices != null ? configServices.size() : 0) + " 个服务配置");

            List<ServiceInfo> services = new ArrayList<>();

            if (configServices != null && !configServices.isEmpty()) {
                System.out.println("获取服务列表: 开始转换服务配置...");
                for (int i = 0; i < configServices.size(); i++) {
                    com.redis.jedis.config.ServicesConfig.ServiceInfo configService = configServices.get(i);
                    System.out.println("  转换服务 " + (i + 1) + ":");
                    System.out.println("    - 名称: " + configService.getName());
                    System.out.println("    - JAR名称: " + configService.getJarName());
                    System.out.println("    - JAR路径: " + configService.getJarPath());
                    System.out.println("    - 目标节点: " + configService.getTargetNodes());
                    System.out.println("    - 描述: " + configService.getDescription());

                    ServiceInfo service = new ServiceInfo();
                    service.setName(configService.getName());
                    service.setJarName(configService.getJarName());
                    service.setJarPath(configService.getJarPath());
                    service.setTargetNodes(configService.getTargetNodes());
                    service.setDescription(configService.getDescription());
                    services.add(service);

                    System.out.println("    ✓ 服务转换完成");
                }
                System.out.println("获取服务列表: 所有服务配置转换完成");
            } else {
                System.out.println("获取服务列表: 配置中没有找到服务信息");
                System.out.println("  请检查application.yml中是否配置了services节点");
                System.out.println("  配置格式示例:");
                System.out.println("  services:");
                System.out.println("    services:");
                System.out.println("      - name: service-abc");
                System.out.println("        jarName: abc-1.0.0.jar");
                System.out.println("        jarPath: /path/to/abc-1.0.0.jar");
                System.out.println("        targetNodes: [\"node1\", \"node2\"]");
                System.out.println("        description: ABC服务");
            }

            System.out.println("获取服务列表: 返回 " + services.size() + " 个服务");
            return services;

        } catch (Exception e) {
            System.err.println("获取服务列表: 发生异常 - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("获取服务列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量部署所有服务
     */
    public BatchDeployResult deployAllServices(BatchDeployRequest request) {
        System.out.println("=== 开始批量部署所有服务 ===");
        System.out.println("请求参数: " + (request != null ? request.toString() : "null"));

        try {
            // 获取服务列表
            System.out.println("步骤1: 获取可用服务列表...");
            List<ServiceInfo> services = getAvailableServices();
            System.out.println("找到 " + services.size() + " 个服务配置:");

            for (int i = 0; i < services.size(); i++) {
                ServiceInfo service = services.get(i);
                System.out.println("  服务" + (i + 1) + ": " + service.getName());
                System.out.println("    - JAR路径: " + service.getJarPath());
                System.out.println("    - 目标节点: " + service.getTargetNodes());
                System.out.println("    - 描述: " + service.getDescription());
            }

            if (services.isEmpty()) {
                System.out.println("警告: 没有找到任何服务配置，请检查application.yml中的services配置");
            }

            List<String> taskIds = new ArrayList<>();
            List<String> skippedServices = new ArrayList<>();
            List<String> failedServices = new ArrayList<>();

            System.out.println("步骤2: 开始逐个部署服务...");
            for (int i = 0; i < services.size(); i++) {
                ServiceInfo service = services.get(i);
                System.out.println("处理服务 " + (i + 1) + "/" + services.size() + ": " + service.getName());

                try {
                    // 检查JAR文件是否存在
                    System.out.println("  检查JAR文件: " + service.getJarPath());
                    if (!new java.io.File(service.getJarPath()).exists()) {
                        System.out.println("  ✗ JAR文件不存在，跳过部署");
                        skippedServices.add(service.getName() + " (JAR文件不存在)");
                        continue;
                    }
                    System.out.println("  ✓ JAR文件存在");

                    // 检查目标节点
                    if (service.getTargetNodes() == null || service.getTargetNodes().isEmpty()) {
                        System.out.println("  ✗ 目标节点为空，跳过部署");
                        skippedServices.add(service.getName() + " (目标节点为空)");
                        continue;
                    }
                    System.out.println("  ✓ 目标节点: " + service.getTargetNodes());

                    // 创建部署请求
                    System.out.println("  创建部署请求...");
                    DeployRequest deployRequest = new DeployRequest();
                    deployRequest.setServiceName(service.getName());
                    deployRequest.setJarPath(service.getJarPath());
                    deployRequest.setTargetNodes(service.getTargetNodes());
                    deployRequest.setForceDeploy(request != null ? request.isForceDeploy() : false);
                    deployRequest.setDeployMode(request != null ? request.getDeployMode() : null);
                    deployRequest.setOperator(request != null ? request.getOperator() : null);

                    System.out.println("  部署请求详情:");
                    System.out.println("    - 服务名: " + deployRequest.getServiceName());
                    System.out.println("    - JAR路径: " + deployRequest.getJarPath());
                    System.out.println("    - 目标节点: " + deployRequest.getTargetNodes());
                    System.out.println("    - 强制部署: " + deployRequest.isForceDeploy());
                    System.out.println("    - 部署模式: " + deployRequest.getDeployMode());
                    System.out.println("    - 操作员: " + deployRequest.getOperator());

                    // 部署服务
                    System.out.println("  开始部署服务...");
                    String taskId = deployService(deployRequest);
                    taskIds.add(taskId);
                    System.out.println("  ✓ 部署任务创建成功，任务ID: " + taskId);

                } catch (Exception e) {
                    // 记录错误但继续处理其他服务
                    System.err.println("  ✗ 部署服务 " + service.getName() + " 失败: " + e.getMessage());
                    e.printStackTrace();
                    failedServices.add(service.getName() + " (" + e.getMessage() + ")");
                }
            }

            System.out.println("步骤3: 汇总部署结果...");
            System.out.println("  总服务数: " + services.size());
            System.out.println("  成功创建任务数: " + taskIds.size());
            System.out.println("  跳过服务数: " + skippedServices.size());
            System.out.println("  失败服务数: " + failedServices.size());

            if (!skippedServices.isEmpty()) {
                System.out.println("  跳过的服务:");
                for (String skipped : skippedServices) {
                    System.out.println("    - " + skipped);
                }
            }

            if (!failedServices.isEmpty()) {
                System.out.println("  失败的服务:");
                for (String failed : failedServices) {
                    System.out.println("    - " + failed);
                }
            }

            if (!taskIds.isEmpty()) {
                System.out.println("  创建的任务ID:");
                for (String taskId : taskIds) {
                    System.out.println("    - " + taskId);
                }
            }

            BatchDeployResult result = new BatchDeployResult();
            result.setTotalServices(services.size());
            result.setTaskIds(taskIds);
            result.setStatus("批量部署任务已创建");
            result.setOperator(request != null ? request.getOperator() : null);

            System.out.println("=== 批量部署处理完成 ===");
            return result;

        } catch (Exception e) {
            System.err.println("=== 批量部署失败 ===");
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace();
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
                    deployRequest.setForceDeploy(request != null ? request.isForceDeploy() : false);
                    deployRequest.setDeployMode(request != null ? request.getDeployMode() : null);
                    deployRequest.setOperator(request != null ? request.getOperator() : null);

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
     * 更新服务配置中的JAR路径
     */
    private void updateServiceJarPath(String serviceName, String newJarPath) {
        try {
            System.out.println("更新服务配置: " + serviceName + " -> " + newJarPath);

            // 获取当前服务列表
            List<ServiceInfo> services = getAvailableServices();

            // 查找并更新对应服务的JAR路径
            for (ServiceInfo service : services) {
                if (service.getName().equals(serviceName)) {
                    String oldPath = service.getJarPath();
                    service.setJarPath(newJarPath);
                    System.out.println("服务JAR路径已更新: " + oldPath + " -> " + newJarPath);
                    break;
                }
            }

            // 注意：这里只是更新内存中的配置，如果需要持久化到配置文件，需要额外实现
            System.out.println("服务配置更新完成（内存中）");

        } catch (Exception e) {
            System.err.println("更新服务配置失败: " + e.getMessage());
            // 不抛出异常，因为这不是关键错误
        }
    }

    /**
     * 记录构建日志
     */
    private void logBuildInfo(String projectName, String gitUrl, String branch, String jarPath, long duration) {
        System.out.println("Git构建成功: " + projectName + " (" + gitUrl + ":" + branch + ") -> " + jarPath + " (耗时: "
                + duration + "ms)");
    }
}