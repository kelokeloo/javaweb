package topics.mybatis.demo;

import org.apache.ibatis.session.SqlSession;
import topics.mybatis.common.MyBatisUtils;
import topics.mybatis.entity.MyBatisCustomer;
import topics.mybatis.mapper.MyBatisOrderCustomerMapper;

/** MyBatis 复杂查询示例：演示一对多 collection 和多对一 association。 */
public class MyBatisOrderCustomerDemo {
    public static void main(String[] args) {
        try (SqlSession session = MyBatisUtils.openSession()) {
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
