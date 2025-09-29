package com.redis.jedis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志服务
 */
@Service
public class LogService {

    @Value("${deploy.agent.logs:/opt/agent/logs}")
    private String logsPath;

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 记录任务执行日志
     */
    @Async("taskExecutor")
    public void logTask(String taskId, String operation, String details) {
        String logMessage = String.format("[%s] TASK - %s - %s: %s%n", 
            LocalDateTime.now().format(TIMESTAMP_FORMAT), 
            taskId, 
            operation, 
            details);
        writeToFile("task.log", logMessage);
    }

    /**
     * 记录服务管理日志
     */
    @Async("taskExecutor")
    public void logService(String serviceName, String operation, String details) {
        String logMessage = String.format("[%s] SERVICE - %s - %s: %s%n", 
            LocalDateTime.now().format(TIMESTAMP_FORMAT), 
            serviceName, 
            operation, 
            details);
        writeToFile("service.log", logMessage);
    }

    /**
     * 记录错误日志
     */
    @Async("taskExecutor")
    public void logError(String component, String operation, String error) {
        String logMessage = String.format("[%s] ERROR - %s - %s: %s%n", 
            LocalDateTime.now().format(TIMESTAMP_FORMAT), 
            component, 
            operation, 
            error);
        writeToFile("error.log", logMessage);
    }

    /**
     * 记录系统日志
     */
    @Async("taskExecutor")
    public void logSystem(String component, String operation, String details) {
        String logMessage = String.format("[%s] SYSTEM - %s - %s: %s%n", 
            LocalDateTime.now().format(TIMESTAMP_FORMAT), 
            component, 
            operation, 
            details);
        writeToFile("system.log", logMessage);
    }

    /**
     * 写入日志文件
     */
    private void writeToFile(String fileName, String message) {
        try {
            // 确保日志目录存在
            Path logDir = Paths.get(logsPath);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // 写入日志文件
            Path logFile = logDir.resolve(fileName);
            try (FileWriter writer = new FileWriter(logFile.toFile(), true)) {
                writer.write(message);
            }
        } catch (IOException e) {
            System.err.println("写入日志文件失败: " + e.getMessage());
        }
    }
}
