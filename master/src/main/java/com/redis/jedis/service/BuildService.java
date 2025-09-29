package com.redis.jedis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Maven构建服务
 */
@Service
public class BuildService {


    /**
     * 执行Maven构建
     * @param projectPath 项目路径
     * @param buildCommand 构建命令
     * @return 构建结果
     */
    public BuildResult build(String projectPath, String buildCommand) {
        BuildResult result = new BuildResult();
        result.setProjectPath(projectPath);
        result.setBuildCommand(buildCommand);
        result.setStartTime(System.currentTimeMillis());

        try {
            // 确保项目目录存在
            Path projectDir = Paths.get(projectPath);
            if (!Files.exists(projectDir)) {
                result.setSuccess(false);
                result.setErrorMessage("项目目录不存在: " + projectPath);
                return result;
            }

            // 检查pom.xml是否存在
            Path pomFile = projectDir.resolve("pom.xml");
            if (!Files.exists(pomFile)) {
                result.setSuccess(false);
                result.setErrorMessage("pom.xml文件不存在: " + pomFile);
                return result;
            }

            // 执行Maven命令
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(projectDir.toFile());
            
            // 根据操作系统选择Maven命令
            String mavenCmd = getMavenCommand();
            List<String> command = new ArrayList<>();
            command.add(mavenCmd);
            
            if (StringUtils.hasText(buildCommand)) {
                command.addAll(parseBuildCommand(buildCommand));
            } else {
                command.add("clean");
                command.add("package");
                command.add("-DskipTests");
            }

            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            result.setExitCode(exitCode);
            result.setOutput(output.toString());
            result.setEndTime(System.currentTimeMillis());

            if (exitCode == 0) {
                result.setSuccess(true);
                // 查找生成的JAR文件
                String jarPath = findGeneratedJar(projectPath);
                result.setJarPath(jarPath);
            } else {
                result.setSuccess(false);
                result.setErrorMessage("Maven构建失败，退出码: " + exitCode);
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("构建过程中发生异常: " + e.getMessage());
            result.setEndTime(System.currentTimeMillis());
        }

        return result;
    }

    /**
     * 获取Maven命令
     */
    private String getMavenCommand() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            return "mvn.cmd";
        } else {
            return "mvn";
        }
    }

    /**
     * 解析构建命令
     */
    private List<String> parseBuildCommand(String buildCommand) {
        List<String> commands = new ArrayList<>();
        String[] parts = buildCommand.split("\\s+");
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                commands.add(part.trim());
            }
        }
        return commands;
    }

    /**
     * 查找生成的JAR文件
     */
    private String findGeneratedJar(String projectPath) {
        try {
            Path targetDir = Paths.get(projectPath, "target");
            if (!Files.exists(targetDir)) {
                return null;
            }

            return Files.walk(targetDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> !path.toString().contains("original"))
                    .filter(path -> !path.toString().contains("sources"))
                    .map(Path::toString)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建结果
     */
    public static class BuildResult {
        private String projectPath;
        private String buildCommand;
        private boolean success;
        private String errorMessage;
        private int exitCode;
        private String output;
        private String jarPath;
        private long startTime;
        private long endTime;

        // Getters and Setters
        public String getProjectPath() { return projectPath; }
        public void setProjectPath(String projectPath) { this.projectPath = projectPath; }

        public String getBuildCommand() { return buildCommand; }
        public void setBuildCommand(String buildCommand) { this.buildCommand = buildCommand; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public int getExitCode() { return exitCode; }
        public void setExitCode(int exitCode) { this.exitCode = exitCode; }

        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }

        public String getJarPath() { return jarPath; }
        public void setJarPath(String jarPath) { this.jarPath = jarPath; }

        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }

        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }

        public long getDuration() {
            return endTime - startTime;
        }
    }
}
