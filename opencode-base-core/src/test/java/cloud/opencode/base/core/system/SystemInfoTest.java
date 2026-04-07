package cloud.opencode.base.core.system;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link SystemInfo} and related records.
 *
 * @author Leon Soo
 * @since 1.0.3
 */
class SystemInfoTest {

    @Nested
    @DisplayName("CpuInfo - CPU 信息")
    class CpuInfoTests {

        @Test
        @DisplayName("可用处理器数大于零")
        void availableProcessors_shouldBePositive() {
            CpuInfo cpu = SystemInfo.cpu();
            assertThat(cpu.availableProcessors()).isGreaterThan(0);
        }

        @Test
        @DisplayName("处理器架构不为空")
        void arch_shouldNotBeNullOrBlank() {
            CpuInfo cpu = SystemInfo.cpu();
            assertThat(cpu.arch()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("系统 CPU 负载返回 -1 或 [0,1] 范围内的值")
        void systemCpuLoad_shouldReturnValidRange() {
            CpuInfo cpu = SystemInfo.cpu();
            double load = cpu.systemCpuLoad();
            assertThat(load == -1.0 || (load >= 0.0 && load <= 1.0))
                    .as("systemCpuLoad should be -1 or in [0.0, 1.0], got: %f", load)
                    .isTrue();
        }

        @Test
        @DisplayName("进程 CPU 负载返回 -1 或 [0,1] 范围内的值")
        void processCpuLoad_shouldReturnValidRange() {
            CpuInfo cpu = SystemInfo.cpu();
            double load = cpu.processCpuLoad();
            assertThat(load == -1.0 || load >= 0.0)
                    .as("processCpuLoad should be -1 or >= 0.0, got: %f", load)
                    .isTrue();
        }

        @Test
        @DisplayName("平均负载数组不为 null")
        void loadAverage_shouldNotBeNull() {
            CpuInfo cpu = SystemInfo.cpu();
            assertThat(cpu.loadAverage()).isNotNull();
        }

        @Test
        @DisplayName("loadAverage 返回防御性副本")
        void loadAverage_shouldReturnDefensiveCopy() {
            CpuInfo cpu = SystemInfo.cpu();
            double[] first = cpu.loadAverage();
            double[] second = cpu.loadAverage();
            assertThat(first).isNotSameAs(second);
        }

        @Test
        @DisplayName("archDisplay 对 amd64 返回 x86_64")
        void archDisplay_shouldNormalizeAmd64() {
            CpuInfo cpu = new CpuInfo(4, "amd64", -1, -1, new double[0]);
            assertThat(cpu.archDisplay()).isEqualTo("x86_64");
        }

        @Test
        @DisplayName("archDisplay 对 x86 返回 x86_32")
        void archDisplay_shouldNormalizeX86() {
            CpuInfo cpu = new CpuInfo(4, "x86", -1, -1, new double[0]);
            assertThat(cpu.archDisplay()).isEqualTo("x86_32");
        }

        @Test
        @DisplayName("archDisplay 对未知架构原样返回")
        void archDisplay_shouldPassThroughUnknown() {
            CpuInfo cpu = new CpuInfo(4, "aarch64", -1, -1, new double[0]);
            assertThat(cpu.archDisplay()).isEqualTo("aarch64");
        }

        @Test
        @DisplayName("isLoadAvailable 在有值时返回 true")
        void isLoadAvailable_shouldReturnTrueWhenPresent() {
            CpuInfo cpu = new CpuInfo(4, "amd64", -1, -1, new double[]{1.5});
            assertThat(cpu.isLoadAvailable()).isTrue();
        }

        @Test
        @DisplayName("isLoadAvailable 在空数组时返回 false")
        void isLoadAvailable_shouldReturnFalseWhenEmpty() {
            CpuInfo cpu = new CpuInfo(4, "amd64", -1, -1, new double[0]);
            assertThat(cpu.isLoadAvailable()).isFalse();
        }

        @Test
        @DisplayName("构造时 availableProcessors 小于 1 应抛出异常")
        void constructor_shouldRejectInvalidProcessorCount() {
            assertThatThrownBy(() -> new CpuInfo(0, "amd64", -1, -1, new double[0]))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("构造时 arch 为 null 应抛出异常")
        void constructor_shouldRejectNullArch() {
            assertThatThrownBy(() -> new CpuInfo(4, null, -1, -1, new double[0]))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 loadAverage 为 null 应抛出异常")
        void constructor_shouldRejectNullLoadAverage() {
            assertThatThrownBy(() -> new CpuInfo(4, "amd64", -1, -1, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("loadAverage 防御性复制 — 修改返回数组不影响原始数据")
        void loadAverage_defensiveCopy_shouldNotAffectOriginal() {
            double[] original = {1.0, 2.0, 3.0};
            CpuInfo cpu = new CpuInfo(4, "amd64", -1, -1, original);
            // 修改构造传入的数组不影响记录内部数据
            original[0] = 999.0;
            assertThat(cpu.loadAverage()[0]).isEqualTo(1.0);

            // 修改返回的数组不影响记录内部数据
            double[] returned = cpu.loadAverage();
            returned[0] = 888.0;
            assertThat(cpu.loadAverage()[0]).isEqualTo(1.0);
        }

        @Test
        @DisplayName("archDisplay 对 aarch64 原样返回")
        void archDisplay_shouldReturnAarch64AsIs() {
            CpuInfo cpu = new CpuInfo(4, "aarch64", -1, -1, new double[0]);
            assertThat(cpu.archDisplay()).isEqualTo("aarch64");
        }

        @Test
        @DisplayName("archDisplay 对其他未知架构原样返回")
        void archDisplay_shouldReturnOtherArchAsIs() {
            CpuInfo cpu = new CpuInfo(4, "riscv64", -1, -1, new double[0]);
            assertThat(cpu.archDisplay()).isEqualTo("riscv64");
        }

        @Test
        @DisplayName("isLoadAvailable 在负值时返回 false")
        void isLoadAvailable_shouldReturnFalseWhenNegative() {
            CpuInfo cpu = new CpuInfo(4, "amd64", -1, -1, new double[]{-1.0});
            assertThat(cpu.isLoadAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("MemoryInfo - 内存信息")
    class MemoryInfoTests {

        @Test
        @DisplayName("物理内存总量大于零")
        void physicalMemoryTotal_shouldBePositive() {
            long total = SystemInfo.physicalMemoryTotal();
            // -1 if unavailable (non-Sun JVM), otherwise positive
            assertThat(total == -1 || total > 0)
                    .as("physicalMemoryTotal should be -1 or > 0, got: %d", total)
                    .isTrue();
        }

        @Test
        @DisplayName("堆内存总量大于零")
        void heapMemory_totalShouldBePositive() {
            MemoryInfo heap = SystemInfo.heapMemory();
            assertThat(heap.total()).isGreaterThan(0);
        }

        @Test
        @DisplayName("堆内存已用量大于零")
        void heapMemory_usedShouldBePositive() {
            MemoryInfo heap = SystemInfo.heapMemory();
            assertThat(heap.used()).isGreaterThan(0);
        }

        @Test
        @DisplayName("堆内存空闲量非负")
        void heapMemory_freeShouldBeNonNegative() {
            MemoryInfo heap = SystemInfo.heapMemory();
            assertThat(heap.free()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("非堆内存已用量大于零")
        void nonHeapMemory_usedShouldBePositive() {
            MemoryInfo nonHeap = SystemInfo.nonHeapMemory();
            assertThat(nonHeap.used()).isGreaterThan(0);
        }

        @Test
        @DisplayName("使用百分比在 [0, 100] 范围内")
        void usagePercent_shouldBeInValidRange() {
            MemoryInfo heap = SystemInfo.heapMemory();
            assertThat(heap.usagePercent()).isBetween(0.0, 100.0);
        }

        @Test
        @DisplayName("总量为零时使用百分比返回 0")
        void usagePercent_shouldReturnZeroWhenTotalIsZero() {
            MemoryInfo info = MemoryInfo.of(0, 0, 0, -1);
            assertThat(info.usagePercent()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("formatBytes 正确格式化字节数")
        void formatBytes_shouldFormatCorrectly() {
            assertThat(MemoryInfo.formatBytes(0)).isEqualTo("0 B");
            assertThat(MemoryInfo.formatBytes(512)).isEqualTo("512 B");
            assertThat(MemoryInfo.formatBytes(1024)).isEqualTo("1.0 KB");
            assertThat(MemoryInfo.formatBytes(1536)).isEqualTo("1.5 KB");
            assertThat(MemoryInfo.formatBytes(1024L * 1024)).isEqualTo("1.0 MB");
            assertThat(MemoryInfo.formatBytes(1024L * 1024 * 1024)).isEqualTo("1.0 GB");
            assertThat(MemoryInfo.formatBytes(1024L * 1024 * 1024 * 1024)).isEqualTo("1.0 TB");
        }

        @Test
        @DisplayName("formatBytes 对负值返回 N/A")
        void formatBytes_shouldReturnNAForNegative() {
            assertThat(MemoryInfo.formatBytes(-1)).isEqualTo("N/A");
        }

        @Test
        @DisplayName("显示方法返回非空字符串")
        void displayMethods_shouldReturnNonBlankStrings() {
            MemoryInfo heap = SystemInfo.heapMemory();
            assertThat(heap.totalDisplay()).isNotBlank();
            assertThat(heap.usedDisplay()).isNotBlank();
            assertThat(heap.freeDisplay()).isNotBlank();
        }

        @Test
        @DisplayName("ofPhysical 工厂方法正确计算 used")
        void ofPhysical_shouldCalculateUsed() {
            MemoryInfo info = MemoryInfo.ofPhysical(1000, 400);
            assertThat(info.total()).isEqualTo(1000);
            assertThat(info.free()).isEqualTo(400);
            assertThat(info.used()).isEqualTo(600);
            assertThat(info.max()).isEqualTo(1000);
        }

        @Test
        @DisplayName("ofPhysical 当 free 大于 total 时 used 为 0")
        void ofPhysical_shouldClampUsedToZero() {
            MemoryInfo info = MemoryInfo.ofPhysical(100, 200);
            assertThat(info.used()).isEqualTo(0);
        }

        @Test
        @DisplayName("构造时 total 为负数应抛出异常")
        void constructor_shouldRejectNegativeTotal() {
            assertThatThrownBy(() -> MemoryInfo.of(-1, 0, 0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("构造时 used 为负数应抛出异常")
        void constructor_shouldRejectNegativeUsed() {
            assertThatThrownBy(() -> MemoryInfo.of(100, -1, 0, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("构造时 max 小于 -1 应抛出异常")
        void constructor_shouldRejectInvalidMax() {
            assertThatThrownBy(() -> MemoryInfo.of(100, 50, 50, -2))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("构造时 free 为负数应抛出异常")
        void constructor_shouldRejectNegativeFree() {
            assertThatThrownBy(() -> MemoryInfo.of(100, 50, -1, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of 工厂方法创建实例所有字段正确")
        void of_shouldCreateWithAllParams() {
            MemoryInfo info = MemoryInfo.of(2048, 1024, 1024, 4096);
            assertThat(info.total()).isEqualTo(2048);
            assertThat(info.used()).isEqualTo(1024);
            assertThat(info.free()).isEqualTo(1024);
            assertThat(info.max()).isEqualTo(4096);
        }

        @Test
        @DisplayName("max 为 -1 表示未定义是合法的")
        void constructor_shouldAcceptNegativeOneMax() {
            MemoryInfo info = MemoryInfo.of(100, 50, 50, -1);
            assertThat(info.max()).isEqualTo(-1);
        }

        @Test
        @DisplayName("formatBytes 精确边界值 — 1023 字节")
        void formatBytes_shouldFormat1023AsBytes() {
            assertThat(MemoryInfo.formatBytes(1023)).isEqualTo("1023 B");
        }

        @Test
        @DisplayName("formatBytes 精确边界值 — 1024*1024-1 仍为 KB")
        void formatBytes_shouldFormatJustBelowMBAsKB() {
            assertThat(MemoryInfo.formatBytes(1024L * 1024 - 1)).startsWith("1024.0 KB").isNotNull();
        }

        @Test
        @DisplayName("formatBytes 多 TB 值")
        void formatBytes_shouldFormatMultipleTB() {
            assertThat(MemoryInfo.formatBytes(2L * 1024 * 1024 * 1024 * 1024)).isEqualTo("2.0 TB");
        }

        @Test
        @DisplayName("usagePercent 正常计算")
        void usagePercent_shouldCalculateCorrectly() {
            MemoryInfo info = MemoryInfo.of(200, 100, 100, -1);
            assertThat(info.usagePercent()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("totalDisplay / usedDisplay / freeDisplay 使用 formatBytes")
        void displayMethods_shouldDelegateToFormatBytes() {
            MemoryInfo info = MemoryInfo.of(1024, 512, 512, -1);
            assertThat(info.totalDisplay()).isEqualTo("1.0 KB");
            assertThat(info.usedDisplay()).isEqualTo("512 B");
            assertThat(info.freeDisplay()).isEqualTo("512 B");
        }
    }

    @Nested
    @DisplayName("DiskInfo - 磁盘信息")
    class DiskInfoTests {

        @Test
        @DisplayName("disks 返回非空列表")
        void disks_shouldReturnNonEmptyList() {
            List<DiskInfo> disks = SystemInfo.disks();
            assertThat(disks).isNotEmpty();
        }

        @Test
        @DisplayName("当前目录的磁盘信息有效")
        void diskForCurrentDir_shouldReturnValidInfo() throws IOException {
            DiskInfo disk = SystemInfo.disk(Path.of("."));
            assertThat(disk).isNotNull();
            assertThat(disk.name()).isNotNull();
            assertThat(disk.type()).isNotNull();
            assertThat(disk.totalSpace()).isGreaterThan(0);
            assertThat(disk.usableSpace()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("磁盘使用百分比在 [0, 100] 范围内")
        void usagePercent_shouldBeInValidRange() throws IOException {
            DiskInfo disk = SystemInfo.disk(Path.of("."));
            assertThat(disk.usagePercent()).isBetween(0.0, 100.0);
        }

        @Test
        @DisplayName("总量为零时使用百分比返回 0")
        void usagePercent_shouldReturnZeroWhenTotalIsZero() {
            DiskInfo disk = new DiskInfo("test", "ext4", 0, 0, 0, false);
            assertThat(disk.usagePercent()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("usedSpace 计算正确")
        void usedSpace_shouldCalculateCorrectly() {
            DiskInfo disk = new DiskInfo("test", "ext4", 1000, 600, 400, false);
            assertThat(disk.usedSpace()).isEqualTo(600);
        }

        @Test
        @DisplayName("usedSpace 不返回负值")
        void usedSpace_shouldNotBeNegative() {
            // Edge case: unallocated > total (shouldn't happen but guard)
            DiskInfo disk = new DiskInfo("test", "ext4", 100, 50, 200, false);
            assertThat(disk.usedSpace()).isEqualTo(0);
        }

        @Test
        @DisplayName("显示方法返回非空字符串")
        void displayMethods_shouldReturnNonBlankStrings() throws IOException {
            DiskInfo disk = SystemInfo.disk(Path.of("."));
            assertThat(disk.totalDisplay()).isNotBlank();
            assertThat(disk.usableDisplay()).isNotBlank();
        }

        @Test
        @DisplayName("diskTotal 返回正值")
        void diskTotal_shouldReturnPositiveValue() {
            long total = SystemInfo.diskTotal(Path.of("."));
            assertThat(total).isGreaterThan(0);
        }

        @Test
        @DisplayName("diskFree 返回非负值")
        void diskFree_shouldReturnNonNegativeValue() {
            long free = SystemInfo.diskFree(Path.of("."));
            assertThat(free).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("diskUsable 返回非负值")
        void diskUsable_shouldReturnNonNegativeValue() {
            long usable = SystemInfo.diskUsable(Path.of("."));
            assertThat(usable).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("构造时 name 为 null 应抛出异常")
        void constructor_shouldRejectNullName() {
            assertThatThrownBy(() -> new DiskInfo(null, "ext4", 100, 50, 50, false))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 totalSpace 为负数应抛出异常")
        void constructor_shouldRejectNegativeTotalSpace() {
            assertThatThrownBy(() -> new DiskInfo("test", "ext4", -1, 50, 50, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("构造时 type 为 null 应抛出异常")
        void constructor_shouldRejectNullType() {
            assertThatThrownBy(() -> new DiskInfo("test", null, 100, 50, 50, false))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 usableSpace 为负数应抛出异常")
        void constructor_shouldRejectNegativeUsableSpace() {
            assertThatThrownBy(() -> new DiskInfo("test", "ext4", 100, -1, 50, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("构造时 unallocatedSpace 为负数应抛出异常")
        void constructor_shouldRejectNegativeUnallocatedSpace() {
            assertThatThrownBy(() -> new DiskInfo("test", "ext4", 100, 50, -1, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("readOnly 字段可正确访问")
        void readOnly_shouldBeAccessible() {
            DiskInfo readOnlyDisk = new DiskInfo("ro", "ext4", 1000, 500, 500, true);
            assertThat(readOnlyDisk.readOnly()).isTrue();

            DiskInfo rwDisk = new DiskInfo("rw", "ext4", 1000, 500, 500, false);
            assertThat(rwDisk.readOnly()).isFalse();
        }

        @Test
        @DisplayName("totalDisplay 和 usableDisplay 格式化正确")
        void displayMethods_shouldFormatCorrectly() {
            DiskInfo disk = new DiskInfo("test", "ext4", 1024L * 1024 * 1024, 512L * 1024 * 1024, 512L * 1024 * 1024, false);
            assertThat(disk.totalDisplay()).isEqualTo("1.0 GB");
            assertThat(disk.usableDisplay()).isEqualTo("512.0 MB");
        }

        @Test
        @DisplayName("disk(null) 应抛出 NullPointerException")
        void disk_shouldRejectNullPath() {
            assertThatThrownBy(() -> SystemInfo.disk(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("disks 返回不可修改列表")
        void disks_shouldReturnUnmodifiableList() {
            List<DiskInfo> disks = SystemInfo.disks();
            assertThatThrownBy(() -> disks.add(new DiskInfo("x", "y", 0, 0, 0, false)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("OsInfo - 操作系统信息")
    class OsInfoTests {

        @Test
        @DisplayName("操作系统名称不为空")
        void name_shouldNotBeNull() {
            OsInfo os = SystemInfo.os();
            assertThat(os.name()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("操作系统版本不为空")
        void version_shouldNotBeNull() {
            OsInfo os = SystemInfo.os();
            assertThat(os.version()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("处理器架构不为空")
        void arch_shouldNotBeNull() {
            OsInfo os = SystemInfo.os();
            assertThat(os.arch()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("主机名不为空")
        void hostname_shouldNotBeNull() {
            OsInfo os = SystemInfo.os();
            assertThat(os.hostname()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("可用处理器数大于零")
        void availableProcessors_shouldBePositive() {
            OsInfo os = SystemInfo.os();
            assertThat(os.availableProcessors()).isGreaterThan(0);
        }

        @Test
        @DisplayName("构造时 name 为 null 应抛出异常")
        void constructor_shouldRejectNullName() {
            assertThatThrownBy(() -> new OsInfo(null, "1.0", "amd64", "host", 4, 1024, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 availableProcessors 小于 1 应抛出异常")
        void constructor_shouldRejectInvalidProcessors() {
            assertThatThrownBy(() -> new OsInfo("Linux", "5.0", "amd64", "host", 0, 1024, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("构造时 version 为 null 应抛出异常")
        void constructor_shouldRejectNullVersion() {
            assertThatThrownBy(() -> new OsInfo("Linux", null, "amd64", "host", 4, 1024, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 arch 为 null 应抛出异常")
        void constructor_shouldRejectNullArch() {
            assertThatThrownBy(() -> new OsInfo("Linux", "5.0", null, "host", 4, 1024, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 hostname 为 null 应抛出异常")
        void constructor_shouldRejectNullHostname() {
            assertThatThrownBy(() -> new OsInfo("Linux", "5.0", "amd64", null, 4, 1024, 0))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("OsInfo 所有字段可正确访问")
        void allFields_shouldBeAccessible() {
            OsInfo os = new OsInfo("Linux", "5.15", "amd64", "myhost", 8, 16384, 4096);
            assertThat(os.name()).isEqualTo("Linux");
            assertThat(os.version()).isEqualTo("5.15");
            assertThat(os.arch()).isEqualTo("amd64");
            assertThat(os.hostname()).isEqualTo("myhost");
            assertThat(os.availableProcessors()).isEqualTo(8);
            assertThat(os.physicalMemoryTotal()).isEqualTo(16384);
            assertThat(os.swapTotal()).isEqualTo(4096);
        }
    }

    @Nested
    @DisplayName("RuntimeInfo - JVM 运行时信息")
    class RuntimeInfoTests {

        @Test
        @DisplayName("Java 版本不为空")
        void javaVersion_shouldNotBeNull() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.javaVersion()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("JVM 运行时间大于零")
        void uptime_shouldBePositive() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.uptime()).isGreaterThan(0);
        }

        @Test
        @DisplayName("进程 ID 大于零")
        void pid_shouldBePositive() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.pid()).isGreaterThan(0);
        }

        @Test
        @DisplayName("VM 名称不为空")
        void vmName_shouldNotBeNull() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.vmName()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("VM 版本不为空")
        void vmVersion_shouldNotBeNull() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.vmVersion()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Java 厂商不为空")
        void javaVendor_shouldNotBeNull() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.javaVendor()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("Java Home 不为空")
        void javaHome_shouldNotBeNull() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.javaHome()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("启动时间大于零")
        void startTime_shouldBePositive() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.startTime()).isGreaterThan(0);
        }

        @Test
        @DisplayName("输入参数列表不为 null")
        void inputArguments_shouldNotBeNull() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.inputArguments()).isNotNull();
        }

        @Test
        @DisplayName("输入参数列表不可修改")
        void inputArguments_shouldBeUnmodifiable() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThatThrownBy(() -> rt.inputArguments().add("test"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("构造时 javaVersion 为 null 应抛出异常")
        void constructor_shouldRejectNullJavaVersion() {
            assertThatThrownBy(() -> new RuntimeInfo(
                    null, "vendor", "/java", "vm", "1.0", 100, 100, 1, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 javaVendor 为 null 应抛出异常")
        void constructor_shouldRejectNullJavaVendor() {
            assertThatThrownBy(() -> new RuntimeInfo(
                    "25", null, "/java", "vm", "1.0", 100, 100, 1, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 javaHome 为 null 应抛出异常")
        void constructor_shouldRejectNullJavaHome() {
            assertThatThrownBy(() -> new RuntimeInfo(
                    "25", "vendor", null, "vm", "1.0", 100, 100, 1, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 vmName 为 null 应抛出异常")
        void constructor_shouldRejectNullVmName() {
            assertThatThrownBy(() -> new RuntimeInfo(
                    "25", "vendor", "/java", null, "1.0", 100, 100, 1, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 vmVersion 为 null 应抛出异常")
        void constructor_shouldRejectNullVmVersion() {
            assertThatThrownBy(() -> new RuntimeInfo(
                    "25", "vendor", "/java", "vm", null, 100, 100, 1, List.of()))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("构造时 inputArguments 为 null 应抛出异常")
        void constructor_shouldRejectNullInputArguments() {
            assertThatThrownBy(() -> new RuntimeInfo(
                    "25", "vendor", "/java", "vm", "1.0", 100, 100, 1, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("inputArguments 返回不可修改列表（防御性复制）")
        void inputArguments_shouldBeDefensiveCopy() {
            List<String> mutableArgs = new ArrayList<>(List.of("-Xmx256m", "-verbose:gc"));
            RuntimeInfo rt = new RuntimeInfo("25", "vendor", "/java", "vm", "1.0", 100, 100, 1, mutableArgs);

            // 修改原始列表不影响记录
            mutableArgs.add("-Xms128m");
            assertThat(rt.inputArguments()).hasSize(2);

            // 返回的列表不可修改
            assertThatThrownBy(() -> rt.inputArguments().add("test"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("RuntimeInfo 所有字段可正确访问")
        void allFields_shouldBeAccessible() {
            RuntimeInfo rt = new RuntimeInfo("25", "Oracle", "/usr/lib/jvm", "HotSpot", "25+36",
                    5000, 1000, 42, List.of("-Xmx512m"));
            assertThat(rt.javaVersion()).isEqualTo("25");
            assertThat(rt.javaVendor()).isEqualTo("Oracle");
            assertThat(rt.javaHome()).isEqualTo("/usr/lib/jvm");
            assertThat(rt.vmName()).isEqualTo("HotSpot");
            assertThat(rt.vmVersion()).isEqualTo("25+36");
            assertThat(rt.uptime()).isEqualTo(5000);
            assertThat(rt.startTime()).isEqualTo(1000);
            assertThat(rt.pid()).isEqualTo(42);
            assertThat(rt.inputArguments()).containsExactly("-Xmx512m");
        }
    }

    @Nested
    @DisplayName("SystemInfo 门面方法")
    class FacadeTests {

        @Test
        @DisplayName("cpu 返回非 null")
        void cpu_shouldReturnNonNull() {
            assertThat(SystemInfo.cpu()).isNotNull();
        }

        @Test
        @DisplayName("cpuLoad 返回有效范围")
        void cpuLoad_shouldReturnValidRange() {
            double load = SystemInfo.cpuLoad();
            assertThat(load == -1.0 || load >= 0.0)
                    .as("cpuLoad should be -1 or >= 0.0, got: %f", load)
                    .isTrue();
        }

        @Test
        @DisplayName("processCpuLoad 返回有效范围")
        void processCpuLoad_shouldReturnValidRange() {
            double load = SystemInfo.processCpuLoad();
            assertThat(load == -1.0 || load >= 0.0)
                    .as("processCpuLoad should be -1 or >= 0.0, got: %f", load)
                    .isTrue();
        }

        @Test
        @DisplayName("loadAverage 返回非 null")
        void loadAverage_shouldReturnNonNull() {
            assertThat(SystemInfo.loadAverage()).isNotNull();
        }

        @Test
        @DisplayName("memory 返回非 null")
        void memory_shouldReturnNonNull() {
            assertThat(SystemInfo.memory()).isNotNull();
        }

        @Test
        @DisplayName("heapMemory 返回非 null")
        void heapMemory_shouldReturnNonNull() {
            assertThat(SystemInfo.heapMemory()).isNotNull();
        }

        @Test
        @DisplayName("nonHeapMemory 返回非 null")
        void nonHeapMemory_shouldReturnNonNull() {
            assertThat(SystemInfo.nonHeapMemory()).isNotNull();
        }

        @Test
        @DisplayName("physicalMemoryTotal 返回正值或 -1")
        void physicalMemoryTotal_shouldReturnPositiveOrMinusOne() {
            long total = SystemInfo.physicalMemoryTotal();
            assertThat(total == -1 || total > 0)
                    .as("physicalMemoryTotal should be -1 or > 0, got: %d", total)
                    .isTrue();
        }

        @Test
        @DisplayName("physicalMemoryFree 返回正值或 -1")
        void physicalMemoryFree_shouldReturnPositiveOrMinusOne() {
            long free = SystemInfo.physicalMemoryFree();
            assertThat(free == -1 || free >= 0)
                    .as("physicalMemoryFree should be -1 or >= 0, got: %d", free)
                    .isTrue();
        }

        @Test
        @DisplayName("swapTotal 返回非负值或 -1")
        void swapTotal_shouldReturnNonNegativeOrMinusOne() {
            long swap = SystemInfo.swapTotal();
            assertThat(swap == -1 || swap >= 0)
                    .as("swapTotal should be -1 or >= 0, got: %d", swap)
                    .isTrue();
        }

        @Test
        @DisplayName("swapFree 返回非负值或 -1")
        void swapFree_shouldReturnNonNegativeOrMinusOne() {
            long swap = SystemInfo.swapFree();
            assertThat(swap == -1 || swap >= 0)
                    .as("swapFree should be -1 or >= 0, got: %d", swap)
                    .isTrue();
        }

        @Test
        @DisplayName("hostname 不为空")
        void hostname_shouldNotBeBlank() {
            assertThat(SystemInfo.hostname()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("uptime 大于零")
        void uptime_shouldBePositive() {
            assertThat(SystemInfo.uptime()).isGreaterThan(0);
        }

        @Test
        @DisplayName("environmentVariables 不为空")
        void environmentVariables_shouldNotBeEmpty() {
            Map<String, String> env = SystemInfo.environmentVariables();
            assertThat(env).isNotEmpty();
        }

        @Test
        @DisplayName("environmentVariables 不可修改")
        void environmentVariables_shouldBeUnmodifiable() {
            Map<String, String> env = SystemInfo.environmentVariables();
            assertThatThrownBy(() -> env.put("TEST_KEY", "TEST_VALUE"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("os 返回非 null")
        void os_shouldReturnNonNull() {
            assertThat(SystemInfo.os()).isNotNull();
        }

        @Test
        @DisplayName("runtime 返回非 null")
        void runtime_shouldReturnNonNull() {
            assertThat(SystemInfo.runtime()).isNotNull();
        }

        @Test
        @DisplayName("disks 返回非 null")
        void disks_shouldReturnNonNull() {
            assertThat(SystemInfo.disks()).isNotNull();
        }

        @Test
        @DisplayName("runtime 所有字段均有效")
        void runtime_allFieldsShouldBeValid() {
            RuntimeInfo rt = SystemInfo.runtime();
            assertThat(rt.javaVersion()).isNotBlank();
            assertThat(rt.javaVendor()).isNotBlank();
            assertThat(rt.javaHome()).isNotBlank();
            assertThat(rt.vmName()).isNotBlank();
            assertThat(rt.vmVersion()).isNotBlank();
            assertThat(rt.uptime()).isGreaterThan(0);
            assertThat(rt.startTime()).isGreaterThan(0);
            assertThat(rt.pid()).isGreaterThan(0);
            assertThat(rt.inputArguments()).isNotNull();
        }

        @Test
        @DisplayName("SystemInfo 私有构造器不可实例化")
        void constructor_shouldThrowAssertionError() throws Exception {
            var ctor = SystemInfo.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            assertThatThrownBy(ctor::newInstance)
                    .hasCauseInstanceOf(AssertionError.class);
        }

        @Test
        @DisplayName("diskTotal 对当前目录返回正值")
        void diskTotal_shouldReturnPositiveForCurrentDir() {
            assertThat(SystemInfo.diskTotal(Path.of("."))).isGreaterThan(0);
        }

        @Test
        @DisplayName("diskFree 对当前目录返回非负值")
        void diskFree_shouldReturnNonNegativeForCurrentDir() {
            assertThat(SystemInfo.diskFree(Path.of("."))).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("diskUsable 对当前目录返回非负值")
        void diskUsable_shouldReturnNonNegativeForCurrentDir() {
            assertThat(SystemInfo.diskUsable(Path.of("."))).isGreaterThanOrEqualTo(0);
        }
    }
}
