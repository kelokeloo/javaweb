package topics.logging.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 第 3 课：配置生效——一次运行，三个出口各拿到什么。
 *
 * 跑完后去看项目根目录的 logs/ 目录：
 *   logging-demo.log   全量（root 的三个 appender 之一）
 *   logging-error.log  只有本 demo 最后那条 ERROR
 * 对照 README 第 3 课逐行读懂 src/main/resources/logback.xml。
 */
public class LogbackConfigDemo {

    private static final Logger log = LoggerFactory.getLogger(LogbackConfigDemo.class);

    public static void main(String[] args) {
        log.info("这条 INFO：控制台有、logging-demo.log 有、logging-error.log 没有");
        log.warn("这条 WARN：同上，LevelFilter 把它挡在 error 文件外");
        log.debug("这条 DEBUG：topics 包被单独放到 DEBUG，所以三个出口的前两个能看到它");
        // 故意造一条 ERROR：只有它会被 LevelFilter 放行进 logging-error.log
        log.error("这条 ERROR：三个出口全都有——生产告警就抓这个文件", new IllegalStateException("模拟支付超时"));

        System.out.println();
        System.out.println(">>> 现在去看 logs/ 目录，对照两个文件的差异。");
    }
}
