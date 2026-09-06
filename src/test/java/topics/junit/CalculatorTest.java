package topics.junit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 主题 · 第 1 课：最基础的用法。
 *
 * 跑法：目录下执行 `mvn test`，或专跑本类 `mvn -Dtest=CalculatorTest test`。
 * 之后在 `target/surefire-reports/` 里能看每个用法的输出。
 *
 * 本课讲四件事：
 *  1) @Test：一个普通方法变成测试用例（带 @Test 才跑，不带不跑）。
 *  2) 断言（assertEquals / assertTrue / assertThrows）：验证"实际结果 == 预期"。
 *  3) @BeforeEach / @AfterEach：每个 @Test 方法前后各跑一次，保证起点一致、收尾干净。
 *  4) @DisplayName：给测试方法起中文名字，测试报告更好读。
 */
class CalculatorTest {

    // 被测对象。放 private 字段，由 @BeforeEach 统一初始化/复位——每个方法都从同一起点开始。
    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();  // 新建实例
        calculator.reset();             // 清零累计值，保证测试入口一致
    }

    @AfterEach
    void tearDown() {
        // 本类无外部资源（文件/连接）可关，仅作演示：
        // 如果有，这里负责清理，比如关闭流、释放连接。
        // 注意：@AfterEach 哪怕 @Test 断言失败也会执行——保证脏资源一定被收尾。
    }

    @Test
    @DisplayName("加法：2 + 3 = 5，且确实执行了一次运算")
    void add() {
        calculator.add(2);
        calculator.add(3);
        // 第一个参数是"预期值"，第二个是"实际值"，assertEquals 两者相等才通过。
        assertEquals(5, calculator.getValue());
        // 额外验证副作用：真实执行了 2 次，而不是靠某处写死。
        assertEquals(2, calculator.getOperationCount());
    }

    @Test
    @DisplayName("减法：10 - 3 = 7")
    void subtract() {
        calculator.add(10);
        calculator.subtract(3);
        assertEquals(7, calculator.getValue());
    }

    @Test
    @DisplayName("乘法：4 * 6 = 24")
    void multiply() {
        calculator.add(4);
        calculator.multiply(6);
        assertEquals(24, calculator.getValue());
    }

    @Test
    @DisplayName("除法结果为负数时：-8 / 2 = -4")
    void divideNegative() {
        calculator.add(-8);
        calculator.divide(2);
        assertEquals(-4, calculator.getValue());
    }

    @Test
    @DisplayName("除数为 0：断言抛出 ArithmeticException")
    void divideByZero() {
        calculator.add(8);
        // assertThrows 不光断言"抛了异常"，还要求抛的是指定类型的异常。
        // 第二个参数用 lambda，写出"会出问题的那一行动作"——哪怕它其实没抛，测试也会失败。
        assertThrows(ArithmeticException.class, () -> calculator.divide(0));
        // 异常发生后，运算没成功，次数不应增加：仍是 add 的那 1 次。
        assertEquals(1, calculator.getOperationCount());
    }

    @Test
    @DisplayName("没有副作用时（reset）+ 布尔断言：值是 0")
    void noSideEffects() {
        // assertTrue / assertFalse：断言布尔表达式成立/不成立。
        assertTrue(calculator.getValue() == 0);
    }
}
