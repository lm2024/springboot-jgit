package com.redis.jedis.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Git服务
 * 负责代码下载和更新
 */
@Service
public class GitService {
    
    @Value("${deploy.git.workspace}")
    private String workspace;
    
    @Value("${deploy.git.default-branch:master}")
    private String defaultBranch;
    
    @Value("${deploy.git.username:}")
    private String username;
    
    @Value("${deploy.git.password:}")
    private String password;
    
    /**
     * 克隆或拉取代码
     */
    public String cloneOrPull(String gitUrl, String branch) throws Exception {
        try {
            // 确保工作空间目录存在
            File workspaceDir = new File(workspace);
            if (!workspaceDir.exists()) {
                Files.createDirectories(workspaceDir.toPath());
            }
            
            String projectName = extractProjectName(gitUrl);
            File projectDir = new File(workspaceDir, projectName);
            
            if (projectDir.exists()) {
                // 拉取最新代码
                return pullLatestCode(projectDir, branch);
            } else {
                // 克隆代码
                return cloneRepository(gitUrl, branch, projectDir);
            }
        } catch (Exception e) {
            throw new RuntimeException("Git操作失败: " + e.getMessage());
        }
    }
    
    /**
     * 克隆仓库
     */
    private String cloneRepository(String gitUrl, String branch, File projectDir) throws GitAPIException {
        try {
            // 构建带认证的URL
            String authUrl = buildAuthUrl(gitUrl);
            
            Git.cloneRepository()
                .setURI(authUrl)
                .setBranch(branch)
                .setDirectory(projectDir)
                .call();
            
            System.out.println("代码克隆成功: " + gitUrl + " -> " + projectDir.getAbsolutePath());
            return projectDir.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("克隆仓库失败: " + e.getMessage());
        }
    }
    
    /**
     * 拉取最新代码
     */
    private String pullLatestCode(File projectDir, String branch) throws Exception {
        try (Git git = Git.open(projectDir)) {
            // 切换到指定分支
            git.checkout()
                .setName(branch)
                .call();
            
            // 拉取最新代码
            PullCommand pullCommand = git.pull();
            pullCommand.call();
            
            System.out.println("代码拉取成功: " + projectDir.getAbsolutePath());
            return projectDir.getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException("拉取代码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目信息
     */
    public GitProjectInfo getProjectInfo(String projectPath) {
        try {
            File projectDir = new File(projectPath);
            if (!projectDir.exists()) {
                return null;
            }
            
            try (Git git = Git.open(projectDir)) {
                Repository repository = git.getRepository();
                
                GitProjectInfo info = new GitProjectInfo();
                info.setProjectPath(projectPath);
                info.setProjectName(projectDir.getName());
                info.setCurrentBranch(repository.getBranch());
                info.setRemoteUrl(repository.getConfig().getString("remote", "origin", "url"));
                info.setLastCommitId(repository.resolve("HEAD").name());
                info.setLastCommitTime(repository.parseCommit(repository.resolve("HEAD")).getCommitTime());
                
                return info;
            }
        } catch (Exception e) {
            System.err.println("获取项目信息失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查代码是否有更新
     */
    public boolean hasUpdates(String projectPath) {
        try {
            File projectDir = new File(projectPath);
            if (!projectDir.exists()) {
                return false;
            }
            
            try (Git git = Git.open(projectDir)) {
                // 获取远程更新
                git.fetch().call();
                
                // 检查是否有新的提交
                String localCommit = git.getRepository().resolve("HEAD").name();
                String remoteCommit = git.getRepository().resolve("origin/" + defaultBranch).name();
                
                return !localCommit.equals(remoteCommit);
            }
        } catch (Exception e) {
            System.err.println("检查代码更新失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取提交历史
     */
    public String getCommitHistory(String projectPath, int limit) {
        try {
            File projectDir = new File(projectPath);
            if (!projectDir.exists()) {
                return "";
            }
            
            try (Git git = Git.open(projectDir)) {
                StringBuilder history = new StringBuilder();
                git.log()
                    .setMaxCount(limit)
                    .call()
                    .forEach(commit -> {
                        history.append("Commit: ").append(commit.getName())
                               .append(" - ").append(commit.getShortMessage())
                               .append(" (").append(commit.getAuthorIdent().getWhen())
                               .append(")\n");
                    });
                
                return history.toString();
            }
        } catch (Exception e) {
            System.err.println("获取提交历史失败: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 提取项目名称
     */
    private String extractProjectName(String gitUrl) {
        String name = gitUrl;
        if (name.endsWith(".git")) {
            name = name.substring(0, name.length() - 4);
        }
        if (name.contains("/")) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        return name;
    }
    
    /**
     * Git项目信息DTO
     */
    public static class GitProjectInfo {
        private String projectPath;
        private String projectName;
        private String currentBranch;
        private String remoteUrl;
        private String lastCommitId;
        private int lastCommitTime;
        
        // Getters and Setters
        public String getProjectPath() {
            return projectPath;
        }
        
        public void setProjectPath(String projectPath) {
            this.projectPath = projectPath;
        }
        
        public String getProjectName() {
            return projectName;
        }
        
        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }
        
        public String getCurrentBranch() {
            return currentBranch;
        }
        
        public void setCurrentBranch(String currentBranch) {
            this.currentBranch = currentBranch;
        }
        
        public String getRemoteUrl() {
            return remoteUrl;
        }
        
        public void setRemoteUrl(String remoteUrl) {
            this.remoteUrl = remoteUrl;
        }
        
        public String getLastCommitId() {
            return lastCommitId;
        }
        
        public void setLastCommitId(String lastCommitId) {
            this.lastCommitId = lastCommitId;
        }
        
        public int getLastCommitTime() {
            return lastCommitTime;
        }
        
        public void setLastCommitTime(int lastCommitTime) {
            this.lastCommitTime = lastCommitTime;
        }
    }
    
    /**
     * 构建带认证的URL
     */
    private String buildAuthUrl(String gitUrl) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            try {
                java.net.URL url = new java.net.URL(gitUrl);
                String protocol = url.getProtocol();
                String host = url.getHost();
                int port = url.getPort();
                String path = url.getPath();
                
                String authUrl = protocol + "://" + username + ":" + password + "@" + host;
                if (port != -1) {
                    authUrl += ":" + port;
                }
                authUrl += path;
                
                return authUrl;
            } catch (Exception e) {
                System.err.println("构建认证URL失败，使用原始URL: " + e.getMessage());
                return gitUrl;
            }
        }
        return gitUrl;
    }
}

