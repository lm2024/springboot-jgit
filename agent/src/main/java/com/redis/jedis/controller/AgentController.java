package com.redis.jedis.controller;

import com.redis.jedis.dto.*;
import com.redis.jedis.service.AgentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent节点Controller
 * 负责接收任务、管理服务、状态上报
 */
@RestController
@RequestMapping("/api/agent")
@Api(tags = "Agent节点")
public class AgentController {
    
    @Autowired
    private AgentService agentService;
    
    @ApiOperation("获取节点状态")
    @GetMapping("/status")
    public ApiResponse<StatusReport> getNodeStatus() {
        try {
            StatusReport report = agentService.getNodeStatus();
            return ApiResponse.success(report);
        } catch (Exception e) {
            return ApiResponse.error("获取节点状态失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("手动上报状态")
    @PostMapping("/report")
    public ApiResponse<String> reportStatus() {
        try {
            agentService.reportStatus();
            return ApiResponse.success("状态上报成功");
        } catch (Exception e) {
            return ApiResponse.error("状态上报失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("执行部署任务")
    @PostMapping("/deploy")
    public ApiResponse<String> executeDeploy(@RequestBody DeployTask task) {
        try {
            agentService.executeDeployTask(task);
            return ApiResponse.success("部署任务执行成功");
        } catch (Exception e) {
            return ApiResponse.error("部署任务执行失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("启动服务")
    @PostMapping("/start/{serviceName}")
    public ApiResponse<String> startService(@PathVariable String serviceName) {
        try {
            agentService.startService(serviceName);
            return ApiResponse.success("服务启动成功");
        } catch (Exception e) {
            return ApiResponse.error("服务启动失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("停止服务")
    @PostMapping("/stop/{serviceName}")
    public ApiResponse<String> stopService(@PathVariable String serviceName) {
        try {
            agentService.stopService(serviceName);
            return ApiResponse.success("服务停止成功");
        } catch (Exception e) {
            return ApiResponse.error("服务停止失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("重启服务")
    @PostMapping("/restart/{serviceName}")
    public ApiResponse<String> restartService(@PathVariable String serviceName) {
        try {
            agentService.restartService(serviceName);
            return ApiResponse.success("服务重启成功");
        } catch (Exception e) {
            return ApiResponse.error("服务重启失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取服务状态")
    @GetMapping("/service-status/{serviceName}")
    public ApiResponse<ServiceStatus> getServiceStatus(@PathVariable String serviceName) {
        try {
            ServiceStatus status = agentService.getServiceStatus(serviceName);
            if (status == null) {
                return ApiResponse.error("服务不存在: " + serviceName);
            }
            return ApiResponse.success(status);
        } catch (Exception e) {
            return ApiResponse.error("获取服务状态失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取所有服务状态")
    @GetMapping("/services")
    public ApiResponse<List<ServiceStatus>> getAllServices() {
        try {
            List<ServiceStatus> services = agentService.getAllServices();
            return ApiResponse.success(services);
        } catch (Exception e) {
            return ApiResponse.error("获取服务列表失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("健康检查")
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        try {
            boolean healthy = agentService.healthCheck();
            if (healthy) {
                return ApiResponse.success("节点健康");
            } else {
                return ApiResponse.error("节点不健康");
            }
        } catch (Exception e) {
            return ApiResponse.error("健康检查失败: " + e.getMessage());
        }
    }
}
