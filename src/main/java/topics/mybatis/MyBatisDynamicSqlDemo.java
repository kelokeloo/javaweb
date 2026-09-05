package topics.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * MyBatis 动态 SQL 示例：
 * 顺序演示 &lt;sql&gt;/&lt;include&gt;、&lt;where&gt;+&lt;if&gt;、&lt;bind&gt;、&lt;choose&gt;、&lt;set&gt;+&lt;if&gt;、&lt;foreach&gt; 六大核心用法，
 * 最终 rollback 回滚，本次演示不会落库（与 MyBatisCrudDemo 约定一致）。
 */
public class MyBatisDynamicSqlDemo {
    public static void main(String[] args) throws IOException {
        try (Reader config = Resources.getResourceAsReader("mybatis-config.xml")) {
            SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(config);

            // false 表示关闭自动提交；演示结束时 rollback，因此不会留下临时数据。
            try (SqlSession session = factory.openSession(false)) {
                MyBatisDynamicSqlMapper mapper = session.getMapper(MyBatisDynamicSqlMapper.class);

                System.out.println("====== 1. <sql>/<include> 复用公共列 + findAll ======");
                List<MyBatisProduct> all = mapper.findAll();
                all.forEach(System.out::println);

                System.out.println("====== 2. <where>+<if> 只传 category = '手机' ======");
                MyBatisProductQuery q = new MyBatisProductQuery();
                q.setCategory("手机");
                mapper.searchProducts(q).forEach(System.out::println);

                System.out.println("====== 3. <where>+<if> 组合条件：手机 + 价格[4000,7000] + ON_SALE ======");
                q = new MyBatisProductQuery();
                q.setCategory("手机");
                q.setMinPrice(new BigDecimal("4000"));
                q.setMaxPrice(new BigDecimal("7000"));
                q.setStatus("ON_SALE");
                mapper.searchProducts(q).forEach(System.out::println);

                System.out.println("====== 4. <bind> 模糊查询 productName = '华' ======");
                q = new MyBatisProductQuery();
                q.setProductName("华");
                mapper.searchProducts(q).forEach(System.out::println);

                System.out.println("====== 5. <choose> 排序：sortBy = 'price' ======");
                q = new MyBatisProductQuery();
                q.setCategory("手机");
                q.setSortBy("price");
                mapper.searchProducts(q).forEach(System.out::println);

                System.out.println("====== 6. <set>+<if> 选择性更新：只改 price 和 status ======");
                MyBatisProduct upd = new MyBatisProduct();
                upd.setProductId(1L);
                upd.setPrice(new BigDecimal("5999.00"));
                upd.setStatus("SOLD_OUT");
                int updated = mapper.updateSelective(upd);
                System.out.println("updated = " + updated);

                System.out.println("====== 7. <foreach> IN 查询：selectByIds([1, 3]) ======");
                mapper.selectByIds(Arrays.asList(1L, 3L)).forEach(System.out::println);

                System.out.println("====== 8. <foreach> 批量插入 2 条临时商品 ======");
                MyBatisProduct p1 = new MyBatisProduct();
                p1.setProductName("临时平板A");
                p1.setCategory("平板");
                p1.setBrand("临时品牌");
                p1.setPrice(new BigDecimal("2999.00"));
                p1.setStatus("ON_SALE");
                p1.setStock(10);

                MyBatisProduct p2 = new MyBatisProduct();
                p2.setProductName("临时平板B");
                p2.setCategory("平板");
                p2.setBrand("临时品牌");
                p2.setPrice(new BigDecimal("1999.00"));
                p2.setStatus("OFF_SALE");
                p2.setStock(5);

                int inserted = mapper.batchInsert(Arrays.asList(p1, p2));
                System.out.println("inserted = " + inserted + ", 回填主键 = " + p1.getProductId() + "," + p2.getProductId());

                System.out.println("====== 事务：已回滚，本次演示未落库 ======");
                session.rollback();
            }
        }
    }
}
