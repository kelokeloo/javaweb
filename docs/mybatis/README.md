# MyBatis 入门

示例使用 MyBatis 3.5.19、Lombok、MySQL 和原生 `SqlSessionFactory`。

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
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.MyBatisBeginnerDemo
```

也可以在 IDEA 中运行 `topics.mybatis.MyBatisBeginnerDemo`。

本入门示例使用 `session.getMapper(MyBatisStudentMapper.class)` 获取 Mapper 接口，再调用 `mapper.findAll()` 查询数据。SQL 仍定义在 Mapper XML 中，由接口方法名和 XML 中的 statement id 对应。这样业务代码不需要直接依赖 namespace + statement id 字符串，是 MyBatis 更常见的使用方式。

### resultMap 示例

运行 `topics.mybatis.MyBatisResultMapDemo` 可以查看 `resultMap` 的使用方式：

```bash
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.MyBatisResultMapDemo
```

`MyBatisStudentMapper.xml` 中的 `studentSummaryMap` 显式配置了数据库列和 Java 属性的对应关系：`student_id` 映射到 `id`，`name` 映射到 `studentName`，`grade` 映射到 `className`。查询通过 `resultMap="studentSummaryMap"` 引用这组映射，而不是使用 `resultType` 的自动映射。

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
java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.mybatis.MyBatisCrudDemo
```

这个演示使用 `openSession(false)` 关闭自动提交，插入、修改、删除都针对同一条临时数据，最后调用 `session.rollback()`。因此可以完整观察 CRUD 的执行顺序，同时不会把演示数据真正写入数据库。正式业务需要持久化时，把事务成功路径改为 `session.commit()`，异常路径保留 `rollback()`。
