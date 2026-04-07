package cloud.opencode.base.json.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * JsonAutoDetect, JsonPropertyOrder, JsonInclude 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-json V1.0.0
 */
@DisplayName("JSON 类级别注解测试")
class JsonClassAnnotationsTest {

    // ==================== JsonAutoDetect Tests ====================

    @Nested
    @DisplayName("JsonAutoDetect 注解测试")
    class JsonAutoDetectTests {

        @Nested
        @DisplayName("Visibility 枚举测试")
        class VisibilityEnumTests {

            @Test
            @DisplayName("所有Visibility值存在")
            void testAllVisibilityValues() {
                assertThat(JsonAutoDetect.Visibility.values())
                    .containsExactlyInAnyOrder(
                        JsonAutoDetect.Visibility.ANY,
                        JsonAutoDetect.Visibility.NON_PRIVATE,
                        JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC,
                        JsonAutoDetect.Visibility.PUBLIC_ONLY,
                        JsonAutoDetect.Visibility.NONE,
                        JsonAutoDetect.Visibility.DEFAULT
                    );
            }

            @Test
            @DisplayName("valueOf返回正确值")
            void testValueOf() {
                assertThat(JsonAutoDetect.Visibility.valueOf("ANY"))
                    .isEqualTo(JsonAutoDetect.Visibility.ANY);
                assertThat(JsonAutoDetect.Visibility.valueOf("NON_PRIVATE"))
                    .isEqualTo(JsonAutoDetect.Visibility.NON_PRIVATE);
                assertThat(JsonAutoDetect.Visibility.valueOf("PROTECTED_AND_PUBLIC"))
                    .isEqualTo(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
                assertThat(JsonAutoDetect.Visibility.valueOf("PUBLIC_ONLY"))
                    .isEqualTo(JsonAutoDetect.Visibility.PUBLIC_ONLY);
                assertThat(JsonAutoDetect.Visibility.valueOf("NONE"))
                    .isEqualTo(JsonAutoDetect.Visibility.NONE);
                assertThat(JsonAutoDetect.Visibility.valueOf("DEFAULT"))
                    .isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
            }

            @Test
            @DisplayName("无效Visibility名抛出异常")
            void testInvalidValueOf() {
                assertThatThrownBy(() -> JsonAutoDetect.Visibility.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("Visibility序号正确")
            void testVisibilityOrdinals() {
                assertThat(JsonAutoDetect.Visibility.ANY.ordinal()).isEqualTo(0);
                assertThat(JsonAutoDetect.Visibility.NON_PRIVATE.ordinal()).isEqualTo(1);
                assertThat(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC.ordinal()).isEqualTo(2);
                assertThat(JsonAutoDetect.Visibility.PUBLIC_ONLY.ordinal()).isEqualTo(3);
                assertThat(JsonAutoDetect.Visibility.NONE.ordinal()).isEqualTo(4);
                assertThat(JsonAutoDetect.Visibility.DEFAULT.ordinal()).isEqualTo(5);
            }
        }

        @Nested
        @DisplayName("注解默认值测试")
        class DefaultValueTests {

            @Test
            @DisplayName("所有属性默认为DEFAULT")
            void testAllDefaultValues() {
                JsonAutoDetect annotation = DefaultAutoDetectClass.class
                    .getAnnotation(JsonAutoDetect.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.fieldVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
                assertThat(annotation.getterVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
                assertThat(annotation.setterVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
                assertThat(annotation.isGetterVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
                assertThat(annotation.creatorVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.DEFAULT);
            }
        }

        @Nested
        @DisplayName("注解属性测试")
        class AttributeTests {

            @Test
            @DisplayName("自定义可见性设置")
            void testCustomVisibility() {
                JsonAutoDetect annotation = CustomAutoDetectClass.class
                    .getAnnotation(JsonAutoDetect.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.fieldVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.ANY);
                assertThat(annotation.getterVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.NONE);
                assertThat(annotation.setterVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.PUBLIC_ONLY);
                assertThat(annotation.isGetterVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
                assertThat(annotation.creatorVisibility())
                    .isEqualTo(JsonAutoDetect.Visibility.NON_PRIVATE);
            }
        }

        @Nested
        @DisplayName("注解元数据测试")
        class MetadataTests {

            @Test
            @DisplayName("Target为TYPE")
            void testTarget() {
                java.lang.annotation.Target target =
                    JsonAutoDetect.class.getAnnotation(java.lang.annotation.Target.class);

                assertThat(target.value()).containsExactly(ElementType.TYPE);
            }

            @Test
            @DisplayName("Retention为RUNTIME")
            void testRetention() {
                java.lang.annotation.Retention retention =
                    JsonAutoDetect.class.getAnnotation(java.lang.annotation.Retention.class);

                assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
            }

            @Test
            @DisplayName("Documented存在")
            void testDocumented() {
                java.lang.annotation.Documented documented =
                    JsonAutoDetect.class.getAnnotation(java.lang.annotation.Documented.class);

                assertThat(documented).isNotNull();
            }
        }
    }

    // ==================== JsonPropertyOrder Tests ====================

    @Nested
    @DisplayName("JsonPropertyOrder 注解测试")
    class JsonPropertyOrderTests {

        @Nested
        @DisplayName("注解默认值测试")
        class DefaultValueTests {

            @Test
            @DisplayName("默认value为空数组")
            void testDefaultValue() {
                JsonPropertyOrder annotation = DefaultOrderClass.class
                    .getAnnotation(JsonPropertyOrder.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).isEmpty();
            }

            @Test
            @DisplayName("默认alphabetic为false")
            void testDefaultAlphabetic() {
                JsonPropertyOrder annotation = DefaultOrderClass.class
                    .getAnnotation(JsonPropertyOrder.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.alphabetic()).isFalse();
            }
        }

        @Nested
        @DisplayName("注解属性测试")
        class AttributeTests {

            @Test
            @DisplayName("自定义属性顺序")
            void testCustomOrder() {
                JsonPropertyOrder annotation = CustomOrderClass.class
                    .getAnnotation(JsonPropertyOrder.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).containsExactly("id", "name", "email");
            }

            @Test
            @DisplayName("启用字母排序")
            void testAlphabeticOrder() {
                JsonPropertyOrder annotation = AlphabeticOrderClass.class
                    .getAnnotation(JsonPropertyOrder.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.alphabetic()).isTrue();
            }

            @Test
            @DisplayName("组合属性顺序和字母排序")
            void testCombinedOrder() {
                JsonPropertyOrder annotation = CombinedOrderClass.class
                    .getAnnotation(JsonPropertyOrder.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).containsExactly("id", "name");
                assertThat(annotation.alphabetic()).isTrue();
            }
        }

        @Nested
        @DisplayName("注解元数据测试")
        class MetadataTests {

            @Test
            @DisplayName("Target为TYPE")
            void testTarget() {
                java.lang.annotation.Target target =
                    JsonPropertyOrder.class.getAnnotation(java.lang.annotation.Target.class);

                assertThat(target.value()).containsExactly(ElementType.TYPE);
            }

            @Test
            @DisplayName("Retention为RUNTIME")
            void testRetention() {
                java.lang.annotation.Retention retention =
                    JsonPropertyOrder.class.getAnnotation(java.lang.annotation.Retention.class);

                assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
            }

            @Test
            @DisplayName("Documented存在")
            void testDocumented() {
                java.lang.annotation.Documented documented =
                    JsonPropertyOrder.class.getAnnotation(java.lang.annotation.Documented.class);

                assertThat(documented).isNotNull();
            }
        }
    }

    // ==================== JsonInclude Tests ====================

    @Nested
    @DisplayName("JsonInclude 注解测试")
    class JsonIncludeTests {

        @Nested
        @DisplayName("Include 枚举测试")
        class IncludeEnumTests {

            @Test
            @DisplayName("所有Include值存在")
            void testAllIncludeValues() {
                assertThat(JsonInclude.Include.values())
                    .containsExactlyInAnyOrder(
                        JsonInclude.Include.ALWAYS,
                        JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.NON_ABSENT,
                        JsonInclude.Include.NON_EMPTY,
                        JsonInclude.Include.NON_DEFAULT,
                        JsonInclude.Include.CUSTOM,
                        JsonInclude.Include.USE_DEFAULTS
                    );
            }

            @Test
            @DisplayName("valueOf返回正确值")
            void testValueOf() {
                assertThat(JsonInclude.Include.valueOf("ALWAYS"))
                    .isEqualTo(JsonInclude.Include.ALWAYS);
                assertThat(JsonInclude.Include.valueOf("NON_NULL"))
                    .isEqualTo(JsonInclude.Include.NON_NULL);
                assertThat(JsonInclude.Include.valueOf("NON_ABSENT"))
                    .isEqualTo(JsonInclude.Include.NON_ABSENT);
                assertThat(JsonInclude.Include.valueOf("NON_EMPTY"))
                    .isEqualTo(JsonInclude.Include.NON_EMPTY);
                assertThat(JsonInclude.Include.valueOf("NON_DEFAULT"))
                    .isEqualTo(JsonInclude.Include.NON_DEFAULT);
                assertThat(JsonInclude.Include.valueOf("CUSTOM"))
                    .isEqualTo(JsonInclude.Include.CUSTOM);
                assertThat(JsonInclude.Include.valueOf("USE_DEFAULTS"))
                    .isEqualTo(JsonInclude.Include.USE_DEFAULTS);
            }

            @Test
            @DisplayName("无效Include名抛出异常")
            void testInvalidValueOf() {
                assertThatThrownBy(() -> JsonInclude.Include.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("Include序号正确")
            void testIncludeOrdinals() {
                assertThat(JsonInclude.Include.ALWAYS.ordinal()).isEqualTo(0);
                assertThat(JsonInclude.Include.NON_NULL.ordinal()).isEqualTo(1);
                assertThat(JsonInclude.Include.NON_ABSENT.ordinal()).isEqualTo(2);
                assertThat(JsonInclude.Include.NON_EMPTY.ordinal()).isEqualTo(3);
                assertThat(JsonInclude.Include.NON_DEFAULT.ordinal()).isEqualTo(4);
                assertThat(JsonInclude.Include.CUSTOM.ordinal()).isEqualTo(5);
                assertThat(JsonInclude.Include.USE_DEFAULTS.ordinal()).isEqualTo(6);
            }
        }

        @Nested
        @DisplayName("注解默认值测试")
        class DefaultValueTests {

            @Test
            @DisplayName("默认value为ALWAYS")
            void testDefaultValue() {
                JsonInclude annotation = DefaultIncludeClass.class
                    .getAnnotation(JsonInclude.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).isEqualTo(JsonInclude.Include.ALWAYS);
            }

            @Test
            @DisplayName("默认content为ALWAYS")
            void testDefaultContent() {
                JsonInclude annotation = DefaultIncludeClass.class
                    .getAnnotation(JsonInclude.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.content()).isEqualTo(JsonInclude.Include.ALWAYS);
            }
        }

        @Nested
        @DisplayName("注解属性测试")
        class AttributeTests {

            @Test
            @DisplayName("类级别NON_NULL")
            void testClassLevelNonNull() {
                JsonInclude annotation = NonNullIncludeClass.class
                    .getAnnotation(JsonInclude.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).isEqualTo(JsonInclude.Include.NON_NULL);
            }

            @Test
            @DisplayName("组合value和content策略")
            void testCombinedStrategy() {
                JsonInclude annotation = CombinedIncludeClass.class
                    .getAnnotation(JsonInclude.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).isEqualTo(JsonInclude.Include.NON_EMPTY);
                assertThat(annotation.content()).isEqualTo(JsonInclude.Include.NON_NULL);
            }

            @Test
            @DisplayName("字段级别注解")
            void testFieldLevelAnnotation() throws NoSuchFieldException {
                Field field = FieldIncludeClass.class.getDeclaredField("email");
                JsonInclude annotation = field.getAnnotation(JsonInclude.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).isEqualTo(JsonInclude.Include.NON_EMPTY);
            }

            @Test
            @DisplayName("方法级别注解")
            void testMethodLevelAnnotation() throws NoSuchMethodException {
                Method method = MethodIncludeClass.class.getDeclaredMethod("getTags");
                JsonInclude annotation = method.getAnnotation(JsonInclude.class);

                assertThat(annotation).isNotNull();
                assertThat(annotation.value()).isEqualTo(JsonInclude.Include.NON_EMPTY);
                assertThat(annotation.content()).isEqualTo(JsonInclude.Include.NON_NULL);
            }
        }

        @Nested
        @DisplayName("注解元数据测试")
        class MetadataTests {

            @Test
            @DisplayName("Target包含TYPE、METHOD、FIELD")
            void testTarget() {
                java.lang.annotation.Target target =
                    JsonInclude.class.getAnnotation(java.lang.annotation.Target.class);

                assertThat(target.value())
                    .containsExactlyInAnyOrder(
                        ElementType.TYPE, ElementType.METHOD, ElementType.FIELD
                    );
            }

            @Test
            @DisplayName("Retention为RUNTIME")
            void testRetention() {
                java.lang.annotation.Retention retention =
                    JsonInclude.class.getAnnotation(java.lang.annotation.Retention.class);

                assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
            }

            @Test
            @DisplayName("Documented存在")
            void testDocumented() {
                java.lang.annotation.Documented documented =
                    JsonInclude.class.getAnnotation(java.lang.annotation.Documented.class);

                assertThat(documented).isNotNull();
            }
        }
    }

    // ==================== Test Helper Classes ====================

    @JsonAutoDetect
    static class DefaultAutoDetectClass {
        String name;
    }

    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC,
        creatorVisibility = JsonAutoDetect.Visibility.NON_PRIVATE
    )
    static class CustomAutoDetectClass {
        private String name;
        private int age;
    }

    @JsonPropertyOrder
    static class DefaultOrderClass {
        String name;
        String email;
    }

    @JsonPropertyOrder({"id", "name", "email"})
    static class CustomOrderClass {
        String email;
        String name;
        long id;
    }

    @JsonPropertyOrder(alphabetic = true)
    static class AlphabeticOrderClass {
        String zebra;
        String alpha;
    }

    @JsonPropertyOrder(value = {"id", "name"}, alphabetic = true)
    static class CombinedOrderClass {
        String email;
        String name;
        long id;
        String phone;
    }

    @JsonInclude
    static class DefaultIncludeClass {
        String name;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class NonNullIncludeClass {
        String name;
        String email;
    }

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
    static class CombinedIncludeClass {
        Map<String, String> settings;
    }

    static class FieldIncludeClass {
        String name;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        String email;
    }

    static class MethodIncludeClass {
        private List<String> tags;

        @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
        List<String> getTags() {
            return tags;
        }
    }
}
