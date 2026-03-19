package cloud.opencode.base.yml.bind;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlAnnotationsTest Tests
 * YmlAnnotationsTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YML 绑定注解测试")
class YmlAnnotationsTest {

    @Nested
    @DisplayName("YmlProperty注解测试")
    class YmlPropertyTests {

        static class TestClass {
            @YmlProperty("server.port")
            int port;

            @YmlProperty(value = "server.host", defaultValue = "localhost", required = true)
            String host;
        }

        @Test
        @DisplayName("value返回属性路径")
        void testValue() throws NoSuchFieldException {
            YmlProperty annotation = TestClass.class.getDeclaredField("port").getAnnotation(YmlProperty.class);
            assertThat(annotation.value()).isEqualTo("server.port");
        }

        @Test
        @DisplayName("defaultValue和required返回正确值")
        void testDefaultValueAndRequired() throws NoSuchFieldException {
            YmlProperty annotation = TestClass.class.getDeclaredField("host").getAnnotation(YmlProperty.class);
            assertThat(annotation.defaultValue()).isEqualTo("localhost");
            assertThat(annotation.required()).isTrue();
        }

        @Test
        @DisplayName("默认值正确")
        void testDefaults() throws NoSuchFieldException {
            YmlProperty annotation = TestClass.class.getDeclaredField("port").getAnnotation(YmlProperty.class);
            assertThat(annotation.defaultValue()).isEmpty();
            assertThat(annotation.required()).isFalse();
        }
    }

    @Nested
    @DisplayName("YmlIgnore注解测试")
    class YmlIgnoreTests {

        static class TestClass {
            @YmlIgnore
            String password;
            String name;
        }

        @Test
        @DisplayName("注解存在于标记的字段上")
        void testAnnotationPresent() throws NoSuchFieldException {
            assertThat(TestClass.class.getDeclaredField("password").isAnnotationPresent(YmlIgnore.class)).isTrue();
            assertThat(TestClass.class.getDeclaredField("name").isAnnotationPresent(YmlIgnore.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("YmlAlias注解测试")
    class YmlAliasTests {

        static class TestClass {
            @YmlAlias({"db.url", "database.url", "jdbc.url"})
            String url;
        }

        @Test
        @DisplayName("value返回所有别名路径")
        void testValue() throws NoSuchFieldException {
            YmlAlias annotation = TestClass.class.getDeclaredField("url").getAnnotation(YmlAlias.class);
            assertThat(annotation.value()).containsExactly("db.url", "database.url", "jdbc.url");
        }
    }

    @Nested
    @DisplayName("YmlValue注解测试")
    class YmlValueTests {

        static class TestClass {
            @YmlValue("server.port")
            int port;

            @YmlValue(value = "server.host", defaultValue = "localhost")
            String host;
        }

        @Test
        @DisplayName("value返回YAML属性路径")
        void testValue() throws NoSuchFieldException {
            YmlValue annotation = TestClass.class.getDeclaredField("port").getAnnotation(YmlValue.class);
            assertThat(annotation.value()).isEqualTo("server.port");
        }

        @Test
        @DisplayName("defaultValue返回正确值")
        void testDefaultValue() throws NoSuchFieldException {
            YmlValue annotation = TestClass.class.getDeclaredField("host").getAnnotation(YmlValue.class);
            assertThat(annotation.defaultValue()).isEqualTo("localhost");
        }
    }

    @Nested
    @DisplayName("YmlNestedProperty注解测试")
    class YmlNestedPropertyTests {

        static class TestClass {
            @YmlNestedProperty(prefix = "database")
            Object database;

            @YmlNestedProperty
            Object cache;
        }

        @Test
        @DisplayName("prefix返回前缀路径")
        void testPrefix() throws NoSuchFieldException {
            YmlNestedProperty annotation = TestClass.class.getDeclaredField("database").getAnnotation(YmlNestedProperty.class);
            assertThat(annotation.prefix()).isEqualTo("database");
        }

        @Test
        @DisplayName("prefix默认值为空字符串")
        void testDefaultPrefix() throws NoSuchFieldException {
            YmlNestedProperty annotation = TestClass.class.getDeclaredField("cache").getAnnotation(YmlNestedProperty.class);
            assertThat(annotation.prefix()).isEmpty();
        }
    }
}
