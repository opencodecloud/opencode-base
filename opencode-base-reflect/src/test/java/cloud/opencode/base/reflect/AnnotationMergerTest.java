package cloud.opencode.base.reflect;

import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * AnnotationMerger Tests
 * AnnotationMerger 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.3
 */
@DisplayName("AnnotationMerger 测试")
class AnnotationMergerTest {

    // ==================== Test Annotations | 测试注解 ====================

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface BaseAnnotation {
        String value() default "";
        int priority() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @BaseAnnotation(priority = 10)
    @interface ComposedAnnotation {
        String value() default "composed";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface AnotherAnnotation {
        String name() default "another";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @ComposedAnnotation(value = "deep")
    @interface DeeplyComposedAnnotation {
        String value() default "deeply-composed";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @BaseAnnotation(priority = 5)
    @interface SecondComposedAnnotation {
        String value() default "second";
    }

    // ==================== Test Classes | 测试类 ====================

    @BaseAnnotation(value = "direct", priority = 99)
    static class DirectlyAnnotatedClass {}

    @ComposedAnnotation(value = "test")
    static class ComposedAnnotatedClass {}

    @ComposedAnnotation
    static class ComposedDefaultClass {}

    @DeeplyComposedAnnotation(value = "overridden")
    static class DeeplyAnnotatedClass {}

    @AnotherAnnotation(name = "hello")
    static class UnrelatedAnnotatedClass {}

    static class UnannotatedClass {}

    @ComposedAnnotation(value = "first")
    @SecondComposedAnnotation(value = "second-val")
    static class MultiComposedClass {}

    // ==================== Tests | 测试 ====================

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = AnnotationMerger.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getMergedAnnotation 方法测试")
    class GetMergedAnnotationTests {

        @Test
        @DisplayName("直接注解 - 无需合并")
        void testDirectAnnotation() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    DirectlyAnnotatedClass.class, BaseAnnotation.class);

            assertThat(result).isNotNull();
            assertThat(result.value()).isEqualTo("direct");
            assertThat(result.priority()).isEqualTo(99);
        }

        @Test
        @DisplayName("组合注解 - 属性覆盖")
        void testComposedAnnotationOverride() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    ComposedAnnotatedClass.class, BaseAnnotation.class);

            assertThat(result).isNotNull();
            // value should be overridden by ComposedAnnotation's value
            assertThat(result.value()).isEqualTo("test");
            // priority should come from meta-annotation @BaseAnnotation(priority = 10)
            assertThat(result.priority()).isEqualTo(10);
        }

        @Test
        @DisplayName("组合注解 - 默认值保留")
        void testComposedAnnotationDefaultPreserved() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    ComposedDefaultClass.class, BaseAnnotation.class);

