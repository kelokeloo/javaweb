package topics.mybatis.demo;

import org.apache.ibatis.session.SqlSession;
import topics.mybatis.common.MyBatisUtils;
import topics.mybatis.mapper.MyBatisStudentMapper;

/** MyBatis 入门：通过 Mapper 接口调用 XML 中定义的查询。 */
public class MyBatisBeginnerDemo {
    public static void main(String[] args) {
        try (SqlSession session = MyBatisUtils.openSession()) {
            // MyBatis 为 Mapper 接口创建动态代理，调用方法时执行 XML 中对应的 SQL。
            MyBatisStudentMapper mapper = session.getMapper(MyBatisStudentMapper.class);
            mapper.findAll()
                    .forEach(System.out::println);
        }
    }
}
