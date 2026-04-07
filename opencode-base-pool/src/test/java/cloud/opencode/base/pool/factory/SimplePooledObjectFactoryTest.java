package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.ObjectPool;
import cloud.opencode.base.pool.OpenPool;
import cloud.opencode.base.pool.PoolConfig;
import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.exception.OpenPoolException;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * SimplePooledObjectFactoryTest - Tests for Functional Pooled Object Factory
 * SimplePooledObjectFactoryTest - 函数式池化对象工厂测试类
 *
 * <p>Verifies static factory methods, builder pattern, lifecycle callbacks,
 * defaults, null handling, and integration with OpenPool.</p>
 * <p>验证静态工厂方法、建造者模式、生命周期回调、默认值、空值处理
 * 以及与 OpenPool 的集成。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.3
 */
@DisplayName("SimplePooledObjectFactory 测试")
class SimplePooledObjectFactoryTest {

    @Nested
    @DisplayName("of(Supplier) 工厂方法测试")
    class OfSupplierTests {

        /**
         * Tests that of(Supplier) creates a factory that produces objects.
         * 测试 of(Supplier) 创建能生产对象的工厂。
         */
        @Test
        @DisplayName("of(Supplier) 创建工厂并生产对象")
        void testOfSupplierCreatesFactory() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.of(StringBuilder::new);

            PooledObject<StringBuilder> po = factory.makeObject();

            assertThat(po).isNotNull();
            assertThat(po.getObject()).isInstanceOf(StringBuilder.class);
        }

        /**
         * Tests that of(Supplier) wraps object in DefaultPooledObject.
         * 测试 of(Supplier) 将对象包装在 DefaultPooledObject 中。
         */
        @Test
        @DisplayName("makeObject() 包装为 DefaultPooledObject")
        void testMakeObjectWrapsInDefaultPooledObject() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.of(() -> "hello");

            PooledObject<String> po = factory.makeObject();

