package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.OpenClone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * MapHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("MapHandler 测试")
class MapHandlerTest {

    private MapHandler handler;
    private Cloner cloner;
    private CloneContext context;

    @BeforeEach
    void setUp() {
        handler = new MapHandler();
        cloner = OpenClone.getDefaultCloner();
        context = CloneContext.create();
    }

    // Test entity
    public static class TestEntity {
        private String name;

        public TestEntity() {}
        public TestEntity(String name) { this.name = name; }
        public String getName() { return name; }
    }

    @Nested
    @DisplayName("clone() 测试")
    class CloneTests {

        @Test
        @DisplayName("克隆null返回null")
        void testCloneNull() {
            Map<?, ?> result = handler.clone(null, cloner, context);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("克隆HashMap")
        void testCloneHashMap() {
            HashMap<String, TestEntity> original = new HashMap<>();
            original.put("key1", new TestEntity("a"));
            original.put("key2", new TestEntity("b"));

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(HashMap.class);
            assertThat(cloned).hasSize(2);
        }

        @Test
        @DisplayName("克隆LinkedHashMap")
        void testCloneLinkedHashMap() {
            LinkedHashMap<String, String> original = new LinkedHashMap<>();
            original.put("a", "1");
            original.put("b", "2");

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(LinkedHashMap.class);
        }

        @Test
        @DisplayName("克隆TreeMap")
        void testCloneTreeMap() {
            TreeMap<String, String> original = new TreeMap<>();
            original.put("a", "1");
            original.put("b", "2");

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(TreeMap.class);
        }

        @Test
        @DisplayName("克隆ConcurrentHashMap")
        void testCloneConcurrentHashMap() {
            ConcurrentHashMap<String, String> original = new ConcurrentHashMap<>();
            original.put("a", "1");

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(ConcurrentHashMap.class);
        }

        @Test
        @DisplayName("克隆ConcurrentSkipListMap")
        void testCloneConcurrentSkipListMap() {
            ConcurrentSkipListMap<String, String> original = new ConcurrentSkipListMap<>();
            original.put("a", "1");

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(ConcurrentSkipListMap.class);
        }

        @Test
        @DisplayName("克隆Hashtable")
        void testCloneHashtable() {
            Hashtable<String, String> original = new Hashtable<>();
            original.put("a", "1");

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(Hashtable.class);
        }

        @Test
        @DisplayName("克隆WeakHashMap")
        void testCloneWeakHashMap() {
            WeakHashMap<String, String> original = new WeakHashMap<>();
            original.put("a", "1");

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(WeakHashMap.class);
        }

        @Test
        @DisplayName("克隆IdentityHashMap")
        void testCloneIdentityHashMap() {
            IdentityHashMap<String, String> original = new IdentityHashMap<>();
            original.put("a", "1");

            Map<?, ?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(IdentityHashMap.class);
        }

        @Test
        @DisplayName("深度克隆键和值")
        void testDeepCloneKeysAndValues() {
            HashMap<TestEntity, TestEntity> original = new HashMap<>();
            TestEntity key = new TestEntity("key");
            TestEntity value = new TestEntity("value");
            original.put(key, value);

            @SuppressWarnings("unchecked")
            HashMap<TestEntity, TestEntity> cloned = (HashMap<TestEntity, TestEntity>) handler.clone(original, cloner, context);

            TestEntity clonedKey = cloned.keySet().iterator().next();
            TestEntity clonedValue = cloned.values().iterator().next();

            assertThat(clonedKey).isNotSameAs(key);
            assertThat(clonedValue).isNotSameAs(value);
            assertThat(clonedKey.getName()).isEqualTo("key");
            assertThat(clonedValue.getName()).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("cloneDeep() 测试")
    class CloneDeepTests {

        @Test
        @DisplayName("深度克隆Map")
        void testCloneDeep() {
            Map<String, TestEntity> original = new HashMap<>();
            original.put("key1", new TestEntity("a"));

            Map<String, TestEntity> cloned = handler.cloneDeep(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.get("key1")).isNotSameAs(original.get("key1"));
            assertThat(cloned.get("key1").getName()).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("cloneValues() 测试")
    class CloneValuesTests {

        @Test
        @DisplayName("克隆null返回null")
        void testCloneValuesNull() {
            Map<String, TestEntity> result = handler.cloneValues(null, cloner, context);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("仅深度克隆值")
        void testCloneValuesOnly() {
            Map<String, TestEntity> original = new HashMap<>();
            original.put("key1", new TestEntity("a"));

            Map<String, TestEntity> cloned = handler.cloneValues(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.get("key1")).isNotSameAs(original.get("key1"));
        }
    }

    @Nested
    @DisplayName("createInstance() 测试")
    class CreateInstanceTests {

        @Test
        @DisplayName("创建HashMap实例")
        void testCreateHashMap() {
            Map<String, String> instance = handler.createInstance(HashMap.class, 10);
            assertThat(instance).isInstanceOf(HashMap.class);
        }

        @Test
        @DisplayName("创建LinkedHashMap实例")
        void testCreateLinkedHashMap() {
            Map<String, String> instance = handler.createInstance(LinkedHashMap.class, 10);
            assertThat(instance).isInstanceOf(LinkedHashMap.class);
        }

        @Test
        @DisplayName("创建TreeMap实例")
        void testCreateTreeMap() {
            Map<String, String> instance = handler.createInstance(TreeMap.class, 10);
            assertThat(instance).isInstanceOf(TreeMap.class);
        }

        @Test
        @DisplayName("创建ConcurrentHashMap实例")
        void testCreateConcurrentHashMap() {
            Map<String, String> instance = handler.createInstance(ConcurrentHashMap.class, 10);
            assertThat(instance).isInstanceOf(ConcurrentHashMap.class);
        }

        @Test
        @DisplayName("创建ConcurrentSkipListMap实例")
        void testCreateConcurrentSkipListMap() {
            Map<String, String> instance = handler.createInstance(ConcurrentSkipListMap.class, 10);
            assertThat(instance).isInstanceOf(ConcurrentSkipListMap.class);
        }

        @Test
        @DisplayName("创建Hashtable实例")
        void testCreateHashtable() {
            Map<String, String> instance = handler.createInstance(Hashtable.class, 10);
            assertThat(instance).isInstanceOf(Hashtable.class);
        }

        @Test
        @DisplayName("创建WeakHashMap实例")
        void testCreateWeakHashMap() {
            Map<String, String> instance = handler.createInstance(WeakHashMap.class, 10);
            assertThat(instance).isInstanceOf(WeakHashMap.class);
        }

        @Test
        @DisplayName("创建IdentityHashMap实例")
        void testCreateIdentityHashMap() {
            Map<String, String> instance = handler.createInstance(IdentityHashMap.class, 10);
            assertThat(instance).isInstanceOf(IdentityHashMap.class);
        }

        @Test
        @DisplayName("Map接口默认创建HashMap")
        void testCreateMapInterface() {
            Map<String, String> instance = handler.createInstance(Map.class, 10);
            assertThat(instance).isInstanceOf(HashMap.class);
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("支持Map类型")
        void testSupportsMap() {
            assertThat(handler.supports(HashMap.class)).isTrue();
            assertThat(handler.supports(TreeMap.class)).isTrue();
            assertThat(handler.supports(Map.class)).isTrue();
        }

        @Test
        @DisplayName("不支持非Map类型")
        void testNotSupportsNonMap() {
            assertThat(handler.supports(String.class)).isFalse();
            assertThat(handler.supports(ArrayList.class)).isFalse();
        }

        @Test
        @DisplayName("null返回false")
        void testSupportsNull() {
            assertThat(handler.supports(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("priority() 测试")
    class PriorityTests {

        @Test
        @DisplayName("优先级为20")
        void testPriority() {
            assertThat(handler.priority()).isEqualTo(20);
        }
    }
}
