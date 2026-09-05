package topics.lombok.objectmethods;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

public class ObjectMethodsDemo {
    @AllArgsConstructor
    @ToString // 自动生成便于调试的 toString()
    // onlyExplicitlyIncluded = true：只有标记 @Include 的字段参与 equals/hashCode。
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    static class Account {
        @EqualsAndHashCode.Include
        private final long id; // 只按稳定的编号判断相等
        private String name;
        @ToString.Exclude // 明确排除密码，避免日志或调试输出泄露敏感信息
        private String password; // 避免 toString 输出敏感字段，不代表加密
    }

    public static void main(String[] args) {
        Account first = new Account(1, "小明", "demo-secret-1");
        Account second = new Account(1, "新昵称", "demo-secret-2");
        System.out.println("不含密码：" + first);
        System.out.println("相同编号是否相等：" + first.equals(second)); // true
        Set<Account> accounts = new HashSet<>();
        accounts.add(first);
        accounts.add(second);
        System.out.println("去重后数量：" + accounts.size()); // 1
        // 放入 HashSet/作为 HashMap 的 key 后，不要修改参与 equals/hashCode 的字段。
        // 有继承关系时，需明确是否用 callSuper = true 纳入父类信息。
    }
}
