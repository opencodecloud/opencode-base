package cloud.opencode.base.log.marker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Markers 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
@DisplayName("Markers 测试")
class MarkersTest {

    @Nested
    @DisplayName("类定义测试")
    class ClassDefinitionTests {

        @Test
        @DisplayName("类是final的")
        void testIsFinal() {
            assertThat(java.lang.reflect.Modifier.isFinal(Markers.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("私有构造函数")
        void testPrivateConstructor() throws NoSuchMethodException {
            var constructor = Markers.class.getDeclaredConstructor();
            assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("预定义标记测试")
    class PredefinedMarkersTests {

        @Test
        @DisplayName("SECURITY标记存在")
        void testSecurityMarker() {
            assertThat(Markers.SECURITY).isNotNull();
            assertThat(Markers.SECURITY.getName()).isEqualTo("SECURITY");
        }

        @Test
        @DisplayName("PERFORMANCE标记存在")
        void testPerformanceMarker() {
            assertThat(Markers.PERFORMANCE).isNotNull();
            assertThat(Markers.PERFORMANCE.getName()).isEqualTo("PERFORMANCE");
        }

        @Test
        @DisplayName("AUDIT标记存在")
        void testAuditMarker() {
            assertThat(Markers.AUDIT).isNotNull();
            assertThat(Markers.AUDIT.getName()).isEqualTo("AUDIT");
        }

        @Test
        @DisplayName("BUSINESS标记存在")
        void testBusinessMarker() {
            assertThat(Markers.BUSINESS).isNotNull();
            assertThat(Markers.BUSINESS.getName()).isEqualTo("BUSINESS");
        }

        @Test
        @DisplayName("SYSTEM标记存在")
        void testSystemMarker() {
            assertThat(Markers.SYSTEM).isNotNull();
            assertThat(Markers.SYSTEM.getName()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("DATABASE标记存在")
        void testDatabaseMarker() {
            assertThat(Markers.DATABASE).isNotNull();
            assertThat(Markers.DATABASE.getName()).isEqualTo("DATABASE");
        }

        @Test
        @DisplayName("NETWORK标记存在")
        void testNetworkMarker() {
            assertThat(Markers.NETWORK).isNotNull();
            assertThat(Markers.NETWORK.getName()).isEqualTo("NETWORK");
        }

        @Test
        @DisplayName("SENSITIVE标记存在")
        void testSensitiveMarker() {
            assertThat(Markers.SENSITIVE).isNotNull();
            assertThat(Markers.SENSITIVE.getName()).isEqualTo("SENSITIVE");
        }

        @Test
        @DisplayName("CONFIDENTIAL标记存在")
        void testConfidentialMarker() {
            assertThat(Markers.CONFIDENTIAL).isNotNull();
            assertThat(Markers.CONFIDENTIAL.getName()).isEqualTo("CONFIDENTIAL");
        }

        @Test
        @DisplayName("ENTRY_EXIT标记存在")
        void testEntryExitMarker() {
            assertThat(Markers.ENTRY_EXIT).isNotNull();
            assertThat(Markers.ENTRY_EXIT.getName()).isEqualTo("ENTRY_EXIT");
        }
    }

    @Nested
    @DisplayName("getMarker(String)方法测试")
    class GetMarkerByNameTests {

        @Test
        @DisplayName("获取新标记")
        void testGetNewMarker() {
            Marker marker = Markers.getMarker("NEW_MARKER_" + System.currentTimeMillis());
            assertThat(marker).isNotNull();
        }

        @Test
        @DisplayName("获取已存在的标记返回相同实例")
        void testGetExistingMarker() {
            Marker marker1 = Markers.getMarker("SAME_MARKER");
            Marker marker2 = Markers.getMarker("SAME_MARKER");

            assertThat(marker1).isSameAs(marker2);
        }

        @Test
        @DisplayName("null名称抛出异常")
        void testNullName() {
            assertThatThrownBy(() -> Markers.getMarker(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getMarker(String, Marker...)方法测试")
    class GetMarkerWithReferencesTests {

        @Test
        @DisplayName("获取带引用的标记")
        void testGetMarkerWithReferences() {
            Marker ref1 = Markers.getMarker("REF1_" + System.currentTimeMillis());
            Marker ref2 = Markers.getMarker("REF2_" + System.currentTimeMillis());
            Marker marker = Markers.getMarker("WITH_REFS_" + System.currentTimeMillis(), ref1, ref2);

            assertThat(marker.hasReferences()).isTrue();
            assertThat(marker.contains(ref1)).isTrue();
            assertThat(marker.contains(ref2)).isTrue();
        }
    }

    @Nested
    @DisplayName("exists方法测试")
    class ExistsTests {

        @Test
        @DisplayName("存在的标记返回true")
        void testExistsTrue() {
            String name = "EXISTS_TEST_" + System.currentTimeMillis();
            Markers.getMarker(name);

            assertThat(Markers.exists(name)).isTrue();
        }

        @Test
        @DisplayName("不存在的标记返回false")
        void testExistsFalse() {
            assertThat(Markers.exists("NONEXISTENT_MARKER_" + System.currentTimeMillis())).isFalse();
        }
    }

    @Nested
    @DisplayName("detachMarker方法测试")
    class DetachMarkerTests {

        @Test
        @DisplayName("移除存在的标记返回true")
        void testDetachExisting() {
            String name = "DETACH_TEST_" + System.currentTimeMillis();
            Markers.getMarker(name);

            boolean result = Markers.detachMarker(name);

            assertThat(result).isTrue();
            assertThat(Markers.exists(name)).isFalse();
        }

        @Test
        @DisplayName("移除不存在的标记返回false")
        void testDetachNonExisting() {
            boolean result = Markers.detachMarker("NONEXISTENT_" + System.currentTimeMillis());
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getMarkerNames方法测试")
    class GetMarkerNamesTests {

        @Test
        @DisplayName("返回所有标记名称")
        void testGetMarkerNames() {
            Set<String> names = Markers.getMarkerNames();

            assertThat(names).isNotNull();
            // At least the predefined markers should exist
            assertThat(names).contains("SECURITY", "PERFORMANCE", "AUDIT");
        }

        @Test
        @DisplayName("返回的集合是不可变的")
        void testGetMarkerNamesUnmodifiable() {
            Set<String> names = Markers.getMarkerNames();

            assertThatThrownBy(() -> names.add("NEW"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("BasicMarker内部类测试")
    class BasicMarkerTests {

        @Test
        @DisplayName("toString不带引用")
        void testToStringWithoutReferences() {
            Marker marker = Markers.getMarker("TO_STRING_TEST_" + System.currentTimeMillis());

            assertThat(marker.toString()).contains("TO_STRING_TEST");
        }

        @Test
        @DisplayName("toString带引用")
        void testToStringWithReferences() {
            Marker parent = Markers.getMarker("PARENT_TS_" + System.currentTimeMillis());
            Marker child = Markers.getMarker("CHILD_TS_" + System.currentTimeMillis());
            parent.add(child);

            String str = parent.toString();
            assertThat(str).contains("[");
            assertThat(str).contains("]");
        }

        @Test
        @DisplayName("add null抛出异常")
        void testAddNull() {
            Marker marker = Markers.getMarker("ADD_NULL_" + System.currentTimeMillis());

            assertThatThrownBy(() -> marker.add(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("不能添加循环引用")
        void testNoCircularReference() {
            Marker m1 = Markers.getMarker("CIRC1_" + System.currentTimeMillis());
            Marker m2 = Markers.getMarker("CIRC2_" + System.currentTimeMillis());

            m1.add(m2);
            m2.add(m1); // Should not add because m1 already contains m2

            // m2 should not contain m1
            assertThat(m2.contains(m1)).isFalse();
        }

        @Test
        @DisplayName("contains(null) Marker返回false")
        void testContainsNullMarker() {
            Marker marker = Markers.getMarker("CONTAIN_NULL_" + System.currentTimeMillis());
            assertThat(marker.contains((Marker) null)).isFalse();
        }

        @Test
        @DisplayName("contains(null) String返回false")
        void testContainsNullString() {
            Marker marker = Markers.getMarker("CONTAIN_NULL_STR_" + System.currentTimeMillis());
            assertThat(marker.contains((String) null)).isFalse();
        }

        @Test
        @DisplayName("contains自身返回true")
        void testContainsSelf() {
            Marker marker = Markers.getMarker("SELF_" + System.currentTimeMillis());
            assertThat(marker.contains(marker)).isTrue();
        }

        @Test
        @DisplayName("contains深层引用")
        void testContainsDeep() {
            long ts = System.currentTimeMillis();
            Marker m1 = Markers.getMarker("DEEP1_" + ts);
            Marker m2 = Markers.getMarker("DEEP2_" + ts);
            Marker m3 = Markers.getMarker("DEEP3_" + ts);

            m1.add(m2);
            m2.add(m3);

            assertThat(m1.contains(m3)).isTrue();
            assertThat(m1.contains("DEEP3_" + ts)).isTrue();
            assertThat(m1.contains("NONEXISTENT_MARKER")).isFalse();
        }

        @Test
        @DisplayName("equals不同对象类型返回false")
        void testEqualsWrongType() {
            Marker marker = Markers.getMarker("EQUALS_TYPE_" + System.currentTimeMillis());
            assertThat(marker.equals("string")).isFalse();
        }

        @Test
        @DisplayName("equals同一对象返回true")
        void testEqualsSameObject() {
            Marker marker = Markers.getMarker("EQUALS_SAME_" + System.currentTimeMillis());
            assertThat(marker.equals(marker)).isTrue();
        }
    }
}
