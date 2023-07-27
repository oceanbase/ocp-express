# 项目结构

# 1. 项目分层

由上到下分为：Web层、服务层、核心层、基础层，依赖管理层
- Web层：提供 Web 服务，全局配置及系统初始化，请求入口
- 服务层：OCP业务，按照业务域划分
- 核心层：业务相关的共享模型、元数据库访问的封装
- 基础层：业务无关的工具类、模式、框架等
- 依赖层：管理项目依赖，及项目打包编译


# 2. 目录说明

```text
ocp-express
├──────── 前端 ────────
│
├── frontend 前端代码
│
├──────── Web 层 ────────
│
├── server   提供 WEB 服务，包括静态资源，全局配置，系统初始化
│
├──────── 服务层 ────────
├── bootstrap  项目初始化模块，负责初始化 metadb 及版本迁移
├── monitor  监控模块，包括监控采集、存储、计算
├── obops    OB 相关运维功能
├── perf     诊断
├── security 安全模块，负责授权认证
├── task     任务模块，承担运维任务及定时任务
│
├──────── 核心层 ────────
│
├── core   核心模块，模块间的共享模型等相关封装
├── library  通用依赖库
│     ├── command-executor    命令执行器，负责访问 agent
│     ├── ob-parser           OB SQL 解析器
│     ├── obsdk               封装 OB 内部相关操作
│     ├── partition-rollover  分区巡检，负责新建及维护分区轮转
│     └── vault               密码箱，保存用户相关敏感信息
│
├──────── 基础层 ────────
│
├── common 基础模块，业务无关
│
├──────── 其他  ────────
│
├── starter  子模块 starter，负责初始化子模块
├── bom 依赖管理
│     ├── bom                项目模块依赖
│     ├── dep-apache-commons apache 相关依赖
│     ├── dep-core           核心依赖
│     ├── dep-dao            DAO 依赖
│     ├── dep-spring         Spring 相关
│     └── dep-test           测试相关依赖
├── build  编码相关
│     ├── coding-style  代码规范
│     ├── packaging     打包 RPM 相关配置
│     └── scripts       项目内置脚本
└── docs 文档
```
