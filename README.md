# JavaWeb 学习项目

这是一个按主题组织的 Java 学习项目。Java 示例代码统一放在 `src/main/java/topics` 下，各主题的详细说明放在 `docs`，数据库脚本放在 `sql`。

## 学习主题

- [JDBC 文档](docs/java-jdbc.md) · 代码：`src/main/java/topics/jdbc`
- [MyBatis 入门](docs/mybatis/README.md) · 代码：`src/main/java/topics/mybatis`
- [Lombok 常用注解](docs/lombok/README.md) · 代码：`src/main/java/topics/lombok`
- [Socket 示例](src/main/java/topics/socket) · 代码：`src/main/java/topics/socket`
- [SQL 语言](docs/sql-languages/README.md)
- [JUnit 测试](src/main/java/topics/junit) · 代码：`src/main/java/topics/junit` + `src/test/java/topics/junit`

## Maven 项目目录约定

- `src/main/java`：Java 源代码
- `src/main/resources`：XML、properties 等运行时资源
- `src/test/java`：单元测试（JUnit）——被测代码归 `main`，测试归 `test`
- `docs`：学习笔记和运行说明
- `sql`：数据库初始化及练习脚本
