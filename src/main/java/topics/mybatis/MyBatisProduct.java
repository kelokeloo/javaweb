package topics.mybatis;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 对应 mybatis_product 表的商品实体，用于演示动态 SQL。 */
@Data
public class MyBatisProduct {
    private Long productId;
    private String productName;
    private String category;
    private String brand;
    private BigDecimal price;
    private String status;
    private Integer stock;
    private LocalDateTime createdAt;
}
