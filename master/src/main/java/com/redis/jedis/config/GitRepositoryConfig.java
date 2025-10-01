package com.redis.jedis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Git仓库配置类
 * 用于管理要部署的项目仓库信息
 */
@Component
@ConfigurationProperties(prefix = "git")
public class GitRepositoryConfig {
    
    private String workspace;
    private String defaultBranch;
    private String username;
    private String password;
    private List<RepositoryInfo> repositories;
    
    public static class RepositoryInfo {
        private String name;
        private String url;
        private String branch;
        private String buildCommand;
        private String description;
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getBranch() {
            return branch;
        }
        
        public void setBranch(String branch) {
            this.branch = branch;
        }
        
        public String getBuildCommand() {
            return buildCommand;
        }
        
        public void setBuildCommand(String buildCommand) {
            this.buildCommand = buildCommand;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
            return "RepositoryInfo{" +
                    "name='" + name + '\'' +
                    ", url='" + url + '\'' +
                    ", branch='" + branch + '\'' +
                    ", buildCommand='" + buildCommand + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
    
    // Getters and Setters
    public String getWorkspace() {
        return workspace;
    }
    
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
    
    public String getDefaultBranch() {
        return defaultBranch;
    }
    
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public List<RepositoryInfo> getRepositories() {
        return repositories;
    }
    
    public void setRepositories(List<RepositoryInfo> repositories) {
        this.repositories = repositories;
    }
    
    /**
     * 根据名称获取仓库信息
     */
    public RepositoryInfo getRepositoryByName(String name) {
        if (repositories == null) {
            return null;
        }
        return repositories.stream()
                .filter(repo -> name.equals(repo.getName()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public String toString() {
        return "GitRepositoryConfig{" +
                "workspace='" + workspace + '\'' +
                ", defaultBranch='" + defaultBranch + '\'' +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", repositories=" + repositories +
                '}';
    }
}
