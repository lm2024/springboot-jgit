package com.redis.jedis.service;

import com.redis.jedis.config.GitRepositoryConfig;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;

/**
 * Git服务
 * 负责代码下载和更新
 */
@Service
public class GitService {
    
    @Autowired
    private GitRepositoryConfig gitConfig;
    
    @Value("${git.workspace}")
    private String workspace;
    
    @Value("${git.default-branch:master}")
    private String defaultBranch;
    
    @Value("${git.username:}")
    private String username;
    
    @Value("${git.password:}")
    private String password;
    
    /**
     * 根据项目名称克隆或拉取代码
     */
    public String cloneOrPullByProjectName(String projectName) throws Exception {
        System.out.println("Git服务: 开始处理项目 " + projectName);
        
        GitRepositoryConfig.RepositoryInfo repoInfo = gitConfig.getRepositoryByName(projectName);
        if (repoInfo == null) {
            System.err.println("Git服务错误: 未找到项目配置: " + projectName);
            throw new RuntimeException("未找到项目配置: " + projectName);
        }
        
        System.out.println("Git服务: 找到项目配置");
        System.out.println("  - URL: " + repoInfo.getUrl());
        System.out.println("  - 分支: " + repoInfo.getBranch());
        
        return cloneOrPull(repoInfo.getUrl(), repoInfo.getBranch());
    }
    
    /**
     * 克隆或拉取代码
     */
    public String cloneOrPull(String gitUrl, String branch) throws Exception {
        System.out.println("Git服务: 开始Git操作");
        System.out.println("  - Git URL: " + gitUrl);
        System.out.println("  - 分支: " + branch);
        System.out.println("  - 工作空间: " + workspace);
        
        try {
            // 确保工作空间目录存在
            File workspaceDir = new File(workspace);
            System.out.println("Git服务: 检查工作空间目录: " + workspaceDir.getAbsolutePath());
            
            if (!workspaceDir.exists()) {
                System.out.println("Git服务: 创建工作空间目录");
                Files.createDirectories(workspaceDir.toPath());
            } else {
                System.out.println("Git服务: 工作空间目录已存在");
            }
            
            String projectName = extractProjectName(gitUrl);
            System.out.println("Git服务: 提取项目名称: " + projectName);
            
            File projectDir = new File(workspaceDir, projectName);
            System.out.println("Git服务: 项目目录: " + projectDir.getAbsolutePath());
            
            if (projectDir.exists()) {
                System.out.println("Git服务: 项目目录已存在，执行拉取操作");
                return pullLatestCode(projectDir, branch);
            } else {
                System.out.println("Git服务: 项目目录不存在，执行克隆操作");
                return cloneRepository(gitUrl, branch, projectDir);
            }
        } catch (Exception e) {
            System.err.println("Git服务错误: Git操作失败");
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Git操作失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 克隆仓库
     */
    private String cloneRepository(String gitUrl, String branch, File projectDir) throws GitAPIException {
        System.out.println("Git服务: 开始克隆仓库");
        System.out.println("  - 原始URL: " + gitUrl);
        System.out.println("  - 分支: " + branch);
        System.out.println("  - 目标目录: " + projectDir.getAbsolutePath());
        
        try {
            // 构建带认证的URL
            String authUrl = buildAuthUrl(gitUrl);
            System.out.println("Git服务: 构建认证URL完成");
            
            System.out.println("Git服务: 执行Git克隆命令...");
            Git git = Git.cloneRepository()
                .setURI(authUrl)
                .setBranch(branch)
                .setDirectory(projectDir)
                .call();
            
            System.out.println("Git服务: 代码克隆成功");
            System.out.println("  - 源: " + gitUrl);
            System.out.println("  - 目标: " + projectDir.getAbsolutePath());
            
            git.close();
            return projectDir.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Git服务错误: 克隆仓库失败");
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("克隆仓库失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 拉取最新代码
     */
    private String pullLatestCode(File projectDir, String branch) throws Exception {
        System.out.println("Git服务: 开始拉取最新代码");
        System.out.println("  - 项目目录: " + projectDir.getAbsolutePath());
        System.out.println("  - 分支: " + branch);
        
        try (Git git = Git.open(projectDir)) {
            System.out.println("Git服务: 打开Git仓库成功");
            
            // 切换到指定分支
            System.out.println("Git服务: 切换到分支 " + branch);
            git.checkout()
                .setName(branch)
                .call();
            System.out.println("Git服务: 分支切换成功");
            
            // 拉取最新代码
            System.out.println("Git服务: 执行Git拉取操作...");
            PullCommand pullCommand = git.pull();
            pullCommand.call();
            
            System.out.println("Git服务: 代码拉取成功");
            System.out.println("  - 目录: " + projectDir.getAbsolutePath());
            return projectDir.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Git服务错误: 拉取代码失败");
            System.err.println("错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("拉取代码失败: " + e.getMessage(), e);
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
     * 获取所有配置的仓库信息
     */
    public java.util.List<GitRepositoryConfig.RepositoryInfo> getAllRepositories() {
        return gitConfig.getRepositories();
    }
    
    /**
     * 获取指定项目的仓库信息
     */
    public GitRepositoryConfig.RepositoryInfo getRepositoryInfo(String projectName) {
        return gitConfig.getRepositoryByName(projectName);
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

