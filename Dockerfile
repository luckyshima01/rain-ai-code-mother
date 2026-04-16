# 注意，本文件仅为示例！由于本项目涉及的依赖较多，不推荐使用 Docker 部署
# 使用轻量级 JDK21 运行环境
FROM openjdk:21-slim

# 配置阿里云 apt 源并安装 Chrome 浏览器及其依赖
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources && \
    apt-get update && \
    apt-get install -y wget gnupg2 curl ca-certificates && \
    wget -q -O /tmp/google-chrome.deb https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb || \
    wget -q -O /tmp/google-chrome.deb https://mirrors.aliyun.com/google-chrome/google-chrome-stable_current_amd64.deb && \
    apt-get install -y /tmp/google-chrome.deb && \
    rm -f /tmp/google-chrome.deb && \
    # 安装中文字体（文泉驿正黑 + Noto CJK）
    apt-get install -y fonts-wqy-zenhei fonts-noto-cjk fontconfig && \
    # 刷新字体缓存
    fc-cache -fv && \
    # 安装 Node.js 20.x LTS (使用 NodeSource)
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg && \
    echo "deb [signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_20.x nodistro main" > /etc/apt/sources.list.d/nodesource.list && \
    apt-get update && \
    apt-get install -y nodejs && \
    # 清理缓存
    rm -rf /var/lib/apt/lists/*

# 验证 Node.js 和 npm 版本
RUN node --version && npm --version

# 工作目录
WORKDIR /app

# 复制已经打包好的 JAR 文件（假设已放在当前目录）
COPY target/rain-ai-code-mother-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8123

# 使用生产环境配置启动应用
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
