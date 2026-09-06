package topics.junit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JUnit 主题 · 第 2 课：稍进阶的两招——参数化测试 与 嵌套分组。
 *
 * @ParameterizedTest：一个"测试模板"喂多组入参，几组就跑几遍，不用复制粘贴。
 * @Nested：用内部类把测试按场景分组，报告更清晰（测试多时尤其有用）。
 *
 * 重点关注：@Nested 内层类的方法也会先执行外层的 @BeforeEach（先 reset），
 * 再执行内层自己的 @BeforeEach（打底数据）——顺序是 外层→内层。
 */
class CalculatorMoreTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
        calculator.reset();
    }

    // ---------- 参数化测试 ----------

    @ParameterizedTest
    @CsvSource({
            "1, 2, 3",
            "10, 20, 30",
            "-5, 5, 0"
    })
    @DisplayName("参数化加法：三组入参，一次跑三遍")
    void addParameterized(int a, int b, int expected) {
        calculator.add(a);
        calculator.add(b);
        // 三组分别断言，任何一组不符都会失败并报告是哪一组。
        assertEquals(expected, calculator.getValue());
    }

    // ---------- 嵌套分组 ----------

    @Nested
    @DisplayName("加法场景")
    class AddGroup {

        @BeforeEach
        void seed() {
            // 本组每个方法先用 add(100) 打底，验证"在已有值上继续累加"。
            calculator.add(100);
        }

        @Test
        @DisplayName("打底 100 后再加 5，得 105")
        void addOnTop() {
            calculator.add(5);
            assertEquals(105, calculator.getValue());
        }
    }

    @Nested
    @DisplayName("除法场景")
    class DivideGroup {

        @BeforeEach
        void seed() {
            // 打底 9，保证除法有值可除。
            calculator.add(9);
        }

        @Test
        @DisplayName("9 / 3 = 3：正常除法")
        void divideOk() {
            calculator.divide(3);
            assertEquals(3, calculator.getValue());
            // 打底 1 次 + 除法 1 次 = 累计执行 2 次运算。
            assertEquals(2, calculator.getOperationCount());
        }

        @Test
        @DisplayName("9 / 0：抛出 ArithmeticException")
        void divideByZero() {
            assertThrows(ArithmeticException.class, () -> calculator.divide(0));
        }
    }
}
