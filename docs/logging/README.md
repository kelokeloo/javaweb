# 日志：SLF4J + Logback 入门到企业实践

用 SLF4J 写代码，让 Logback 去输出——这是 Spring Boot 的默认日志方案。

## 怎么读

**代码是主角，本档只补它表达不了的。** 每个 demo 都能直接跑、注释已写明"为什么这样写、跑起来看什么"。所以先跑、先看代码。

本档只留四类内容：**决策/比较表**（级别怎么选）、**概念模型**（层级树、三种接入方式）、**跨主题联动**（日志 × 框架）、**评审清单 & 速查表**。代码能讲的一律不在这重复。

「上手篇」必学，「原理篇」可跳过。

## 课程索引

| # | 主题 | 入口类（`topics.logging.demo` 包） |
|---|------|--------------------------------|
| 1 | 入门：为什么不用 System.out.println | `LoggingBeginnerDemo` |
| 2 | 级别与占位符：规范的一半在这里 | `LoggingLevelsDemo` |
| 3 | logback.xml：三个出口与输出格式 | `LogbackConfigDemo` + `src/main/resources/logback.xml` |
| 4 | 按包控制级别：压噪音、看 SQL | `HierarchyLevelDemo` |
| 5 | 异常日志与 review 检查清单 | `ExceptionLoggingDemo` |

**原理篇**（可跳过）｜第 6 课：SLF4J 和 Logback 是怎么接上的 —— `Slf4jWiringDemo`
**附录**：A 两条 jar 开膛命令（彩蛋）｜ B 一页速查表

```bash
# 每个 demo 通用（以第 1 课为例，换成别的类名即可）
mvn compile
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.logging.demo.LoggingBeginnerDemo
```

---

# 上手篇

## 第 1 课：为什么不用 println

看 `LoggingBeginnerDemo`。四个硬伤（没级别 / 没上下文 / 目的地写死 / 改行为要改代码）和门面/实现分工，类的 javadoc 已经讲清；跑起来对比 println 与 `log.info`、看 TRACE 不输出、异常自动带堆栈。

本课没有代码表达不了的概念，只要记住一句：**写代码认识 SLF4J，配置行为认 Logback**。

## 第 2 课：级别与占位符

**级别是决策，不是语法。** 判断一问就够了——这句是给谁看的？

| 级别 | 给谁看 | 生产环境 |
|------|--------|---------|
| TRACE | 写这行代码的自己 | 永远关着 |
| DEBUG | 排查问题的开发 | 关着（本项目特意开着） |
| INFO | 运维 / 产品 / 未来的你 | **开着的最低级** |
| WARN | 值班的开发 | 开着 |
| ERROR | 需要立刻看的人 | 开着，且单独落文件（第 3 课） |

两个误区：**INFO 不是"普通日志"的代名词**，量大琐碎该用 DEBUG；**WARN 不是低配 ERROR**，它表示"我处理了但你该知道"，真处理不了就 ERROR。

占位符为什么存在：看 `LoggingLevelsDemo`，`Loud` 的 `>>>` 出现几次就是答案——①②③ 三层道理代码注释已写。只补一句易忽略的：`{}` 省的是**格式化**，可参数表达式本身仍会求值。

## 第 3 课：logback.xml——三个出口与输出格式

看 `src/main/resources/logback.xml`。三个概念（appender=去哪、encoder/pattern=每行长啥样、logger=谁打）、pattern 转换符、滚动策略、LevelFilter，注释都写在这个文件里了。跑 `LogbackConfigDemo`，再看 `logs/` 目录。

只提醒两件事：出口 2 按 **天切 / 封顶 10MB / 留 7 天**是标配（防磁盘撑爆）；出口 3 用 LevelFilter 只放行 ERROR——**告警只盯一个文件**。

## 第 4 课：按包控制级别——压噪音、看 SQL

**层级树是概念，代码表达不了**：logger 名按 `.` 分层，给某个前缀配级别，后代全部继承——配一处、管一片，无需配到每个类。

```text
root (INFO)
├── topics (DEBUG)            ← 以 topics 开头全放行
└── org.apache.ibatis (INFO)  ← 以 org.apache.ibatis 开头压到 INFO
```

