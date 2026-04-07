package cloud.opencode.base.image.exif;

import cloud.opencode.base.image.exception.ImageIOException;
import cloud.opencode.base.image.internal.RotateOp;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * EXIF Operations — Read, Strip and Auto-Orient
 * EXIF 操作 — 读取、清除和自动旋转
 *
 * <p>Pure Java EXIF parser for JPEG images. Reads EXIF metadata from the APP1
 * segment without any third-party dependencies. Supports reading orientation,
 * camera info, GPS coordinates, date/time, and software tags.</p>
 * <p>纯 Java 实现的 JPEG 图片 EXIF 解析器。从 APP1 段读取 EXIF 元数据，
 * 不依赖任何第三方库。支持读取方向、相机信息、GPS 坐标、日期时间和软件标签。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Read EXIF metadata from JPEG files or byte arrays - 从 JPEG 文件或字节数组读取 EXIF 元数据</li>
 *   <li>Strip EXIF tags selectively or entirely - 选择性或完全清除 EXIF 标签</li>
 *   <li>Auto-orient images based on EXIF orientation - 根据 EXIF 方向自动旋转图片</li>
 *   <li>Pure Java implementation, no third-party dependencies - 纯 Java 实现，无第三方依赖</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Read EXIF from file
 * ExifInfo info = ExifOp.read(Path.of("photo.jpg"));
 *
 * // Read EXIF from bytes
 * ExifInfo info = ExifOp.read(jpegBytes);
 *
 * // Strip all EXIF data
 * byte[] clean = ExifOp.strip(jpegBytes, ExifTag.ALL);
 *
 * // Auto-orient image
 * BufferedImage corrected = ExifOp.autoOrient(image, info.orientation());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具）</li>
 *   <li>Null-safe: No (throws NullPointerException on null input) - 空值安全: 否（null 输入抛出 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
public final class ExifOp {

    // JPEG markers
    private static final int JPEG_SOI = 0xFFD8;
    private static final int MARKER_APP1 = 0xFFE1;

    // EXIF header
    private static final byte[] EXIF_HEADER = {'E', 'x', 'i', 'f', 0, 0};

    // TIFF tag IDs
    private static final int TAG_IMAGE_WIDTH = 0x0100;
    private static final int TAG_IMAGE_HEIGHT = 0x0101;
    private static final int TAG_MAKE = 0x010F;
    private static final int TAG_MODEL = 0x0110;
    private static final int TAG_ORIENTATION = 0x0112;
    private static final int TAG_SOFTWARE = 0x0131;
    private static final int TAG_DATETIME = 0x0132;
    private static final int TAG_EXIF_IFD = 0x8769;
    private static final int TAG_GPS_IFD = 0x8825;

    // GPS tag IDs
    private static final int TAG_GPS_LAT_REF = 0x0001;
    private static final int TAG_GPS_LAT = 0x0002;
    private static final int TAG_GPS_LON_REF = 0x0003;
    private static final int TAG_GPS_LON = 0x0004;

    // TIFF types
    private static final int TYPE_BYTE = 1;
    private static final int TYPE_ASCII = 2;
    private static final int TYPE_SHORT = 3;
    private static final int TYPE_LONG = 4;
    private static final int TYPE_RATIONAL = 5;

    // EXIF DateTime format: "yyyy:MM:dd HH:mm:ss"
    private static final DateTimeFormatter EXIF_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    // Maximum IFD depth to prevent infinite loops from malformed data
    private static final int MAX_IFD_DEPTH = 8;

    private ExifOp() {
        // Utility class
    }

