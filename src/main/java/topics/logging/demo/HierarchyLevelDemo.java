package topics.logging.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 第 4 课：层级树——同一句 debug()，三个 logger 三种命运。
 *
 * 关键认知：getLogger(名字) 里的"名字"决定它挂在配置树的哪个枝上；
 * 树按 "." 分层，给某个前缀配了级别，所有后代继承。本 demo 依赖 logback.xml 两行：
 * topics=DEBUG、org.apache.ibatis=INFO。类名只是最惯用的取名方式
 * （所以第 1 课惯例用 getLogger(类.class)）。
 */
public class HierarchyLevelDemo {

    /** 名字 = topics.logging.demo.HierarchyLevelDemo，在 topics 枝下 → 继承 DEBUG。 */
    private static final Logger mine = LoggerFactory.getLogger(HierarchyLevelDemo.class);

    /** 名字 = org.apache.ibatis.io.DefaultVFS，在 org.apache.ibatis 枝下 → 被显式压到 INFO。 */
    private static final Logger framework = LoggerFactory.getLogger("org.apache.ibatis.io.DefaultVFS");

    /** 名字没有任何 logger 配置认领 → 直接落 root，INFO。 */
    private static final Logger stranger = LoggerFactory.getLogger("com.acme.cache.LocalCache");

    public static void main(String[] args) {
        mine.debug("我在 topics 枝下：DEBUG 放行");
        framework.debug("我在 org.apache.ibatis 枝下：被显式压到 INFO，看不到我");
        stranger.debug("无人认领我的枝：落 root 的 INFO，也看不到我");

        System.out.println("--- 上面三句 debug，你应该只看到第一句。下面是都放行的 WARN：");
        mine.warn("我可见");
        framework.warn("我可见");
        stranger.warn("我可见");

        System.out.println();
        System.out.println(">>> 进阶实锤（需本地 MySQL）：运行 topics.mybatis.demo.MyBatisBeginnerDemo，");
        System.out.println(">>> SQL 语句（==>  Preparing: ...）会照常打印——因为 MyBatis 的 SQL 日志用的");
        System.out.println(">>> logger 名就是 Mapper 接口全限定名（topics.mybatis.mapper.*），");
        System.out.println(">>> 挂在 topics 枝下吃到 DEBUG；而 VFS 扫描噪音（org.apache.ibatis.io.*）被压掉了。");
        System.out.println(">>> 这就是『压框架噪音、放自己业务、还能看 SQL』一行配置都不多写的原理。");
    }
}
