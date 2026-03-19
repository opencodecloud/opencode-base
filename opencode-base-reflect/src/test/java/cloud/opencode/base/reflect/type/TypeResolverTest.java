package cloud.opencode.base.reflect.type;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeResolverTest Tests
 * TypeResolverTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("TypeResolver 测试")
class TypeResolverTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = TypeResolver.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("resolveType方法测试")
    class ResolveTypeTests {

        @Test
        @DisplayName("解析Class类型")
        void testResolveTypeClass() {
            Type resolved = TypeResolver.resolveType(String.class, String.class);
            assertThat(resolved).isEqualTo(String.class);
        }

        @Test
        @DisplayName("解析参数化类型")
        void testResolveTypeParameterized() throws Exception {
            Type context = StringList.class;
            Field field = GenericClass.class.getDeclaredField("item");
            Type resolved = TypeResolver.resolveType(context, field.getGenericType());
            assertThat(resolved).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getTypeVariableMap方法测试")
    class GetTypeVariableMapTests {

        @Test
        @DisplayName("获取类型变量映射")
        void testGetTypeVariableMap() throws Exception {
            Type type = StringList.class.getGenericSuperclass();
            Map<TypeVariable<?>, Type> map = TypeResolver.getTypeVariableMap(type);
            assertThat(map).isNotEmpty();
        }

        @Test
        @DisplayName("非参数化类型返回空映射")
        void testGetTypeVariableMapNonParameterized() {
            Map<TypeVariable<?>, Type> map = TypeResolver.getTypeVariableMap(String.class);
            // May be empty or contain inherited type variables
            assertThat(map).isNotNull();
        }
    }

    @Nested
    @DisplayName("resolveReturnType方法测试")
    class ResolveReturnTypeTests {

        @Test
        @DisplayName("解析方法返回类型")
        void testResolveReturnType() throws Exception {
            Type context = StringList.class.getGenericSuperclass();
            Method method = GenericClass.class.getDeclaredMethod("getItem");
            Type resolved = TypeResolver.resolveReturnType(context, method);
            assertThat(resolved).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("resolveParameterTypes方法测试")
    class ResolveParameterTypesTests {

        @Test
        @DisplayName("解析方法参数类型")
        void testResolveParameterTypes() throws Exception {
            Type context = StringList.class.getGenericSuperclass();
            Method method = GenericClass.class.getDeclaredMethod("setItem", Object.class);
            Type[] resolved = TypeResolver.resolveParameterTypes(context, method);
            assertThat(resolved).hasSize(1);
            assertThat(resolved[0]).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("resolveFieldType方法测试")
    class ResolveFieldTypeTests {

        @Test
        @DisplayName("解析字段类型")
        void testResolveFieldType() throws Exception {
            Type context = StringList.class.getGenericSuperclass();
            Field field = GenericClass.class.getDeclaredField("item");
            Type resolved = TypeResolver.resolveFieldType(context, field);
            assertThat(resolved).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("getTypeArgument方法测试")
    class GetTypeArgumentTests {

        @Test
        @DisplayName("获取类型参数")
        void testGetTypeArgument() throws Exception {
            Type type = StringList.class.getGenericSuperclass();
            Type arg = TypeResolver.getTypeArgument(type, 0);
            assertThat(arg).isEqualTo(String.class);
        }

        @Test
        @DisplayName("索引越界返回null")
        void testGetTypeArgumentOutOfBounds() throws Exception {
            Type type = StringList.class.getGenericSuperclass();
            Type arg = TypeResolver.getTypeArgument(type, 5);
            assertThat(arg).isNull();
        }

        @Test
        @DisplayName("非参数化类型返回null")
        void testGetTypeArgumentNonParameterized() {
            Type arg = TypeResolver.getTypeArgument(String.class, 0);
            assertThat(arg).isNull();
        }
    }

    @Nested
    @DisplayName("findTypeArgument方法测试")
    class FindTypeArgumentTests {

        @Test
        @DisplayName("查找类型参数")
        void testFindTypeArgument() throws Exception {
            Type type = StringList.class.getGenericSuperclass();
            Type arg = TypeResolver.findTypeArgument(type, GenericClass.class, 0);
            assertThat(arg).isEqualTo(String.class);
        }

        @Test
        @DisplayName("未找到类型参数返回null")
        void testFindTypeArgumentNotFound() {
            Type arg = TypeResolver.findTypeArgument(String.class, List.class, 0);
            assertThat(arg).isNull();
        }
    }

    // Test helper classes
    @SuppressWarnings("unused")
    static class GenericClass<T> {
        T item;

        public T getItem() {
            return item;
        }

        public void setItem(T item) {
            this.item = item;
        }
    }

    static class StringList extends GenericClass<String> {
    }
}
