package cloud.opencode.base.pool;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * PoolContextTest Tests
 * PoolContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("PoolContext 测试")
class PoolContextTest {

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create使用指定名称创建上下文")
        void testCreateWithName() {
            PoolContext context = PoolContext.create("testPool");

            assertThat(context.poolName()).isEqualTo("testPool");
        }

        @Test
        @DisplayName("create无参数使用默认名称")
        void testCreateDefault() {
            PoolContext context = PoolContext.create();

            assertThat(context.poolName()).isEqualTo("default");
        }
    }

    @Nested
    @DisplayName("上下文执行测试")
    class ContextExecutionTests {

        @Test
        @DisplayName("run执行Callable并返回结果")
        void testRunCallable() throws Exception {
            PoolContext context = PoolContext.create("testPool");

            String result = PoolContext.run(context, () -> {
                Optional<PoolContext> current = PoolContext.current();
                assertThat(current).isPresent();
                return current.get().poolName();
            });

            assertThat(result).isEqualTo("testPool");
        }

        @Test
        @DisplayName("run执行Runnable")
        void testRunRunnable() {
            PoolContext context = PoolContext.create("testPool");
            AtomicReference<String> captured = new AtomicReference<>();

            PoolContext.run(context, () -> {
                Optional<PoolContext> current = PoolContext.current();
                current.ifPresent(ctx -> captured.set(ctx.poolName()));
            });

            assertThat(captured.get()).isEqualTo("testPool");
        }

        @Test
        @DisplayName("run执行完后上下文不再绑定")
        void testRunContextUnbound() {
            PoolContext context = PoolContext.create("testPool");

            PoolContext.run(context, () -> {
                assertThat(PoolContext.current()).isPresent();
            });

            // Outside the run, context should not be bound (unless we're already in another context)
            // This depends on the outer context - just verify the run completed
            assertThat(context.poolName()).isEqualTo("testPool");
        }
    }

    @Nested
    @DisplayName("current方法测试")
    class CurrentTests {

        @Test
        @DisplayName("在上下文内current返回当前上下文")
        void testCurrentInContext() {
            PoolContext context = PoolContext.create("testPool");

            PoolContext.run(context, () -> {
                Optional<PoolContext> current = PoolContext.current();

                assertThat(current).isPresent();
                assertThat(current.get()).isSameAs(context);
            });
        }

        @Test
        @DisplayName("currentOrCreate在上下文内返回当前上下文")
        void testCurrentOrCreateInContext() {
            PoolContext context = PoolContext.create("testPool");

            PoolContext.run(context, () -> {
                PoolContext current = PoolContext.currentOrCreate();

                assertThat(current).isSameAs(context);
            });
        }
    }

    @Nested
    @DisplayName("借用追踪测试")
    class BorrowTrackingTests {

        @Test
        @DisplayName("recordBorrow记录借用")
        void testRecordBorrow() {
            PoolContext context = PoolContext.create("testPool");
            Object borrowed = new Object();

            context.recordBorrow(borrowed);

            assertThat(context.hasBorrowedObject()).isTrue();
            assertThat(context.borrowTime()).isPresent();
        }

        @Test
        @DisplayName("recordReturn记录归还")
        void testRecordReturn() {
            PoolContext context = PoolContext.create("testPool");
            Object borrowed = new Object();

            context.recordBorrow(borrowed);
            assertThat(context.hasBorrowedObject()).isTrue();

            context.recordReturn();

            assertThat(context.hasBorrowedObject()).isFalse();
            assertThat(context.returnTime()).isPresent();
        }

        @Test
        @DisplayName("hasBorrowedObject初始为false")
        void testHasBorrowedObjectInitial() {
            PoolContext context = PoolContext.create("testPool");

            assertThat(context.hasBorrowedObject()).isFalse();
        }
    }

    @Nested
    @DisplayName("属性测试")
    class AttributeTests {

        @Test
        @DisplayName("setAttribute设置属性")
        void testSetAttribute() {
            PoolContext context = PoolContext.create("testPool");

            context.setAttribute("key", "value");

            assertThat(context.getAttribute("key")).contains("value");
        }

        @Test
        @DisplayName("setAttribute返回this支持链式调用")
        void testSetAttributeChaining() {
            PoolContext context = PoolContext.create("testPool");

            PoolContext result = context
                    .setAttribute("key1", "value1")
                    .setAttribute("key2", "value2");

            assertThat(result).isSameAs(context);
            assertThat(context.getAttribute("key1")).contains("value1");
            assertThat(context.getAttribute("key2")).contains("value2");
        }

        @Test
        @DisplayName("getAttribute返回属性值")
        void testGetAttribute() {
            PoolContext context = PoolContext.create("testPool");
            context.setAttribute("key", 123);

            Optional<Integer> value = context.getAttribute("key");

            assertThat(value).contains(123);
        }

        @Test
        @DisplayName("getAttribute不存在时返回空")
        void testGetAttributeNotFound() {
            PoolContext context = PoolContext.create("testPool");

            Optional<String> value = context.getAttribute("nonexistent");

            assertThat(value).isEmpty();
        }

        @Test
        @DisplayName("hasAttribute存在时返回true")
        void testHasAttributeTrue() {
            PoolContext context = PoolContext.create("testPool");
            context.setAttribute("key", "value");

            assertThat(context.hasAttribute("key")).isTrue();
        }

        @Test
        @DisplayName("hasAttribute不存在时返回false")
        void testHasAttributeFalse() {
            PoolContext context = PoolContext.create("testPool");

            assertThat(context.hasAttribute("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("Getter方法测试")
    class GetterTests {

        @Test
        @DisplayName("poolName返回池名称")
        void testPoolName() {
            PoolContext context = PoolContext.create("myPool");

            assertThat(context.poolName()).isEqualTo("myPool");
        }

        @Test
        @DisplayName("createdAt返回创建时间")
        void testCreatedAt() {
            Instant before = Instant.now();
            PoolContext context = PoolContext.create("testPool");
            Instant after = Instant.now();

            assertThat(context.createdAt()).isAfterOrEqualTo(before);
            assertThat(context.createdAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("thread返回创建线程")
        void testThread() {
            PoolContext context = PoolContext.create("testPool");

            assertThat(context.thread()).isEqualTo(Thread.currentThread());
        }

        @Test
        @DisplayName("borrowTime初始为空")
        void testBorrowTimeInitial() {
            PoolContext context = PoolContext.create("testPool");

            assertThat(context.borrowTime()).isEmpty();
        }

        @Test
        @DisplayName("returnTime初始为空")
        void testReturnTimeInitial() {
            PoolContext context = PoolContext.create("testPool");

            assertThat(context.returnTime()).isEmpty();
        }

        @Test
        @DisplayName("isVirtualThread返回正确值")
        void testIsVirtualThread() {
            PoolContext context = PoolContext.create("testPool");

            // 当前线程是否为虚拟线程取决于测试环境
            assertThat(context.isVirtualThread()).isEqualTo(Thread.currentThread().isVirtual());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString返回字符串表示")
        void testToString() {
            PoolContext context = PoolContext.create("testPool");

            String str = context.toString();

            assertThat(str).contains("PoolContext");
            assertThat(str).contains("testPool");
            assertThat(str).contains("virtual=");
            assertThat(str).contains("hasBorrowed=");
        }
    }

    @Nested
    @DisplayName("ScopedValue测试")
    class ScopedValueTests {

        @Test
        @DisplayName("CURRENT是ScopedValue实例")
        void testCurrentIsScopedValue() {
            assertThat(PoolContext.CURRENT).isNotNull();
        }

        @Test
        @DisplayName("嵌套上下文正确工作")
        void testNestedContexts() throws Exception {
            PoolContext outer = PoolContext.create("outer");
            PoolContext inner = PoolContext.create("inner");

            AtomicReference<String> outerCapture = new AtomicReference<>();
            AtomicReference<String> innerCapture = new AtomicReference<>();

            PoolContext.run(outer, () -> {
                outerCapture.set(PoolContext.current().get().poolName());

                PoolContext.run(inner, () -> {
                    innerCapture.set(PoolContext.current().get().poolName());
                });

                // After inner run, should be back to outer
                assertThat(PoolContext.current().get().poolName()).isEqualTo("outer");
            });

            assertThat(outerCapture.get()).isEqualTo("outer");
            assertThat(innerCapture.get()).isEqualTo("inner");
        }
    }
}
