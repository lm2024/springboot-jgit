#!/bin/bash

echo "启动Redis集群..."
docker-compose -f docker-compose.yml up -d

echo "等待集群初始化完成..."
sleep 30

echo "检查标准Redis实例状态..."
docker exec redis-standalone redis-cli -p 6379 ping

echo "检查集群状态..."
docker exec redis-master-1 redis-cli -p 7001 cluster nodes

echo ""
echo "Redis集群已启动！"
echo ""
echo "=== 连接信息 ==="
echo "标准Redis实例 (用于Redis Insight):"
echo "  连接字符串: redis://127.0.0.1:6379"
echo "  或: redis://default@127.0.0.1:6379"
echo ""
echo "Redis集群:"
echo "  主节点端口: 7001-7008"
echo "  从节点端口: 8001-8008"
echo "  集群连接: redis-cli -c -p 7001"
echo ""
echo "Redis Insight 配置:"
echo "  Host: 127.0.0.1"
echo "  Port: 6379"
echo "  Name: Redis Standalone"
