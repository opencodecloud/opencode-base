package cloud.opencode.base.reflect.sealed;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * SealedUtilTest Tests
 * SealedUtilTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("SealedUtil 测试")
class SealedUtilTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = SealedUtil.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isSealed方法测试")
    class IsSealedTests {

        @Test
        @DisplayName("密封类返回true")
        void testIsSealedTrue() {
            assertThat(SealedUtil.isSealed(Shape.class)).isTrue();
        }

        @Test
        @DisplayName("非密封类返回false")
        void testIsSealedFalse() {
            assertThat(SealedUtil.isSealed(String.class)).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsSealedNull() {
            assertThat(SealedUtil.isSealed(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSealedClass方法测试")
    class IsSealedClassTests {

        @Test
        @DisplayName("密封类实例返回false（Circle是final非sealed）")
        void testIsSealedClassFinalSubclass() {
            // Circle is a final class that implements sealed Shape, but Circle itself is not sealed
            assertThat(SealedUtil.isSealedClass(new Circle())).isFalse();
        }

        @Test
        @DisplayName("非密封类实例返回false")
        void testIsSealedClassFalse() {
            assertThat(SealedUtil.isSealedClass("test")).isFalse();
        }
    }

    @Nested
    @DisplayName("requireSealed方法测试")
    class RequireSealedTests {

        @Test
        @DisplayName("密封类不抛异常")
        void testRequireSealedSuccess() {
            assertThatCode(() -> SealedUtil.requireSealed(Shape.class)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("非密封类抛出异常")
        void testRequireSealedFailure() {
            assertThatThrownBy(() -> SealedUtil.requireSealed(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getPermittedSubclasses方法测试")
    class GetPermittedSubclassesTests {

        @Test
        @DisplayName("获取许可子类")
        void testGetPermittedSubclasses() {
            Class<?>[] permitted = SealedUtil.getPermittedSubclasses(Shape.class);
            assertThat(permitted).hasSize(2);
            assertThat(permitted).contains(Circle.class, Rectangle.class);
        }
    }

    @Nested
    @DisplayName("getPermittedSubclassList方法测试")
    class GetPermittedSubclassListTests {

        @Test
        @DisplayName("获取许可子类列表")
        void testGetPermittedSubclassList() {
            List<Class<?>> permitted = SealedUtil.getPermittedSubclassList(Shape.class);
            assertThat(permitted).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getPermittedSubclassCount方法测试")
    class GetPermittedSubclassCountTests {

        @Test
        @DisplayName("获取许可子类数量")
        void testGetPermittedSubclassCount() {
            assertThat(SealedUtil.getPermittedSubclassCount(Shape.class)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("isPermittedSubclass方法测试")
    class IsPermittedSubclassTests {

        @Test
        @DisplayName("许可子类返回true")
        void testIsPermittedSubclassTrue() {
            assertThat(SealedUtil.isPermittedSubclass(Shape.class, Circle.class)).isTrue();
        }

        @Test
        @DisplayName("非许可子类返回false")
        void testIsPermittedSubclassFalse() {
            assertThat(SealedUtil.isPermittedSubclass(Shape.class, String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllSubclassesRecursive方法测试")
    class GetAllSubclassesRecursiveTests {

        @Test
        @DisplayName("递归获取所有子类")
        void testGetAllSubclassesRecursive() {
            Set<Class<?>> all = SealedUtil.getAllSubclassesRecursive(Shape.class);
            assertThat(all).contains(Circle.class, Rectangle.class);
        }
    }

    @Nested
    @DisplayName("getLeafClasses方法测试")
    class GetLeafClassesTests {

        @Test
        @DisplayName("获取叶类")
        void testGetLeafClasses() {
            List<Class<?>> leaves = SealedUtil.getLeafClasses(Shape.class);
            assertThat(leaves).contains(Circle.class, Rectangle.class);
        }
    }

    @Nested
    @DisplayName("getConcreteTypes方法测试")
    class GetConcreteTypesTests {

        @Test
        @DisplayName("获取具体类型")
        void testGetConcreteTypes() {
            List<Class<?>> concrete = SealedUtil.getConcreteTypes(Shape.class);
            assertThat(concrete).contains(Circle.class, Rectangle.class);
        }
    }

    @Nested
    @DisplayName("hasSealedParent方法测试")
    class HasSealedParentTests {

        @Test
        @DisplayName("实现密封接口不算有密封父类")
        void testHasSealedParentInterface() {
            // hasSealedParent only checks superclass chain, not interfaces
            assertThat(SealedUtil.hasSealedParent(Circle.class)).isFalse();
        }

        @Test
        @DisplayName("无密封父类返回false")
        void testHasSealedParentFalse() {
            assertThat(SealedUtil.hasSealedParent(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getSealedParent方法测试")
    class GetSealedParentTests {

        @Test
        @DisplayName("实现密封接口不返回父类")
        void testGetSealedParentInterface() {
            // getSealedParent only checks superclass chain, not interfaces
            Optional<Class<?>> parent = SealedUtil.getSealedParent(Circle.class);
            assertThat(parent).isEmpty();
        }

        @Test
        @DisplayName("无密封父类返回空")
        void testGetSealedParentEmpty() {
            Optional<Class<?>> parent = SealedUtil.getSealedParent(String.class);
            assertThat(parent).isEmpty();
        }
    }

    @Nested
    @DisplayName("validateHierarchy方法测试")
    class ValidateHierarchyTests {

        @Test
        @DisplayName("有效层次结构返回true")
        void testValidateHierarchyTrue() {
            assertThat(SealedUtil.validateHierarchy(Shape.class)).isTrue();
        }

        @Test
        @DisplayName("非密封类返回false")
        void testValidateHierarchyFalse() {
            assertThat(SealedUtil.validateHierarchy(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getHierarchyDepth方法测试")
    class GetHierarchyDepthTests {

        @Test
        @DisplayName("获取层次结构深度")
        void testGetHierarchyDepth() {
            int depth = SealedUtil.getHierarchyDepth(Shape.class);
            assertThat(depth).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("非密封类深度为0")
        void testGetHierarchyDepthNonSealed() {
            int depth = SealedUtil.getHierarchyDepth(String.class);
            assertThat(depth).isZero();
        }
    }

    @Nested
    @DisplayName("getTotalTypeCount方法测试")
    class GetTotalTypeCountTests {

        @Test
        @DisplayName("获取类型总数")
        void testGetTotalTypeCount() {
            int count = SealedUtil.getTotalTypeCount(Shape.class);
            assertThat(count).isGreaterThanOrEqualTo(3); // Shape + Circle + Rectangle
        }
    }

    @Nested
    @DisplayName("缓存管理测试")
    class CacheManagementTests {

        @Test
        @DisplayName("清除缓存")
        void testClearCache() {
            SealedUtil.getPermittedSubclasses(Shape.class);
            assertThatCode(() -> SealedUtil.clearCache()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("清除特定类的缓存")
        void testClearCacheForClass() {
            SealedUtil.getPermittedSubclasses(Shape.class);
            assertThatCode(() -> SealedUtil.clearCache(Shape.class)).doesNotThrowAnyException();
        }
    }

    // Test helper sealed hierarchy
    sealed interface Shape permits Circle, Rectangle {}

    static final class Circle implements Shape {}

    static final class Rectangle implements Shape {}
}
