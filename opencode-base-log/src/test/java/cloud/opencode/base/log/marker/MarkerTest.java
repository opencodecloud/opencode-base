package cloud.opencode.base.log.marker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.*;

/**
 * Marker 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("Marker 接口测试")
class MarkerTest {

    @Nested
    @DisplayName("接口定义测试")
    class InterfaceDefinitionTests {

        @Test
        @DisplayName("Marker是接口")
        void testIsInterface() {
            assertThat(Marker.class.isInterface()).isTrue();
        }

        @Test
        @DisplayName("定义了getName方法")
        void testGetNameMethod() throws NoSuchMethodException {
            assertThat(Marker.class.getMethod("getName")).isNotNull();
        }

        @Test
        @DisplayName("定义了add方法")
        void testAddMethod() throws NoSuchMethodException {
            assertThat(Marker.class.getMethod("add", Marker.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了remove方法")
        void testRemoveMethod() throws NoSuchMethodException {
            assertThat(Marker.class.getMethod("remove", Marker.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了hasReferences方法")
        void testHasReferencesMethod() throws NoSuchMethodException {
            assertThat(Marker.class.getMethod("hasReferences")).isNotNull();
        }

        @Test
        @DisplayName("定义了iterator方法")
        void testIteratorMethod() throws NoSuchMethodException {
            assertThat(Marker.class.getMethod("iterator")).isNotNull();
        }

        @Test
        @DisplayName("定义了contains(Marker)方法")
        void testContainsMarkerMethod() throws NoSuchMethodException {
            assertThat(Marker.class.getMethod("contains", Marker.class)).isNotNull();
        }

        @Test
        @DisplayName("定义了contains(String)方法")
        void testContainsStringMethod() throws NoSuchMethodException {
            assertThat(Marker.class.getMethod("contains", String.class)).isNotNull();
        }
    }

    @Nested
    @DisplayName("实现测试")
    class ImplementationTests {

        @Test
        @DisplayName("通过Markers获取Marker实例")
        void testGetMarkerInstance() {
            Marker marker = Markers.getMarker("TEST_MARKER");
            assertThat(marker).isNotNull();
            assertThat(marker.getName()).isEqualTo("TEST_MARKER");
        }

        @Test
        @DisplayName("getName返回标记名称")
        void testGetName() {
            Marker marker = Markers.getMarker("MY_MARKER");
            assertThat(marker.getName()).isEqualTo("MY_MARKER");
        }

        @Test
        @DisplayName("add添加引用")
        void testAdd() {
            Marker parent = Markers.getMarker("PARENT_MARKER");
            Marker child = Markers.getMarker("CHILD_MARKER");

            parent.add(child);

            assertThat(parent.hasReferences()).isTrue();
            assertThat(parent.contains(child)).isTrue();
        }

        @Test
        @DisplayName("remove移除引用")
        void testRemove() {
            Marker parent = Markers.getMarker("PARENT_REMOVE");
            Marker child = Markers.getMarker("CHILD_REMOVE");

            parent.add(child);
            boolean removed = parent.remove(child);

            assertThat(removed).isTrue();
            assertThat(parent.contains(child)).isFalse();
        }

        @Test
        @DisplayName("hasReferences检查是否有引用")
        void testHasReferences() {
            Marker marker = Markers.getMarker("NO_REF_MARKER");
            assertThat(marker.hasReferences()).isFalse();

            Marker ref = Markers.getMarker("REF_MARKER");
            marker.add(ref);
            assertThat(marker.hasReferences()).isTrue();
        }

        @Test
        @DisplayName("iterator遍历引用")
        void testIterator() {
            Marker parent = Markers.getMarker("ITER_PARENT");
            Marker child1 = Markers.getMarker("ITER_CHILD1");
            Marker child2 = Markers.getMarker("ITER_CHILD2");

            parent.add(child1);
            parent.add(child2);

            Iterator<Marker> iter = parent.iterator();
            int count = 0;
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("contains(Marker)检查包含")
        void testContainsMarker() {
            Marker parent = Markers.getMarker("CONTAIN_PARENT");
            Marker child = Markers.getMarker("CONTAIN_CHILD");

            assertThat(parent.contains(child)).isFalse();
            parent.add(child);
            assertThat(parent.contains(child)).isTrue();
        }

        @Test
        @DisplayName("contains(String)检查包含")
        void testContainsString() {
            Marker parent = Markers.getMarker("CONTAIN_STR_PARENT");
            Marker child = Markers.getMarker("CONTAIN_STR_CHILD");

            parent.add(child);
            assertThat(parent.contains("CONTAIN_STR_CHILD")).isTrue();
            assertThat(parent.contains("NONEXISTENT")).isFalse();
        }

        @Test
        @DisplayName("equals基于名称比较")
        void testEquals() {
            Marker marker1 = Markers.getMarker("EQUAL_TEST");
            Marker marker2 = Markers.getMarker("EQUAL_TEST");

            assertThat(marker1).isEqualTo(marker2);
        }

        @Test
        @DisplayName("hashCode基于名称")
        void testHashCode() {
            Marker marker1 = Markers.getMarker("HASH_TEST");
            Marker marker2 = Markers.getMarker("HASH_TEST");

            assertThat(marker1.hashCode()).isEqualTo(marker2.hashCode());
        }
    }
}
