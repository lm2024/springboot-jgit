#!/bin/bash

echo "停止Redis集群..."
docker-compose down

echo "清理容器和网络..."
docker-compose down --volumes --remove-orphans

echo "Redis集群已停止！"
