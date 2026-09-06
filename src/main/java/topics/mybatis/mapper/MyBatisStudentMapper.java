package topics.mybatis.mapper;

import topics.mybatis.entity.MyBatisStudent;
import topics.mybatis.entity.MyBatisStudentSummary;

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
