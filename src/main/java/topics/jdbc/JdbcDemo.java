package topics.jdbc;

import java.sql.*;

/**
 * JDBC 入门演示 —— 依赖由 Maven 管理。
 *
 * 驱动坐标声明在根目录 pom.xml（com.mysql:mysql-connector-j，scope=runtime）：
 *   - 编译时不需要它：源码只 import 了 JDK 自带的 java.sql 标准接口
 *   - 运行时必须有它：DriverManager 按连接 url 反射加载驱动实现类
 *
 * Maven 替你把"运行地图"拼好（target/classes + ~/.m2 里的驱动 jar），
 * 在 IDEA 中直接运行本类即可；命令行方式：
 *   mvn compile
 *   java -cp "target/classes:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" topics.jdbc.JdbcDemo
 */
public class JdbcDemo {
    public static void main(String[] args) throws Exception {
        // 1. 拿连接：DriverManager 遍历已注册驱动，按 url 里的 "jdbc:mysql" 匹配
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/", "root", "123456");

        // 关键观察点：Connection 只是接口，真正干活的实现类来自驱动 jar
        System.out.println("连接的运行时类型：" + conn.getClass().getName());

        // 2. 写 SQL
        Statement stmt = conn.createStatement();

        // 3. 执行 + 4. 取结果
        ResultSet rs = stmt.executeQuery("SELECT VERSION()");
        rs.next();
        System.out.println("MySQL 版本：" + rs.getString(1));

        // 5. 关资源（顺序：rs -> stmt -> conn）
        rs.close();
        stmt.close();
        conn.close();
    }
}
