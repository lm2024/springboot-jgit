#!/bin/bash

# Agent节点启动脚本

echo "启动Agent节点..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境"
    exit 1
fi

# 创建必要的目录
mkdir -p logs
mkdir -p /opt/agent/workspace
mkdir -p /opt/agent/services
mkdir -p /opt/agent/backup
mkdir -p /opt/agent/logs
mkdir -p /tmp/agent-downloads

# 设置JVM参数
JAVA_OPTS="-Xms256m -Xmx512m -Dspring.profiles.active=agent"

# 启动Agent节点
echo "启动Agent节点 (端口: 8081)..."
cd agent
java $JAVA_OPTS -jar target/agent-1.0.0.jar

echo "Agent节点已停止"
