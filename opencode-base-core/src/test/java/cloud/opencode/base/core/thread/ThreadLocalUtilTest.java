package cloud.opencode.base.core.thread;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * ThreadLocalUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("ThreadLocalUtil 测试")
class ThreadLocalUtilTest {

    @AfterEach
    void cleanup() {
        ThreadLocalUtil.clear();
    }

    @Nested
    @DisplayName("基本操作测试")
    class BasicOperationsTests {

        @Test
        @DisplayName("set 和 get")
        void testSetAndGet() {
            ThreadLocalUtil.set("key1", "value1");
            String value = ThreadLocalUtil.get("key1");
            assertThat(value).isEqualTo("value1");
        }

        @Test
        @DisplayName("get 不存在的 key")
        void testGetNotExist() {
            String value = ThreadLocalUtil.get("notExist");
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("get 带默认值")
        void testGetWithDefault() {
            String value = ThreadLocalUtil.get("notExist", "default");
            assertThat(value).isEqualTo("default");

            ThreadLocalUtil.set("key", "value");
            String value2 = ThreadLocalUtil.get("key", "default");
            assertThat(value2).isEqualTo("value");
        }

        @Test
        @DisplayName("remove")
        void testRemove() {
            ThreadLocalUtil.set("key", "value");
            assertThat(ThreadLocalUtil.contains("key")).isTrue();

            ThreadLocalUtil.remove("key");
            assertThat((Object) ThreadLocalUtil.get("key")).isNull();
        }

        @Test
        @DisplayName("clear")
        void testClear() {
            ThreadLocalUtil.set("key1", "value1");
            ThreadLocalUtil.set("key2", "value2");

            ThreadLocalUtil.clear();

            assertThat((Object) ThreadLocalUtil.get("key1")).isNull();
            assertThat((Object) ThreadLocalUtil.get("key2")).isNull();
        }
    }

    @Nested
    @DisplayName("getOrCompute 测试")
    class GetOrComputeTests {

