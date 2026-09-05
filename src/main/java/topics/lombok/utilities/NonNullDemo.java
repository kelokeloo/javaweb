package topics.lombok.utilities;

import lombok.NonNull;

public class NonNullDemo {
    // @NonNull 会在方法入口插入 null 检查，调用 welcome(null) 会立即抛出异常。
    static String welcome(@NonNull String name) {
        // Lombok 在方法开头插入 null 检查，默认抛出 NullPointerException。
        return "你好，" + name;
    }

    public static void main(String[] args) {
        System.out.println(welcome("小明"));
        System.out.println("空字符串仍被允许：" + welcome(""));
        try {
            welcome(null);
            throw new IllegalStateException("预期 null 被拒绝");
        } catch (NullPointerException e) {
            System.out.println("预期的空值异常：" + e.getMessage());
        }
        // @NonNull 不是 Bean Validation，不检查空白字符串、长度、范围等业务规则。
    }
}
