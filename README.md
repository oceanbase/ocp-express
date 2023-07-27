# OCP Express

OCP Express（OceanBase Cloud Platform Express）是一款为 OceanBase 集群量身打造的轻量化管理平台，兼容 OceanBase 4.0.0+
版本。OCP Express
提供对 OceanBase 图形化管理能力，包括数据库及相关资源的管理、监控、诊断等，旨在协助客户更加高效、轻量的管理OceanBase
数据库，降低运维成本、使用成本和用户的学习成本。

## 代码编译

需要如下依赖来编译 OCP Express:

* 前端
    * 最新文档 LTS 版本 [Node.js 16.x](https://nodejs.org/download/release/latest-v16.x/)
    * `可选`最新稳定版本 cnpm，也可以使用 Node.js 内置的 npm
* 后端
    * 最新的稳定版 [OpenJDK 8](https://openjdk.org/install/)
    * 最新的稳定版 [Apache Maven](https://maven.apache.org/)
    * rpmbuild 需要此依赖打包 rpm

依赖安装完成后可以执行 `sh build/script/package.sh jar` 来编译、打包前后端代码，也可以参考 [编译](docs/build.md) 来手动进行打包编译。

## 项目结构

项目整体使用分层架构，各个模块说明参考 [项目结构](docs/structure.md)。

## 启动

启动 OCP Express 需要存在一个 OceanBase 集群、OBAgent，可以使用 OBD 部署 OCP
Express，相关步骤参考[命令行部署 OCP Express](https://www.oceanbase.com/docs/community-obd-cn-10000000002048168)
，详细启动说明参考 [启动](docs/start.md)

## License

[木兰宽松许可证, 第2版](http://license.coscl.org.cn/MulanPSL2)