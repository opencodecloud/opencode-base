package cloud.opencode.base.reflect.type;

import org.junit.jupiter.api.*;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * WildcardTypeImplTest Tests
 * WildcardTypeImplTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("WildcardTypeImpl 测试")
class WildcardTypeImplTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("创建带上界的通配符类型")
        void testCreateWithUpperBound() {
            WildcardTypeImpl type = new WildcardTypeImpl(new Type[]{Number.class}, null);
            assertThat(type.getUpperBounds()).containsExactly(Number.class);
            assertThat(type.getLowerBounds()).isEmpty();
        }

        @Test
        @DisplayName("创建带下界的通配符类型")
        void testCreateWithLowerBound() {
            WildcardTypeImpl type = new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{Integer.class});
            assertThat(type.getLowerBounds()).containsExactly(Integer.class);
        }

        @Test
        @DisplayName("无上界时默认为Object")
        void testCreateWithoutUpperBound() {
            WildcardTypeImpl type = new WildcardTypeImpl(null, null);
            assertThat(type.getUpperBounds()).containsExactly(Object.class);
        }
    }

    @Nested
    @DisplayName("extendsOf工厂方法测试")
    class ExtendsOfTests {

        @Test
        @DisplayName("创建? extends Number")
        void testExtendsOf() {
            WildcardTypeImpl type = WildcardTypeImpl.extendsOf(Number.class);
            assertThat(type.getUpperBounds()).containsExactly(Number.class);
            assertThat(type.getLowerBounds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("superOf工厂方法测试")
    class SuperOfTests {

        @Test
        @DisplayName("创建? super Integer")
        void testSuperOf() {
            WildcardTypeImpl type = WildcardTypeImpl.superOf(Integer.class);
            assertThat(type.getUpperBounds()).containsExactly(Object.class);
            assertThat(type.getLowerBounds()).containsExactly(Integer.class);
        }
    }

    @Nested
    @DisplayName("unbounded工厂方法测试")
    class UnboundedTests {

        @Test
        @DisplayName("创建无界通配符?")
        void testUnbounded() {
            WildcardTypeImpl type = WildcardTypeImpl.unbounded();
            assertThat(type.getUpperBounds()).containsExactly(Object.class);
            assertThat(type.getLowerBounds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUpperBounds方法测试")
    class GetUpperBoundsTests {

        @Test
        @DisplayName("获取上界")
        void testGetUpperBounds() {
            WildcardTypeImpl type = WildcardTypeImpl.extendsOf(Number.class);
            Type[] bounds = type.getUpperBounds();
            assertThat(bounds).containsExactly(Number.class);
        }

        @Test
        @DisplayName("返回的数组是副本")
        void testGetUpperBoundsReturnsClone() {
            WildcardTypeImpl type = WildcardTypeImpl.extendsOf(Number.class);
            Type[] bounds1 = type.getUpperBounds();
            Type[] bounds2 = type.getUpperBounds();
            assertThat(bounds1).isNotSameAs(bounds2);
        }
    }

    @Nested
    @DisplayName("getLowerBounds方法测试")
    class GetLowerBoundsTests {

        @Test
        @DisplayName("获取下界")
        void testGetLowerBounds() {
            WildcardTypeImpl type = WildcardTypeImpl.superOf(Integer.class);
            Type[] bounds = type.getLowerBounds();
            assertThat(bounds).containsExactly(Integer.class);
        }

        @Test
        @DisplayName("返回的数组是副本")
        void testGetLowerBoundsReturnsClone() {
            WildcardTypeImpl type = WildcardTypeImpl.superOf(Integer.class);
            Type[] bounds1 = type.getLowerBounds();
            Type[] bounds2 = type.getLowerBounds();
            assertThat(bounds1).isNotSameAs(bounds2);
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同通配符类型相等")
        void testEquals() {
            WildcardTypeImpl type1 = WildcardTypeImpl.extendsOf(Number.class);
            WildcardTypeImpl type2 = WildcardTypeImpl.extendsOf(Number.class);
            assertThat(type1).isEqualTo(type2);
        }

        @Test
        @DisplayName("不同上界不相等")
        void testNotEqualsDifferentUpperBound() {
            WildcardTypeImpl type1 = WildcardTypeImpl.extendsOf(Number.class);
            WildcardTypeImpl type2 = WildcardTypeImpl.extendsOf(String.class);
            assertThat(type1).isNotEqualTo(type2);
        }

        @Test
        @DisplayName("不同下界不相等")
        void testNotEqualsDifferentLowerBound() {
            WildcardTypeImpl type1 = WildcardTypeImpl.superOf(Integer.class);
            WildcardTypeImpl type2 = WildcardTypeImpl.superOf(Number.class);
            assertThat(type1).isNotEqualTo(type2);
        }

        @Test
        @DisplayName("与自身相等")
        void testEqualsSelf() {
            WildcardTypeImpl type = WildcardTypeImpl.extendsOf(Number.class);
            assertThat(type).isEqualTo(type);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同类型有相同hashCode")
        void testHashCode() {
            WildcardTypeImpl type1 = WildcardTypeImpl.extendsOf(Number.class);
            WildcardTypeImpl type2 = WildcardTypeImpl.extendsOf(Number.class);
            assertThat(type1.hashCode()).isEqualTo(type2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("带上界的toString")
        void testToStringExtends() {
            WildcardTypeImpl type = WildcardTypeImpl.extendsOf(Number.class);
            assertThat(type.toString()).isEqualTo("? extends Number");
        }

        @Test
        @DisplayName("带下界的toString")
        void testToStringSuper() {
            WildcardTypeImpl type = WildcardTypeImpl.superOf(Integer.class);
            assertThat(type.toString()).isEqualTo("? super Integer");
        }

        @Test
        @DisplayName("无界的toString")
        void testToStringUnbounded() {
            WildcardTypeImpl type = WildcardTypeImpl.unbounded();
            assertThat(type.toString()).isEqualTo("?");
        }
    }
}
