package cloud.opencode.base.io.compress;

import java.time.Instant;

/**
 * Zip Entry Metadata Record
 * Zip条目元数据记录
 *
 * <p>Immutable record holding metadata about a single entry within a Zip archive.
 * Used by {@link ZipUtil#list(java.nio.file.Path)} to report archive contents.</p>
 * <p>不可变记录，持有Zip归档中单个条目的元数据。
 * 被{@link ZipUtil#list(java.nio.file.Path)}用于报告归档内容。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Entry name, sizes, CRC, directory flag, timestamp - 条目名、大小、CRC、目录标志、时间戳</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param name           the entry name (path within the archive) | 条目名（归档内的路径）
 * @param size           the uncompressed size in bytes, or -1 if unknown | 未压缩大小（字节），未知时为-1
 * @param compressedSize the compressed size in bytes, or -1 if unknown | 压缩后大小（字节），未知时为-1
 * @param isDirectory    true if the entry is a directory | 如果条目是目录则为true
 * @param crc            the CRC-32 checksum, or -1 if unknown | CRC-32校验和，未知时为-1
 * @param lastModified   the last modification time, or null if unknown | 最后修改时间，未知时为null
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public record ZipEntryInfo(
        String name,
        long size,
        long compressedSize,
        boolean isDirectory,
        long crc,
        Instant lastModified
) {
}
