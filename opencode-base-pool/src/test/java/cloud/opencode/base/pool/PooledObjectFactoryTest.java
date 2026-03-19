package cloud.opencode.base.pool;

import cloud.opencode.base.pool.exception.OpenPoolException;
import cloud.opencode.base.pool.factory.DefaultPooledObject;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PooledObjectFactoryTest Tests
 * PooledObjectFactoryTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pool V1.0.0
 */
@DisplayName("PooledObjectFactory 接口测试")
class PooledObjectFactoryTest {

    @Nested
    @DisplayName("接口实现测试")
    class ImplementationTests {

        @Test
        @DisplayName("makeObject创建新对象")
        void testMakeObject() {
            PooledObjectFactory<String> factory = createFactory();
            PooledObject<String> pooled = factory.makeObject();
            assertThat(pooled).isNotNull();
            assertThat(pooled.getObject()).isEqualTo("new-object");
        }

        @Test
        @DisplayName("validateObject验证对象")
        void testValidateObject() {
            PooledObjectFactory<String> factory = createFactory();
            PooledObject<String> pooled = new DefaultPooledObject<>("valid");
            assertThat(factory.validateObject(pooled)).isTrue();
        }

        @Test
        @DisplayName("destroyObject销毁对象")
        void testDestroyObject() {
            List<String> destroyed = new ArrayList<>();
            PooledObjectFactory<String> factory = new PooledObjectFactory<>() {
                @Override
                public PooledObject<String> makeObject() {
                    return new DefaultPooledObject<>("obj");
                }

                @Override
                public void destroyObject(PooledObject<String> obj) {
                    destroyed.add(obj.getObject());
                }

                @Override
                public boolean validateObject(PooledObject<String> obj) { return true; }

                @Override
                public void activateObject(PooledObject<String> obj) {}

                @Override
                public void passivateObject(PooledObject<String> obj) {}
            };

            PooledObject<String> pooled = factory.makeObject();
            factory.destroyObject(pooled);
            assertThat(destroyed).containsExactly("obj");
        }

        @Test
        @DisplayName("activateObject不抛出异常")
        void testActivateObject() {
            PooledObjectFactory<String> factory = createFactory();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            assertThatNoException().isThrownBy(() -> factory.activateObject(pooled));
        }

        @Test
        @DisplayName("passivateObject不抛出异常")
        void testPassivateObject() {
            PooledObjectFactory<String> factory = createFactory();
            PooledObject<String> pooled = new DefaultPooledObject<>("test");
            assertThatNoException().isThrownBy(() -> factory.passivateObject(pooled));
        }
    }

    private PooledObjectFactory<String> createFactory() {
        return new PooledObjectFactory<>() {
            @Override
            public PooledObject<String> makeObject() throws OpenPoolException {
                return new DefaultPooledObject<>("new-object");
            }

            @Override
            public void destroyObject(PooledObject<String> obj) {}

            @Override
            public boolean validateObject(PooledObject<String> obj) { return true; }

            @Override
            public void activateObject(PooledObject<String> obj) {}

            @Override
            public void passivateObject(PooledObject<String> obj) {}
        };
    }
}
