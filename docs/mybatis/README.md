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

本入门示例使用 `session.selectList("topics.mybatis.MyBatisStudentMapper.findAll")`，通过 Mapper XML 的 namespace + statement id 找到 SQL。`session.getMapper(...)` 也很常用，但会放到后续“常见用法”示例中专门对比。
