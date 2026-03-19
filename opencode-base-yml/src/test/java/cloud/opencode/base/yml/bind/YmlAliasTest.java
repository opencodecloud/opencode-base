package cloud.opencode.base.yml.bind;

import org.junit.jupiter.api.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * YmlAliasTest Tests
 * YmlAliasTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
@DisplayName("YmlAlias Annotation Tests")
class YmlAliasTest {

    @Nested
    @DisplayName("Retention and Target Tests")
    class RetentionAndTargetTests {

        @Test
        @DisplayName("should have RUNTIME retention")
        void shouldHaveRuntimeRetention() {
            var retention = YmlAlias.class.getAnnotation(java.lang.annotation.Retention.class);
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("should target FIELD, METHOD, and PARAMETER")
        void shouldTargetFieldMethodAndParameter() {
            var target = YmlAlias.class.getAnnotation(java.lang.annotation.Target.class);
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactlyInAnyOrder(
                ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER);
        }

        @Test
        @DisplayName("should be documented")
        void shouldBeDocumented() {
            assertThat(YmlAlias.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("Annotation Values Tests")
    class AnnotationValuesTests {

        @YmlAlias({"db.url", "database.url", "jdbc.url"})
        private String aliasedField;

        @YmlAlias({"single"})
        private String singleAliasField;

        @Test
        @DisplayName("should support multiple alias values")
        void shouldSupportMultipleAliases() throws NoSuchFieldException {
            YmlAlias annotation = getClass().getDeclaredField("aliasedField")
                .getAnnotation(YmlAlias.class);
            assertThat(annotation.value()).containsExactly("db.url", "database.url", "jdbc.url");
        }

        @Test
        @DisplayName("should support single alias value")
        void shouldSupportSingleAlias() throws NoSuchFieldException {
            YmlAlias annotation = getClass().getDeclaredField("singleAliasField")
                .getAnnotation(YmlAlias.class);
            assertThat(annotation.value()).containsExactly("single");
        }
    }
}
