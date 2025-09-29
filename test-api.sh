#!/bin/bash

# API测试脚本

MASTER_URL="http://localhost:8080"
AGENT_URL="http://localhost:8081"

echo "=== 分布式服务监控与部署系统 API测试 ==="

# 测试Master节点健康检查
echo "1. 测试Master节点健康检查..."
curl -s "$MASTER_URL/api/monitor/nodes" | jq '.' || echo "Master节点未启动或响应异常"

echo -e "\n2. 测试Agent节点健康检查..."
curl -s "$AGENT_URL/api/agent/status" | jq '.' || echo "Agent节点未启动或响应异常"

# 测试服务列表
echo -e "\n3. 测试获取服务列表..."
curl -s "$MASTER_URL/api/deploy/services" | jq '.' || echo "获取服务列表失败"

# 测试Git构建
echo -e "\n4. 测试Git构建..."
curl -s -X POST "$MASTER_URL/api/deploy/build-from-git" \
  -d "gitUrl=https://github.com/spring-projects/spring-boot.git" \
  -d "branch=main" \
  -d "projectName=spring-boot-demo" \
  -d "buildCommand=clean package -DskipTests" | jq '.' || echo "Git构建测试失败"

# 测试文件上传
echo -e "\n5. 测试文件上传..."
echo "请手动上传JAR文件进行测试"

echo -e "\n=== API测试完成 ==="
