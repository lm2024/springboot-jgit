package com.redis.jedis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 目录管理服务
 */
@Service
public class DirectoryService {

    @Value("${deploy.master.workspace:/opt/deploy/workspace}")
    private String workspacePath;

    @Value("${deploy.master.build:/opt/deploy/build}")
    private String buildPath;

    @Value("${deploy.master.distribute:/opt/deploy/distribute}")
    private String distributePath;

    @Value("${deploy.master.backup:/opt/deploy/backup}")
    private String backupPath;

    @Value("${deploy.master.logs:/opt/deploy/logs}")
    private String logsPath;

    /**
     * 初始化时创建必要的目录
     */
    @PostConstruct
    public void initDirectories() {
        createDirectoryIfNotExists(workspacePath);
        createDirectoryIfNotExists(buildPath);
        createDirectoryIfNotExists(distributePath);
        createDirectoryIfNotExists(backupPath);
        createDirectoryIfNotExists(logsPath);
    }

    /**
     * 创建目录（如果不存在）
     */
    public void createDirectoryIfNotExists(String path) {
        try {
            Path dir = Paths.get(path);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                System.out.println("创建目录: " + path);
            }
        } catch (IOException e) {
            System.err.println("创建目录失败: " + path + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 获取工作空间路径
     */
    public String getWorkspacePath() {
        return workspacePath;
    }

    /**
     * 获取构建路径
     */
    public String getBuildPath() {
        return buildPath;
    }

    /**
     * 获取分发路径
     */
    public String getDistributePath() {
        return distributePath;
    }

    /**
     * 获取备份路径
     */
    public String getBackupPath() {
        return backupPath;
    }

    /**
     * 获取日志路径
     */
    public String getLogsPath() {
        return logsPath;
    }

    /**
     * 获取项目工作空间路径
     */
    public String getProjectWorkspacePath(String projectName) {
        String projectPath = Paths.get(workspacePath, projectName).toString();
        createDirectoryIfNotExists(projectPath);
        return projectPath;
    }

    /**
     * 获取项目构建路径
     */
    public String getProjectBuildPath(String projectName) {
        String projectPath = Paths.get(buildPath, projectName).toString();
        createDirectoryIfNotExists(projectPath);
        return projectPath;
    }

    /**
     * 获取项目分发路径
     */
    public String getProjectDistributePath(String projectName) {
        String projectPath = Paths.get(distributePath, projectName).toString();
        createDirectoryIfNotExists(projectPath);
        return projectPath;
    }

    /**
     * 获取项目备份路径
     */
    public String getProjectBackupPath(String projectName) {
        String projectPath = Paths.get(backupPath, projectName).toString();
        createDirectoryIfNotExists(projectPath);
        return projectPath;
    }

    /**
     * 获取项目日志路径
     */
    public String getProjectLogsPath(String projectName) {
        String projectPath = Paths.get(logsPath, projectName).toString();
        createDirectoryIfNotExists(projectPath);
        return projectPath;
    }
}
