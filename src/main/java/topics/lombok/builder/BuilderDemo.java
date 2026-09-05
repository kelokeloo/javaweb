package topics.lombok.builder;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

public class BuilderDemo {
    @Getter
    @ToString
    // @Builder 把“很多参数的构造方法”变成可读的链式调用：Course.builder().title(...).build()。
    // toBuilder = true 额外生成 toBuilder()，可基于旧对象创建一个新对象。
    @Builder(toBuilder = true)
    static class Course {
        @NonNull
        private String title;
        @Builder.Default // builder 没有设置该字段时才使用这里的 30
        private int capacity = 30; // builder 未指定时使用 30；显式传 0 就是 0
        @Singular("tag") // 集合字段生成 tag(单个)、tags(批量)、clearTags() 方法
        private List<String> tags; // 生成 tag、tags、clearTags，构建后集合不可修改
    }

    public static void main(String[] args) {
        Course original = Course.builder()
                .title("Java 入门")
                .tag("后端")
                .tags(List.of("基础", "练习"))
                .build();
        Course expanded = original.toBuilder().capacity(50).build();
        Course untagged = original.toBuilder().clearTags().build();
        System.out.println("默认容量：" + original.getCapacity()); // 30
        System.out.println("原对象：" + original);
        System.out.println("调整后的新对象：" + expanded);
        System.out.println("清空标签的新对象：" + untagged);
        // toBuilder() 不是深拷贝；@Builder 本身也不生成 getter/setter。
        // title 标记了 @NonNull，遗漏它会在 build() 时抛出异常。
    }
}
