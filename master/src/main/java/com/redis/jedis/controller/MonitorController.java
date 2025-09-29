package com.redis.jedis.controller;

import com.redis.jedis.dto.ApiResponse;
import com.redis.jedis.dto.NodeStatus;
import com.redis.jedis.dto.NodeDetail;
import com.redis.jedis.dto.HealthStatus;
import com.redis.jedis.service.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控Controller
 * 提供节点状态监控功能
 */
@RestController
@RequestMapping("/api/monitor")
@Api(tags = "服务监控")
public class MonitorController {
    
    @Autowired
    private MonitorService monitorService;
    
    @ApiOperation("获取所有节点状态")
    @GetMapping("/nodes")
    public ApiResponse<List<NodeStatus>> getAllNodes() {
        try {
            List<NodeStatus> nodes = monitorService.getAllNodes();
            return ApiResponse.success(nodes);
        } catch (Exception e) {
            return ApiResponse.error("获取节点状态失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取单个节点详情")
    @GetMapping("/nodes/{nodeId}")
    public ApiResponse<NodeDetail> getNodeDetail(@PathVariable String nodeId) {
        try {
            NodeDetail detail = monitorService.getNodeDetail(nodeId);
            if (detail == null) {
                return ApiResponse.error("节点不存在: " + nodeId);
            }
            return ApiResponse.success(detail);
        } catch (Exception e) {
            return ApiResponse.error("获取节点详情失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取节点健康状态")
    @GetMapping("/health/{nodeId}")
    public ApiResponse<HealthStatus> getNodeHealth(@PathVariable String nodeId) {
        try {
            HealthStatus health = monitorService.getNodeHealth(nodeId);
            if (health == null) {
                return ApiResponse.error("节点不存在: " + nodeId);
            }
            return ApiResponse.success(health);
        } catch (Exception e) {
            return ApiResponse.error("获取节点健康状态失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("获取所有节点健康状态")
    @GetMapping("/health")
    public ApiResponse<List<HealthStatus>> getAllNodeHealth() {
        try {
            List<HealthStatus> healthList = monitorService.getAllNodeHealth();
            return ApiResponse.success(healthList);
        } catch (Exception e) {
            return ApiResponse.error("获取所有节点健康状态失败: " + e.getMessage());
        }
    }
    
    @ApiOperation("刷新节点状态")
    @PostMapping("/refresh")
    public ApiResponse<String> refreshNodeStatus() {
        try {
            monitorService.refreshAllNodeStatus();
            return ApiResponse.success("节点状态刷新成功");
        } catch (Exception e) {
            return ApiResponse.error("刷新节点状态失败: " + e.getMessage());
        }
    }
}
