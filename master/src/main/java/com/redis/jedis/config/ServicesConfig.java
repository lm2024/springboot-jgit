package com.redis.jedis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 服务配置类
 * 用于管理要部署的服务信息
 */
@Component
@ConfigurationProperties(prefix = "")
public class ServicesConfig {
    
    private List<ServiceInfo> services;
    
    public static class ServiceInfo {
        private String name;
        private String jarName;
        private String jarPath;
        private List<String> targetNodes;
        private String description;
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getJarName() {
            return jarName;
        }
        
        public void setJarName(String jarName) {
            this.jarName = jarName;
        }
        
        public String getJarPath() {
            return jarPath;
        }
        
        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }
        
        public List<String> getTargetNodes() {
            return targetNodes;
        }
        
        public void setTargetNodes(List<String> targetNodes) {
            this.targetNodes = targetNodes;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "ServiceInfo{" +
                    "name='" + name + '\'' +
                    ", jarName='" + jarName + '\'' +
                    ", jarPath='" + jarPath + '\'' +
                    ", targetNodes=" + targetNodes +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
    
    // Getters and Setters
    public List<ServiceInfo> getServices() {
        return services;
    }
    
    public void setServices(List<ServiceInfo> services) {
        this.services = services;
    }
    
    /**
     * 根据名称获取服务信息
     */
    public ServiceInfo getServiceByName(String name) {
        if (services == null) {
            return null;
        }
        return services.stream()
                .filter(service -> name.equals(service.getName()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取第一个服务信息
     */
    public ServiceInfo getFirstService() {
        if (services == null || services.isEmpty()) {
            return null;
        }
        return services.get(0);
    }
    
    @Override
    public String toString() {
        return "ServicesConfig{" +
                "services=" + services +
                '}';
    }
}
