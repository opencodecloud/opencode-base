package cloud.opencode.base.feature;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * FeatureContext 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("FeatureContext 测试")
class FeatureContextTest {

    @Nested
    @DisplayName("empty() 测试")
    class EmptyTests {

        @Test
        @DisplayName("创建空上下文")
        void testEmpty() {
            FeatureContext context = FeatureContext.empty();

            assertThat(context.userId()).isNull();
            assertThat(context.tenantId()).isNull();
            assertThat(context.attributes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ofUser() 测试")
    class OfUserTests {

        @Test
        @DisplayName("创建用户上下文")
        void testOfUser() {
            FeatureContext context = FeatureContext.ofUser("user123");

            assertThat(context.userId()).isEqualTo("user123");
            assertThat(context.tenantId()).isNull();
            assertThat(context.attributes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("ofTenant() 测试")
    class OfTenantTests {

        @Test
        @DisplayName("创建租户上下文")
        void testOfTenant() {
            FeatureContext context = FeatureContext.ofTenant("tenant456");

            assertThat(context.userId()).isNull();
            assertThat(context.tenantId()).isEqualTo("tenant456");
            assertThat(context.attributes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("of() 测试")
    class OfTests {

        @Test
        @DisplayName("创建用户和租户上下文")
        void testOf() {
            FeatureContext context = FeatureContext.of("user123", "tenant456");

            assertThat(context.userId()).isEqualTo("user123");
            assertThat(context.tenantId()).isEqualTo("tenant456");
            assertThat(context.attributes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("builder() 测试")
    class BuilderTests {

        @Test
        @DisplayName("构建完整上下文")
        void testBuilderFull() {
            FeatureContext context = FeatureContext.builder()
                    .userId("user123")
                    .tenantId("tenant456")
                    .attribute("role", "admin")
                    .attribute("plan", "premium")
                    .build();

            assertThat(context.userId()).isEqualTo("user123");
            assertThat(context.tenantId()).isEqualTo("tenant456");
            assertThat(context.attributes())
                    .containsEntry("role", "admin")
                    .containsEntry("plan", "premium");
        }

        @Test
        @DisplayName("从Map添加属性")
        void testBuilderWithAttributesMap() {
            FeatureContext context = FeatureContext.builder()
                    .attributes(Map.of("k1", "v1", "k2", "v2"))
                    .build();

            assertThat(context.attributes()).hasSize(2);
        }

        @Test
        @DisplayName("null属性Map不抛异常")
        void testBuilderWithNullAttributesMap() {
            FeatureContext context = FeatureContext.builder()
                    .attributes(null)
                    .build();

            assertThat(context.attributes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAttribute() 测试")
    class GetAttributeTests {

        @Test
        @DisplayName("获取存在的属性")
        void testGetExistingAttribute() {
            FeatureContext context = FeatureContext.builder()
                    .attribute("role", "admin")
                    .build();

            String role = context.getAttribute("role");
            assertThat(role).isEqualTo("admin");
        }

        @Test
        @DisplayName("获取不存在的属性返回null")
        void testGetNonExistingAttribute() {
            FeatureContext context = FeatureContext.empty();

            String value = context.getAttribute("nonexistent");
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("获取属性带默认值")
        void testGetAttributeWithDefault() {
            FeatureContext context = FeatureContext.empty();

            String value = context.getAttribute("key", "default");
            assertThat(value).isEqualTo("default");
        }

        @Test
        @DisplayName("存在值时不使用默认值")
        void testGetAttributeExistsNoDefault() {
            FeatureContext context = FeatureContext.builder()
                    .attribute("key", "actual")
                    .build();

            String value = context.getAttribute("key", "default");
            assertThat(value).isEqualTo("actual");
        }
    }

    @Nested
    @DisplayName("hasUserId() 测试")
    class HasUserIdTests {

        @Test
        @DisplayName("有userId返回true")
        void testHasUserId() {
            FeatureContext context = FeatureContext.ofUser("user123");

            assertThat(context.hasUserId()).isTrue();
        }

        @Test
        @DisplayName("无userId返回false")
        void testHasNoUserId() {
            FeatureContext context = FeatureContext.empty();

            assertThat(context.hasUserId()).isFalse();
        }

        @Test
        @DisplayName("空userId返回false")
        void testHasEmptyUserId() {
            FeatureContext context = FeatureContext.ofUser("");

            assertThat(context.hasUserId()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasTenantId() 测试")
    class HasTenantIdTests {

        @Test
        @DisplayName("有tenantId返回true")
        void testHasTenantId() {
            FeatureContext context = FeatureContext.ofTenant("tenant123");

            assertThat(context.hasTenantId()).isTrue();
        }

        @Test
        @DisplayName("无tenantId返回false")
        void testHasNoTenantId() {
            FeatureContext context = FeatureContext.empty();

            assertThat(context.hasTenantId()).isFalse();
        }

        @Test
        @DisplayName("空tenantId返回false")
        void testHasEmptyTenantId() {
            FeatureContext context = FeatureContext.ofTenant("");

            assertThat(context.hasTenantId()).isFalse();
        }
    }

    @Nested
    @DisplayName("Record组件测试")
    class RecordComponentTests {

        @Test
        @DisplayName("所有record组件可访问")
        void testRecordComponents() {
            FeatureContext context = new FeatureContext(
                    "user", "tenant", Map.of("key", "value")
            );

            assertThat(context.userId()).isEqualTo("user");
            assertThat(context.tenantId()).isEqualTo("tenant");
            assertThat(context.attributes()).containsEntry("key", "value");
        }
    }
}
