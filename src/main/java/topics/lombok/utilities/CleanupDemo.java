package topics.lombok.utilities;

import lombok.Cleanup;

import java.io.IOException;
import java.io.StringReader;

public class CleanupDemo {
    static void readText() throws IOException {
        // 在离开当前作用域时调用 close()；本例不访问磁盘。
        // @Cleanup 在离开当前方法/代码块时自动调用 reader.close()，减少 finally 模板代码。
        @Cleanup StringReader reader = new StringReader("Lombok");
        System.out.println("首字符：" + (char) reader.read()); // L
    }

    public static void main(String[] args) throws IOException {
        readText();
        System.out.println("已离开读取作用域，reader 已关闭");
        // JDK AutoCloseable 资源通常优先用 try-with-resources，能更好地保留异常信息。
        // @Cleanup 生成的清理逻辑若再次抛异常，可能覆盖业务代码原先抛出的异常。
    }
}
