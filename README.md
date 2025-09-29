# springboot-jgit

基于 Spring Boot 和 Redis 的半自动化运维示例，包含分布式服务监控、自动部署、回滚等功能，支持简单场景的主控（Master）与执行节点（Agent）协作。

## 系统架构

- **Master节点**: 负责服务监控、部署管理、任务调度
- **Agent节点**: 负责服务执行、状态报告、任务处理
- **Redis集群**: 负责消息传递、状态存储、任务队列

## 功能特性

### 监控功能
- 实时监控服务健康状态
- 系统资源监控（CPU、内存、磁盘）
- 服务运行状态跟踪
- 部署历史记录

### 部署功能
- 支持JAR包上传部署
- 支持从Git仓库自动构建部署
- 支持批量部署和选择性部署
- 支持服务回滚和版本管理

### 文件分发
- 共享存储分发（推荐用于大文件）
- HTTP服务器分发（支持断点续传）
- Redis消息队列（小文件协调）

## 快速开始

### 环境要求
- Java 8+
- Maven 3.6+
- Redis 4.x集群
- Git（用于代码构建）

### 配置说明

#### Master 节点配置
```yaml
server:
  port: 8080

redis:
  cluster:
    nodes: 
      - 192.168.1.10:6379
      - 192.168.1.11:6379
      - 192.168.1.12:6379
    password: your_password

deploy:
  master:
    workspace: /opt/deploy/workspace
    build: /opt/deploy/build
    distribute: /opt/deploy/distribute
    backup: /opt/deploy/backup
    logs: /opt/deploy/logs
```

#### Agent 节点配置
```yaml
server:
  port: 8081

node:
  id: node001
  name: Agent Node 001

deploy:
  agent:
    workspace: /opt/agent/workspace
    services: /opt/agent/services
    backup: /opt/agent/backup
    logs: /opt/agent/logs
```

### 启动系统

1. **启动Redis集群**
```bash
# 确保Redis集群正常运行
redis-cli -c -h 192.168.1.10 -p 6379 ping
```

2. **启动Master节点**
```bash
./start-master.sh
```

3. **启动Agent节点**
```bash
./start-agent.sh
```

4. **测试API**
```bash
./test-api.sh
```

## API 接口

### Master 节点 API

#### 监控接口
- `GET /api/monitor/nodes` - 获取所有节点状态
- `GET /api/monitor/nodes/{nodeId}` - 获取指定节点详情
- `GET /api/monitor/nodes/{nodeId}/health` - 获取节点健康状态

#### 部署接口
- `POST /api/deploy/build-from-git` - 从Git构建JAR包
- `POST /api/deploy/upload` - 上传JAR包
- `POST /api/deploy/deploy` - 部署服务
- `POST /api/deploy/deploy-all` - 一键部署所有服务
- `POST /api/deploy/deploy-selected` - 部署选定服务
- `GET /api/deploy/status/{taskId}` - 获取任务状态

#### 回滚接口
- `POST /api/deploy/rollback-all` - 一键回滚所有服务
- `POST /api/deploy/rollback-selected` - 回滚选定服务
- `GET /api/deploy/rollback-history` - 获取回滚历史

### Agent 节点 API

- `GET /api/agent/status` - 获取节点状态
- `POST /api/agent/report` - 报告状态
- `POST /api/agent/deploy` - 执行部署任务
- `POST /api/agent/start/{serviceName}` - 启动服务
- `POST /api/agent/stop/{serviceName}` - 停止服务
- `POST /api/agent/restart/{serviceName}` - 重启服务

## 使用示例

### 1. 从Git构建并部署服务

```bash
curl -X POST "http://localhost:8080/api/deploy/build-from-git" \
  -d "gitUrl=https://github.com/your-repo/your-service.git" \
  -d "branch=main" \
  -d "projectName=your-service" \
  -d "buildCommand=clean package -DskipTests"
```

### 2. 上传JAR包并部署

```bash
# 上传JAR包
curl -X POST "http://localhost:8080/api/deploy/upload" \
  -F "file=@your-service.jar"

# 部署服务
curl -X POST "http://localhost:8080/api/deploy/deploy" \
  -H "Content-Type: application/json" \
  -d '{
    "serviceName": "your-service",
    "jarPath": "/opt/deploy/distribute/your-service/your-service.jar",
    "targetNodes": ["node001", "node002"],
    "forceDeploy": true,
    "deployMode": "ROLLING",
    "operator": "admin"
  }'
```

### 3. 监控服务状态

```bash
# 获取所有节点状态
curl "http://localhost:8080/api/monitor/nodes"

# 获取特定节点详情
curl "http://localhost:8080/api/monitor/nodes/node001"
```

## 目录结构

```
distributed-service-monitor/
├── master/                          # Master节点
│   ├── src/main/java/
│   │   └── com/redis/jedis/
│   │       ├── config/              # 配置类
│   │       ├── controller/          # 控制器
│   │       ├── dto/                 # 数据传输对象
│   │       └── service/             # 服务类
│   └── src/main/resources/
│       └── application.yml          # 配置文件
├── agent/                           # Agent节点
│   ├── src/main/java/
│   │   └── com/redis/jedis/
│   │       ├── config/              # 配置类
│   │       ├── controller/          # 控制器
│   │       ├── dto/                 # 数据传输对象
│   │       └── service/             # 服务类
│   └── src/main/resources/
│       └── application.yml          # 配置文件
├── start-master.sh                  # Master启动脚本
├── start-agent.sh                   # Agent启动脚本
├── test-api.sh                      # API测试脚本
└── README.md                        # 项目说明
```

## 注意事项

1. **Redis集群**: 确保Redis集群正常运行，配置正确的连接信息
2. **目录权限**: 确保应用有权限创建和访问配置的目录
3. **网络连通性**: 确保Master和Agent节点之间网络连通
4. **文件大小**: 大文件（>50MB）建议使用共享存储或HTTP分发
5. **日志管理**: 定期清理日志文件，避免磁盘空间不足

## 故障排除

### 常见问题

1. **Redis连接失败**
   - 检查Redis集群状态
   - 验证连接配置和密码

2. **文件分发失败**
   - 检查共享存储路径权限
   - 验证HTTP服务器配置

3. **服务启动失败**
   - 检查JAR文件路径
   - 查看Agent节点日志

4. **构建失败**
   - 检查Git仓库访问权限
   - 验证Maven配置

## 许可证

MIT License
