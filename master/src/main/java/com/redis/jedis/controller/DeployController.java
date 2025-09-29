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
 */
@RestController
@RequestMapping("/api/deploy")
@Api(tags = "服务部署")
public class DeployController {
    
    @Autowired
    private DeployService deployService;
    
    @ApiOperation("从Git构建JAR包")
    @PostMapping("/build-from-git")
    public ApiResponse<String> buildFromGit(
            @RequestParam String gitUrl,
            @RequestParam(defaultValue = "main") String branch,
            @RequestParam String projectName,
            @RequestParam(required = false) String buildCommand) {
        try {
            String jarPath = deployService.buildFromGit(gitUrl, branch, projectName, buildCommand);
            return ApiResponse.success("Git构建成功", jarPath);
        } catch (Exception e) {
            return ApiResponse.error("Git构建失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("上传JAR包")
    @PostMapping("/upload")
    public ApiResponse<String> uploadJar(@RequestParam("file") MultipartFile file) {
        try {
            String jarPath = deployService.uploadJar(file);
            return ApiResponse.success("JAR包上传成功", jarPath);
        } catch (Exception e) {
            return ApiResponse.error("JAR包上传失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("部署服务")
    @PostMapping("/deploy")
    public ApiResponse<String> deployService(@RequestBody DeployRequest request) {
        try {
            String taskId = deployService.deployService(request);
            return ApiResponse.success("部署任务已创建", taskId);
        } catch (Exception e) {
            return ApiResponse.error("部署失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取任务状态")
    @GetMapping("/status/{taskId}")
    public ApiResponse<TaskStatus> getTaskStatus(@PathVariable String taskId) {
        try {
            TaskStatus status = deployService.getTaskStatus(taskId);
            if (status == null) {
                return ApiResponse.error("任务不存在: " + taskId);
            }
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error("获取任务状态失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取服务列表")
    @GetMapping("/services")
    public ApiResponse<List<ServiceInfo>> getAvailableServices() {
        try {
            List<ServiceInfo> services = deployService.getAvailableServices();
            return ApiResponse.success(services);
        } catch (Exception e) {
            return ApiResponse.error("获取服务列表失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("一键部署所有服务")
    @PostMapping("/deploy-all")
    public ApiResponse<BatchDeployResult> deployAllServices(@RequestBody BatchDeployRequest request) {
        try {
            BatchDeployResult result = deployService.deployAllServices(request);
            return ApiResponse.success("批量部署任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("批量部署失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("部署特定服务")
    @PostMapping("/deploy-selected")
    public ApiResponse<BatchDeployResult> deploySelectedServices(@RequestBody SelectedDeployRequest request) {
        try {
            BatchDeployResult result = deployService.deploySelectedServices(request);
            return ApiResponse.success("选定服务部署任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("选定服务部署失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取批量部署状态")
    @GetMapping("/batch-status")
    public ApiResponse<List<TaskStatus>> getBatchDeployStatus(@RequestParam List<String> taskIds) {
        try {
            List<TaskStatus> statusList = deployService.getBatchDeployStatus(taskIds);
            return ApiResponse.success(statusList);
        } catch (Exception e) {
            return ApiResponse.error("获取批量部署状态失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("一键回滚所有服务")
    @PostMapping("/rollback-all")
    public ApiResponse<RollbackResult> rollbackAllServices(@RequestBody RollbackRequest request) {
        try {
            RollbackResult result = deployService.rollbackAllServices(request);
            return ApiResponse.success("批量回滚任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("批量回滚失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("回滚特定服务")
    @PostMapping("/rollback-selected")
    public ApiResponse<RollbackResult> rollbackSelectedServices(@RequestBody SelectedRollbackRequest request) {
        try {
            RollbackResult result = deployService.rollbackSelectedServices(request);
            return ApiResponse.success("选定服务回滚任务已创建", result);
        } catch (Exception e) {
            return ApiResponse.error("选定服务回滚失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取回滚历史")
    @GetMapping("/rollback-history")
    public ApiResponse<List<RollbackHistory>> getRollbackHistory(@RequestParam(required = false) String serviceName) {
        try {
            List<RollbackHistory> history = deployService.getRollbackHistory(serviceName);
            return ApiResponse.success(history);
        } catch (Exception e) {
            return ApiResponse.error("获取回滚历史失败: " + e.getMessage());
        }
    }
}
