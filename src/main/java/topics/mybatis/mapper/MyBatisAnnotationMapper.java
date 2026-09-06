package topics.mybatis.mapper;

import topics.mybatis.entity.MyBatisProduct;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 纯注解方式写 Mapper 的示例：适用于「简单、单条就能写完」的场景，无需额外 XML 文件。
 * 这里的每个方法都是一句能读完的简单语句；一旦涉及动态 SQL（&lt;if&gt;/&lt;foreach&gt;）
 * 或复杂映射（collection/association），就应改用 XML。注解只用来覆盖简单场景，而非常规主力。
 */
public interface MyBatisAnnotationMapper {

    @Select("SELECT * FROM mybatis_product WHERE product_id = #{productId}")
    MyBatisProduct findById(@Param("productId") Long productId);

    @Select("SELECT * FROM mybatis_product WHERE category = #{category}")
    List<MyBatisProduct> findByCategory(@Param("category") String category);

    @Insert("INSERT INTO mybatis_product (product_name, category, brand, price, status, stock) " +
            "VALUES (#{productName}, #{category}, #{brand}, #{price}, #{status}, #{stock})")
    @Options(useGeneratedKeys = true, keyProperty = "productId")
    int insert(MyBatisProduct product);

    @Update("UPDATE mybatis_product SET price = #{price}, status = #{status} WHERE product_id = #{productId}")
    int update(MyBatisProduct product);

    @Delete("DELETE FROM mybatis_product WHERE product_id = #{productId}")
    int deleteById(@Param("productId") Long productId);
}
