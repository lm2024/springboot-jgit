package com.redis.jedis.controller;

import com.redis.jedis.dto.*;
import com.redis.jedis.service.DeployService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 部署Controller
 * 提供服务部署管理功能
 * 
 * 测试流程说明：
 * 1. 代码下载阶段 - 从Git仓库下载代码
 * 2. 代码构建阶段 - 编译打包生成JAR文件
 * 3. 文件分发阶段 - 将JAR包分发到Agent节点
 * 4. 服务部署阶段 - 在Agent节点启动服务
 * 5. 监控管理阶段 - 监控服务状态和管理
 */
@RestController
@RequestMapping("/api/deploy")
@Api(tags = "服务部署 - 按流程顺序测试")
public class DeployController {
    
    @Autowired
    private DeployService deployService;
    
    @Autowired
    private com.redis.jedis.config.ServicesConfig servicesConfig;
    
    /**
     * 创建默认的部署请求
     */
    private DeployRequest createDefaultDeployRequest() {
        DeployRequest request = new DeployRequest();
        
        // 从配置中获取第一个服务的信息
        com.redis.jedis.config.ServicesConfig.ServiceInfo serviceInfo = servicesConfig.getFirstService();
        
        if (serviceInfo != null) {
            request.setServiceName(serviceInfo.getName());
            request.setJarPath(serviceInfo.getJarPath());
            request.setTargetNodes(serviceInfo.getTargetNodes());
        } else {
            // 如果配置中没有服务，抛出错误提示用户检查配置
            throw new RuntimeException("配置中没有找到任何服务，请检查application.yml中的services配置");
        }
        
        // 设置默认的部署配置
        request.setForceDeploy(false);
        request.setDeployMode("parallel");
        request.setOperator("system");
        
        return request;
    }
    
    /**
     * 创建默认的批量部署请求
     */
    private BatchDeployRequest createDefaultBatchDeployRequest() {
        BatchDeployRequest request = new BatchDeployRequest();
        
        // 设置默认的部署配置
        request.setForceDeploy(false);
        request.setDeployMode("parallel");
        request.setOperator("system");
        
        return request;
    }
    
    // ===========================================
    // 第一阶段：代码下载 (Step 1)
    // ===========================================
    
