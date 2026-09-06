# Maven 构建机制：插件、生命周期，以及测试为什么不用 `main`

> 源自 JUnit 主题的两处疑惑：①为什么测试类没有 `main` 也能在 IDEA 里点运行？②`maven-compiler-plugin` / `maven-surefire-plugin` 是不是 Maven 插件，分别干嘛？
> 这篇把它们串成一条线讲，而不是散成点。

**先记一句话总纲：在 Java 世界里，「谁在什么时候对代码做什么」往往不是你的代码自己干的，而是某个工具/框架在背后调度。** 下面分两条线：一条讲「测试怎么被跑起来」，一条讲「编译和测试交给谁」。

---

## 一、为什么测试类没有 `main`？

先分清两种「入口」：

- **`main` 是 JVM 的入口**：命令行 `java 类名`，JVM 找到 `main` 方法并调用。你的 logging、MyBatis demo 都要写 `main`，因为你在给 JVM 写程序。
- **`@Test` 是 JUnit 框架的入口**：测试类是写给 JUnit 的。JUnit 有个「启动器」（JUnit Platform Launcher），它扫描类路径、用反射找出所有带 `@Test` 的方法再逐个调用。**这个 Launcher 自己就是它的 `main`**——所以测试类不用写 `main`。

IDEA 的绿色箭头就是它内置的一个 JUnit Launcher：认出 `@Test`，点一下帮你调用那个 Launcher。你没写 `main`，是 IDEA 替你接上了。

这跟 Servlet 是同一件事：你写 Servlet 从不写 `main`，启动它的是 Tomcat 容器。这叫**框架反转控制（IoC）**——不是你的程序去调用框架，而是框架来调用你的代码。

顺带解释一个相关现象：为什么每个测试方法前都要 `@BeforeEach` 复位？因为 JUnit 默认**每个 `@Test` 方法都在全新的实例上运行**（生命周期 `PER_METHOD`），测试之间不互相污染，所以复位放在 `@BeforeEach`（方法前执行），而不是构造器里。

---

## 二、compiler 和 surefire 是 Maven 插件吗？

**是，两个都是 Maven 插件，不是 IDEA 插件。** 先建立认知：Maven 自己几乎不干活，全靠插件——你敲的每个 `mvn xxx` 都是触发某个插件的某个 goal。插件声明在 `pom.xml` 的 `<build><plugins>` 里，跟 `mvn` 命令绑定，与 IDEA 无关。

Maven 把一次构建拆成固定顺序的**生命周期阶段（phase）**，插件各自「挂」在上面：

```text
validate → compile → test → package → verify → install → deploy
             │        │
             │        └── maven-surefire-plugin
             └── maven-compiler-plugin
```

> 注：`maven-surefire-plugin` 挂在 `test` 阶段；`maven-compiler-plugin` 挂在 `compile` 阶段（它其实还挂在 `test-compile` 阶段、负责把测试代码也编成字节码，这里为聚焦只画 `compile`）。跑 `mvn test` 会先后触发这两个插件。

### 1. `maven-compiler-plugin` —— 干「编译」
挂在 `compile` 阶段，调用 javac 把 `.java` 编成 `.class`。我们配了 `source/target=26`（Java 26）和 `annotationProcessorPaths`（注解处理器 = Lombok）。

你之前确认的 Lombok 理解，在这里可以更精确：不是「编译器发现了 Lombok 注解」，而是 **javac 在编译期读到 `@Data`/`@Slf4j` 这类注解时，把它们委托给 `annotationProcessorPaths` 里声明的处理器 → Lombok 生成 getter/setter、构造器、`log` 字段 → 再一并编译出生效的字节码**。生成发生在编译期，所以你运行时看到的已是「生成之后」的产物。

### 2. `maven-surefire-plugin` —— 干「跑测试」
挂在 `test` 阶段，发现并执行测试。它**按约定**找测试类：文件名以 `Test`/`Tests` 结尾（所以 `CalculatorTest`、`CalculatorMoreTest` 自动被找到——命名本身就是发现机制）。靠 provider（`surefire-junit-platform`）执行 JUnit 5，跑完在 `target/surefire-reports/` 写报告。

### 最该记住的结论：Maven 和 IDEA 是两条独立的「轨道」
**`mvn test` 和 IDEA 里的绿色箭头是两套东西，但通向同一个引擎：**

- `mvn test` → 走 **surefire**（Maven 插件）。
- IDEA 点箭头 → 走 **IDEA 自己的 JUnit Runner**（内置 JUnit Platform Launcher），**不经过 surefire**。
- 但二者最终都调用**同一个 JUnit Platform 引擎**（`junit-jupiter-engine`），区别只是「谁来调用」——一个 IDEA，一个 surefire。

这也再次印证问题①：IDEA 的 Runner 是自带实现，所以既不依赖 `main`，也不依赖 `mvn test`。

---

## 附：一页速查

| 我想…… | 靠谁 | 备注 |
|---|---|---|
| 把代码编成字节码 | `maven-compiler-plugin` | compile 阶段调 javac |
| 让 javac 处理 Lombok 注解 | `annotationProcessorPaths` → Lombok | 编译期生成代码 |
| 命令行跑 JUnit 5 | `maven-surefire-plugin` | test 阶段，按 `*Test` 约定发现 |
| 测试详细报告 | `target/surefire-reports/` | surefire 写出 |
| IDEA 里直接跑某个测试 | IDEA 内置 JUnit Runner | 不走 surefire，走 JUnit Platform |
| 测试类为何不用写 `main` | JUnit Platform Launcher | 框架自带启动器 |

**参考**：被测类 `src/main/java/topics/junit/Calculator.java`；测试 `src/test/java/topics/junit/CalculatorTest.java`（基础）、`CalculatorMoreTest.java`（参数化/嵌套）；插件配置在 `pom.xml`。
