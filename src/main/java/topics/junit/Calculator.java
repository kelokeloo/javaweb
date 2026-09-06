package topics.junit;

/**
 * 一个「好被测」的小类——JUnit 主题里被测试的对象。
 *
 * 为什么拿它当被测对象：好的单元测试对象通常具备这几个特征，它恰好都有——
 *  1) 纯逻辑、无 IO、无外部依赖：不连数据库、不调网络，测试结果确定、跑得快。
 *  2) 有内部可变状态 {@code value}：每个测试方法前需要先清零，
 *     这正好让 @BeforeEach（方法前执行）有了实际用途，而不是摆设。
 *  3) 有失败分支 {@code divide(0)}：抛出 {@link ArithmeticException}，
 *     这正好让 {@code assertThrows}（断言抛异常）有了可测的目标。
 *
 * 其它主题（logging 的 main 方法、mybatis 连数据库）是「跑起来看效果」；
 * JUnit 主题是「对某个类做可重复的校验」——所以它刻意保持最小、无副作用。
 */
public class Calculator {

    // 累计值。测试里通常在每个方法开始前用 reset() 归零。
    private int value;

    // 累计执行了多少次运算。用来验证「确实执行了一件事」，也是可断言的观测点。
    private int operationCount;

    /** 归零，让每个测试从干净状态开始。 */
    public void reset() {
        value = 0;
        operationCount = 0;
    }

    /** 加 n。 */
    public void add(int n) {
        value += n;
        operationCount++;
    }

    /** 减 n。 */
    public void subtract(int n) {
        value -= n;
        operationCount++;
    }

    /** 乘 n。 */
    public void multiply(int n) {
        value *= n;
        operationCount++;
    }

    /**
     * 除以 divisor。
     * 整数除以 0 时 JVM 会自动抛 {@link ArithmeticException}，无需我们显式判断——
     * 这正是 assertThrows 要验证的「意料中的异常」。
     */
    public void divide(int divisor) {
        value = value / divisor;
        operationCount++;
    }

    /** 当前累计值。 */
    public int getValue() {
        return value;
    }

    /** 已执行运算次数。 */
    public int getOperationCount() {
        return operationCount;
    }
}
