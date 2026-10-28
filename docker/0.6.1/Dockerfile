# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE text that was distributed with this
# work for additional information regarding copyright ownership.
#
# dubbo-admin 0.6.1 镜像构建文件（基于官方 docker/0.6.0/Dockerfile 改造）
# 与官方 0.6.0 版本的差异：
#   1. 源码直接取自本地构建上下文（COPY . /source），不再从 GitHub 下载发布包——
#      0.6.1 是内部安全修复版本，官方没有对应的发布包；
#   2. 运行阶段 ENV JAVA_OPTS 默认注入 -Dfastjson.parser.safeMode=true，
#      修复安全扫描命中的 fastjson autoType 反序列化漏洞
#      （规则：fastjson 1.2.68 <= version <= 1.2.83 且 safemode=false）；
#   3. 构建阶段使用国内镜像源（Maven 阿里云镜像、npm/Node npmmirror 镜像），解决国内网络下载慢的问题。
#
# 构建方式（在仓库根目录执行）：
#   docker build -f docker/0.6.1/Dockerfile -t dubbo-admin:0.6.1 .
# 构建上下文较大时可先确认根目录 .dockerignore 已排除 .git 与 target。

# ---------- 第一阶段：容器内编译 ----------
FROM maven:3-openjdk-8 AS builder

# 写入 Maven 阿里云镜像配置，加速依赖下载
RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<?xml version="1.0" encoding="UTF-8"?>' \
    '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">' \
    '  <mirrors>' \
    '    <mirror>' \
    '      <id>aliyun-public</id>' \
    '      <mirrorOf>central</mirrorOf>' \
    '      <url>https://maven.aliyun.com/nexus/content/groups/public</url>' \
    '    </mirror>' \
    '  </mirrors>' \
    '</settings>' > /root/.m2/settings.xml

# npm 依赖与 Node 二进制下载均走 npmmirror 国内镜像
ENV npm_config_registry=https://registry.npmmirror.com

WORKDIR /source
COPY . /source
# 与官方 0.6.0 保持一致：用 docker 目录下的默认配置覆盖应用自带配置（部署时可在运行期覆盖）
COPY docker/0.6.1/application.properties dubbo-admin-server/src/main/resources/application.properties
# -DnodeDownloadRoot：frontend-maven-plugin 下载 Node v9.11.1 走国内镜像
RUN mvn --batch-mode clean package -Dmaven.test.skip=true \
        -DnodeDownloadRoot=https://npmmirror.com/mirrors/node/

# ---------- 第二阶段：运行镜像 ----------
# 官方 0.6.0 用的 openjdk:8-jdk 已从 Docker Hub 下架，改用官方继任镜像 eclipse-temurin:8-jdk（同为 Ubuntu 系）
FROM eclipse-temurin:8-jdk
LABEL maintainer="dev@dubbo.apache.org"
RUN apt-get update && apt-get install -y tini
COPY --from=builder /source/dubbo-admin-distribution/target/dubbo-admin-0.6.1.jar /app.jar
COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh

# 安全兜底：0.6.1 已把 fastjson1 整体升级为 fastjson2（根 pom fastjson2-version=2.0.66），
# 此处再默认开启 fastjson1 的 safeMode 开关，防未来传递依赖混入 fastjson1 时暴露 autoType 漏洞。
# 应用主类里有代码级兜底，即使这里被运行时的 JAVA_OPTS 环境变量覆盖，该开关仍会生效。
ENV JAVA_OPTS "-Dfastjson.parser.safeMode=true"

ENTRYPOINT ["tini", "--", "/usr/local/bin/entrypoint.sh"]
EXPOSE 8080
