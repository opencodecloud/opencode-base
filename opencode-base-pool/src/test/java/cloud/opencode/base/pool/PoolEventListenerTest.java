package cloud.opencode.base.pool;

import cloud.opencode.base.pool.factory.BasePooledObjectFactory;
import cloud.opencode.base.pool.impl.GenericObjectPool;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * PoolEventListenerTest - Tests for Pool Lifecycle Event Listener
 * PoolEventListenerTest - 池生命周期事件监听器测试类
 *
 * <p>Verifies event notifications for borrow, return, create, destroy,
 * exhaustion, timeout, default no-op behavior, and exception isolation.</p>
 * <p>验证借用、归还、创建、销毁、耗尽、超时事件通知，
 * 默认空操作行为以及异常隔离。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
@DisplayName("PoolEventListener 测试")
class PoolEventListenerTest {

    private GenericObjectPool<String> pool;
    private AtomicInteger createCounter;

    @BeforeEach
    void setUp() {
        createCounter = new AtomicInteger(0);
    }

    @AfterEach
    void tearDown() {
        if (pool != null) {
            pool.close();
        }
    }

    private PooledObjectFactory<String> stringFactory() {
        return new BasePooledObjectFactory<>() {
            @Override
            protected String create() {
                return "object-" + createCounter.incrementAndGet();
            }
        };
    }

    @Nested
    @DisplayName("默认方法测试 - Default Methods")
    class DefaultMethodTests {

