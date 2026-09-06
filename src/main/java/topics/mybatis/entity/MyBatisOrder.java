package topics.mybatis.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单实体：一个订单属于一个客户。 */
@Data
public class MyBatisOrder {
    private Long orderId;
    private String orderNo;
    private Long customerId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    // 避免 Order -> customer -> orders 在 toString/equals 中递归展开。
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private MyBatisCustomer customer;
}
