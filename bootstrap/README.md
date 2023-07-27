# OCP-Express-Bootstrap
OCP 安装升级模块

## 模块概要
实现完全通过配置文件来定义表结构、初始化数据和升级时的迁移。

`ocp-express-bootstrap-facade` 中主要是基本模型定义以及 spi 定义。其他模块可以引入 `ocp-express-bootstrap-facade`，来实现代码定义迁移、或者升级步骤。

`ocp-express-bootstrap-service` 中是主体逻辑的实现。目前，所有的数据配置也在其中。

## 执行流程
### 新安装

1. 创建表
2. 插入初始数据
3. 更新指定的 config_properties

### 升级
1. 处理需要重命名的表
2. 创建新表
3. 执行修改表结构
   1. 删除索引
   2. 修改字段
   3. 修改索引
4. 插入初始数据
5. 执行数据迁移
6. 异步执行延后任务
   1. 执行延后的修改表结构
   2. 执行延后的数据迁移
   3. 删除表
7. 此时 OCP 已经能正常启动


## 配置说明
OCP-Express-Bootstrap 完全通过 yaml 配置来实现
在 `ocp-express-bootstrap-service` 的 `src/main/resources/ocp_bootstrap_definitions` 中有目前所有的数据定义。实际上，OCP-Express-Bootstrap 会从 class path 中加载所有的 `ocp_bootstrap_definitions` 中的 .yaml 文件。所以，各模块也可以把配置放在自己的模块中。

每个 yaml 配置文件中都可以定义表结构、初始数据和数据迁移，可以按需组织配置。目前都是把每个模块的表结构定义放一个文件，数据初始化和迁移则按表或功能来分文件。尽量让每个配置文件能内聚一点。

```yaml
data_source: dataSource
table_definitions:
  # 表结构定义
data_definitions:
  # 初始数据定义
migrations:
  # 数据迁移定义
```

这里，`data_source` 是 spring boot 中数据源的名字。

配置文件中的 `table_definitions`、`data_definitions`、`migrations` 都是 map。其中的 key 是配置名，value 是配置内容。 每一个数据源中，同一类配置不能重名，不同配置文件中也不可以。如多个文件中，都可以为 metaDataSource 定义 `table_definitions`，这些表定义的名字都不能出现重名，否则会报错。

### 表结构定义

表结构定义配置如下：

```yaml
data_source: dataSource
table_definitions:
  table1:
    fields: 
       field1: # 字段名
           type: varchar(255) # 字段类型
           nullable: false # 可否null，默认false
           default_value: null #默认值
           comment: 注释 可选
           auto_increment: false # 是否自增，默认false
           on_update: !const 'CURRENT_TIMESTAMP' # on_update更新的值 可选
           renamed_from: old_field_1 # 重命名的字段的原始字段名 可选
       field2: { drop: true } # 要删除的字段要显式定义
    primary_key: [ field1 ] # 主键字段列表
    indexes:
      index1: # 索引名
        fields: [ field1, field2 ]
        unique: false # 是否唯一
        local: false # 是否本地索引，默认false
        delay: false # 是否延后执行，默认false
      index2: # 索引名
          drop: true # 要删除的索引要显式定义
    comment: 表注释 可选
    default_charset: utf8mb4 # 表默认字符集 
    auto_increment: 1 # 初始自增值 可选
    renamed_from: old_table1 # 重命名表的原始表名 可选
    partition: # 分区定义，可选
        type: RANGE # 分区类型，RANGE 或 HASH
        fields: [ timestamp ] #分区字段列表
        range_partitions: # 初始 range 分区定义
            - { DUMMY: 0 }
        subpartition: { type: HASH, hash_partition_count: 30, by_expr: series_id } # hash 子分区定义
  table2: ...
```

