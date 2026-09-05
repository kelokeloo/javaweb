package topics.mybatis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品查询的可选条件参数：哪个字段非空，XML 的 &lt;if&gt; 就拼接哪个条件。
 * 这种「把多个可选条件装进一个对象」的方式，比方法上一大堆 @Param 参数更清爽。
 */
@Data
public class MyBatisProductQuery {
    /** 商品名称，用于 <bind> 模糊查询（拼 %xx%） */
    private String productName;
    /** 分类，等值筛选 */
    private String category;
    /** 品牌，等值筛选 */
    private String brand;
    /** 价格下限（>=） */
    private BigDecimal minPrice;
    /** 价格上限（<=） */
    private BigDecimal maxPrice;
    /** 状态，等值筛选 */
    private String status;
    /** 库存下限（>=） */
    private Integer minStock;
    /** 排序字段：price / name / created_at，缺省或其它值则按 product_id */
    private String sortBy;
}
