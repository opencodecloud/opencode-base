package cloud.opencode.base.log;

import cloud.opencode.base.log.spi.LogProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * LoggerFactory 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("LoggerFactory 测试")
class LoggerFactoryTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(LoggerFactory.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = LoggerFactory.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getLogger(Class)方法测试")
    class GetLoggerByClassTests {

        @Test
        @DisplayName("获取Logger实例")
        void testGetLoggerByClass() {
            Logger logger = LoggerFactory.getLogger(LoggerFactoryTest.class);

            assertThat(logger).isNotNull();
            assertThat(logger.getName()).contains("LoggerFactoryTest");
        }

        @Test
        @DisplayName("多次调用返回一致的Logger")
        void testGetLoggerConsistent() {
            Logger logger1 = LoggerFactory.getLogger(LoggerFactoryTest.class);
            Logger logger2 = LoggerFactory.getLogger(LoggerFactoryTest.class);

            assertThat(logger1.getName()).isEqualTo(logger2.getName());
        }
    }

    @Nested
    @DisplayName("getLogger(String)方法测试")
    class GetLoggerByNameTests {

        @Test
        @DisplayName("获取Logger实例")
        void testGetLoggerByName() {
            Logger logger = LoggerFactory.getLogger("com.example.MyService");

            assertThat(logger).isNotNull();
            assertThat(logger.getName()).isEqualTo("com.example.MyService");
        }

        @Test
        @DisplayName("使用简单名称")
        void testGetLoggerSimpleName() {
            Logger logger = LoggerFactory.getLogger("MyLogger");

            assertThat(logger).isNotNull();
            assertThat(logger.getName()).isEqualTo("MyLogger");
        }
    }

    @Nested
    @DisplayName("getLogger()无参方法测试")
    class GetLoggerNoArgsTests {

        @Test
        @DisplayName("自动检测调用类")
        void testGetLoggerAutoDetect() {
            Logger logger = LoggerFactory.getLogger();

            assertThat(logger).isNotNull();
            assertThat(logger.getName()).contains("LoggerFactoryTest");
        }
    }

    @Nested
    @DisplayName("getProvider方法测试")
    class GetProviderTests {

        @Test
        @DisplayName("获取Provider实例")
        void testGetProvider() {
            LogProvider provider = LoggerFactory.getProvider();

            assertThat(provider).isNotNull();
        }
    }

    @Nested
    @DisplayName("静态方法定义测试")
    class StaticMethodDefinitionTests {

        @Test
        @DisplayName("getLogger(Class)是静态方法")
        void testGetLoggerClassIsStatic() throws NoSuchMethodException {
            var method = LoggerFactory.class.getMethod("getLogger", Class.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("getLogger(String)是静态方法")
        void testGetLoggerStringIsStatic() throws NoSuchMethodException {
            var method = LoggerFactory.class.getMethod("getLogger", String.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("getLogger()是静态方法")
        void testGetLoggerNoArgsIsStatic() throws NoSuchMethodException {
            var method = LoggerFactory.class.getMethod("getLogger");
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("getProvider是静态方法")
        void testGetProviderIsStatic() throws NoSuchMethodException {
            var method = LoggerFactory.class.getMethod("getProvider");
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("setProvider是静态方法")
        void testSetProviderIsStatic() throws NoSuchMethodException {
            var method = LoggerFactory.class.getMethod("setProvider", LogProvider.class);
            assertThat(java.lang.reflect.Modifier.isStatic(method.getModifiers())).isTrue();
        }
    }
}
