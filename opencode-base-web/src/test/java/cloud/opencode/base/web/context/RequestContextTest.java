package cloud.opencode.base.web.context;

import org.junit.jupiter.api.*;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * RequestContextTest Tests
 * RequestContextTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
@DisplayName("RequestContext Tests")
class RequestContextTest {

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("builder should create request context")
        void builderShouldCreateRequestContext() {
            RequestContext context = RequestContext.builder()
                .traceId("trace-123")
                .requestId("req-456")
                .clientIp("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .locale(Locale.US)
                .build();

            assertThat(context.traceId()).isEqualTo("trace-123");
            assertThat(context.requestId()).isEqualTo("req-456");
            assertThat(context.clientIp()).isEqualTo("192.168.1.1");
            assertThat(context.userAgent()).isEqualTo("Mozilla/5.0");
            assertThat(context.locale()).isEqualTo(Locale.US);
        }

        @Test
        @DisplayName("of should create simple request context")
        void ofShouldCreateSimpleRequestContext() {
            RequestContext context = RequestContext.of("trace-123");

            assertThat(context.traceId()).isEqualTo("trace-123");
        }
    }

    @Nested
    @DisplayName("Compact Constructor Tests")
    class CompactConstructorTests {

        @Test
        @DisplayName("should set default request time")
        void shouldSetDefaultRequestTime() {
            RequestContext context = RequestContext.builder().build();

            assertThat(context.requestTime()).isNotNull();
        }

        @Test
        @DisplayName("should set default locale")
        void shouldSetDefaultLocale() {
            RequestContext context = RequestContext.builder().build();

            assertThat(context.locale()).isNotNull();
        }

        @Test
        @DisplayName("should set default user to anonymous")
        void shouldSetDefaultUserToAnonymous() {
            RequestContext context = RequestContext.builder().build();

            assertThat(context.user()).isEqualTo(UserContext.ANONYMOUS);
        }

        @Test
        @DisplayName("should create immutable attributes map")
        void shouldCreateImmutableAttributesMap() {
            RequestContext context = RequestContext.builder()
                .attribute("key", "value")
                .build();

            assertThatThrownBy(() -> context.attributes().put("new", "value"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("builder should set all fields")
        void builderShouldSetAllFields() {
            Instant now = Instant.now();
            UserContext user = UserContext.of("user1", "testuser");

            RequestContext context = RequestContext.builder()
                .traceId("trace-123")
                .requestId("req-456")
                .requestTime(now)
                .clientIp("192.168.1.1")
                .userAgent("Chrome")
                .locale(Locale.JAPAN)
                .user(user)
                .attribute("key1", "value1")
                .attributes(Map.of("key2", "value2"))
                .build();

            assertThat(context.traceId()).isEqualTo("trace-123");
            assertThat(context.requestId()).isEqualTo("req-456");
            assertThat(context.requestTime()).isEqualTo(now);
            assertThat(context.clientIp()).isEqualTo("192.168.1.1");
            assertThat(context.userAgent()).isEqualTo("Chrome");
            assertThat(context.locale()).isEqualTo(Locale.JAPAN);
            assertThat(context.user()).isEqualTo(user);
            assertThat(context.attributes()).containsEntry("key1", "value1");
            assertThat(context.attributes()).containsEntry("key2", "value2");
        }
    }

    @Nested
    @DisplayName("Attribute Access Tests")
    class AttributeAccessTests {

        @Test
        @DisplayName("getAttribute should return attribute value")
        void getAttributeShouldReturnAttributeValue() {
            RequestContext context = RequestContext.builder()
                .attribute("key", "value")
                .build();

            String value = context.getAttribute("key");

            assertThat(value).isEqualTo("value");
        }

        @Test
        @DisplayName("getAttribute should return null for missing key")
        void getAttributeShouldReturnNullForMissingKey() {
            RequestContext context = RequestContext.builder().build();

            String value = context.getAttribute("missing");

            assertThat(value).isNull();
        }

        @Test
        @DisplayName("getAttribute with default should return default for missing key")
        void getAttributeWithDefaultShouldReturnDefaultForMissingKey() {
            RequestContext context = RequestContext.builder().build();

            String value = context.getAttribute("missing", "default");

            assertThat(value).isEqualTo("default");
        }

        @Test
        @DisplayName("getAttribute with default should return value when present")
        void getAttributeWithDefaultShouldReturnValueWhenPresent() {
            RequestContext context = RequestContext.builder()
                .attribute("key", "value")
                .build();

            String value = context.getAttribute("key", "default");

            assertThat(value).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("User Access Tests")
    class UserAccessTests {

        @Test
        @DisplayName("isAuthenticated should return true for authenticated user")
        void isAuthenticatedShouldReturnTrueForAuthenticatedUser() {
            UserContext user = UserContext.of("user1", "testuser");
            RequestContext context = RequestContext.builder()
                .user(user)
                .build();

            assertThat(context.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("isAuthenticated should return false for anonymous user")
        void isAuthenticatedShouldReturnFalseForAnonymousUser() {
            RequestContext context = RequestContext.builder().build();

            assertThat(context.isAuthenticated()).isFalse();
        }

        @Test
        @DisplayName("getUserId should return user ID")
        void getUserIdShouldReturnUserId() {
            UserContext user = UserContext.of("user1", "testuser");
            RequestContext context = RequestContext.builder()
                .user(user)
                .build();

            assertThat(context.getUserId()).isEqualTo("user1");
        }

        @Test
        @DisplayName("getUsername should return username")
        void getUsernameShouldReturnUsername() {
            UserContext user = UserContext.of("user1", "testuser");
            RequestContext context = RequestContext.builder()
                .user(user)
                .build();

            assertThat(context.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("getUserId should return null for null user")
        void getUserIdShouldReturnNullForNullUser() {
            RequestContext context = new RequestContext(
                "trace", "req", Instant.now(), "ip", "ua", Locale.US, null, Map.of()
            );

            assertThat(context.getUserId()).isNull();
        }
    }
}