    /**
     * Read EXIF metadata from a JPEG file
     * 从 JPEG 文件读取 EXIF 元数据
     *
     * @param path the path to the JPEG file | JPEG 文件路径
     * @return the parsed EXIF info, or empty if not a JPEG or no EXIF | 解析的 EXIF 信息，非 JPEG 或无 EXIF 则返回空
     * @throws ImageIOException if the file cannot be read | 当文件无法读取时
     * @throws NullPointerException if path is null | 当 path 为 null 时
     */
    public static ExifInfo read(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try {
            long fileSize = Files.size(path);
            if (fileSize > 100 * 1024 * 1024) {
                throw new ImageIOException("File too large for EXIF reading: " + fileSize + " bytes");
            }
            byte[] data = Files.readAllBytes(path);
            return read(data);
        } catch (IOException e) {
            throw new ImageIOException("Failed to read EXIF from file", e);
        }
    }

    /**
     * Read EXIF metadata from JPEG bytes
     * 从 JPEG 字节数组读取 EXIF 元数据
     *
     * @param jpegBytes the JPEG image bytes | JPEG 图片字节数组
     * @return the parsed EXIF info, or empty if not a JPEG or no EXIF | 解析的 EXIF 信息，非 JPEG 或无 EXIF 则返回空
     * @throws NullPointerException if jpegBytes is null | 当 jpegBytes 为 null 时
     */
    public static ExifInfo read(byte[] jpegBytes) {
        Objects.requireNonNull(jpegBytes, "jpegBytes must not be null");
        try {
            return parseExif(jpegBytes);
        } catch (ImageIOException e) {
            throw e;
        } catch (Exception e) {
            // Corrupt or malformed EXIF data
            return ExifInfo.empty();
        }
    }

    /**
     * Strip EXIF tags from JPEG bytes
     * 从 JPEG 字节数组中清除 EXIF 标签
     *
     * <p>If {@link ExifTag#ALL} is included, the entire APP1 segment is removed.
     * Otherwise, only the specified tag categories are zeroed out within the
     * existing EXIF structure.</p>
     * <p>如果包含 {@link ExifTag#ALL}，则移除整个 APP1 段。
     * 否则，仅在现有 EXIF 结构中将指定标签类别清零。</p>
     *
     * @param jpegBytes the JPEG image bytes | JPEG 图片字节数组
     * @param tags      the tag categories to strip | 要清除的标签类别
     * @return the JPEG bytes with specified tags stripped | 清除指定标签后的 JPEG 字节数组
     * @throws NullPointerException if jpegBytes or tags is null | 当 jpegBytes 或 tags 为 null 时
     */
    public static byte[] strip(byte[] jpegBytes, ExifTag... tags) {
        Objects.requireNonNull(jpegBytes, "jpegBytes must not be null");
        Objects.requireNonNull(tags, "tags must not be null");

        if (!isJpeg(jpegBytes)) {
            return jpegBytes.clone();
        }

        Set<ExifTag> tagSet = Set.of(tags);

        if (tagSet.contains(ExifTag.ALL)) {
            return stripAllApp1(jpegBytes);
        }

        return stripSelectiveTags(jpegBytes, tagSet);
    }

    /**
     * Auto-orient an image based on EXIF orientation value
     * 根据 EXIF 方向值自动旋转图片
     *
     * @param image       the source image | 源图片
     * @param orientation the EXIF orientation value (1-8) | EXIF 方向值 (1-8)
     * @return the correctly oriented image | 正确方向的图片
     * @throws NullPointerException if image is null | 当 image 为 null 时
     */
    public static BufferedImage autoOrient(BufferedImage image, int orientation) {
        Objects.requireNonNull(image, "image must not be null");
        return RotateOp.applyExifOrientation(image, orientation);
    }

    // ==================== Internal parsing ====================

