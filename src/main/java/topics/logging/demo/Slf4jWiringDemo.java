package topics.logging.demo;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 第 6 课（原理篇）：它们是怎么接上的——手写派与注解派输出完全相同的证明。
 *
 * Handwritten 手写获取 logger，WithLombok 用 @Slf4j 注解——
 * 跑起来你会看到两行日志格式一字不差，因为 @Slf4j 生成的字段就是你手写的那三行。
 */
public class Slf4jWiringDemo {

    public static void main(String[] args) {
        System.out.println("=== 注解只是贴纸，Lombok 在编译期按硬编码模板把贴纸换成代码 ===");
        Handwritten.tell();
        WithLombok.tell();
        System.out.println("=== 两行输出同源同格式：捷径与手写等价 ===");
    }

    /** 手写派：第 1 课的写法。 */
    static class Handwritten {
        private static final Logger log = LoggerFactory.getLogger(Handwritten.class);

        static void tell() {
            log.info("[手写派] 我的 log 字段类型是 org.slf4j.Logger");
        }
    }

    /**
     * 注解派：源码里没有任何 log 字段声明，@Slf4j 编译期塞进来。
     * 它生成的模板焊死在 lombok.jar 的 LoggingFramework 枚举里：
     *   "org.slf4j.Logger org.slf4j.LoggerFactory.getLogger(TYPE)(TOPIC)"
     */
    @Slf4j
    static class WithLombok {
        static void tell() {
            log.info("[注解派] 我看起来凭空用了 log，其实字段是 Lombok 生成时塞进来的");
        }
    }
}
