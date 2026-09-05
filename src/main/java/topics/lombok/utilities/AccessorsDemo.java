package topics.lombok.utilities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

public class AccessorsDemo {
    @Getter
    @Setter
    @ToString
    // chain = true 让 setter 返回 this，因此可以连续 setName(...).setAge(...)。
    @Accessors(chain = true)
    static class Student {
        private String name;
        private int age;
    }

    @Getter
    @Setter
    // fluent = true 去掉 get/set 前缀：size(20) 负责赋值，size() 负责读取。
    @Accessors(fluent = true)
    static class Page {
        private int size;
    }

    public static void main(String[] args) {
        // chain = true：setter 返回 this，仍使用 setXxx/getXxx 名称。
        Student student = new Student().setName("小明").setAge(18);
        System.out.println("链式赋值：" + student);
        // fluent = true：省略 get/set 前缀，默认也启用 chain。
        Page page = new Page().size(20);
        System.out.println("流式读取：" + page.size());
        // @Accessors 属于 experimental，且本身不生成 getter/setter。
        // fluent 命名偏离 JavaBean 约定，与映射/序列化框架配合前应检查兼容性。
    }
}
