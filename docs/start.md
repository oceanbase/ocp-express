# 启动 OCP Express

启动 OCP Express 前需要确保如下组件存在，如不存在可以使用 OBD 部署相关组件：

* OceanBase：V4.0.0.0 及以上版本。
* OBAgent：V1.3.0 及以上版本。

在编译打包好 OCP Express 后可以使用如下方式启动 OCP Express。

1. OBD 启动：OBD 会自动加载及部署相关组件及前置依赖 `推荐使用此方式`。
2. Jar 包启动：需要手动创建及配置相关依赖项。

## 1. OBD 启动 Express `推荐`

需要先打包出 OCP Express 的 RPM 文件，可以参考[OCP Express 打包流程](build.md)

1. [安装 OBD](https://www.oceanbase.com/docs/community-obd-cn-10000000002049468)
2. 将编译好的 RPM 复制到 OBD 本地仓库，可以参考
   OBD [命令说明](https://www.oceanbase.com/docs/community-obd-cn-10000000002048172)。

```bash
obd mirror clone <path-to-rpm>/ocp-express-***.rpm
```

3. 使用 OBD 命令[部署](https://www.oceanbase.com/docs/community-obd-cn-10000000002048168)
   或[升级](https://www.oceanbase.com/docs/community-obd-cn-10000000002048169) OCP Express 组件。

## 2. Jar 包启动

### 前置条件

* 启动 OCP Express 前需要提前部署好 OB 集群，并安装 OBAgent，并创建好 OCP Express 需要使用的租户、用户。
* 如果是全新部署则需要指定 `--bootstrap` 参数初始化 metadb，启动时会自动判断 metadb 信息来确认是否需要初始化。
* 为了保证 OCP Express 能够稳定运行，建议为 OCP Express 预留 512MB 以上内存，推荐使用 768MB+ 内存。

### 启动配置

* OCP Express 需要在启动时指定 admin 用户密码、管理的集群信息。
    * 指定 admin 用户密码有如下两种方式
        1. 通过环境变量传递，如 `export OCP_EXPRESS_ADMIN_PASSWD=******`
        2. 通过 VM 参数传递，如 java -jar 增加启动参数 `-DOCP_EXPRESS_ADMIN_PASSWD=******`
        3. admin 密码有强度要求，至少包含 2 个大写字母、2 个小写字母、2 个数字、2 个特殊符号，长度在 8-32 位。
            * 特殊符号包括：~!@#%^&*_-+=|(){}[]:;,.?/

* 初始化配置支持两种格式传递
    1. 指定 yaml 初始化方式启动，通过 `--spring.config.additional-location=xxx.yaml` 类指定
    2. 指定 JSON 环境变量或 VM 参数启动

#### 指定 Yaml 配置文件启动

配置示例：

```yaml
ocp:
  init:
    # ob agent 访问 manager API 的用户名
    agent-username: "***"
    # ob agent 访问 manager API 的密码
    agent-password: ""
    cluster:
      # 集群名
      name: "***"
      # 集群 ID
      ob-cluster-id: 1
      # root@sys 密码
      root-sys-password: ""
      # observer 列表信息
      server-addresses:
        - # observer ip 地址 
          address: "127.0.0.1"
          # observer rpc 端口
          svr-port: 2882
          # observer sql 端口
          sql-port: 2881
          # 是否是 rootserver
          with-root-server: true
          # agent 的管理端口
          agent-mgr-port: 6281
          # agent 的监控端口
          agent-mon-port: 6282
        - address: "127.0.0.1"
          svr-port: 2892
          sql-port: 2891
          with-root-server: false
          agent-mgr-port: 6283
          agent-mon-port: 6284
```

启动命令示例:

```bash
java -jar
-DJDBC_URL="jdbc:oceanbase://<datasource-url>:<datasource-port>/<database>"
-DJDBC_USERNAME="xxx"
-DJDBC_PASSWORD=""
-DOCP_EXPRESS_ADMIN_PASSWD="******"
ocp-express.jar
--bootstrap
--port=8080
--spring.config.additional-location="***.yaml"
```

#### 指定 JSON 变量启动

配置示例（参数意义与 Yaml 格式一致)

```json
{
  "cluster": {
    "name": "cluster01",
    "obClusterId": 6,
    "rootSysPassword": "",
    "serverAddresses": [
      {
        "address": "127.0.0.1",
        "svrPort": 2882,
        "sqlPort": 2881,
        "withRootServer": true,
        "agentMgrPort": 62888,
        "agentMonPort": 62889
      },
      {
        "address": "127.0.0.2",
        "svrPort": 2882,
        "sqlPort": 2881,
        "withRootServer": false,
        "agentMgrPort": 62888,
        "agentMonPort": 62889
      }
    ]
  },
  "agentUsername": "ocp_agent",
  "agentPassword": ""
}
```

启动命令示例:

```bash
export OCP_EXPRESS_ADMIN_PASSWD=******
export OCP_EXPRESS_INIT_PROPERTIES='<json-config>'
java -jar
-DJDBC_URL="jdbc:oceanbase://<datasource-url>:<datasource-port>/<database>"
-DJDBC_USERNAME="xxx"
-DJDBC_PASSWORD=""
--bootstrap
--port=8080
```
