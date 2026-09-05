package topics.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/** MyBatis 入门：通过 Mapper 接口调用 XML 中定义的查询。 */
public class MyBatisBeginnerDemo {
    public static void main(String[] args) throws IOException {
        try (Reader config = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(config);
            try (SqlSession session = factory.openSession()) {
                // MyBatis 为 Mapper 接口创建动态代理，调用方法时执行 XML 中对应的 SQL。
                MyBatisStudentMapper mapper = session.getMapper(MyBatisStudentMapper.class);
                mapper.findAll()
                        .forEach(System.out::println);
            }
        }
    }
}
