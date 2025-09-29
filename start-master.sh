#!/bin/bash

# Master节点启动脚本

echo "启动Master节点..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境"
    exit 1
fi

# 创建必要的目录
mkdir -p logs
mkdir -p /opt/deploy/workspace
mkdir -p /opt/deploy/build
mkdir -p /opt/deploy/distribute
mkdir -p /opt/deploy/backup
mkdir -p /opt/deploy/logs
mkdir -p /opt/git-workspace
mkdir -p /opt/shared/storage
mkdir -p /opt/http-files

# 设置JVM参数
JAVA_OPTS="-Xms512m -Xmx1024m -Dspring.profiles.active=master"

# 启动Master节点
echo "启动Master节点 (端口: 8080)..."
cd master
java $JAVA_OPTS -jar target/master-1.0.0.jar

echo "Master节点已停止"
