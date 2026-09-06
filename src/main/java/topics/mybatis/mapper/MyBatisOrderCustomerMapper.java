package topics.mybatis.mapper;

import topics.mybatis.entity.MyBatisCustomer;
import topics.mybatis.entity.MyBatisOrder;

import org.apache.ibatis.annotations.Param;

public interface MyBatisOrderCustomerMapper {
    MyBatisCustomer findCustomerWithOrders(@Param("email") String email);

    MyBatisOrder findOrderWithCustomer(@Param("orderNo") String orderNo);
}
