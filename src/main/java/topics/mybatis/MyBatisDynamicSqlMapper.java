package topics.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 动态 SQL 演示的 Mapper 接口：绑定资源目录下的 MyBatisDynamicSqlMapper.xml。 */
public interface MyBatisDynamicSqlMapper {

    /** <sql> + <include>：复用公共 SELECT 列。 */
    List<MyBatisProduct> findAll();

    /** <where> + <if> + <bind> + <choose>：按可选条件组合查询。 */
    List<MyBatisProduct> searchProducts(MyBatisProductQuery query);

    /** <set> + <if>：只更新传入的非空字段。 */
    int updateSelective(MyBatisProduct product);

    /** <foreach>：按 id 列表做 IN 查询。 */
    List<MyBatisProduct> selectByIds(@Param("ids") List<Long> ids);

    /** <foreach>：一次插入多条商品。 */
    int batchInsert(@Param("products") List<MyBatisProduct> products);
}
