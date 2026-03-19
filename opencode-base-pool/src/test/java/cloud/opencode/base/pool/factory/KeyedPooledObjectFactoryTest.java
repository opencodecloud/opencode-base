package cloud.opencode.base.pool.factory;

import cloud.opencode.base.pool.PooledObject;
import cloud.opencode.base.pool.exception.OpenPoolException;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * KeyedPooledObjectFactoryTest Tests
 * KeyedPooledObjectFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("KeyedPooledObjectFactory Tests")
class KeyedPooledObjectFactoryTest {

    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {

        @Test
        @DisplayName("should be an interface")
        void shouldBeAnInterface() {
            assertThat(KeyedPooledObjectFactory.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("should declare makeObject method")
        void shouldDeclareMakeObject() throws NoSuchMethodException {
            assertThat(KeyedPooledObjectFactory.class.getMethod("makeObject", Object.class))
                .isNotNull();
        }

        @Test
        @DisplayName("should declare destroyObject method")
        void shouldDeclareDestroyObject() throws NoSuchMethodException {
            assertThat(KeyedPooledObjectFactory.class.getMethod("destroyObject", Object.class, PooledObject.class))
                .isNotNull();
        }

        @Test
        @DisplayName("should declare validateObject method")
        void shouldDeclareValidateObject() throws NoSuchMethodException {
            assertThat(KeyedPooledObjectFactory.class.getMethod("validateObject", Object.class, PooledObject.class))
                .isNotNull();
        }

        @Test
        @DisplayName("should declare activateObject method")
        void shouldDeclareActivateObject() throws NoSuchMethodException {
            assertThat(KeyedPooledObjectFactory.class.getMethod("activateObject", Object.class, PooledObject.class))
                .isNotNull();
        }

        @Test
        @DisplayName("should declare passivateObject method")
        void shouldDeclarePassivateObject() throws NoSuchMethodException {
            assertThat(KeyedPooledObjectFactory.class.getMethod("passivateObject", Object.class, PooledObject.class))
                .isNotNull();
        }
    }

    @Nested
    @DisplayName("Mock Implementation Tests")
    class MockImplementationTests {

        @Test
        @DisplayName("should allow implementation with custom key and value types")
        void shouldAllowCustomImplementation() {
            KeyedPooledObjectFactory<String, StringBuilder> factory = new KeyedPooledObjectFactory<>() {
                @Override
                public PooledObject<StringBuilder> makeObject(String key) throws OpenPoolException {
                    return null;
                }

                @Override
                public void destroyObject(String key, PooledObject<StringBuilder> obj) throws OpenPoolException {
                }

                @Override
                public boolean validateObject(String key, PooledObject<StringBuilder> obj) {
                    return true;
                }

                @Override
                public void activateObject(String key, PooledObject<StringBuilder> obj) throws OpenPoolException {
                }

                @Override
                public void passivateObject(String key, PooledObject<StringBuilder> obj) throws OpenPoolException {
                }
            };

            assertThat(factory).isNotNull();
            assertThat(factory.validateObject("test", null)).isTrue();
        }

        @Test
        @DisplayName("makeObject should accept key parameter")
        void makeObjectShouldAcceptKey() {
            KeyedPooledObjectFactory<Integer, String> factory = new KeyedPooledObjectFactory<>() {
                @Override
                public PooledObject<String> makeObject(Integer key) throws OpenPoolException {
                    return null;
                }

                @Override
                public void destroyObject(Integer key, PooledObject<String> obj) {}

                @Override
                public boolean validateObject(Integer key, PooledObject<String> obj) {
                    return key != null && key > 0;
                }

                @Override
                public void activateObject(Integer key, PooledObject<String> obj) {}

                @Override
                public void passivateObject(Integer key, PooledObject<String> obj) {}
            };

            assertThat(factory.validateObject(1, null)).isTrue();
            assertThat(factory.validateObject(-1, null)).isFalse();
        }
    }
}
