package cloud.opencode.base.config.bind;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * NestedConfigTest Tests
 * NestedConfigTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-config V1.0.0
 */
@DisplayName("NestedConfig Annotation Tests")
class NestedConfigTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = NestedConfig.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD only")
        void shouldTargetField() {
            var target = NestedConfig.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @NestedConfig
        private Object defaultField;

        @NestedConfig(prefix = "db")
        private Object customPrefixField;

        @Test
        @DisplayName("prefix should default to empty string")
        void prefixShouldDefaultToEmpty() throws NoSuchFieldException {
            NestedConfig annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(NestedConfig.class);
            assertThat(annotation.prefix()).isEmpty();
        }

        @Test
        @DisplayName("should support custom prefix")
        void shouldSupportCustomPrefix() throws NoSuchFieldException {
            NestedConfig annotation = getClass().getDeclaredField("customPrefixField")
                .getAnnotation(NestedConfig.class);
            assertThat(annotation.prefix()).isEqualTo("db");
        }
    }
}
