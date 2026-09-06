package topics.mybatis.demo;

import org.apache.ibatis.session.SqlSession;
import topics.mybatis.common.MyBatisUtils;
import topics.mybatis.entity.MyBatisProduct;
import topics.mybatis.mapper.MyBatisAnnotationMapper;

import java.math.BigDecimal;

/**
 * 纯注解 Mapper 的演示：简单 CRUD 直接写在注解里，不需要 XML 文件。
 * 依次展示 @Select / @Insert + @Options / @Update / @Delete，
 * 最后 rollback 回滚，本次演示不会落库（与其它演示约定一致）。
 */
public class MyBatisAnnotationDemo {
    public static void main(String[] args) {
        // false 表示关闭自动提交；演示结束时 rollback，因此不会留下临时数据。
        try (SqlSession session = MyBatisUtils.openSession(false)) {
            MyBatisAnnotationMapper mapper = session.getMapper(MyBatisAnnotationMapper.class);

            System.out.println("--- @Select 按 id 查询 ---");
            System.out.println(mapper.findById(1L));

            System.out.println("--- @Select 按分类查询 ---");
            mapper.findByCategory("手机").forEach(System.out::println);

            System.out.println("--- @Insert + @Options 插入一条临时商品 ---");
            MyBatisProduct p = new MyBatisProduct();
            p.setProductName("临时演示商品");
            p.setCategory("测试");
            p.setBrand("临时");
            p.setPrice(new BigDecimal("99.00"));
            p.setStatus("ON_SALE");
            p.setStock(1);
            int inserted = mapper.insert(p);
            System.out.println("inserted = " + inserted + ", 回填主键 = " + p.getProductId());

            System.out.println("--- @Update 更新价格和状态 ---");
            p.setPrice(new BigDecimal("199.00"));
            p.setStatus("SOLD_OUT");
            System.out.println("updated = " + mapper.update(p));

            System.out.println("--- @Delete 删除临时商品 ---");
            System.out.println("deleted = " + mapper.deleteById(p.getProductId()));

            System.out.println("--- 事务：已回滚，本次演示未落库 ---");
            session.rollback();
        }
    }
}
