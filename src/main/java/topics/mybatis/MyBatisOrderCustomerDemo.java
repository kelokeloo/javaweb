package topics.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/** MyBatis 复杂查询示例：演示一对多 collection 和多对一 association。 */
public class MyBatisOrderCustomerDemo {
    public static void main(String[] args) throws IOException {
        try (Reader config = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(config);
            try (SqlSession session = factory.openSession()) {
                MyBatisOrderCustomerMapper mapper = session.getMapper(MyBatisOrderCustomerMapper.class);

                System.out.println("--- 一对多：查询客户及其订单 ---");
                MyBatisCustomer customer = mapper.findCustomerWithOrders("zhangsan@example.com");
                System.out.println(customer);
                customer.getOrders().forEach(System.out::println);

                System.out.println("--- 多对一：查询订单及其客户 ---");
                System.out.println(mapper.findOrderWithCustomer("ORD-20260905-001"));
            }
        }
    }
}
