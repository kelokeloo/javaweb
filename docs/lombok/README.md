# Lombok 常用注解

Lombok 在编译阶段生成重复代码。源码里没有手写 `getName()`，编译后的类里仍然有这个方法。它不是运行时通过反射替你赋值，也不会替代业务校验。

示例位于 `src/main/java/topics/lombok`，按用途分组。每个文件包含一个可独立运行的 `main`，内部的静态类就是演示用模型，方便在同一文件对照注解与调用。无需数据库、网络或额外的日志库。

## 学习顺序与分类

| 顺序 | 分组 / 示例 | 注解 | 重点 |
| --- | --- | --- | --- |
| 1 | [basics/GetterSetterDemo](../../src/main/java/topics/lombok/basics/GetterSetterDemo.java) | `@Getter`、`@Setter`、`AccessLevel.NONE` | 自动生成访问方法；类级与字段级设置 |
| 2 | [constructors/ConstructorDemo](../../src/main/java/topics/lombok/constructors/ConstructorDemo.java) | `@NoArgsConstructor`、`@AllArgsConstructor`、`@RequiredArgsConstructor` | 无参、全参、必需参数三种构造方式 |
| 3 | [objectmethods/ObjectMethodsDemo](../../src/main/java/topics/lombok/objectmethods/ObjectMethodsDemo.java) | `@ToString`、`@ToString.Exclude`、`@EqualsAndHashCode`、`.Include` | 排除密码输出，只按编号判断相等与去重 |
| 4 | [basics/DataDemo](../../src/main/java/topics/lombok/basics/DataDemo.java) | `@Data` | 将访问方法、对象方法、必需参数构造组合起来 |
| 5 | [builder/BuilderDemo](../../src/main/java/topics/lombok/builder/BuilderDemo.java) | `@Builder`、`@Builder.Default`、`@Singular` | 命名式构建、默认值、集合、`toBuilder()` |
| 6 | [immutable/ValueWithDemo](../../src/main/java/topics/lombok/immutable/ValueWithDemo.java) | `@Value`、`@With` | 不可变数据对象与局部修改后的副本 |
| 7 | [utilities/NonNullDemo](../../src/main/java/topics/lombok/utilities/NonNullDemo.java) | `@NonNull` | 方法参数检查与预期异常 |
| 8 | [utilities/AccessorsDemo](../../src/main/java/topics/lombok/utilities/AccessorsDemo.java) | `@Accessors` | 链式 setter 与 fluent 命名的区别 |
| 9 | [utilities/CleanupDemo](../../src/main/java/topics/lombok/utilities/CleanupDemo.java) | `@Cleanup` | 作用域结束后关闭资源 |
| 10 | [logging/LogDemo](../../src/main/java/topics/lombok/logging/LogDemo.java) | `@Log` | 自动生成 JDK 日志字段，了解与 `@Slf4j` 的区别 |

## 怎么运行

在 IDE 中打开任意 Demo，运行 `main` 即可。也可以在项目根目录先编译，再选择一个类运行：

```bash
mvn compile
java -cp target/classes topics.lombok.basics.GetterSetterDemo
java -cp target/classes topics.lombok.constructors.ConstructorDemo
java -cp target/classes topics.lombok.objectmethods.ObjectMethodsDemo
java -cp target/classes topics.lombok.basics.DataDemo
java -cp target/classes topics.lombok.builder.BuilderDemo
java -cp target/classes topics.lombok.immutable.ValueWithDemo
java -cp target/classes topics.lombok.utilities.NonNullDemo
java -cp target/classes topics.lombok.utilities.AccessorsDemo
java -cp target/classes topics.lombok.utilities.CleanupDemo
java -cp target/classes topics.lombok.logging.LogDemo
```

项目沿用现有 `pom.xml` 的 Java 26、Lombok 1.18.42 和注解处理器配置。这些示例运行时不需要把 Lombok jar 加入 classpath。

运行后重点观察：getter 示例输出“小明 / true / 1001”；对象方法示例去重后数量为 `1`；Data 示例相等结果从 `true` 变为 `false`；Builder 默认容量为 `30`，副本变为 `50` 而原对象保持 `30`；Value 示例修改街道后原地址保留；NonNull 示例捕获并打印预期异常。日志示例默认通过 JDK 日志处理器输出，通常出现在标准错误流。

## 注解到底省了哪些代码

以 `@Getter @Setter private String name;` 为例，主要相当于手写：

```java
public String getName() {
    return this.name;
}

public void setName(String name) {
    this.name = name;
}
```

`@RequiredArgsConstructor` 的参数是**未初始化的 final 字段和未初始化的 @NonNull 字段**，按照字段声明顺序排列。普通字段不自动成为参数，`final` 也不意味着非空。

`@Data` 组合了 `@Getter`、非 final 字段的 `@Setter`、`@ToString`、`@EqualsAndHashCode` 和 `@RequiredArgsConstructor`。它不等于“无参构造 + 全参构造”，也不意味着不可变。已有手写构造方法时，不能再假定它会自动生成所需构造方法。

## 常见易错点

- **Builder 默认值**：通过 builder 构建时，普通非 final 字段的初始化表达式不能直接当作 builder 默认值，使用 `@Builder.Default`。显式设置 `0` 或 `null` 不会回退到默认值。
- **Builder 与构造方法**：已有显式构造方法时，可以把 `@Builder` 放在目标构造方法上。不要随意把类级 `@Builder` 与 `@NoArgsConstructor` 堆在一起。`@Builder` 本身不生成 getter/setter。
- **集合与复制**：`@Singular` 提供单个添加、批量添加和清空方法，生成的集合不可修改，但不会深拷贝集合元素。`toBuilder()` 也不是深拷贝。详见 [Builder 官方说明](https://projectlombok.org/features/Builder)。
- **不可变边界**：`@Value` 默认将类和字段设为 final，不生成 setter；如果字段引用可变对象，仍需自己处理防御性复制。详见 [Value 官方说明](https://projectlombok.org/features/Value)。
- **相等与日志**：`@Data` 默认会让字段参与相等判断和字符串输出。实体有敏感字段、双向关联或可变标识时，应单独设计 `@ToString`、`@EqualsAndHashCode`，避免泄露字段、递归输出或破坏哈希集合查找。
- **非空不是业务校验**：`@NonNull` 拒绝 null，但允许空字符串。必要时还需要手写校验。
- **链式访问与 Builder 不同**：链式 setter 修改当前对象，builder 在 `build()` 时创建对象。`@Accessors` 本身不生成访问方法；`fluent = true` 改变 JavaBean 命名习惯。它属于 experimental 包，详见 [Accessors 官方说明](https://projectlombok.org/features/experimental/Accessors)。
- **资源关闭**：`@Cleanup` 可省去清理代码，但 AutoCloseable 资源通常优先使用 Java 原生 `try-with-resources`，其异常保留机制更完善。
- **日志依赖**：本主题使用 `@Log`，直接配合 JDK。后续使用 `@Slf4j` 时，需要 SLF4J API 及运行时日志实现；Lombok 只生成日志字段。

## 查看生成的方法

编译后可以查看类的方法签名，对照注解理解结果；内部类名包含 `$`，命令中用单引号保护：

```bash
javap -p -classpath target/classes 'topics.lombok.basics.DataDemo$Student'
```

如果 Maven 编译正常而 IDE 报“找不到 getter”，检查 IDE 的 Lombok 支持和注解处理设置，并重新导入 Maven 项目。