    /**
     * Parse EXIF metadata from JPEG bytes.
     */
    private static ExifInfo parseExif(byte[] data) {
        if (!isJpeg(data)) {
            return ExifInfo.empty();
        }

        // Find APP1 marker
        int app1Offset = findApp1(data);
        if (app1Offset < 0) {
            return ExifInfo.empty();
        }

        // APP1 segment: marker(2) + length(2) + "Exif\0\0"(6) + TIFF data
        int segStart = app1Offset + 2; // skip marker
        if (segStart + 2 > data.length) {
            return ExifInfo.empty();
        }
        int segLen = ((data[segStart] & 0xFF) << 8) | (data[segStart + 1] & 0xFF);
        int exifStart = segStart + 2; // skip length

        // Verify "Exif\0\0" header
        if (exifStart + 6 > data.length) {
            return ExifInfo.empty();
        }
        for (int i = 0; i < EXIF_HEADER.length; i++) {
            if (data[exifStart + i] != EXIF_HEADER[i]) {
                return ExifInfo.empty();
            }
        }

        int tiffStart = exifStart + 6; // TIFF header start
        if (tiffStart + 8 > data.length) {
            return ExifInfo.empty();
        }

        // Determine byte order
        ByteOrder order;
        if (data[tiffStart] == 'I' && data[tiffStart + 1] == 'I') {
            order = ByteOrder.LITTLE_ENDIAN;
        } else if (data[tiffStart] == 'M' && data[tiffStart + 1] == 'M') {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            return ExifInfo.empty();
        }

        ByteBuffer buf = ByteBuffer.wrap(data).order(order);

        // Verify TIFF magic 0x002A
        int magic = buf.getShort(tiffStart + 2) & 0xFFFF;
        if (magic != 0x002A) {
            return ExifInfo.empty();
        }

        // IFD0 offset (relative to TIFF start)
        int ifd0Offset = buf.getInt(tiffStart + 4);
        if (ifd0Offset < 8) {
            return ExifInfo.empty();
        }

        // Parse IFD0
        ExifBuilder builder = new ExifBuilder();
        parseIfd(buf, tiffStart, ifd0Offset, builder, false, 0);

        return builder.build();
    }

    /**
     * Parse an IFD (Image File Directory).
     */
    private static void parseIfd(ByteBuffer buf, int tiffStart, int ifdOffset,
                                  ExifBuilder builder, boolean isGps, int depth) {
        if (depth > MAX_IFD_DEPTH) {
            return;
        }

        int absOffset = tiffStart + ifdOffset;
        if (absOffset + 2 > buf.limit()) {
            return;
        }

        int entryCount = buf.getShort(absOffset) & 0xFFFF;
        if (entryCount > 1000) {
            // Sanity check to avoid malformed data
            return;
        }

        int entryStart = absOffset + 2;
        for (int i = 0; i < entryCount; i++) {
            int pos = entryStart + i * 12;
            if (pos + 12 > buf.limit()) {
                return;
            }

            int tag = buf.getShort(pos) & 0xFFFF;
            int type = buf.getShort(pos + 2) & 0xFFFF;
            int count = buf.getInt(pos + 4);
            int valueOffset = pos + 8; // in-place value

            if (isGps) {
                parseGpsTag(buf, tiffStart, tag, type, count, valueOffset, builder);
            } else {
                parseIfd0Tag(buf, tiffStart, tag, type, count, valueOffset, builder, depth);
            }
        }
    }

    /**
     * Parse a tag from IFD0 or ExifIFD.
     */
    private static void parseIfd0Tag(ByteBuffer buf, int tiffStart, int tag, int type,
                                      int count, int valueOffset, ExifBuilder builder, int depth) {
        switch (tag) {
            case TAG_ORIENTATION -> {
                if (type == TYPE_SHORT && count == 1) {
                    builder.orientation = buf.getShort(valueOffset) & 0xFFFF;
                }
            }
            case TAG_MAKE -> builder.cameraMake = readString(buf, tiffStart, type, count, valueOffset);
            case TAG_MODEL -> builder.cameraModel = readString(buf, tiffStart, type, count, valueOffset);
            case TAG_SOFTWARE -> builder.software = readString(buf, tiffStart, type, count, valueOffset);
            case TAG_DATETIME -> {
                String dtStr = readString(buf, tiffStart, type, count, valueOffset);
                if (dtStr != null && !dtStr.isBlank()) {
                    builder.dateTime = parseDateTime(dtStr);
                }
            }
            case TAG_IMAGE_WIDTH -> builder.imageWidth = readIntValue(buf, type, count, valueOffset);
            case TAG_IMAGE_HEIGHT -> builder.imageHeight = readIntValue(buf, type, count, valueOffset);
            case TAG_EXIF_IFD -> {
                int subIfdOffset = buf.getInt(valueOffset);
                if (subIfdOffset > 0) {
                    parseIfd(buf, tiffStart, subIfdOffset, builder, false, depth + 1);
                }
            }
            case TAG_GPS_IFD -> {
                int gpsIfdOffset = buf.getInt(valueOffset);
                if (gpsIfdOffset > 0) {
                    parseIfd(buf, tiffStart, gpsIfdOffset, builder, true, depth + 1);
                }
            }
            default -> {
                // Ignore unknown tags
            }
        }
    }

