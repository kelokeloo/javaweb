package topics.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/** MyBatis 入门：先用 statement id 直接查询，暂不引入 getMapper。 */
public class MyBatisBeginnerDemo {
    public static void main(String[] args) throws IOException {
        try (Reader config = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(config);
            try (SqlSession session = factory.openSession()) {
                session.selectList("topics.mybatis.MyBatisStudentMapper.findAll")
                        .forEach(System.out::println);
            }
        }
    }
}