        /**
         * Tests that all default methods are no-ops (do not throw exceptions).
         * 测试所有默认方法为空操作（不抛出异常）。
         */
        @Test
        @DisplayName("所有默认方法为空操作")
        void testAllDefaultMethodsAreNoOp() {
            PoolEventListener<String> listener = new PoolEventListener<>() {};

            assertThatCode(() -> {
                listener.onBorrow("test");
                listener.onReturn("test");
                listener.onCreate("test");
                listener.onDestroy("test");
                listener.onEvict("test");
                listener.onExhausted();
                listener.onTimeout(Duration.ofSeconds(1));
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("事件通知测试 - Event Notifications")
    class EventNotificationTests {

        /**
         * Tests that onBorrow fires when an object is borrowed.
         * 测试借用对象时触发 onBorrow。
         */
        @Test
        @DisplayName("onBorrow 借用时触发")
        void testOnBorrowFires() {
            var borrowedObjects = new CopyOnWriteArrayList<String>();
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onBorrow(String object) {
                    borrowedObjects.add(object);
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .minIdle(0)
                            .eventListener(listener)
                            .build());

            String obj = pool.borrowObject();
            pool.returnObject(obj);

            assertThat(borrowedObjects).containsExactly(obj);
        }

        /**
         * Tests that onReturn fires when an object is returned.
         * 测试归还对象时触发 onReturn。
         */
        @Test
        @DisplayName("onReturn 归还时触发")
        void testOnReturnFires() {
            var returnedObjects = new CopyOnWriteArrayList<String>();
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onReturn(String object) {
                    returnedObjects.add(object);
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .minIdle(0)
                            .eventListener(listener)
                            .build());

            String obj = pool.borrowObject();
            pool.returnObject(obj);

            assertThat(returnedObjects).containsExactly(obj);
        }

        /**
         * Tests that onCreate fires when an object is created.
         * 测试创建对象时触发 onCreate。
         */
        @Test
        @DisplayName("onCreate 创建时触发")
        void testOnCreateFires() {
            var createdObjects = new CopyOnWriteArrayList<String>();
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onCreate(String object) {
                    createdObjects.add(object);
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .minIdle(0)
                            .eventListener(listener)
                            .build());

            String obj = pool.borrowObject();
            pool.returnObject(obj);

            assertThat(createdObjects).isNotEmpty();
            assertThat(createdObjects).contains(obj);
        }

        /**
         * Tests that onDestroy fires when an object is destroyed.
         * 测试销毁对象时触发 onDestroy。
         */
        @Test
        @DisplayName("onDestroy 销毁时触发")
        void testOnDestroyFires() {
            var destroyedObjects = new CopyOnWriteArrayList<String>();
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onDestroy(String object) {
                    destroyedObjects.add(object);
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .minIdle(0)
                            .eventListener(listener)
                            .build());

            String obj = pool.borrowObject();
            pool.invalidateObject(obj);

            assertThat(destroyedObjects).containsExactly(obj);
        }

        /**
         * Tests that multiple events fire in correct order for a borrow/return cycle.
         * 测试借用/归还循环中多个事件按正确顺序触发。
         */
        @Test
        @DisplayName("借用/归还循环中事件按顺序触发")
        void testEventOrderInBorrowReturnCycle() {
            var events = new CopyOnWriteArrayList<String>();
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onCreate(String object) {
                    events.add("create");
                }

                @Override
                public void onBorrow(String object) {
                    events.add("borrow");
                }

                @Override
                public void onReturn(String object) {
                    events.add("return");
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .minIdle(0)
                            .eventListener(listener)
                            .build());

            String obj = pool.borrowObject();
            pool.returnObject(obj);

            assertThat(events).containsSubsequence("create", "borrow", "return");
        }
    }

    @Nested
    @DisplayName("耗尽与超时测试 - Exhaustion & Timeout")
    class ExhaustionTests {

        /**
         * Tests that onExhausted fires when pool is exhausted.
         * 测试池耗尽时触发 onExhausted。
         */
        @Test
        @DisplayName("onExhausted 池耗尽时触发")
        void testOnExhaustedFires() {
            var exhausted = new AtomicBoolean(false);
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onExhausted() {
                    exhausted.set(true);
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .maxTotal(1)
                            .minIdle(0)
                            .maxWait(Duration.ofMillis(100))
                            .eventListener(listener)
                            .build());

            // Borrow the only available object
            String obj = pool.borrowObject();

            // Try to borrow again (pool exhausted)
            try {
                pool.borrowObject(Duration.ofMillis(50));
            } catch (Exception _) {
                // expected timeout
            }

            pool.returnObject(obj);

            assertThat(exhausted.get()).isTrue();
        }

        /**
         * Tests that onTimeout fires when borrow times out.
         * 测试借用超时时触发 onTimeout。
         */
        @Test
        @DisplayName("onTimeout 借用超时时触发")
        void testOnTimeoutFires() {
            var timedOut = new AtomicBoolean(false);
            var waitDurationRef = new AtomicReference<Duration>();
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onTimeout(Duration waitDuration) {
                    timedOut.set(true);
                    waitDurationRef.set(waitDuration);
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .maxTotal(1)
                            .minIdle(0)
                            .maxWait(Duration.ofMillis(100))
                            .eventListener(listener)
                            .build());

            String obj = pool.borrowObject();

            try {
                pool.borrowObject(Duration.ofMillis(50));
            } catch (Exception _) {
                // expected timeout
            }

            pool.returnObject(obj);

            assertThat(timedOut.get()).isTrue();
            assertThat(waitDurationRef.get()).isNotNull();
        }
    }

    @Nested
    @DisplayName("异常隔离测试 - Exception Isolation")
    class ExceptionIsolationTests {

        /**
         * Tests that listener exception does not break pool operation.
         * 测试监听器异常不会中断池操作。
         */
        @Test
        @DisplayName("监听器异常不影响池操作")
        void testListenerExceptionDoesNotBreakPool() {
            PoolEventListener<String> throwingListener = new PoolEventListener<>() {
                @Override
                public void onBorrow(String object) {
                    throw new RuntimeException("listener error on borrow");
                }

                @Override
                public void onReturn(String object) {
                    throw new RuntimeException("listener error on return");
                }

                @Override
                public void onCreate(String object) {
                    throw new RuntimeException("listener error on create");
                }

                @Override
                public void onDestroy(String object) {
                    throw new RuntimeException("listener error on destroy");
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .minIdle(0)
                            .eventListener(throwingListener)
                            .build());

            // Pool operations should succeed despite listener exceptions
            assertThatCode(() -> {
                String obj = pool.borrowObject();
                pool.returnObject(obj);

                String obj2 = pool.borrowObject();
                pool.invalidateObject(obj2);
            }).doesNotThrowAnyException();

            assertThat(pool.getNumActive()).isZero();
        }

        /**
         * Tests that listener exception in onExhausted does not break pool.
         * 测试 onExhausted 中监听器异常不会中断池。
         */
        @Test
        @DisplayName("onExhausted 异常不影响池操作")
        void testOnExhaustedExceptionDoesNotBreakPool() {
            PoolEventListener<String> throwingListener = new PoolEventListener<>() {
                @Override
                public void onExhausted() {
                    throw new RuntimeException("listener error on exhausted");
                }

                @Override
                public void onTimeout(Duration d) {
                    throw new RuntimeException("listener error on timeout");
                }
            };

            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .maxTotal(1)
                            .minIdle(0)
                            .maxWait(Duration.ofMillis(50))
                            .eventListener(throwingListener)
                            .build());

            String obj = pool.borrowObject();

            // The pool operation itself should throw timeout, not listener exception
            assertThatThrownBy(() -> pool.borrowObject(Duration.ofMillis(20)))
                    .isInstanceOf(Exception.class);

            pool.returnObject(obj);
        }
    }

    @Nested
    @DisplayName("集成测试 - Integration")
    class IntegrationTests {

        /**
         * Tests full lifecycle events with GenericObjectPool via PoolConfig.
         * 测试通过 PoolConfig 与 GenericObjectPool 的完整生命周期事件。
         */
        @Test
        @DisplayName("PoolConfig 配置监听器的完整生命周期")
        void testFullLifecycleWithConfig() {
            var events = new CopyOnWriteArrayList<String>();
            PoolEventListener<String> listener = new PoolEventListener<>() {
                @Override
                public void onBorrow(String object) {
                    events.add("borrow:" + object);
                }

                @Override
                public void onReturn(String object) {
                    events.add("return:" + object);
                }

                @Override
                public void onCreate(String object) {
                    events.add("create:" + object);
                }

                @Override
                public void onDestroy(String object) {
                    events.add("destroy:" + object);
                }
            };

            PoolConfig config = PoolConfig.builder()
                    .maxTotal(2)
                    .minIdle(0)
                    .eventListener(listener)
                    .build();

            pool = new GenericObjectPool<>(stringFactory(), config);

            // Borrow, use, invalidate
            String obj = pool.borrowObject();
            pool.invalidateObject(obj);

            assertThat(events).anyMatch(e -> e.startsWith("create:"));
            assertThat(events).anyMatch(e -> e.startsWith("borrow:"));
            assertThat(events).anyMatch(e -> e.startsWith("destroy:"));
        }

        /**
         * Tests that null event listener in config works (no events fired).
         * 测试配置中 null 事件监听器正常工作（不触发事件）。
         */
        @Test
        @DisplayName("null 监听器不触发事件")
        void testNullListenerNoEvents() {
            pool = new GenericObjectPool<>(stringFactory(),
                    PoolConfig.builder()
                            .minIdle(0)
                            .eventListener(null)
                            .build());

            assertThatCode(() -> {
                String obj = pool.borrowObject();
                pool.returnObject(obj);
            }).doesNotThrowAnyException();
        }
    }
}
