package com.redis.jedis.service;

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
        System.out.println("构建服务: 开始Maven构建");
        System.out.println("  - 项目路径: " + projectPath);
        System.out.println("  - 构建命令: " + buildCommand);
        
        BuildResult result = new BuildResult();
        result.setProjectPath(projectPath);
        result.setBuildCommand(buildCommand);
        result.setStartTime(System.currentTimeMillis());

        try {
            // 确保项目目录存在
            Path projectDir = Paths.get(projectPath);
            System.out.println("构建服务: 检查项目目录: " + projectDir.toAbsolutePath());
            
            if (!Files.exists(projectDir)) {
                System.err.println("构建服务错误: 项目目录不存在: " + projectPath);
                result.setSuccess(false);
                result.setErrorMessage("项目目录不存在: " + projectPath);
                return result;
            }
            System.out.println("构建服务: 项目目录存在");

            // 检查pom.xml是否存在
            Path pomFile = projectDir.resolve("pom.xml");
            System.out.println("构建服务: 检查pom.xml文件: " + pomFile.toAbsolutePath());
            
            if (!Files.exists(pomFile)) {
                System.err.println("构建服务错误: pom.xml文件不存在: " + pomFile);
                result.setSuccess(false);
                result.setErrorMessage("pom.xml文件不存在: " + pomFile);
                return result;
            }
            System.out.println("构建服务: pom.xml文件存在");

            // 执行Maven命令
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(projectDir.toFile());
            
            // 根据操作系统选择Maven命令
            String mavenCmd = getMavenCommand();
            System.out.println("构建服务: Maven命令: " + mavenCmd);
            
            List<String> command = new ArrayList<>();
            command.add(mavenCmd);
            
            if (StringUtils.hasText(buildCommand)) {
                command.addAll(parseBuildCommand(buildCommand));
            } else {
                command.add("clean");
                command.add("package");
                command.add("-DskipTests");
            }

            System.out.println("构建服务: 完整命令: " + String.join(" ", command));
            processBuilder.command(command);
            processBuilder.redirectErrorStream(true);

            System.out.println("构建服务: 启动Maven进程...");
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    // 实时打印构建输出
                    System.out.println("Maven: " + line);
                }
            }

            System.out.println("构建服务: 等待Maven进程完成...");
            int exitCode = process.waitFor();
            result.setExitCode(exitCode);
            result.setOutput(output.toString());
            result.setEndTime(System.currentTimeMillis());

            System.out.println("构建服务: Maven进程完成");
            System.out.println("  - 退出码: " + exitCode);
            System.out.println("  - 耗时: " + result.getDuration() + "ms");

            if (exitCode == 0) {
                System.out.println("构建服务: Maven构建成功，查找生成的JAR文件...");
                result.setSuccess(true);
                // 查找生成的JAR文件
                String jarPath = findGeneratedJar(projectPath);
                result.setJarPath(jarPath);
                
                if (jarPath != null) {
                    System.out.println("构建服务: 找到JAR文件: " + jarPath);
                } else {
                    System.err.println("构建服务警告: 未找到生成的JAR文件");
                }
            } else {
                System.err.println("构建服务错误: Maven构建失败");
                System.err.println("退出码: " + exitCode);
                System.err.println("输出: " + output.toString());
                result.setSuccess(false);
                result.setErrorMessage("Maven构建失败，退出码: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("构建服务错误: 构建过程中发生异常");
            System.err.println("异常详情: " + e.getMessage());
            e.printStackTrace();
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
                String trimmedPart = part.trim();
                // 跳过命令中的 mvn 部分，因为我们已经添加了 Maven 命令
                if (!trimmedPart.equals("mvn") && !trimmedPart.equals("mvn.cmd")) {
                    commands.add(trimmedPart);
                }
            }
        }
        return commands;
    }

    /**
     * 查找生成的JAR文件
     */
    private String findGeneratedJar(String projectPath) {
        System.out.println("构建服务: 查找生成的JAR文件");
        System.out.println("  - 项目路径: " + projectPath);
        
        try {
            Path targetDir = Paths.get(projectPath, "target");
            System.out.println("  - target目录: " + targetDir.toAbsolutePath());
            
            if (!Files.exists(targetDir)) {
                System.err.println("构建服务错误: target目录不存在: " + targetDir);
                return null;
            }
            System.out.println("构建服务: target目录存在");

            System.out.println("构建服务: 扫描target目录中的JAR文件...");
            java.util.List<String> allJars = new java.util.ArrayList<>();
            java.util.List<String> filteredJars = new java.util.ArrayList<>();
            
            Files.walk(targetDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .forEach(path -> {
                        String jarPath = path.toString();
                        allJars.add(jarPath);
                        System.out.println("  - 找到JAR: " + jarPath);
                        
                        if (!jarPath.contains("original") && !jarPath.contains("sources")) {
                            filteredJars.add(jarPath);
                            System.out.println("    ✓ 符合条件");
                        } else {
                            System.out.println("    ✗ 跳过（original或sources）");
                        }
                    });
            
            System.out.println("构建服务: JAR文件扫描完成");
            System.out.println("  - 总共找到: " + allJars.size() + " 个JAR文件");
            System.out.println("  - 符合条件: " + filteredJars.size() + " 个JAR文件");
            
            if (filteredJars.isEmpty()) {
                System.err.println("构建服务错误: 未找到符合条件的JAR文件");
                return null;
            }
            
            String selectedJar = filteredJars.get(0);
            System.out.println("构建服务: 选择JAR文件: " + selectedJar);
            return selectedJar;
            
        } catch (Exception e) {
            System.err.println("构建服务错误: 查找JAR文件时发生异常");
            System.err.println("异常详情: " + e.getMessage());
            e.printStackTrace();
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