            assertThat(po).isInstanceOf(DefaultPooledObject.class);
            assertThat(po.getObject()).isEqualTo("hello");
        }

        /**
         * Tests that each call to makeObject() creates a distinct object.
         * 测试每次调用 makeObject() 创建不同的对象。
         */
        @Test
        @DisplayName("每次 makeObject() 创建新对象")
        void testMakeObjectCreatesDistinctObjects() throws OpenPoolException {
            var counter = new AtomicInteger(0);
            var factory = SimplePooledObjectFactory.of(counter::incrementAndGet);

            PooledObject<Integer> po1 = factory.makeObject();
            PooledObject<Integer> po2 = factory.makeObject();

            assertThat(po1.getObject()).isNotEqualTo(po2.getObject());
        }
    }

    @Nested
    @DisplayName("of(Supplier, Consumer) 工厂方法测试")
    class OfSupplierConsumerTests {

        /**
         * Tests that of(Supplier, Consumer) creates factory with destroyer.
         * 测试 of(Supplier, Consumer) 创建带有销毁器的工厂。
         */
        @Test
        @DisplayName("of(Supplier, Consumer) 创建带销毁器的工厂")
        void testOfSupplierConsumerCreatesFactoryWithDestroyer() throws OpenPoolException {
            var destroyed = new AtomicBoolean(false);
            var factory = SimplePooledObjectFactory.of(
                    StringBuilder::new,
                    sb -> destroyed.set(true)
            );

            PooledObject<StringBuilder> po = factory.makeObject();
            factory.destroyObject(po);

            assertThat(destroyed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("builder() 建造者模式测试")
    class BuilderTests {

        /**
         * Tests builder with all optional callbacks.
         * 测试带所有可选回调的建造者。
         */
        @Test
        @DisplayName("builder() 设置所有回调")
        void testBuilderWithAllCallbacks() throws OpenPoolException {
            var activated = new AtomicBoolean(false);
            var passivated = new AtomicBoolean(false);
            var validated = new AtomicBoolean(false);
            var destroyed = new AtomicBoolean(false);

            var factory = SimplePooledObjectFactory.<StringBuilder>builder(StringBuilder::new)
                    .destroyer(sb -> destroyed.set(true))
                    .validator(sb -> {
                        validated.set(true);
                        return true;
                    })
                    .activator(sb -> activated.set(true))
                    .passivator(sb -> passivated.set(true))
                    .build();

            PooledObject<StringBuilder> po = factory.makeObject();

            factory.activateObject(po);
            assertThat(activated.get()).isTrue();

            factory.passivateObject(po);
            assertThat(passivated.get()).isTrue();

            boolean valid = factory.validateObject(po);
            assertThat(validated.get()).isTrue();
            assertThat(valid).isTrue();

            factory.destroyObject(po);
            assertThat(destroyed.get()).isTrue();
        }

        /**
         * Tests that builder with only creator uses no-op defaults.
         * 测试仅使用创建者的建造者使用默认空操作。
         */
        @Test
        @DisplayName("builder() 仅设置创建者，其他使用默认值")
        void testBuilderWithDefaultsOnly() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.<String>builder(() -> "test").build();

            PooledObject<String> po = factory.makeObject();

            // Default validate returns true
            assertThat(factory.validateObject(po)).isTrue();

            // Default destroy/activate/passivate are no-ops (should not throw)
            assertThatCode(() -> factory.destroyObject(po)).doesNotThrowAnyException();
            assertThatCode(() -> factory.activateObject(po)).doesNotThrowAnyException();
            assertThatCode(() -> factory.passivateObject(po)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("生命周期回调测试 - Lifecycle Callbacks")
    class LifecycleTests {

        /**
         * Tests that destroyObject() calls the destroyer consumer.
         * 测试 destroyObject() 调用销毁器消费者。
         */
        @Test
        @DisplayName("destroyObject() 调用销毁器")
        void testDestroyObjectCallsDestroyer() throws OpenPoolException {
            var ref = new AtomicReference<StringBuilder>();
            var factory = SimplePooledObjectFactory.of(
                    StringBuilder::new,
                    ref::set
            );

            PooledObject<StringBuilder> po = factory.makeObject();
            factory.destroyObject(po);

            assertThat(ref.get()).isSameAs(po.getObject());
        }

        /**
         * Tests that validateObject() calls the validator predicate.
         * 测试 validateObject() 调用验证器谓词。
         */
        @Test
        @DisplayName("validateObject() 调用验证器")
        void testValidateObjectCallsValidator() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.<String>builder(() -> "test")
                    .validator(s -> s.length() > 3)
                    .build();

            PooledObject<String> po = factory.makeObject();

            assertThat(factory.validateObject(po)).isTrue();
        }

        /**
         * Tests that validateObject() returns false for invalid objects.
         * 测试 validateObject() 对无效对象返回 false。
         */
        @Test
        @DisplayName("validateObject() 对无效对象返回 false")
        void testValidateObjectReturnsFalseForInvalid() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.<String>builder(() -> "ab")
                    .validator(s -> s.length() > 3)
                    .build();

            PooledObject<String> po = factory.makeObject();

            assertThat(factory.validateObject(po)).isFalse();
        }

        /**
         * Tests that validator exception returns false rather than propagating.
         * 测试验证器异常返回 false 而非传播异常。
         */
        @Test
        @DisplayName("validateObject() 验证器异常返回 false")
        void testValidateObjectExceptionReturnsFalse() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.<String>builder(() -> "test")
                    .validator(s -> { throw new RuntimeException("boom"); })
                    .build();

            PooledObject<String> po = factory.makeObject();

            assertThat(factory.validateObject(po)).isFalse();
        }

        /**
         * Tests that activateObject() calls the activator consumer.
         * 测试 activateObject() 调用激活器消费者。
         */
        @Test
        @DisplayName("activateObject() 调用激活器")
        void testActivateObjectCallsActivator() throws OpenPoolException {
            var activated = new AtomicBoolean(false);
            var factory = SimplePooledObjectFactory.<String>builder(() -> "test")
                    .activator(s -> activated.set(true))
                    .build();

            PooledObject<String> po = factory.makeObject();
            factory.activateObject(po);

            assertThat(activated.get()).isTrue();
        }

        /**
         * Tests that passivateObject() calls the passivator consumer.
         * 测试 passivateObject() 调用钝化器消费者。
         */
        @Test
        @DisplayName("passivateObject() 调用钝化器")
        void testPassivateObjectCallsPassivator() throws OpenPoolException {
            var passivated = new AtomicBoolean(false);
            var factory = SimplePooledObjectFactory.<String>builder(() -> "test")
                    .passivator(s -> passivated.set(true))
                    .build();

            PooledObject<String> po = factory.makeObject();
            factory.passivateObject(po);

            assertThat(passivated.get()).isTrue();
        }

        /**
         * Tests that makeObject() wraps creator exception in OpenPoolException.
         * 测试 makeObject() 将创建者异常包装为 OpenPoolException。
         */
        @Test
        @DisplayName("makeObject() 创建者异常包装为 OpenPoolException")
        void testMakeObjectWrapsCreatorException() {
            var factory = SimplePooledObjectFactory.<String>of(() -> {
                throw new RuntimeException("creation failed");
            });

            assertThatThrownBy(factory::makeObject)
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("Failed to create")
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        /**
         * Tests that destroyObject() wraps destroyer exception in OpenPoolException.
         * 测试 destroyObject() 将销毁器异常包装为 OpenPoolException。
         */
        @Test
        @DisplayName("destroyObject() 销毁器异常包装为 OpenPoolException")
        void testDestroyObjectWrapsException() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.of(
                    () -> "test",
                    s -> { throw new RuntimeException("destroy failed"); }
            );

            PooledObject<String> po = factory.makeObject();

            assertThatThrownBy(() -> factory.destroyObject(po))
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("Failed to destroy");
        }

        /**
         * Tests that activateObject() wraps activator exception in OpenPoolException.
         * 测试 activateObject() 将激活器异常包装为 OpenPoolException。
         */
        @Test
        @DisplayName("activateObject() 激活器异常包装为 OpenPoolException")
        void testActivateObjectWrapsException() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.<String>builder(() -> "test")
                    .activator(s -> { throw new RuntimeException("activate failed"); })
                    .build();

            PooledObject<String> po = factory.makeObject();

            assertThatThrownBy(() -> factory.activateObject(po))
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("Failed to activate");
        }

        /**
         * Tests that passivateObject() wraps passivator exception in OpenPoolException.
         * 测试 passivateObject() 将钝化器异常包装为 OpenPoolException。
         */
        @Test
        @DisplayName("passivateObject() 钝化器异常包装为 OpenPoolException")
        void testPassivateObjectWrapsException() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.<String>builder(() -> "test")
                    .passivator(s -> { throw new RuntimeException("passivate failed"); })
                    .build();

            PooledObject<String> po = factory.makeObject();

            assertThatThrownBy(() -> factory.passivateObject(po))
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("Failed to passivate");
        }
    }

    @Nested
    @DisplayName("默认行为测试 - Defaults")
    class DefaultTests {

        /**
         * Tests that default validate returns true.
         * 测试默认验证器返回 true。
         */
        @Test
        @DisplayName("默认 validate 返回 true")
        void testDefaultValidateReturnsTrue() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.of(() -> "test");

            PooledObject<String> po = factory.makeObject();

            assertThat(factory.validateObject(po)).isTrue();
        }

        /**
         * Tests that default destroy/activate/passivate are no-ops.
         * 测试默认销毁/激活/钝化为空操作。
         */
        @Test
        @DisplayName("默认 destroy/activate/passivate 为空操作")
        void testDefaultLifecycleAreNoOps() throws OpenPoolException {
            var factory = SimplePooledObjectFactory.of(() -> "test");

            PooledObject<String> po = factory.makeObject();

            assertThatCode(() -> factory.destroyObject(po)).doesNotThrowAnyException();
            assertThatCode(() -> factory.activateObject(po)).doesNotThrowAnyException();
            assertThatCode(() -> factory.passivateObject(po)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("空值校验测试 - Null Checks")
    class NullCheckTests {

        /**
         * Tests that null supplier throws NullPointerException.
         * 测试 null 供应器抛出 NullPointerException。
         */
        @Test
        @DisplayName("of(null) 抛出 NullPointerException")
        void testOfNullSupplierThrows() {
            assertThatThrownBy(() -> SimplePooledObjectFactory.<String>of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("creator");
        }

        /**
         * Tests that null supplier in of(Supplier, Consumer) throws NullPointerException.
         * 测试 of(Supplier, Consumer) 中 null 供应器抛出 NullPointerException。
         */
        @Test
        @DisplayName("of(null, Consumer) 抛出 NullPointerException")
        void testOfNullSupplierWithConsumerThrows() {
            assertThatThrownBy(() -> SimplePooledObjectFactory.<String>of(null, s -> {}))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("creator");
        }

        /**
         * Tests that null destroyer in of(Supplier, Consumer) throws NullPointerException.
         * 测试 of(Supplier, Consumer) 中 null 销毁器抛出 NullPointerException。
         */
        @Test
        @DisplayName("of(Supplier, null) 抛出 NullPointerException")
        void testOfNullDestroyerThrows() {
            assertThatThrownBy(() -> SimplePooledObjectFactory.of(() -> "x", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("destroyer");
        }

        /**
         * Tests that null creator in builder throws NullPointerException.
         * 测试建造者中 null 创建者抛出 NullPointerException。
         */
        @Test
        @DisplayName("builder(null) 抛出 NullPointerException")
        void testBuilderNullCreatorThrows() {
            assertThatThrownBy(() -> SimplePooledObjectFactory.<String>builder(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("creator");
        }

        /**
         * Tests that null callbacks in builder throw NullPointerException.
         * 测试建造者中 null 回调抛出 NullPointerException。
         */
        @Test
        @DisplayName("builder 中 null 回调抛出 NullPointerException")
        void testBuilderNullCallbacksThrow() {
            var builder = SimplePooledObjectFactory.<String>builder(() -> "x");

            assertThatThrownBy(() -> builder.destroyer(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> builder.validator(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> builder.activator(null))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> builder.passivator(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("集成测试 - Integration")
    class IntegrationTests {

        /**
         * Tests integration: create pool with OpenPool.createPool(Supplier).
         * 测试集成: 使用 OpenPool.createPool(Supplier) 创建池。
         */
        @Test
        @DisplayName("OpenPool.createPool(Supplier) 集成")
        void testOpenPoolCreatePoolWithSupplier() throws Exception {
            try (ObjectPool<StringBuilder> pool = OpenPool.createPool(StringBuilder::new)) {
                StringBuilder sb = pool.borrowObject();
                assertThat(sb).isNotNull();
                pool.returnObject(sb);
            }
        }

        /**
         * Tests integration: create pool with OpenPool.createPool(Supplier, Consumer).
         * 测试集成: 使用 OpenPool.createPool(Supplier, Consumer) 创建池。
         */
        @Test
        @DisplayName("OpenPool.createPool(Supplier, Consumer) 集成")
        void testOpenPoolCreatePoolWithSupplierAndDestroyer() throws Exception {
            var destroyed = new AtomicBoolean(false);

            try (ObjectPool<StringBuilder> pool = OpenPool.createPool(
                    StringBuilder::new,
                    sb -> destroyed.set(true))) {
                StringBuilder sb = pool.borrowObject();
                pool.invalidateObject(sb);
            }

            assertThat(destroyed.get()).isTrue();
        }

        /**
         * Tests that SimplePooledObjectFactory works with a real pool borrow/return cycle.
         * 测试 SimplePooledObjectFactory 在真实池借用/归还循环中工作。
         */
        @Test
        @DisplayName("完整借用/归还循环")
        void testFullBorrowReturnCycle() throws Exception {
            var factory = SimplePooledObjectFactory.of(() -> new StringBuilder("pooled"));

            try (ObjectPool<StringBuilder> pool = OpenPool.createPool(factory,
                    PoolConfig.builder().maxTotal(2).minIdle(0).build())) {

                StringBuilder sb1 = pool.borrowObject();
                StringBuilder sb2 = pool.borrowObject();

                assertThat(sb1.toString()).isEqualTo("pooled");
                assertThat(sb2.toString()).isEqualTo("pooled");

                pool.returnObject(sb1);
                pool.returnObject(sb2);

                assertThat(pool.getNumIdle()).isEqualTo(2);
                assertThat(pool.getNumActive()).isZero();
            }
        }
    }
}
