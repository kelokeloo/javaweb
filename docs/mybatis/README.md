# MyBatis 入门

示例使用 MyBatis 3.5.19、Lombok、MySQL 和原生 `SqlSessionFactory`。

## 课程索引（建议按序学习）

| # | 主题 | 入口类（`topics.mybatis.demo` 包） |
|---|------|--------------------------------|
| 1 | 入门：Mapper 接口调用 XML 查询 | `MyBatisBeginnerDemo` |
| 2 | resultMap 显式映射与 DTO | `MyBatisResultMapDemo` |
| 3 | CRUD 与事务边界 | `MyBatisCrudDemo` |
| 4 | 一对多 / 多对一嵌套映射 | `MyBatisOrderCustomerDemo` |
| 5 | 动态 SQL 六大标签 | `MyBatisDynamicSqlDemo` |
| 6 | 纯注解 Mapper | `MyBatisAnnotationDemo` |

### 代码结构

学完后的代码按真实项目惯例分成四个子包：

```
src/main/java/topics/mybatis/
├── common/   MyBatisUtils —— 全局唯一的 SqlSessionFactory，所有 Demo 共用
├── entity/   实体和 DTO，对应数据库表或查询结果
├── mapper/   Mapper 接口，只声明数据访问方法
└── demo/     每节课的演示入口，main 方法只关心业务 SQL
src/main/resources/
├── mybatis-config.xml
├── mybatis-db.properties
└── topics/mybatis/mapper/    与接口同包路径的 Mapper XML
```

两个设计点：

- **SqlSessionFactory 只建一次**（`MyBatisUtils` 静态代码块），SqlSession 每个演示现开现关——这正是官方文档推荐的生命周期用法。
- **`mybatis-config.xml` 用 `<package>` 包扫描**：`topics.mybatis.entity` 自动注册类型别名，`topics.mybatis.mapper` 自动注册 Mapper（同目录同名 XML 自动加载，无 XML 的接口则解析注解）。新增实体或 Mapper 不需要再改配置文件。

## 分课讲解

### 准备数据库

确认 MySQL 已启动；如有需要，修改项目根目录下 `src/main/resources/mybatis-db.properties` 中的账号密码（默认 `root / 123456`）：

```bash
mysql -h127.0.0.1 -P3306 -uroot -p < sql/mybatis/01-init.sql
mysql -h127.0.0.1 -P3306 -uroot -p -e "USE mybatis_learning; SHOW TABLES; SELECT * FROM mybatis_student;"
```

脚本会创建 `mybatis_learning` 数据库和 `mybatis_student` 表；只有空表时才插入示例数据，可重复执行。

`src/main/resources` 是 Java/Maven 项目放置运行时资源的常见目录：Java 源码在 `src/main/java`，XML、properties 等配置在 `resources`，构建时会复制到 classpath，程序可用 `Resources.getResourceAsReader` 读取。Lombok 通过编译期注解（本例的 `@Data`）生成实体类的 getter、setter 和 `toString`。

### 运行 MyBatis 查询

```bash
mvn compile
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.demo.MyBatisBeginnerDemo
```

也可以在 IDEA 中运行 `topics.mybatis.demo.MyBatisBeginnerDemo`。

本入门示例使用 `session.getMapper(MyBatisStudentMapper.class)` 获取 Mapper 接口，再调用 `mapper.findAll()` 查询数据。SQL 仍定义在 Mapper XML 中，由接口方法名和 XML 中的 statement id 对应。这样业务代码不需要直接依赖 namespace + statement id 字符串，是 MyBatis 更常见的使用方式。

### resultMap 示例

运行 `topics.mybatis.demo.MyBatisResultMapDemo` 可以查看 `resultMap` 的使用方式：

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.demo.MyBatisResultMapDemo
```

`topics/mybatis/mapper/MyBatisStudentMapper.xml` 中的 `studentSummaryMap` 显式配置了数据库列和 Java 属性的对应关系：`student_id` 映射到 `id`，`name` 映射到 `studentName`，`grade` 映射到 `className`。查询通过 `resultMap="studentSummaryMap"` 引用这组映射，而不是使用 `resultType` 的自动映射。

这个查询返回的 `MyBatisStudentSummary` 是一个 DTO。DTO 是 `Data Transfer Object` 的缩写，中文叫“数据传输对象”，用于在不同层或不同系统之间传递数据；它不是 “data to object”。DTO 通常只保存本次场景需要的数据，不负责数据库操作或业务逻辑。

这个例子中 Java 属性名和数据库列名刻意不同，因此能直观看出 `resultMap` 的作用。虽然 SQL 别名也可以实现类似效果，但在字段名复杂、需要类型转换或存在嵌套对象时，`resultMap` 可以提供更集中、更明确的映射配置。

### CRUD 示例

`MyBatisCrudDemo` 演示了常见的增删改查组织方式：

- `MyBatisStudent`：实体对象，承载 SQL 参数和查询结果。
- `MyBatisStudentMapper`：只声明数据访问方法，不写 SQL。
- `MyBatisStudentMapper.xml`：集中编写 `select`、`insert`、`update`、`delete`。
- `MyBatisCrudDemo`：按业务流程获取 Mapper、调用方法并管理事务。

运行：

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.demo.MyBatisCrudDemo
```

