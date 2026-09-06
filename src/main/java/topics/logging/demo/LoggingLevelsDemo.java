package topics.logging.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 第 2 课：级别与占位符——规范的一半在这里。
 *
 * 运行前须知：项目根目录已有 logback.xml（第 3 课教材，提前就位）——
 * topics 包是 DEBUG、其它包走 root 的 INFO。本课的"被拦截"现象靠它。
 */
public class LoggingLevelsDemo {

    private static final Logger log = LoggerFactory.getLogger(LoggingLevelsDemo.class);

    /**
     * 模拟"不归我们管"的第三方库 logger：名字不在 topics 下，继承 root 的 INFO，
     * 所以它打的 DEBUG 会被拦截——拦截发生在哪里，下面三种写法对比的就是这个。
     */
    private static final Logger thirdParty = LoggerFactory.getLogger("com.example.secretive.Library");

    /** 一个"话很多"的对象：toString 被调用时会大声嚷嚷，用来观察格式化有没有真的发生。 */
    static class Loud {
        @Override
        public String toString() {
            System.out.println("   >>> Loud.toString() 执行了（模拟一次昂贵的格式化/计算）");
            return "noisy data";
        }
    }

    public static void main(String[] args) {
        fiveLevels();
        placeholderVsConcat();
    }

    /** 场景：用户下单。五个级别各司其职，判断标准只有一问——"这句是给谁看的？" */
    static void fiveLevels() {
        String user = "kelo";
        // trace：粒度细到循环内部的变量快照，给自己单步调试用，生产几乎不写
        log.trace("trace | 循环中: i={}, 累计金额={}", 0, 0.0);
        // debug：开发诊断——入参、中间结果、分支走向
        log.debug("debug | 收到下单请求, user={}, 商品数={}", user, 3);
        // warn：没出错，但有情况需要留意——缺省值兜底、重试、降级
        log.warn("warn  | 用户 {} 无默认地址，已使用注册地址", user);
        // info：正常业务的关键节点——对外可交代"系统干了什么"
        log.info("info  | 用户 {} 下单成功, orderNo={}", user, 9527);
        // error：出错了，需要人来看、来看告警文件
        log.error("error | 用户 {} 的支付回调超时", user);
    }

    /** 同一条"会被拦截的 DEBUG"，三种写法浪费程度大不同——这是 {} 占位符存在的真正理由。 */
    static void placeholderVsConcat() {
        System.out.println("—— ① 字符串拼接：级别拦住了输出，但拦不住拼接（toString 白跑）");
        thirdParty.debug("result=" + new Loud());

        System.out.println("—— ② {} 占位符：级别拦住输出，格式化也就不会发生（参数表达式本身仍会求值）");
        thirdParty.debug("result={}", new Loud());

        System.out.println("—— ③ isDebugEnabled 守卫：连参数的构造/计算都跳过");
        if (thirdParty.isDebugEnabled()) {
            thirdParty.debug("result={}", new Loud());
        } else {
            System.out.println("   (isDebugEnabled()=false，昂贵的准备整段跳过)");
        }
        System.out.println("结论：默认一律用 {}；只有当『构造参数本身』很贵时才加守卫。");
    }
}
