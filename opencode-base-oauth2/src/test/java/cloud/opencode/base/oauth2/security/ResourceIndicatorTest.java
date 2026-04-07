package cloud.opencode.base.oauth2.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ResourceIndicator Tests
 * ResourceIndicator 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
@DisplayName("ResourceIndicator 测试")
class ResourceIndicatorTest {

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("有效URI正常构造")
        void testValidUri() {
            ResourceIndicator indicator = new ResourceIndicator("https://api.example.com");

            assertThat(indicator.resource()).isEqualTo("https://api.example.com");
        }

        @Test
        @DisplayName("带路径的URI")
        void testUriWithPath() {
            ResourceIndicator indicator = new ResourceIndicator("https://api.example.com/v1/resource");

            assertThat(indicator.resource()).isEqualTo("https://api.example.com/v1/resource");
        }

        @Test
        @DisplayName("null resource抛出NullPointerException")
        void testNullResource() {
            assertThatThrownBy(() -> new ResourceIndicator(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("resource");
        }

        @Test
        @DisplayName("空白resource抛出IllegalArgumentException")
        void testBlankResource() {
            assertThatThrownBy(() -> new ResourceIndicator("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }

        @Test
        @DisplayName("空字符串resource抛出IllegalArgumentException")
        void testEmptyResource() {
            assertThatThrownBy(() -> new ResourceIndicator(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }

        @Test
        @DisplayName("无效URI抛出IllegalArgumentException")
        void testInvalidUri() {
            assertThatThrownBy(() -> new ResourceIndicator("not a valid uri with spaces[and]brackets"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("valid URI");
        }

        @Test
        @DisplayName("相对URI抛出IllegalArgumentException (RFC 8707)")
        void testRelativeUriRejected() {
            assertThatThrownBy(() -> new ResourceIndicator("/api/resource"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("absolute URI");
        }
    }

    @Nested
    @DisplayName("of工厂方法测试")
    class OfTests {

        @Test
        @DisplayName("of创建正确的实例")
        void testOf() {
            ResourceIndicator indicator = ResourceIndicator.of("https://api.example.com");

            assertThat(indicator.resource()).isEqualTo("https://api.example.com");
        }

        @Test
        @DisplayName("of与构造器等价")
        void testOfEquivalentToConstructor() {
            ResourceIndicator fromOf = ResourceIndicator.of("https://api.example.com");
            ResourceIndicator fromConstructor = new ResourceIndicator("https://api.example.com");

            assertThat(fromOf).isEqualTo(fromConstructor);
        }

        @Test
        @DisplayName("of null参数抛出NullPointerException")
        void testOfNull() {
            assertThatThrownBy(() -> ResourceIndicator.of(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("toParam方法测试")
    class ToParamTests {

        @Test
        @DisplayName("toParam返回resource字符串")
        void testToParam() {
            ResourceIndicator indicator = ResourceIndicator.of("https://api.example.com/v2");

            assertThat(indicator.toParam()).isEqualTo("https://api.example.com/v2");
        }

        @Test
        @DisplayName("toParam与resource()相同")
        void testToParamSameAsResource() {
            ResourceIndicator indicator = ResourceIndicator.of("https://api.example.com");

            assertThat(indicator.toParam()).isEqualTo(indicator.resource());
        }
    }

    @Nested
    @DisplayName("Record方法测试")
    class RecordMethodsTests {

        @Test
        @DisplayName("equals和hashCode")
        void testEqualsAndHashCode() {
            ResourceIndicator r1 = ResourceIndicator.of("https://api.example.com");
            ResourceIndicator r2 = ResourceIndicator.of("https://api.example.com");

            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("不同资源不相等")
        void testNotEqual() {
            ResourceIndicator r1 = ResourceIndicator.of("https://api1.example.com");
            ResourceIndicator r2 = ResourceIndicator.of("https://api2.example.com");

            assertThat(r1).isNotEqualTo(r2);
        }

        @Test
        @DisplayName("toString包含资源URI")
        void testToString() {
            ResourceIndicator indicator = ResourceIndicator.of("https://api.example.com");

            assertThat(indicator.toString()).contains("https://api.example.com");
        }
    }

    @Nested
    @DisplayName("各种URI格式测试")
    class VariousUriFormatsTests {

        @Test
        @DisplayName("HTTP URI")
        void testHttpUri() {
            ResourceIndicator indicator = ResourceIndicator.of("http://api.example.com");
            assertThat(indicator.resource()).isEqualTo("http://api.example.com");
        }

        @Test
        @DisplayName("带端口的URI")
        void testUriWithPort() {
            ResourceIndicator indicator = ResourceIndicator.of("https://api.example.com:8443");
            assertThat(indicator.resource()).isEqualTo("https://api.example.com:8443");
        }

        @Test
        @DisplayName("带查询参数的URI")
        void testUriWithQuery() {
            ResourceIndicator indicator = ResourceIndicator.of("https://api.example.com?version=2");
            assertThat(indicator.resource()).isEqualTo("https://api.example.com?version=2");
        }

        @Test
        @DisplayName("URN格式")
        void testUrnFormat() {
            ResourceIndicator indicator = ResourceIndicator.of("urn:example:resource:123");
            assertThat(indicator.resource()).isEqualTo("urn:example:resource:123");
        }
    }
}
