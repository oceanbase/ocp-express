# 编译打包

# 1. 前端

1. 安装 [Node.js 16.x](https://nodejs.org/download/release/latest-v16.x/)
2. `可选` 安装 [cnpm](https://github.com/cnpm/cnpm)
3. 构建代码

npm
```bash
# 切换到前端目录
cd frontend

# 安装依赖
npm install

# 构建前端代码
npm run build
```

cnpm
```bash
# 切换到前端目录
cd frontend

# 安装依赖
cnpm install

# 构建前端代码
cnpm run build
```
4. 编译成功后会在项目的 `frontend/dist` 目录生成前端资源文件。

5. 将前端资源文件 copy 到后端代码 resources 目录.

```bash
  rm -rf server/src/main/resources/static
  mkdir -p server/src/main/resources/static
  cp -r frontend/dist/* server/src/main/resources/static
```
# 2. 后端

1. 安装最新的稳定版 [OpenJDK 8](https://openjdk.org/install/)
2. 安装最新的稳定版 [Apache Maven](https://maven.apache.org/)
3. 安装 `rpm-build`
 
```bash
# CentOS
yum install rpm-build

# debian
apt-get install rpm

# MacOS
brew install rpm
```

4. 编译 jar

编译之前可以指定版本号，可以通过 `build/scripts/change_version.sh` 来修改版本号.

```bash
mvn clean package -Dmaven.test.skip=true
```
也可以使用内置导报脚本来实现一键打包 jar，`sh build/scripts/package.sh jar`

5. 打包 RPM

可以使用内置导报脚本来实现一键打包 rpm，`sh build/scripts/package.sh rpm`
 