看 `HierarchyLevelDemo`：同一句 `debug()`，三个 logger 三种下场。这也解释了第 1 课"logger 名用类全限定名"——名字起着，才能按片管理。

**跨主题（日志 × 框架）就发生在这**：引入 logback.xml 前后，MyBatis demo 输出天差地别。之前 VFS 扫描 class 的噪音刷屏几十行（无配置、全进程 DEBUG），之后只剩三行真 SQL。原因：MyBatis 的 SQL 日志 logger 名是**语句 ID**（`topics.mybatis.mapper.MyBatisStudentMapper.findAll`），吃到 `topics=DEBUG`；框架内部的 VFS 话痨（`org.apache.ibatis.io.*`）被压到 INFO。结果：**框架安静、业务清晰、SQL 还能看**，一行多余配置都没有。

生产配置里没有 `topics=DEBUG`（全进程 INFO）。要排查就用能动态改级别的机制——Spring Boot 的 `/actuator/loggers`，不改代码、不发版。

## 第 5 课：异常日志与 review 检查清单

看 `ExceptionLoggingDemo`。同一个异常四种打法，跑一遍就看出错法丢了什么；包装异常要**传 cause**、异常对象放最后不占位——demo 注释已讲。

review 是评审动作，不属于代码，所以留在这：

- **ERROR 必带异常对象**：`log.error("消息, key={}", v, e)`，`e` 放最后、不占位符。
- **不吞异常**：catch 里什么都不干最容易埋 bug；转抛要带 cause；上下层都记时只记一层，防告警风暴。
- **`e.printStackTrace()` 不算日志**：走 stderr、绕过级别和配置文件。
- **敏感信息不落日志**：密码、token、身份证、完整卡号；要打就打掩码。
- **日志不是给用户看的**：给用户的走响应体；日志给工程师看。
- **循环体内慎打**：一个循环一万行，磁盘和告警一起爆。

---

# 原理篇：日志是怎么"接上"的（可跳过）

> 回答那句迟早会冒出来的"咦，我没配过它俩怎么就通了？"。这是纯概念，代码里只有 `Slf4jWiringDemo` 能验证其中一小条（手写派 == 注解派）。

## 第 6 课：SLF4J 和 Logback 是怎么接上的

**一个进程最终只有一个日志实现在输出。** 你的代码、MyBatis、Spring、老依赖库都会产生日志，但汇到同一个实现上：

```text
你的代码(手写/@Slf4j)   MyBatis   Spring   老依赖库(commons-logging/JUL)
        │                │        │            │
        │                │        │            └ 桥接：同名jar替换老API，转发给SLF4J
        │                │        └ 探测：classpath有SLF4J就接上
        │                └ 同左
        └ 绑定：直接就用 SLF4J
                     ↓
          SLF4J 门面   →   Logback 实现   →   控制台/文件
```

- **绑定（binding）**：SLF4J 运行时要挑实现。恰好一个正常；零个则日志全失效；多个则出 "multiple bindings" 警告、随机挑一个。
- **探测**：框架自带微型日志接口 + 一排适配器，**不自带任何日志框架的类**，按固定顺序试，第一个碰到的就用。
- **桥接（bridge）**：老代码用 commons-logging 或 JDK 自带日志，塞一个同名但改成转发给 SLF4J 的 jar 把它们拉进来。知道有这回事即可——Spring Boot 已替你配好。

**总纲：在 Java 世界里，"往 classpath 放哪些 jar"本身就是配置。** 你只是加了 `logback-classic`，其实已经向所有带探测能力的库广播了自己的日志方案。Spring Boot 的 starter 无非是把这套 jar 组合摆好，再排除掉可能绕过路由的 jar。

**注解只是"贴纸"**：`@Slf4j` 本身没逻辑，是 **Lombok 这个编译期处理器**读它并按模板生成代码。以后见到任何 `@Xxx` 改变行为，第一反应都该是：谁在读它、在什么阶段读？

跑 `Slf4jWiringDemo` 验证：手写派和注解派输出一字不差。想亲手拆 jar 看证据，见附录 A。

---

# 附录

## 附录 A：两条 jar 开膛命令（彩蛋）

纯属好奇，**不学不影响**，当收藏。（前提：`~/.m2` 里已有对应版本的依赖。**① Lombok 的模板是硬编码的**）

