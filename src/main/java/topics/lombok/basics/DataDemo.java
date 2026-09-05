package topics.lombok.basics;

import lombok.Data;
import lombok.NonNull;

/** @Data 适合简单的可变数据对象，但不会自动生成任意组合的构造方法。 */
public class DataDemo {
    // @Data 是一组组合注解：Getter + Setter + ToString + EqualsAndHashCode
    // + RequiredArgsConstructor，适合“主要存数据”的普通 JavaBean。
    @Data
    static class Student {
        private final long id; // final 字段会进入 @Data 生成的必需参数构造方法
        @NonNull // 生成 setter/构造方法中的 null 检查
        private String name;
        private int age;
    }

    public static void main(String[] args) {
        // @Data 包含 RequiredArgsConstructor：参数是未初始化的 final 和 @NonNull 字段。
        Student first = new Student(1001, "小明");
        first.setAge(18);
        Student second = new Student(1001, "小明");
        second.setAge(18);
        System.out.println(first); // 自动生成 toString()
        System.out.println("字段相同是否相等：" + first.equals(second)); // true
        second.setAge(19);
        System.out.println("年龄改变后是否相等：" + first.equals(second)); // false
        // id 是 final，不生成 setId()；name、age 有 getter/setter。
    }
}
