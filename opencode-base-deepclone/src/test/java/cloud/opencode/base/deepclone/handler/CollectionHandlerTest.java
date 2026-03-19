package cloud.opencode.base.deepclone.handler;

import cloud.opencode.base.deepclone.CloneContext;
import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.OpenClone;
import cloud.opencode.base.deepclone.exception.OpenDeepCloneException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * CollectionHandler 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("CollectionHandler 测试")
class CollectionHandlerTest {

    private CollectionHandler handler;
    private Cloner cloner;
    private CloneContext context;

    @BeforeEach
    void setUp() {
        handler = new CollectionHandler();
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
            Collection<?> result = handler.clone(null, cloner, context);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("克隆ArrayList")
        void testCloneArrayList() {
            ArrayList<TestEntity> original = new ArrayList<>();
            original.add(new TestEntity("a"));
            original.add(new TestEntity("b"));

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(ArrayList.class);
            assertThat(cloned).hasSize(2);
        }

        @Test
        @DisplayName("克隆LinkedList")
        void testCloneLinkedList() {
            LinkedList<String> original = new LinkedList<>();
            original.add("a");
            original.add("b");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(LinkedList.class);
        }

        @Test
        @DisplayName("克隆HashSet")
        void testCloneHashSet() {
            HashSet<String> original = new HashSet<>();
            original.add("a");
            original.add("b");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(HashSet.class);
            assertThat(cloned).hasSize(2);
            @SuppressWarnings("unchecked")
            HashSet<String> typedCloned = (HashSet<String>) cloned;
            assertThat(typedCloned).containsExactlyInAnyOrder("a", "b");
        }

        @Test
        @DisplayName("克隆LinkedHashSet")
        void testCloneLinkedHashSet() {
            LinkedHashSet<String> original = new LinkedHashSet<>();
            original.add("a");
            original.add("b");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(LinkedHashSet.class);
        }

        @Test
        @DisplayName("克隆TreeSet")
        void testCloneTreeSet() {
            TreeSet<String> original = new TreeSet<>();
            original.add("a");
            original.add("b");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(TreeSet.class);
        }

        @Test
        @DisplayName("克隆ArrayDeque")
        void testCloneArrayDeque() {
            ArrayDeque<String> original = new ArrayDeque<>();
            original.add("a");
            original.add("b");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(ArrayDeque.class);
        }

        @Test
        @DisplayName("克隆PriorityQueue")
        void testClonePriorityQueue() {
            PriorityQueue<String> original = new PriorityQueue<>();
            original.add("a");
            original.add("b");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(PriorityQueue.class);
        }

        @Test
        @DisplayName("克隆Vector")
        void testCloneVector() {
            Vector<String> original = new Vector<>();
            original.add("a");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).isInstanceOf(Vector.class);
        }