```bash
L=~/.m2/repository/org/projectlombok/lombok/1.18.42/lombok-1.18.42.jar
unzip -p $L 'SCL.lombok/lombok/core/handlers/LoggingFramework.SCL.lombok' | strings | grep -iE 'getLogger|slf4j'
```

输出里就有 `org.slf4j.Logger org.slf4j.LoggerFactory.getLogger(TYPE)(TOPIC)`（`TYPE`/`TOPIC` 是编译期占位符）；同一枚举里还躺着 `java.util.logging`、`log4j`、`log4j2` 等模板——`@Log`、`@Log4j`、`@Slf4j` 各引用其中一行。为什么 SLF4J 成了主流？**越常用大家越内置对它的支持，越多支持越常用**——事实标准就是这么形成的。

（**② MyBatis 的探测清单**）

```bash
M=~/.m2/repository/org/mybatis/mybatis/3.5.19/mybatis-3.5.19.jar
unzip -p $M 'org/apache/ibatis/logging/LogFactory.class' | strings | grep -iE 'slf4j|log4j|commons|jdk14|stdout'
```

输出依次是 slf4j / commons / log4j / log4j2 / jdk14 / stdout 六个适配器，固定顺序逐个试。你加依赖前 classpath 没 SLF4J，MyBatis 落到 JDK 自带日志（JUL）；加完第一次运行它自己报到了（当时真实捕获的一行）：

```text
Logging initialized using 'class org.apache.ibatis.logging.slf4j.Slf4jImpl' adapter.
```

你一行"关联配置"都没写它就接上——**约定优于配置**。（想违抗可配 `<setting name="logImpl" .../>`，实战几乎没人写。这条"报到"日志本身是 DEBUG 级、以 `org.apache.ibatis` 开头，第 4 课把它压掉了；临时调低到 DEBUG 再跑一次 mybatis demo 就能看到。）

## 附录 B：一页速查表

| 我想…… | 写法 / 配置 | 备注 |
|---------|-----------|------|
| 拿一个 logger | `private static final Logger log = LoggerFactory.getLogger(本类.class);` | 一个类一个 |
| 或注解 | `@Slf4j`（Lombok）+ 直接用 `log` | 生成的就是上面那行（第 6 课） |
| 打一条日志 | `log.info("订单 {} 已创建", id);` | 用 `{}`，别拼接 |
| 记异常 | `log.error("支付失败, orderId={}", id, e);` | `e` 放最后、不占位符（第 5 课） |
| 选级别 | 问"这句是给谁看的？" | TRACE/DEBUG/INFO/WARN/ERROR（第 2 课） |
| DEBUG 时别白干 | `if (log.isDebugEnabled()) { ... }` | 只有凑参数很贵才用 |
| 日志写哪 | `<appender>`：Console / File / RollingFile | logback.xml（第 3 课） |
| 归档 | `<rollingPolicy>` 按天切 + `maxFileSize` + `maxHistory` | 按天、封顶、只留 N 天 |
| 只留 ERROR 给告警 | `<filter class="...LevelFilter"><level>ERROR</level>` | `onMatch=ACCEPT`、`onMismatch=DENY` |
| 控制某包级别 | `<logger name="org.apache.ibatis" level="INFO"/>` | 树形继承（第 4 课） |
| 临时看 SQL/调试 | `<logger name="topics" level="DEBUG"/>` | 生产别这么留 |
| 每行格式 | `%d %thread %-5level %logger{36} - %msg%n` | `%L` 偏慢，生产慎用 |

---

## 已知副作用

1. **MyBatis demo 输出变干净了**：`Logging initialized using Slf4jImpl adapter` 表明它自动对接了 SLF4J（此前走 JDK 自带日志、无配置时全量 DEBUG），现在被 logback.xml 统一管束——第 4 课 + 原理篇第 6 课的教材。
2. **Lombok 主题的 `@Log`（走 JDK 日志）不进 Logback**：它生成的字段是 `java.util.logging.Logger`，格式和这里所有 demo 不同——对照原理篇第 6 课。

到 Spring Boot 你会重逢它们：`spring-boot-starter-logging`（原理篇第 6 课的依赖全家桶）、`logging.level.*`（第 4 课的树）、`/actuator/loggers`（第 4 课的动态改级别）。
