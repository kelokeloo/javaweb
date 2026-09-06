package topics.logging.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 第 5 课：异常日志与企业 review 常抓点。
 * 每种错法都现场演一遍，输出差异即教训。
 */
public class ExceptionLoggingDemo {

    private static final Logger log = LoggerFactory.getLogger(ExceptionLoggingDemo.class);

    public static void main(String[] args) {
        int orderId = 42;

        System.out.println("—— 错法 1：只打 e.getMessage()，堆栈全丢（现场只剩一句话）");
        try {
            pay(orderId);
        } catch (Exception e) {
            log.error("支付失败: {}", e.getMessage());
        }

        System.out.println("—— 错法 2：吞异常（catch 了却只说“出错了”，信息量为零，还照常往下跑）");
        try {
            pay(orderId);
        } catch (Exception e) {
            log.warn("出错了");
        }

        System.out.println("—— 正解：异常对象做最后一个参数（不占位符），消息给现场、堆栈给根因");
        try {
            pay(orderId);
        } catch (Exception e) {
            log.error("支付失败, orderId={}", orderId, e);
        }

        System.out.println("—— 对照：e.printStackTrace() 不走日志系统（直写 stderr，没有级别、也不会写进配置的文件）");
        try {
            pay(orderId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();
        System.out.println(">>> 再去 logs/logging-error.log：错法 1、正解各进来一条（都是 ERROR）；");
        System.out.println(">>> 错法 2 的 warn 和 printStackTrace 都不在——这就是告警文件为什么只抓 ERROR。");
    }

    /** 造一个带原因的异常，模拟真实调用链。 */
    static void pay(int orderId) {
        try {
            if (orderId == 42) {
                throw new java.net.ConnectException("connect timed out: 支付网关 10.0.3.7:443");
            }
        } catch (Exception e) {
            // 包装成业务异常再抛出——保留 cause，这是"异常链"，正解里堆栈能打印全靠它
            throw new IllegalStateException("订单 " + orderId + " 支付环节失败", e);
        }
    }
}
