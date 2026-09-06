package topics.mybatis.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/** 客户实体：一个客户可以拥有多个订单。 */
@Data
public class MyBatisCustomer {
    private Long customerId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    // 避免 Customer -> orders -> customer 在 toString/equals 中递归展开。
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MyBatisOrder> orders;
}
