#!/bin/bash

echo "=== Redis数据库演示 ==="
echo ""

echo "1. 连接到Redis并查看当前数据库..."
docker exec redis-standalone redis-cli -p 6379 info keyspace

echo ""
echo "2. 在db0中设置一些数据..."
docker exec redis-standalone redis-cli -p 6379 set "db0_key" "这是db0的数据"

echo ""
echo "3. 切换到db1并设置数据..."
docker exec redis-standalone redis-cli -p 6379 -n 1 set "db1_key" "这是db1的数据"

echo ""
echo "4. 切换到db2并设置数据..."
docker exec redis-standalone redis-cli -p 6379 -n 2 set "db2_key" "这是db2的数据"

echo ""
echo "5. 查看所有数据库的键..."
echo "db0的键:"
docker exec redis-standalone redis-cli -p 6379 -n 0 keys "*"

echo "db1的键:"
docker exec redis-standalone redis-cli -p 6379 -n 1 keys "*"

echo "db2的键:"
docker exec redis-standalone redis-cli -p 6379 -n 2 keys "*"

echo ""
echo "6. 查看数据库信息..."
docker exec redis-standalone redis-cli -p 6379 info keyspace

echo ""
echo "=== 在Redis Insight中查看不同数据库 ==="
echo "方法1: 在连接配置中指定数据库编号"
echo "  - 创建新连接，在Database字段输入: 0, 1, 2, 3..."
echo ""
echo "方法2: 在已连接的情况下使用SELECT命令"
echo "  - 在Redis Insight的命令行中输入: SELECT 1"
echo "  - 然后输入: SELECT 2"
echo ""
echo "方法3: 使用连接字符串指定数据库"
echo "  - redis://127.0.0.1:6379/0  (db0)"
echo "  - redis://127.0.0.1:6379/1  (db1)"
echo "  - redis://127.0.0.1:6379/2  (db2)"
