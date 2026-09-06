package topics.mybatis.common;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

/**
 * 全局唯一的 SqlSessionFactory，供所有 Demo 复用。
 *
 * 这正是官方文档推荐的用法：
 * - SqlSessionFactory 是「重量级」对象，包含数据库配置和连接池，整个应用只创建一次（这里用静态代码块实现单例）。
 * - SqlSession 是「轻量级」对象，对应一次数据库会话（请求），用完即关，所以每个方法通过 openSession() 现开现关。
 *
 * 之前每个 Demo 各自写一遍 build 流程，现在收敛到这里，Demo 只关心业务 SQL。
 */
public final class MyBatisUtils {

    private static final SqlSessionFactory SQL_SESSION_FACTORY;

    static {
        try (Reader config = Resources.getResourceAsReader("mybatis-config.xml")) {
            SQL_SESSION_FACTORY = new SqlSessionFactoryBuilder().build(config);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("读取 mybatis-config.xml 失败: " + e.getMessage());
        }
    }

    private MyBatisUtils() {
    }

    /** 自动提交模式（默认 true）：每条 SQL 立即生效，适合纯查询演示。 */
    public static SqlSession openSession() {
        return SQL_SESSION_FACTORY.openSession();
    }

    /**
     * 手动事务模式：autoCommit=false 时 SQL 不会自动提交，
     * 需要显式 session.commit() 或 session.rollback()。演示「跑完回滚不落库」用这个。
     */
    public static SqlSession openSession(boolean autoCommit) {
        return SQL_SESSION_FACTORY.openSession(autoCommit);
    }
}
