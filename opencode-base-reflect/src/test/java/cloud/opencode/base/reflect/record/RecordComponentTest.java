package cloud.opencode.base.reflect.record;

import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.lang.reflect.Type;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * RecordComponentTest Tests
 * RecordComponentTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("RecordComponent 测试")
class RecordComponentTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建RecordComponent")
        void testCreate() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component).isNotNull();
        }

        @Test
        @DisplayName("null参数抛出异常")
        void testCreateNull() {
            assertThatThrownBy(() -> new RecordComponent(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取组件名称")
        void testGetName() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取组件类型")
        void testGetType() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent nameComponent = new RecordComponent(components[0]);
            RecordComponent ageComponent = new RecordComponent(components[1]);

            assertThat(nameComponent.getType()).isEqualTo(String.class);
            assertThat(ageComponent.getType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("getGenericType方法测试")
    class GetGenericTypeTests {

        @Test
        @DisplayName("获取泛型类型")
        void testGetGenericType() {
            java.lang.reflect.RecordComponent[] components = GenericRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            Type genericType = component.getGenericType();
            assertThat(genericType).isNotNull();
        }
    }

    @Nested
    @DisplayName("getAccessor方法测试")
    class GetAccessorTests {

        @Test
        @DisplayName("获取访问器方法")
        void testGetAccessor() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.getAccessor()).isNotNull();
            assertThat(component.getAccessor().getName()).isEqualTo("name");
        }
    }

    @Nested
    @DisplayName("getDeclaringClass方法测试")
    class GetDeclaringClassTests {

        @Test
        @DisplayName("获取声明类")
        void testGetDeclaringClass() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.getDeclaringClass()).isEqualTo(TestRecord.class);
        }
    }

    @Nested
    @DisplayName("getValue方法测试")
    class GetValueTests {

        @Test
        @DisplayName("获取组件值")
        void testGetValue() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            TestRecord record = new TestRecord("test", 25);
            assertThat(component.getValue(record)).isEqualTo("test");
        }

        @Test
        @DisplayName("获取组件值带类型")
        void testGetValueTyped() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            TestRecord record = new TestRecord("test", 25);
            String value = component.getValue(record, String.class);
            assertThat(value).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("注解方法测试")
    class AnnotationTests {

        @Test
        @DisplayName("获取注解")
        void testGetAnnotation() {
            java.lang.reflect.RecordComponent[] components = AnnotatedRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            TestAnnotation annotation = component.getAnnotation(TestAnnotation.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("查找注解返回Optional")
        void testFindAnnotation() {
            java.lang.reflect.RecordComponent[] components = AnnotatedRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.findAnnotation(TestAnnotation.class)).isPresent();
            assertThat(component.findAnnotation(Deprecated.class)).isEmpty();
        }

        @Test
        @DisplayName("检查注解是否存在")
        void testHasAnnotation() {
            java.lang.reflect.RecordComponent[] components = AnnotatedRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.hasAnnotation(TestAnnotation.class)).isTrue();
            assertThat(component.hasAnnotation(Deprecated.class)).isFalse();
        }

        @Test
        @DisplayName("获取所有注解")
        void testGetAnnotations() {
            java.lang.reflect.RecordComponent[] components = AnnotatedRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            Annotation[] annotations = component.getAnnotations();
            assertThat(annotations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("unwrap方法测试")
    class UnwrapTests {

        @Test
        @DisplayName("获取底层RecordComponent")
        void testUnwrap() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.unwrap()).isSameAs(components[0]);
        }
    }

    @Nested
    @DisplayName("getIndex方法测试")
    class GetIndexTests {

        @Test
        @DisplayName("获取组件索引")
        void testGetIndex() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent first = new RecordComponent(components[0]);
            RecordComponent second = new RecordComponent(components[1]);

            assertThat(first.getIndex()).isEqualTo(0);
            assertThat(second.getIndex()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("isPrimitive方法测试")
    class IsPrimitiveTests {

        @Test
        @DisplayName("原始类型组件返回true")
        void testIsPrimitiveTrue() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent ageComponent = new RecordComponent(components[1]);
            assertThat(ageComponent.isPrimitive()).isTrue();
        }

        @Test
        @DisplayName("非原始类型组件返回false")
        void testIsPrimitiveFalse() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent nameComponent = new RecordComponent(components[0]);
            assertThat(nameComponent.isPrimitive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isArray方法测试")
    class IsArrayTests {

        @Test
        @DisplayName("数组组件返回true")
        void testIsArrayTrue() {
            java.lang.reflect.RecordComponent[] components = ArrayRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.isArray()).isTrue();
        }

        @Test
        @DisplayName("非数组组件返回false")
        void testIsArrayFalse() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent component = new RecordComponent(components[0]);
            assertThat(component.isArray()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同组件相等")
        void testEquals() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent c1 = new RecordComponent(components[0]);
            RecordComponent c2 = new RecordComponent(components[0]);
            assertThat(c1).isEqualTo(c2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent c = new RecordComponent(components[0]);
            assertThat(c).isEqualTo(c);
        }

        @Test
        @DisplayName("不同组件不相等")
        void testNotEquals() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent c1 = new RecordComponent(components[0]);
            RecordComponent c2 = new RecordComponent(components[1]);
            assertThat(c1).isNotEqualTo(c2);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同组件有相同hashCode")
        void testHashCode() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent c1 = new RecordComponent(components[0]);
            RecordComponent c2 = new RecordComponent(components[0]);
            assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含组件信息")
        void testToString() {
            java.lang.reflect.RecordComponent[] components = TestRecord.class.getRecordComponents();
            RecordComponent c = new RecordComponent(components[0]);
            String str = c.toString();
            assertThat(str).contains("RecordComponent");
            assertThat(str).contains("name");
            assertThat(str).contains("String");
        }
    }

    // Test helper types
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.RECORD_COMPONENT)
    @interface TestAnnotation {}

    record TestRecord(String name, int age) {}
    record GenericRecord(List<String> items) {}
    record AnnotatedRecord(@TestAnnotation String value) {}
    record ArrayRecord(String[] values) {}
}
