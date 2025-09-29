package com.redis.jedis.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.Socket;

/**
 * 健康检查服务
 * 负责检查服务健康状态
 */
@Service
public class HealthCheckService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 检查服务健康状态
     */
    public boolean checkHealth(String serviceName, String healthCheckUrl) {
        try {
            // 1. 检查服务进程是否运行
            if (!isProcessRunning(serviceName)) {
                return false;
            }
            
            // 2. 检查健康检查端点
            if (healthCheckUrl != null && !healthCheckUrl.isEmpty()) {
                try {
                    String response = restTemplate.getForObject(healthCheckUrl, String.class);
                    return response != null && response.contains("UP");
                } catch (Exception e) {
                    System.err.println("健康检查端点访问失败: " + e.getMessage());
                }
            }
            
            // 3. 检查端口是否监听
            int port = getServicePort(serviceName);
            return isPortListening(port);
            
        } catch (Exception e) {
            System.err.println("健康检查失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查进程是否运行
     */
    public boolean isProcessRunning(String serviceName) {
        try {
            Process process = Runtime.getRuntime().exec("pgrep -f " + serviceName);
            return process.waitFor() == 0;
        } catch (Exception e) {
            System.err.println("检查进程运行状态失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查端口是否监听
     */
    public boolean isPortListening(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 获取服务端口
     */
    public int getServicePort(String serviceName) {
        // 根据服务名获取端口号
        // 这里需要根据实际配置返回
        switch (serviceName) {
            case "service-a":
                return 8080;
            case "service-b":
                return 8081;
            default:
                return 8080;
        }
    }
    
    /**
     * 获取服务健康状态详情
     */
    public HealthCheckResult getHealthCheckResult(String serviceName, String healthCheckUrl) {
        HealthCheckResult result = new HealthCheckResult();
        result.setServiceName(serviceName);
        result.setCheckTime(System.currentTimeMillis());
        
        try {
            boolean isProcessRunning = isProcessRunning(serviceName);
            boolean isPortListening = isPortListening(getServicePort(serviceName));
            boolean isHealthEndpointOk = false;
            
            if (healthCheckUrl != null && !healthCheckUrl.isEmpty()) {
                try {
                    String response = restTemplate.getForObject(healthCheckUrl, String.class);
                    isHealthEndpointOk = response != null && response.contains("UP");
                } catch (Exception e) {
                    result.setErrorMessage("健康检查端点访问失败: " + e.getMessage());
                }
            }
            
            boolean isHealthy = isProcessRunning && isPortListening && isHealthEndpointOk;
            result.setHealthy(isHealthy);
            result.setProcessRunning(isProcessRunning);
            result.setPortListening(isPortListening);
            result.setHealthEndpointOk(isHealthEndpointOk);
            
            if (!isHealthy) {
                result.setErrorMessage("服务不健康");
            }
            
        } catch (Exception e) {
            result.setHealthy(false);
            result.setErrorMessage("健康检查异常: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 健康检查结果DTO
     */
    public static class HealthCheckResult {
        private String serviceName;
        private boolean healthy;
        private boolean processRunning;
        private boolean portListening;
        private boolean healthEndpointOk;
        private String errorMessage;
        private long checkTime;
        
        // Getters and Setters
        public String getServiceName() {
            return serviceName;
        }
        
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        
        public boolean isHealthy() {
            return healthy;
        }
        
        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }
        
        public boolean isProcessRunning() {
            return processRunning;
        }
        
        public void setProcessRunning(boolean processRunning) {
            this.processRunning = processRunning;
        }
        
        public boolean isPortListening() {
            return portListening;
        }
        
        public void setPortListening(boolean portListening) {
            this.portListening = portListening;
        }
        
        public boolean isHealthEndpointOk() {
            return healthEndpointOk;
        }
        
        public void setHealthEndpointOk(boolean healthEndpointOk) {
            this.healthEndpointOk = healthEndpointOk;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        
        public long getCheckTime() {
            return checkTime;
        }
        
        public void setCheckTime(long checkTime) {
            this.checkTime = checkTime;
        }
    }
}
