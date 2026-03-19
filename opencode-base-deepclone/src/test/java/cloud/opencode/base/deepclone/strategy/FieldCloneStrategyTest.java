package cloud.opencode.base.deepclone.strategy;

import cloud.opencode.base.deepclone.annotation.CloneDeep;
import cloud.opencode.base.deepclone.annotation.CloneIgnore;
import cloud.opencode.base.deepclone.annotation.CloneReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

/**
 * FieldCloneStrategy 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("FieldCloneStrategy 测试")
class FieldCloneStrategyTest {

    // Test entity with various annotations
    public static class AnnotatedEntity {
        private String defaultField;

        @CloneIgnore
        private String ignoredField;

        @CloneReference
        private String referenceField;

        @CloneDeep
        private String deepField;

        @CloneIgnore
        @CloneReference
        private String multiAnnotated;
    }

    @Nested
    @DisplayName("枚举值测试")
    class EnumValuesTests {

        @Test
        @DisplayName("DEEP策略")
        void testDeepStrategy() {
            assertThat(FieldCloneStrategy.DEEP).isNotNull();
            assertThat(FieldCloneStrategy.DEEP.name()).isEqualTo("DEEP");
        }

        @Test
        @DisplayName("SHALLOW策略")
        void testShallowStrategy() {
            assertThat(FieldCloneStrategy.SHALLOW).isNotNull();
            assertThat(FieldCloneStrategy.SHALLOW.name()).isEqualTo("SHALLOW");
        }

        @Test
        @DisplayName("IGNORE策略")
        void testIgnoreStrategy() {
            assertThat(FieldCloneStrategy.IGNORE).isNotNull();
            assertThat(FieldCloneStrategy.IGNORE.name()).isEqualTo("IGNORE");
        }

        @Test
        @DisplayName("NULL策略")
        void testNullStrategy() {
            assertThat(FieldCloneStrategy.NULL).isNotNull();
            assertThat(FieldCloneStrategy.NULL.name()).isEqualTo("NULL");
        }

        @Test
        @DisplayName("所有枚举值")
        void testAllValues() {
            FieldCloneStrategy[] values = FieldCloneStrategy.values();

            assertThat(values).hasSize(4);
            assertThat(values).contains(
                    FieldCloneStrategy.DEEP,
                    FieldCloneStrategy.SHALLOW,
                    FieldCloneStrategy.IGNORE,
                    FieldCloneStrategy.NULL
            );
        }

        @Test
        @DisplayName("valueOf()方法")
        void testValueOf() {
            assertThat(FieldCloneStrategy.valueOf("DEEP")).isEqualTo(FieldCloneStrategy.DEEP);
            assertThat(FieldCloneStrategy.valueOf("SHALLOW")).isEqualTo(FieldCloneStrategy.SHALLOW);
            assertThat(FieldCloneStrategy.valueOf("IGNORE")).isEqualTo(FieldCloneStrategy.IGNORE);
            assertThat(FieldCloneStrategy.valueOf("NULL")).isEqualTo(FieldCloneStrategy.NULL);
        }
    }

    @Nested
    @DisplayName("fromAnnotations() 测试")
    class FromAnnotationsTests {

        @Test
        @DisplayName("无注解字段返回DEEP")
        void testNoAnnotation() throws NoSuchFieldException {
            Field field = AnnotatedEntity.class.getDeclaredField("defaultField");

            FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);

            assertThat(strategy).isEqualTo(FieldCloneStrategy.DEEP);
        }

        @Test
        @DisplayName("@CloneIgnore返回IGNORE")
        void testCloneIgnore() throws NoSuchFieldException {
            Field field = AnnotatedEntity.class.getDeclaredField("ignoredField");

            FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);

            assertThat(strategy).isEqualTo(FieldCloneStrategy.IGNORE);
        }

        @Test
        @DisplayName("@CloneReference返回SHALLOW")
        void testCloneReference() throws NoSuchFieldException {
            Field field = AnnotatedEntity.class.getDeclaredField("referenceField");

            FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);

            assertThat(strategy).isEqualTo(FieldCloneStrategy.SHALLOW);
        }

        @Test
        @DisplayName("@CloneDeep返回DEEP")
        void testCloneDeep() throws NoSuchFieldException {
            Field field = AnnotatedEntity.class.getDeclaredField("deepField");

            FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);

            assertThat(strategy).isEqualTo(FieldCloneStrategy.DEEP);
        }

        @Test
        @DisplayName("多注解优先级: @CloneIgnore > @CloneReference")
        void testAnnotationPriority() throws NoSuchFieldException {
            Field field = AnnotatedEntity.class.getDeclaredField("multiAnnotated");

            FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);

            // @CloneIgnore has highest priority
            assertThat(strategy).isEqualTo(FieldCloneStrategy.IGNORE);
        }
    }
}
