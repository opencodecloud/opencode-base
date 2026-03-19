package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.exception.OpenPoolException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BaseKeyedPooledObjectFactoryTest Tests
 * BaseKeyedPooledObjectFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("BaseKeyedPooledObjectFactory 测试")
class BaseKeyedPooledObjectFactoryTest {

    private BaseKeyedPooledObjectFactory<String, String> factory;

    @BeforeEach
    void setUp() {
        factory = new BaseKeyedPooledObjectFactory<>() {
            @Override
            protected String create(String key) {
                return "value-for-" + key;
            }
        };
    }

    @Nested
    @DisplayName("makeObject方法测试")
    class MakeObjectTests {

        @Test
        @DisplayName("makeObject创建并包装对象")
        void testMakeObject() throws OpenPoolException {
            PooledObject<String> pooled = factory.makeObject("key1");

            assertThat(pooled).isNotNull();
            assertThat(pooled.getObject()).isEqualTo("value-for-key1");
            assertThat(pooled).isInstanceOf(DefaultPooledObject.class);
        }

        @Test
        @DisplayName("makeObject使用不同键创建不同对象")
        void testMakeObjectDifferentKeys() throws OpenPoolException {
            PooledObject<String> pooled1 = factory.makeObject("key1");
            PooledObject<String> pooled2 = factory.makeObject("key2");

            assertThat(pooled1.getObject()).isEqualTo("value-for-key1");
            assertThat(pooled2.getObject()).isEqualTo("value-for-key2");
        }
    }

    @Nested
    @DisplayName("wrap方法测试")
    class WrapTests {

        @Test
        @DisplayName("wrap包装对象为DefaultPooledObject")
        void testWrap() throws OpenPoolException {
            // makeObject内部调用wrap
            PooledObject<String> pooled = factory.makeObject("test");

            assertThat(pooled).isInstanceOf(DefaultPooledObject.class);
            assertThat(pooled.getObject()).isEqualTo("value-for-test");
        }
    }

    @Nested
    @DisplayName("默认生命周期方法测试")
    class DefaultLifecycleMethodTests {

        @Test
        @DisplayName("destroyObject默认为空操作")
        void testDestroyObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            assertThatCode(() -> factory.destroyObject("key", pooled))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateObject默认返回true")
        void testValidateObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            boolean result = factory.validateObject("key", pooled);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("activateObject默认为空操作")
        void testActivateObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            assertThatCode(() -> factory.activateObject("key", pooled))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("passivateObject默认为空操作")
        void testPassivateObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            assertThatCode(() -> factory.passivateObject("key", pooled))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("自定义生命周期方法测试")
    class CustomLifecycleMethodTests {

        @Test
        @DisplayName("可以覆盖validateObject")
        void testOverrideValidateObject() {
            BaseKeyedPooledObjectFactory<String, String> customFactory = new BaseKeyedPooledObjectFactory<>() {
                @Override
                protected String create(String key) {
                    return key;
                }

                @Override
                public boolean validateObject(String key, PooledObject<String> obj) {
                    return key.equals(obj.getObject());
                }
            };

            PooledObject<String> matchingPooled = new DefaultPooledObject<>("key1");
            PooledObject<String> nonMatchingPooled = new DefaultPooledObject<>("key2");

            assertThat(customFactory.validateObject("key1", matchingPooled)).isTrue();
            assertThat(customFactory.validateObject("key1", nonMatchingPooled)).isFalse();
        }

        @Test
        @DisplayName("可以覆盖destroyObject")
        void testOverrideDestroyObject() throws OpenPoolException {
            StringBuilder tracker = new StringBuilder();

            BaseKeyedPooledObjectFactory<String, String> customFactory = new BaseKeyedPooledObjectFactory<>() {
                @Override
                protected String create(String key) {
                    return key;
                }

                @Override
                public void destroyObject(String key, PooledObject<String> obj) {
                    tracker.append("destroyed:").append(key);
                }
            };

            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            customFactory.destroyObject("mykey", pooled);

            assertThat(tracker.toString()).isEqualTo("destroyed:mykey");
        }

        @Test
        @DisplayName("可以覆盖activateObject")
        void testOverrideActivateObject() throws OpenPoolException {
            StringBuilder tracker = new StringBuilder();

            BaseKeyedPooledObjectFactory<String, String> customFactory = new BaseKeyedPooledObjectFactory<>() {
                @Override
                protected String create(String key) {
                    return key;
                }

                @Override
                public void activateObject(String key, PooledObject<String> obj) {
                    tracker.append("activated:").append(key);
                }
            };

            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            customFactory.activateObject("mykey", pooled);

            assertThat(tracker.toString()).isEqualTo("activated:mykey");
        }

        @Test
        @DisplayName("可以覆盖passivateObject")
        void testOverridePassivateObject() throws OpenPoolException {
            StringBuilder tracker = new StringBuilder();

            BaseKeyedPooledObjectFactory<String, String> customFactory = new BaseKeyedPooledObjectFactory<>() {
                @Override
                protected String create(String key) {
                    return key;
                }

                @Override
                public void passivateObject(String key, PooledObject<String> obj) {
                    tracker.append("passivated:").append(key);
                }
            };

            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            customFactory.passivateObject("mykey", pooled);

            assertThat(tracker.toString()).isEqualTo("passivated:mykey");
        }
    }

    @Nested
    @DisplayName("create方法抛出异常测试")
    class CreateExceptionTests {

        @Test
        @DisplayName("create抛出异常时makeObject传播异常")
        void testCreateThrowsException() {
            BaseKeyedPooledObjectFactory<String, String> throwingFactory = new BaseKeyedPooledObjectFactory<>() {
                @Override
                protected String create(String key) throws OpenPoolException {
                    throw new OpenPoolException("Creation failed for " + key);
                }
            };

            assertThatThrownBy(() -> throwingFactory.makeObject("key"))
                    .isInstanceOf(OpenPoolException.class)
                    .hasMessageContaining("Creation failed for key");
        }
    }
}
