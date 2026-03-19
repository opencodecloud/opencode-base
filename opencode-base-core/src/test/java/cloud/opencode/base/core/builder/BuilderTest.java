package cloud.opencode.base.core.builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Builder 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("Builder 测试")
class BuilderTest {

    @Nested
    @DisplayName("函数式接口测试")
    class FunctionalInterfaceTests {

        @Test
        @DisplayName("使用 Lambda 实现")
        void testLambdaImplementation() {
            Builder<String> builder = () -> "test";
            assertThat(builder.build()).isEqualTo("test");
        }

        @Test
        @DisplayName("使用方法引用实现")
        void testMethodReferenceImplementation() {
            Builder<String> builder = UUID.randomUUID()::toString;
            String result = builder.build();
            assertThat(result).isNotNull();
            assertThat(result).hasSize(36);
        }

        @Test
        @DisplayName("自定义对象构建")
        void testCustomObjectBuild() {
            Builder<TestObject> builder = () -> new TestObject("name", 25);
            TestObject obj = builder.build();
            assertThat(obj.name).isEqualTo("name");
            assertThat(obj.age).isEqualTo(25);
        }

        @Test
        @DisplayName("有状态构建器")
        void testStatefulBuilder() {
            AtomicInteger counter = new AtomicInteger(0);
            Builder<Integer> builder = counter::incrementAndGet;

            assertThat(builder.build()).isEqualTo(1);
            assertThat(builder.build()).isEqualTo(2);
            assertThat(builder.build()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("build 方法测试")
    class BuildMethodTests {

        @Test
        @DisplayName("build 返回非 null")
        void testBuildNotNull() {
            Builder<Object> builder = Object::new;
            assertThat(builder.build()).isNotNull();
        }

        @Test
        @DisplayName("build 返回 null")
        void testBuildNull() {
            Builder<Object> builder = () -> null;
            assertThat(builder.build()).isNull();
        }

        @Test
        @DisplayName("build 可抛异常")
        void testBuildThrowsException() {
            Builder<Object> builder = () -> {
                throw new RuntimeException("Build failed");
            };

            assertThatThrownBy(builder::build)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Build failed");
        }
    }

    @Nested
    @DisplayName("复杂场景测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("嵌套构建器")
        void testNestedBuilder() {
            Builder<Builder<String>> outerBuilder = () -> () -> "nested";
            Builder<String> innerBuilder = outerBuilder.build();
            assertThat(innerBuilder.build()).isEqualTo("nested");
        }

        @Test
        @DisplayName("构建器组合")
        void testBuilderComposition() {
            Builder<String> nameBuilder = () -> "John";
            Builder<Integer> ageBuilder = () -> 30;

            Builder<TestObject> compositeBuilder = () ->
                    new TestObject(nameBuilder.build(), ageBuilder.build());

            TestObject obj = compositeBuilder.build();
            assertThat(obj.name).isEqualTo("John");
            assertThat(obj.age).isEqualTo(30);
        }
    }

    static class TestObject {
        String name;
        int age;

        TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
