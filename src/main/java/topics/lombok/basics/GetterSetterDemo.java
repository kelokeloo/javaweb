package topics.lombok.basics;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/** 类级注解作用于实例字段；字段级注解可以覆盖类级设置。 */
public class GetterSetterDemo {
    // @Getter 自动生成 getName()/isActive() 等读取方法；@Setter 自动生成 setXxx() 修改方法。
    // 注解放在类上会作用于所有字段，也可以像下面这样在字段上单独覆盖设置。
    @Getter
    @Setter
    static class Student {
        private String name;
        private boolean active; // boolean 生成 isActive()，包装类型 Boolean 则生成 getActive()
        // AccessLevel.NONE 表示明确不生成 setter，常用于 id、创建时间等只读字段。
        @Setter(AccessLevel.NONE)
        private final long id = 1001; // 不提供 setter，只允许读取
    }

    public static void main(String[] args) {
        Student student = new Student();
        student.setName("小明"); // 相当于手写 public void setName(String name)
        student.setActive(true);
        System.out.println("姓名：" + student.getName());
        System.out.println("是否启用：" + student.isActive());
        System.out.println("只读编号：" + student.getId());
    }
}
