package topics.lombok.constructors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class ConstructorDemo {
    // @NoArgsConstructor 生成 Student()；@AllArgsConstructor 生成包含全部字段的构造方法。
    @NoArgsConstructor
    @AllArgsConstructor
    @Setter
    @ToString
    static class Student {
        private long id;
        private String name;
    }

    // @RequiredArgsConstructor 只接收 final 和 @NonNull 字段，适合表达“创建对象必须提供什么”。
    @RequiredArgsConstructor
    @ToString
    static class Course {
        private final String code;
        @NonNull
        private String title;
        private int credits; // 普通字段不进入 required 构造方法，默认是 0
    }

    public static void main(String[] args) {
        Student empty = new Student();
        empty.setId(1001);
        empty.setName("小明");
        System.out.println("无参构造后赋值：" + empty);
        System.out.println("全参构造（按字段声明顺序）：" + new Student(1002, "小红"));
        System.out.println("必需参数构造：" + new Course("JAVA", "Java 入门"));
        // final 只限制重新赋值，并不检查 null；@NonNull 才生成 null 检查。
        // 不要随意用 @NoArgsConstructor(force = true)：它会把 final 字段强制设为默认值。
    }
}
