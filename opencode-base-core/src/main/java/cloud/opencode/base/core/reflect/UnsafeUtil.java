package cloud.opencode.base.core.reflect;

import cloud.opencode.base.core.exception.OpenException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unsafe Utility Class - Modern low-level operations utility
 * 底层操作工具类 - 现代化的底层操作封装
 *
 * <p>Provides modern alternatives to sun.misc.Unsafe using VarHandle and FFM API where possible.</p>
 * <p>尽可能使用 VarHandle 和 FFM API 提供 sun.misc.Unsafe 的现代替代方案。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>CAS operations via VarHandle (no warnings) - 通过 VarHandle 实现 CAS 操作 (无警告)</li>
 *   <li>Volatile field access via VarHandle - 通过 VarHandle 实现 volatile 字段访问</li>
 *   <li>Direct memory via FFM API (Java 22+) or Unsafe (Java 21) - 通过 FFM API 或 Unsafe 实现直接内存</li>
 *   <li>Instance allocation without constructor - 无构造器实例分配</li>
 * </ul>
 *
 * <p><strong>Memory Backend Selection | 内存后端选择:</strong></p>
 * <ul>
 *   <li>Java 22+: Uses Foreign Function &amp; Memory API (no deprecation warnings)</li>
 *   <li>Java 21: Falls back to sun.misc.Unsafe (with deprecation warnings)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // CAS operation with VarHandle (recommended, no warnings)
 * VarHandle vh = UnsafeUtil.findVarHandle(MyClass.class, "value", int.class);
 * UnsafeUtil.compareAndSetInt(vh, obj, 0, 1);
 *
 * // Legacy offset-based API (backward compatible)
 * long offset = UnsafeUtil.objectFieldOffset(MyClass.class, "value");
 * UnsafeUtil.compareAndSwapInt(obj, offset, 0, 1);
 *
 * // Direct memory (uses FFM on Java 22+, Unsafe on Java 21)
 * long addr = UnsafeUtil.allocateMemory(1024);
 * UnsafeUtil.putInt(addr, 42);
 * UnsafeUtil.freeMemory(addr);
 *
 * // Check which backend is being used
 * System.out.println("Using FFM: " + UnsafeUtil.isUsingFFM());
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, uses ConcurrentHashMap for allocations - 线程安全: 是，使用ConcurrentHashMap管理分配</li>
 *   <li>Null-safe: No, null arguments throw exceptions - 空值安全: 否，null参数抛出异常</li>
 *   <li>Direct memory access requires careful lifecycle management - 直接内存访问需要仔细的生命周期管理</li>
 *   <li>Prefers FFM API on Java 22+ over deprecated sun.misc.Unsafe - Java 22+上优先使用FFM API</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per operation - 每次操作 O(1)</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class UnsafeUtil {

    // VarHandle cache for field access
    private static final ConcurrentHashMap<FieldKey, VarHandle> FIELD_VAR_HANDLES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, VarHandle> ARRAY_VAR_HANDLES = new ConcurrentHashMap<>();

    // Memory backend selection
    private static volatile MemoryBackend MEMORY_BACKEND;
    private static volatile boolean BACKEND_INITIALIZED = false;

    // Unsafe is lazily initialized only when needed (fallback for Java 21)
    private static volatile Object UNSAFE;
    private static volatile boolean UNSAFE_AVAILABLE;
    private static volatile boolean UNSAFE_CHECKED = false;

    // Cached methods for Unsafe operations (lazily initialized)
    private static volatile Method allocateMemoryMethod;
    private static volatile Method reallocateMemoryMethod;
    private static volatile Method freeMemoryMethod;
    private static volatile Method setMemoryMethod;
    private static volatile Method copyMemoryMethod;
    private static volatile Method putByteMethod;
    private static volatile Method getByteMethod;
    private static volatile Method putShortMethod;
    private static volatile Method getShortMethod;
    private static volatile Method putIntAddrMethod;
    private static volatile Method getIntAddrMethod;
    private static volatile Method putLongAddrMethod;
    private static volatile Method getLongAddrMethod;
    private static volatile Method putFloatMethod;
    private static volatile Method getFloatMethod;
    private static volatile Method putDoubleMethod;
    private static volatile Method getDoubleMethod;
    private static volatile Method pageSizeMethod;
    private static volatile Method allocateInstanceMethod;

    // FFM API handles (Java 22+)
    private static volatile MethodHandle arenaOfAutoHandle; // Arena.ofAuto()
    private static volatile MethodHandle arenaAllocateHandle;
    private static volatile MethodHandle memorySegmentAddressHandle;
    private static volatile MethodHandle memorySegmentReinterpretHandle;
    private static volatile MethodHandle memorySegmentSetByteHandle;
    private static volatile MethodHandle memorySegmentGetByteHandle;
    private static volatile MethodHandle memorySegmentSetShortHandle;
    private static volatile MethodHandle memorySegmentGetShortHandle;
    private static volatile MethodHandle memorySegmentSetIntHandle;
    private static volatile MethodHandle memorySegmentGetIntHandle;
    private static volatile MethodHandle memorySegmentSetLongHandle;
    private static volatile MethodHandle memorySegmentGetLongHandle;
    private static volatile MethodHandle memorySegmentSetFloatHandle;
    private static volatile MethodHandle memorySegmentGetFloatHandle;
    private static volatile MethodHandle memorySegmentSetDoubleHandle;
    private static volatile MethodHandle memorySegmentGetDoubleHandle;
    private static volatile MethodHandle memorySegmentFillHandle;
    private static volatile MethodHandle memorySegmentCopyFromHandle;
    private static volatile MethodHandle memorySegmentByteSizeHandle;
    private static volatile MethodHandle memorySegmentOfAddressHandle; // MemorySegment.ofAddress
    private static volatile MethodHandle memorySegmentAsSliceHandle; // MemorySegment.asSlice(long, long)
    private static volatile Object byteLayout; // ValueLayout.JAVA_BYTE
    private static volatile Object shortLayout;
    private static volatile Object intLayout;
    private static volatile Object longLayout;
    private static volatile Object floatLayout;
    private static volatile Object doubleLayout;

    // Track allocated segments for FFM (address -> {segment, arena, size}), sorted for O(log n) lookup
    private static final java.util.concurrent.ConcurrentSkipListMap<Long, FFMAllocation> FFM_ALLOCATIONS =
            new java.util.concurrent.ConcurrentSkipListMap<>();

    private record FFMAllocation(Object segment, Object arena, long size) {}
    private record SegmentAndOffset(Object segment, long offset) {}

    /**
     * Find the segment containing the given address and calculate the offset
     */
    private static SegmentAndOffset findSegmentForAddress(long address, long minSize) {
        var entry = FFM_ALLOCATIONS.floorEntry(address);
        if (entry != null) {
            long baseAddr = entry.getKey();
            FFMAllocation alloc = entry.getValue();
            long offset = address - baseAddr;
            // Use subtraction instead of addition to avoid overflow
            if (offset >= 0 && minSize <= alloc.size() - offset) {
                return new SegmentAndOffset(alloc.segment(), offset);
            }
        }
        throw new IllegalStateException("No FFM allocation found containing address: " + address);
    }

    private enum MemoryBackend {
        FFM,    // Foreign Function & Memory API (Java 22+)
        UNSAFE  // sun.misc.Unsafe (Java 21, deprecated)
    }

    private UnsafeUtil() {
    }

    // ==================== Backend Detection & Initialization ====================

    /**
     * Checks if FFM API is being used
     * 检查是否正在使用 FFM API
     */
    public static boolean isUsingFFM() {
        ensureBackendInitialized();
        return MEMORY_BACKEND == MemoryBackend.FFM;
    }

    /**
     * Gets the name of the current memory backend
     * 获取当前使用的内存后端名称
     */
    public static String getMemoryBackendName() {
        ensureBackendInitialized();
        return MEMORY_BACKEND == MemoryBackend.FFM ? "FFM (Foreign Function & Memory API)" : "Unsafe (sun.misc.Unsafe)";
    }

    /**
     * Gets the current Java version
     * 获取当前 Java 版本
     */
    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, 3));
        }
        int dot = version.indexOf('.');
        if (dot > 0) {
            return Integer.parseInt(version.substring(0, dot));
        }
        // Handle versions like "22" without dots
        try {
            return Integer.parseInt(version.split("-")[0]);
        } catch (NumberFormatException e) {
            return 21; // Default to 21
        }
    }

    private static void ensureBackendInitialized() {
        if (!BACKEND_INITIALIZED) {
            synchronized (UnsafeUtil.class) {
                if (!BACKEND_INITIALIZED) {
                    // Try FFM first (Java 22+)
                    if (getJavaVersion() >= 22 && tryInitFFM()) {
                        MEMORY_BACKEND = MemoryBackend.FFM;
                    } else {
                        // Fallback to Unsafe
                        ensureUnsafeInitialized();
                        MEMORY_BACKEND = UNSAFE_AVAILABLE ? MemoryBackend.UNSAFE : null;
                    }
                    BACKEND_INITIALIZED = true;
                }
            }
        }
    }

    private static boolean tryInitFFM() {
        try {
            // Use full-privilege lookup for caller-sensitive methods like Arena.ofAuto()
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            // Load FFM classes
            Class<?> arenaClass = Class.forName("java.lang.foreign.Arena");
            Class<?> memorySegmentClass = Class.forName("java.lang.foreign.MemorySegment");
            Class<?> valueLayoutClass = Class.forName("java.lang.foreign.ValueLayout");

            // Get Arena.ofAuto() - creates arenas with automatic memory management
            // Note: Arena.ofAuto() is caller-sensitive, so we need full lookup privileges
            arenaOfAutoHandle = lookup.findStatic(arenaClass, "ofAuto", MethodType.methodType(arenaClass));

            // Get Arena.allocate(long)
            arenaAllocateHandle = lookup.findVirtual(arenaClass, "allocate",
                MethodType.methodType(memorySegmentClass, long.class));

            // Get MemorySegment.address()
            memorySegmentAddressHandle = lookup.findVirtual(memorySegmentClass, "address",
                MethodType.methodType(long.class));

            // Get MemorySegment.byteSize()
            memorySegmentByteSizeHandle = lookup.findVirtual(memorySegmentClass, "byteSize",
                MethodType.methodType(long.class));

            // Get MemorySegment.reinterpret(long)
            memorySegmentReinterpretHandle = lookup.findVirtual(memorySegmentClass, "reinterpret",
                MethodType.methodType(memorySegmentClass, long.class));

            // Get MemorySegment.asSlice(long, long)
            memorySegmentAsSliceHandle = lookup.findVirtual(memorySegmentClass, "asSlice",
                MethodType.methodType(memorySegmentClass, long.class, long.class));

            // Get ValueLayouts
            byteLayout = lookup.findStaticGetter(valueLayoutClass, "JAVA_BYTE",
                Class.forName("java.lang.foreign.ValueLayout$OfByte")).invoke();
            shortLayout = lookup.findStaticGetter(valueLayoutClass, "JAVA_SHORT",
                Class.forName("java.lang.foreign.ValueLayout$OfShort")).invoke();
            intLayout = lookup.findStaticGetter(valueLayoutClass, "JAVA_INT",
                Class.forName("java.lang.foreign.ValueLayout$OfInt")).invoke();
            longLayout = lookup.findStaticGetter(valueLayoutClass, "JAVA_LONG",
                Class.forName("java.lang.foreign.ValueLayout$OfLong")).invoke();
            floatLayout = lookup.findStaticGetter(valueLayoutClass, "JAVA_FLOAT",
                Class.forName("java.lang.foreign.ValueLayout$OfFloat")).invoke();
            doubleLayout = lookup.findStaticGetter(valueLayoutClass, "JAVA_DOUBLE",
                Class.forName("java.lang.foreign.ValueLayout$OfDouble")).invoke();

            // Get MemorySegment.set/get methods
            Class<?> ofByteClass = Class.forName("java.lang.foreign.ValueLayout$OfByte");
            Class<?> ofShortClass = Class.forName("java.lang.foreign.ValueLayout$OfShort");
            Class<?> ofIntClass = Class.forName("java.lang.foreign.ValueLayout$OfInt");
            Class<?> ofLongClass = Class.forName("java.lang.foreign.ValueLayout$OfLong");
            Class<?> ofFloatClass = Class.forName("java.lang.foreign.ValueLayout$OfFloat");
            Class<?> ofDoubleClass = Class.forName("java.lang.foreign.ValueLayout$OfDouble");

            memorySegmentSetByteHandle = lookup.findVirtual(memorySegmentClass, "set",
                MethodType.methodType(void.class, ofByteClass, long.class, byte.class));
            memorySegmentGetByteHandle = lookup.findVirtual(memorySegmentClass, "get",
                MethodType.methodType(byte.class, ofByteClass, long.class));

            memorySegmentSetShortHandle = lookup.findVirtual(memorySegmentClass, "set",
                MethodType.methodType(void.class, ofShortClass, long.class, short.class));
            memorySegmentGetShortHandle = lookup.findVirtual(memorySegmentClass, "get",
                MethodType.methodType(short.class, ofShortClass, long.class));

            memorySegmentSetIntHandle = lookup.findVirtual(memorySegmentClass, "set",
                MethodType.methodType(void.class, ofIntClass, long.class, int.class));
            memorySegmentGetIntHandle = lookup.findVirtual(memorySegmentClass, "get",
                MethodType.methodType(int.class, ofIntClass, long.class));

            memorySegmentSetLongHandle = lookup.findVirtual(memorySegmentClass, "set",
                MethodType.methodType(void.class, ofLongClass, long.class, long.class));
            memorySegmentGetLongHandle = lookup.findVirtual(memorySegmentClass, "get",
                MethodType.methodType(long.class, ofLongClass, long.class));

            memorySegmentSetFloatHandle = lookup.findVirtual(memorySegmentClass, "set",
                MethodType.methodType(void.class, ofFloatClass, long.class, float.class));
            memorySegmentGetFloatHandle = lookup.findVirtual(memorySegmentClass, "get",
                MethodType.methodType(float.class, ofFloatClass, long.class));

            memorySegmentSetDoubleHandle = lookup.findVirtual(memorySegmentClass, "set",
                MethodType.methodType(void.class, ofDoubleClass, long.class, double.class));
            memorySegmentGetDoubleHandle = lookup.findVirtual(memorySegmentClass, "get",
                MethodType.methodType(double.class, ofDoubleClass, long.class));

            // Get MemorySegment.fill(byte)
            memorySegmentFillHandle = lookup.findVirtual(memorySegmentClass, "fill",
                MethodType.methodType(memorySegmentClass, byte.class));

            // Get MemorySegment.copyFrom(MemorySegment)
            memorySegmentCopyFromHandle = lookup.findVirtual(memorySegmentClass, "copyFrom",
                MethodType.methodType(memorySegmentClass, memorySegmentClass));

            // Test allocation to verify FFM is working
            Object testArena = arenaOfAutoHandle.invoke();
            Object testSegment = arenaAllocateHandle.invoke(testArena, 8L);
            long testAddr = (long) memorySegmentAddressHandle.invoke(testSegment);
            if (testAddr == 0) {
                return false;
            }

            return true;
        } catch (Throwable t) {
            // FFM not available, will fall back to Unsafe
            return false;
        }
    }

    // ==================== VarHandle Operations (No Warnings) ====================

    /**
     * Gets the VarHandle for a field
     * 获取字段的 VarHandle
     */
    public static VarHandle findVarHandle(Class<?> clazz, String fieldName, Class<?> fieldType) {
        FieldKey key = new FieldKey(clazz, fieldName);
        return FIELD_VAR_HANDLES.computeIfAbsent(key, k -> {
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                return lookup.findVarHandle(clazz, fieldName, fieldType);
            } catch (Exception e) {
                throw new OpenException("Failed to find VarHandle for field: " + fieldName, e);
            }
        });
    }

    /**
     * Gets the VarHandle for a static field
     * 获取静态字段的 VarHandle
     */
    public static VarHandle findStaticVarHandle(Class<?> clazz, String fieldName, Class<?> fieldType) {
        FieldKey key = new FieldKey(clazz, fieldName + "#static");
        return FIELD_VAR_HANDLES.computeIfAbsent(key, k -> {
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                return lookup.findStaticVarHandle(clazz, fieldName, fieldType);
            } catch (Exception e) {
                throw new OpenException("Failed to find static VarHandle for field: " + fieldName, e);
            }
        });
    }

    /**
     * Gets the VarHandle for an array
     * 获取数组的 VarHandle
     */
    public static VarHandle arrayVarHandle(Class<?> arrayClass) {
        return ARRAY_VAR_HANDLES.computeIfAbsent(arrayClass, k ->
            MethodHandles.arrayElementVarHandle(arrayClass));
    }

    // ==================== CAS Operations via VarHandle ====================

    /**
     * CAS sets int value (new API, no warnings)
     * CAS 设置 int 值 (新 API，无警告)
     */
    public static boolean compareAndSetInt(VarHandle vh, Object obj, int expect, int update) {
        return vh.compareAndSet(obj, expect, update);
    }

    /**
     * CAS sets long value (new API, no warnings)
     * CAS 设置 long 值 (新 API，无警告)
     */
    public static boolean compareAndSetLong(VarHandle vh, Object obj, long expect, long update) {
        return vh.compareAndSet(obj, expect, update);
    }

    /**
     * CAS sets object reference (new API, no warnings)
     * CAS 设置对象引用 (新 API，无警告)
     */
    public static boolean compareAndSetObject(VarHandle vh, Object obj, Object expect, Object update) {
        return vh.compareAndSet(obj, expect, update);
    }

    /**
     * CAS sets array element int
     * CAS 设置数组元素 int
     */
    public static boolean compareAndSetArrayInt(int[] array, int index, int expect, int update) {
        VarHandle vh = arrayVarHandle(int[].class);
        return vh.compareAndSet(array, index, expect, update);
    }

    /**
     * CAS sets array element long
     * CAS 设置数组元素 long
     */
    public static boolean compareAndSetArrayLong(long[] array, int index, long expect, long update) {
        VarHandle vh = arrayVarHandle(long[].class);
        return vh.compareAndSet(array, index, expect, update);
    }

    /**
     * CAS sets array element Object
     * CAS 设置数组元素 Object
     */
    public static boolean compareAndSetArrayObject(Object[] array, int index, Object expect, Object update) {
        VarHandle vh = arrayVarHandle(Object[].class);
        return vh.compareAndSet(array, index, expect, update);
    }

    // ==================== Volatile Field Access via VarHandle ====================

    /**
     * Gets int field value (volatile, new API)
     * 获取 int 字段值 (volatile，新 API)
     */
    public static int getIntVolatile(VarHandle vh, Object obj) {
        return (int) vh.getVolatile(obj);
    }

    /**
     * Sets int field value (volatile, new API)
     * 设置 int 字段值 (volatile，新 API)
     */
    public static void putIntVolatile(VarHandle vh, Object obj, int value) {
        vh.setVolatile(obj, value);
    }

    /**
     * Gets long field value (volatile, new API)
     * 获取 long 字段值 (volatile，新 API)
     */
    public static long getLongVolatile(VarHandle vh, Object obj) {
        return (long) vh.getVolatile(obj);
    }

    /**
     * Sets long field value (volatile, new API)
     * 设置 long 字段值 (volatile，新 API)
     */
    public static void putLongVolatile(VarHandle vh, Object obj, long value) {
        vh.setVolatile(obj, value);
    }

    /**
     * Gets object reference (volatile, new API)
     * 获取对象引用 (volatile，新 API)
     */
    public static Object getObjectVolatile(VarHandle vh, Object obj) {
        return vh.getVolatile(obj);
    }

    /**
     * Sets object reference (volatile, new API)
     * 设置对象引用 (volatile，新 API)
     */
    public static void putObjectVolatile(VarHandle vh, Object obj, Object value) {
        vh.setVolatile(obj, value);
    }

    // ==================== Array Operations ====================

    /**
     * Gets the array base offset
     * 获取数组基础偏移量
     */
    public static int arrayBaseOffset(Class<?> arrayClass) {
        Class<?> componentType = arrayClass.getComponentType();
        if (componentType == null) {
            throw new IllegalArgumentException("Not an array class: " + arrayClass);
        }
        // Standard JVM object header size (16 bytes for 64-bit with compressed oops)
        return 16;
    }

    /**
     * Gets the array element scale factor
     * 获取数组元素缩放因子
     */
    public static int arrayIndexScale(Class<?> arrayClass) {
        Class<?> componentType = arrayClass.getComponentType();
        if (componentType == null) {
            throw new IllegalArgumentException("Not an array class: " + arrayClass);
        }
        if (componentType == byte.class || componentType == boolean.class) return 1;
        if (componentType == short.class || componentType == char.class) return 2;
        if (componentType == int.class || componentType == float.class) return 4;
        if (componentType == long.class || componentType == double.class) return 8;
        // Object references: 4 bytes with compressed oops
        return 4;
    }

    // ==================== Unsafe Operations (Lazy Initialization) ====================

    /**
     * Checks if Unsafe is available
     * 检查 Unsafe 是否可用
     */
    public static boolean isAvailable() {
        ensureUnsafeInitialized();
        return UNSAFE_AVAILABLE;
    }

    /**
     * Gets the Unsafe instance
     * 获取 Unsafe 实例
     */
    public static Object getUnsafe() {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        return UNSAFE;
    }

    private static void ensureUnsafeInitialized() {
        if (!UNSAFE_CHECKED) {
            synchronized (UnsafeUtil.class) {
                if (!UNSAFE_CHECKED) {
                    try {
                        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                        Field field = unsafeClass.getDeclaredField("theUnsafe");
                        field.setAccessible(true);
                        UNSAFE = field.get(null);

                        // Cache memory operation methods
                        allocateMemoryMethod = unsafeClass.getMethod("allocateMemory", long.class);
                        reallocateMemoryMethod = unsafeClass.getMethod("reallocateMemory", long.class, long.class);
                        freeMemoryMethod = unsafeClass.getMethod("freeMemory", long.class);
                        setMemoryMethod = unsafeClass.getMethod("setMemory", long.class, long.class, byte.class);
                        copyMemoryMethod = unsafeClass.getMethod("copyMemory", long.class, long.class, long.class);
                        putByteMethod = unsafeClass.getMethod("putByte", long.class, byte.class);
                        getByteMethod = unsafeClass.getMethod("getByte", long.class);
                        putShortMethod = unsafeClass.getMethod("putShort", long.class, short.class);
                        getShortMethod = unsafeClass.getMethod("getShort", long.class);
                        putIntAddrMethod = unsafeClass.getMethod("putInt", long.class, int.class);
                        getIntAddrMethod = unsafeClass.getMethod("getInt", long.class);
                        putLongAddrMethod = unsafeClass.getMethod("putLong", long.class, long.class);
                        getLongAddrMethod = unsafeClass.getMethod("getLong", long.class);
                        putFloatMethod = unsafeClass.getMethod("putFloat", long.class, float.class);
                        getFloatMethod = unsafeClass.getMethod("getFloat", long.class);
                        putDoubleMethod = unsafeClass.getMethod("putDouble", long.class, double.class);
                        getDoubleMethod = unsafeClass.getMethod("getDouble", long.class);
                        pageSizeMethod = unsafeClass.getMethod("pageSize");
                        allocateInstanceMethod = unsafeClass.getMethod("allocateInstance", Class.class);

                        UNSAFE_AVAILABLE = true;
                    } catch (Exception e) {
                        UNSAFE_AVAILABLE = false;
                    }
                    UNSAFE_CHECKED = true;
                }
            }
        }
    }

    /**
     * Allocates an instance (without calling constructor)
     * 分配实例（不调用构造器）
     */
    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> clazz) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available for allocateInstance");
        }
        try {
            return (T) allocateInstanceMethod.invoke(UNSAFE, clazz);
        } catch (Exception e) {
            throw new OpenException("Failed to allocate instance: " + clazz.getName(), e);
        }
    }

    /**
     * Throws an exception (unchecked) - uses sneaky throw, no Unsafe needed
     * 抛出异常（不检查）- 使用 sneaky throw，无需 Unsafe
     */
    public static void throwException(Throwable t) {
        UnsafeUtil.<RuntimeException>sneakyThrow(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Gets the page size
     * 获取页大小
     */
    public static int pageSize() {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            return 4096; // Default page size
        }
        try {
            return (int) pageSizeMethod.invoke(UNSAFE);
        } catch (Exception e) {
            return 4096;
        }
    }

    // ==================== Legacy Offset-based API ====================

    /**
     * Gets the field offset (legacy API compatible)
     * 获取字段偏移量 (兼容旧 API)
     * @deprecated 使用 {@link #findVarHandle(Class, String, Class)} 代替
     */
    @Deprecated
    public static long objectFieldOffset(Class<?> clazz, String fieldName) {
        // Return a synthetic offset that encodes class hash and field name hash
        return ((long) clazz.hashCode() << 32) | (fieldName.hashCode() & 0xFFFFFFFFL);
    }

    /**
     * Gets the field offset (legacy API compatible)
     * 获取字段偏移量 (兼容旧 API)
     * @deprecated 使用 {@link #findVarHandle(Class, String, Class)} 代替
     */
    @Deprecated
    public static long objectFieldOffset(Field field) {
        return objectFieldOffset(field.getDeclaringClass(), field.getName());
    }

    /**
     * Gets the static field offset (legacy API compatible)
     * 获取静态字段偏移量 (兼容旧 API)
     * @deprecated 使用 {@link #findStaticVarHandle(Class, String, Class)} 代替
     */
    @Deprecated
    public static long staticFieldOffset(Field field) {
        return objectFieldOffset(field);
    }

    /**
     * CAS sets int value (legacy API compatible)
     * CAS 设置 int 值 (兼容旧 API)
     * @deprecated 使用 {@link #compareAndSetInt(VarHandle, Object, int, int)} 代替
     */
    @Deprecated
    public static boolean compareAndSwapInt(Object obj, long offset, int expect, int update) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == int.class) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), int.class);
                    return vh.compareAndSet(obj, expect, update);
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * CAS sets long value (legacy API compatible)
     * CAS 设置 long 值 (兼容旧 API)
     * @deprecated 使用 {@link #compareAndSetLong(VarHandle, Object, long, long)} 代替
     */
    @Deprecated
    public static boolean compareAndSwapLong(Object obj, long offset, long expect, long update) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == long.class) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), long.class);
                    return vh.compareAndSet(obj, expect, update);
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * CAS sets object reference (legacy API compatible)
     * CAS 设置对象引用 (兼容旧 API)
     * @deprecated 使用 {@link #compareAndSetObject(VarHandle, Object, Object, Object)} 代替
     */
    @Deprecated
    public static boolean compareAndSwapObject(Object obj, long offset, Object expect, Object update) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), f.getType());
                    return vh.compareAndSet(obj, expect, update);
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * Gets object int field value (legacy API compatible)
     * 获取对象的 int 字段值 (兼容旧 API)
     * @deprecated 使用 {@link #getIntVolatile(VarHandle, Object)} 代替
     */
    @Deprecated
    public static int getIntVolatile(Object obj, long offset) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == int.class) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), int.class);
                    return (int) vh.getVolatile(obj);
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * Sets object int field value (legacy API compatible)
     * 设置对象的 int 字段值 (兼容旧 API)
     * @deprecated 使用 {@link #putIntVolatile(VarHandle, Object, int)} 代替
     */
    @Deprecated
    public static void putIntVolatile(Object obj, long offset, int value) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == int.class) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), int.class);
                    vh.setVolatile(obj, value);
                    return;
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * Gets object long field value (legacy API compatible)
     * 获取对象的 long 字段值 (兼容旧 API)
     * @deprecated 使用 {@link #getLongVolatile(VarHandle, Object)} 代替
     */
    @Deprecated
    public static long getLongVolatile(Object obj, long offset) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == long.class) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), long.class);
                    return (long) vh.getVolatile(obj);
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * Sets object long field value (legacy API compatible)
     * 设置对象的 long 字段值 (兼容旧 API)
     * @deprecated 使用 {@link #putLongVolatile(VarHandle, Object, long)} 代替
     */
    @Deprecated
    public static void putLongVolatile(Object obj, long offset, long value) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == long.class) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), long.class);
                    vh.setVolatile(obj, value);
                    return;
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * Gets object reference field value (legacy API compatible)
     * 获取对象的引用字段值 (兼容旧 API)
     * @deprecated 使用 {@link #getObjectVolatile(VarHandle, Object)} 代替
     */
    @Deprecated
    public static Object getObjectVolatile(Object obj, long offset) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), f.getType());
                    return vh.getVolatile(obj);
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    /**
     * Sets object reference field value (legacy API compatible)
     * 设置对象的引用字段值 (兼容旧 API)
     * @deprecated 使用 {@link #putObjectVolatile(VarHandle, Object, Object)} 代替
     */
    @Deprecated
    public static void putObjectVolatile(Object obj, long offset, Object value) {
        Class<?> clazz = obj.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
                if (objectFieldOffset(f) == offset) {
                    VarHandle vh = findVarHandle(clazz, f.getName(), f.getType());
                    vh.setVolatile(obj, value);
                    return;
                }
            }
        }
        throw new OpenException("Field not found for offset: " + offset);
    }

    // ==================== Direct Memory Operations (FFM or Unsafe) ====================

    /**
     * Allocates direct memory
     * 分配直接内存
     * <p>Java 22+: 使用 FFM API (无警告)</p>
     * <p>Java 21: 使用 Unsafe (有弃用警告)</p>
     */
    public static long allocateMemory(long bytes) {
        if (bytes <= 0) {
            throw new IllegalArgumentException("bytes must be positive: " + bytes);
        }
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return allocateMemoryFFM(bytes);
        } else {
            return allocateMemoryUnsafe(bytes);
        }
    }

    private static long allocateMemoryFFM(long bytes) {
        try {
            // Create a new auto-managed arena for this allocation
            Object arena = arenaOfAutoHandle.invoke();
            Object segment = arenaAllocateHandle.invoke(arena, bytes);
            long address = (long) memorySegmentAddressHandle.invoke(segment);
            FFM_ALLOCATIONS.put(address, new FFMAllocation(segment, arena, bytes));
            return address;
        } catch (Throwable e) {
            throw new OpenException("Failed to allocate memory via FFM", e);
        }
    }

    private static long allocateMemoryUnsafe(long bytes) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (long) allocateMemoryMethod.invoke(UNSAFE, bytes);
        } catch (Exception e) {
            throw new OpenException("Failed to allocate memory", e);
        }
    }

    /**
     * Reallocates direct memory
     * 重新分配直接内存
     * <p>注意: FFM API 不支持 realloc，会分配新内存并复制数据</p>
     */
    public static long reallocateMemory(long address, long bytes) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return reallocateMemoryFFM(address, bytes);
        } else {
            return reallocateMemoryUnsafe(address, bytes);
        }
    }

    private static long reallocateMemoryFFM(long oldAddress, long newBytes) {
        try {
            FFMAllocation oldAlloc = FFM_ALLOCATIONS.get(oldAddress);
            if (oldAlloc == null) {
                // Address not tracked, just allocate new
                return allocateMemoryFFM(newBytes);
            }

            // Allocate new segment
            Object newArena = arenaOfAutoHandle.invoke();
            Object newSegment = arenaAllocateHandle.invoke(newArena, newBytes);
            long newAddress = (long) memorySegmentAddressHandle.invoke(newSegment);

            // Copy data from old to new (copy min of old and new size)
            long copySize = Math.min(oldAlloc.size(), newBytes);
            Object oldSegmentView = memorySegmentReinterpretHandle.invoke(oldAlloc.segment(), copySize);
            Object newSegmentView = memorySegmentReinterpretHandle.invoke(newSegment, copySize);
            memorySegmentCopyFromHandle.invoke(newSegmentView, oldSegmentView);

            // Track new allocation and remove old
            FFM_ALLOCATIONS.put(newAddress, new FFMAllocation(newSegment, newArena, newBytes));
            FFM_ALLOCATIONS.remove(oldAddress);

            return newAddress;
        } catch (Throwable e) {
            throw new OpenException("Failed to reallocate memory via FFM", e);
        }
    }

    private static long reallocateMemoryUnsafe(long address, long bytes) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (long) reallocateMemoryMethod.invoke(UNSAFE, address, bytes);
        } catch (Exception e) {
            throw new OpenException("Failed to reallocate memory", e);
        }
    }

    /**
     * Frees direct memory
     * 释放直接内存
     * <p>FFM: 内存由 Arena 管理，此方法仅移除跟踪</p>
     * <p>Unsafe: 显式释放内存</p>
     */
    public static void freeMemory(long address) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            // FFM uses Arena.ofAuto() which manages memory automatically via GC
            // Just remove from tracking; memory will be freed when arena is GC'd
            FFM_ALLOCATIONS.remove(address);
        } else {
            freeMemoryUnsafe(address);
        }
    }

    private static void freeMemoryUnsafe(long address) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            return;
        }
        try {
            freeMemoryMethod.invoke(UNSAFE, address);
        } catch (Exception e) {
            throw new OpenException("Failed to free memory", e);
        }
    }

    /**
     * Sets memory value
     * 设置内存值
     */
    public static void setMemory(long address, long bytes, byte value) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            setMemoryFFM(address, bytes, value);
        } else {
            setMemoryUnsafe(address, bytes, value);
        }
    }

    private static void setMemoryFFM(long address, long bytes, byte value) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, bytes);
            Object slice = memorySegmentAsSliceHandle.invoke(so.segment(), so.offset(), bytes);
            memorySegmentFillHandle.invoke(slice, value);
        } catch (Throwable e) {
            throw new OpenException("Failed to set memory via FFM", e);
        }
    }

    private static void setMemoryUnsafe(long address, long bytes, byte value) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            setMemoryMethod.invoke(UNSAFE, address, bytes, value);
        } catch (Exception e) {
            throw new OpenException("Failed to set memory", e);
        }
    }

    /**
     * Copies memory
     * 复制内存
     */
    public static void copyMemory(long srcAddress, long destAddress, long bytes) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            copyMemoryFFM(srcAddress, destAddress, bytes);
        } else {
            copyMemoryUnsafe(srcAddress, destAddress, bytes);
        }
    }

    private static void copyMemoryFFM(long srcAddress, long destAddress, long bytes) {
        try {
            SegmentAndOffset srcSo = findSegmentForAddress(srcAddress, bytes);
            SegmentAndOffset destSo = findSegmentForAddress(destAddress, bytes);

            Object srcSlice = memorySegmentAsSliceHandle.invoke(srcSo.segment(), srcSo.offset(), bytes);
            Object destSlice = memorySegmentAsSliceHandle.invoke(destSo.segment(), destSo.offset(), bytes);
            memorySegmentCopyFromHandle.invoke(destSlice, srcSlice);
        } catch (Throwable e) {
            throw new OpenException("Failed to copy memory via FFM", e);
        }
    }

    private static void copyMemoryUnsafe(long srcAddress, long destAddress, long bytes) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            copyMemoryMethod.invoke(UNSAFE, srcAddress, destAddress, bytes);
        } catch (Exception e) {
            throw new OpenException("Failed to copy memory", e);
        }
    }

    /**
     * Writes a byte to memory
     * 写入 byte 到内存
     */
    public static void putByte(long address, byte value) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            putByteFFM(address, value);
        } else {
            putByteUnsafe(address, value);
        }
    }

    private static void putByteFFM(long address, byte value) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 1);
            memorySegmentSetByteHandle.invoke(so.segment, byteLayout, so.offset, value);
        } catch (Throwable e) {
            throw new OpenException("Failed to put byte via FFM", e);
        }
    }

    private static void putByteUnsafe(long address, byte value) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            putByteMethod.invoke(UNSAFE, address, value);
        } catch (Exception e) {
            throw new OpenException("Failed to put byte", e);
        }
    }

    /**
     * Reads a byte from memory
     * 从内存读取 byte
     */
    public static byte getByte(long address) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return getByteFFM(address);
        } else {
            return getByteUnsafe(address);
        }
    }

    private static byte getByteFFM(long address) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 1);
            return (byte) memorySegmentGetByteHandle.invoke(so.segment, byteLayout, so.offset);
        } catch (Throwable e) {
            throw new OpenException("Failed to get byte via FFM", e);
        }
    }

    private static byte getByteUnsafe(long address) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (byte) getByteMethod.invoke(UNSAFE, address);
        } catch (Exception e) {
            throw new OpenException("Failed to get byte", e);
        }
    }

    /**
     * Writes a short to memory
     * 写入 short 到内存
     */
    public static void putShort(long address, short value) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            putShortFFM(address, value);
        } else {
            putShortUnsafe(address, value);
        }
    }

    private static void putShortFFM(long address, short value) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 2);
            memorySegmentSetShortHandle.invoke(so.segment, shortLayout, so.offset, value);
        } catch (Throwable e) {
            throw new OpenException("Failed to put short via FFM", e);
        }
    }

    private static void putShortUnsafe(long address, short value) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            putShortMethod.invoke(UNSAFE, address, value);
        } catch (Exception e) {
            throw new OpenException("Failed to put short", e);
        }
    }

    /**
     * Reads a short from memory
     * 从内存读取 short
     */
    public static short getShort(long address) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return getShortFFM(address);
        } else {
            return getShortUnsafe(address);
        }
    }

    private static short getShortFFM(long address) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 2);
            return (short) memorySegmentGetShortHandle.invoke(so.segment, shortLayout, so.offset);
        } catch (Throwable e) {
            throw new OpenException("Failed to get short via FFM", e);
        }
    }

    private static short getShortUnsafe(long address) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (short) getShortMethod.invoke(UNSAFE, address);
        } catch (Exception e) {
            throw new OpenException("Failed to get short", e);
        }
    }

    /**
     * Writes an int to memory
     * 写入 int 到内存
     */
    public static void putInt(long address, int value) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            putIntFFM(address, value);
        } else {
            putIntUnsafe(address, value);
        }
    }

    private static void putIntFFM(long address, int value) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 4);
            memorySegmentSetIntHandle.invoke(so.segment, intLayout, so.offset, value);
        } catch (Throwable e) {
            throw new OpenException("Failed to put int via FFM", e);
        }
    }

    private static void putIntUnsafe(long address, int value) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            putIntAddrMethod.invoke(UNSAFE, address, value);
        } catch (Exception e) {
            throw new OpenException("Failed to put int", e);
        }
    }

    /**
     * Reads an int from memory
     * 从内存读取 int
     */
    public static int getInt(long address) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return getIntFFM(address);
        } else {
            return getIntUnsafe(address);
        }
    }

    private static int getIntFFM(long address) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 4);
            return (int) memorySegmentGetIntHandle.invoke(so.segment, intLayout, so.offset);
        } catch (Throwable e) {
            throw new OpenException("Failed to get int via FFM", e);
        }
    }

    private static int getIntUnsafe(long address) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (int) getIntAddrMethod.invoke(UNSAFE, address);
        } catch (Exception e) {
            throw new OpenException("Failed to get int", e);
        }
    }

    /**
     * Writes a long to memory
     * 写入 long 到内存
     */
    public static void putLong(long address, long value) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            putLongFFM(address, value);
        } else {
            putLongUnsafe(address, value);
        }
    }

    private static void putLongFFM(long address, long value) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 8);
            memorySegmentSetLongHandle.invoke(so.segment, longLayout, so.offset, value);
        } catch (Throwable e) {
            throw new OpenException("Failed to put long via FFM", e);
        }
    }

    private static void putLongUnsafe(long address, long value) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            putLongAddrMethod.invoke(UNSAFE, address, value);
        } catch (Exception e) {
            throw new OpenException("Failed to put long", e);
        }
    }

    /**
     * Reads a long from memory
     * 从内存读取 long
     */
    public static long getLong(long address) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return getLongFFM(address);
        } else {
            return getLongUnsafe(address);
        }
    }

    private static long getLongFFM(long address) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 8);
            return (long) memorySegmentGetLongHandle.invoke(so.segment, longLayout, so.offset);
        } catch (Throwable e) {
            throw new OpenException("Failed to get long via FFM", e);
        }
    }

    private static long getLongUnsafe(long address) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (long) getLongAddrMethod.invoke(UNSAFE, address);
        } catch (Exception e) {
            throw new OpenException("Failed to get long", e);
        }
    }

    /**
     * Writes a float to memory
     * 写入 float 到内存
     */
    public static void putFloat(long address, float value) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            putFloatFFM(address, value);
        } else {
            putFloatUnsafe(address, value);
        }
    }

    private static void putFloatFFM(long address, float value) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 4);
            memorySegmentSetFloatHandle.invoke(so.segment, floatLayout, so.offset, value);
        } catch (Throwable e) {
            throw new OpenException("Failed to put float via FFM", e);
        }
    }

    private static void putFloatUnsafe(long address, float value) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            putFloatMethod.invoke(UNSAFE, address, value);
        } catch (Exception e) {
            throw new OpenException("Failed to put float", e);
        }
    }

    /**
     * Reads a float from memory
     * 从内存读取 float
     */
    public static float getFloat(long address) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return getFloatFFM(address);
        } else {
            return getFloatUnsafe(address);
        }
    }

    private static float getFloatFFM(long address) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 4);
            return (float) memorySegmentGetFloatHandle.invoke(so.segment, floatLayout, so.offset);
        } catch (Throwable e) {
            throw new OpenException("Failed to get float via FFM", e);
        }
    }

    private static float getFloatUnsafe(long address) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (float) getFloatMethod.invoke(UNSAFE, address);
        } catch (Exception e) {
            throw new OpenException("Failed to get float", e);
        }
    }

    /**
     * Writes a double to memory
     * 写入 double 到内存
     */
    public static void putDouble(long address, double value) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            putDoubleFFM(address, value);
        } else {
            putDoubleUnsafe(address, value);
        }
    }

    private static void putDoubleFFM(long address, double value) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 8);
            memorySegmentSetDoubleHandle.invoke(so.segment, doubleLayout, so.offset, value);
        } catch (Throwable e) {
            throw new OpenException("Failed to put double via FFM", e);
        }
    }

    private static void putDoubleUnsafe(long address, double value) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            putDoubleMethod.invoke(UNSAFE, address, value);
        } catch (Exception e) {
            throw new OpenException("Failed to put double", e);
        }
    }

    /**
     * Reads a double from memory
     * 从内存读取 double
     */
    public static double getDouble(long address) {
        ensureBackendInitialized();
        if (MEMORY_BACKEND == MemoryBackend.FFM) {
            return getDoubleFFM(address);
        } else {
            return getDoubleUnsafe(address);
        }
    }

    private static double getDoubleFFM(long address) {
        try {
            SegmentAndOffset so = findSegmentForAddress(address, 8);
            return (double) memorySegmentGetDoubleHandle.invoke(so.segment, doubleLayout, so.offset);
        } catch (Throwable e) {
            throw new OpenException("Failed to get double via FFM", e);
        }
    }

    private static double getDoubleUnsafe(long address) {
        ensureUnsafeInitialized();
        if (!UNSAFE_AVAILABLE) {
            throw new UnsupportedOperationException("Unsafe is not available");
        }
        try {
            return (double) getDoubleMethod.invoke(UNSAFE, address);
        } catch (Exception e) {
            throw new OpenException("Failed to get double", e);
        }
    }

    // ==================== Helper Classes ====================

    private record FieldKey(Class<?> clazz, String fieldName) {}
}
