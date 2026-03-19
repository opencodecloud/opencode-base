package cloud.opencode.base.log.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * MDC 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("MDC 测试")
class MDCTest {

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(MDC.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = MDC.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("put/get方法测试")
    class PutGetTests {

        @Test
        @DisplayName("put和get基本操作")
        void testPutAndGet() {
            MDC.put("key1", "value1");
            assertThat(MDC.get("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("get不存在的键返回null")
        void testGetNonExistent() {
            assertThat(MDC.get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("覆盖已存在的键")
        void testPutOverwrite() {
            MDC.put("key", "value1");
            MDC.put("key", "value2");
            assertThat(MDC.get("key")).isEqualTo("value2");
        }
    }

    @Nested
    @DisplayName("remove方法测试")
    class RemoveTests {

        @Test
        @DisplayName("移除存在的键")
        void testRemoveExisting() {
            MDC.put("key", "value");
            MDC.remove("key");
            assertThat(MDC.get("key")).isNull();
        }

        @Test
        @DisplayName("移除不存在的键不报错")
        void testRemoveNonExistent() {
            assertThatCode(() -> MDC.remove("nonexistent")).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("clear方法测试")
    class ClearTests {

        @Test
        @DisplayName("清空所有条目")
        void testClear() {
            MDC.put("key1", "value1");
            MDC.put("key2", "value2");
            MDC.clear();

            assertThat(MDC.get("key1")).isNull();
            assertThat(MDC.get("key2")).isNull();
        }
    }

    @Nested
    @DisplayName("getCopyOfContextMap方法测试")
    class GetCopyOfContextMapTests {

        @Test
        @DisplayName("返回上下文映射的副本")
        void testGetCopyOfContextMap() {
            MDC.put("key1", "value1");
            MDC.put("key2", "value2");

            Map<String, String> copy = MDC.getCopyOfContextMap();

            assertThat(copy).containsEntry("key1", "value1");
            assertThat(copy).containsEntry("key2", "value2");
        }

        @Test
        @DisplayName("返回的是副本不是原引用")
        void testGetCopyIsCopy() {
            MDC.put("key", "value");
            Map<String, String> copy = MDC.getCopyOfContextMap();

            MDC.put("key", "newValue");
            assertThat(copy.get("key")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("setContextMap方法测试")
    class SetContextMapTests {

        @Test
        @DisplayName("设置上下文映射")
        void testSetContextMap() {
            Map<String, String> newMap = Map.of("key1", "value1", "key2", "value2");
            MDC.setContextMap(newMap);

            assertThat(MDC.get("key1")).isEqualTo("value1");
            assertThat(MDC.get("key2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("设置上下文映射会替换现有映射")
        void testSetContextMapReplaces() {
            MDC.put("oldKey", "oldValue");
            MDC.setContextMap(Map.of("newKey", "newValue"));

            assertThat(MDC.get("oldKey")).isNull();
            assertThat(MDC.get("newKey")).isEqualTo("newValue");
        }
    }

    @Nested
    @DisplayName("runWith方法测试")
    class RunWithTests {

        @Test
        @DisplayName("临时设置值并执行任务")
        void testRunWith() {
            AtomicReference<String> captured = new AtomicReference<>();

            MDC.runWith("tempKey", "tempValue", () -> {
                captured.set(MDC.get("tempKey"));
            });

            assertThat(captured.get()).isEqualTo("tempValue");
            assertThat(MDC.get("tempKey")).isNull();
        }

        @Test
        @DisplayName("执行后恢复之前的值")
        void testRunWithRestoresPrevious() {
            MDC.put("key", "original");

            MDC.runWith("key", "temp", () -> {
                assertThat(MDC.get("key")).isEqualTo("temp");
            });

            assertThat(MDC.get("key")).isEqualTo("original");
        }
    }

    @Nested
    @DisplayName("callWith方法测试")
    class CallWithTests {

        @Test
        @DisplayName("临时设置值并执行带返回值的任务")
        void testCallWith() {
            String result = MDC.callWith("key", "value", () -> {
                return MDC.get("key") + "-result";
            });

            assertThat(result).isEqualTo("value-result");
            assertThat(MDC.get("key")).isNull();
        }

        @Test
        @DisplayName("执行后恢复之前的值")
        void testCallWithRestoresPrevious() {
            MDC.put("key", "original");

            String result = MDC.callWith("key", "temp", () -> MDC.get("key"));

            assertThat(result).isEqualTo("temp");
            assertThat(MDC.get("key")).isEqualTo("original");
        }
    }

    @Nested
    @DisplayName("scope方法测试")
    class ScopeTests {

        @Test
        @DisplayName("单值scope自动清理")
        void testScopeSingleValue() {
            try (MDC.MDCScope scope = MDC.scope("key", "value")) {
                assertThat(MDC.get("key")).isEqualTo("value");
            }
            assertThat(MDC.get("key")).isNull();
        }

        @Test
        @DisplayName("单值scope恢复之前的值")
        void testScopeSingleValueRestoresPrevious() {
            MDC.put("key", "original");

            try (MDC.MDCScope scope = MDC.scope("key", "temp")) {
                assertThat(MDC.get("key")).isEqualTo("temp");
            }
            assertThat(MDC.get("key")).isEqualTo("original");
        }

        @Test
        @DisplayName("多值scope自动清理")
        void testScopeMultipleValues() {
            try (MDC.MDCScope scope = MDC.scope(Map.of("key1", "value1", "key2", "value2"))) {
                assertThat(MDC.get("key1")).isEqualTo("value1");
                assertThat(MDC.get("key2")).isEqualTo("value2");
            }
        }
    }

    @Nested
    @DisplayName("MDCScope内部类测试")
    class MDCScopeTests {

        @Test
        @DisplayName("MDCScope是final类")
        void testMDCScopeIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(MDC.MDCScope.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("MDCScope实现AutoCloseable")
        void testMDCScopeImplementsAutoCloseable() {
            assertThat(AutoCloseable.class.isAssignableFrom(MDC.MDCScope.class)).isTrue();
        }
    }
}
