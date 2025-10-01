package com.redis.jedis.service;

import com.redis.jedis.dto.ServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务管理服务
 * 负责管理本地服务的启停
 */
@Service
public class ServiceManagementService {
    
    @Value("${node.id}")
    private String nodeId;
    
    @Value("${agent.work-dir:/tmp/agent}")
    private String workDir;
    
    @Autowired
    private ProcessManager processManager;
    
    @Autowired
    private HealthCheckService healthCheckService;
    
    // 服务进程映射表
    private final Map<String, Process> serviceProcesses = new ConcurrentHashMap<>();
    
    /**
     * 启动服务
     */
    public void startService(String serviceName) {
        System.out.println("ServiceMgmt[" + nodeId + "]: === 开始启动服务: " + serviceName + " ===");
        
        try {
            // 检查服务是否已经在运行
            if (isServiceRunning(serviceName)) {
                System.out.println("ServiceMgmt[" + nodeId + "]: 服务已在运行，无需重复启动");
                return;
            }
            
            // 查找服务目录
            Path servicePath = Paths.get(workDir, serviceName);
            if (!Files.exists(servicePath)) {
                throw new RuntimeException("服务目录不存在: " + servicePath);
            }
            System.out.println("ServiceMgmt[" + nodeId + "]: 服务目录: " + servicePath.toAbsolutePath());
            
            // 查找启动脚本
            Path startScript = servicePath.resolve("start.sh");
            if (!Files.exists(startScript)) {
                // 如果没有启动脚本，尝试直接启动JAR
                startServiceDirectly(serviceName, servicePath);
            } else {
                // 使用启动脚本
                startServiceWithScript(serviceName, startScript);
            }
            
            // 等待服务启动
            Thread.sleep(3000);
            
            if (isServiceRunning(serviceName)) {
                System.out.println("ServiceMgmt[" + nodeId + "]: === 服务启动成功: " + serviceName + " ===");
            } else {
                throw new RuntimeException("服务启动失败，进程未正常运行");
            }
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: === 服务启动失败: " + serviceName + " ===");
            System.err.println("ServiceMgmt[" + nodeId + "]: 错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("服务启动失败: " + e.getMessage());
        }
    }
    
    /**
     * 直接启动JAR文件
     */
    private void startServiceDirectly(String serviceName, Path servicePath) throws Exception {
        System.out.println("ServiceMgmt[" + nodeId + "]: 直接启动JAR文件...");
        
        // 查找JAR文件
        Path jarFile = servicePath.resolve(serviceName + ".jar");
        if (!Files.exists(jarFile)) {
            throw new RuntimeException("JAR文件不存在: " + jarFile);
        }
        
        System.out.println("ServiceMgmt[" + nodeId + "]: JAR文件: " + jarFile.toAbsolutePath());
        
        // 构建启动命令
        ProcessBuilder pb = new ProcessBuilder(
            "java", "-jar", jarFile.getFileName().toString()
        );
        pb.directory(servicePath.toFile());
        
        // 重定向输出到日志文件
        Path logFile = servicePath.resolve(serviceName + ".log");
        pb.redirectOutput(logFile.toFile());
        pb.redirectError(logFile.toFile());
        
        System.out.println("ServiceMgmt[" + nodeId + "]: 启动命令: " + String.join(" ", pb.command()));
        System.out.println("ServiceMgmt[" + nodeId + "]: 工作目录: " + pb.directory());
        System.out.println("ServiceMgmt[" + nodeId + "]: 日志文件: " + logFile.toAbsolutePath());
        
        // 启动进程
        Process process = pb.start();
        serviceProcesses.put(serviceName, process);
        
        // 保存PID
        savePid(serviceName, servicePath, process);
        
        System.out.println("ServiceMgmt[" + nodeId + "]: 服务进程已启动");
    }
    
    /**
     * 使用启动脚本启动服务
     */
    private void startServiceWithScript(String serviceName, Path startScript) throws Exception {
        System.out.println("ServiceMgmt[" + nodeId + "]: 使用启动脚本: " + startScript.toAbsolutePath());
        
        ProcessBuilder pb = new ProcessBuilder("bash", startScript.toString());
        pb.directory(startScript.getParent().toFile());
        
        System.out.println("ServiceMgmt[" + nodeId + "]: 启动命令: " + String.join(" ", pb.command()));
        System.out.println("ServiceMgmt[" + nodeId + "]: 工作目录: " + pb.directory());
        
        Process process = pb.start();
        
        // 等待脚本执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("启动脚本执行失败，退出码: " + exitCode);
        }
        
        System.out.println("ServiceMgmt[" + nodeId + "]: 启动脚本执行完成");
    }
    
