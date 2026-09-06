package topics.logging.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 第 1 课：打第一条日志——对比 System.out.println，感受日志系统给了什么。
 *
 * 为什么不用 println：它 ①没级别 ②没上下文（时间/来源） ③目的地写死 ④改行为要改代码。
 * 日志系统把这几件事标准化：带级别、带时间来源、目的地可配、行为可配。
 * SLF4J 是门面（接口，规定怎么写），Logback 是实现（真正输出）——为什么拆两层，
 * 见 pom.xml 两个依赖的注释，或原理篇第 6 课。
 */
public class LoggingBeginnerDemo {

    // 企业惯例：每个类一个 log 字段，private static final。
    // logger 名字用当前类的全限定名，这是后面按包控制级别的基础（第 4 课展开）。
    private static final Logger log = LoggerFactory.getLogger(LoggingBeginnerDemo.class);

    public static void main(String[] args) {
        // 1) 同一句话，两种写法。看输出里 println 少了什么：时间、级别、谁打的。
        System.out.println("用户登录成功");
        log.info("用户登录成功");

        // 2) {} 占位符：描述和参数分开写，不要用字符串拼接。
        String username = "kelo";
        int orderId = 42;
        log.info("用户 {} 下单成功，订单号 {}", username, orderId);

        // 3) 五个级别，严重程度从低到高。没有任何配置时，TRACE 不会输出——
        //    先感受“可以被过滤”，为什么这样设计在第 2 课讲。
        log.trace("TRACE：最细的跟踪信息，生产几乎不用");
        log.debug("DEBUG：开发排查用的诊断信息");
        log.info("INFO：正常业务的关键节点");
        log.warn("WARN：还没出错，但需要留意");
        log.error("ERROR：出错了");

        // 4) 打异常：异常对象放最后一个参数（不给它占位符），Logback 会打印完整堆栈。
        //    这是初学者最常写错的地方，第 5 课专门讲。
        try {
            throw new IllegalStateException("模拟余额不足");
        } catch (Exception e) {
            log.error("下单失败, orderId={}", orderId, e);
        }
    }
}