    /**
     * Parse a GPS tag.
     */
    private static void parseGpsTag(ByteBuffer buf, int tiffStart, int tag, int type,
                                     int count, int valueOffset, ExifBuilder builder) {
        switch (tag) {
            case TAG_GPS_LAT_REF -> builder.latRef = readString(buf, tiffStart, type, count, valueOffset);
            case TAG_GPS_LAT -> {
                if (type == TYPE_RATIONAL && count == 3) {
                    builder.latRationals = readRationals(buf, tiffStart, valueOffset, 3);
                }
            }
            case TAG_GPS_LON_REF -> builder.lonRef = readString(buf, tiffStart, type, count, valueOffset);
            case TAG_GPS_LON -> {
                if (type == TYPE_RATIONAL && count == 3) {
                    builder.lonRationals = readRationals(buf, tiffStart, valueOffset, 3);
                }
            }
            default -> {
                // Ignore other GPS tags
            }
        }
    }

    /**
     * Read an ASCII string from EXIF data.
     */
    private static String readString(ByteBuffer buf, int tiffStart, int type, int count, int valueOffset) {
        if (type != TYPE_ASCII || count <= 0 || count > 65535) {
            return null;
        }
        int dataOffset;
        if (count <= 4) {
            dataOffset = valueOffset;
        } else {
            long relOffsetL = buf.getInt(valueOffset) & 0xFFFFFFFFL;
            long dataOffsetL = (long) tiffStart + relOffsetL;
            if (dataOffsetL < 0 || dataOffsetL + count > buf.limit()) {
                return null;
            }
            dataOffset = (int) dataOffsetL;
        }
        if (dataOffset < 0 || dataOffset + count > buf.limit()) {
            return null;
        }
        byte[] strBytes = new byte[count];
        for (int i = 0; i < count; i++) {
            strBytes[i] = buf.get(dataOffset + i);
        }
        // Trim null terminator
        int len = count;
        while (len > 0 && strBytes[len - 1] == 0) {
            len--;
        }
        if (len == 0) {
            return null;
        }
        return new String(strBytes, 0, len, StandardCharsets.US_ASCII).trim();
    }

    /**
     * Read an integer value (SHORT or LONG).
     */
    private static Integer readIntValue(ByteBuffer buf, int type, int count, int valueOffset) {
        if (count != 1) {
            return null;
        }
        if (type == TYPE_SHORT) {
            return (int) (buf.getShort(valueOffset) & 0xFFFF);
        } else if (type == TYPE_LONG) {
            return buf.getInt(valueOffset);
        }
        return null;
    }

    /**
     * Read RATIONAL values (numerator/denominator pairs).
     */
    private static double[] readRationals(ByteBuffer buf, int tiffStart, int valueOffset, int count) {
        // RATIONALs are always stored at an offset (3 rationals = 24 bytes > 4)
        long relOffsetL = buf.getInt(valueOffset) & 0xFFFFFFFFL;
        long absOffsetL = (long) tiffStart + relOffsetL;
        if (absOffsetL < 0 || absOffsetL + (long) count * 8 > buf.limit()) {
            return null;
        }
        int absOffset = (int) absOffsetL;
        if (absOffset < 0 || absOffset + count * 8 > buf.limit()) {
            return null;
        }
        double[] values = new double[count];
        for (int i = 0; i < count; i++) {
            long numerator = buf.getInt(absOffset + i * 8) & 0xFFFFFFFFL;
            long denominator = buf.getInt(absOffset + i * 8 + 4) & 0xFFFFFFFFL;
            if (denominator == 0) {
                return null;
            }
            values[i] = (double) numerator / denominator;
        }
        return values;
    }