    /**
     * 保存进程PID
     */
    private void savePid(String serviceName, Path servicePath, Process process) {
        try {
            // Java 8 兼容方式：通过反射获取PID或使用其他方法
            // 这里先跳过PID保存，因为启动脚本会处理
            System.out.println("ServiceMgmt[" + nodeId + "]: 进程已启动，PID将由启动脚本保存");
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: 保存PID失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止服务
     */
    public void stopService(String serviceName) {
        System.out.println("ServiceMgmt[" + nodeId + "]: === 开始停止服务: " + serviceName + " ===");
        
        try {
            // 检查服务是否在运行
            if (!isServiceRunning(serviceName)) {
                System.out.println("ServiceMgmt[" + nodeId + "]: 服务未运行，无需停止");
                return;
            }
            
            // 尝试优雅停止
            boolean stopped = stopServiceGracefully(serviceName);
            
            if (!stopped) {
                // 强制停止
                System.out.println("ServiceMgmt[" + nodeId + "]: 优雅停止失败，尝试强制停止...");
                stopServiceForcefully(serviceName);
            }
            
            // 清理进程映射
            serviceProcesses.remove(serviceName);
            
            System.out.println("ServiceMgmt[" + nodeId + "]: === 服务停止成功: " + serviceName + " ===");
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: === 服务停止失败: " + serviceName + " ===");
            System.err.println("ServiceMgmt[" + nodeId + "]: 错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("服务停止失败: " + e.getMessage());
        }
    }
    
    /**
     * 优雅停止服务
     */
    private boolean stopServiceGracefully(String serviceName) {
        try {
            System.out.println("ServiceMgmt[" + nodeId + "]: 尝试优雅停止服务...");
            
            // 从进程映射中获取进程
            Process process = serviceProcesses.get(serviceName);
            if (process != null && process.isAlive()) {
                System.out.println("ServiceMgmt[" + nodeId + "]: 发送SIGTERM信号...");
                process.destroy();
                
                // 等待进程结束
                boolean terminated = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
                if (terminated) {
                    System.out.println("ServiceMgmt[" + nodeId + "]: 服务优雅停止成功");
                    return true;
                }
            }
            
            // 尝试通过PID停止
            return stopServiceByPid(serviceName, false);
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: 优雅停止失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 强制停止服务
     */
    private void stopServiceForcefully(String serviceName) throws Exception {
        System.out.println("ServiceMgmt[" + nodeId + "]: 强制停止服务...");
        
        // 从进程映射中获取进程
        Process process = serviceProcesses.get(serviceName);
        if (process != null && process.isAlive()) {
            System.out.println("ServiceMgmt[" + nodeId + "]: 发送SIGKILL信号...");
            process.destroyForcibly();
            process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        // 通过PID强制停止
        stopServiceByPid(serviceName, true);
    }
    
    /**
     * 通过PID停止服务
     */
    private boolean stopServiceByPid(String serviceName, boolean force) {
        try {
            Path servicePath = Paths.get(workDir, serviceName);
            Path pidFile = servicePath.resolve(serviceName + ".pid");
            
            if (!Files.exists(pidFile)) {
                System.out.println("ServiceMgmt[" + nodeId + "]: PID文件不存在: " + pidFile);
                return false;
            }
            
            String pidStr = new String(Files.readAllBytes(pidFile)).trim();
            System.out.println("ServiceMgmt[" + nodeId + "]: 读取PID: " + pidStr);
            
            String signal = force ? "-9" : "-15";
            ProcessBuilder pb = new ProcessBuilder("kill", signal, pidStr);
            Process killProcess = pb.start();
            int exitCode = killProcess.waitFor();
            
            if (exitCode == 0) {
                System.out.println("ServiceMgmt[" + nodeId + "]: kill命令执行成功");
                // 删除PID文件
                Files.deleteIfExists(pidFile);
                return true;
            } else {
                System.err.println("ServiceMgmt[" + nodeId + "]: kill命令执行失败，退出码: " + exitCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: 通过PID停止服务失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 重启服务
     */
    public void restartService(String serviceName) {
        System.out.println("ServiceMgmt[" + nodeId + "]: === 开始重启服务: " + serviceName + " ===");
        
        try {
            // 先停止服务
            stopService(serviceName);
            
            // 等待一段时间
            Thread.sleep(2000);
            
            // 再启动服务
            startService(serviceName);
            
            System.out.println("ServiceMgmt[" + nodeId + "]: === 服务重启成功: " + serviceName + " ===");
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: === 服务重启失败: " + serviceName + " ===");
            System.err.println("ServiceMgmt[" + nodeId + "]: 错误详情: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("服务重启失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取服务状态
     */
    public ServiceStatus getServiceStatus(String serviceName) {
        System.out.println("ServiceMgmt[" + nodeId + "]: 获取服务状态: " + serviceName);
        
        try {
            ServiceStatus status = new ServiceStatus();
            status.setServiceName(serviceName);
            status.setNodeId(nodeId);
            status.setCheckTime(System.currentTimeMillis());
            
            if (isServiceRunning(serviceName)) {
                status.setStatus("RUNNING");
                status.setHealthy(true);
                status.setMessage("服务正常运行");
            } else {
                status.setStatus("STOPPED");
                status.setHealthy(false);
                status.setMessage("服务已停止");
            }
            
            return status;
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: 获取服务状态失败: " + e.getMessage());
            
            ServiceStatus status = new ServiceStatus();
            status.setServiceName(serviceName);
            status.setNodeId(nodeId);
            status.setStatus("ERROR");
            status.setHealthy(false);
            status.setMessage("获取状态失败: " + e.getMessage());
            status.setCheckTime(System.currentTimeMillis());
            
            return status;
        }
    }
    
    /**
     * 获取所有服务状态
     */
    public List<ServiceStatus> getAllServices() {
        System.out.println("ServiceMgmt[" + nodeId + "]: 获取所有服务状态...");
        
        List<ServiceStatus> statusList = new ArrayList<>();
        
        try {
            Path workDirPath = Paths.get(workDir);
            if (!Files.exists(workDirPath)) {
                System.out.println("ServiceMgmt[" + nodeId + "]: 工作目录不存在: " + workDirPath);
                return statusList;
            }
            
            // 扫描工作目录中的服务
            Files.list(workDirPath)
                .filter(Files::isDirectory)
                .forEach(servicePath -> {
                    String serviceName = servicePath.getFileName().toString();
                    ServiceStatus status = getServiceStatus(serviceName);
                    statusList.add(status);
                });
            
            System.out.println("ServiceMgmt[" + nodeId + "]: 找到 " + statusList.size() + " 个服务");
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: 获取所有服务状态失败: " + e.getMessage());
        }
        
        return statusList;
    }
    
    /**
     * 恢复服务
     */
    public void recoverServices() {
        System.out.println("ServiceMgmt[" + nodeId + "]: === 开始恢复服务 ===");
        
        try {
            List<ServiceStatus> services = getAllServices();
            for (ServiceStatus service : services) {
                if (!"RUNNING".equals(service.getStatus())) {
                    System.out.println("ServiceMgmt[" + nodeId + "]: 尝试恢复服务: " + service.getServiceName());
                    try {
                        startService(service.getServiceName());
                    } catch (Exception e) {
                        System.err.println("ServiceMgmt[" + nodeId + "]: 恢复服务失败: " + service.getServiceName() + " - " + e.getMessage());
                    }
                }
            }
            
            System.out.println("ServiceMgmt[" + nodeId + "]: === 服务恢复完成 ===");
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: === 服务恢复失败 ===");
            System.err.println("ServiceMgmt[" + nodeId + "]: 错误详情: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查服务是否运行
     */
    public boolean isServiceRunning(String serviceName) {
        try {
            // 方法1: 检查进程映射中的进程
            Process process = serviceProcesses.get(serviceName);
            if (process != null && process.isAlive()) {
                return true;
            }
            
            // 方法2: 通过PID文件检查
            Path servicePath = Paths.get(workDir, serviceName);
            Path pidFile = servicePath.resolve(serviceName + ".pid");
            
            if (Files.exists(pidFile)) {
                String pidStr = new String(Files.readAllBytes(pidFile)).trim();
                
                // 检查进程是否存在
                ProcessBuilder pb = new ProcessBuilder("kill", "-0", pidStr);
                Process checkProcess = pb.start();
                int exitCode = checkProcess.waitFor();
                
                if (exitCode == 0) {
                    return true;
                } else {
                    // PID文件存在但进程不存在，清理PID文件
                    Files.deleteIfExists(pidFile);
                }
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println("ServiceMgmt[" + nodeId + "]: 检查服务运行状态失败: " + e.getMessage());
            return false;
        }
    }
}
