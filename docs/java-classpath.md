# Classpath：从 -cp 到 Maven/Gradle 的依赖管理

> **Classpath 是 JVM 找类的地图；Maven/Gradle 做的全部事情，就是帮你自动拼出这条 `-cp` 字符串。** 理解了这一点，`pom.xml` 就不再神秘——它是"依赖声明 + 自动拼地图"；理解了"编译时和运行时是两套地图"，scope 的每个取值就都有了着落。

## 1. Classpath 是什么：JVM 找类的地图

你说"我要用 `com.example.App` 这个类"，JVM 说"好的，但我不知道它在哪，你得给我一张地图"。这张地图就是 **Classpath（类路径）**：JVM 查找 `.class` 文件和其他资源（`.properties`、`.xml`）的路径集合。

类比操作系统的 `PATH`：`PATH` 告诉系统去哪找可执行程序，`CLASSPATH` 告诉 JVM 去哪找类。

地图上有两种条目：

| 类型 | 比喻 | 例子 | JVM 怎么找 |
|---|---|---|---|
| 目录 | 书架 | `out/` | 按包路径拼：`com.example.App` → `out/com/example/App.class` |
| Jar 包 | 压缩好的书箱 | `lib/myutils.jar` | 直接在压缩包内按包路径找 |

> ⚠️ **Classpath 指向的是 `.class` 的根目录（或 jar 本身），不是 `.java` 源文件所在的目录。** 这是最常见的理解错误，第 2 节会亲手踩一遍。

找类的过程：拿到全限定类名 → 按地图顺序逐个条目找 → 找到就加载；全找完都没有 → `ClassNotFoundException`。

## 2. 动手用 -cp：javac 和 java 怎么给地图

以一个依赖了 `myutils.jar` 的小项目为例，两条命令各给一张地图：

```bash
# 编译：javac 的 -cp 是"编译地图"——只需要理解源码所需的 jar
javac -cp libs/myutils.jar -d out src/com/example/App.java

# 运行：java 的 -cp 是"运行地图"——自己的 out + 全部依赖 jar
java -cp out:libs/myutils.jar com.example.App
```

把运行命令拆开看，有两个容易混的概念：

| 概念 | 作用 | 例子里是谁 |
|---|---|---|
| `-cp`（搜索路径） | 告诉 JVM **去哪找**类 | `out:libs/myutils.jar` |
| 主类名（入口） | 告诉 JVM **从哪个类开始执行** | `com.example.App` |

```bash
java -cp out:libs/myutils.jar com.example.App
#    ^^^^^^^^^^^^^^^^^^^^^^^  ^^^^^^^^^^^^^^^
#    搜索路径（地图）             入口（主类全限定名）
```

条目之间用分隔符隔开：**Linux/Mac 用 `:`，Windows 用 `;`**。顺序即优先级：先搜 `out`（自己的类），再搜 `myutils.jar`。

### 编译和运行，为什么是两条不同的命令？

**编译时**，`javac` 只需要"理解你的源码"所需的类；**运行时**，JVM 需要执行 `.class` 指令所需的**所有**类（包括自己的）。所以：

```bash
javac -cp <源码直接引用的 jar>  -d out src/com/example/App.java
java  -cp <out + 全部依赖 jar>  com.example.App
#      ^^^ 自己的类也要放进去，别漏了
```

> 为什么"编译需要"和"运行需要"不是一回事？第 4、5 节展开。

### 最容易踩的三个错

先看一个不带依赖的最小例子（`javac -d out src/com/example/App.java` 把 `.class` 输出到 `out/`，保留包结构）：

```bash
# ✅ 正确：classpath 指向 .class 的【根目录】
java -cp out com.example.App

# ❌ 错误：指向了包目录
java -cp out/com/example com.example.App
# Error: Could not find or load main class com.example.App

# ❌ 错误：什么都没指定 → 地图只有当前目录 .
java com.example.App
# Error: Could not find or load main class com.example.App
```

### 规则与陷阱速查

| 场景 | 行为 | 注意 |
|---|---|---|
| 什么都没指定 | 默认地图 = 当前目录 `.` | 只够最简单的单文件程序 |
| 用了 `-cp` | **完全覆盖**环境变量 `CLASSPATH` | 最常用、最推荐 |
| 用了 `-jar` | **忽略 `-cp` 和环境变量** | 依赖只能写在 `MANIFEST.MF` 里 |
| 通配符 `lib/*` | 匹配目录下所有 jar | 只匹配 jar，不递归子目录 |
| 环境变量 `CLASSPATH` | 全局生效 | 不推荐：所有项目共用一张地图，容易类冲突 |

`-jar` 那条最反直觉：`java -cp lib/xxx.jar -jar myapp.jar` 里的 `-cp` **被完全忽略**，可执行 jar 的依赖必须声明在 `META-INF/MANIFEST.MF`（路径相对 jar 所在位置）：

