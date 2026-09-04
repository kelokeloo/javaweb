package topics.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 在 Java 里跑一遍 SQL 四类语言里最常用的三类：DDL / DML / DQL。
 *
 * 对照记忆（和 docs/sql-languages 一致）：
 *   DDL  改结构  —— CREATE / ALTER / DROP     → Statement.executeUpdate
 *   DML  改数据  —— INSERT / UPDATE / DELETE  → Statement.executeUpdate
 *   DQL  只看    —— SELECT                    → Statement.executeQuery
 *
 * JDBC 的分工就这一条：要结果集用 executeQuery，改库（结构或数据）用 executeUpdate。
 * 本类可反复运行：每次先 DROP 再建，不污染你平时用的库。
 *
 * 运行方式：
 *   - IDEA：右键 Run 'SqlDemo.main()'
 *   - 命令行：mvn compile 后执行 java -cp "target/classes:$(mvn -q dependency:build-classpath)" topics.jdbc.SqlDemo
 *
 * 前置：先跑通 {@link JdbcDemo}（连上 + 打印版本）。
 */
public class SqlDemo {

    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            // 1. 建库并切换
            createAndUseDatabase(stmt);

            // 2. 建表
            createTable(stmt);

            // 3. 插入数据并查看
            insertStudents(stmt);
            printAllStudents(stmt);

            // 4. 更新数据并查看
            updateStudentScore(stmt);
            printAllStudents(stmt);

            // 5. 删除数据并查看
            deleteStudent(stmt);
            printAllStudents(stmt);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createAndUseDatabase(Statement stmt) throws SQLException {
        // 关键观察点：executeUpdate 返回的是"受影响行数"，DDL 通常返回 0
        stmt.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS jdbc_demo DEFAULT CHARACTER SET utf8mb4");
        stmt.executeUpdate("USE jdbc_demo");
    }

    private static void createTable(Statement stmt) throws SQLException {
        System.out.println("---- 建表 ----");
        // 每次重跑都从空表开始，所以先拆再建（DROP 不可回滚，这里是故意的）
        stmt.executeUpdate("DROP TABLE IF EXISTS student");

        // 关键观察点：executeUpdate 用于所有"改结构"的 DDL，不管是 CREATE 还是 ALTER
        stmt.executeUpdate("""
                CREATE TABLE student (
                    student_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name       VARCHAR(50) NOT NULL,
                    grade      VARCHAR(20)
                )
                """);

        // ALTER 也是 DDL：给有数据之前的空表加列，最安全
        stmt.executeUpdate("ALTER TABLE student ADD COLUMN score INT");
        System.out.println("表 student 已创建\n");
    }

    private static void insertStudents(Statement stmt) throws SQLException {
        System.out.println("---- 插入学生 ----");
        // 关键观察点：一条 INSERT 语句插入多行，返回值就是插入的总行数
        int inserted = stmt.executeUpdate("""
                INSERT INTO student(name, grade, score) VALUES
                    ('王小华', '高二(3)班', 88),
                    ('李小明', '高二(2)班', 61),
                    ('赵小刚', '高二(3)班', 95)
                """);
        System.out.println("插入 " + inserted + " 行\n");
    }

    private static void updateStudentScore(Statement stmt) throws SQLException {
        System.out.println("---- 更新李小明的成绩 ----");
        // 关键观察点：返回值 = 真正被修改的行数，WHERE 没匹配到就是 0
        int updated = stmt.executeUpdate(
                "UPDATE student SET score = 70 WHERE name = '李小明'");
        System.out.println("更新 " + updated + " 行\n");
    }

    private static void deleteStudent(Statement stmt) throws SQLException {
        System.out.println("---- 删除李小明 ----");
        // 关键观察点：DELETE 的返回值同样是删掉的行数
        int deleted = stmt.executeUpdate(
                "DELETE FROM student WHERE name = '李小明'");
        System.out.println("删除 " + deleted + " 行\n");
    }

    private static void printAllStudents(Statement stmt) throws SQLException {
        System.out.println("---- 查询所有学生 ----");
        // 关键观察点：ResultSet 是游标，next() 移到下一行，首次调用移到第一行
        try (ResultSet rs = stmt.executeQuery(
                "SELECT student_id, name, grade, score FROM student ORDER BY student_id")) {
            while (rs.next()) {
                System.out.printf("  %d  %s  %s  %s%n",
                        rs.getLong("student_id"),
                        rs.getString("name"),
                        rs.getString("grade"),
                        rs.getObject("score")); // 允许 NULL，用 getObject 不会把空变成 0
            }
        }
        System.out.println();
    }
}
