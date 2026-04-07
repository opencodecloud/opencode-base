package cloud.opencode.base.classloader.loader;

import cloud.opencode.base.classloader.exception.OpenClassLoaderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for HotSwapClassLoader
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.0
 */
@DisplayName("HotSwapClassLoader Tests")
class HotSwapClassLoaderTest {

    @TempDir
    Path tempDir;

    /**
     * Generate minimal valid bytecode for a class with the given fully-qualified name.
     * The class extends java.lang.Object and has no fields or methods.
     */
    private static byte[] generateBytecode(String className) {
        try {
            String internalName = className.replace('.', '/');
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);

            // Magic number
            out.writeInt(0xCAFEBABE);
            // Minor version
            out.writeShort(0);
            // Major version (Java 8 = 52, safe for all modern JVMs)
            out.writeShort(52);

            // Constant pool
            // We need: 1=this_class, 2=super_class, 3=this_name(Utf8), 4=super_name(Utf8),
            //          5=Code(Utf8), 6=<init>(Utf8), 7=()V(Utf8), 8=super <init> NameAndType,
            //          9=super <init> Methodref
            int cpCount = 10; // constant_pool_count = max index + 1
            out.writeShort(cpCount);

            // #1 Class -> #3
            out.writeByte(7); // CONSTANT_Class
            out.writeShort(3);

            // #2 Class -> #4
            out.writeByte(7); // CONSTANT_Class
            out.writeShort(4);

            // #3 Utf8 - this class name
            out.writeByte(1); // CONSTANT_Utf8
            byte[] thisNameBytes = internalName.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            out.writeShort(thisNameBytes.length);
            out.write(thisNameBytes);

            // #4 Utf8 - super class name
            out.writeByte(1);
            byte[] superNameBytes = "java/lang/Object".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            out.writeShort(superNameBytes.length);
            out.write(superNameBytes);

            // #5 Utf8 - "Code"
            out.writeByte(1);
            byte[] codeBytes = "Code".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            out.writeShort(codeBytes.length);
            out.write(codeBytes);

            // #6 Utf8 - "<init>"
            out.writeByte(1);
            byte[] initBytes = "<init>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            out.writeShort(initBytes.length);
            out.write(initBytes);

            // #7 Utf8 - "()V"
            out.writeByte(1);
            byte[] descBytes = "()V".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            out.writeShort(descBytes.length);
            out.write(descBytes);

            // #8 NameAndType -> #6, #7
            out.writeByte(12); // CONSTANT_NameAndType
            out.writeShort(6);
            out.writeShort(7);

            // #9 Methodref -> #2, #8
            out.writeByte(10); // CONSTANT_Methodref
            out.writeShort(2);
            out.writeShort(8);

            // Access flags: ACC_PUBLIC | ACC_SUPER
            out.writeShort(0x0021);
            // This class: #1
            out.writeShort(1);
            // Super class: #2
            out.writeShort(2);
            // Interfaces count
            out.writeShort(0);
            // Fields count
            out.writeShort(0);

            // Methods count: 1 (<init>)
            out.writeShort(1);
            // Method: <init>()V
            out.writeShort(0x0001); // ACC_PUBLIC
            out.writeShort(6);     // name_index -> "<init>"
            out.writeShort(7);     // descriptor_index -> "()V"
            out.writeShort(1);     // attributes_count = 1 (Code)
            // Code attribute
            out.writeShort(5);     // attribute_name_index -> "Code"
            // Code attribute body: max_stack(2) + max_locals(2) + code_length(4) + code + exception_table_length(2) + attributes_count(2)
            byte[] code = new byte[] {
                    0x2A,                   // aload_0
                    (byte) 0xB7, 0x00, 0x09, // invokespecial #9 (Object.<init>)
                    (byte) 0xB1             // return
            };
            int codeAttrLength = 2 + 2 + 4 + code.length + 2 + 2;
            out.writeInt(codeAttrLength);
            out.writeShort(1);           // max_stack
            out.writeShort(1);           // max_locals
            out.writeInt(code.length);   // code_length
            out.write(code);
            out.writeShort(0);           // exception_table_length
            out.writeShort(0);           // code attributes_count

            // Class attributes count
            out.writeShort(0);

            out.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate bytecode", e);
        }
    }

    @Nested
    @DisplayName("Factory Methods Tests")
    class FactoryMethodsTests {

        @Test
        @DisplayName("Should create with default constructor via factory")
        void shouldCreateWithDefaultConstructor() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create with direct default constructor")
        void shouldCreateWithDirectDefaultConstructor() {
            HotSwapClassLoader loader = new HotSwapClassLoader();

            assertThat(loader).isNotNull();
            assertThat(loader.isClosed()).isFalse();
        }

        @Test
        @DisplayName("Should create with parent classloader via factory")
        void shouldCreateWithParentClassLoader() {
            HotSwapClassLoader loader = HotSwapClassLoader.create(getClass().getClassLoader());

            assertThat(loader).isNotNull();
        }

        @Test
        @DisplayName("Should create with direct constructor with parent")
        void shouldCreateWithDirectConstructorWithParent() {
            ClassLoader parent = getClass().getClassLoader();
            HotSwapClassLoader loader = new HotSwapClassLoader(parent);

            assertThat(loader).isNotNull();
            assertThat(loader.getParent()).isSameAs(parent);
        }

        @Test
        @DisplayName("Should create with maxHistoryVersions via factory")
        void shouldCreateWithMaxHistoryVersions() {
            HotSwapClassLoader loader = HotSwapClassLoader.create(3);

            assertThat(loader).isNotNull();
            assertThat(loader.getMaxHistoryVersions()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should create with parent and maxHistoryVersions via factory")
        void shouldCreateWithParentAndMaxHistoryVersions() {
            ClassLoader parent = getClass().getClassLoader();
            HotSwapClassLoader loader = HotSwapClassLoader.create(parent, 10);

            assertThat(loader).isNotNull();
            assertThat(loader.getMaxHistoryVersions()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should default maxHistoryVersions to 5")
        void shouldDefaultMaxHistoryVersions() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.getMaxHistoryVersions()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reject negative maxHistoryVersions")
        void shouldRejectNegativeMaxHistoryVersions() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> HotSwapClassLoader.create(-1));
        }
    }

    @Nested
    @DisplayName("Class Loading Tests")
    class ClassLoadingTests {

        @Test
        @DisplayName("Should load class from bytecode")
        void shouldLoadClassFromBytecode() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.MyClass");

            Class<?> clazz = loader.loadClass("test.gen.MyClass", bytecode);

            assertThat(clazz).isNotNull();
            assertThat(clazz.getName()).isEqualTo("test.gen.MyClass");
            assertThat(loader.getVersion("test.gen.MyClass")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reload class from file")
        void shouldReloadClassFromFile() throws IOException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.FileClass");
            Path classFile = tempDir.resolve("FileClass.class");
            Files.write(classFile, bytecode);

            Class<?> clazz = loader.reloadClass("test.gen.FileClass", classFile);

            assertThat(clazz).isNotNull();
            assertThat(clazz.getName()).isEqualTo("test.gen.FileClass");
        }

        @Test
        @DisplayName("Should delegate to parent for system classes")
        void shouldDelegateToParentForSystemClasses() throws Exception {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            Class<?> stringClass = loader.loadClass("java.lang.String");

            assertThat(stringClass).isEqualTo(String.class);
        }
    }

    @Nested
    @DisplayName("Version Tracking Tests")
    class VersionTrackingTests {

        @Test
        @DisplayName("Should return 0 for unloaded class")
        void shouldReturnZeroForUnloadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.getVersion("com.example.Unknown")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should track loaded class names")
        void shouldTrackLoadedClassNames() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.Tracked");

            loader.loadClass("test.gen.Tracked", bytecode);

            assertThat(loader.getLoadedClassNames()).contains("test.gen.Tracked");
        }

        @Test
        @DisplayName("Should increment version on reload")
        void shouldIncrementVersionOnReload() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.Versioned");

            loader.loadClass("test.gen.Versioned", bytecode);
            assertThat(loader.getVersion("test.gen.Versioned")).isEqualTo(1);

            loader.loadClass("test.gen.Versioned", bytecode);
            assertThat(loader.getVersion("test.gen.Versioned")).isEqualTo(2);

            loader.loadClass("test.gen.Versioned", bytecode);
            assertThat(loader.getVersion("test.gen.Versioned")).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("IsLoaded Tests")
    class IsLoadedTests {

        @Test
        @DisplayName("Should return false for unloaded class")
        void shouldReturnFalseForUnloadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.isLoaded("com.example.Unknown")).isFalse();
        }
    }

    @Nested
    @DisplayName("GetBytecode Tests")
    class GetBytecodeTests {

        @Test
        @DisplayName("Should return empty for unloaded class")
        void shouldReturnEmptyForUnloadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            Optional<byte[]> bytecode = loader.getBytecode("com.example.Unknown");

            assertThat(bytecode).isEmpty();
        }
    }

    @Nested
    @DisplayName("Rollback Tests")
    class RollbackTests {

        @Test
        @DisplayName("Should rollback to previous version")
        void shouldRollbackToPreviousVersion() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.Rollback");

            Class<?> v1 = loader.loadClass("test.gen.Rollback", bytecode);
            Class<?> v2 = loader.loadClass("test.gen.Rollback", bytecode);

            assertThat(loader.getVersion("test.gen.Rollback")).isEqualTo(2);

            Optional<Class<?>> rolled = loader.rollback("test.gen.Rollback");

            assertThat(rolled).isPresent();
            assertThat(rolled.get()).isSameAs(v1);
            assertThat(loader.getVersion("test.gen.Rollback")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty when no history")
        void shouldReturnEmptyWhenNoHistory() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.NoHistory");

            loader.loadClass("test.gen.NoHistory", bytecode);

            Optional<Class<?>> rolled = loader.rollback("test.gen.NoHistory");

            assertThat(rolled).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for never-loaded class")
        void shouldReturnEmptyForNeverLoadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            Optional<Class<?>> rolled = loader.rollback("test.gen.NeverLoaded");

            assertThat(rolled).isEmpty();
        }

        @Test
        @DisplayName("Should support multiple rollbacks")
        void shouldSupportMultipleRollbacks() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.MultiRoll");

            Class<?> v1 = loader.loadClass("test.gen.MultiRoll", bytecode);
            Class<?> v2 = loader.loadClass("test.gen.MultiRoll", bytecode);
            Class<?> v3 = loader.loadClass("test.gen.MultiRoll", bytecode);

            assertThat(loader.getVersion("test.gen.MultiRoll")).isEqualTo(3);

            // Rollback v3 -> v2
            Optional<Class<?>> roll1 = loader.rollback("test.gen.MultiRoll");
            assertThat(roll1).isPresent();
            assertThat(roll1.get()).isSameAs(v2);
            assertThat(loader.getVersion("test.gen.MultiRoll")).isEqualTo(2);

            // Rollback v2 -> v1
            Optional<Class<?>> roll2 = loader.rollback("test.gen.MultiRoll");
            assertThat(roll2).isPresent();
            assertThat(roll2.get()).isSameAs(v1);
            assertThat(loader.getVersion("test.gen.MultiRoll")).isEqualTo(1);

            // No more history
            Optional<Class<?>> roll3 = loader.rollback("test.gen.MultiRoll");
            assertThat(roll3).isEmpty();
        }

        @Test
        @DisplayName("Should throw on rollback when closed")
        void shouldThrowOnRollbackWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThatThrownBy(() -> loader.rollback("test.gen.Closed"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on rollback with null class name")
        void shouldThrowOnRollbackWithNullClassName() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.rollback(null));
        }
    }

    @Nested
    @DisplayName("History Count Tests")
    class HistoryCountTests {

        @Test
        @DisplayName("Should return 0 for unloaded class")
        void shouldReturnZeroForUnloadedClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.getHistoryCount("test.gen.Unknown")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return 0 for single version")
        void shouldReturnZeroForSingleVersion() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.Single");

            loader.loadClass("test.gen.Single", bytecode);

            assertThat(loader.getHistoryCount("test.gen.Single")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should track history count on multiple loads")
        void shouldTrackHistoryCountOnMultipleLoads() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.Multi");

            loader.loadClass("test.gen.Multi", bytecode);
            assertThat(loader.getHistoryCount("test.gen.Multi")).isEqualTo(0);

            loader.loadClass("test.gen.Multi", bytecode);
            assertThat(loader.getHistoryCount("test.gen.Multi")).isEqualTo(1);

            loader.loadClass("test.gen.Multi", bytecode);
            assertThat(loader.getHistoryCount("test.gen.Multi")).isEqualTo(2);
        }

        @Test
        @DisplayName("Should respect maxHistoryVersions limit")
        void shouldRespectMaxHistoryVersionsLimit() {
            HotSwapClassLoader loader = HotSwapClassLoader.create(2);
            byte[] bytecode = generateBytecode("test.gen.Limited");

            // Load 5 versions (current + 4 history attempts, but max 2 kept)
            loader.loadClass("test.gen.Limited", bytecode); // v1, no history
            loader.loadClass("test.gen.Limited", bytecode); // v2, history=[v1]
            loader.loadClass("test.gen.Limited", bytecode); // v3, history=[v1,v2]
            loader.loadClass("test.gen.Limited", bytecode); // v4, history=[v2,v3] (v1 trimmed)
            loader.loadClass("test.gen.Limited", bytecode); // v5, history=[v3,v4] (v2 trimmed)

            assertThat(loader.getHistoryCount("test.gen.Limited")).isEqualTo(2);
            assertThat(loader.getVersion("test.gen.Limited")).isEqualTo(5);
        }

        @Test
        @DisplayName("Should keep zero history when maxHistoryVersions is 0")
        void shouldKeepZeroHistoryWhenMaxIsZero() {
            HotSwapClassLoader loader = HotSwapClassLoader.create(0);
            byte[] bytecode = generateBytecode("test.gen.ZeroHistory");

            loader.loadClass("test.gen.ZeroHistory", bytecode);
            loader.loadClass("test.gen.ZeroHistory", bytecode);
            loader.loadClass("test.gen.ZeroHistory", bytecode);

            assertThat(loader.getHistoryCount("test.gen.ZeroHistory")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should decrease history count after rollback")
        void shouldDecreaseHistoryCountAfterRollback() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.RollCount");

            loader.loadClass("test.gen.RollCount", bytecode);
            loader.loadClass("test.gen.RollCount", bytecode);
            loader.loadClass("test.gen.RollCount", bytecode);
            assertThat(loader.getHistoryCount("test.gen.RollCount")).isEqualTo(2);

            loader.rollback("test.gen.RollCount");
            assertThat(loader.getHistoryCount("test.gen.RollCount")).isEqualTo(1);

            loader.rollback("test.gen.RollCount");
            assertThat(loader.getHistoryCount("test.gen.RollCount")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Listener Tests")
    class ListenerTests {

        @Test
        @DisplayName("Should notify listener on loadClass")
        void shouldNotifyListenerOnLoadClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            AtomicReference<String> capturedName = new AtomicReference<>();
            AtomicInteger capturedOld = new AtomicInteger(-1);
            AtomicInteger capturedNew = new AtomicInteger(-1);

            loader.addListener((name, oldV, newV) -> {
                capturedName.set(name);
                capturedOld.set(oldV);
                capturedNew.set(newV);
            });

            byte[] bytecode = generateBytecode("test.gen.Listener");
            loader.loadClass("test.gen.Listener", bytecode);

            assertThat(capturedName.get()).isEqualTo("test.gen.Listener");
            assertThat(capturedOld.get()).isEqualTo(0); // first load, no old version
            assertThat(capturedNew.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should notify listener with old and new version on reload")
        void shouldNotifyListenerWithVersionsOnReload() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            List<int[]> versions = new ArrayList<>();

            loader.addListener((name, oldV, newV) -> versions.add(new int[]{oldV, newV}));

            byte[] bytecode = generateBytecode("test.gen.ListReload");
            loader.loadClass("test.gen.ListReload", bytecode); // v1
            loader.loadClass("test.gen.ListReload", bytecode); // v2

            assertThat(versions).hasSize(2);
            assertThat(versions.get(0)).containsExactly(0, 1); // first load
            assertThat(versions.get(1)).containsExactly(1, 2); // reload
        }

        @Test
        @DisplayName("Should notify multiple listeners")
        void shouldNotifyMultipleListeners() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            AtomicInteger count1 = new AtomicInteger(0);
            AtomicInteger count2 = new AtomicInteger(0);

            loader.addListener((n, o, v) -> count1.incrementAndGet());
            loader.addListener((n, o, v) -> count2.incrementAndGet());

            byte[] bytecode = generateBytecode("test.gen.MultiList");
            loader.loadClass("test.gen.MultiList", bytecode);

            assertThat(count1.get()).isEqualTo(1);
            assertThat(count2.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should not notify after removeListener")
        void shouldNotNotifyAfterRemoveListener() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            AtomicInteger count = new AtomicInteger(0);
            HotSwapListener listener = (n, o, v) -> count.incrementAndGet();

            loader.addListener(listener);
            byte[] bytecode = generateBytecode("test.gen.Remove");
            loader.loadClass("test.gen.Remove", bytecode);
            assertThat(count.get()).isEqualTo(1);

            loader.removeListener(listener);
            loader.loadClass("test.gen.Remove", bytecode);
            assertThat(count.get()).isEqualTo(1); // not incremented
        }

        @Test
        @DisplayName("Should not fail if listener throws exception")
        void shouldNotFailIfListenerThrows() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            AtomicInteger goodCount = new AtomicInteger(0);

            loader.addListener((n, o, v) -> { throw new RuntimeException("bad listener"); });
            loader.addListener((n, o, v) -> goodCount.incrementAndGet());

            byte[] bytecode = generateBytecode("test.gen.BadListener");

            assertThatCode(() -> loader.loadClass("test.gen.BadListener", bytecode))
                    .doesNotThrowAnyException();
            assertThat(goodCount.get()).isEqualTo(1);
            assertThat(loader.isLoaded("test.gen.BadListener")).isTrue();
        }

        @Test
        @DisplayName("Should throw on null listener in addListener")
        void shouldThrowOnNullListenerInAdd() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.addListener(null));
        }

        @Test
        @DisplayName("Should throw on null listener in removeListener")
        void shouldThrowOnNullListenerInRemove() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.removeListener(null));
        }

        @Test
        @DisplayName("Should notify listener on rollback")
        void shouldNotifyListenerOnRollback() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            List<int[]> versions = new ArrayList<>();

            loader.addListener((name, oldV, newV) -> versions.add(new int[]{oldV, newV}));

            byte[] bytecode = generateBytecode("test.gen.RollbackNotify");
            loader.loadClass("test.gen.RollbackNotify", bytecode); // v1
            loader.loadClass("test.gen.RollbackNotify", bytecode); // v2
            loader.rollback("test.gen.RollbackNotify"); // rollback v2 -> v1

            assertThat(versions).hasSize(3);
            assertThat(versions.get(2)).containsExactly(2, 1); // rollback from v2 to v1
        }

        @Test
        @DisplayName("Concurrent addListener and loadClass should be safe")
        void concurrentAddListenerAndLoadClassShouldBeSafe() throws InterruptedException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            int threads = 10;
            CountDownLatch latch = new CountDownLatch(threads);
            CopyOnWriteArrayList<Throwable> errors = new CopyOnWriteArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            try {
                for (int i = 0; i < threads; i++) {
                    int idx = i;
                    executor.submit(() -> {
                        try {
                            // Half add listeners, half load classes
                            if (idx % 2 == 0) {
                                loader.addListener((n, o, v) -> { /* no-op */ });
                            } else {
                                String name = "test.gen.Concurrent" + idx;
                                byte[] bytecode = generateBytecode(name);
                                loader.loadClass(name, bytecode);
                            }
                        } catch (Throwable t) {
                            errors.add(t);
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
                assertThat(errors).isEmpty();
            } finally {
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        }
    }

    @Nested
    @DisplayName("IsClosed Tests")
    class IsClosedTests {

        @Test
        @DisplayName("Should return false when not closed")
        void shouldReturnFalseWhenNotClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThat(loader.isClosed()).isFalse();
        }

        @Test
        @DisplayName("Should return true after close")
        void shouldReturnTrueAfterClose() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThat(loader.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should throw on loadClass with bytecode when closed")
        void shouldThrowOnLoadClassWithBytecodeWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            byte[] bytecode = generateBytecode("test.gen.ClosedLoad");

            assertThatThrownBy(() -> loader.loadClass("test.gen.ClosedLoad", bytecode))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on loadClass when closed")
        void shouldThrowOnLoadClassWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThatThrownBy(() -> loader.loadClass("java.lang.String"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on reloadClass when closed")
        void shouldThrowOnReloadClassWhenClosed() throws IOException {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            Path classFile = tempDir.resolve("Test.class");
            Files.writeString(classFile, "dummy");
            loader.close();

            assertThatThrownBy(() -> loader.reloadClass("test.ClosedReload", classFile))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on unloadClass when closed")
        void shouldThrowOnUnloadClassWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThatThrownBy(() -> loader.unloadClass("test.Class"))
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }

        @Test
        @DisplayName("Should throw on clear when closed")
        void shouldThrowOnClearWhenClosed() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            loader.close();

            assertThatThrownBy(loader::clear)
                    .isInstanceOf(OpenClassLoaderException.class)
                    .hasMessageContaining("closed");
        }
    }

    @Nested
    @DisplayName("ReloadClass Tests")
    class ReloadClassTests {

        @Test
        @DisplayName("Should throw on nonexistent file")
        void shouldThrowOnNonexistentFile() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            Path nonexistent = tempDir.resolve("nonexistent.class");

            assertThatThrownBy(() -> loader.reloadClass("test.Nonexistent", nonexistent))
                    .isInstanceOf(OpenClassLoaderException.class);
        }
    }

    @Nested
    @DisplayName("Null Validation Tests")
    class NullValidationTests {

        @Test
        @DisplayName("Should throw on null class name in loadClass with bytecode")
        void shouldThrowOnNullClassNameInLoadClassWithBytecode() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.Null");

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.loadClass(null, bytecode));
        }

        @Test
        @DisplayName("Should throw on null bytecode in loadClass")
        void shouldThrowOnNullBytecodeInLoadClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThatNullPointerException()
                    .isThrownBy(() -> loader.loadClass("test.NullBytecode", null));
        }
    }

    @Nested
    @DisplayName("Unload Tests")
    class UnloadTests {

        @Test
        @DisplayName("Should unload class")
        void shouldUnloadClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            loader.unloadClass("com.example.MyClass");

            // Should not throw, unloading marks class for reload
            assertThat(loader.getVersion("com.example.MyClass")).isEqualTo(0);
        }

        @Test
        @DisplayName("Should clear all classes")
        void shouldClearAllClasses() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();
            byte[] bytecode = generateBytecode("test.gen.ClearMe");

            loader.loadClass("test.gen.ClearMe", bytecode);
            loader.clear();

            assertThat(loader.getLoadedClassNames()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should close successfully")
        void shouldCloseSuccessfully() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            assertThatCode(loader::close).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should work with try-with-resources")
        void shouldWorkWithTryWithResources() {
            assertThatCode(() -> {
                try (HotSwapClassLoader loader = HotSwapClassLoader.create()) {
                    loader.loadClass("java.lang.String");
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null class name in getVersion")
        void shouldHandleNullClassNameInGetVersion() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // Null class name may throw or return 0
            try {
                int version = loader.getVersion(null);
                assertThat(version).isGreaterThanOrEqualTo(0);
            } catch (NullPointerException e) {
                // Expected if implementation doesn't handle null
            }
        }

        @Test
        @DisplayName("Should handle null class name in unload")
        void shouldHandleNullClassNameInUnload() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // May throw NullPointerException or handle gracefully
            try {
                loader.unloadClass(null);
            } catch (NullPointerException e) {
                // Expected if implementation doesn't handle null
            }
        }

        @Test
        @DisplayName("Should throw ClassNotFoundException for unknown class in findClass")
        void shouldThrowForUnknownClassInFindClass() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // findClass is protected but called internally by loadClass when parent can't find
            assertThatThrownBy(() -> loader.loadClass("com.nonexistent.Unknown123456"))
                    .isInstanceOf(ClassNotFoundException.class);
        }

        @Test
        @DisplayName("Close should be idempotent")
        void closeShouldBeIdempotent() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // Multiple close calls should not throw
            assertThatCode(() -> {
                loader.close();
                loader.close();
                loader.close();
            }).doesNotThrowAnyException();

            assertThat(loader.isClosed()).isTrue();
        }

        @Test
        @DisplayName("Should handle isLoaded for null class name")
        void shouldHandleIsLoadedForNull() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            // null key in ConcurrentHashMap.containsKey returns false or throws
            try {
                boolean loaded = loader.isLoaded(null);
                assertThat(loaded).isFalse();
            } catch (NullPointerException e) {
                // Expected
            }
        }

        @Test
        @DisplayName("Should handle getBytecode for null class name")
        void shouldHandleGetBytecodeForNull() {
            HotSwapClassLoader loader = HotSwapClassLoader.create();

            try {
                Optional<byte[]> bytecode = loader.getBytecode(null);
                assertThat(bytecode).isEmpty();
            } catch (NullPointerException e) {
                // Expected
            }
        }
    }
}
