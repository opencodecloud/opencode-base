package cloud.opencode.base.deepclone.cloner;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * SerializingCloner 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("SerializingCloner 测试")
class SerializingClonerTest {

    private SerializingCloner cloner;

    @BeforeEach
    void setUp() {
        cloner = SerializingCloner.create();
    }

    // Test entities
    public static class SerializableEntity implements Serializable {
        private String name;
        private int value;
        private SerializableEntity nested;

        public SerializableEntity() {}

        public SerializableEntity(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        public SerializableEntity getNested() { return nested; }
        public void setNested(SerializableEntity nested) { this.nested = nested; }
    }

    public static class NonSerializableEntity {
        private String name;

        public NonSerializableEntity(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    public static class CircularSerializable implements Serializable {
        private String name;
        private CircularSerializable ref;

        public CircularSerializable(String name) {
            this.name = name;
        }

        public String getName() { return name; }
        public CircularSerializable getRef() { return ref; }
        public void setRef(CircularSerializable ref) { this.ref = ref; }
    }

    @Nested
    @DisplayName("工厂方法测试")
    class FactoryMethodTests {

        @Test
        @DisplayName("create() 创建克隆器")
        void testCreate() {
            SerializingCloner cloner = SerializingCloner.create();

            assertThat(cloner).isNotNull();
            assertThat(cloner.getStrategyName()).isEqualTo("serializing");
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
        @DisplayName("克隆Serializable对象")
        void testCloneSerializable() {
            SerializableEntity original = new SerializableEntity("test", 123);

            SerializableEntity cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("test");
            assertThat(cloned.getValue()).isEqualTo(123);
        }

        @Test
        @DisplayName("克隆嵌套Serializable对象")
        void testCloneNestedSerializable() {
            SerializableEntity nested = new SerializableEntity("nested", 100);
            SerializableEntity original = new SerializableEntity("parent", 200);
            original.setNested(nested);

            SerializableEntity cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getNested()).isNotSameAs(original.getNested());
            assertThat(cloned.getNested().getName()).isEqualTo("nested");
        }

        @Test
        @DisplayName("克隆非Serializable对象抛出异常")
        void testCloneNonSerializable() {
            NonSerializableEntity original = new NonSerializableEntity("test");

            assertThatThrownBy(() -> cloner.clone(original))
                    .isInstanceOf(OpenDeepCloneException.class)
                    .hasMessageContaining("Unsupported type");
        }

        @Test
        @DisplayName("克隆Serializable数组")
        void testCloneSerializableArray() {
            Integer[] original = {1, 2, 3, 4, 5};

            Integer[] cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("克隆Serializable List")
        void testCloneSerializableList() {
            ArrayList<SerializableEntity> original = new ArrayList<>();
            original.add(new SerializableEntity("a", 1));
            original.add(new SerializableEntity("b", 2));

            ArrayList<SerializableEntity> cloned = cloner.clone(original);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).hasSize(2);
            assertThat(cloned.get(0)).isNotSameAs(original.get(0));
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
            CircularSerializable a = new CircularSerializable("A");
            CircularSerializable b = new CircularSerializable("B");
            a.setRef(b);
            b.setRef(a);

            CircularSerializable clonedA = cloner.clone(a);

            assertThat(clonedA).isNotSameAs(a);
            assertThat(clonedA.getRef()).isNotSameAs(b);
            assertThat(clonedA.getRef().getRef()).isSameAs(clonedA);
        }
    }

    @Nested
    @DisplayName("clone(T, CloneContext) 测试")
    class CloneWithContextTests {

        @Test
        @DisplayName("使用自定义上下文克隆")
        void testCloneWithContext() {
            SerializableEntity original = new SerializableEntity("test", 123);
            CloneContext context = CloneContext.create(50);

            SerializableEntity cloned = cloner.clone(original, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getName()).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("支持Serializable类型")
        void testSupportsSerializable() {
            assertThat(cloner.supports(SerializableEntity.class)).isTrue();
            assertThat(cloner.supports(String.class)).isTrue();
            assertThat(cloner.supports(ArrayList.class)).isTrue();
        }

        @Test
        @DisplayName("不支持非Serializable类型")
        void testNotSupportsNonSerializable() {
            assertThat(cloner.supports(NonSerializableEntity.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("getStrategyName() 测试")
    class StrategyNameTests {

        @Test
        @DisplayName("返回serializing")
        void testStrategyName() {
            assertThat(cloner.getStrategyName()).isEqualTo("serializing");
        }
    }
}
