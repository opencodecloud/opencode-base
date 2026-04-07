package cloud.opencode.base.core.thread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * ScopedValueUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ScopedValueUtil 测试")
class ScopedValueUtilTest {

    @Nested
    @DisplayName("ScopedValue 创建测试")
    class CreateTests {

        @Test
        @DisplayName("newScopedValue 创建新实例")
        void testNewScopedValue() {
            ScopedValue<String> sv = ScopedValueUtil.newScopedValue();
            assertThat(sv).isNotNull();
            assertThat(sv.isBound()).isFalse();
        }

        @Test
        @DisplayName("不同调用返回不同实例")
        void testNewScopedValueDifferentInstances() {
            ScopedValue<String> sv1 = ScopedValueUtil.newScopedValue();
            ScopedValue<String> sv2 = ScopedValueUtil.newScopedValue();
            assertThat(sv1).isNotSameAs(sv2);
        }
    }

    @Nested
    @DisplayName("runWhere 测试")
    class RunWhereTests {

        @Test
        @DisplayName("runWhere 单个绑定")
        void testRunWhereSingle() {
            ScopedValue<String> sv = ScopedValue.newInstance();
            AtomicReference<String> captured = new AtomicReference<>();

            ScopedValueUtil.runWhere(sv, "testValue", () -> {
                captured.set(sv.get());
            });

            assertThat(captured.get()).isEqualTo("testValue");
        }

        @Test
        @DisplayName("runWhere 作用域外未绑定")
        void testRunWhereNotBoundOutsideScope() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            ScopedValueUtil.runWhere(sv, "testValue", () -> {
                assertThat(sv.isBound()).isTrue();
            });

            assertThat(sv.isBound()).isFalse();
        }

