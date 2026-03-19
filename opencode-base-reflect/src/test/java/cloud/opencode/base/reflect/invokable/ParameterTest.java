package cloud.opencode.base.reflect.invokable;

import cloud.opencode.base.reflect.type.TypeToken;
import org.junit.jupiter.api.*;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ParameterTest Tests
 * ParameterTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("Parameter 测试")
class ParameterTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建Parameter")
        void testCreate() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter).isNotNull();
        }

        @Test
        @DisplayName("null参数抛出异常")
        void testCreateNullParameter() {
            assertThatThrownBy(() -> new Parameter(null, 0))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getType方法测试")
    class GetTypeTests {

        @Test
        @DisplayName("获取参数类型TypeToken")
        void testGetType() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            TypeToken<?> type = parameter.getType();
            assertThat(type.getRawType()).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getRawType方法测试")
    class GetRawTypeTests {

        @Test
        @DisplayName("获取原始参数类型")
        void testGetRawType() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[1];
            Parameter parameter = new Parameter(param, 1);
            assertThat(parameter.getRawType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取参数名")
        void testGetName() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            // Name may or may not be present depending on compilation options
            assertThat(parameter.getName()).isNotNull();
        }
    }

    @Nested
    @DisplayName("isNamePresent方法测试")
    class IsNamePresentTests {

        @Test
        @DisplayName("检查参数名是否存在")
        void testIsNamePresent() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            // Just verify it returns a boolean
            assertThat(parameter.isNamePresent()).isIn(true, false);
        }
    }

    @Nested
    @DisplayName("getIndex方法测试")
    class GetIndexTests {

        @Test
        @DisplayName("获取参数索引")
        void testGetIndex() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[1];
            Parameter parameter = new Parameter(param, 1);
            assertThat(parameter.getIndex()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("isVarArgs方法测试")
    class IsVarArgsTests {

        @Test
        @DisplayName("可变参数返回true")
        void testIsVarArgsTrue() throws Exception {
            Method method = TestClass.class.getMethod("varArgsMethod", String[].class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.isVarArgs()).isTrue();
        }

        @Test
        @DisplayName("非可变参数返回false")
        void testIsVarArgsFalse() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.isVarArgs()).isFalse();
        }
    }

    @Nested
    @DisplayName("isImplicit方法测试")
    class IsImplicitTests {

        @Test
        @DisplayName("检查是否为隐式参数")
        void testIsImplicit() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.isImplicit()).isFalse();
        }
    }

    @Nested
    @DisplayName("isSynthetic方法测试")
    class IsSyntheticTests {

        @Test
        @DisplayName("检查是否为合成参数")
        void testIsSynthetic() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.isSynthetic()).isFalse();
        }
    }

    @Nested
    @DisplayName("getUnderlying方法测试")
    class GetUnderlyingTests {

        @Test
        @DisplayName("获取底层参数")
        void testGetUnderlying() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.getUnderlying()).isSameAs(param);
        }
    }

    @Nested
    @DisplayName("注解方法测试")
    class AnnotationTests {

        @Test
        @DisplayName("检查注解是否存在")
        void testIsAnnotationPresent() throws Exception {
            Method method = TestClass.class.getMethod("annotatedParamMethod", String.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.isAnnotationPresent(TestAnnotation.class)).isTrue();
        }

        @Test
        @DisplayName("获取注解")
        void testGetAnnotation() throws Exception {
            Method method = TestClass.class.getMethod("annotatedParamMethod", String.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            TestAnnotation annotation = parameter.getAnnotation(TestAnnotation.class);
            assertThat(annotation).isNotNull();
        }

        @Test
        @DisplayName("获取所有注解")
        void testGetAnnotations() throws Exception {
            Method method = TestClass.class.getMethod("annotatedParamMethod", String.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.getAnnotations()).isNotEmpty();
        }

        @Test
        @DisplayName("获取声明的注解")
        void testGetDeclaredAnnotations() throws Exception {
            Method method = TestClass.class.getMethod("annotatedParamMethod", String.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.getDeclaredAnnotations()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同参数相等")
        void testEquals() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter1 = new Parameter(param, 0);
            Parameter parameter2 = new Parameter(param, 0);
            assertThat(parameter1).isEqualTo(parameter2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter).isEqualTo(parameter);
        }

        @Test
        @DisplayName("不同索引不相等")
        void testNotEqualsDifferentIndex() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter1 = new Parameter(param, 0);
            Parameter parameter2 = new Parameter(param, 1);
            assertThat(parameter1).isNotEqualTo(parameter2);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同参数有相同hashCode")
        void testHashCode() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter1 = new Parameter(param, 0);
            Parameter parameter2 = new Parameter(param, 0);
            assertThat(parameter1.hashCode()).isEqualTo(parameter2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含参数信息")
        void testToString() throws Exception {
            Method method = TestClass.class.getMethod("method", String.class, int.class);
            java.lang.reflect.Parameter param = method.getParameters()[0];
            Parameter parameter = new Parameter(param, 0);
            assertThat(parameter.toString()).contains("Parameter");
        }
    }

    // Test helper classes
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface TestAnnotation {}

    @SuppressWarnings("unused")
    static class TestClass {
        public void method(String str, int num) {}
        public void varArgsMethod(String... args) {}
        public void annotatedParamMethod(@TestAnnotation String param) {}
    }
}
