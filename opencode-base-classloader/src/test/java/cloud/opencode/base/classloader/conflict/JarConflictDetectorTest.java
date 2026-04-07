package cloud.opencode.base.classloader.conflict;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for JarConflictDetector, ConflictReport, and JarInfo
 * JarConflictDetector、ConflictReport 和 JarInfo 的测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
class JarConflictDetectorTest {

    @TempDir
    Path tempDir;

    /**
     * Helper: create a test JAR with the specified class entries and optional version
     */
    private Path createTestJar(String jarName, String version, String... classEntries) throws IOException {
        Path jarPath = tempDir.resolve(jarName);
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (version != null) {
            manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, version);
        }
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            for (String entry : classEntries) {
                JarEntry jarEntry = new JarEntry(entry);
                jos.putNextEntry(jarEntry);
                // Write minimal bytecode-like content (not real bytecode, just placeholder)
                jos.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
                jos.closeEntry();
            }
        }
        return jarPath;
    }

    @Nested
    class DetectTests {

        @Test
        void detectFindsConflictsWhenSameClassInTwoJars() throws IOException {
            Path jar1 = createTestJar("lib-a.jar", "1.0",
                    "com/example/Shared.class", "com/example/OnlyInA.class");
            Path jar2 = createTestJar("lib-b.jar", "2.0",
                    "com/example/Shared.class", "com/example/OnlyInB.class");

            ConflictReport report = JarConflictDetector.detect(jar1, jar2);

            assertThat(report.hasConflicts()).isTrue();
            assertThat(report.totalConflicts()).isEqualTo(1);
            assertThat(report.conflictingClasses()).containsExactly("com.example.Shared");

            List<JarInfo> jars = report.conflicts().get("com.example.Shared");
            assertThat(jars).hasSize(2);
            assertThat(jars).extracting(JarInfo::name)
                    .containsExactlyInAnyOrder("lib-a.jar", "lib-b.jar");
            assertThat(jars).extracting(JarInfo::version)
                    .containsExactlyInAnyOrder("1.0", "2.0");
        }

        @Test
        void detectReturnsEmptyWhenNoConflicts() throws IOException {
            Path jar1 = createTestJar("unique-a.jar", "1.0",
                    "com/example/ClassA.class");
            Path jar2 = createTestJar("unique-b.jar", "2.0",
                    "com/example/ClassB.class");

            ConflictReport report = JarConflictDetector.detect(jar1, jar2);

            assertThat(report.hasConflicts()).isFalse();
            assertThat(report.totalConflicts()).isEqualTo(0);
            assertThat(report.conflictingClasses()).isEmpty();
        }

        @Test
        void detectSkipsMetaInfAndModuleInfo() throws IOException {
            Path jar1 = createTestJar("meta-a.jar", null,
                    "META-INF/versions/21/com/example/Foo.class",
                    "module-info.class",
                    "com/example/Foo.class");
            Path jar2 = createTestJar("meta-b.jar", null,
                    "META-INF/versions/21/com/example/Foo.class",
                    "module-info.class",
                    "com/example/Foo.class");

            ConflictReport report = JarConflictDetector.detect(jar1, jar2);

            // Only com.example.Foo should conflict (META-INF and module-info excluded)
            assertThat(report.conflictingClasses()).containsExactly("com.example.Foo");
            assertThat(report.totalConflicts()).isEqualTo(1);
        }

        @Test
        void detectWithEmptyJars() throws IOException {
            Path jar1 = createTestJar("empty-a.jar", null);
            Path jar2 = createTestJar("empty-b.jar", null);

            ConflictReport report = JarConflictDetector.detect(jar1, jar2);

            assertThat(report.hasConflicts()).isFalse();
            assertThat(report.totalClassesScanned()).isEqualTo(0);
        }

        @Test
        void detectMultipleConflicts() throws IOException {
            Path jar1 = createTestJar("multi-a.jar", "1.0",
                    "com/example/A.class", "com/example/B.class", "com/example/C.class");
            Path jar2 = createTestJar("multi-b.jar", "2.0",
                    "com/example/A.class", "com/example/B.class", "com/example/D.class");

            ConflictReport report = JarConflictDetector.detect(jar1, jar2);

            assertThat(report.totalConflicts()).isEqualTo(2);
            assertThat(report.conflictingClasses())
                    .containsExactlyInAnyOrder("com.example.A", "com.example.B");
        }

        @Test
        void detectThrowsOnNullVarargs() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JarConflictDetector.detect((Path[]) null));
        }

        @Test
        void detectThrowsOnNullCollection() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JarConflictDetector.detect((java.util.Collection<Path>) null));
        }

        @Test
        void detectThrowsOnNullElementInArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JarConflictDetector.detect(new Path[]{null}));
        }
    }

    @Nested
    class DetectInDirectoryTests {

        @Test
        void detectInDirectoryScansJarFiles() throws IOException {
            createTestJar("dir-a.jar", "1.0",
                    "com/example/Overlap.class", "com/example/UniqueA.class");
            createTestJar("dir-b.jar", "2.0",
                    "com/example/Overlap.class", "com/example/UniqueB.class");

            ConflictReport report = JarConflictDetector.detectInDirectory(tempDir);

            assertThat(report.hasConflicts()).isTrue();
            assertThat(report.conflictingClasses()).containsExactly("com.example.Overlap");
        }

        @Test
        void detectInDirectoryWithGlobPattern() throws IOException {
            createTestJar("include-me.jar", null, "com/example/A.class");
            createTestJar("exclude-me.jar", null, "com/example/A.class");

            // Only scan "include-*" JARs
            ConflictReport report = JarConflictDetector.detectInDirectory(tempDir, "include-*.jar");

            // Only one JAR matched, so no conflicts
            assertThat(report.hasConflicts()).isFalse();
        }

        @Test
        void detectInDirectoryIgnoresNonJarFiles() throws IOException {
            createTestJar("real.jar", null, "com/example/A.class");
            // Create a non-JAR file
            Files.writeString(tempDir.resolve("not-a-jar.txt"), "hello");

            ConflictReport report = JarConflictDetector.detectInDirectory(tempDir);

            assertThat(report.hasConflicts()).isFalse();
            // Should have scanned classes from only the one JAR
            assertThat(report.totalClassesScanned()).isEqualTo(1);
        }

        @Test
        void detectInDirectoryHandlesNonExistentDirectory() {
            Path nonExistent = tempDir.resolve("does-not-exist");

            ConflictReport report = JarConflictDetector.detectInDirectory(nonExistent);

            assertThat(report.hasConflicts()).isFalse();
            assertThat(report.totalClassesScanned()).isEqualTo(0);
        }

        @Test
        void detectInDirectoryThrowsOnNullDirectory() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JarConflictDetector.detectInDirectory(null));
        }

        @Test
        void detectInDirectoryThrowsOnNullGlobPattern() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JarConflictDetector.detectInDirectory(tempDir, null));
        }
    }

    @Nested
    class CorruptJarTests {

        @Test
        void corruptJarIsHandledGracefully() throws IOException {
            // Create a corrupt file pretending to be a JAR
            Path corruptJar = tempDir.resolve("corrupt.jar");
            Files.writeString(corruptJar, "this is not a jar file");

            Path validJar = createTestJar("valid.jar", "1.0", "com/example/A.class");

            // Should not throw, corrupt JAR is skipped
            ConflictReport report = JarConflictDetector.detect(corruptJar, validJar);

            assertThat(report.hasConflicts()).isFalse();
            assertThat(report.totalClassesScanned()).isEqualTo(1);
        }

        @Test
        void corruptJarInDirectoryIsSkipped() throws IOException {
            Path corruptJar = tempDir.resolve("bad.jar");
            Files.writeString(corruptJar, "not-a-jar");
            createTestJar("good.jar", "1.0", "com/example/A.class");

            ConflictReport report = JarConflictDetector.detectInDirectory(tempDir);

            assertThat(report.hasConflicts()).isFalse();
        }
    }

    @Nested
    class ConflictReportTests {

        @Test
        void summaryReturnsReadableString() throws IOException {
            Path jar1 = createTestJar("summary-a.jar", "1.0", "com/example/Dup.class");
            Path jar2 = createTestJar("summary-b.jar", "2.0", "com/example/Dup.class");

            ConflictReport report = JarConflictDetector.detect(jar1, jar2);
            String summary = report.summary();

            assertThat(summary).contains("JAR Conflict Report");
            assertThat(summary).contains("Total conflicts");
            assertThat(summary).contains("com.example.Dup");
            assertThat(summary).contains("summary-a.jar");
            assertThat(summary).contains("summary-b.jar");
            assertThat(summary).contains("v1.0");
            assertThat(summary).contains("v2.0");
        }

        @Test
        void summaryWithNoConflicts() {
            ConflictReport report = new ConflictReport(Map.of(), 0, 100);
            String summary = report.summary();

            assertThat(summary).contains("No conflicts detected");
            assertThat(summary).contains("100");
        }

        @Test
        void getConflictsForJar() throws IOException {
            Path jar1 = createTestJar("filter-a.jar", "1.0",
                    "com/example/X.class", "com/example/Y.class");
            Path jar2 = createTestJar("filter-b.jar", "2.0",
                    "com/example/X.class");
            Path jar3 = createTestJar("filter-c.jar", "3.0",
                    "com/example/Y.class");

            ConflictReport report = JarConflictDetector.detect(jar1, jar2, jar3);

            // jar1 is involved in both conflicts
            Map<String, List<JarInfo>> jar1Conflicts = report.getConflictsForJar(jar1);
            assertThat(jar1Conflicts).hasSize(2);

            // jar2 is only involved in the X conflict
            Map<String, List<JarInfo>> jar2Conflicts = report.getConflictsForJar(jar2);
            assertThat(jar2Conflicts).hasSize(1);
            assertThat(jar2Conflicts).containsKey("com.example.X");
        }

        @Test
        void getConflictsForJarThrowsOnNull() {
            ConflictReport report = new ConflictReport(Map.of(), 0, 0);
            assertThatNullPointerException()
                    .isThrownBy(() -> report.getConflictsForJar(null));
        }

        @Test
        void defensiveCopyPreventsModification() throws IOException {
            Map<String, List<JarInfo>> mutable = new java.util.HashMap<>();
            JarInfo info = new JarInfo(Path.of("/test.jar"), "1.0", "test.jar");
            mutable.put("com.example.A", new java.util.ArrayList<>(List.of(info, info)));

            ConflictReport report = new ConflictReport(mutable, 1, 2);

            // Modify the original map
            mutable.put("com.example.B", List.of(info));

            // Report should not be affected
            assertThat(report.conflicts()).doesNotContainKey("com.example.B");

            // Returned map should be unmodifiable
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> report.conflicts().put("hack", List.of()));
        }
    }

    @Nested
    class JarInfoTests {

        @Test
        void jarInfoRecordAccessors() {
            Path path = Path.of("/libs/test.jar");
            JarInfo info = new JarInfo(path, "1.2.3", "test.jar");

            assertThat(info.path()).isEqualTo(path);
            assertThat(info.version()).isEqualTo("1.2.3");
            assertThat(info.name()).isEqualTo("test.jar");
        }

        @Test
        void jarInfoAllowsNullVersion() {
            JarInfo info = new JarInfo(Path.of("/test.jar"), null, "test.jar");
            assertThat(info.version()).isNull();
        }

        @Test
        void jarInfoRejectsNullPath() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new JarInfo(null, "1.0", "test.jar"));
        }

        @Test
        void jarInfoRejectsNullName() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new JarInfo(Path.of("/test.jar"), "1.0", null));
        }
    }

    @Nested
    class VersionExtractionTests {

        @Test
        void extractsImplementationVersion() throws IOException {
            Path jar = createTestJar("versioned.jar", "3.14.159", "com/example/V.class");

            ConflictReport report = JarConflictDetector.detect(jar);

            // No conflicts with single JAR, but we can verify via a two-JAR conflict
            Path jar2 = createTestJar("versioned2.jar", "2.71.828", "com/example/V.class");
            report = JarConflictDetector.detect(jar, jar2);

            List<JarInfo> infos = report.conflicts().get("com.example.V");
            assertThat(infos).extracting(JarInfo::version)
                    .containsExactlyInAnyOrder("3.14.159", "2.71.828");
        }

        @Test
        void handlesJarWithNoManifestVersion() throws IOException {
            Path jar1 = createTestJar("no-ver-a.jar", null, "com/example/NV.class");
            Path jar2 = createTestJar("no-ver-b.jar", null, "com/example/NV.class");

            ConflictReport report = JarConflictDetector.detect(jar1, jar2);

            List<JarInfo> infos = report.conflicts().get("com.example.NV");
            assertThat(infos).extracting(JarInfo::version)
                    .containsExactly(null, null);
        }
    }
}