        @Test
        @DisplayName("runWhere 双绑定")
        void testRunWhereDouble() {
            ScopedValue<String> sv1 = ScopedValue.newInstance();
            ScopedValue<Integer> sv2 = ScopedValue.newInstance();
            AtomicReference<String> captured1 = new AtomicReference<>();
            AtomicReference<Integer> captured2 = new AtomicReference<>();

            ScopedValueUtil.runWhere(sv1, "value1", sv2, 42, () -> {
                captured1.set(sv1.get());
                captured2.set(sv2.get());
            });

            assertThat(captured1.get()).isEqualTo("value1");
            assertThat(captured2.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("runWhere 三绑定")
        void testRunWhereTriple() {
            ScopedValue<String> sv1 = ScopedValue.newInstance();
            ScopedValue<Integer> sv2 = ScopedValue.newInstance();
            ScopedValue<Boolean> sv3 = ScopedValue.newInstance();
            AtomicReference<String> captured1 = new AtomicReference<>();
            AtomicReference<Integer> captured2 = new AtomicReference<>();
            AtomicReference<Boolean> captured3 = new AtomicReference<>();

            ScopedValueUtil.runWhere(sv1, "value1", sv2, 42, sv3, true, () -> {
                captured1.set(sv1.get());
                captured2.set(sv2.get());
                captured3.set(sv3.get());
            });

            assertThat(captured1.get()).isEqualTo("value1");
            assertThat(captured2.get()).isEqualTo(42);
            assertThat(captured3.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("callWhere 测试")
    class CallWhereTests {

        @Test
        @DisplayName("callWhere 单个绑定")
        void testCallWhereSingle() throws Throwable {
            ScopedValue<String> sv = ScopedValue.newInstance();

            String result = ScopedValueUtil.callWhere(sv, "testValue", () -> {
                return sv.get() + "-processed";
            });

            assertThat(result).isEqualTo("testValue-processed");
        }

        @Test
        @DisplayName("callWhere 双绑定")
        void testCallWhereDouble() throws Throwable {
            ScopedValue<String> sv1 = ScopedValue.newInstance();
            ScopedValue<Integer> sv2 = ScopedValue.newInstance();

            String result = ScopedValueUtil.callWhere(sv1, "prefix", sv2, 100, () -> {
                return sv1.get() + "-" + sv2.get();
            });

            assertThat(result).isEqualTo("prefix-100");
        }

        @Test
        @DisplayName("callWhere 异常传播")
        void testCallWhereException() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            assertThatThrownBy(() -> {
                ScopedValueUtil.callWhere(sv, "value", () -> {
                    throw new IllegalStateException("Test exception");
                });
            }).isInstanceOf(IllegalStateException.class)
                    .hasMessage("Test exception");
        }
    }

    @Nested
    @DisplayName("isBound 测试")
    class IsBoundTests {

        @Test
        @DisplayName("未绑定时返回 false")
        void testNotBound() {
            ScopedValue<String> sv = ScopedValue.newInstance();
            assertThat(ScopedValueUtil.isBound(sv)).isFalse();
        }

        @Test
        @DisplayName("作用域内返回 true")
        void testBoundInScope() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            ScopedValueUtil.runWhere(sv, "value", () -> {
                assertThat(ScopedValueUtil.isBound(sv)).isTrue();
            });
        }
    }

    @Nested
    @DisplayName("getOrDefault 测试")
    class GetOrDefaultTests {

        @Test
        @DisplayName("未绑定时返回默认值")
        void testGetOrDefaultNotBound() {
            ScopedValue<String> sv = ScopedValue.newInstance();
            String result = ScopedValueUtil.getOrDefault(sv, "default");
            assertThat(result).isEqualTo("default");
        }

        @Test
        @DisplayName("已绑定时返回绑定值")
        void testGetOrDefaultBound() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            ScopedValueUtil.runWhere(sv, "boundValue", () -> {
                String result = ScopedValueUtil.getOrDefault(sv, "default");
                assertThat(result).isEqualTo("boundValue");
            });
        }
    }

    @Nested
    @DisplayName("get 测试")
    class GetTests {

        @Test
        @DisplayName("已绑定时返回值")
        void testGetBound() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            ScopedValueUtil.runWhere(sv, "testValue", () -> {
                String result = ScopedValueUtil.get(sv);
                assertThat(result).isEqualTo("testValue");
            });
        }

        @Test
        @DisplayName("未绑定时抛出异常")
        void testGetNotBound() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            assertThatThrownBy(() -> ScopedValueUtil.get(sv))
                    .isInstanceOf(java.util.NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("where 测试")
    class WhereTests {

        @Test
        @DisplayName("where 创建 Carrier")
        void testWhere() {
            ScopedValue<String> sv = ScopedValue.newInstance();
            ScopedValue.Carrier carrier = ScopedValueUtil.where(sv, "value");
            assertThat(carrier).isNotNull();
        }

        @Test
        @DisplayName("where 链式绑定")
        void testWhereChained() {
            ScopedValue<String> sv1 = ScopedValue.newInstance();
            ScopedValue<Integer> sv2 = ScopedValue.newInstance();
            AtomicReference<String> captured1 = new AtomicReference<>();
            AtomicReference<Integer> captured2 = new AtomicReference<>();

            ScopedValueUtil.where(sv1, "value1")
                    .where(sv2, 42)
                    .run(() -> {
                        captured1.set(sv1.get());
                        captured2.set(sv2.get());
                    });

            assertThat(captured1.get()).isEqualTo("value1");
            assertThat(captured2.get()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("嵌套作用域测试")
    class NestedScopeTests {

        @Test
        @DisplayName("嵌套作用域覆盖值")
        void testNestedScopeOverride() {
            ScopedValue<String> sv = ScopedValue.newInstance();
            AtomicReference<String> outer = new AtomicReference<>();
            AtomicReference<String> inner = new AtomicReference<>();
            AtomicReference<String> afterInner = new AtomicReference<>();

            ScopedValueUtil.runWhere(sv, "outer", () -> {
                outer.set(sv.get());

                ScopedValueUtil.runWhere(sv, "inner", () -> {
                    inner.set(sv.get());
                });

                afterInner.set(sv.get());
            });

            assertThat(outer.get()).isEqualTo("outer");
            assertThat(inner.get()).isEqualTo("inner");
            assertThat(afterInner.get()).isEqualTo("outer");
        }
    }

    @Nested
    @DisplayName("null 值测试")
    class NullValueTests {

        @Test
        @DisplayName("绑定 null 值")
        void testBindNullValue() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            ScopedValueUtil.runWhere(sv, null, () -> {
                assertThat(sv.isBound()).isTrue();
                assertThat(sv.get()).isNull();
            });
        }
    }

    @Nested
    @DisplayName("getIfBound 测试")
    class GetIfBoundTests {

        @Test
        @DisplayName("已绑定时返回 present Optional")
        void testGetIfBoundWhenBound() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            ScopedValueUtil.runWhere(sv, "hello", () -> {
                Optional<String> result = ScopedValueUtil.getIfBound(sv);
                assertThat(result).isPresent().contains("hello");
            });
        }

        @Test
        @DisplayName("未绑定时返回 empty Optional")
        void testGetIfBoundWhenNotBound() {
            ScopedValue<String> sv = ScopedValue.newInstance();
            Optional<String> result = ScopedValueUtil.getIfBound(sv);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("绑定 null 值时返回 empty Optional（而非 NPE）")
        void testGetIfBoundWithNullValue() {
            ScopedValue<String> sv = ScopedValue.newInstance();

            ScopedValueUtil.runWhere(sv, null, () -> {
                assertThat(sv.isBound()).isTrue();
                Optional<String> result = ScopedValueUtil.getIfBound(sv);
                // Should return empty Optional for null value, NOT throw NPE
                assertThat(result).isEmpty();
            });
        }
    }

    @Nested
    @DisplayName("runWhere(Carrier) 测试")
    class RunWhereCarrierTests {

        @Test
        @DisplayName("通过 Carrier 同时绑定两个 ScopedValue")
        void testRunWhereWithCarrier() {
            ScopedValue<String> sv1 = ScopedValue.newInstance();
            ScopedValue<Integer> sv2 = ScopedValue.newInstance();
            AtomicReference<String> captured1 = new AtomicReference<>();
            AtomicReference<Integer> captured2 = new AtomicReference<>();

            ScopedValue.Carrier carrier = ScopedValue.where(sv1, "carrierVal")
                    .where(sv2, 99);

            ScopedValueUtil.runWhere(carrier, () -> {
                captured1.set(sv1.get());
                captured2.set(sv2.get());
            });

            assertThat(captured1.get()).isEqualTo("carrierVal");
            assertThat(captured2.get()).isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("callWhere(Carrier) 测试")
    class CallWhereCarrierTests {

        @Test
        @DisplayName("通过 Carrier 调用并返回计算值")
        void testCallWhereWithCarrier() throws Throwable {
            ScopedValue<String> sv1 = ScopedValue.newInstance();
            ScopedValue<Integer> sv2 = ScopedValue.newInstance();

            ScopedValue.Carrier carrier = ScopedValue.where(sv1, "prefix")
                    .where(sv2, 200);

            String result = ScopedValueUtil.callWhere(carrier, () -> {
                return sv1.get() + "-" + sv2.get();
            });

            assertThat(result).isEqualTo("prefix-200");
        }
    }
}