```text
Main-Class: com.example.App
Class-Path: lib/myutils.jar
```

### 调试：看实际生效的地图

遇到 `ClassNotFoundException` / `NoClassDefFoundError` 时三招排查：

```bash
java -verbose:class -cp ... com.example.Main   # ① 打印每个类的加载来源
```

```java
System.getProperty("java.class.path");          // ② 运行时获取地图内容
X.class.getProtectionDomain().getCodeSource();  // ③ 某个类实际从哪个 jar 加载的
```

## 3. Maven/Gradle 在做什么：把拼地图自动化

一句话：**依赖声明 → 依赖解析 → 下载缓存 → 按 scope 拼 `-cp` 字符串 → 交给 `javac` / `java`。** 底层没有任何新东西，还是第 2 节那两条命令。

```mermaid
flowchart LR
    A["你写一行声明<br/>implementation 'guava:32.1.2'"] --> B["依赖解析<br/>递归展开传递依赖<br/>+ 版本仲裁"]
    B --> C["下载 / 本地缓存<br/>~/.m2 或 ~/.gradle/caches"]
    C --> D["按 scope 拼 -cp 串<br/>编译 / 运行 / 测试三张地图"]
    D --> E["javac -cp 'a.jar:b.jar:...' -d target/classes ..."]
    E --> F["java -cp 'target/classes:a.jar:b.jar:...' 主类"]
```

**传递依赖**：你只声明了 guava，它自己还要依赖 failureaccess、jsr305 等——构建工具递归展开这棵树，全部加进地图：

```text
guava:32.1.2-jre
├── failureaccess:1.0.1
├── jsr305:3.0.2
├── checker-qual:3.33.0
└── ...
```

**本地缓存**：jar 下载一次全机器共用，按 `groupId/artifactId/version` 存放：

```text
Maven:  ~/.m2/repository/com/google/guava/guava/32.1.2-jre/guava-32.1.2-jre.jar
Gradle: ~/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/...
```

**实际生成的命令**长这样（这就是你声明依赖后，构建工具替你做的事）：

```bash
javac -cp "~/.m2/.../guava.jar:~/.m2/.../jsr305.jar:..." -d target/classes src/main/java/com/example/App.java
java  -cp "target/classes:~/.m2/.../guava.jar:..." com.example.App
```

### 想亲眼看地图？两条命令

```bash
# Maven：看依赖树 / 导出实际 classpath
mvn dependency:tree
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt && cat cp.txt

# Gradle：看依赖树 / 打印实际 classpath
gradle dependencies --configuration runtimeClasspath
```

Gradle 打印 classpath 需在 `build.gradle` 注册一个任务：

```groovy
tasks.register('printClasspath') {
    doLast { println sourceSets.main.runtimeClasspath.asPath }
}
```

> `dependency:tree` 里看到的 `(omitted for conflict with x.y.z)` 就是**版本仲裁**的结果：同一个 jar 出现在依赖树的多条路径上时，Maven 选路径最短的（最近优先），Gradle 默认选版本最高的。

### Maven vs Gradle

| 维度 | Maven | Gradle |
|---|---|---|
| 依赖声明 | XML，固定结构 | Groovy/Kotlin DSL，可编程 |
| 版本仲裁 | **最近优先**（依赖路径最短的赢） | **最高版本优先**（可配置） |
| classpath 分组 | `<scope>` 固定几种 | configurations 可自定义任意组合 |
| 增量编译 | 全量重新编译 | 支持增量，只编译变化的类 |
| 本质 | 固定生命周期 + 插件 | 依赖图任务引擎 |

## 4. scope：编译时和运行时是两套地图

### 为什么要有两套

| 阶段 | 地图里有什么 | 给谁用 |
|---|---|---|
| 编译时（compile） | 源码直接引用的类 | `javac` |
| 运行时（runtime） | 编译时依赖 + 运行时才需要的类 | `java` |
| 测试时（test） | 运行时依赖 + 测试框架 | 测试运行器 |

如果所有依赖一股脑进所有地图，问题就来了——下面四个场景，每一个都对应一种 scope。

### 场景一：Lombok → 编译时有，运行时没有（`provided` / `compileOnly`）

```java
@Data
public class User {
    private String name;
}
```

编译时：`javac` 需要 Lombok 的注解处理器，在编译期把 `getName()`、`setName()` 等方法生成进 `.class`；运行时：产物已经是普通字节码，JVM 根本不知道 Lombok 的存在。多打进去 = 白白增加 jar 体积和攻击面（为什么能改产物，见第 6 节）。

### 场景二：数据库驱动 → 运行时有，编译时没有（`runtime` / `runtimeOnly`）

