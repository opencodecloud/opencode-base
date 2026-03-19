package cloud.opencode.base.core.reflect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.lang.invoke.VarHandle;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * UnsafeUtil 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@DisplayName("UnsafeUtil 测试")
class UnsafeUtilTest {

    @Nested
    @DisplayName("后端检测测试")
    class BackendDetectionTests {

        @Test
        @DisplayName("getJavaVersion 返回有效版本")
        void testGetJavaVersion() {
            int version = UnsafeUtil.getJavaVersion();
            assertThat(version).isGreaterThanOrEqualTo(21);
        }

        @Test
        @DisplayName("getMemoryBackendName 返回有效名称")
        void testGetMemoryBackendName() {
            String backendName = UnsafeUtil.getMemoryBackendName();
            assertThat(backendName).isIn(
                "FFM (Foreign Function & Memory API)",
                "Unsafe (sun.misc.Unsafe)"
            );
            System.out.println("当前内存后端: " + backendName);
        }

        @Test
        @DisplayName("isUsingFFM 根据 Java 版本返回正确值")
        void testIsUsingFFM() {
            boolean usingFFM = UnsafeUtil.isUsingFFM();
            int javaVersion = UnsafeUtil.getJavaVersion();

            if (javaVersion >= 22) {
                // Java 22+ 应该使用 FFM (除非 FFM 初始化失败)
                System.out.println("Java " + javaVersion + ": isUsingFFM=" + usingFFM);
            } else {
                // Java 21 应该使用 Unsafe
                assertThat(usingFFM).isFalse();
            }
        }
    }

    // 测试用类
    static class TestClass {
        int intValue;
        long longValue;
        Object objectValue;
        volatile int volatileInt;
        volatile long volatileLong;
        volatile Object volatileObject;
        static String staticField = "static";
    }

    @Nested
    @DisplayName("可用性检查测试")
    class AvailabilityTests {

