#!/bin/bash

# 分布式服务监控与部署系统 - 快速测试脚本（无参数版本）
# 所有接口都支持不传参数，自动从application.yml配置中读取
# 配置来源：
# - 服务信息：从services配置中读取
# - 项目信息：从git.repositories配置中读取
# - 目标节点：从services.target-nodes配置中读取

echo "=========================================="
echo "开始快速测试（无参数版本）"
echo "=========================================="
echo "配置信息（从application.yml读取）："
echo "- 服务名称：从services配置中读取"
echo "- JAR包路径：从services.jar-path配置中读取"
echo "- 目标节点：从services.target-nodes配置中读取"
echo "- Git仓库：从git.repositories配置中读取"
echo "=========================================="

# 检查服务是否启动
echo "检查服务状态..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ Master节点未启动，请先启动Master节点"
    exit 1
fi

if ! curl -s http://localhost:8082/actuator/health > /dev/null; then
    echo "❌ Agent节点未启动，请先启动Agent节点"
    exit 1
fi

echo "✅ 服务状态正常"

# ===========================================
# 第一阶段：代码下载和构建
# ===========================================
echo ""
echo "步骤1：代码下载和构建..."
echo "----------------------------------------"

# 方式1：使用配置的Git仓库（推荐）
echo "测试1.1：从配置的Git仓库下载和构建（无参数）"
RESPONSE1=$(curl -s -X POST "http://localhost:8080/api/deploy/step1-download-and-build")
echo "结果: $RESPONSE1"
echo ""

# 方式2：手动指定Git仓库（无参数）
echo "测试1.2：手动指定Git仓库下载和构建（无参数）"
RESPONSE2=$(curl -s -X POST "http://localhost:8080/api/deploy/step1-manual-download-and-build")
echo "结果: $RESPONSE2"
echo ""

# ===========================================
# 第二阶段：服务部署
# ===========================================
echo "步骤2：服务部署..."
echo "----------------------------------------"

# 方式1：部署单个服务（无参数）
echo "测试2.1：部署单个服务（无参数）"
RESPONSE3=$(curl -s -X POST "http://localhost:8080/api/deploy/step2-deploy-service" \
  -H "Content-Type: application/json")
echo "结果: $RESPONSE3"
echo ""

# 方式2：批量部署所有服务（无参数）
echo "测试2.2：批量部署所有服务（无参数）"
RESPONSE4=$(curl -s -X POST "http://localhost:8080/api/deploy/step2-deploy-all" \
  -H "Content-Type: application/json")
echo "结果: $RESPONSE4"
echo ""

# 方式3：部署选定服务（无参数）
echo "测试2.3：部署选定服务（无参数）"
RESPONSE5=$(curl -s -X POST "http://localhost:8080/api/deploy/step2-deploy-selected" \
  -H "Content-Type: application/json")
echo "结果: $RESPONSE5"
echo ""

# ===========================================
# 第三阶段：状态监控
# ===========================================
echo "步骤3：状态监控..."
echo "----------------------------------------"

# 查看所有可用服务（无参数）
echo "测试3.1：查看所有可用服务（无参数）"
RESPONSE6=$(curl -s -X GET "http://localhost:8080/api/deploy/step3-available-services")
echo "结果: $RESPONSE6"
echo ""

# 查看批量部署状态（无参数）
echo "测试3.2：查看批量部署状态（无参数）"
RESPONSE7=$(curl -s -X GET "http://localhost:8080/api/deploy/step3-batch-status")
echo "结果: $RESPONSE7"
echo ""

# ===========================================
# 第四阶段：服务管理
# ===========================================
echo "步骤4：服务管理..."
echo "----------------------------------------"

# 回滚所有服务（无参数）
echo "测试4.1：回滚所有服务（无参数）"
RESPONSE8=$(curl -s -X POST "http://localhost:8080/api/deploy/step4-rollback-all" \
  -H "Content-Type: application/json")
echo "结果: $RESPONSE8"
echo ""

# 回滚选定服务（无参数）
echo "测试4.2：回滚选定服务（无参数）"
RESPONSE9=$(curl -s -X POST "http://localhost:8080/api/deploy/step4-rollback-selected" \
  -H "Content-Type: application/json")
echo "结果: $RESPONSE9"
echo ""

# 查看回滚历史（无参数）
echo "测试4.3：查看回滚历史（无参数）"
RESPONSE10=$(curl -s -X GET "http://localhost:8080/api/deploy/step4-rollback-history")
echo "结果: $RESPONSE10"
echo ""

# ===========================================
# 测试完成
# ===========================================
echo "=========================================="
echo "快速测试完成！"
echo "=========================================="
echo ""
echo "所有接口都支持无参数调用，自动从application.yml配置中读取："
echo "- 项目名称：从git.repositories配置中读取"
echo "- 目标节点：从services.target-nodes配置中读取"
echo "- 部署配置：使用默认的JVM参数和端口"
echo ""
echo "如果需要自定义参数，可以在调用时传入相应的参数。"
echo "=========================================="