```java
import java.sql.*;   // JDK 自带的标准接口，不需要任何外部 jar
Connection conn = DriverManager.getConnection(url, user, password);
```

- 编译时：`DriverManager`、`Connection` 全在 JDK 里，**不需要驱动 jar**
- 运行时：`DriverManager` 内部反射加载驱动实现类（如 `com.mysql.cj.jdbc.Driver`）——这个类只在驱动 jar 里，**地图上没有它就连不上库**

如果错标成默认 scope（编译也有）：你可能不小心 `import com.mysql.cj.xxx` 内部类，写出和 MySQL 绑死的代码——违背"面向标准接口编程"的设计初衷。

### 场景三：Servlet API → 编译用，运行时容器提供（`provided`）

```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet { ... }
```

编译时需要 `javax.servlet-api.jar` 才能通过；运行时 Tomcat 自带 Servlet API，应用里再带一份 → 两份同名类，版本不一致就 `NoSuchMethodError` / `ClassCastException`。**这是 Java Web 最经典的 classpath 冲突**，后面写 Servlet 时会真实遇到。

### 场景四：JUnit → 只在测试时（`test`）

```java
@Test
void shouldReturnUser() { ... }
```

`mvn compile` 时 JUnit 不在地图上；`mvn test` 时加入；`mvn package` 打出的产物里**不含** JUnit——测试框架进生产包毫无意义。

### scope 对照表

| scope（Maven / Gradle） | 编译 | 运行 | 测试 | 典型例子 | 前端等价 |
|---|---|---|---|---|---|
| `compile` / `implementation`（默认） | ✅ | ✅ | ✅ | Guava、Spring Core | `dependencies` |
| `provided` / `compileOnly` | ✅ | ❌ | ✅ | Lombok、Servlet API | `peerDependencies` |
| `runtime` / `runtimeOnly` | ❌ | ✅ | ✅ | 数据库驱动、Logback | 运行时动态加载 |
| `test` / `testImplementation` | ❌ | ❌ | ✅ | JUnit、Mockito | `devDependencies` 里的测试库 |

标错的三大后果：

| 后果 | 说明 |
|---|---|
| 产物膨胀 | Lombok + JUnit + Servlet API 全打进去，部署包体积翻倍 |
| 类冲突 | 两份同名类在不同 jar 里，JVM 按地图顺序加载，行为不可预测 |
| 泄露抽象 | 编译期能 import 到不该看的类（如驱动内部类），写出不可移植的代码 |

### 前端视角：为什么 Java 不能像 Vite 那样自动判断？

Vite 项目里你从没手动区分过依赖归属——它分析 `import` 语句就知道了：被源码 import 的进产物，只在 config/测试文件里出现的是开发依赖。

Java 做不到，因为 **`import` 的语义不完整**，Maven/Gradle 干脆不读你的源码，scope 必须你亲手标：

| 你写的 import | 构建工具能推断出什么？ |
|---|---|
| `import java.sql.Connection` | 来自 JDK，不需要任何外部 jar |
| `import lombok.Data` | 来自 lombok.jar，但运行时不需要 |
| （什么都没写） | 运行时可能反射加载驱动实现类——根本不出现在 import 里 |

| 维度 | 前端（Vite） | Java（Maven/Gradle） |
|---|---|---|
| 依赖类型判定 | 自动：分析 import 的 AST | 手动：读配置文件里的 scope |
| 是否读源码 | ✅ 是 | ❌ 否 |
| 标错的后果 | 构建时通常就能发现 | 可能到运行时/生产环境才暴露 |

## 5. 判断标准：编译后 .class 里还剩什么引用

"编译需要"和"运行需要"的分界线，一句话：

> **编译需要 = `javac` 需要这个 jar 才能理解你的源码、生成 `.class`；运行需要 = JVM 执行 `.class` 中的指令时还需要它。**

所以判断一个依赖该不该进运行时地图，看**编译后的 `.class` 里有没有对它的引用**：

- **Lombok：引用消失了。** `import lombok.Data;` 这行不会进字节码，getter/setter 已经是普通的 Java 方法——产物和手写没区别，JVM 不需要 Lombok。
- **Guava：引用还在。** 源码里的 `ImmutableList.of("a", "b")` 编译成字节码里的 `invokestatic com/google/common/collect/ImmutableList.of(...)`——执行这条指令必须要有 guava.jar。

| 依赖 | .class 里还有引用吗 | 运行时需要吗 |
|---|---|---|
| Lombok | ❌ 没有（注解处理器已生成普通代码） | ❌ 不需要 |
| MapStruct | ❌ 没有（生成了普通 Impl 类） | ❌ 不需要 |
| Guava | ✅ 有（方法调用直接指向 Guava 的类） | ✅ 需要 |
| Spring Core | ✅ 有（注解元数据 + 反射引用保留在字节码中） | ✅ 需要 |
| Servlet API | ✅ 有（你的类 `extends HttpServlet`） | ⚠️ 需要，但容器已提供 |
| 数据库驱动 | ❌ 没有（字节码里只引用 `java/sql/DriverManager` 这类标准接口） | ✅ 需要（反射加载） |

