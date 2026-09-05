package topics.mybatis;

import org.apache.ibatis.annotations.Param;

public interface MyBatisOrderCustomerMapper {
    MyBatisCustomer findCustomerWithOrders(@Param("email") String email);

    MyBatisOrder findOrderWithCustomer(@Param("orderNo") String orderNo);
}