        @Test
        @DisplayName("getOrCompute 不存在时计算")
        void testGetOrComputeNotExist() {
            AtomicInteger counter = new AtomicInteger(0);

            String value = ThreadLocalUtil.getOrCompute("key",
                    () -> {
                        counter.incrementAndGet();
                        return "computed";
                    });

            assertThat(value).isEqualTo("computed");
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("getOrCompute 已存在时不计算")
        void testGetOrComputeExist() {
            ThreadLocalUtil.set("key", "existing");
            AtomicInteger counter = new AtomicInteger(0);

            String value = ThreadLocalUtil.getOrCompute("key",
                    () -> {
                        counter.incrementAndGet();
                        return "computed";
                    });

            assertThat(value).isEqualTo("existing");
            assertThat(counter.get()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("contains 测试")
    class ContainsTests {

        @Test
        @DisplayName("contains true")
        void testContainsTrue() {
            ThreadLocalUtil.set("key", "value");
            assertThat(ThreadLocalUtil.contains("key")).isTrue();
        }

        @Test
        @DisplayName("contains false")
        void testContainsFalse() {
            assertThat(ThreadLocalUtil.contains("notExist")).isFalse();
        }
    }

    @Nested
    @DisplayName("ThreadLocal 创建测试")
    class CreateTests {

        @Test
        @DisplayName("create")
        void testCreate() {
            ThreadLocal<String> tl = ThreadLocalUtil.create();
            assertThat(tl).isNotNull();
            assertThat(tl.get()).isNull();

            tl.set("value");
            assertThat(tl.get()).isEqualTo("value");
            tl.remove();
        }

        @Test
        @DisplayName("createWithInitial")
        void testCreateWithInitial() {
            ThreadLocal<String> tl = ThreadLocalUtil.createWithInitial(() -> "initial");
            assertThat(tl.get()).isEqualTo("initial");
            tl.remove();
        }

        @Test
        @DisplayName("createInheritable")
        void testCreateInheritable() {
            InheritableThreadLocal<String> tl = ThreadLocalUtil.createInheritable();
            assertThat(tl).isNotNull();
            assertThat(tl).isInstanceOf(InheritableThreadLocal.class);
            tl.remove();
        }

        @Test
        @DisplayName("createInheritableWithInitial")
        void testCreateInheritableWithInitial() {
            InheritableThreadLocal<String> tl = ThreadLocalUtil.createInheritableWithInitial(() -> "initial");
            assertThat(tl.get()).isEqualTo("initial");
            tl.remove();
        }
    }

    @Nested
    @DisplayName("上下文执行测试")
    class ContextExecutionTests {

        @Test
        @DisplayName("runWithContext")
        void testRunWithContext() {
            ThreadLocalUtil.set("key", "original");

            StringBuilder sb = new StringBuilder();
            ThreadLocalUtil.runWithContext("key", "contextValue", () -> {
                sb.append((String) ThreadLocalUtil.get("key"));
            });

            assertThat(sb.toString()).isEqualTo("contextValue");
            assertThat((String) ThreadLocalUtil.get("key")).isEqualTo("original");
        }

        @Test
        @DisplayName("runWithContext 原值不存在")
        void testRunWithContextNoOriginal() {
            ThreadLocalUtil.runWithContext("newKey", "contextValue", () -> {
                assertThat((String) ThreadLocalUtil.get("newKey")).isEqualTo("contextValue");
            });

            assertThat((Object) ThreadLocalUtil.get("newKey")).isNull();
        }

        @Test
        @DisplayName("callWithContext")
        void testCallWithContext() {
            ThreadLocalUtil.set("key", "original");

            String result = ThreadLocalUtil.callWithContext("key", "contextValue",
                    () -> ThreadLocalUtil.get("key"));

            assertThat(result).isEqualTo("contextValue");
            assertThat((String) ThreadLocalUtil.get("key")).isEqualTo("original");
        }

        @Test
        @DisplayName("callWithContext 原值不存在")
        void testCallWithContextNoOriginal() {
            String result = ThreadLocalUtil.callWithContext("newKey", "contextValue",
                    () -> ThreadLocalUtil.get("newKey"));

            assertThat(result).isEqualTo("contextValue");
            assertThat((Object) ThreadLocalUtil.get("newKey")).isNull();
        }

        @Test
        @DisplayName("runWithContext 异常恢复")
        void testRunWithContextException() {
            ThreadLocalUtil.set("key", "original");

            assertThatThrownBy(() -> {
                ThreadLocalUtil.runWithContext("key", "contextValue", () -> {
                    throw new RuntimeException("Test exception");
                });
            }).isInstanceOf(RuntimeException.class);

            assertThat((String) ThreadLocalUtil.get("key")).isEqualTo("original");
        }
    }

    @Nested
    @DisplayName("keys 测试")
    class KeysTests {

        @Test
        @DisplayName("keys 返回所有 key")
        void testKeys() {
            ThreadLocalUtil.set("key1", "value1");
            ThreadLocalUtil.set("key2", "value2");

            Set<String> keys = ThreadLocalUtil.keys();
            assertThat(keys).contains("key1", "key2");
        }
    }

    @Nested
    @DisplayName("多类型测试")
    class MultiTypeTests {

        @Test
        @DisplayName("存储整数")
        void testInteger() {
            ThreadLocalUtil.set("number", 123);
            Integer value = ThreadLocalUtil.get("number");
            assertThat(value).isEqualTo(123);
        }

        @Test
        @DisplayName("存储自定义对象")
        void testCustomObject() {
            record User(String name, int age) {}

            User user = new User("Leon", 30);
            ThreadLocalUtil.set("user", user);

            User retrieved = ThreadLocalUtil.get("user");
            assertThat(retrieved).isEqualTo(user);
        }
    }
}
