CREATE DATABASE IF NOT EXISTS mybatis_learning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mybatis_learning;
CREATE TABLE IF NOT EXISTS mybatis_student (
    student_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    grade VARCHAR(20),
    score INT
);
INSERT INTO mybatis_student (name, grade, score)
SELECT * FROM (SELECT '王小华', '高二(3)班', 88 UNION ALL SELECT '李小明', '高二(2)班', 70 UNION ALL SELECT '赵小刚', '高二(3)班', 95) AS seed
WHERE NOT EXISTS (SELECT 1 FROM mybatis_student);