这个演示使用 `openSession(false)` 关闭自动提交，插入、修改、删除都针对同一条临时数据，最后调用 `session.rollback()`。因此可以完整观察 CRUD 的执行顺序，同时不会把演示数据真正写入数据库。正式业务需要持久化时，把事务成功路径改为 `session.commit()`，异常路径保留 `rollback()`。

### 复杂查询：订单和客户

订单和客户使用一对多关系：一个客户可以有多个订单，一个订单只属于一个客户。执行 `sql/mybatis/02-order-customer.sql` 创建示例表和数据：

```bash
mysql -h127.0.0.1 -P3306 -uroot -p < sql/mybatis/02-order-customer.sql
```

`MyBatisOrderCustomerDemo` 展示两种嵌套映射：

- `findCustomerWithOrders`：客户包含多个订单，使用 `resultMap` 的 `<collection>`。
- `findOrderWithCustomer`：订单包含一个客户，使用 `resultMap` 的 `<association>`。

初次接触时可以这样理解：`<collection>` 用来接收多行查询结果，并把它们组装成一个集合属性；`<association>` 用来把当前行中的多列组装成一个嵌套对象属性。比如客户查询返回张三和他的两条订单，MyBatis 会把两行订单组装到同一个 `Customer.orders` 列表中；订单查询返回一行订单及客户列，MyBatis 会把客户相关列组装成 `Order.customer` 对象。它们都是 `resultMap` 的对象映射能力，不是 SQL 聚合函数。

运行：

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.demo.MyBatisOrderCustomerDemo
```

两个查询都使用一次 `JOIN` 完成映射。客户查询中的 `LEFT JOIN` 可以保留没有订单的客户；订单查询中的 `INNER JOIN` 则表示订单必须关联一个客户。两套 `resultMap` 不互相继续嵌套，避免 `Customer -> orders -> customer` 的循环对象结构。

### 动态 SQL

订单客户是「关系映射」，动态 SQL 解决的是另一个问题：**根据传参不同拼接出不同的 SQL**。先执行 `sql/mybatis/03-dynamic-sql.sql` 创建 `mybatis_product` 表和示例数据：

```bash
mysql -h127.0.0.1 -P3306 -uroot -p < sql/mybatis/03-dynamic-sql.sql
```

运行：

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.demo.MyBatisDynamicSqlDemo
```

`MyBatisDynamicSqlDemo` 按输出顺序演示六个核心标签：

| 标签 | 作用 | 演示步骤 |
|------|------|---------|
| `<sql>` + `<include>` | 抽取公共列片段，多处复用 | 1 |
| `<where>` + `<if>` | 只拼接非空条件，自动去掉多余的 `AND` | 2、3 |
| `<bind>` | 预先计算变量（如把 `华` 拼成 `%华%`），避免 `${}` 拼接注入风险 | 4 |
| `<choose>` / `<when>` / `<otherwise>` | 多分支二选一，类似 Java 的 if-else if-else | 5 |
| `<set>` + `<if>` | 只更新传入的非空字段，自动去掉末尾逗号 | 6 |
| `<foreach>` | 遍历集合，拼 `IN (...)` 或批量 INSERT | 7、8 |

查询条件用 `MyBatisProductQuery` 这个参数对象承载（哪个字段非空就拼哪个条件），比在接口方法上堆一串 `@Param` 清爽。带写入的步骤 6、8 最后统一 `rollback`，不落库。

### 纯注解 Mapper

简单 SQL 可以不用 XML，直接写在 `@Select` / `@Insert` / `@Update` / `@Delete` 注解里。运行：

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.demo.MyBatisAnnotationDemo
```

`MyBatisAnnotationMapper` 演示了注解写法的完整一套，包括 `@Options(useGeneratedKeys = true, keyProperty = "productId")` 回填自增主键。这个接口没有 XML，靠配置里的 `<package>` 扫描注册。

选 XML 还是注解，看 SQL 复杂度：**一行能读完的简单语句用注解；涉及动态 SQL（`<if>`/`<foreach>`）或嵌套映射（`collection`/`association`）就用 XML**。实际项目里 XML 仍是主力，注解只是补充。

## 小结

到这里 MyBatis 的核心能力就覆盖完了：配置与生命周期（`MyBatisUtils` + `mybatis-config.xml`）、Mapper 代理调用、`resultType`/`resultMap` 两种映射、嵌套关系映射、动态 SQL、注解与 XML 的取舍。下一步进入 Spring 生态后，`mybatis-config.xml` 和 `SqlSessionFactory` 的创建会被 Spring Boot starter 接管，但上面这些概念本身不变。
