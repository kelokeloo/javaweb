package topics.lombok.logging;

import lombok.extern.java.Log;

/** 使用 JDK 自带日志，不需要额外引入日志依赖。 */
// @Log 自动创建名为 log 的 java.util.logging.Logger 静态字段，下面可直接调用 log.info()。
@Log
public class LogDemo {
    public static void main(String[] args) {
        // 相当于生成 private static final java.util.logging.Logger log =
        //     java.util.logging.Logger.getLogger(LogDemo.class.getName());
        log.info("开始学习 Lombok 日志注解");
        String topic = "lombok";
        log.info(() -> "当前主题：" + topic);
        log.warning("这是一条演示用警告日志");
        // 常见的 @Slf4j 生成 org.slf4j.Logger，需要另行添加 SLF4J API 和运行时实现。
        // @Log 使用 java.util.logging，不支持 SLF4J 的 {} 占位符写法。
    }
}
