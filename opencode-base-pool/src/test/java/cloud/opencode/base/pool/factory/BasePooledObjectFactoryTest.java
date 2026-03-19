package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.exception.OpenPoolException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * BasePooledObjectFactoryTest Tests
 * BasePooledObjectFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("BasePooledObjectFactory 测试")
class BasePooledObjectFactoryTest {

    private BasePooledObjectFactory<String> factory;

    @BeforeEach
    void setUp() {
        factory = new BasePooledObjectFactory<>() {
            @Override
            protected String create() {
                return "test-object";
            }
        };
    }

    @Nested
    @DisplayName("makeObject方法测试")
    class MakeObjectTests {

        @Test
        @DisplayName("makeObject创建并包装对象")
        void testMakeObject() throws OpenPoolException {
            PooledObject<String> pooled = factory.makeObject();

            assertThat(pooled).isNotNull();
            assertThat(pooled.getObject()).isEqualTo("test-object");
            assertThat(pooled).isInstanceOf(DefaultPooledObject.class);
        }

        @Test
        @DisplayName("makeObject每次创建新对象")
        void testMakeObjectCreatesNewObjects() throws OpenPoolException {
            PooledObject<String> pooled1 = factory.makeObject();
            PooledObject<String> pooled2 = factory.makeObject();

            assertThat(pooled1).isNotSameAs(pooled2);
        }
    }

    @Nested
    @DisplayName("wrap方法测试")
    class WrapTests {

        @Test
        @DisplayName("wrap包装对象为DefaultPooledObject")
        void testWrap() throws OpenPoolException {
            // wrap is called internally by makeObject,
            // verify that the returned object is a DefaultPooledObject
            PooledObject<String> pooled = factory.makeObject();

            assertThat(pooled).isInstanceOf(DefaultPooledObject.class);
            assertThat(pooled.getObject()).isEqualTo("test-object");
        }
    }

    @Nested
    @DisplayName("默认生命周期方法测试")
    class DefaultLifecycleMethodTests {

        @Test
        @DisplayName("destroyObject默认为空操作")
        void testDestroyObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            // Should not throw
            assertThatCode(() -> factory.destroyObject(pooled)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("validateObject默认返回true")
        void testValidateObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            boolean result = factory.validateObject(pooled);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("activateObject默认为空操作")
        void testActivateObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            // Should not throw
            assertThatCode(() -> factory.activateObject(pooled)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("passivateObject默认为空操作")
        void testPassivateObject() {
            PooledObject<String> pooled = new DefaultPooledObject<>("test");

            // Should not throw
            assertThatCode(() -> factory.passivateObject(pooled)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("自定义生命周期方法测试")
    class CustomLifecycleMethodTests {

        @Test
        @DisplayName("可以覆盖validateObject")
        void testOverrideValidateObject() {
            BasePooledObjectFactory<String> customFactory = new BasePooledObjectFactory<>() {
                @Override
                protected String create() {
                    return "test";
                }

                @Override
                public boolean validateObject(PooledObject<String> obj) {
                    return obj.getObject().startsWith("valid");
                }
            };

            PooledObject<String> valid = new DefaultPooledObject<>("valid-object");
            PooledObject<String> invalid = new DefaultPooledObject<>("invalid");

            assertThat(customFactory.validateObject(valid)).isTrue();
            assertThat(customFactory.validateObject(invalid)).isFalse();
        }

        @Test
        @DisplayName("可以覆盖passivateObject")
        void testOverridePassivateObject() throws OpenPoolException {
            StringBuilder tracker = new StringBuilder();

            BasePooledObjectFactory<String> customFactory = new BasePooledObjectFactory<>() {
                @Override
                protected String create() {
                    return "test";
                }

                @Override
                public void passivateObject(PooledObject<String> obj) {
                    tracker.append("passivated");
                }
            };

            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            customFactory.passivateObject(pooled);

            assertThat(tracker.toString()).isEqualTo("passivated");
        }
    }
}