可以参考现有表结构定义对照理解。这里出现的 !const 是一个[自定义配置类型](#自定义配置类型)，参见下文。

### 初始数据定义

初始数据定义配置如下：

```yaml
data_source: metaDataSource
data_definitions:
  definition1: # 初始数据定义名。一般也可以用表名
      table_name: table1 # 表名
      on_duplicate_update: [field1, field2] # 插入数据发生唯一约束冲突时，更新哪些字段。可选。未配置或设为 null 时，发生唯一约束冲突后跳过。
      rows: # 要插入或刷新的数据
        - { name: "alice", class_id: 11 }
        - { name: "bob", class_id: 12 } # list 的每一行都是 kv，内容是列名-> 值
      delete: # 要清理的数据
        - { name: "cat" } # 每一行都是 kv，按满足 列名-> 值 的条件删除数据
  definition2:
      ....
```

### 数据迁移定义

数据迁移定义配置如下：

```yaml
data_source: metaDataSource
migrations:
  definition1: # 数据迁移定义名。可以解释这个迁移的意图。
    # 通过源 sql 和转换来定义。数据迁移尽量通过这种形式来定义。
    # 这种方法便于 dry run，以及测试验证要修改的数据是否正确
    condition: oldOcpVersion().before('3.2.0') # 执行迁移的条件，用表达式计算，可选
    source_sql: SELECT id, name FROM table1 # 数据来源
    with: # 上下文变量，可选，用表达式计算。可以使用 data 来表示输入数据
        var1: { 'a': 'alice', 'b':'bob'}
    expr: # 转换规则，可选，用表达式计算。可以使用 data 来表示输入数据，以及 with 中定义的变量
    target_table: table2 # 迁移的目标表。
    # 以下三种可选配置只能同时出现一种。都不设置时，生成 INSERT IGNORE，违反唯一约束时忽略
    on_duplicate_update: [ field1, field2 ] # 生成 INSERT ... ON DUPLICATE UPDATE，违反唯一约束时更新指定字段
    update_by: [ field1, field2 ] # 生成 UPDATE 语句，查询结果中的字段，在 update_by 中的作为更新 WHERE 条件，其余的作为更新内容
    delete_by: [ field1, field2 ] # 生成 DELETE 语句，查询结果中的字段，在 delete_by 中的作为删除 WHERE 条件
        
  definition2: # 数据迁移定义名。可以解释这个迁移的意图
    # 这个是通过原始 SQL 来定义。主要用来处理一些数据量特别大的场景，避免前面的方法在内存中产生大量中间数据
    # 尽量用前面的方式来定义，只在不得已时，才写原始 SQL
    condition: oldOcpVersion().before('3.2.0') # 执行迁移的条件，用表达式计算，可选
    raw_sqls:
      - UPDATE table1 SET class_id=100 WHERE class_id=0 # 可以直接写 SQL
      - ...
```

### 自定义配置类型
#### !const
表示 SQL 常量，在 yaml 中用以和字符串标量区分。后面的类型是字符串。如 `!const CURRENT_TIMESTAMP`、`!const CURRENT_TIMESTAMP(6)`、`!const NULL`

#### !expr
用 EL 表达式的结果来替换值。如 `!expr 1+2+3` 实际相当于 `6`

#### !resource
加载一个资源文件，将内容作为字符串值。如 `!resource dir/test1.js` 会从 class path 中加载资源文件 `dir/test1.js` 的内容来替换值。

#### !resourceYaml
加载一个 yaml 资源文件，将其替换入当前位置。如 `!resource dir/test1.yaml.part` 会从 class path 中加载资源文件 `dir/test1.yaml.part` 的内容来替换值。注意，这种方式下，加载的 yaml 文件不支持自定义配置类型！

#### !transform
自定义 yaml 结构转换。`!transform` 接受一个 map，结构参考如下例子：

```yaml
with: # 可选，上下文变量，用表达式计算, 也可以使用 data 来表示输入数据
  nameMap: { 'a': 'alice', 'b':'bob'}
expr: | # 转换表达式，data 变量即配置的 data 部分
    data.stream().map(x -> { name: nameMap[x.name], value: x.value*2 }).toList()
data: #输入数据
    - { name: a, value: 1}
    - { name: b, value: 2}
```

这个例子会转换为
```yaml
- { name: alice, value: 2 }
- { name: bob, value: 4 }
```

### 脚本说明
配置中的脚本都是 EL 表达式，实现上采用 javax 的实现。在脚本环境中，加了一些自定义函数来方便配置处理。

#### debugPrint
在 stderr 中输出参数对象的 json 并返回该对象，用于脚本调试
`Object debugPrint(Object o)`

#### toSet
转换为 Set
`Set<Object> toSet(Collection<Object> objects)`

#### chain
串联一系列 List
`List<Object> chain(List<Object>... lists)`

#### concat
串联一系列字符串
`String concat(String... strings)`

#### join
用分割符连接列表
`String join(String delimiter, Collection<String> strings)`

#### flattenMap
将一个 Map 中的 Map 成员提取到上层 Map 中，field 参数即要提取的 key
`Map<String, Object> flattenMap(String key, Map<String, Object> m)`

#### flatten
展开一个 Map 的 List 成员生成一个 Stream。对 List 成员中的每一个，复制 Map，将 List 中的元素替换进原 key。

`org.apache.el.stream.Stream flatten(String field, Map<String, Object> m)`

#### jsonEncode
json 编码
`String jsonEncode(Object o)`

#### jsonDecode
json 解码
`Object jsonDecode(String s)`

#### bcryptHash
计算 OCP 中的密码 hash
`String bcryptHash(String password)`

#### aesEncrypt
计算 OCP 中的 AES 加密
`String aesEncrypt(String key, String data)`

#### aesDecrypt
计算 OCP 中的 AES 解密

`String aesDecrypt(String key, String data)`

#### systemProperty
读取 系统 property

`String systemProperty(String name)`

#### oldOcpVersion
获取升级前的 OCP 版本
`Version oldOcpVersion()`

#### newOcpVersion
获取新的 OCP 版本

`Version newOcpVersion()`

## 启动参数
引入 OCP-Bootstrap 后，可以通过启动 OCP 时，指定一系列参数来开启 bootstrap 功能
* --help 输出参数帮助
* --bootstrap 开启升级模式。以下所有参数仅在开启 --bootstrap 时有效。
* --install / --upgrade 初次安装或升级，二选一
* --port 监听的端口，默认为 8080。因为需要提前加载 Web 服务，此时无法通过依赖数据库的 config properties 配置。 
* --meta-address={address} metadb 的 地址端口。
* --meta-database={database} metadb 的 数据库名。 
* --meta-user={username} metadb 的用户名。
* --meta-password={password} metadb 的密码。
* --meta-pub-key={pub_key} 用于解密 metadb 的密码的公钥。
* --with-property={propertyName}:{propertyValue} 指定一些需要在启动时确定的 config property 配置。如 monitordb 配置、数据中台的 ES 配置等。

#### 数据库初始化
##### 行为类型
* INSTALL 如果之前 metaDb 是空的，那么执行安装步骤，安装就是纯初始化数据库
* UPGRADE 如果当前 metaDb 版本和之前不一样，那么执行升级步骤，升级会比较当前库和代码定义的差异，然后针对差异做变更
* SKIP 如果当前 metaDb 版本和之前一样，那么什么都不做

##### 初始化阶段
stageProgress 显示了数据库初始化每个阶段的情况，目前支持的阶段有：
* RENAME_TABLES 修改原有表表名，表结构没变化
* CREATE_TABLES 创建表
* ALTER_TABLES 更新表定义
* DEFAULT_DATA 插入默认数据
* MIGRATIONS 迁移数据
* CONFIG_PROPERTIES 创建 config_properties 表及数据
* ALTER_TABLES_DELAYED 更新表定义（可延迟执行，不影响初始化主流程）
* MIGRATIONS_DELAYED 迁移数据（可延迟执行，不影响主流程）
* DROP_TABLES 删除表
* UPGRADE_AGENT 更新 agent 版本

##### 阶段状态
* finishedTasks 完成任务数
* totalTasks 总任务数
* isDone 是否所有任务都跑完了

#### spring bean 初始化
通过 spring 提供的 BeanPostProcessor 来监听 bean 初始化情况
* initializedBeans 当前 spring 已初始化 bean 的数量
* isRootServletInitialized 当初始化完 ServletWebServerFactory 这个 bean 时，该值设置为 true。表示已为 spring boot 设置好了 servlet / tomcat
* isApplicationReady 实现 SpringApplicationRunListener.running 接口，该接口被调用且判断 isRootServletInitialized 为 true 时候设置该参数为 true。表示整个服务已经起来
* pendingBeans 表示正在初始化中的 spring bean 列表，随着时间变化

#### 判断服务是否启动方式
调用 progress 接口，查看 isApplicationReady 字段值，为 true 则表示服务已经完全启动起来了。

### 初始化日志
初始化相关的日志输出到 bootstrap.log 和 ocp.log 文件中
#### bootstrap.log
该日志中包含了初始化进度的相关信息，包含了 bean、数据库初始化各个阶段、sql 任务、agent 升级等信息：
```plain
[2022-10-17T11:02:20.028] BEGIN dataSource UPGRADE -
[2022-10-17T11:02:21.619] BEGIN dataSource DEFAULT_DATA - 3
[2022-10-17T11:02:21.626] BEGIN dataSource DEFAULT_DATA sqls:cmdb_config 15
[2022-10-17T11:02:21.645] END dataSource DEFAULT_DATA sqls:cmdb_config
[2022-10-17T11:02:21.653] BEGIN dataSource DEFAULT_DATA sqls:config_properties 370
[2022-10-17T11:02:21.877] END dataSource DEFAULT_DATA sqls:config_properties
[2022-10-17T11:02:21.878] BEGIN dataSource DEFAULT_DATA sqls:config_properties 40
[2022-10-17T11:02:21.890] END dataSource DEFAULT_DATA sqls:config_properties
[2022-10-17T11:02:21.901] END dataSource DEFAULT_DATA -
[2022-10-17T11:02:21.902] BEGIN dataSource MIGRATIONS - 3
[2022-10-18T19:47:40.189] BEGIN - UPGRADE_AGENT - 1
[2022-10-18T19:47:41.790] END - UPGRADE_AGENT ******(1) 0
```
主要记录的是开始和结束信息，格式如下：
```plain
[日志时间] BEGIN dataSource stage name message
或者
[日志时间] END now dataSource stage name message errorStack
```
* dataSource 数据源，（参考初始化进度和状态中的数据源定义），不涉及数据源的即为"-"
* stage 表示为哪个阶段（参考初始化进度和状态中的阶段定义）
* name 表示阶段内执行的任务名称或者主机 ip，比如 sqls:config_properties 表示在初始化 config_properties 相关表，不涉及名称的即为"-"
* message 表示执行的 sql 数量或者需要升级的主机数量
* errorStack 如果有异常，那么显示异常栈信息

#### ocp.log
ocp.log 中需要关注的是关键字为"BootstrapRunListener"、"DataSourceInterceptor"的相关日志，这块是 ocp-bootstrap 注册监听器后产生的日志，
在启动时用于拦截处理 spring 初始化 bean 事件，主要关注是否有不预期报错。
