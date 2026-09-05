package topics.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/** MyBatis 增删改查示例：一次事务内演示 CRUD，最后回滚避免改变示例数据。 */
public class MyBatisCrudDemo {
    public static void main(String[] args) throws IOException {
        try (Reader config = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(config);

            // false 表示关闭自动提交；这个演示结束时 rollback，因此不会留下测试数据。
            try (SqlSession session = factory.openSession(false)) {
                MyBatisStudentMapper mapper = session.getMapper(MyBatisStudentMapper.class);

                System.out.println("--- 查询：全部学生 ---");
                mapper.findAll().forEach(System.out::println);

                System.out.println("--- 新增：插入临时学生 ---");
                MyBatisStudent student = new MyBatisStudent();
                student.setName("临时学生");
                student.setGrade("演示班");
                student.setScore(60);
                int inserted = mapper.insert(student);
                System.out.println("inserted = " + inserted + ", generatedId = " + student.getStudentId());

                System.out.println("--- 查询：按 id 查询 ---");
                System.out.println(mapper.findById(student.getStudentId()));

                System.out.println("--- 修改：更新刚插入的学生 ---");
                student.setScore(95);
                int updated = mapper.update(student);
                System.out.println("updated = " + updated);
                System.out.println(mapper.findById(student.getStudentId()));

                System.out.println("--- 删除：删除刚插入的学生 ---");
                int deleted = mapper.deleteById(student.getStudentId());
                System.out.println("deleted = " + deleted);

                // 这里展示事务边界：所有 SQL 都执行过，但本次示例不落库。
                session.rollback();
                System.out.println("--- 事务：已回滚，本次演示未落库 ---");
            }
        }
    }
}
