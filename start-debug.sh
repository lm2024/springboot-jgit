#!/bin/bash

# 分布式服务监控与部署系统 - 调试启动脚本
# 用于启动Master和Agent节点进行调试

echo "=========================================="
echo "启动分布式服务监控与部署系统"
echo "=========================================="

# 检查Redis是否运行
echo "检查Redis服务..."
if ! pgrep -x "redis-server" > /dev/null; then
    echo "❌ Redis服务未运行，请先启动Redis"
    echo "启动命令: redis-server"
    exit 1
else
    echo "✅ Redis服务正在运行"
fi

# 创建必要的目录
echo "创建必要的目录..."
mkdir -p /tmp/deploy/workspace
mkdir -p /tmp/deploy/build
mkdir -p /tmp/deploy/distribute
mkdir -p /tmp/deploy/backup
mkdir -p /tmp/deploy/logs
mkdir -p /tmp/services
mkdir -p /tmp/backup
mkdir -p /tmp/shared/jars
mkdir -p /tmp/shared/storage
mkdir -p /tmp/agent/workspace
mkdir -p /tmp/agent/services
mkdir -p /tmp/agent/backup
mkdir -p /tmp/agent/logs
mkdir -p /tmp/agent-downloads
mkdir -p logs

echo "✅ 目录创建完成"

# 启动Master节点
echo "启动Master节点..."
cd master
nohup java -jar target/master-1.0.0.jar > ../logs/master.log 2>&1 &
MASTER_PID=$!
echo "Master节点PID: $MASTER_PID"

# 等待Master节点启动
echo "等待Master节点启动..."
sleep 10

# 检查Master节点是否启动成功
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ Master节点启动成功 (http://localhost:8080)"
else
    echo "❌ Master节点启动失败，请检查日志: logs/master.log"
    exit 1
fi

# 启动Agent节点
echo "启动Agent节点..."
cd ../agent
nohup java -jar target/agent-1.0.0.jar > ../logs/agent.log 2>&1 &
AGENT_PID=$!
echo "Agent节点PID: $AGENT_PID"

# 等待Agent节点启动
echo "等待Agent节点启动..."
sleep 10

# 检查Agent节点是否启动成功
if curl -s http://localhost:8082/actuator/health > /dev/null; then
    echo "✅ Agent节点启动成功 (http://localhost:8082)"
else
    echo "❌ Agent节点启动失败，请检查日志: logs/agent.log"
    exit 1
fi

echo "=========================================="
echo "系统启动完成！"
echo "=========================================="
echo "Master节点: http://localhost:8080"
echo "Agent节点:  http://localhost:8082"
echo "API文档:    http://localhost:8080/swagger-ui.html"
echo ""
echo "测试部署命令（无参数版本）:"
echo "curl -X POST 'http://localhost:8080/api/deploy/step1-download-and-build'"
echo "curl -X POST 'http://localhost:8080/api/deploy/step2-deploy-service'"
echo ""
echo "停止服务:"
echo "kill $MASTER_PID $AGENT_PID"
echo "=========================================="
