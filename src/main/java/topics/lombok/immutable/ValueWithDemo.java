package topics.lombok.immutable;

import lombok.Value;
import lombok.With;

/** @Value 默认生成 final 类、private final 字段、全参构造、getter 和对象方法。 */
public class ValueWithDemo {
    // @Value 默认让类和字段变成 final，并生成全参构造、getter、toString、equals/hashCode。
    // 因此对象创建后不能通过 setter 修改，适合表示不可变的值对象。
    @Value
    static class Address {
        String city;
        @With // 生成 withStreet()：复制当前对象，只替换 street 字段
        String street; // 生成 withStreet()，其余字段不生成 with 方法
    }

    public static void main(String[] args) {
        Address original = new Address("杭州", "文一路");
        Address moved = original.withStreet("文二路");
        System.out.println("原地址：" + original);
        System.out.println("新地址：" + moved);
        System.out.println("是否同一个对象：" + (original == moved)); // false
        System.out.println("保留城市：" + moved.getCity());
        // 没有 setter。若字段是可变 List，@Value 不会自动防御性复制，不能保证深度不可变。
        // with 方法在传入原字段的同一值/引用时，可以直接返回原对象。
    }
}
