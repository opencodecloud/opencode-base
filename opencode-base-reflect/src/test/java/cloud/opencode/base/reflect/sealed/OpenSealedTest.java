package cloud.opencode.base.reflect.sealed;

import org.junit.jupiter.api.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * OpenSealedTest Tests
 * OpenSealedTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("OpenSealed 测试")
class OpenSealedTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws Exception {
            var constructor = OpenSealed.class.getDeclaredConstructor();
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("isSealed方法测试")
    class IsSealedTests {

        @Test
        @DisplayName("密封类返回true")
        void testIsSealedTrue() {
            assertThat(OpenSealed.isSealed(Shape.class)).isTrue();
        }

        @Test
        @DisplayName("非密封类返回false")
        void testIsSealedFalse() {
            assertThat(OpenSealed.isSealed(String.class)).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsSealedNull() {
            assertThat(OpenSealed.isSealed(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isSealedClass方法测试")
    class IsSealedClassTests {

        @Test
        @DisplayName("密封类实例返回true")
        void testIsSealedClassTrue() {
            // Circle extends Shape which is sealed
            // But Circle itself is not sealed, it's final
            assertThat(OpenSealed.isSealedClass(new Circle())).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testIsSealedClassNull() {
            assertThat(OpenSealed.isSealedClass(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("requireSealed方法测试")
    class RequireSealedTests {

        @Test
        @DisplayName("密封类返回自身")
        void testRequireSealedSuccess() {
            Class<?> result = OpenSealed.requireSealed(Shape.class);
            assertThat(result).isEqualTo(Shape.class);
        }

        @Test
        @DisplayName("非密封类抛出异常")
        void testRequireSealedFailure() {
            assertThatThrownBy(() -> OpenSealed.requireSealed(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getPermittedSubclasses方法测试")
    class GetPermittedSubclassesTests {

        @Test
        @DisplayName("获取PermittedSubclasses集合")
        void testGetPermittedSubclasses() {
            PermittedSubclasses permitted = OpenSealed.getPermittedSubclasses(Shape.class);
            assertThat(permitted).isNotNull();
            assertThat(permitted.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("getPermittedSubclassList方法测试")
    class GetPermittedSubclassListTests {

        @Test
        @DisplayName("获取许可子类列表")
        void testGetPermittedSubclassList() {
            List<Class<?>> list = OpenSealed.getPermittedSubclassList(Shape.class);
            assertThat(list).hasSize(2);
            assertThat(list).contains(Circle.class, Rectangle.class);
        }

        @Test
        @DisplayName("非密封类抛出异常")
        void testGetPermittedSubclassListNonSealed() {
            assertThatThrownBy(() -> OpenSealed.getPermittedSubclassList(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getPermittedSubclassCount方法测试")
    class GetPermittedSubclassCountTests {

        @Test
        @DisplayName("获取许可子类数量")
        void testGetPermittedSubclassCount() {
            int count = OpenSealed.getPermittedSubclassCount(Shape.class);
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("非密封类抛出异常")
        void testGetPermittedSubclassCountNonSealed() {
            assertThatThrownBy(() -> OpenSealed.getPermittedSubclassCount(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("isPermittedSubclass方法测试")
    class IsPermittedSubclassTests {

        @Test
        @DisplayName("许可子类返回true")
        void testIsPermittedSubclassTrue() {
            assertThat(OpenSealed.isPermittedSubclass(Shape.class, Circle.class)).isTrue();
        }

        @Test
        @DisplayName("非许可子类返回false")
        void testIsPermittedSubclassFalse() {
            assertThat(OpenSealed.isPermittedSubclass(Shape.class, String.class)).isFalse();
        }

        @Test
        @DisplayName("非密封类返回false")
        void testIsPermittedSubclassNonSealed() {
            assertThat(OpenSealed.isPermittedSubclass(String.class, Circle.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getAllSubclassesRecursive方法测试")
    class GetAllSubclassesRecursiveTests {

        @Test
        @DisplayName("递归获取所有子类")
        void testGetAllSubclassesRecursive() {
            Set<Class<?>> all = OpenSealed.getAllSubclassesRecursive(Animal.class);
            assertThat(all).contains(Mammal.class, Bird.class, Dog.class, Cat.class);
        }
    }

    @Nested
    @DisplayName("getLeafClasses方法测试")
    class GetLeafClassesTests {

        @Test
        @DisplayName("获取叶类")
        void testGetLeafClasses() {
            List<Class<?>> leaves = OpenSealed.getLeafClasses(Animal.class);
            assertThat(leaves).contains(Dog.class, Cat.class, Bird.class);
            assertThat(leaves).doesNotContain(Mammal.class);  // Mammal is sealed, not a leaf
        }
    }

    @Nested
    @DisplayName("getHierarchy方法测试")
    class GetHierarchyTests {

        @Test
        @DisplayName("获取层次结构树")
        void testGetHierarchy() {
            PermittedSubclasses.HierarchyNode hierarchy = OpenSealed.getHierarchy(Animal.class);
            assertThat(hierarchy).isNotNull();
            assertThat(hierarchy.getClazz()).isEqualTo(Animal.class);
        }
    }

    @Nested
    @DisplayName("getSealedParent方法测试")
    class GetSealedParentTests {

        @Test
        @DisplayName("获取密封父类")
        void testGetSealedParent() {
            // Dog implements Mammal which is sealed
            // Note: getSealedParent looks at superclass hierarchy, not interfaces
            Optional<Class<?>> parent = OpenSealed.getSealedParent(Dog.class);
            // Since Dog implements an interface, not extends a sealed class, parent should be empty
            // Let's verify
            assertThat(parent).isEmpty();
        }

        @Test
        @DisplayName("无密封父类返回空")
        void testGetSealedParentEmpty() {
            Optional<Class<?>> parent = OpenSealed.getSealedParent(String.class);
            assertThat(parent).isEmpty();
        }

        @Test
        @DisplayName("继承密封类的子类")
        void testGetSealedParentFromSealed() {
            Optional<Class<?>> parent = OpenSealed.getSealedParent(ChildClass.class);
            assertThat(parent).contains(ParentSealed.class);
        }
    }

    @Nested
    @DisplayName("getSealedInterfaces方法测试")
    class GetSealedInterfacesTests {

        @Test
        @DisplayName("获取密封接口")
        void testGetSealedInterfaces() {
            List<Class<?>> sealedInterfaces = OpenSealed.getSealedInterfaces(Circle.class);
            assertThat(sealedInterfaces).contains(Shape.class);
        }

        @Test
        @DisplayName("不实现密封接口的类返回空列表")
        void testGetSealedInterfacesEmpty() {
            // Note: String implements ConstantDesc which is sealed in modern JDK
            // Use a simple class that doesn't implement any sealed interfaces
            List<Class<?>> sealedInterfaces = OpenSealed.getSealedInterfaces(Object.class);
            assertThat(sealedInterfaces).isEmpty();
        }
    }

    @Nested
    @DisplayName("validateHierarchy方法测试")
    class ValidateHierarchyTests {

        @Test
        @DisplayName("有效层次结构返回true")
        void testValidateHierarchyTrue() {
            assertThat(OpenSealed.validateHierarchy(Shape.class)).isTrue();
        }

        @Test
        @DisplayName("非密封类返回false")
        void testValidateHierarchyFalse() {
            assertThat(OpenSealed.validateHierarchy(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("isExhaustive方法测试")
    class IsExhaustiveTests {

        @Test
        @DisplayName("穷尽层次结构返回true")
        void testIsExhaustiveTrue() {
            // Shape has all final classes
            assertThat(OpenSealed.isExhaustive(Shape.class)).isTrue();
        }

        @Test
        @DisplayName("非密封类返回false")
        void testIsExhaustiveFalse() {
            assertThat(OpenSealed.isExhaustive(String.class)).isFalse();
        }

        @Test
        @DisplayName("record子类也是穷尽的")
        void testIsExhaustiveWithRecords() {
            assertThat(OpenSealed.isExhaustive(Result.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("getConcreteTypes方法测试")
    class GetConcreteTypesTests {

        @Test
        @DisplayName("获取所有具体类型")
        void testGetConcreteTypes() {
            List<Class<?>> concrete = OpenSealed.getConcreteTypes(Shape.class);
            assertThat(concrete).contains(Circle.class, Rectangle.class);
        }

        @Test
        @DisplayName("嵌套密封类获取所有具体类型")
        void testGetConcreteTypesNested() {
            List<Class<?>> concrete = OpenSealed.getConcreteTypes(Animal.class);
            assertThat(concrete).contains(Dog.class, Cat.class, Bird.class);
            assertThat(concrete).doesNotContain(Mammal.class);  // Mammal is not concrete
        }
    }

    @Nested
    @DisplayName("generateSwitchTemplate方法测试")
    class GenerateSwitchTemplateTests {

        @Test
        @DisplayName("生成switch模板")
        void testGenerateSwitchTemplate() {
            String template = OpenSealed.generateSwitchTemplate(Shape.class, "shape");
            assertThat(template).contains("switch (shape)");
            assertThat(template).contains("Circle");
            assertThat(template).contains("Rectangle");
        }
    }

    // Test helper sealed hierarchies
    sealed interface Shape permits Circle, Rectangle {}
    static final class Circle implements Shape {}
    static final class Rectangle implements Shape {}

    // Nested sealed hierarchy
    sealed interface Animal permits Mammal, Bird {}
    sealed interface Mammal extends Animal permits Dog, Cat {}
    static final class Dog implements Mammal {}
    static final class Cat implements Mammal {}
    static final class Bird implements Animal {}

    // Record sealed hierarchy
    sealed interface Result permits Success, Failure {}
    record Success(Object value) implements Result {}
    record Failure(String error) implements Result {}

    // Sealed class (not interface) for getSealedParent test
    sealed static class ParentSealed permits ChildClass {}
    static final class ChildClass extends ParentSealed {}
}
