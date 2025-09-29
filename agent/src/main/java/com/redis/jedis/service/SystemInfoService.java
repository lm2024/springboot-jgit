package com.redis.jedis.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;

/**
 * 系统信息服务
 * 负责获取系统资源使用情况
 */
@Service
public class SystemInfoService {
    
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    /**
     * 获取CPU使用率
     */
    public double getCpuUsage() {
        try {
            // 使用操作系统MXBean获取CPU使用率
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                return sunOsBean.getProcessCpuLoad() * 100.0;
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("获取CPU使用率失败: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * 获取内存使用率
     */
    public double getMemoryUsage() {
        try {
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long usedMemory = totalMemory - freeMemory;
            return (double) usedMemory / totalMemory * 100.0;
        } catch (Exception e) {
            System.err.println("获取内存使用率失败: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * 获取磁盘使用率
     */
    public double getDiskUsage() {
        try {
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            return (double) usedSpace / totalSpace * 100.0;
        } catch (Exception e) {
            System.err.println("获取磁盘使用率失败: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * 获取JVM堆内存使用情况
     */
    public long getJvmHeapUsed() {
        try {
            return memoryBean.getHeapMemoryUsage().getUsed();
        } catch (Exception e) {
            System.err.println("获取JVM堆内存使用情况失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 获取JVM堆内存最大值
     */
    public long getJvmHeapMax() {
        try {
            return memoryBean.getHeapMemoryUsage().getMax();
        } catch (Exception e) {
            System.err.println("获取JVM堆内存最大值失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 获取系统总内存
     */
    public long getTotalMemory() {
        try {
            return Runtime.getRuntime().totalMemory();
        } catch (Exception e) {
            System.err.println("获取系统总内存失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 获取可用内存
     */
    public long getFreeMemory() {
        try {
            return Runtime.getRuntime().freeMemory();
        } catch (Exception e) {
            System.err.println("获取可用内存失败: " + e.getMessage());
            return 0;
        }
    }
}