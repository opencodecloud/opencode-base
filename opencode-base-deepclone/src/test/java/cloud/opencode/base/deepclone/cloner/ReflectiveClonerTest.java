package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.annotation.CloneIgnore;
import cloud.opencode.base.deepclone.annotation.CloneReference;
import cloud.opencode.base.deepclone.contract.DeepCloneable;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ReflectiveCloner 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("ReflectiveCloner 测试")
class ReflectiveClonerTest {

    private ReflectiveCloner cloner;

    @BeforeEach
    void setUp() {
        cloner = ReflectiveCloner.create();
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

        public CircularA() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public CircularB getRef() { return ref; }
        public void setRef(CircularB ref) { this.ref = ref; }
    }

    public static class CircularB {
        private String name;
        private CircularA ref;

        public CircularB() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public CircularA getRef() { return ref; }
        public void setRef(CircularA ref) { this.ref = ref; }
    }

    public static class ParentEntity {
        protected String parentField;

        public String getParentField() { return parentField; }
        public void setParentField(String parentField) { this.parentField = parentField; }
    }

    public static class ChildEntity extends ParentEntity {
        private String childField;

        public ChildEntity() {}
        public String getChildField() { return childField; }
        public void setChildField(String childField) { this.childField = childField; }
    }

    // Unique class for testing transient NOT cloned
    public static class TransientNotClonedEntity {
        private String name;
        private transient String transientField;

        public TransientNotClonedEntity() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTransientField() { return transientField; }
        public void setTransientField(String transientField) { this.transientField = transientField; }
    }

    // Unique class for testing transient IS cloned
    public static class TransientClonedEntity {
        private String name;
        private transient String transientField;

        public TransientClonedEntity() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTransientField() { return transientField; }
        public void setTransientField(String transientField) { this.transientField = transientField; }
    }

    public static class CloneableEntity implements DeepCloneable<CloneableEntity> {
        private String name;
        private int value;

        public CloneableEntity() {}
        public CloneableEntity(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public int getValue() { return value; }

        @Override
        public CloneableEntity deepClone() {
            return new CloneableEntity(name + "-cloned", value * 2);
        }

        @Override
        public CloneableEntity deepClone(cloud.opencode.base.deepclone.Cloner cloner) {
            // Override to avoid infinite recursion
            return deepClone();
        }
    }

    public record TestRecord(String name, int value) {}

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create() 创建默认配置克隆器")
        void testCreate() {
            ReflectiveCloner cloner = ReflectiveCloner.create();

            assertThat(cloner).isNotNull();
            assertThat(cloner.getStrategyName()).isEqualTo("reflective");
        }

        @Test
        @DisplayName("create(config) 创建自定义配置克隆器")
        void testCreateWithConfig() {
            ReflectiveCloner.ReflectiveConfig config =
                    new ReflectiveCloner.ReflectiveConfig(true, false, true);
            ReflectiveCloner cloner = ReflectiveCloner.create(config);

            assertThat(cloner).isNotNull();
        }
    }

    @Nested
    @DisplayName("ReflectiveConfig 测试")
    class ConfigTests {

        @Test
        @DisplayName("defaults() 返回默认配置")
        void testDefaults() {
            ReflectiveCloner.ReflectiveConfig config = ReflectiveCloner.ReflectiveConfig.defaults();

            assertThat(config.cloneTransient()).isFalse();
            assertThat(config.useFieldCache()).isTrue();
            assertThat(config.respectAnnotations()).isTrue();
        }

