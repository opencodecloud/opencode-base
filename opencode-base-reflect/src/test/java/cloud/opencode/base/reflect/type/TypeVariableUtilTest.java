package cloud.opencode.base.reflect.type;

import org.junit.jupiter.api.*;

import java.lang.reflect.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeVariableUtilTest Tests
 * TypeVariableUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("TypeVariableUtil 测试")
class TypeVariableUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = TypeVariableUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("getTypeVariable方法测试")
    class GetTypeVariableTests {

        @Test
        @DisplayName("获取类型变量")
        void testGetTypeVariable() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(GenericClass.class, "T");
            assertThat(tv).isNotNull();
            assertThat(tv.getName()).isEqualTo("T");
        }

        @Test
        @DisplayName("获取不存在的类型变量返回null")
        void testGetTypeVariableNotFound() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(GenericClass.class, "X");
            assertThat(tv).isNull();
        }

        @Test
        @DisplayName("从非泛型类获取返回null")
        void testGetTypeVariableNonGeneric() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(String.class, "T");
            assertThat(tv).isNull();
        }
    }

    @Nested
    @DisplayName("getTypeVariableNames方法测试")
    class GetTypeVariableNamesTests {

        @Test
        @DisplayName("获取类型变量名列表")
        void testGetTypeVariableNames() {
            List<String> names = TypeVariableUtil.getTypeVariableNames(Map.class);
            assertThat(names).containsExactly("K", "V");
        }

        @Test
        @DisplayName("非泛型类返回空列表")
        void testGetTypeVariableNamesNonGeneric() {
            List<String> names = TypeVariableUtil.getTypeVariableNames(String.class);
            assertThat(names).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBounds方法测试")
    class GetBoundsTests {

        @Test
        @DisplayName("获取类型变量边界")
        void testGetBounds() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(BoundedClass.class, "T");
            Type[] bounds = TypeVariableUtil.getBounds(tv);
            assertThat(bounds).isNotEmpty();
            assertThat(bounds[0]).isEqualTo(Number.class);
        }
    }

    @Nested
    @DisplayName("hasBounds方法测试")
    class HasBoundsTests {

        @Test
        @DisplayName("有非Object边界返回true")
        void testHasBoundsTrue() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(BoundedClass.class, "T");
            assertThat(TypeVariableUtil.hasBounds(tv)).isTrue();
        }

        @Test
        @DisplayName("仅Object边界返回false")
        void testHasBoundsFalse() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(GenericClass.class, "T");
            assertThat(TypeVariableUtil.hasBounds(tv)).isFalse();
        }
    }

    @Nested
    @DisplayName("getUpperBound方法测试")
    class GetUpperBoundTests {

        @Test
        @DisplayName("获取上界")
        void testGetUpperBound() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(BoundedClass.class, "T");
            Type bound = TypeVariableUtil.getUpperBound(tv);
            assertThat(bound).isEqualTo(Number.class);
        }

        @Test
        @DisplayName("无边界返回Object")
        void testGetUpperBoundDefault() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(GenericClass.class, "T");
            Type bound = TypeVariableUtil.getUpperBound(tv);
            assertThat(bound).isEqualTo(Object.class);
        }
    }

    @Nested
    @DisplayName("getRawUpperBound方法测试")
    class GetRawUpperBoundTests {

        @Test
        @DisplayName("获取原始上界类")
        void testGetRawUpperBound() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(BoundedClass.class, "T");
            Class<?> bound = TypeVariableUtil.getRawUpperBound(tv);
            assertThat(bound).isEqualTo(Number.class);
        }
    }

    @Nested
    @DisplayName("containsTypeVariable方法测试")
    class ContainsTypeVariableTests {

        @Test
        @DisplayName("Class类型不包含类型变量")
        void testContainsTypeVariableClass() {
            assertThat(TypeVariableUtil.containsTypeVariable(String.class)).isFalse();
        }

        @Test
        @DisplayName("TypeVariable包含类型变量")
        void testContainsTypeVariableTypeVariable() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(GenericClass.class, "T");
            assertThat(TypeVariableUtil.containsTypeVariable(tv)).isTrue();
        }

        @Test
        @DisplayName("参数化类型包含类型变量")
        void testContainsTypeVariableParameterized() throws Exception {
            Type type = GenericFieldHolder.class.getDeclaredField("list").getGenericType();
            assertThat(TypeVariableUtil.containsTypeVariable(type)).isTrue();
        }

        @Test
        @DisplayName("具体参数化类型不包含类型变量")
        void testContainsTypeVariableConcreteParameterized() throws Exception {
            Type type = ConcreteFieldHolder.class.getDeclaredField("stringList").getGenericType();
            assertThat(TypeVariableUtil.containsTypeVariable(type)).isFalse();
        }
    }

    @Nested
    @DisplayName("collectTypeVariables方法测试")
    class CollectTypeVariablesTests {

        @Test
        @DisplayName("收集类型变量")
        void testCollectTypeVariables() {
            TypeVariable<?> tv = TypeVariableUtil.getTypeVariable(GenericClass.class, "T");
            Set<TypeVariable<?>> result = TypeVariableUtil.collectTypeVariables(tv);
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("从具体类型收集为空")
        void testCollectTypeVariablesEmpty() {
            Set<TypeVariable<?>> result = TypeVariableUtil.collectTypeVariables(String.class);
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTypeArgumentsByName方法测试")
    class GetTypeArgumentsByNameTests {

        @Test
        @DisplayName("获取类型参数映射")
        void testGetTypeArgumentsByName() throws Exception {
            Type type = StringList.class.getGenericSuperclass();
            Map<String, Type> map = TypeVariableUtil.getTypeArgumentsByName(type);
            assertThat(map).containsKey("T");
            assertThat(map.get("T")).isEqualTo(String.class);
        }

        @Test
        @DisplayName("非参数化类型返回空映射")
        void testGetTypeArgumentsByNameNonParameterized() {
            Map<String, Type> map = TypeVariableUtil.getTypeArgumentsByName(String.class);
            assertThat(map).isEmpty();
        }
    }

    // Test helper classes
    static class GenericClass<T> {
        T value;
    }

    static class BoundedClass<T extends Number> {
        T value;
    }

    static class GenericFieldHolder<T> {
        List<T> list;
    }

    static class ConcreteFieldHolder {
        List<String> stringList;
    }

    static class StringList extends GenericClass<String> {
    }
}
