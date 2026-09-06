package topics.mybatis.demo;

import org.apache.ibatis.session.SqlSession;
import topics.mybatis.common.MyBatisUtils;
import topics.mybatis.mapper.MyBatisStudentMapper;

/** MyBatis resultMap 示例：显式配置数据库列和 Java 属性之间的映射关系。 */
public class MyBatisResultMapDemo {
    public static void main(String[] args) {
        try (SqlSession session = MyBatisUtils.openSession()) {
            // MyBatis 为 Mapper 接口创建动态代理，调用方法时执行 XML 中对应的 SQL。
            MyBatisStudentMapper mapper = session.getMapper(MyBatisStudentMapper.class);
            mapper.findAllWithResultMap()
                    .forEach(System.out::println);
        }
    }
}