        @Test
        @DisplayName("isAvailable - Unsafe 懒加载检查")
        void testIsAvailable() {
            // Unsafe 在大多数 JDK 上应该可用
            assertThat(UnsafeUtil.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("getUnsafe 可用时返回实例")
        void testGetUnsafeAvailable() {
            assumeTrue(UnsafeUtil.isAvailable());
            assertThat(UnsafeUtil.getUnsafe()).isNotNull();
        }
    }

    @Nested
    @DisplayName("VarHandle 测试")
    class VarHandleTests {

        @Test
        @DisplayName("findVarHandle 获取实例字段")
        void testFindVarHandle() {
            VarHandle vh = UnsafeUtil.findVarHandle(TestClass.class, "intValue", int.class);
            assertThat(vh).isNotNull();
        }

        @Test
        @DisplayName("findStaticVarHandle 获取静态字段")
        void testFindStaticVarHandle() {
            VarHandle vh = UnsafeUtil.findStaticVarHandle(TestClass.class, "staticField", String.class);
            assertThat(vh).isNotNull();
        }

        @Test
        @DisplayName("arrayVarHandle 获取数组句柄")
        void testArrayVarHandle() {
            VarHandle vh = UnsafeUtil.arrayVarHandle(int[].class);
            assertThat(vh).isNotNull();
        }

        @Test
        @DisplayName("findVarHandle 不存在的字段抛异常")
        void testFindVarHandleNotFound() {
            assertThatThrownBy(() -> UnsafeUtil.findVarHandle(TestClass.class, "nonExistent", int.class))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to find VarHandle");
        }
    }

    @Nested
    @DisplayName("新 CAS 操作测试 (VarHandle，无警告)")
    class NewCASTests {

        @Test
        @DisplayName("compareAndSetInt 使用 VarHandle")
        void testCompareAndSetIntVarHandle() {
            TestClass obj = new TestClass();
            VarHandle vh = UnsafeUtil.findVarHandle(TestClass.class, "intValue", int.class);

            boolean result = UnsafeUtil.compareAndSetInt(vh, obj, 0, 42);
            assertThat(result).isTrue();
            assertThat(obj.intValue).isEqualTo(42);
        }

        @Test
        @DisplayName("compareAndSetLong 使用 VarHandle")
        void testCompareAndSetLongVarHandle() {
            TestClass obj = new TestClass();
            VarHandle vh = UnsafeUtil.findVarHandle(TestClass.class, "longValue", long.class);

            boolean result = UnsafeUtil.compareAndSetLong(vh, obj, 0L, 100L);
            assertThat(result).isTrue();
            assertThat(obj.longValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("compareAndSetObject 使用 VarHandle")
        void testCompareAndSetObjectVarHandle() {
            TestClass obj = new TestClass();
            VarHandle vh = UnsafeUtil.findVarHandle(TestClass.class, "objectValue", Object.class);

            boolean result = UnsafeUtil.compareAndSetObject(vh, obj, null, "new");
            assertThat(result).isTrue();
            assertThat(obj.objectValue).isEqualTo("new");
        }

        @Test
        @DisplayName("compareAndSetArrayInt")
        void testCompareAndSetArrayInt() {
            int[] array = {0, 0, 0};
            boolean result = UnsafeUtil.compareAndSetArrayInt(array, 1, 0, 42);
            assertThat(result).isTrue();
            assertThat(array[1]).isEqualTo(42);
        }

        @Test
        @DisplayName("compareAndSetArrayLong")
        void testCompareAndSetArrayLong() {
            long[] array = {0L, 0L, 0L};
            boolean result = UnsafeUtil.compareAndSetArrayLong(array, 1, 0L, 100L);
            assertThat(result).isTrue();
            assertThat(array[1]).isEqualTo(100L);
        }

        @Test
        @DisplayName("compareAndSetArrayObject")
        void testCompareAndSetArrayObject() {
            Object[] array = {null, null, null};
            boolean result = UnsafeUtil.compareAndSetArrayObject(array, 1, null, "test");
            assertThat(result).isTrue();
            assertThat(array[1]).isEqualTo("test");
        }
    }

    @Nested
    @DisplayName("新 Volatile 访问测试 (VarHandle，无警告)")
    class NewVolatileTests {

        @Test
        @DisplayName("getIntVolatile / putIntVolatile 使用 VarHandle")
        void testIntVolatileVarHandle() {
            TestClass obj = new TestClass();
            VarHandle vh = UnsafeUtil.findVarHandle(TestClass.class, "volatileInt", int.class);

            UnsafeUtil.putIntVolatile(vh, obj, 100);
            assertThat(UnsafeUtil.getIntVolatile(vh, obj)).isEqualTo(100);
        }

        @Test
        @DisplayName("getLongVolatile / putLongVolatile 使用 VarHandle")
        void testLongVolatileVarHandle() {
            TestClass obj = new TestClass();
            VarHandle vh = UnsafeUtil.findVarHandle(TestClass.class, "volatileLong", long.class);

            UnsafeUtil.putLongVolatile(vh, obj, 200L);
            assertThat(UnsafeUtil.getLongVolatile(vh, obj)).isEqualTo(200L);
        }

        @Test
        @DisplayName("getObjectVolatile / putObjectVolatile 使用 VarHandle")
        void testObjectVolatileVarHandle() {
            TestClass obj = new TestClass();
            VarHandle vh = UnsafeUtil.findVarHandle(TestClass.class, "volatileObject", Object.class);

            UnsafeUtil.putObjectVolatile(vh, obj, "volatile value");
            assertThat(UnsafeUtil.getObjectVolatile(vh, obj)).isEqualTo("volatile value");
        }
    }

    @Nested
    @DisplayName("数组操作测试")
    class ArrayTests {

        @Test
        @DisplayName("arrayBaseOffset")
        void testArrayBaseOffset() {
            int intOffset = UnsafeUtil.arrayBaseOffset(int[].class);
            int longOffset = UnsafeUtil.arrayBaseOffset(long[].class);
            int objOffset = UnsafeUtil.arrayBaseOffset(Object[].class);

            assertThat(intOffset).isGreaterThan(0);
            assertThat(longOffset).isGreaterThan(0);
            assertThat(objOffset).isGreaterThan(0);
        }

        @Test
        @DisplayName("arrayIndexScale")
        void testArrayIndexScale() {
            int intScale = UnsafeUtil.arrayIndexScale(int[].class);
            int longScale = UnsafeUtil.arrayIndexScale(long[].class);
            int byteScale = UnsafeUtil.arrayIndexScale(byte[].class);

            assertThat(intScale).isEqualTo(4); // int 是 4 字节
            assertThat(longScale).isEqualTo(8); // long 是 8 字节
            assertThat(byteScale).isEqualTo(1); // byte 是 1 字节
        }

        @Test
        @DisplayName("arrayBaseOffset 非数组类抛异常")
        void testArrayBaseOffsetNonArray() {
            assertThatThrownBy(() -> UnsafeUtil.arrayBaseOffset(String.class))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not an array class");
        }
    }

    @Nested
    @DisplayName("其他操作测试")
    @EnabledIf("cloud.opencode.base.core.reflect.UnsafeUtilTest#unsafeAvailable")
    class OtherOperationsTests {

        @Test
        @DisplayName("pageSize")
        void testPageSize() {
            int pageSize = UnsafeUtil.pageSize();
            assertThat(pageSize).isGreaterThan(0);
        }

        @Test
        @DisplayName("allocateInstance 不调用构造器")
        void testAllocateInstance() {
            TestClassWithConstructor instance = UnsafeUtil.allocateInstance(TestClassWithConstructor.class);
            // 构造器不会被调用，所以 initialized 应该是 false
            assertThat(instance).isNotNull();
            assertThat(instance.initialized).isFalse();
        }

        @Test
        @DisplayName("throwException 抛出检查异常")
        void testThrowException() {
            assertThatThrownBy(() -> {
                UnsafeUtil.throwException(new java.io.IOException("Test exception"));
            }).isInstanceOf(java.io.IOException.class)
                    .hasMessage("Test exception");
        }
    }

    @Nested
    @DisplayName("兼容旧 API 测试 (使用 VarHandle，无警告)")
    class LegacyAPITests {

        @Test
        @DisplayName("objectFieldOffset 兼容旧 API")
        void testLegacyObjectFieldOffset() {
            long offset = UnsafeUtil.objectFieldOffset(TestClass.class, "intValue");
            // 新实现使用合成偏移量
            assertThat(offset).isNotEqualTo(0);
        }

        @Test
        @DisplayName("compareAndSwapInt 兼容旧 API")
        @SuppressWarnings("deprecation")
        void testLegacyCompareAndSwapInt() {
            TestClass obj = new TestClass();
            long offset = UnsafeUtil.objectFieldOffset(TestClass.class, "intValue");

            boolean result = UnsafeUtil.compareAndSwapInt(obj, offset, 0, 42);
            assertThat(result).isTrue();
            assertThat(obj.intValue).isEqualTo(42);
        }

        @Test
        @DisplayName("compareAndSwapLong 兼容旧 API")
        @SuppressWarnings("deprecation")
        void testLegacyCompareAndSwapLong() {
            TestClass obj = new TestClass();
            long offset = UnsafeUtil.objectFieldOffset(TestClass.class, "longValue");

            boolean result = UnsafeUtil.compareAndSwapLong(obj, offset, 0L, 100L);
            assertThat(result).isTrue();
            assertThat(obj.longValue).isEqualTo(100L);
        }

        @Test
        @DisplayName("compareAndSwapObject 兼容旧 API")
        @SuppressWarnings("deprecation")
        void testLegacyCompareAndSwapObject() {
            TestClass obj = new TestClass();
            long offset = UnsafeUtil.objectFieldOffset(TestClass.class, "objectValue");

            boolean result = UnsafeUtil.compareAndSwapObject(obj, offset, null, "new");
            assertThat(result).isTrue();
            assertThat(obj.objectValue).isEqualTo("new");
        }

        @Test
        @DisplayName("getIntVolatile / putIntVolatile 兼容旧 API")
        @SuppressWarnings("deprecation")
        void testLegacyIntVolatile() {
            TestClass obj = new TestClass();
            long offset = UnsafeUtil.objectFieldOffset(TestClass.class, "volatileInt");

            UnsafeUtil.putIntVolatile(obj, offset, 100);
            assertThat(UnsafeUtil.getIntVolatile(obj, offset)).isEqualTo(100);
        }

        @Test
        @DisplayName("getLongVolatile / putLongVolatile 兼容旧 API")
        @SuppressWarnings("deprecation")
        void testLegacyLongVolatile() {
            TestClass obj = new TestClass();
            long offset = UnsafeUtil.objectFieldOffset(TestClass.class, "volatileLong");

            UnsafeUtil.putLongVolatile(obj, offset, 200L);
            assertThat(UnsafeUtil.getLongVolatile(obj, offset)).isEqualTo(200L);
        }

        @Test
        @DisplayName("getObjectVolatile / putObjectVolatile 兼容旧 API")
        @SuppressWarnings("deprecation")
        void testLegacyObjectVolatile() {
            TestClass obj = new TestClass();
            long offset = UnsafeUtil.objectFieldOffset(TestClass.class, "volatileObject");

            UnsafeUtil.putObjectVolatile(obj, offset, "volatile value");
            assertThat(UnsafeUtil.getObjectVolatile(obj, offset)).isEqualTo("volatile value");
        }
    }

    @Nested
    @DisplayName("直接内存操作测试 (FFM 或 Unsafe)")
    @EnabledIf("cloud.opencode.base.core.reflect.UnsafeUtilTest#unsafeAvailable")
    class DirectMemoryTests {

        @Test
        @DisplayName("allocateMemory 和 freeMemory")
        void testAllocateAndFreeMemory() {
            System.out.println("使用后端: " + UnsafeUtil.getMemoryBackendName());
            long address = UnsafeUtil.allocateMemory(64);
            assertThat(address).isGreaterThan(0);
            UnsafeUtil.freeMemory(address);
        }

        @Test
        @DisplayName("内存读写操作")
        void testMemoryReadWrite() {
            long address = UnsafeUtil.allocateMemory(64);
            try {
                UnsafeUtil.putByte(address, (byte) 42);
                assertThat(UnsafeUtil.getByte(address)).isEqualTo((byte) 42);

                UnsafeUtil.putShort(address + 2, (short) 1234);
                assertThat(UnsafeUtil.getShort(address + 2)).isEqualTo((short) 1234);

                UnsafeUtil.putInt(address + 4, 12345678);
                assertThat(UnsafeUtil.getInt(address + 4)).isEqualTo(12345678);

                UnsafeUtil.putLong(address + 8, 123456789012345L);
                assertThat(UnsafeUtil.getLong(address + 8)).isEqualTo(123456789012345L);

                UnsafeUtil.putFloat(address + 16, 3.14f);
                assertThat(UnsafeUtil.getFloat(address + 16)).isEqualTo(3.14f);

                UnsafeUtil.putDouble(address + 24, 3.14159265359);
                assertThat(UnsafeUtil.getDouble(address + 24)).isEqualTo(3.14159265359);
            } finally {
                UnsafeUtil.freeMemory(address);
            }
        }

        @Test
        @DisplayName("setMemory")
        void testSetMemory() {
            long address = UnsafeUtil.allocateMemory(16);
            try {
                UnsafeUtil.setMemory(address, 16, (byte) 0xFF);
                assertThat(UnsafeUtil.getByte(address)).isEqualTo((byte) 0xFF);
            } finally {
                UnsafeUtil.freeMemory(address);
            }
        }

        @Test
        @DisplayName("copyMemory")
        void testCopyMemory() {
            long src = UnsafeUtil.allocateMemory(8);
            long dest = UnsafeUtil.allocateMemory(8);
            try {
                UnsafeUtil.putLong(src, 12345678L);
                UnsafeUtil.copyMemory(src, dest, 8);
                assertThat(UnsafeUtil.getLong(dest)).isEqualTo(12345678L);
            } finally {
                UnsafeUtil.freeMemory(src);
                UnsafeUtil.freeMemory(dest);
            }
        }

        @Test
        @DisplayName("reallocateMemory")
        void testReallocateMemory() {
            long address = UnsafeUtil.allocateMemory(8);
            try {
                UnsafeUtil.putLong(address, 42L);
                long newAddress = UnsafeUtil.reallocateMemory(address, 16);
                assertThat(newAddress).isGreaterThan(0);
                assertThat(UnsafeUtil.getLong(newAddress)).isEqualTo(42L);
                UnsafeUtil.freeMemory(newAddress);
            } catch (Exception e) {
                UnsafeUtil.freeMemory(address);
                throw e;
            }
        }
    }

    // 用于测试 allocateInstance
    static class TestClassWithConstructor {
        boolean initialized = false;

        public TestClassWithConstructor() {
            initialized = true;
        }
    }

    // 条件方法
    static boolean unsafeAvailable() {
        return UnsafeUtil.isAvailable();
    }
}
