# BookManage 图书借阅管理

[简体中文](README.md) | [English](README.en.md)

传统 Java Web 项目：Spring MVC 6、Spring Security 6、MyBatis、Thymeleaf。
支持登录、图书增删、学生列表、借阅与归还；归还记录保存在数据库，图书删除采用软删除。
普通 `USER` 用户只读，`ADMIN` 用户可以维护图书和借阅记录。

## 环境要求

- JDK **17+**，Maven 3.9+
- MySQL 8 或 MariaDB 10.11（本机使用 MariaDB 10.11 独立实例）
- 可通过内嵌 Tomcat 10.1 启动，也可把 WAR 部署到外部 Tomcat 10.1

所有本地安装文件和数据库数据位于 `~/.local/share/bookmanage-runtime/`，不需要 sudo。
环境路径定义在 `scripts/local-env.sh`，可以通过 `BOOKMANAGE_RUNTIME` 覆盖。
本地安装不会注册系统服务，也不会在重启后自动启动。

Ubuntu 24.04 amd64 可以用 `bash scripts/install-local-runtime.sh` 重建免 root 工具链。
创建全新的独立数据库实例时，先 `source scripts/local-env.sh`，然后运行：

```bash
"$BOOKMANAGE_RUNTIME/local/usr/bin/mariadb-install-db" --no-defaults \
  --basedir="$BOOKMANAGE_RUNTIME/local/usr" --datadir="$BOOKMANAGE_RUNTIME/data" \
  --auth-root-authentication-method=socket --auth-root-socket-user="$(id -un)" --skip-test-db
```

`bash scripts/start-local.sh --database-only` 会启动该实例；通过 `mariadb --socket="$BOOKMANAGE_RUNTIME/mysql.sock"`
（客户端位于 `$BOOKMANAGE_RUNTIME/local/usr/bin/`）进入数据库后，按下方“新环境配置”创建账号及应用表。

## 当前机器启动 / 停止

```bash
cd /home/rabbit/Documents/OpenCode/JavaProject/BookManage
bash scripts/start-local.sh
# 打开 http://127.0.0.1:8080/login

# 停止应用与独立数据库，不删除数据
bash scripts/stop-local.sh
```

账号：`admin`。本机初始密码保存在 `.runtime/credentials.txt`（仅当前用户可读，未纳入 Git）。
配置和数据库密码保存在 `.env.local`，不要提交这些文件。

日志：`.runtime/application.log`、`~/.local/share/bookmanage-runtime/mysql.log`。
默认只绑定 `127.0.0.1:8080` 和 `127.0.0.1:3307`。

## 编译和测试

```bash
# 若使用本机的免 root JDK/Maven
source scripts/local-env.sh
bash scripts/build.sh
```

构建会运行集成测试、生成 `target/BookManage-1.0-SNAPSHOT.war` 和本地启动 classpath。
测试使用独立 H2 内存数据库（MySQL 模式），不连接或清空真实数据库。
覆盖：借阅字段映射、历史保留、金额精度、无效/重复借阅、并发竞争、软删除、角色权限、
CSRF、真实密码认证、remember-me、页面渲染、非根路径部署和参数校验。

修改源码后，需要重新构建并重启应用。

真实数据库 HTTP 联调（**仅限演示环境**，会创建测试图书并完成借阅/归还/软删除，保留历史）：

```bash
ALLOW_SMOKE_WRITES=1 python3 scripts/smoke-local.py
```

结果保存在 `.runtime/smoke-results.txt`。该测试包含真实并发竞争、中文表单提交和 CSRF 拒绝检查。
可用 `SMOKE_BASE` 和 `SMOKE_PASSWORD` 环境变量指定测试地址及管理员密码。

## 新环境配置（已有 JDK、Maven 和数据库）

1. 创建专用空数据库及初始化账号，不要使用现有业务数据库：

   ```sql
   CREATE DATABASE bookmanage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'bookmanage'@'127.0.0.1' IDENTIFIED BY '替换为随机密码';
   GRANT ALL PRIVILEGES ON bookmanage.* TO 'bookmanage'@'127.0.0.1';
   ```

2. 复制 `.env.example` 到 `.env.local`，修改连接地址、数据库密码，以及至少12位的管理员密码：

   ```bash
   cp .env.example .env.local
   chmod 600 .env.local
   bash scripts/build.sh
   bash scripts/init-db.sh
   ```

   初始化器只接受**没有应用表的数据库**，不会覆盖已有表或数据。
   `schema.sql` 仅用于新安装，重复执行索引定义会报错；不要把它当作自动迁移脚本。

3. 初始化成功后撤回 DDL 权限（由数据库管理账号执行），应用只需查询和增删改权限：

   ```sql
   REVOKE ALL PRIVILEGES ON bookmanage.* FROM 'bookmanage'@'127.0.0.1';
   GRANT SELECT, INSERT, UPDATE, DELETE ON bookmanage.* TO 'bookmanage'@'127.0.0.1';
   ```

4. 从 `.env.local` 删除 `ADMIN_PASSWORD`，执行：

   ```bash
   bash scripts/run.sh
   ```

   `APP_PORT` 控制端口；`APP_CONTEXT_PATH=/library` 可验证非根路径部署。
   外部 WAR 部署时设置 `DB_URL`、`DB_USER`、`DB_PASSWORD` 环境变量。

## 数据结构及旧数据库注意事项

- `book`：价格为 `DECIMAL(10,2)`，新增 `deleted` 软删除标志。
- `student`：统一表名，性别使用字符串。
- `borrow`：`sid`、`bid` 外键；新增 `returned_at`，NULL 表示尚未归还。
- `user`：唯一用户名、BCrypt 密码、`ADMIN` / `USER` 角色。
- `persistent_logins`：Spring Security 记住我令牌。

**不要直接导入 NOTES 压缩包里的旧 `study.sql`**：它包含 DROP TABLE，且结构与现版本不同。
如有旧数据库，先备份，再单独审查迁移：统一 `student2` / `student`，转换性别，调整金额精度，
增加软删除及归还时间，清理重复借阅、孤立外键和重复用户名后再加约束。
本次本地运行创建全新的 `bookmanage` 数据库，不访问旧的 `study` 数据库。

## 关键修复

- 借阅查询显式列名与别名，避免多个 `id` 混淆及学生编号覆盖借阅编号。
- 图书与状态通过同一查询返回，消除多次查询及位置对齐问题；统计使用 COUNT。
- 借阅、归还、删除通过事务和同一图书行锁串行化，服务端检查有效性和当前状态。
- 启用 CSRF，所有修改和退出操作使用 POST；Thymeleaf 表单自动生成 CSRF 令牌。
- 管理端接口校验 ADMIN 角色，页面隐藏普通用户的维护按钮。
- 服务端表单校验、统一错误页面、UTF-8 过滤器和上下文路径感知链接。
- 外置数据库凭据、连接池、统一依赖版本和自动化测试。

本项目仍是教学型应用。对公网部署前应使用 HTTPS、配置安全 Cookie、登录限流、
数据库备份、审计、分页及运维监控，不应直接暴露本地演示配置。

## 原始项目

原始教程（中文）：https://www.bilibili.com/video/BV1fV411M7aS?p=19
保留仓库原始 LICENSE。
