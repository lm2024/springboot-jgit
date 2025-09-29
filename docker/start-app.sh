#!/bin/bash

echo "================================="
echo "启动Spring Boot Redis集群应用"
echo "================================="

# 检查Redis集群是否运行
echo "检查Redis集群状态..."
if ! docker-compose ps | grep -q "redis-standalone.*Up"; then
    echo "Redis集群未运行，正在启动..."
    ./start-redis-cluster.sh
    echo "等待Redis集群完全启动..."
    sleep 10
else
    echo "Redis集群已运行"
fi

# 编译并启动Spring Boot应用
echo "编译Spring Boot应用..."
mvn clean compile

echo "启动Spring Boot应用..."
mvn spring-boot:run &

# 等待应用启动
echo "等待应用启动..."
sleep 15

echo ""
echo "================================="
echo "应用启动完成！"
echo "================================="
echo "API文档地址: http://localhost:8080/doc.html"
echo "Swagger文档: http://localhost:8080/swagger-ui.html"
echo "应用端口: 8080"
echo "Redis端口: 6379"
echo "================================="
echo ""
echo "按 Ctrl+C 停止应用"
