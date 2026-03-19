package cloud.opencode.base.reflect.accessor;

import org.junit.jupiter.api.*;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;

/**
 * VarHandleAccessorTest Tests
 * VarHandleAccessorTest 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@DisplayName("VarHandleAccessor 测试")
class VarHandleAccessorTest {

    @Nested
    @DisplayName("fromField静态方法测试")
    class FromFieldTests {

        @Test
        @DisplayName("从Field创建")
        void testFromField() throws NoSuchFieldException {
            Field field = TestBean.class.getDeclaredField("value");
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.fromField(field);
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("null字段抛出异常")
        void testFromFieldNull() {
            assertThatThrownBy(() -> VarHandleAccessor.fromField(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("of静态方法测试")
    class OfTests {

        @Test
        @DisplayName("按类和字段名创建")
        void testOf() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            assertThat(accessor).isNotNull();
        }

        @Test
        @DisplayName("不存在的字段抛出异常")
        void testOfNotFound() {
            assertThatThrownBy(() -> VarHandleAccessor.of(TestBean.class, "nonexistent"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("getVarHandle方法测试")
    class GetVarHandleTests {

        @Test
        @DisplayName("获取底层VarHandle")
        void testGetVarHandle() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            VarHandle vh = accessor.getVarHandle();
            assertThat(vh).isNotNull();
        }
    }

    @Nested
    @DisplayName("getName方法测试")
    class GetNameTests {

        @Test
        @DisplayName("获取字段名")
        void testGetName() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            assertThat(accessor.getName()).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("isStatic方法测试")
    class IsStaticTests {

        @Test
        @DisplayName("实例字段返回false")
        void testIsStaticFalse() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            assertThat(accessor.isStatic()).isFalse();
        }

        @Test
        @DisplayName("静态字段返回true")
        void testIsStaticTrue() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "staticValue");
            assertThat(accessor.isStatic()).isTrue();
        }
    }

    @Nested
    @DisplayName("get和set方法测试")
    class GetSetTests {

        @Test
        @DisplayName("获取和设置值")
        void testGetAndSet() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            accessor.set(bean, 42);
            assertThat(accessor.get(bean)).isEqualTo(42);
        }

        @Test
        @DisplayName("final字段不可写")
        void testSetFinal() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "finalValue");
            TestBean bean = new TestBean();
            assertThatThrownBy(() -> accessor.set(bean, 100))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("getVolatile和setVolatile方法测试")
    class VolatileTests {

        @Test
        @DisplayName("volatile语义读写")
        void testVolatileGetSet() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            accessor.setVolatile(bean, 42);
            assertThat(accessor.getVolatile(bean)).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("getAndSet方法测试")
    class GetAndSetTests {

        @Test
        @DisplayName("原子获取并设置")
        void testGetAndSet() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            bean.value = 10;
            Object old = accessor.getAndSet(bean, 20);
            assertThat(old).isEqualTo(10);
            assertThat(bean.value).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("compareAndSet方法测试")
    class CompareAndSetTests {

        @Test
        @DisplayName("CAS成功")
        void testCompareAndSetSuccess() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            bean.value = 10;
            boolean result = accessor.compareAndSet(bean, 10, 20);
            assertThat(result).isTrue();
            assertThat(bean.value).isEqualTo(20);
        }

        @Test
        @DisplayName("CAS失败")
        void testCompareAndSetFailure() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            bean.value = 10;
            boolean result = accessor.compareAndSet(bean, 5, 20);
            assertThat(result).isFalse();
            assertThat(bean.value).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("compareAndExchange方法测试")
    class CompareAndExchangeTests {

        @Test
        @DisplayName("原子比较并交换")
        void testCompareAndExchange() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            bean.value = 10;
            Object witness = accessor.compareAndExchange(bean, 10, 20);
            assertThat(witness).isEqualTo(10);
            assertThat(bean.value).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("getAndAdd方法测试")
    class GetAndAddTests {

        @Test
        @DisplayName("原子加法")
        void testGetAndAdd() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            bean.value = 10;
            Object old = accessor.getAndAdd(bean, 5);
            assertThat(old).isEqualTo(10);
            assertThat(bean.value).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("getAcquire和setRelease方法测试")
    class AcquireReleaseTests {

        @Test
        @DisplayName("获取/释放语义读写")
        void testAcquireRelease() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            TestBean bean = new TestBean();
            accessor.setRelease(bean, 42);
            assertThat(accessor.getAcquire(bean)).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("equals方法测试")
    class EqualsTests {

        @Test
        @DisplayName("相同字段相等")
        void testEquals() {
            VarHandleAccessor<TestBean> a1 = VarHandleAccessor.of(TestBean.class, "value");
            VarHandleAccessor<TestBean> a2 = VarHandleAccessor.of(TestBean.class, "value");
            assertThat(a1).isEqualTo(a2);
        }
    }

    @Nested
    @DisplayName("hashCode方法测试")
    class HashCodeTests {

        @Test
        @DisplayName("相同字段有相同hashCode")
        void testHashCode() {
            VarHandleAccessor<TestBean> a1 = VarHandleAccessor.of(TestBean.class, "value");
            VarHandleAccessor<TestBean> a2 = VarHandleAccessor.of(TestBean.class, "value");
            assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString方法测试")
    class ToStringTests {

        @Test
        @DisplayName("toString包含信息")
        void testToString() {
            VarHandleAccessor<TestBean> accessor = VarHandleAccessor.of(TestBean.class, "value");
            assertThat(accessor.toString()).contains("VarHandleAccessor");
            assertThat(accessor.toString()).contains("value");
        }
    }

    // Test helper class
    static class TestBean {
        int value;
        final int finalValue = 100;
        static int staticValue;
    }
}