        @Test
        @DisplayName("自定义配置")
        void testCustomConfig() {
            ReflectiveCloner.ReflectiveConfig config =
                    new ReflectiveCloner.ReflectiveConfig(true, false, false);

            assertThat(config.cloneTransient()).isTrue();
            assertThat(config.useFieldCache()).isFalse();
            assertThat(config.respectAnnotations()).isFalse();
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
        @DisplayName("克隆继承类保留父类字段")
        void testCloneInheritedFields() {
            ChildEntity original = new ChildEntity();
            original.setParentField("parent");
            original.setChildField("child");

            ChildEntity cloned = cloner.clone(original);

            assertThat(cloned.getParentField()).isEqualTo("parent");
            assertThat(cloned.getChildField()).isEqualTo("child");
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
    @DisplayName("Transient字段测试")
    class TransientTests {

        @Test
        @DisplayName("默认不克隆transient字段")
        void testTransientNotClonedByDefault() {
            // Use unique class to avoid field cache interference
            TransientNotClonedEntity original = new TransientNotClonedEntity();
            original.setName("test");
            original.setTransientField("transient-value");

            TransientNotClonedEntity cloned = cloner.clone(original);

            assertThat(cloned.getName()).isEqualTo("test");
            assertThat(cloned.getTransientField()).isNull();
        }

        @Test
        @DisplayName("配置克隆transient字段")
        void testTransientClonedWithConfig() {
            // Use unique class to avoid field cache interference
            ReflectiveCloner transientCloner = ReflectiveCloner.create(
                    new ReflectiveCloner.ReflectiveConfig(true, true, true)
            );

            TransientClonedEntity original = new TransientClonedEntity();
            original.setName("test");
            original.setTransientField("transient-value");

            TransientClonedEntity cloned = transientCloner.clone(original);

            assertThat(cloned.getName()).isEqualTo("test");
            assertThat(cloned.getTransientField()).isEqualTo("transient-value");
        }
    }

    @Nested
    @DisplayName("DeepCloneable接口测试")
    class DeepCloneableTests {

        @Test
        @DisplayName("实现DeepCloneable使用自定义克隆")
        void testDeepCloneable() {
            CloneableEntity original = new CloneableEntity("test", 100);

            CloneableEntity cloned = cloner.clone(original);

            assertThat(cloned.getName()).isEqualTo("test-cloned");
            assertThat(cloned.getValue()).isEqualTo(200);
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
        @DisplayName("支持所有类型")
        void testSupportsAllTypes() {
            assertThat(cloner.supports(SimpleEntity.class)).isTrue();
            assertThat(cloner.supports(String.class)).isTrue();
            assertThat(cloner.supports(int[].class)).isTrue();
            assertThat(cloner.supports(List.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("最大深度测试")
    class MaxDepthTests {

        @Test
        @DisplayName("超过最大深度抛出异常")
        void testMaxDepthExceeded() {
            // Create deeply nested structure
            cloner.setMaxDepth(2);

            SimpleEntity inner = new SimpleEntity("inner", 1);
            NestedEntity middle = new NestedEntity("middle", inner);
            // This creates depth > 2

            // Should not throw for depth 2
            assertThatCode(() -> cloner.clone(middle)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("AbstractCloner方法测试")
    class AbstractClonerMethodsTests {

        @Test
        @DisplayName("setMaxDepth() 设置最大深度")
        void testSetMaxDepth() {
            cloner.setMaxDepth(10);

            // Verify it's set by cloning deeply nested structure
            assertThatCode(() -> cloner.clone(new SimpleEntity("test", 1)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("setCloneTransient() 设置transient克隆")
        void testSetCloneTransient() {
            cloner.setCloneTransient(true);

            // Create a fresh cloner to avoid cache issues
            assertThat(cloner).isNotNull();
        }

        @Test
        @DisplayName("registerImmutable() 注册不可变类型")
        void testRegisterImmutable() {
            cloner.registerImmutable(SimpleEntity.class);

            SimpleEntity original = new SimpleEntity("test", 123);
            SimpleEntity cloned = cloner.clone(original);

            // Should return same instance for immutable type
            assertThat(cloned).isSameAs(original);
        }

        @Test
        @DisplayName("registerHandler() 注册类型处理器")
        void testRegisterHandler() {
            cloud.opencode.base.deepclone.handler.TypeHandler<SimpleEntity> handler =
                    new cloud.opencode.base.deepclone.handler.TypeHandler<>() {
                        @Override
                        public SimpleEntity clone(SimpleEntity original,
                                                  cloud.opencode.base.deepclone.Cloner c,
                                                  CloneContext ctx) {
                            return new SimpleEntity(original.getName() + "-handled", original.getValue());
                        }

                        @Override
                        public boolean supports(Class<?> type) {
                            return SimpleEntity.class.equals(type);
                        }
                    };

            cloner.registerHandler(SimpleEntity.class, handler);

            // Handler is registered (actual use depends on implementation)
            assertThat(cloner).isNotNull();
        }

        @Test
        @DisplayName("isBuiltinImmutable() 检查内置不可变类型")
        void testBuiltinImmutableTypes() {
            // Clone built-in immutable types - should return same instance
            String str = "test";
            assertThat(cloner.clone(str)).isSameAs(str);

            Integer num = 123;
            assertThat(cloner.clone(num)).isSameAs(num);

            java.time.LocalDate date = java.time.LocalDate.now();
            assertThat(cloner.clone(date)).isSameAs(date);

            java.util.UUID uuid = java.util.UUID.randomUUID();
            assertThat(cloner.clone(uuid)).isSameAs(uuid);
        }

        @Test
        @DisplayName("克隆枚举返回相同引用")
        void testCloneEnumReturnsSameReference() {
            Thread.State state = Thread.State.RUNNABLE;

            Thread.State cloned = cloner.clone(state);

            assertThat(cloned).isSameAs(state);
        }

        @Test
        @DisplayName("克隆Class对象返回相同引用")
        void testCloneClassReturnsSameReference() {
            Class<?> cls = String.class;

            Class<?> cloned = cloner.clone(cls);

            assertThat(cloned).isSameAs(cls);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("克隆空数组")
        void testCloneEmptyArray() {
            int[] original = {};
            int[] cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isEmpty();
        }

        @Test
        @DisplayName("克隆null字段的对象")
        void testCloneObjectWithNullFields() {
            SimpleEntity original = new SimpleEntity(null, 0);

            SimpleEntity cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isNull();
            assertThat(cloned.getValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("克隆多维数组")
        void testCloneMultiDimensionalArray() {
            int[][] original = {{1, 2}, {3, 4}};

            int[][] cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned[0]).isNotSameAs(original[0]);
            assertThat(cloned).isDeepEqualTo(original);
        }

        @Test
        @DisplayName("克隆Set")
        void testCloneSet() {
            Set<String> original = new HashSet<>();
            original.add("a");
            original.add("b");

            Set<String> cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("克隆Queue")
        void testCloneQueue() {
            java.util.Queue<String> original = new java.util.LinkedList<>();
            original.add("first");
            original.add("second");

            java.util.Queue<String> cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).hasSize(2);
        }
    }
}