            assertThat(result).isNotNull();
            // ComposedAnnotation uses its default "composed", which differs from
            // BaseAnnotation's default "", so it still overrides
            assertThat(result.priority()).isEqualTo(10);
        }

        @Test
        @DisplayName("注解未找到 - 返回null")
        void testAnnotationNotFound() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    UnannotatedClass.class, BaseAnnotation.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("不相关注解 - 返回null")
        void testUnrelatedAnnotation() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    UnrelatedAnnotatedClass.class, BaseAnnotation.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null元素 - 返回null")
        void testNullElement() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    null, BaseAnnotation.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null注解类型 - 返回null")
        void testNullAnnotationType() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    DirectlyAnnotatedClass.class, null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("深层组合注解 - 多层覆盖")
        void testDeeplyComposedAnnotation() {
            BaseAnnotation result = AnnotationMerger.getMergedAnnotation(
                    DeeplyAnnotatedClass.class, BaseAnnotation.class);

            assertThat(result).isNotNull();
            // value is "overridden" from DeeplyComposedAnnotation
            assertThat(result.value()).isEqualTo("overridden");
            // priority is 10 from @BaseAnnotation(priority = 10) on ComposedAnnotation
            assertThat(result.priority()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("getMergedAttributes 方法测试")
    class GetMergedAttributesTests {

        @Test
        @DisplayName("直接注解 - 返回属性映射")
        void testDirectAnnotationAttributes() {
            Map<String, Object> attrs = AnnotationMerger.getMergedAttributes(
                    DirectlyAnnotatedClass.class, BaseAnnotation.class);

            assertThat(attrs).isNotNull();
            assertThat(attrs).containsEntry("value", "direct");
            assertThat(attrs).containsEntry("priority", 99);
        }

        @Test
        @DisplayName("组合注解 - 合并属性映射")
        void testComposedAnnotationAttributes() {
            Map<String, Object> attrs = AnnotationMerger.getMergedAttributes(
                    ComposedAnnotatedClass.class, BaseAnnotation.class);

            assertThat(attrs).isNotNull();
            assertThat(attrs).containsEntry("value", "test");
            assertThat(attrs).containsEntry("priority", 10);
        }

        @Test
        @DisplayName("注解未找到 - 返回null")
        void testNotFoundReturnsNull() {
            Map<String, Object> attrs = AnnotationMerger.getMergedAttributes(
                    UnannotatedClass.class, BaseAnnotation.class);

            assertThat(attrs).isNull();
        }

        @Test
        @DisplayName("null元素 - 返回null")
        void testNullElement() {
            Map<String, Object> attrs = AnnotationMerger.getMergedAttributes(
                    null, BaseAnnotation.class);

            assertThat(attrs).isNull();
        }
    }

    @Nested
    @DisplayName("findAllMergedAnnotations 方法测试")
    class FindAllMergedAnnotationsTests {

        @Test
        @DisplayName("查找直接注解")
        void testFindDirectAnnotation() {
            List<BaseAnnotation> results = AnnotationMerger.findAllMergedAnnotations(
                    DirectlyAnnotatedClass.class, BaseAnnotation.class);

            assertThat(results).hasSize(1);
            assertThat(results.getFirst().value()).isEqualTo("direct");
        }

        @Test
        @DisplayName("查找组合注解")
        void testFindComposedAnnotation() {
            List<BaseAnnotation> results = AnnotationMerger.findAllMergedAnnotations(
                    ComposedAnnotatedClass.class, BaseAnnotation.class);

            assertThat(results).isNotEmpty();
        }

        @Test
        @DisplayName("未找到 - 返回空列表")
        void testFindNoneReturnsEmptyList() {
            List<BaseAnnotation> results = AnnotationMerger.findAllMergedAnnotations(
                    UnannotatedClass.class, BaseAnnotation.class);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("null元素 - 返回空列表")
        void testNullElementReturnsEmptyList() {
            List<BaseAnnotation> results = AnnotationMerger.findAllMergedAnnotations(
                    null, BaseAnnotation.class);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("返回的列表不可修改")
        void testReturnedListIsUnmodifiable() {
            List<BaseAnnotation> results = AnnotationMerger.findAllMergedAnnotations(
                    DirectlyAnnotatedClass.class, BaseAnnotation.class);

            assertThatThrownBy(() -> results.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("isAnnotationPresent 方法测试")
    class IsAnnotationPresentTests {

        @Test
        @DisplayName("直接注解 - 返回true")
        void testDirectAnnotationPresent() {
            assertThat(AnnotationMerger.isAnnotationPresent(
                    DirectlyAnnotatedClass.class, BaseAnnotation.class)).isTrue();
        }

        @Test
        @DisplayName("元注解 - 返回true")
        void testMetaAnnotationPresent() {
            assertThat(AnnotationMerger.isAnnotationPresent(
                    ComposedAnnotatedClass.class, BaseAnnotation.class)).isTrue();
        }

        @Test
        @DisplayName("不存在 - 返回false")
        void testAnnotationNotPresent() {
            assertThat(AnnotationMerger.isAnnotationPresent(
                    UnannotatedClass.class, BaseAnnotation.class)).isFalse();
        }

        @Test
        @DisplayName("不相关注解 - 返回false")
        void testUnrelatedAnnotationNotPresent() {
            assertThat(AnnotationMerger.isAnnotationPresent(
                    UnrelatedAnnotatedClass.class, BaseAnnotation.class)).isFalse();
        }

        @Test
        @DisplayName("null元素 - 返回false")
        void testNullElement() {
            assertThat(AnnotationMerger.isAnnotationPresent(
                    null, BaseAnnotation.class)).isFalse();
        }

        @Test
        @DisplayName("null注解类型 - 返回false")
        void testNullAnnotationType() {
            assertThat(AnnotationMerger.isAnnotationPresent(
                    DirectlyAnnotatedClass.class, null)).isFalse();
        }

        @Test
        @DisplayName("深层元注解 - 返回true")
        void testDeeplyNestedMetaAnnotation() {
            assertThat(AnnotationMerger.isAnnotationPresent(
                    DeeplyAnnotatedClass.class, BaseAnnotation.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("synthesize 方法测试")
    class SynthesizeTests {

        @Test
        @DisplayName("合成注解 - 属性正确")
        void testSynthesizeBasicAnnotation() {
            Map<String, Object> attrs = Map.of("value", "synth", "priority", 42);
            BaseAnnotation synth = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);

            assertThat(synth.value()).isEqualTo("synth");
            assertThat(synth.priority()).isEqualTo(42);
        }

        @Test
        @DisplayName("合成注解 - annotationType正确")
        void testSynthesizeAnnotationType() {
            Map<String, Object> attrs = Map.of("value", "test", "priority", 1);
            BaseAnnotation synth = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);

            assertThat(synth.annotationType()).isEqualTo(BaseAnnotation.class);
        }

        @Test
        @DisplayName("合成注解 - 缺失属性使用默认值")
        void testSynthesizeUsesDefaults() {
            Map<String, Object> attrs = Map.of("value", "partial");
            BaseAnnotation synth = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);

            assertThat(synth.value()).isEqualTo("partial");
            assertThat(synth.priority()).isEqualTo(0); // default
        }

        @Test
        @DisplayName("合成注解 - toString")
        void testSynthesizeToString() {
            Map<String, Object> attrs = Map.of("value", "test", "priority", 5);
            BaseAnnotation synth = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);

            String str = synth.toString();
            assertThat(str).contains("BaseAnnotation");
            assertThat(str).contains("value=test");
            assertThat(str).contains("priority=5");
        }

        @Test
        @DisplayName("合成注解 - hashCode一致性")
        void testSynthesizeHashCodeConsistency() {
            Map<String, Object> attrs = Map.of("value", "test", "priority", 5);
            BaseAnnotation synth1 = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);
            BaseAnnotation synth2 = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);

            assertThat(synth1.hashCode()).isEqualTo(synth2.hashCode());
        }

        @Test
        @DisplayName("合成注解 - equals")
        void testSynthesizeEquals() {
            Map<String, Object> attrs = Map.of("value", "test", "priority", 5);
            BaseAnnotation synth1 = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);
            BaseAnnotation synth2 = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);

            assertThat(synth1.equals(synth2)).isTrue();
        }

        @Test
        @DisplayName("合成注解 - equals不同值")
        void testSynthesizeNotEqual() {
            BaseAnnotation synth1 = AnnotationMerger.synthesize(
                    BaseAnnotation.class, Map.of("value", "a", "priority", 1));
            BaseAnnotation synth2 = AnnotationMerger.synthesize(
                    BaseAnnotation.class, Map.of("value", "b", "priority", 2));

            assertThat(synth1.equals(synth2)).isFalse();
        }

        @Test
        @DisplayName("null注解类型 - 抛出NullPointerException")
        void testSynthesizeNullAnnotationType() {
            assertThatThrownBy(() -> AnnotationMerger.synthesize(null, Map.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null属性映射 - 抛出NullPointerException")
        void testSynthesizeNullAttributes() {
            assertThatThrownBy(() -> AnnotationMerger.synthesize(BaseAnnotation.class, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("合成注解与真实注解equals")
        void testSynthesizeEqualsRealAnnotation() {
            BaseAnnotation real = DirectlyAnnotatedClass.class.getAnnotation(BaseAnnotation.class);
            Map<String, Object> attrs = Map.of("value", "direct", "priority", 99);
            BaseAnnotation synth = AnnotationMerger.synthesize(BaseAnnotation.class, attrs);

            assertThat(synth.equals(real)).isTrue();
        }
    }
}