用 `javap` 亲手验证：

```bash
javap -p target/classes/com/example/User.class
# public java.lang.String getName()     ← Lombok 生成的，方法已真实存在
# public void setName(java.lang.String) ← 产物是普通类，没有 lombok 的影子
```

对一个只用 `java.sql` 的类做 `javap -c`，字节码里也只有 `java/sql/DriverManager` 这类标准接口，一个 `com/mysql` 都没有——驱动实现类是运行时才被反射加载的。

## 6. 延伸：javac 的扩展机制——Lombok 凭什么改产物？

第 5 节留了个疑问：Lombok 写在你的源码里，凭什么能改变编译**产物**？答案和 Vite 一样——**编译器有插件机制**。

```text
前端： 源码(.jsx)  → [Vite + 插件]       → 产物(.js)     插件把 JSX 转成 createElement，产物里没有 JSX
Java： 源码(.java) → [javac + 注解处理器] → 产物(.class)  处理器往 AST 里插方法，产物里没有 Lombok
```

**注解处理器（Annotation Processor）** 的工作方式：

1. **注册**：jar 里的 `META-INF/services/javax.annotation.processing.Processor` 声明处理器（SPI 机制，`javac` 自动发现）
2. **时机**：语义分析之后、字节码生成之前
3. **动作**：拿到 `javac` 内部的 **AST（抽象语法树）**，直接往里插入方法节点；`javac` 继续编译这棵被改过的树
4. **结果**：`User.class` 里包含生成的全部方法，且**不含任何 lombok 引用**

```text
javac 开始编译 User.java
    → 解析源码，生成 AST
    → 发现 @Data 注解，查找注册的处理器
    → Lombok.process()：往 AST 插入 getName() / setName() / equals() / hashCode() / toString()
    → javac 编译"被修改后的 AST" → User.class
    → 产物含全部方法，无 lombok 引用
```

| 维度 | Vite 插件 | Lombok（注解处理器） |
|---|---|---|
| 宿主程序 | Vite 打包器 | javac 编译器 |
| 扩展机制 | `plugins: [...]` 配置 | `META-INF/services` SPI 自动发现 |
| 介入时机 | 构建过程中拦截模块 | 编译过程中处理注解 |
| 操作对象 | 源码字符串 / AST | javac 内部 AST |
| 运行时是否需要 | ❌ 不需要 | ❌ 不需要 |

**Java 的编译期扩展不止注解一种**，一共三层：

| 扩展层 | 注册方式 | 能力 | 代表工具 |
|---|---|---|---|
| 注解处理器（AP） | SPI：`META-INF/services/...Processor` | 读/改 AST、生成新文件；只能处理带注解的元素 | Lombok、MapStruct |
| 编译器插件 | SPI：`com.sun.source.util.Plugin` | 介入编译任意阶段、注入 lint 检查；用内部 API，不保证跨 JDK 兼容 | Error Prone、NullAway |
| 字节码操作 | 编译后 / 类加载前直接改字节码 | 最底层，任意改写 | ASM、ByteBuddy、AspectJ、JaCoCo |

常见用途速览：

| 场景 | 代表工具 | 做了什么 |
|---|---|---|
| 消除 getter/setter 样板 | Lombok | 编译期生成方法进 .class |
| 对象映射代码生成 | MapStruct | 按接口定义生成 Impl 实现类 |
| 编译期依赖注入 | Dagger | 生成 DI 容器代码，零反射 |
| 编译期静态检查 | Error Prone、NullAway | 在编译期发现 bug，比上线早得多 |
| 测试覆盖率插桩 | JaCoCo | 字节码层面插入计数器 |
| AOP 切面织入 | AspectJ、ByteBuddy | 在已有方法前后插入横切逻辑 |

> 对照记忆：**编译期增强**（Lombok）在 javac 里跑，产物就是普通字节码，运行时不存在；**运行期增强**（Spring AOP / CGLIB）在 JVM 里跑，靠代理/字节码改写，运行时确实需要。整个生态的趋势是从运行时反射转向编译期生成（Micronaut、Quarkus）：启动快、内存省、错误更早暴露。

---

**记忆口诀**：classpath 是 JVM 找类的地图，`-cp` 显式给图、顺序即优先级；Maven/Gradle = 声明依赖 → 解析 → 缓存 → **自动拼图**；scope 决定进哪几张图（编译 / 运行 / 测试）；进不进运行时地图，看**编译后 `.class` 里还剩不剩对它的引用**——Lombok 是编译器的插件，产物里没有它的影子。
