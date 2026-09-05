package topics.mybatis;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MyBatisStudentMapper {
    List<MyBatisStudent> findAll();

    MyBatisStudent findById(@Param("studentId") Long studentId);

    int insert(MyBatisStudent student);

    int update(MyBatisStudent student);

    int deleteById(@Param("studentId") Long studentId);

    List<MyBatisStudentSummary> findAllWithResultMap();
}
