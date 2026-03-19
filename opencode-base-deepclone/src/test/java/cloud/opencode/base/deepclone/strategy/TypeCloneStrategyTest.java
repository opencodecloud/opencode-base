package cloud.opencode.base.deepclone.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeCloneStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("TypeCloneStrategy 测试")
class TypeCloneStrategyTest {

    // Test entity
    public static class TestEntity {
        private String name;

        public TestEntity() {}
        public TestEntity(String name) { this.name = name; }
        public String getName() { return name; }
    }

    @Nested
    @DisplayName("构造方法测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建TypeCloneStrategy")
        void testConstructor() {
            TypeCloneStrategy<String> strategy = new TypeCloneStrategy<>(
                    String.class,
                    s -> s + "-cloned",
                    true
            );

            assertThat(strategy.type()).isEqualTo(String.class);
            assertThat(strategy.deepClone()).isTrue();
        }

        @Test
        @DisplayName("type为null抛出异常")
        void testNullType() {
            assertThatThrownBy(() -> new TypeCloneStrategy<>(null, s -> s, true))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("type must not be null");
        }

        @Test
        @DisplayName("cloner为null抛出异常")
        void testNullCloner() {
            assertThatThrownBy(() -> new TypeCloneStrategy<>(String.class, null, true))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("cloner must not be null");
        }
    }

    @Nested
    @DisplayName("deep() 工厂方法测试")
    class DeepFactoryTests {

        @Test
        @DisplayName("创建深度克隆策略")
        void testDeep() {
            TypeCloneStrategy<TestEntity> strategy = TypeCloneStrategy.deep(
                    TestEntity.class,
                    e -> new TestEntity(e.getName() + "-cloned")
            );

            assertThat(strategy.type()).isEqualTo(TestEntity.class);
            assertThat(strategy.deepClone()).isTrue();
        }

        @Test
        @DisplayName("深度克隆策略应用")
        void testDeepApply() {
            TypeCloneStrategy<String> strategy = TypeCloneStrategy.deep(
                    String.class,
                    s -> s + "-cloned"
            );

            String result = strategy.apply("test");

            assertThat(result).isEqualTo("test-cloned");
        }
    }

    @Nested
    @DisplayName("shallow() 工厂方法测试")
    class ShallowFactoryTests {

        @Test
        @DisplayName("创建浅拷贝策略")
        void testShallow() {
            TypeCloneStrategy<String> strategy = TypeCloneStrategy.shallow(String.class);

            assertThat(strategy.type()).isEqualTo(String.class);
            assertThat(strategy.deepClone()).isFalse();
        }

        @Test
        @DisplayName("浅拷贝策略返回相同引用")
        void testShallowReturnsSameReference() {
            TypeCloneStrategy<TestEntity> strategy = TypeCloneStrategy.shallow(TestEntity.class);

            TestEntity original = new TestEntity("test");
            TestEntity result = strategy.apply(original);

            assertThat(result).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("immutable() 工厂方法测试")
    class ImmutableFactoryTests {

        @Test
        @DisplayName("创建不可变策略")
        void testImmutable() {
            TypeCloneStrategy<String> strategy = TypeCloneStrategy.immutable(String.class);

            assertThat(strategy.type()).isEqualTo(String.class);
            assertThat(strategy.deepClone()).isFalse();
        }

        @Test
        @DisplayName("不可变策略返回相同引用")
        void testImmutableReturnsSameReference() {
            TypeCloneStrategy<String> strategy = TypeCloneStrategy.immutable(String.class);

            String original = "test";
            String result = strategy.apply(original);

            assertThat(result).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("apply() 测试")
    class ApplyTests {

        @Test
        @DisplayName("应用克隆函数")
        void testApply() {
            TypeCloneStrategy<Integer> strategy = TypeCloneStrategy.deep(
                    Integer.class,
                    i -> i * 2
            );

            Integer result = strategy.apply(5);

            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("null输入返回null")
        void testApplyNull() {
            TypeCloneStrategy<String> strategy = TypeCloneStrategy.deep(
                    String.class,
                    s -> s + "-cloned"
            );

            String result = strategy.apply(null);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Record组件访问测试")
    class RecordComponentsTests {

        @Test
        @DisplayName("访问type组件")
        void testTypeComponent() {
            TypeCloneStrategy<String> strategy = TypeCloneStrategy.shallow(String.class);

            assertThat(strategy.type()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("访问cloner组件")
        void testClonerComponent() {
            UnaryOperator<String> cloner = s -> s;
            TypeCloneStrategy<String> strategy = new TypeCloneStrategy<>(String.class, cloner, true);

            assertThat(strategy.cloner()).isSameAs(cloner);
        }

        @Test
        @DisplayName("访问deepClone组件")
        void testDeepCloneComponent() {
            TypeCloneStrategy<String> deepStrategy = TypeCloneStrategy.deep(String.class, s -> s);
            TypeCloneStrategy<String> shallowStrategy = TypeCloneStrategy.shallow(String.class);

            assertThat(deepStrategy.deepClone()).isTrue();
            assertThat(shallowStrategy.deepClone()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals() 和 hashCode() 测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("相同策略相等")
        void testEquals() {
            UnaryOperator<String> cloner = s -> s;
            TypeCloneStrategy<String> strategy1 = new TypeCloneStrategy<>(String.class, cloner, true);
            TypeCloneStrategy<String> strategy2 = new TypeCloneStrategy<>(String.class, cloner, true);

            assertThat(strategy1).isEqualTo(strategy2);
            assertThat(strategy1.hashCode()).isEqualTo(strategy2.hashCode());
        }

        @Test
        @DisplayName("不同策略不相等")
        void testNotEquals() {
            TypeCloneStrategy<String> strategy1 = TypeCloneStrategy.deep(String.class, s -> s);
            TypeCloneStrategy<String> strategy2 = TypeCloneStrategy.shallow(String.class);

            assertThat(strategy1).isNotEqualTo(strategy2);
        }
    }
}
