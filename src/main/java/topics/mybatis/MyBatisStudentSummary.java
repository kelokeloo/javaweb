package topics.mybatis;

import lombok.Data;

/** 用于展示查询结果的数据传输对象（DTO）。 */
@Data
public class MyBatisStudentSummary {
    private Long id;
    private String studentName;
    private String className;
    private Integer score;
}
