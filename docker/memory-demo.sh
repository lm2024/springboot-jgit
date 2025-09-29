#!/bin/bash

echo "=== Redis内存使用演示 ==="
echo ""

echo "1. 查看Redis内存使用情况..."
docker exec redis-standalone redis-cli -p 6379 info memory | grep -E "(used_memory|used_memory_human|maxmemory)"

echo ""
echo "2. 在db0中存储大量数据..."
# 存储1000个键值对
for i in {1..1000}; do
    docker exec redis-standalone redis-cli -p 6379 -n 0 set "db0_key_$i" "这是db0的第$i个数据，内容比较长，用来测试内存使用情况"
done

echo ""
echo "3. 查看db0存储后的内存使用..."
docker exec redis-standalone redis-cli -p 6379 info memory | grep -E "(used_memory|used_memory_human|maxmemory)"

echo ""
echo "4. 在db1中存储相同数量的数据..."
for i in {1..1000}; do
    docker exec redis-standalone redis-cli -p 6379 -n 1 set "db1_key_$i" "这是db1的第$i个数据，内容比较长，用来测试内存使用情况"
done

echo ""
echo "5. 查看db1存储后的内存使用..."
docker exec redis-standalone redis-cli -p 6379 info memory | grep -E "(used_memory|used_memory_human|maxmemory)"

echo ""
echo "6. 查看各数据库的键数量..."
echo "db0键数量: $(docker exec redis-standalone redis-cli -p 6379 -n 0 dbsize)"
echo "db1键数量: $(docker exec redis-standalone redis-cli -p 6379 -n 1 dbsize)"
echo "db2键数量: $(docker exec redis-standalone redis-cli -p 6379 -n 2 dbsize)"

echo ""
echo "7. 查看详细的内存信息..."
docker exec redis-standalone redis-cli -p 6379 info memory

echo ""
echo "=== 内存分配说明 ==="
echo "1. Redis的16个数据库共享同一块内存空间"
echo "2. 不是平均分配，而是按需分配"
echo "3. 所有数据库的数据都存储在同一个内存池中"
echo "4. 可以全部放在db0中，也可以分散到多个数据库"
echo "5. 内存使用总量 = 所有数据库的数据总和"
