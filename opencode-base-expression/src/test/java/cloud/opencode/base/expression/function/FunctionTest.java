package cloud.opencode.base.expression.function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Function Tests
 * Function 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("Function Tests | Function 测试")
class FunctionTest {

    @Nested
    @DisplayName("Apply Tests | apply 测试")
    class ApplyTests {

        @Test
        @DisplayName("Lambda function apply | Lambda 函数应用")
        void testLambdaFunctionApply() {
            Function add = args -> (Integer) args[0] + (Integer) args[1];
            assertThat(add.apply(1, 2)).isEqualTo(3);
        }

        @Test
        @DisplayName("No args function apply | 无参函数应用")
        void testNoArgsFunctionApply() {
            Function constant = args -> 42;
            assertThat(constant.apply()).isEqualTo(42);
        }

        @Test
        @DisplayName("Multiple args function apply | 多参函数应用")
        void testMultipleArgsFunctionApply() {
            Function concat = args -> {
                StringBuilder sb = new StringBuilder();
                for (Object arg : args) {
                    sb.append(arg);
                }
                return sb.toString();
            };
            assertThat(concat.apply("a", "b", "c")).isEqualTo("abc");
        }
    }

    @Nested
    @DisplayName("Default Methods Tests | 默认方法测试")
    class DefaultMethodsTests {

        @Test
        @DisplayName("getName returns simple class name | getName 返回简单类名")
        void testGetNameDefault() {
            Function func = args -> null;
            // Anonymous class will have a generated name
            assertThat(func.getName()).isNotNull();
        }

        @Test
        @DisplayName("getMinArgs returns 0 by default | getMinArgs 默认返回 0")
        void testGetMinArgsDefault() {
            Function func = args -> null;
            assertThat(func.getMinArgs()).isEqualTo(0);
        }

        @Test
        @DisplayName("getMaxArgs returns -1 by default (unlimited) | getMaxArgs 默认返回 -1（无限）")
        void testGetMaxArgsDefault() {
            Function func = args -> null;
            assertThat(func.getMaxArgs()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("isValidArgCount Tests | isValidArgCount 测试")
    class IsValidArgCountTests {

        @Test
        @DisplayName("Default unlimited args | 默认无限参数")
        void testUnlimitedArgs() {
            Function func = args -> null;
            assertThat(func.isValidArgCount(0)).isTrue();
            assertThat(func.isValidArgCount(1)).isTrue();
            assertThat(func.isValidArgCount(100)).isTrue();
        }

        @Test
        @DisplayName("Custom min args | 自定义最小参数")
        void testCustomMinArgs() {
            Function func = new Function() {
                @Override
                public Object apply(Object... args) {
                    return null;
                }

                @Override
                public int getMinArgs() {
                    return 2;
                }
            };
            assertThat(func.isValidArgCount(0)).isFalse();
            assertThat(func.isValidArgCount(1)).isFalse();
            assertThat(func.isValidArgCount(2)).isTrue();
            assertThat(func.isValidArgCount(3)).isTrue();
        }

        @Test
        @DisplayName("Custom max args | 自定义最大参数")
        void testCustomMaxArgs() {
            Function func = new Function() {
                @Override
                public Object apply(Object... args) {
                    return null;
                }

                @Override
                public int getMaxArgs() {
                    return 3;
                }
            };
            assertThat(func.isValidArgCount(0)).isTrue();
            assertThat(func.isValidArgCount(3)).isTrue();
            assertThat(func.isValidArgCount(4)).isFalse();
        }

        @Test
        @DisplayName("Custom min and max args | 自定义最小和最大参数")
        void testCustomMinAndMaxArgs() {
            Function func = new Function() {
                @Override
                public Object apply(Object... args) {
                    return null;
                }

                @Override
                public int getMinArgs() {
                    return 1;
                }

                @Override
                public int getMaxArgs() {
                    return 3;
                }
            };
            assertThat(func.isValidArgCount(0)).isFalse();
            assertThat(func.isValidArgCount(1)).isTrue();
            assertThat(func.isValidArgCount(2)).isTrue();
            assertThat(func.isValidArgCount(3)).isTrue();
            assertThat(func.isValidArgCount(4)).isFalse();
        }
    }

    @Nested
    @DisplayName("Custom Implementation Tests | 自定义实现测试")
    class CustomImplementationTests {

        @Test
        @DisplayName("Custom function with name | 自定义函数带名称")
        void testCustomFunctionWithName() {
            Function func = new Function() {
                @Override
                public Object apply(Object... args) {
                    return "result";
                }

                @Override
                public String getName() {
                    return "customFunc";
                }
            };
            assertThat(func.getName()).isEqualTo("customFunc");
            assertThat(func.apply()).isEqualTo("result");
        }

        @Test
        @DisplayName("Function with all custom properties | 函数带所有自定义属性")
        void testFunctionWithAllCustomProperties() {
            Function func = new Function() {
                @Override
                public Object apply(Object... args) {
                    return args[0].toString() + args[1].toString();
                }

                @Override
                public String getName() {
                    return "concat2";
                }

                @Override
                public int getMinArgs() {
                    return 2;
                }

                @Override
                public int getMaxArgs() {
                    return 2;
                }
            };
            assertThat(func.getName()).isEqualTo("concat2");
            assertThat(func.getMinArgs()).isEqualTo(2);
            assertThat(func.getMaxArgs()).isEqualTo(2);
            assertThat(func.isValidArgCount(1)).isFalse();
            assertThat(func.isValidArgCount(2)).isTrue();
            assertThat(func.isValidArgCount(3)).isFalse();
            assertThat(func.apply("hello", "world")).isEqualTo("helloworld");
        }
    }
}
