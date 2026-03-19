package cloud.opencode.base.reflect.sealed;

import org.junit.jupiter.api.*;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * PermittedSubclassesTest Tests
 * PermittedSubclassesTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("PermittedSubclasses 测试")
class PermittedSubclassesTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建PermittedSubclasses")
        void testCreate() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            assertThat(permitted).isNotNull();
        }

        @Test
        @DisplayName("非密封类抛出异常")
        void testCreateNonSealed() {
            assertThatThrownBy(() -> new PermittedSubclasses(String.class))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getSealedClass方法测试")
    class GetSealedClassTests {

        @Test
        @DisplayName("获取密封类")
        void testGetSealedClass() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            assertThat(permitted.getSealedClass()).isEqualTo(Shape.class);
        }
    }

    @Nested
    @DisplayName("getAll方法测试")
    class GetAllTests {

        @Test
        @DisplayName("获取所有许可子类")
        void testGetAll() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            List<Class<?>> all = permitted.getAll();
            assertThat(all).hasSize(3);
            assertThat(all).contains(Circle.class, Rectangle.class, Triangle.class);
        }

        @Test
        @DisplayName("返回不可修改列表")
        void testGetAllUnmodifiable() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            List<Class<?>> all = permitted.getAll();
            assertThatThrownBy(() -> all.add(String.class))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("size方法测试")
    class SizeTests {

        @Test
        @DisplayName("获取许可子类数量")
        void testSize() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            assertThat(permitted.size()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("isEmpty方法测试")
    class IsEmptyTests {

        @Test
        @DisplayName("有子类返回false")
        void testIsEmptyFalse() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            assertThat(permitted.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("isPermitted方法测试")
    class IsPermittedTests {

        @Test
        @DisplayName("许可子类返回true")
        void testIsPermittedTrue() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            assertThat(permitted.isPermitted(Circle.class)).isTrue();
        }

        @Test
        @DisplayName("非许可类返回false")
        void testIsPermittedFalse() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            assertThat(permitted.isPermitted(String.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("get方法测试")
    class GetTests {

        @Test
        @DisplayName("按索引获取子类")
        void testGet() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            Class<?> first = permitted.get(0);
            assertThat(first).isIn(Circle.class, Rectangle.class, Triangle.class);
        }

        @Test
        @DisplayName("索引越界抛出异常")
        void testGetOutOfBounds() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            assertThatThrownBy(() -> permitted.get(10))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("filter方法测试")
    class FilterTests {

        @Test
        @DisplayName("过滤子类")
        void testFilter() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            List<Class<?>> finals = permitted.filter(c -> java.lang.reflect.Modifier.isFinal(c.getModifiers()));
            assertThat(finals).contains(Circle.class, Rectangle.class, Triangle.class);
        }
    }

    @Nested
    @DisplayName("getFinalSubclasses方法测试")
    class GetFinalSubclassesTests {

        @Test
        @DisplayName("获取final子类")
        void testGetFinalSubclasses() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            List<Class<?>> finals = permitted.getFinalSubclasses();
            assertThat(finals).contains(Circle.class, Rectangle.class, Triangle.class);
        }
    }

    @Nested
    @DisplayName("getNonFinalSubclasses方法测试")
    class GetNonFinalSubclassesTests {

        @Test
        @DisplayName("获取非final子类")
        void testGetNonFinalSubclasses() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            List<Class<?>> nonFinals = permitted.getNonFinalSubclasses();
            assertThat(nonFinals).doesNotContain(Circle.class, Rectangle.class, Triangle.class);
        }
    }

    @Nested
    @DisplayName("getSealedSubclasses方法测试")
    class GetSealedSubclassesTests {

        @Test
        @DisplayName("获取密封子类")
        void testGetSealedSubclasses() {
            PermittedSubclasses permitted = new PermittedSubclasses(Animal.class);
            List<Class<?>> sealedSubs = permitted.getSealedSubclasses();
            assertThat(sealedSubs).contains(Mammal.class);
        }
    }

    @Nested
    @DisplayName("getRecordSubclasses方法测试")
    class GetRecordSubclassesTests {

        @Test
        @DisplayName("获取record子类")
        void testGetRecordSubclasses() {
            PermittedSubclasses permitted = new PermittedSubclasses(Result.class);
            List<Class<?>> records = permitted.getRecordSubclasses();
            assertThat(records).contains(Success.class, Failure.class);
        }
    }

    @Nested
    @DisplayName("getNames方法测试")
    class GetNamesTests {

        @Test
        @DisplayName("获取子类名称")
        void testGetNames() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            List<String> names = permitted.getNames();
            assertThat(names).hasSize(3);
        }
    }

    @Nested
    @DisplayName("getSimpleNames方法测试")
    class GetSimpleNamesTests {

        @Test
        @DisplayName("获取子类简单名称")
        void testGetSimpleNames() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            List<String> names = permitted.getSimpleNames();
            assertThat(names).contains("Circle", "Rectangle", "Triangle");
        }
    }

    @Nested
    @DisplayName("stream方法测试")
    class StreamTests {

        @Test
        @DisplayName("创建子类流")
        void testStream() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            Stream<Class<?>> stream = permitted.stream();
            assertThat(stream.count()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getAllRecursive方法测试")
    class GetAllRecursiveTests {

        @Test
        @DisplayName("递归获取所有子类")
        void testGetAllRecursive() {
            PermittedSubclasses permitted = new PermittedSubclasses(Animal.class);
            Set<Class<?>> all = permitted.getAllRecursive();
            assertThat(all).contains(Mammal.class, Bird.class, Dog.class, Cat.class);
        }
    }

    @Nested
    @DisplayName("getHierarchy方法测试")
    class GetHierarchyTests {

        @Test
        @DisplayName("获取层次结构")
        void testGetHierarchy() {
            PermittedSubclasses permitted = new PermittedSubclasses(Animal.class);
            PermittedSubclasses.HierarchyNode hierarchy = permitted.getHierarchy();
            assertThat(hierarchy).isNotNull();
            assertThat(hierarchy.getClazz()).isEqualTo(Animal.class);
        }
    }

    @Nested
    @DisplayName("iterator方法测试")
    class IteratorTests {

        @Test
        @DisplayName("获取迭代器")
        void testIterator() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            Iterator<Class<?>> iterator = permitted.iterator();
            int count = 0;
            while (iterator.hasNext()) {
                iterator.next();
                count++;
            }
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("支持for-each")
        void testForEach() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            int count = 0;
            for (Class<?> clazz : permitted) {
                count++;
            }
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            PermittedSubclasses permitted = new PermittedSubclasses(Shape.class);
            String str = permitted.toString();
            assertThat(str).contains("PermittedSubclasses");
            assertThat(str).contains("Shape");
        }
    }

    @Nested
    @DisplayName("HierarchyNode测试")
    class HierarchyNodeTests {

        @Test
        @DisplayName("获取类")
        void testGetClazz() {
            PermittedSubclasses.HierarchyNode node = new PermittedSubclasses.HierarchyNode(Animal.class);
            assertThat(node.getClazz()).isEqualTo(Animal.class);
        }

        @Test
        @DisplayName("获取子节点")
        void testGetChildren() {
            PermittedSubclasses.HierarchyNode node = new PermittedSubclasses.HierarchyNode(Animal.class);
            List<PermittedSubclasses.HierarchyNode> children = node.getChildren();
            assertThat(children).hasSize(2);  // Mammal, Bird
        }

        @Test
        @DisplayName("子节点列表不可修改")
        void testGetChildrenUnmodifiable() {
            PermittedSubclasses.HierarchyNode node = new PermittedSubclasses.HierarchyNode(Animal.class);
            List<PermittedSubclasses.HierarchyNode> children = node.getChildren();
            assertThatThrownBy(() -> children.add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("叶节点检查")
        void testIsLeaf() {
            PermittedSubclasses.HierarchyNode leafNode = new PermittedSubclasses.HierarchyNode(Circle.class);
            PermittedSubclasses.HierarchyNode sealedNode = new PermittedSubclasses.HierarchyNode(Animal.class);
            assertThat(leafNode.isLeaf()).isTrue();
            assertThat(sealedNode.isLeaf()).isFalse();
        }

        @Test
        @DisplayName("获取深度")
        void testGetDepth() {
            PermittedSubclasses.HierarchyNode node = new PermittedSubclasses.HierarchyNode(Animal.class);
            int depth = node.getDepth();
            assertThat(depth).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("叶节点深度为0")
        void testGetDepthLeaf() {
            PermittedSubclasses.HierarchyNode leafNode = new PermittedSubclasses.HierarchyNode(Circle.class);
            assertThat(leafNode.getDepth()).isEqualTo(0);
        }

        @Test
        @DisplayName("获取所有叶类")
        void testGetLeafClasses() {
            PermittedSubclasses.HierarchyNode node = new PermittedSubclasses.HierarchyNode(Animal.class);
            List<Class<?>> leaves = node.getLeafClasses();
            assertThat(leaves).contains(Dog.class, Cat.class, Bird.class);
        }

        @Test
        @DisplayName("toString输出树结构")
        void testToStringTree() {
            PermittedSubclasses.HierarchyNode node = new PermittedSubclasses.HierarchyNode(Animal.class);
            String str = node.toString();
            assertThat(str).contains("Animal");
            assertThat(str).contains("Mammal");
        }
    }

    // Test helper sealed hierarchies
    sealed interface Shape permits Circle, Rectangle, Triangle {}
    static final class Circle implements Shape {}
    static final class Rectangle implements Shape {}
    static final class Triangle implements Shape {}

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
}