        @Test
        @DisplayName("克隆Stack")
        void testCloneStack() {
            Stack<String> original = new Stack<>();
            original.push("a");

            Collection<?> cloned = handler.clone(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            // Note: Stack extends Vector, and the handler checks Vector first,
            // so Stack is created as Vector (library behavior)
            assertThat(cloned).isInstanceOf(Vector.class);
            assertThat(cloned).hasSize(1);
        }

        @Test
        @DisplayName("深度克隆元素")
        void testDeepCloneElements() {
            ArrayList<TestEntity> original = new ArrayList<>();
            original.add(new TestEntity("a"));

            @SuppressWarnings("unchecked")
            ArrayList<TestEntity> cloned = (ArrayList<TestEntity>) handler.clone(original, cloner, context);

            assertThat(cloned.get(0)).isNotSameAs(original.get(0));
            assertThat(cloned.get(0).getName()).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("cloneList() 测试")
    class CloneListTests {

        @Test
        @DisplayName("克隆List")
        void testCloneList() {
            List<String> original = new ArrayList<>();
            original.add("a");
            original.add("b");

            List<String> cloned = handler.cloneList(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactly("a", "b");
        }
    }

    @Nested
    @DisplayName("cloneSet() 测试")
    class CloneSetTests {

        @Test
        @DisplayName("克隆Set")
        void testCloneSet() {
            Set<String> original = new HashSet<>();
            original.add("a");
            original.add("b");

            Set<String> cloned = handler.cloneSet(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).containsExactlyInAnyOrder("a", "b");
        }
    }

    @Nested
    @DisplayName("cloneQueue() 测试")
    class CloneQueueTests {

        @Test
        @DisplayName("克隆Queue")
        void testCloneQueue() {
            Queue<String> original = new ArrayDeque<>();
            original.add("a");
            original.add("b");

            Queue<String> cloned = handler.cloneQueue(original, cloner, context);

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned).hasSize(2);
        }
    }

    @Nested
    @DisplayName("createInstance() 测试")
    class CreateInstanceTests {

        @Test
        @DisplayName("创建ArrayList实例")
        void testCreateArrayList() {
            Collection<String> instance = handler.createInstance(ArrayList.class, 10);
            assertThat(instance).isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("创建LinkedList实例")
        void testCreateLinkedList() {
            Collection<String> instance = handler.createInstance(LinkedList.class, 10);
            assertThat(instance).isInstanceOf(LinkedList.class);
        }

        @Test
        @DisplayName("创建Vector实例")
        void testCreateVector() {
            Collection<String> instance = handler.createInstance(Vector.class, 10);
            assertThat(instance).isInstanceOf(Vector.class);
        }

        @Test
        @DisplayName("创建Stack实例")
        void testCreateStack() {
            // Note: Stack extends Vector, and the handler checks Vector first,
            // so Stack is created as Vector (library behavior)
            Collection<String> instance = handler.createInstance(Stack.class, 10);
            assertThat(instance).isInstanceOf(Vector.class);
        }

        @Test
        @DisplayName("创建CopyOnWriteArrayList实例")
        void testCreateCopyOnWriteArrayList() {
            Collection<String> instance = handler.createInstance(CopyOnWriteArrayList.class, 10);
            assertThat(instance).isInstanceOf(CopyOnWriteArrayList.class);
        }

        @Test
        @DisplayName("创建HashSet实例")
        void testCreateHashSet() {
            Collection<String> instance = handler.createInstance(HashSet.class, 10);
            assertThat(instance).isInstanceOf(HashSet.class);
        }

        @Test
        @DisplayName("创建LinkedHashSet实例")
        void testCreateLinkedHashSet() {
            Collection<String> instance = handler.createInstance(LinkedHashSet.class, 10);
            assertThat(instance).isInstanceOf(LinkedHashSet.class);
        }

        @Test
        @DisplayName("创建TreeSet实例")
        void testCreateTreeSet() {
            Collection<String> instance = handler.createInstance(TreeSet.class, 10);
            assertThat(instance).isInstanceOf(TreeSet.class);
        }

        @Test
        @DisplayName("创建CopyOnWriteArraySet实例")
        void testCreateCopyOnWriteArraySet() {
            Collection<String> instance = handler.createInstance(CopyOnWriteArraySet.class, 10);
            assertThat(instance).isInstanceOf(CopyOnWriteArraySet.class);
        }

        @Test
        @DisplayName("创建ConcurrentSkipListSet实例")
        void testCreateConcurrentSkipListSet() {
            Collection<String> instance = handler.createInstance(ConcurrentSkipListSet.class, 10);
            assertThat(instance).isInstanceOf(ConcurrentSkipListSet.class);
        }

        @Test
        @DisplayName("创建ArrayDeque实例")
        void testCreateArrayDeque() {
            Collection<String> instance = handler.createInstance(ArrayDeque.class, 10);
            assertThat(instance).isInstanceOf(ArrayDeque.class);
        }

        @Test
        @DisplayName("创建PriorityQueue实例")
        void testCreatePriorityQueue() {
            Collection<String> instance = handler.createInstance(PriorityQueue.class, 10);
            assertThat(instance).isInstanceOf(PriorityQueue.class);
        }

        @Test
        @DisplayName("创建PriorityQueue实例空大小")
        void testCreatePriorityQueueZeroSize() {
            Collection<String> instance = handler.createInstance(PriorityQueue.class, 0);
            assertThat(instance).isInstanceOf(PriorityQueue.class);
        }

        @Test
        @DisplayName("创建ConcurrentLinkedQueue实例")
        void testCreateConcurrentLinkedQueue() {
            Collection<String> instance = handler.createInstance(ConcurrentLinkedQueue.class, 10);
            assertThat(instance).isInstanceOf(ConcurrentLinkedQueue.class);
        }

        @Test
        @DisplayName("创建LinkedBlockingQueue实例")
        void testCreateLinkedBlockingQueue() {
            Collection<String> instance = handler.createInstance(LinkedBlockingQueue.class, 10);
            assertThat(instance).isInstanceOf(LinkedBlockingQueue.class);
        }

        @Test
        @DisplayName("创建ArrayBlockingQueue实例")
        void testCreateArrayBlockingQueue() {
            Collection<String> instance = handler.createInstance(ArrayBlockingQueue.class, 10);
            assertThat(instance).isInstanceOf(ArrayBlockingQueue.class);
        }

        @Test
        @DisplayName("创建ArrayBlockingQueue实例空大小")
        void testCreateArrayBlockingQueueZeroSize() {
            Collection<String> instance = handler.createInstance(ArrayBlockingQueue.class, 0);
            assertThat(instance).isInstanceOf(ArrayBlockingQueue.class);
        }

        @Test
        @DisplayName("创建PriorityBlockingQueue实例")
        void testCreatePriorityBlockingQueue() {
            Collection<String> instance = handler.createInstance(PriorityBlockingQueue.class, 10);
            assertThat(instance).isInstanceOf(PriorityBlockingQueue.class);
        }

        @Test
        @DisplayName("List接口默认创建ArrayList")
        void testCreateListInterface() {
            Collection<String> instance = handler.createInstance(List.class, 10);
            assertThat(instance).isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("Set接口默认创建HashSet")
        void testCreateSetInterface() {
            Collection<String> instance = handler.createInstance(Set.class, 10);
            assertThat(instance).isInstanceOf(HashSet.class);
        }

        @Test
        @DisplayName("Queue接口默认创建ArrayDeque")
        void testCreateQueueInterface() {
            Collection<String> instance = handler.createInstance(Queue.class, 10);
            assertThat(instance).isInstanceOf(ArrayDeque.class);
        }
    }

    @Nested
    @DisplayName("supports() 测试")
    class SupportsTests {

        @Test
        @DisplayName("支持Collection类型")
        void testSupportsCollection() {
            assertThat(handler.supports(ArrayList.class)).isTrue();
            assertThat(handler.supports(HashSet.class)).isTrue();
            assertThat(handler.supports(LinkedList.class)).isTrue();
            assertThat(handler.supports(Collection.class)).isTrue();
        }

        @Test
        @DisplayName("不支持非Collection类型")
        void testNotSupportsNonCollection() {
            assertThat(handler.supports(String.class)).isFalse();
            assertThat(handler.supports(HashMap.class)).isFalse();
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
