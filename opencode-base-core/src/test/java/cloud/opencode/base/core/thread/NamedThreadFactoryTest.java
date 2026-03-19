package cloud.opencode.base.core.thread;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadFactory;

import static org.assertj.core.api.Assertions.*;

/**
 * NamedThreadFactory 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("NamedThreadFactory 测试")
class NamedThreadFactoryTest {

    @Nested
    @DisplayName("基本构造测试")
    class ConstructorTests {

        @Test
        @DisplayName("简单构造器")
        void testSimpleConstructor() {
            NamedThreadFactory factory = new NamedThreadFactory("test");
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getName()).startsWith("test-");
            assertThat(thread.isDaemon()).isFalse();
            assertThat(thread.getPriority()).isEqualTo(Thread.NORM_PRIORITY);
        }

        @Test
        @DisplayName("带 daemon 参数构造器")
        void testDaemonConstructor() {
            NamedThreadFactory factory = new NamedThreadFactory("daemon-test", true);
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getName()).startsWith("daemon-test-");
            assertThat(thread.isDaemon()).isTrue();
        }

        @Test
        @DisplayName("完整构造器")
        void testFullConstructor() {
            NamedThreadFactory factory = new NamedThreadFactory("full", true, Thread.MAX_PRIORITY);
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getName()).startsWith("full-");
            assertThat(thread.isDaemon()).isTrue();
            assertThat(thread.getPriority()).isEqualTo(Thread.MAX_PRIORITY);
        }
    }

    @Nested
    @DisplayName("newThread 测试")
    class NewThreadTests {

        @Test
        @DisplayName("newThread 命名自增")
        void testNewThreadAutoIncrement() {
            NamedThreadFactory factory = new NamedThreadFactory("worker");

            Thread t1 = factory.newThread(() -> {});
            Thread t2 = factory.newThread(() -> {});
            Thread t3 = factory.newThread(() -> {});

            assertThat(t1.getName()).isEqualTo("worker-1");
            assertThat(t2.getName()).isEqualTo("worker-2");
            assertThat(t3.getName()).isEqualTo("worker-3");
        }

        @Test
        @DisplayName("newThread 线程可执行")
        void testNewThreadExecutable() throws InterruptedException {
            NamedThreadFactory factory = new NamedThreadFactory("exec-test");
            StringBuilder result = new StringBuilder();

            Thread thread = factory.newThread(() -> result.append("executed"));
            thread.start();
            thread.join(1000);

            assertThat(result.toString()).isEqualTo("executed");
        }
    }

    @Nested
    @DisplayName("静态工厂方法测试")
    class StaticFactoryTests {

        @Test
        @DisplayName("daemon 静态方法")
        void testDaemonFactory() {
            NamedThreadFactory factory = NamedThreadFactory.daemon("daemon");
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.isDaemon()).isTrue();
            assertThat(thread.getName()).startsWith("daemon-");
        }

        @Test
        @DisplayName("nonDaemon 静态方法")
        void testNonDaemonFactory() {
            NamedThreadFactory factory = NamedThreadFactory.nonDaemon("non-daemon");
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.isDaemon()).isFalse();
            assertThat(thread.getName()).startsWith("non-daemon-");
        }
    }

    @Nested
    @DisplayName("Builder 测试")
    class BuilderTests {

        @Test
        @DisplayName("builder 默认值")
        void testBuilderDefaults() {
            NamedThreadFactory factory = NamedThreadFactory.builder().build();
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getName()).startsWith("thread-");
            assertThat(thread.isDaemon()).isFalse();
            assertThat(thread.getPriority()).isEqualTo(Thread.NORM_PRIORITY);
        }

        @Test
        @DisplayName("builder 设置 namePrefix")
        void testBuilderNamePrefix() {
            NamedThreadFactory factory = NamedThreadFactory.builder()
                    .namePrefix("custom")
                    .build();
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getName()).startsWith("custom-");
        }

        @Test
        @DisplayName("builder 设置 daemon")
        void testBuilderDaemon() {
            NamedThreadFactory factory = NamedThreadFactory.builder()
                    .daemon(true)
                    .build();
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.isDaemon()).isTrue();
        }

        @Test
        @DisplayName("builder 设置 priority")
        void testBuilderPriority() {
            NamedThreadFactory factory = NamedThreadFactory.builder()
                    .priority(Thread.MIN_PRIORITY)
                    .build();
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getPriority()).isEqualTo(Thread.MIN_PRIORITY);
        }

        @Test
        @DisplayName("builder 链式调用")
        void testBuilderChaining() {
            NamedThreadFactory factory = NamedThreadFactory.builder()
                    .namePrefix("chained")
                    .daemon(true)
                    .priority(Thread.MAX_PRIORITY)
                    .build();
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getName()).startsWith("chained-");
            assertThat(thread.isDaemon()).isTrue();
            assertThat(thread.getPriority()).isEqualTo(Thread.MAX_PRIORITY);
        }
    }

    @Nested
    @DisplayName("ThreadFactory 接口兼容性测试")
    class InterfaceCompatibilityTests {

        @Test
        @DisplayName("实现 ThreadFactory 接口")
        void testImplementsThreadFactory() {
            NamedThreadFactory factory = new NamedThreadFactory("compat");
            assertThat(factory).isInstanceOf(ThreadFactory.class);
        }

        @Test
        @DisplayName("可用于 ExecutorService")
        void testUsableWithExecutorService() {
            NamedThreadFactory factory = new NamedThreadFactory("executor");
            java.util.concurrent.ExecutorService executor =
                    java.util.concurrent.Executors.newFixedThreadPool(2, factory);

            try {
                executor.submit(() -> {
                    assertThat(Thread.currentThread().getName()).startsWith("executor-");
                }).get();
            } catch (Exception e) {
                fail("Should not throw exception");
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Nested
    @DisplayName("线程组测试")
    class ThreadGroupTests {

        @Test
        @DisplayName("线程属于当前线程组")
        void testThreadGroupInheritance() {
            NamedThreadFactory factory = new NamedThreadFactory("group-test");
            Thread thread = factory.newThread(() -> {});

            assertThat(thread.getThreadGroup()).isEqualTo(Thread.currentThread().getThreadGroup());
        }
    }
}
