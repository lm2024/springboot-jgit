# Redis 集群 Docker Compose 配置

这个配置包含：
- 1个标准Redis实例（端口6379）- 用于Redis Insight连接
- 8个Redis主节点（端口7001-7008）
- 8个Redis从节点（端口8001-8008）
- 自动集群初始化

## 快速开始

### 启动集群
```bash
./start-redis-cluster.sh
```

### 停止集群
```bash
./stop-redis-cluster.sh
```

### 手动启动
```bash
docker-compose up -d
```

### 手动停止
```bash
docker-compose down
```

## Redis Insight 连接配置

在Redis Insight中添加连接时使用以下配置：

**连接方式1 - 连接字符串：**
```
redis://127.0.0.1:6379
```

**连接方式2 - 连接字符串（带用户名）：**
```
redis://default@127.0.0.1:6379
```

**连接方式3 - 连接字符串（指定数据库）：**
```
redis://127.0.0.1:6379/0    # db0 (默认)
redis://127.0.0.1:6379/1    # db1
redis://127.0.0.1:6379/2    # db2
```

**连接方式4 - 手动配置：**
- Host: `127.0.0.1`
- Port: `6379`
- Name: `Redis Standalone`
- Username: `default` (可选)
- Database: `0` (可选，0-15)

## Redis数据库说明

### 为什么显示db0？
Redis默认有16个数据库（编号0-15），默认连接到db0。这是Redis的标准行为。

### 如何查看不同数据库？
1. **在Redis Insight中切换数据库：**
   - 在连接配置中指定Database字段（0-15）
   - 或在命令行中使用 `SELECT 1` 切换到db1

2. **使用连接字符串指定数据库：**
   - `redis://127.0.0.1:6379/0` - 连接db0
   - `redis://127.0.0.1:6379/1` - 连接db1
   - `redis://127.0.0.1:6379/2` - 连接db2

3. **运行数据库演示：**
   ```bash
   ./test-databases.sh
   ```

## 集群连接

### 通过命令行连接集群
```bash
# 连接到集群
redis-cli -c -p 7001

# 查看集群节点
redis-cli -c -p 7001 cluster nodes

# 查看集群信息
redis-cli -c -p 7001 cluster info
```

### 通过应用程序连接集群
```bash
# 集群节点列表
127.0.0.1:7001
127.0.0.1:7002
127.0.0.1:7003
127.0.0.1:7004
127.0.0.1:7005
127.0.0.1:7006
127.0.0.1:7007
127.0.0.1:7008
```

## 端口说明

- **6379**: 标准Redis实例（用于Redis Insight）
- **7001-7008**: Redis集群主节点
- **8001-8008**: Redis集群从节点

## 注意事项

1. 此配置不启用持久化（`appendonly no --save ""`）
2. 集群会自动初始化，无需手动配置
3. 所有容器都在同一个Docker网络中
4. 适用于开发和测试环境

## 故障排除

### 检查容器状态
```bash
docker-compose ps
```

### 查看日志
```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs redis-standalone
docker-compose logs redis-cluster-init
```

### 重新初始化集群
```bash
docker-compose down
docker-compose up -d
```