    /**
     * Convert DMS rationals to decimal degrees.
     */
    private static Double dmsToDecimal(double[] dms, String ref) {
        if (dms == null || dms.length != 3 || ref == null || ref.isEmpty()) {
            return null;
        }
        double decimal = dms[0] + dms[1] / 60.0 + dms[2] / 3600.0;
        if ("S".equalsIgnoreCase(ref) || "W".equalsIgnoreCase(ref)) {
            decimal = -decimal;
        }
        return decimal;
    }

    /**
     * Parse EXIF datetime string.
     */
    private static Instant parseDateTime(String dateStr) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(dateStr.trim(), EXIF_DATE_FORMAT);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ==================== JPEG structure utilities ====================

    /**
     * Check if data starts with JPEG SOI marker.
     */
    private static boolean isJpeg(byte[] data) {
        return data.length >= 2
                && (data[0] & 0xFF) == 0xFF
                && (data[1] & 0xFF) == 0xD8;
    }

    /**
     * Find the APP1 marker containing EXIF data.
     * Returns the offset of the APP1 marker (0xFFE1), or -1 if not found.
     */
    private static int findApp1(byte[] data) {
        int offset = 2; // skip SOI
        while (offset + 4 <= data.length) {
            int marker = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);

            // Check for SOS (start of scan) - stop searching
            if (marker == 0xFFDA) {
                return -1;
            }

            // Non-marker byte - malformed
            if ((marker & 0xFF00) != 0xFF00) {
                return -1;
            }

            // Standalone markers (no length field)
            if (marker >= 0xFFD0 && marker <= 0xFFD9) {
                offset += 2;
                continue;
            }

            int segLen = ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF);
            if (segLen < 2) {
                return -1;
            }

            if (marker == (MARKER_APP1 & 0xFFFF)) {
                // Verify it's an EXIF APP1 (has "Exif\0\0" header)
                if (offset + 4 + EXIF_HEADER.length <= data.length) {
                    boolean isExif = true;
                    for (int i = 0; i < EXIF_HEADER.length; i++) {
                        if (data[offset + 4 + i] != EXIF_HEADER[i]) {
                            isExif = false;
                            break;
                        }
                    }
                    if (isExif) {
                        return offset;
                    }
                }
            }

