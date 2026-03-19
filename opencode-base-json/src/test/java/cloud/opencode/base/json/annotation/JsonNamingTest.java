package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonNaming 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JsonNaming 注解测试")
class JsonNamingTest {

    @Nested
    @DisplayName("注解属性测试")
    class AnnotationAttributeTests {

        @Test
        @DisplayName("注解可用于类型")
        void testTargetType() {
            JsonNaming annotation = AnnotatedClass.class.getAnnotation(JsonNaming.class);

            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("默认值为IDENTITY")
        void testDefaultValue() {
            JsonNaming annotation = DefaultAnnotatedClass.class.getAnnotation(JsonNaming.class);

            assertThat(annotation.value()).isEqualTo(JsonNaming.Strategy.IDENTITY);
        }

        @Test
        @DisplayName("获取SNAKE_CASE策略")
        void testSnakeCaseStrategy() {
            JsonNaming annotation = SnakeCaseClass.class.getAnnotation(JsonNaming.class);

            assertThat(annotation.value()).isEqualTo(JsonNaming.Strategy.SNAKE_CASE);
        }

        @Test
        @DisplayName("获取KEBAB_CASE策略")
        void testKebabCaseStrategy() {
            JsonNaming annotation = KebabCaseClass.class.getAnnotation(JsonNaming.class);

            assertThat(annotation.value()).isEqualTo(JsonNaming.Strategy.KEBAB_CASE);
        }

        @Test
        @DisplayName("获取PASCAL_CASE策略")
        void testPascalCaseStrategy() {
            JsonNaming annotation = PascalCaseClass.class.getAnnotation(JsonNaming.class);

            assertThat(annotation.value()).isEqualTo(JsonNaming.Strategy.PASCAL_CASE);
        }

        @Test
        @DisplayName("运行时可见")
        void testRuntimeRetention() {
            Retention retention = JsonNaming.class.getAnnotation(Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }
    }

    @Nested
    @DisplayName("Strategy枚举测试")
    class StrategyEnumTests {

        @Test
        @DisplayName("所有策略存在")
        void testAllStrategies() {
            assertThat(JsonNaming.Strategy.values())
                .containsExactlyInAnyOrder(
                    JsonNaming.Strategy.IDENTITY,
                    JsonNaming.Strategy.SNAKE_CASE,
                    JsonNaming.Strategy.UPPER_SNAKE_CASE,
                    JsonNaming.Strategy.KEBAB_CASE,
                    JsonNaming.Strategy.PASCAL_CASE,
                    JsonNaming.Strategy.LOWER_CASE,
                    JsonNaming.Strategy.DOT_CASE
                );
        }

        @Test
        @DisplayName("valueOf返回正确值")
        void testValueOf() {
            assertThat(JsonNaming.Strategy.valueOf("IDENTITY"))
                .isEqualTo(JsonNaming.Strategy.IDENTITY);
            assertThat(JsonNaming.Strategy.valueOf("SNAKE_CASE"))
                .isEqualTo(JsonNaming.Strategy.SNAKE_CASE);
            assertThat(JsonNaming.Strategy.valueOf("UPPER_SNAKE_CASE"))
                .isEqualTo(JsonNaming.Strategy.UPPER_SNAKE_CASE);
            assertThat(JsonNaming.Strategy.valueOf("KEBAB_CASE"))
                .isEqualTo(JsonNaming.Strategy.KEBAB_CASE);
            assertThat(JsonNaming.Strategy.valueOf("PASCAL_CASE"))
                .isEqualTo(JsonNaming.Strategy.PASCAL_CASE);
            assertThat(JsonNaming.Strategy.valueOf("LOWER_CASE"))
                .isEqualTo(JsonNaming.Strategy.LOWER_CASE);
            assertThat(JsonNaming.Strategy.valueOf("DOT_CASE"))
                .isEqualTo(JsonNaming.Strategy.DOT_CASE);
        }

        @Test
        @DisplayName("无效策略名抛出异常")
        void testInvalidValueOf() {
            assertThatThrownBy(() -> JsonNaming.Strategy.valueOf("INVALID"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("策略名称正确")
        void testStrategyNames() {
            assertThat(JsonNaming.Strategy.IDENTITY.name()).isEqualTo("IDENTITY");
            assertThat(JsonNaming.Strategy.SNAKE_CASE.name()).isEqualTo("SNAKE_CASE");
            assertThat(JsonNaming.Strategy.UPPER_SNAKE_CASE.name()).isEqualTo("UPPER_SNAKE_CASE");
            assertThat(JsonNaming.Strategy.KEBAB_CASE.name()).isEqualTo("KEBAB_CASE");
            assertThat(JsonNaming.Strategy.PASCAL_CASE.name()).isEqualTo("PASCAL_CASE");
            assertThat(JsonNaming.Strategy.LOWER_CASE.name()).isEqualTo("LOWER_CASE");
            assertThat(JsonNaming.Strategy.DOT_CASE.name()).isEqualTo("DOT_CASE");
        }

        @Test
        @DisplayName("策略序号正确")
        void testStrategyOrdinals() {
            assertThat(JsonNaming.Strategy.IDENTITY.ordinal()).isEqualTo(0);
            assertThat(JsonNaming.Strategy.SNAKE_CASE.ordinal()).isEqualTo(1);
            assertThat(JsonNaming.Strategy.UPPER_SNAKE_CASE.ordinal()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("注解元数据测试")
    class AnnotationMetadataTests {

        @Test
        @DisplayName("Target为TYPE")
        void testTarget() {
            java.lang.annotation.Target target = JsonNaming.class.getAnnotation(java.lang.annotation.Target.class);

            assertThat(target.value()).containsExactly(ElementType.TYPE);
        }

        @Test
        @DisplayName("Documented存在")
        void testDocumented() {
            java.lang.annotation.Documented documented =
                JsonNaming.class.getAnnotation(java.lang.annotation.Documented.class);

            assertThat(documented).isNotNull();
        }
    }

    // Test helper classes
    @JsonNaming(JsonNaming.Strategy.SNAKE_CASE)
    static class AnnotatedClass {}

    @JsonNaming
    static class DefaultAnnotatedClass {}

    @JsonNaming(JsonNaming.Strategy.SNAKE_CASE)
    static class SnakeCaseClass {}

    @JsonNaming(JsonNaming.Strategy.KEBAB_CASE)
    static class KebabCaseClass {}

    @JsonNaming(JsonNaming.Strategy.PASCAL_CASE)
    static class PascalCaseClass {}
}
