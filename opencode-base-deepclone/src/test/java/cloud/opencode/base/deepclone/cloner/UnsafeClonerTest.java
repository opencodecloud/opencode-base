package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.annotation.CloneIgnore;
import cloud.opencode.base.deepclone.annotation.CloneReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * UnsafeCloner 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("UnsafeCloner 测试")
class UnsafeClonerTest {

    private UnsafeCloner cloner;

    @BeforeEach
    void setUp() {
        assumeTrue(UnsafeCloner.isAvailable(), "Unsafe is not available");
        cloner = UnsafeCloner.create();
    }

    // Test entities
    public static class SimpleEntity {
        private String name;
        private int value;

        public SimpleEntity() {}

        public SimpleEntity(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class NestedEntity {
        private String id;
        private SimpleEntity inner;

        public NestedEntity() {}

        public NestedEntity(String id, SimpleEntity inner) {
            this.id = id;
            this.inner = inner;
        }

        public String getId() { return id; }
        public SimpleEntity getInner() { return inner; }
    }

    public static class NoDefaultConstructor {
        private final String name;
        private final int value;

        public NoDefaultConstructor(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    public static class AnnotatedEntity {
        private String name;

        @CloneIgnore
        private String ignored;

        @CloneReference
        private Object shared;

        public AnnotatedEntity() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIgnored() { return ignored; }
        public void setIgnored(String ignored) { this.ignored = ignored; }
        public Object getShared() { return shared; }
        public void setShared(Object shared) { this.shared = shared; }
    }

    public static class CircularA {
        private String name;
        private CircularB ref;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public CircularB getRef() { return ref; }
        public void setRef(CircularB ref) { this.ref = ref; }
    }

    public static class CircularB {
        private String name;
        private CircularA ref;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public CircularA getRef() { return ref; }
        public void setRef(CircularA ref) { this.ref = ref; }
    }

    public record TestRecord(String name, int value) {}

    @Nested
    @DisplayName("isAvailable() 测试")
    class AvailabilityTests {

        @Test
        @DisplayName("检查Unsafe可用性")
        void testIsAvailable() {
            // This test will run regardless of availability
            boolean available = UnsafeCloner.isAvailable();
            assertThat(available).isTrue(); // Should be true on standard JVMs
        }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create() 创建克隆器")
        void testCreate() {
            UnsafeCloner cloner = UnsafeCloner.create();

            assertThat(cloner).isNotNull();
            assertThat(cloner.getStrategyName()).isEqualTo("unsafe");
        }
    }

    @Nested
    @DisplayName("allocateInstanceStatic() 测试")
    class AllocateInstanceTests {

        @Test
        @DisplayName("不调用构造函数分配实例")
        void testAllocateInstance() {
            NoDefaultConstructor instance = UnsafeCloner.allocateInstanceStatic(NoDefaultConstructor.class);

            assertThat(instance).isNotNull();
            // Fields should have default values (not constructor values)
            assertThat(instance.getName()).isNull();
            assertThat(instance.getValue()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆null返回null")
        void testCloneNull() {
            Object result = cloner.clone(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("克隆简单对象")
        void testCloneSimpleObject() {
            SimpleEntity original = new SimpleEntity("test", 123);

            SimpleEntity cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("test");
            assertThat(cloned.getValue()).isEqualTo(123);
        }

        @Test
        @DisplayName("克隆嵌套对象")
        void testCloneNestedObject() {
            SimpleEntity inner = new SimpleEntity("inner", 100);
            NestedEntity original = new NestedEntity("outer", inner);

            NestedEntity cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getInner()).isNotSameAs(original.getInner());
            assertThat(cloned.getInner().getName()).isEqualTo("inner");
        }

        @Test
        @DisplayName("克隆无默认构造函数对象")
        void testCloneNoDefaultConstructor() {
            NoDefaultConstructor original = new NoDefaultConstructor("test", 123);

            NoDefaultConstructor cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("test");
            assertThat(cloned.getValue()).isEqualTo(123);
        }

        @Test
        @DisplayName("克隆数组")
        void testCloneArray() {
            int[] original = {1, 2, 3, 4, 5};

            int[] cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("克隆对象数组")
        void testCloneObjectArray() {
            SimpleEntity[] original = {
                    new SimpleEntity("a", 1),
                    new SimpleEntity("b", 2)
            };

            SimpleEntity[] cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned[0]).isNotSameAs(original[0]);
            assertThat(cloned[0].getName()).isEqualTo("a");
        }

        @Test
        @DisplayName("克隆List")
        void testCloneList() {
            List<SimpleEntity> original = new ArrayList<>();
            original.add(new SimpleEntity("a", 1));
            original.add(new SimpleEntity("b", 2));

            List<SimpleEntity> cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).hasSize(2);
            assertThat(cloned.get(0)).isNotSameAs(original.get(0));
        }

        @Test
        @DisplayName("克隆Map")
        void testCloneMap() {
            Map<String, SimpleEntity> original = new HashMap<>();
            original.put("key1", new SimpleEntity("a", 1));

            Map<String, SimpleEntity> cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.get("key1")).isNotSameAs(original.get("key1"));
            assertThat(cloned.get("key1").getName()).isEqualTo("a");
        }

        @Test
        @DisplayName("克隆Record")
        void testCloneRecord() {
            TestRecord original = new TestRecord("test", 123);

            TestRecord cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.name()).isEqualTo("test");
            assertThat(cloned.value()).isEqualTo(123);
        }

        @Test
        @DisplayName("不可变类型不克隆")
        void testImmutableNotCloned() {
            String original = "immutable string";

            String cloned = cloner.clone(original);

            assertThat(cloned).isSameAs(original);
        }
    }

    @Nested
    @DisplayName("循环引用测试")
    class CircularReferenceTests {

        @Test
        @DisplayName("处理循环引用")
        void testCircularReference() {
            CircularA a = new CircularA();
            CircularB b = new CircularB();
            a.setName("A");
            a.setRef(b);
            b.setName("B");
            b.setRef(a);

            CircularA clonedA = cloner.clone(a);

            assertThat(clonedA).isNotSameAs(a);
            assertThat(clonedA.getRef()).isNotSameAs(b);
            assertThat(clonedA.getRef().getRef()).isSameAs(clonedA);
        }
    }

    @Nested
    @DisplayName("注解处理测试")
    class AnnotationTests {

        @Test
        @DisplayName("@CloneIgnore 字段被忽略")
        void testCloneIgnore() {
            AnnotatedEntity original = new AnnotatedEntity();
            original.setName("test");
            original.setIgnored("ignored-value");

            AnnotatedEntity cloned = cloner.clone(original);

            assertThat(cloned.getName()).isEqualTo("test");
            assertThat(cloned.getIgnored()).isNull();
        }

        @Test
        @DisplayName("@CloneReference 字段浅拷贝")
        void testCloneReference() {
            Object sharedObject = new Object();
            AnnotatedEntity original = new AnnotatedEntity();
            original.setName("test");
            original.setShared(sharedObject);

            AnnotatedEntity cloned = cloner.clone(original);

            assertThat(cloned.getShared()).isSameAs(sharedObject);
        }
    }

    @Nested
    @DisplayName("clone(T, CloneContext) 测试")
    class CloneWithContextTests {

        @Test
        @DisplayName("使用自定义上下文克隆")
        void testCloneWithContext() {
            SimpleEntity original = new SimpleEntity("test", 123);
            CloneContext context = CloneContext.create(50);

            SimpleEntity cloned = cloner.clone(original, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("Unsafe可用时支持所有类型")
        void testSupportsAllTypes() {
            assertThat(cloner.supports(SimpleEntity.class)).isTrue();
            assertThat(cloner.supports(String.class)).isTrue();
            assertThat(cloner.supports(int[].class)).isTrue();
            assertThat(cloner.supports(NoDefaultConstructor.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("getStrategyName() 测试")
    class StrategyNameTests {

        @Test
        @DisplayName("返回unsafe")
        void testStrategyName() {
            assertThat(cloner.getStrategyName()).isEqualTo("unsafe");
        }
    }
}