            if (offset + 2 + segLen > data.length) {
                return -1; // truncated segment, malformed JPEG
            }
            offset += 2 + segLen;
        }
        return -1;
    }

    /**
     * Remove the entire APP1 EXIF segment from JPEG data.
     */
    private static byte[] stripAllApp1(byte[] data) {
        int app1Offset = findApp1(data);
        if (app1Offset < 0) {
            return data.clone();
        }

        int segLen = ((data[app1Offset + 2] & 0xFF) << 8) | (data[app1Offset + 3] & 0xFF);
        int totalSegLen = 2 + segLen; // marker(2) + segment length

        byte[] result = new byte[data.length - totalSegLen];
        System.arraycopy(data, 0, result, 0, app1Offset);
        System.arraycopy(data, app1Offset + totalSegLen, result, app1Offset,
                data.length - app1Offset - totalSegLen);
        return result;
    }

    /**
     * Strip selective EXIF tags by zeroing their values within the APP1 segment.
     * This preserves the overall JPEG structure.
     */
    private static byte[] stripSelectiveTags(byte[] data, Set<ExifTag> tagSet) {
        int app1Offset = findApp1(data);
        if (app1Offset < 0) {
            return data.clone();
        }

        // Work on a copy
        byte[] result = data.clone();

        int segStart = app1Offset + 2;
        int exifStart = segStart + 2;
        int tiffStart = exifStart + 6;

        if (tiffStart + 8 > result.length) {
            return result;
        }

        ByteOrder order;
        if (result[tiffStart] == 'I' && result[tiffStart + 1] == 'I') {
            order = ByteOrder.LITTLE_ENDIAN;
        } else if (result[tiffStart] == 'M' && result[tiffStart + 1] == 'M') {
            order = ByteOrder.BIG_ENDIAN;
        } else {
            return result;
        }

        ByteBuffer buf = ByteBuffer.wrap(result).order(order);
        int magic = buf.getShort(tiffStart + 2) & 0xFFFF;
        if (magic != 0x002A) {
            return result;
        }

        int ifd0Offset = buf.getInt(tiffStart + 4);
        if (ifd0Offset < 8) {
            return result;
        }

        Set<Integer> tagsToZero = resolveTagIds(tagSet);
        zeroIfdTags(buf, tiffStart, ifd0Offset, tagsToZero, tagSet.contains(ExifTag.GPS), 0);

        return result;
    }

    /**
     * Resolve ExifTag categories to actual TIFF tag IDs.
     */
    private static Set<Integer> resolveTagIds(Set<ExifTag> tagSet) {
        var ids = new java.util.HashSet<Integer>();
        if (tagSet.contains(ExifTag.ORIENTATION)) {
            ids.add(TAG_ORIENTATION);
        }
        if (tagSet.contains(ExifTag.CAMERA)) {
            ids.add(TAG_MAKE);
            ids.add(TAG_MODEL);
        }
        if (tagSet.contains(ExifTag.DATETIME)) {
            ids.add(TAG_DATETIME);
        }
        if (tagSet.contains(ExifTag.SOFTWARE)) {
            ids.add(TAG_SOFTWARE);
        }
        // GPS is handled by removing the GPS IFD pointer
        if (tagSet.contains(ExifTag.GPS)) {
            ids.add(TAG_GPS_IFD);
        }
        return ids;
    }

    /**
     * Zero out IFD entries matching the specified tag IDs.
     */
    private static void zeroIfdTags(ByteBuffer buf, int tiffStart, int ifdOffset,
                                     Set<Integer> tagIds, boolean stripGps, int depth) {
        if (depth > MAX_IFD_DEPTH) {
            return;
        }

        int absOffset = tiffStart + ifdOffset;
        if (absOffset + 2 > buf.limit()) {
            return;
        }

        int entryCount = buf.getShort(absOffset) & 0xFFFF;
        if (entryCount > 1000) {
            return;
        }

        int entryStart = absOffset + 2;
        for (int i = 0; i < entryCount; i++) {
            int pos = entryStart + i * 12;
            if (pos + 12 > buf.limit()) {
                return;
            }

            int tag = buf.getShort(pos) & 0xFFFF;

            if (tagIds.contains(tag)) {
                // Zero out the 12-byte entry
                for (int j = 0; j < 12; j++) {
                    buf.put(pos + j, (byte) 0);
                }
            } else if (tag == TAG_EXIF_IFD) {
                // Recurse into ExifIFD to find matching tags
                int subIfdOffset = buf.getInt(pos + 8);
                if (subIfdOffset > 0) {
                    zeroIfdTags(buf, tiffStart, subIfdOffset, tagIds, stripGps, depth + 1);
                }
            }
        }
    }

    // ==================== Builder ====================

    /**
     * Mutable builder used during EXIF parsing.
     */
    private static final class ExifBuilder {
        int orientation = 0;
        String cameraMake;
        String cameraModel;
        Instant dateTime;
        Integer imageWidth;
        Integer imageHeight;
        String software;
        String latRef;
        double[] latRationals;
        String lonRef;
        double[] lonRationals;

        ExifInfo build() {
            Double latitude = dmsToDecimal(latRationals, latRef);
            Double longitude = dmsToDecimal(lonRationals, lonRef);
            return new ExifInfo(orientation, cameraMake, cameraModel, dateTime,
                    latitude, longitude, imageWidth, imageHeight, software);
        }
    }
}
