package cloud.opencode.base.deepclone.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.*;

/**
 * 注解测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("注解测试")
class AnnotationTest {

    // Test entity with annotations
    public static class AnnotatedEntity {
        @CloneIgnore(reason = "Test ignore reason")
        private String ignoredField;

        @CloneReference(reason = "Test reference reason")
        private String referenceField;

        @CloneDeep(reason = "Test deep reason")
        private String deepField;

        private String normalField;
    }

    public static class DefaultAnnotatedEntity {
        @CloneIgnore
        private String ignoredField;

        @CloneReference
        private String referenceField;

        @CloneDeep
        private String deepField;
    }

    @Nested
    @DisplayName("@CloneIgnore 注解测试")
    class CloneIgnoreTests {

        @Test
        @DisplayName("获取reason属性")
        void testGetReason() throws NoSuchFieldException {
            CloneIgnore annotation = AnnotatedEntity.class
                    .getDeclaredField("ignoredField")
                    .getAnnotation(CloneIgnore.class);

            assertThat(annotation.reason()).isEqualTo("Test ignore reason");
        }

        @Test
        @DisplayName("默认reason为空")
        void testDefaultReason() throws NoSuchFieldException {
            CloneIgnore annotation = DefaultAnnotatedEntity.class
                    .getDeclaredField("ignoredField")
                    .getAnnotation(CloneIgnore.class);

            assertThat(annotation.reason()).isEmpty();
        }

        @Test
        @DisplayName("注解存在于字段上")
        void testAnnotationPresent() throws NoSuchFieldException {
            boolean present = AnnotatedEntity.class
                    .getDeclaredField("ignoredField")
                    .isAnnotationPresent(CloneIgnore.class);

            assertThat(present).isTrue();
        }

        @Test
        @DisplayName("普通字段无注解")
        void testAnnotationNotPresent() throws NoSuchFieldException {
            boolean present = AnnotatedEntity.class
                    .getDeclaredField("normalField")
                    .isAnnotationPresent(CloneIgnore.class);

            assertThat(present).isFalse();
        }

        @Test
        @DisplayName("@CloneIgnore 是RUNTIME保留")
        void testRetention() {
            Retention retention = CloneIgnore.class.getAnnotation(Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("@CloneIgnore 只能用于FIELD")
        void testTarget() {
            Target target = CloneIgnore.class.getAnnotation(Target.class);

            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }
    }

    @Nested
    @DisplayName("@CloneReference 注解测试")
    class CloneReferenceTests {

        @Test
        @DisplayName("获取reason属性")
        void testGetReason() throws NoSuchFieldException {
            CloneReference annotation = AnnotatedEntity.class
                    .getDeclaredField("referenceField")
                    .getAnnotation(CloneReference.class);

            assertThat(annotation.reason()).isEqualTo("Test reference reason");
        }

        @Test
        @DisplayName("默认reason为空")
        void testDefaultReason() throws NoSuchFieldException {
            CloneReference annotation = DefaultAnnotatedEntity.class
                    .getDeclaredField("referenceField")
                    .getAnnotation(CloneReference.class);

            assertThat(annotation.reason()).isEmpty();
        }

        @Test
        @DisplayName("注解存在于字段上")
        void testAnnotationPresent() throws NoSuchFieldException {
            boolean present = AnnotatedEntity.class
                    .getDeclaredField("referenceField")
                    .isAnnotationPresent(CloneReference.class);

            assertThat(present).isTrue();
        }

        @Test
        @DisplayName("@CloneReference 是RUNTIME保留")
        void testRetention() {
            Retention retention = CloneReference.class.getAnnotation(Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("@CloneReference 只能用于FIELD")
        void testTarget() {
            Target target = CloneReference.class.getAnnotation(Target.class);

            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }
    }

    @Nested
    @DisplayName("@CloneDeep 注解测试")
    class CloneDeepTests {

        @Test
        @DisplayName("获取reason属性")
        void testGetReason() throws NoSuchFieldException {
            CloneDeep annotation = AnnotatedEntity.class
                    .getDeclaredField("deepField")
                    .getAnnotation(CloneDeep.class);

            assertThat(annotation.reason()).isEqualTo("Test deep reason");
        }

        @Test
        @DisplayName("默认reason为空")
        void testDefaultReason() throws NoSuchFieldException {
            CloneDeep annotation = DefaultAnnotatedEntity.class
                    .getDeclaredField("deepField")
                    .getAnnotation(CloneDeep.class);

            assertThat(annotation.reason()).isEmpty();
        }

        @Test
        @DisplayName("注解存在于字段上")
        void testAnnotationPresent() throws NoSuchFieldException {
            boolean present = AnnotatedEntity.class
                    .getDeclaredField("deepField")
                    .isAnnotationPresent(CloneDeep.class);

            assertThat(present).isTrue();
        }

        @Test
        @DisplayName("@CloneDeep 是RUNTIME保留")
        void testRetention() {
            Retention retention = CloneDeep.class.getAnnotation(Retention.class);

            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("@CloneDeep 只能用于FIELD")
        void testTarget() {
            Target target = CloneDeep.class.getAnnotation(Target.class);

            assertThat(target.value()).containsExactly(ElementType.FIELD);
        }
    }
}
