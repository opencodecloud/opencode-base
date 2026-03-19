package cloud.opencode.base.yml.bind;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlNestedPropertyTest Tests
 * YmlNestedPropertyTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlNestedProperty Annotation Tests")
class YmlNestedPropertyTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = YmlNestedProperty.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD only")
        void shouldTargetField() {
            var target = YmlNestedProperty.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlNestedProperty.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @YmlNestedProperty
        private Object defaultField;

        @YmlNestedProperty(prefix = "database")
        private Object customField;

        @Test
        @DisplayName("prefix should default to empty string")
        void prefixShouldDefaultToEmpty() throws NoSuchFieldException {
            YmlNestedProperty annotation = getClass().getDeclaredField("defaultField")
                .getAnnotation(YmlNestedProperty.class);
            assertThat(annotation.prefix()).isEmpty();
        }

        @Test
        @DisplayName("should support custom prefix")
        void shouldSupportCustomPrefix() throws NoSuchFieldException {
            YmlNestedProperty annotation = getClass().getDeclaredField("customField")
                .getAnnotation(YmlNestedProperty.class);
            assertThat(annotation.prefix()).isEqualTo("database");
        }
    }
}
