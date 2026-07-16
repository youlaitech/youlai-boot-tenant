<div align="center">
   <img alt="logo" width="100" height="100" src="https://foruda.gitee.com/images/1733417239320800627/3c5290fe_716974.png">
   <h2>youlai-boot-tenant</h2>
   <img alt="Java" src="https://img.shields.io/badge/Java-25-brightgreen.svg"/>
   <img alt="Spring Boot" src="https://img.shields.io/badge/Spring Boot-3.x-green.svg"/>
   <img alt="Multi-tenancy" src="https://img.shields.io/badge/Multi--tenancy-SaaS-blue.svg"/>
   <a href="https://gitee.com/youlaiorg/youlai-boot-tenant" target="_blank">
     <img alt="Gitee star" src="https://gitee.com/youlaiorg/youlai-boot-tenant/badge/star.svg"/>
   </a>     
   <a href="https://github.com/youlaitech/youlai-boot-tenant" target="_blank">
     <img alt="Github star" src="https://img.shields.io/github/stars/youlaitech/youlai-boot-tenant.svg?style=social&label=Stars"/>
   </a>
</div>

<p align="center">
  <a target="_blank" href="https://vue.youlai.tech/">🖥️ 在线预览</a>
  <span>&nbsp;|&nbsp;</span>
  <a target="_blank" href="https://www.youlai.tech/youlai-boot-tenant">📑 阅读文档</a>
  <span>&nbsp;|&nbsp;</span>
  <a target="_blank" href="https://www.youlai.tech">🌐 官网</a>
</p>

## 📢 项目简介

`youlai-boot-tenant` 是 `youlai-boot` 的多租户版本，基于 Spring Boot 3, Spring Security, Mybatis-Plus, JWT, Redis 构建，专为 SaaS 应用提供后端支持。

- **🏢 多租户架构**: 基于 Mybatis-Plus 的单库多租户方案，通过租户 ID 实现数据隔离。
- **🚀 最新技术栈**: 采用 Spring Boot 3 和 JDK 25，享受最新的性能优化和语言特性。
- **🔐 企业级安全**: 深度整合 Spring Security，提供 JWT 无状态认证和 Redis 会话管理双重机制。
- **🔑 精细化权限**: 内置经典的 RBAC 模型，权限控制可精确到菜单、按钮及后端 API 接口。

## 🌈 项目源码

| 项目类型 | Gitee | Github | GitCode |
| --- | --- | --- | --- |
| ✅ Java 多租户 | [youlai-boot-tenant](https://gitee.com/youlaiorg/youlai-boot-tenant) | [youlai-boot-tenant](https://github.com/youlaitech/youlai-boot-tenant) | [youlai-boot-tenant](https://gitcode.com/youlai/youlai-boot-tenant) |
| vue3 前端 | [vue3-element-admin](https://gitee.com/youlaiorg/vue3-element-admin) | [vue3-element-admin](https://github.com/youlaitech/vue3-element-admin) | [vue3-element-admin](https://gitcode.com/youlai/vue3-element-admin) |
| uni-app 移动端 | [vue-uniapp-template](https://gitee.com/youlaiorg/vue-uniapp-template) | [vue-uniapp-template](https://github.com/youlaitech/vue-uniapp-template) | [vue-uniapp-template](https://gitcode.com/youlai/vue-uniapp-template) |

## 📚 项目文档

| 文档名称 | 访问地址 |
| --- | --- |
| 项目介绍与使用指南 | [https://www.youlai.tech/youlai-boot-tenant](https://www.youlai.tech/youlai-boot-tenant) |

## 📁 项目目录

<details>
<summary> 目录结构 </summary>

```text
youlai-boot-tenant/
├─ docker/                    # Docker 编排
├─ sql/                       # 数据库脚本
├─ src/
│  ├─ auth/                   # 认证授权业务
│  ├─ codegen/                # 代码生成器
│  ├─ common/                 # 全局通用（常量、枚举、工具类、统一响应结果）
│  ├─ framework/              # 底层技术基座（缓存/验证码/安全/Web等）
│  ├─ message/                # 消息中心
│  ├─ file/                   # 文件中心
│  ├─ system/                 # 核心系统模块（用户/角色/菜单/部门）
│  └─ YouLaiBootApplication.java  # 启动类
└─ pom.xml                    # Maven 构建配置
```

</details>

## 🚀 快速启动

### 1. 环境准备

| 要求       | 说明        |
| ---------- | ----------- |
| **JDK 25** | 25+ LTS     |
| **MySQL**  | 5.7+ 或 8.x |
| **Redis**  | 7.x 稳定版  |

> ⚠️ **重要提示**：MySQL 与 Redis 为项目启动必需依赖，请确保服务已启动。

### 2. 数据库初始化

推荐使用 **Navicat**、**DBeaver** 或 **MySQL Workbench** 执行 `sql/mysql/youlai_admin_tenant.sql` 脚本，完成数据库和基础数据的初始化。

### 3. 修改配置

编辑 `src/main/resources/application-dev.yml` 文件，根据实际情况修改 MySQL 和 Redis 的连接信息。

### 4. 启动项目

运行 `YouLaiBootApplication.java` 的 `main` 方法启动项目。

启动成功后，访问 [http://localhost:8000/doc.html](http://localhost:8000/doc.html) 验证项目是否成功。

## 🧪 多租户测试

- **预置租户**: 平台默认租户 (`tenant_id=0`) 和演示租户 (`tenant_id=1`)。
- **预置账号**: 平台租户 (`root`/`admin`) 和演示租户 (`admin`)，默认密码 `123456`。
- **本地测试**: 修改本地 `hosts` 文件，添加 `127.0.0.1 vue.youlai.tech` 和 `127.0.0.1 demo.youlai.tech`，通过不同域名访问即可自动切换租户。

## 🐳 项目部署

### 1. Jar 部署

```bash
# 打包
mvn -DskipTests package

# 运行
java -jar target/youlai-boot-tenant.jar --spring.profiles.active=prod
```

### 2. Docker 部署

```bash
# 构建镜像
docker build -t youlai-boot-tenant:latest .

# 运行容器
docker run -d -p 8000:8000 --name youlai-boot-tenant youlai-boot-tenant:latest
```

## 💖 技术交流

- **问题反馈**：[Gitee Issues](https://gitee.com/youlaiorg/youlai-boot-tenant/issues)
- **技术交流群**：[QQ 群：950387562](https://qm.qq.com/cgi-bin/qm/qr?k=U57IDw7ufwuzMA4qQ7BomwZ44hpHGkLg)
- **博客教程**：[https://www.youlai.tech](https://www.youlai.tech)
