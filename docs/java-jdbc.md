# JDBC 三大核心对象：Connection / Statement / ResultSet

> 基于 [JdbcDemo.java](../src/main/java/topics/jdbc/JdbcDemo.java)（已在本机跑通，MySQL 8.4.11）。JDBC 的全部骨架就是三个接口各司其职：**Connection 管通道，Statement 管执行，ResultSet 管读取**。
>
> 前置：[Java IO 流](./java-io.md)——JDBC 和 socket 流是同构的，第 1 节直接对照着讲；[Classpath](./java-classpath.md)——驱动 jar 为什么是 `runtime` scope。

## 1. 心智模型：给数据库打电话

MySQL 服务器只说它自己的协议（TCP 上的二进制协议），你的 Java 代码只说 Java。中间的**翻译官就是驱动 jar**：JDBC 标准只定义接口，实现全在驱动里——所以驱动是 `runtime` scope（见 [classpath 文档](./java-classpath.md) 第 4 节场景二）。

把一次数据库访问当成"打电话"，和你写过的 socket 例子逐一对上：

| JDBC | IO 流（socket 例子） | 干的事 |
|---|---|---|
| `DriverManager` | `new Socket(host, port)` | 拨号 + 认证，拿到通道 |
| `Connection` | `Socket` | 一条到数据库的会话通道 |
| `Statement` | `PrintWriter`（发请求） | 把 SQL 发过去 |
| `ResultSet` | `BufferedReader`（读响应） | 把结果一行行读回来 |
| `close()` | `close()` | 挂断 / 释放 |

```mermaid
flowchart LR
    A["DriverManager.getConnection<br/>拨号 + 认证"] --> B["Connection<br/>通话线路"]
    B -->|"createStatement()"| C["Statement<br/>说一句 SQL"]
    C -->|"executeQuery()"| D["ResultSet<br/>对方回话（游标）"]
    D -->|"next() / getString()"| E["逐行听"]
    E --> F["close ×3<br/>rs → stmt → conn"]
```

> 对照记忆：**MySQL 就是另一台 socket 服务端，驱动就是协议翻译器。** 你在 IO 里练的"建连接 → 发请求 → 读响应 → 关资源"，换了个协议又来了。

## 2. Connection：一条会话通道（重资产）

**Connection = Java 程序与数据库之间的一次会话**，底层一条 TCP 连接，所有 SQL 都从它身上发出。三个职责：

```java
Connection conn = DriverManager.getConnection(url, user, password); // ① 拨号
Statement stmt = conn.createStatement();                            // ② 造执行器
conn.setAutoCommit(false); conn.commit(); conn.rollback();          // ③ 管事务
conn.close();                                                       // ④ 挂断
```

**它贵在哪**：拨号 = TCP 握手 + 用户认证 + 会话初始化，毫秒级开销。所以两条纪律——用完必须 `close()`（连接泄漏会耗尽数据库连接数）；生产环境用**连接池**复用（后面学）。

**关键观察点**（JdbcDemo 里那行输出）：

```text
连接的运行时类型：com.mysql.cj.jdbc.ConnectionImpl
```

`Connection` 是 JDK 里的接口，`ConnectionImpl` 是驱动 jar 里的实现类——**面向接口编程，实现由驱动提供**，和 classpath 文档讲的 SPI 注册一脉相承。

## 3. Statement：SQL 的执行器（用完即弃）

**Statement = 装载一条 SQL、发过去、带回结果的对象**，由 Connection 创建，属于这条会话。它本身很轻，贵的只是背后的 Connection。

两个最常用的方法，按"要不要取回数据"分：

| 方法 | 用途 | 返回 |
|---|---|---|
| `executeQuery(sql)` | **SELECT** | `ResultSet`（结果集，要读） |
| `executeUpdate(sql)` | **INSERT / UPDATE / DELETE / DDL** | `int`（受影响行数） |

```java
ResultSet rs = stmt.executeQuery("SELECT VERSION()");   // 查：回结果集
int rows = stmt.executeUpdate("UPDATE user SET age=20 WHERE id=1"); // 改：回行数
```

> 家族预告：写业务基本都用它的兄弟 **`PreparedStatement`**——SQL 预编译 + `?` 占位符，防 SQL 注入还提性能。本节先吃透 Statement 的分工，它俩的分工模型是一样的。

## 4. ResultSet：带游标的结果集（最反直觉）

**ResultSet = SELECT 结果的容器 + 一根游标。** 游标**初始停在第一行之前**——这就是 JdbcDemo 里那行 `rs.next()` 存在的全部理由：

```java
ResultSet rs = stmt.executeQuery("SELECT VERSION()");
rs.next();                            // ← 游标拨到第一行；没有这行，还悬在"第 0 行"
System.out.println(rs.getString(1));  // 现在 next() 过了，才能取值
```

### next()：拨一格，返回有没有踩到数据

- 拨到有数据的一行 → `true`；越过最后一行 → `false`
- 这天然就是遍历模板：

```java
while (rs.next()) {                        // false 就停，不会越界
    System.out.println(rs.getString("name"));
}
```

### 取值：getString / getInt / getDouble + 列名或下标

| 取值写法 | 例子 | 建议 |
|---|---|---|
| 按列名 | `rs.getString("name")` | ✅ 推荐：列顺序变了不出错 |
| 按下标 | `rs.getString(1)` | ⚠️ **下标从 1 开始**（不是 0！），且和列顺序耦合，少用 |

### 两个脾气

- **只进不退**：默认游标只能 `next()` 往后走，不能回退重读。
- **命短**：它只是"通道上的临时读数"——Connection 一关，ResultSet 立即作废。要长期用就在循环里当场取完、装成对象，别抱着 rs 不放。

## 5. 关闭顺序：rs → stmt → conn

三者是**创建链**：Connection 造出 Statement，Statement 造出 ResultSet。关闭按相反顺序（后开先关，像栈）：

```java
try (Connection conn = DriverManager.getConnection(url, user, pwd);
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
    if (rs.next()) {
        System.out.println("MySQL 版本：" + rs.getString(1));
    }
}   // 自动按 rs → stmt → conn 逆序关闭
```

try-with-resources 和 IO 文档里的用法一致；关最外层 Connection 也会**连锁关闭**它名下的 Statement 和 ResultSet，但三个都显式声明语义最清晰。

> 忘记关的后果不是"浪费一点内存"：数据库连接数有限（`max_connections`），泄漏多了新连接进不来，**整个应用挂掉**。

## 6. 常见坑速查

| 坑 | 后果 / 报错 | 避免 |
|---|---|---|
| 忘了 `rs.next()` 就取值 | `Before start of result set` | 游标初始在第一行**之前**，先 next |
| 按下标从 0 取 | 报错或取错列 | JDBC 下标从 1 起；推荐用列名 |
| Connection 关了还用 rs | `Operation not allowed after ResultSet closed` | 循环里当场取完再关 |
| 只开不关 | 连接泄漏，连接数耗尽 | try-with-resources，顺序 rs → stmt → conn |
| 查询/更新用错方法 | SELECT 配 `executeUpdate` 拿不到结果 | 查 = `executeQuery`，改 = `executeUpdate` |
| SQL 拼接字符串 | SQL 注入 | 后续换 `PreparedStatement` 的 `?` 占位符 |

---

**记忆口诀**：拨号靠 `DriverManager`，通话是 `Connection`，说话用 `Statement`，听话拿 `ResultSet`——**游标先 next，下标从 1 起，用完按 rs → stmt → conn 顺序挂断**。