    @ApiOperation(value = "【测试步骤1】从配置的Git仓库下载代码并构建", 
                  notes = "根据application.yml中的git.repositories配置自动下载和构建项目。不传参数时使用第一个配置的项目")
    @PostMapping("/step1-download-and-build")
    public ApiResponse<String> step1DownloadAndBuild(@RequestParam(required = false) String projectName) {
        try {
            // 如果没有指定项目名称，使用配置中的第一个项目
            if (projectName == null || projectName.trim().isEmpty()) {
                List<com.redis.jedis.config.GitRepositoryConfig.RepositoryInfo> repositories = 
                    deployService.getGitService().getAllRepositories();
                if (repositories == null || repositories.isEmpty()) {
                    return ApiResponse.error("步骤1失败：配置中没有找到任何项目，请检查application.yml中的git.repositories配置");
                }
                projectName = repositories.get(0).getName();
            }
            
            String jarPath = deployService.buildFromGitByProjectName(projectName);
            return ApiResponse.success("步骤1完成：代码下载和构建成功 (项目: " + projectName + ")", jarPath);
        } catch (Exception e) {
            return ApiResponse.error("步骤1失败：代码下载和构建失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤1-备选】手动指定Git仓库下载代码并构建", 
                  notes = "手动指定Git URL、分支等信息进行下载和构建。不传参数时使用配置中的第一个项目")
    @PostMapping("/step1-manual-download-and-build")
    public ApiResponse<String> step1ManualDownloadAndBuild(
            @RequestParam(required = false) String gitUrl,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String buildCommand) {
        try {
            // 如果没有指定参数，使用配置中的第一个项目
            if (projectName == null || projectName.trim().isEmpty()) {
                List<com.redis.jedis.config.GitRepositoryConfig.RepositoryInfo> repositories = 
                    deployService.getGitService().getAllRepositories();
                if (repositories == null || repositories.isEmpty()) {
                    return ApiResponse.error("步骤1失败：配置中没有找到任何项目，请检查application.yml中的git.repositories配置");
                }
                com.redis.jedis.config.GitRepositoryConfig.RepositoryInfo repoInfo = repositories.get(0);
                gitUrl = repoInfo.getUrl();
                branch = repoInfo.getBranch();
                projectName = repoInfo.getName();
                buildCommand = repoInfo.getBuildCommand();
            }
            
            String jarPath = deployService.buildFromGit(gitUrl, branch, projectName, buildCommand);
            return ApiResponse.success("步骤1完成：手动下载和构建成功 (项目: " + projectName + ")", jarPath);
        } catch (Exception e) {
            return ApiResponse.error("步骤1失败：手动下载和构建失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤1-备选】直接上传JAR包", 
                  notes = "如果已有JAR包，可以直接上传跳过下载和构建步骤")
    @PostMapping("/step1-upload-jar")
    public ApiResponse<String> step1UploadJar(@RequestParam("file") MultipartFile file) {
        try {
            String jarPath = deployService.uploadJar(file);
            return ApiResponse.success("步骤1完成：JAR包上传成功", jarPath);
        } catch (Exception e) {
            return ApiResponse.error("步骤1失败：JAR包上传失败 - " + e.getMessage());
        }
    }
    
    // ===========================================
    // 第二阶段：服务部署 (Step 2)
    // ===========================================
    
    @ApiOperation(value = "【测试步骤2】部署单个服务到Agent节点", 
                  notes = "将构建好的JAR包部署到指定的Agent节点。不传参数时使用配置中的第一个服务")
    @PostMapping("/step2-deploy-service")
    public ApiResponse<String> step2DeployService(@RequestBody(required = false) DeployRequest request) {
        try {
            // 如果没有传入请求参数，使用配置中的默认值
            if (request == null) {
                request = createDefaultDeployRequest();
            }
            
            String taskId = deployService.deployService(request);
            return ApiResponse.success("步骤2完成：服务部署任务已创建 (服务: " + request.getServiceName() + ")", taskId);
        } catch (Exception e) {
            return ApiResponse.error("步骤2失败：服务部署失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤2-批量】一键部署所有配置的服务", 
                  notes = "根据application.yml中的services配置部署所有服务。不传参数时使用默认配置")
    @PostMapping("/step2-deploy-all")
    public ApiResponse<BatchDeployResult> step2DeployAllServices(@RequestBody(required = false) BatchDeployRequest request) {
        try {
            // 如果没有传入请求参数，使用默认配置
            if (request == null) {
                request = createDefaultBatchDeployRequest();
            }
            
            BatchDeployResult result = deployService.deployAllServices(request);
            return ApiResponse.success("步骤2完成：批量部署任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("步骤2失败：批量部署失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤2-选择】部署选定的服务", 
                  notes = "从配置的服务列表中选择特定服务进行部署。不传参数时部署第一个配置的服务")
    @PostMapping("/step2-deploy-selected")
    public ApiResponse<BatchDeployResult> step2DeploySelectedServices(@RequestBody(required = false) SelectedDeployRequest request) {
        try {
            // 如果没有传入请求参数，使用默认配置
            if (request == null) {
                request = new SelectedDeployRequest();
                com.redis.jedis.config.ServicesConfig.ServiceInfo serviceInfo = servicesConfig.getFirstService();
                if (serviceInfo != null) {
                    request.setServiceNames(java.util.Arrays.asList(serviceInfo.getName()));
                } else {
                    throw new RuntimeException("配置中没有找到任何服务，请检查application.yml中的services配置");
                }
                request.setForceDeploy(false);
                request.setDeployMode("parallel");
                request.setOperator("system");
            }
            
            BatchDeployResult result = deployService.deploySelectedServices(request);
            return ApiResponse.success("步骤2完成：选定服务部署任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("步骤2失败：选定服务部署失败 - " + e.getMessage());
        }
    }
    
    // ===========================================
    // 第三阶段：状态监控 (Step 3)
    // ===========================================
    
    @ApiOperation(value = "【测试步骤3】查看部署任务状态", 
                  notes = "检查部署任务的执行状态。不传taskId时返回最近的任务状态")
    @GetMapping("/step3-task-status/{taskId}")
    public ApiResponse<TaskStatus> step3GetTaskStatus(@PathVariable(required = false) String taskId) {
        try {
            // 如果没有指定taskId，尝试获取最近的任务状态
            if (taskId == null || taskId.trim().isEmpty()) {
                // 这里可以实现获取最近任务状态的逻辑
                return ApiResponse.error("步骤3失败：请指定taskId参数");
            }
            
            TaskStatus status = deployService.getTaskStatus(taskId);
            if (status == null) {
                return ApiResponse.error("任务不存在: " + taskId);
            }
            return ApiResponse.success("步骤3完成：任务状态查询成功", status);
        } catch (Exception e) {
            return ApiResponse.error("步骤3失败：获取任务状态失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤3】查看所有可用服务", 
                  notes = "查看系统中所有可用的服务列表")
    @GetMapping("/step3-available-services")
    public ApiResponse<List<ServiceInfo>> step3GetAvailableServices() {
        try {
            List<ServiceInfo> services = deployService.getAvailableServices();
            return ApiResponse.success("步骤3完成：服务列表查询成功", services);
        } catch (Exception e) {
            return ApiResponse.error("步骤3失败：获取服务列表失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤3】查看批量部署状态", 
                  notes = "查看多个部署任务的执行状态。不传taskIds时返回所有任务状态")
    @GetMapping("/step3-batch-status")
    public ApiResponse<List<TaskStatus>> step3GetBatchDeployStatus(@RequestParam(required = false) List<String> taskIds) {
        try {
            // 如果没有指定taskIds，返回所有任务状态
            if (taskIds == null || taskIds.isEmpty()) {
                // 这里可以实现获取所有任务状态的逻辑
                return ApiResponse.success("步骤3完成：批量状态查询成功（所有任务）", new java.util.ArrayList<>());
            }
            
            List<TaskStatus> statusList = deployService.getBatchDeployStatus(taskIds);
            return ApiResponse.success("步骤3完成：批量状态查询成功", statusList);
        } catch (Exception e) {
            return ApiResponse.error("步骤3失败：获取批量部署状态失败 - " + e.getMessage());
        }
    }
    
    // ===========================================
    // 第四阶段：服务管理 (Step 4)
    // ===========================================
    
    @ApiOperation(value = "【测试步骤4】回滚所有服务", 
                  notes = "将所有服务回滚到上一个版本。不传参数时使用默认配置")
    @PostMapping("/step4-rollback-all")
    public ApiResponse<RollbackResult> step4RollbackAllServices(@RequestBody(required = false) RollbackRequest request) {
        try {
            // 如果没有传入请求参数，使用默认配置
            if (request == null) {
                request = new RollbackRequest();
                // 设置默认的回滚配置
                request.setForceRollback(false);
                request.setOperator("system");
            }
            
            RollbackResult result = deployService.rollbackAllServices(request);
            return ApiResponse.success("步骤4完成：批量回滚任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("步骤4失败：批量回滚失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤4】回滚选定服务", 
                  notes = "回滚选定的特定服务。不传参数时回滚第一个配置的服务")
    @PostMapping("/step4-rollback-selected")
    public ApiResponse<RollbackResult> step4RollbackSelectedServices(@RequestBody(required = false) SelectedRollbackRequest request) {
        try {
            // 如果没有传入请求参数，使用默认配置
            if (request == null) {
                request = new SelectedRollbackRequest();
                com.redis.jedis.config.ServicesConfig.ServiceInfo serviceInfo = servicesConfig.getFirstService();
                if (serviceInfo != null) {
                    request.setServiceNames(java.util.Arrays.asList(serviceInfo.getName()));
                } else {
                    throw new RuntimeException("配置中没有找到任何服务，请检查application.yml中的services配置");
                }
                request.setForceRollback(false);
                request.setOperator("system");
            }
            
            RollbackResult result = deployService.rollbackSelectedServices(request);
            return ApiResponse.success("步骤4完成：选定服务回滚任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("步骤4失败：选定服务回滚失败 - " + e.getMessage());
        }
    }
    
    @ApiOperation(value = "【测试步骤4】查看回滚历史", 
                  notes = "查看服务的回滚历史记录。不传serviceName时查看所有服务的回滚历史")
    @GetMapping("/step4-rollback-history")
    public ApiResponse<List<RollbackHistory>> step4GetRollbackHistory(@RequestParam(required = false) String serviceName) {
        try {
            // 如果没有指定serviceName，查看所有服务的回滚历史
            List<RollbackHistory> history = deployService.getRollbackHistory(serviceName);
            return ApiResponse.success("步骤4完成：回滚历史查询成功" + 
                (serviceName != null ? " (服务: " + serviceName + ")" : " (所有服务)"), history);
        } catch (Exception e) {
            return ApiResponse.error("步骤4失败：获取回滚历史失败 - " + e.getMessage());
        }
    }
    
    // ===========================================
    // 兼容性接口 (保持向后兼容)
    // ===========================================
    
    @ApiOperation("从配置的Git仓库构建JAR包")
    @PostMapping("/build-from-config")
    public ApiResponse<String> buildFromConfig(@RequestParam String projectName) {
        return step1DownloadAndBuild(projectName);
    }
    
    @ApiOperation("从Git构建JAR包")
    @PostMapping("/build-from-git")
    public ApiResponse<String> buildFromGit(
            @RequestParam String gitUrl,
            @RequestParam(defaultValue = "main") String branch,
            @RequestParam String projectName,
            @RequestParam(required = false) String buildCommand) {
        return step1ManualDownloadAndBuild(gitUrl, branch, projectName, buildCommand);
    }
    
    @ApiOperation("上传JAR包")
    @PostMapping("/upload")
    public ApiResponse<String> uploadJar(@RequestParam("file") MultipartFile file) {
        return step1UploadJar(file);
    }
    
    @ApiOperation("部署服务")
    @PostMapping("/deploy")
    public ApiResponse<String> deployService(@RequestBody DeployRequest request) {
        return step2DeployService(request);
    }
    
    @ApiOperation("获取任务状态")
    @GetMapping("/status/{taskId}")
    public ApiResponse<TaskStatus> getTaskStatus(@PathVariable String taskId) {
        return step3GetTaskStatus(taskId);
    }
    
    @ApiOperation("获取服务列表")
    @GetMapping("/services")
    public ApiResponse<List<ServiceInfo>> getAvailableServices() {
        return step3GetAvailableServices();
    }
    
    @ApiOperation("一键部署所有服务")
    @PostMapping("/deploy-all")
    public ApiResponse<BatchDeployResult> deployAllServices(@RequestBody BatchDeployRequest request) {
        return step2DeployAllServices(request);
    }
    
    @ApiOperation("部署特定服务")
    @PostMapping("/deploy-selected")
    public ApiResponse<BatchDeployResult> deploySelectedServices(@RequestBody SelectedDeployRequest request) {
        return step2DeploySelectedServices(request);
    }
    
    @ApiOperation("获取批量部署状态")
    @GetMapping("/batch-status")
    public ApiResponse<List<TaskStatus>> getBatchDeployStatus(@RequestParam List<String> taskIds) {
        return step3GetBatchDeployStatus(taskIds);
    }
    
    @ApiOperation("一键回滚所有服务")
    @PostMapping("/rollback-all")
    public ApiResponse<RollbackResult> rollbackAllServices(@RequestBody RollbackRequest request) {
        return step4RollbackAllServices(request);
    }
    
    @ApiOperation("回滚特定服务")
    @PostMapping("/rollback-selected")
    public ApiResponse<RollbackResult> rollbackSelectedServices(@RequestBody SelectedRollbackRequest request) {
        return step4RollbackSelectedServices(request);
    }
    
    @ApiOperation("获取回滚历史")
    @GetMapping("/rollback-history")
    public ApiResponse<List<RollbackHistory>> getRollbackHistory(@RequestParam(required = false) String serviceName) {
        return step4GetRollbackHistory(serviceName);
    }
}
