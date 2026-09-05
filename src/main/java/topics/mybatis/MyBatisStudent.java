package topics.mybatis;

import lombok.Data;

/** 对应 mybatis_student 表的入门实体。 */
@Data
public class MyBatisStudent {
    private Long studentId;
    private String name;
    private String grade;
    private Integer score;
}
