package cloud.opencode.base.image.exif;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.*;

/**
 * ExifOp 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-image V2.0.0
 */
@DisplayName("ExifOp EXIF操作测试")
class ExifOpTest {

    /**
     * Create a minimal JPEG byte array (no EXIF) using ImageIO.
     */
    private static byte[] createMinimalJpeg() throws Exception {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "JPEG", baos);
        return baos.toByteArray();
    }

    /**
     * Create a JPEG byte array with a manually injected APP1 EXIF segment.
     * The EXIF contains an orientation tag with the given value.
     */
    private static byte[] createJpegWithExif(int orientation) throws Exception {
        byte[] baseJpeg = createMinimalJpeg();

        // Build TIFF/IFD data (big-endian "MM")
        // TIFF header: "MM" + 0x002A + IFD0 offset (8)
        // IFD0: 1 entry (orientation)
        // Entry: tag(2) + type(2) + count(4) + value(4) = 12 bytes
        // IFD0 next pointer: 4 bytes (0)

        int ifdEntryCount = 1;
        int tiffDataLen = 8 + 2 + ifdEntryCount * 12 + 4; // header + count + entries + next

        ByteBuffer tiff = ByteBuffer.allocate(tiffDataLen).order(ByteOrder.BIG_ENDIAN);
        // TIFF header
        tiff.put((byte) 'M');
        tiff.put((byte) 'M');
        tiff.putShort((short) 0x002A);
        tiff.putInt(8); // IFD0 offset

        // IFD0
        tiff.putShort((short) ifdEntryCount);

        // Orientation entry: tag=0x0112, type=SHORT(3), count=1, value=orientation
        tiff.putShort((short) 0x0112);
        tiff.putShort((short) 3); // SHORT
        tiff.putInt(1);           // count
        tiff.putShort((short) orientation);
        tiff.putShort((short) 0); // padding

        // Next IFD offset = 0 (no more IFDs)
        tiff.putInt(0);

        byte[] tiffBytes = tiff.array();

        // APP1 segment: "Exif\0\0" + TIFF data
        byte[] exifHeader = {'E', 'x', 'i', 'f', 0, 0};
        int app1DataLen = exifHeader.length + tiffBytes.length;
        int app1SegLen = 2 + app1DataLen; // length field includes itself

        // Build the full segment: FF E1 + length(2) + exifHeader + tiff
        ByteBuffer app1 = ByteBuffer.allocate(2 + 2 + app1DataLen);
        app1.put((byte) 0xFF);
        app1.put((byte) 0xE1);
        app1.putShort((short) app1SegLen);
        app1.put(exifHeader);
        app1.put(tiffBytes);
        byte[] app1Bytes = app1.array();

        // Inject APP1 after SOI (first 2 bytes)
        byte[] result = new byte[baseJpeg.length + app1Bytes.length];
        System.arraycopy(baseJpeg, 0, result, 0, 2); // SOI
        System.arraycopy(app1Bytes, 0, result, 2, app1Bytes.length);
        System.arraycopy(baseJpeg, 2, result, 2 + app1Bytes.length, baseJpeg.length - 2);

        return result;
    }

    /**
     * Create a JPEG with EXIF containing multiple tags (orientation + make + model).
     */
    private static byte[] createJpegWithMultipleExifTags(int orientation, String make, String model)
            throws Exception {
        byte[] baseJpeg = createMinimalJpeg();

        // We'll use big-endian
        int entryCount = 1; // orientation always
        byte[] makeBytes = null;
        byte[] modelBytes = null;

        if (make != null) {
            makeBytes = (make + "\0").getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            entryCount++;
        }
        if (model != null) {
            modelBytes = (model + "\0").getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            entryCount++;
        }

        // Calculate sizes
        int ifdSize = 2 + entryCount * 12 + 4; // count + entries + next pointer
        int dataAreaOffset = 8 + ifdSize; // after TIFF header + IFD

        int dataAreaSize = 0;
        int makeDataOffset = dataAreaOffset;
        if (makeBytes != null && makeBytes.length > 4) {
            dataAreaSize += makeBytes.length;
        }
        int modelDataOffset = dataAreaOffset + dataAreaSize;
        if (modelBytes != null && modelBytes.length > 4) {
            dataAreaSize += modelBytes.length;
        }

        int tiffDataLen = 8 + ifdSize + dataAreaSize;
        ByteBuffer tiff = ByteBuffer.allocate(tiffDataLen).order(ByteOrder.BIG_ENDIAN);

        // TIFF header
        tiff.put((byte) 'M');
        tiff.put((byte) 'M');
        tiff.putShort((short) 0x002A);
        tiff.putInt(8); // IFD0 offset

        // IFD0 entry count
        tiff.putShort((short) entryCount);

        // Orientation entry
        tiff.putShort((short) 0x0112);
        tiff.putShort((short) 3); // SHORT
        tiff.putInt(1);
        tiff.putShort((short) orientation);
        tiff.putShort((short) 0);

        // Make entry
        if (makeBytes != null) {
            tiff.putShort((short) 0x010F);
            tiff.putShort((short) 2); // ASCII
            tiff.putInt(makeBytes.length);
            if (makeBytes.length <= 4) {
                tiff.put(makeBytes);
                for (int i = makeBytes.length; i < 4; i++) tiff.put((byte) 0);
            } else {
                tiff.putInt(makeDataOffset);
            }
        }

        // Model entry
        if (modelBytes != null) {
            tiff.putShort((short) 0x0110);
            tiff.putShort((short) 2); // ASCII
            tiff.putInt(modelBytes.length);
            if (modelBytes.length <= 4) {
                tiff.put(modelBytes);
                for (int i = modelBytes.length; i < 4; i++) tiff.put((byte) 0);
            } else {
                tiff.putInt(modelDataOffset);
            }
        }

        // Next IFD offset
        tiff.putInt(0);

        // Data area
        if (makeBytes != null && makeBytes.length > 4) {
            tiff.put(makeBytes);
        }
        if (modelBytes != null && modelBytes.length > 4) {
            tiff.put(modelBytes);
        }

        byte[] tiffBytes = new byte[tiff.position()];
        tiff.flip();
        tiff.get(tiffBytes);

        // Build APP1
        byte[] exifHeader = {'E', 'x', 'i', 'f', 0, 0};
        int app1DataLen = exifHeader.length + tiffBytes.length;
        int app1SegLen = 2 + app1DataLen;

        ByteBuffer app1 = ByteBuffer.allocate(2 + 2 + app1DataLen);
        app1.put((byte) 0xFF);
        app1.put((byte) 0xE1);
        app1.putShort((short) app1SegLen);
        app1.put(exifHeader);
        app1.put(tiffBytes);
        byte[] app1Bytes = app1.array();

        byte[] result = new byte[baseJpeg.length + app1Bytes.length];
        System.arraycopy(baseJpeg, 0, result, 0, 2);
        System.arraycopy(app1Bytes, 0, result, 2, app1Bytes.length);
        System.arraycopy(baseJpeg, 2, result, 2 + app1Bytes.length, baseJpeg.length - 2);

        return result;
    }

    @Nested
    @DisplayName("read(byte[])测试")
    class ReadBytesTests {

        @Test
        @DisplayName("非JPEG数据返回ExifInfo.empty()")
        void testNonJpegReturnsEmpty() {
            byte[] pngLike = {(byte) 0x89, 0x50, 0x4E, 0x47};
            ExifInfo info = ExifOp.read(pngLike);
            assertThat(info).isEqualTo(ExifInfo.empty());
        }

        @Test
        @DisplayName("空数组返回ExifInfo.empty()")
        void testEmptyArrayReturnsEmpty() {
            ExifInfo info = ExifOp.read(new byte[0]);
            assertThat(info).isEqualTo(ExifInfo.empty());
        }

        @Test
        @DisplayName("无APP1的JPEG返回ExifInfo.empty()")
        void testJpegWithoutApp1ReturnsEmpty() throws Exception {
            byte[] jpeg = createMinimalJpeg();
            ExifInfo info = ExifOp.read(jpeg);
            assertThat(info).isEqualTo(ExifInfo.empty());
        }

        @Test
        @DisplayName("含EXIF的JPEG读取方向值")
        void testJpegWithExifOrientation() throws Exception {
            byte[] jpeg = createJpegWithExif(6);
            ExifInfo info = ExifOp.read(jpeg);
            assertThat(info.orientation()).isEqualTo(6);
            assertThat(info.needsRotation()).isTrue();
        }

        @Test
        @DisplayName("含EXIF的JPEG读取orientation=1")
        void testJpegWithExifOrientationNormal() throws Exception {
            byte[] jpeg = createJpegWithExif(1);
            ExifInfo info = ExifOp.read(jpeg);
            assertThat(info.orientation()).isEqualTo(1);
            assertThat(info.needsRotation()).isFalse();
        }

        @Test
        @DisplayName("含多标签EXIF的JPEG读取相机信息")
        void testJpegWithMultipleTags() throws Exception {
            byte[] jpeg = createJpegWithMultipleExifTags(3, "Canon", "EOS R5");
            ExifInfo info = ExifOp.read(jpeg);
            assertThat(info.orientation()).isEqualTo(3);
            assertThat(info.cameraMake()).isEqualTo("Canon");
            assertThat(info.cameraModel()).isEqualTo("EOS R5");
        }

        @Test
        @DisplayName("损坏的EXIF返回ExifInfo.empty()")
        void testCorruptExifReturnsEmpty() {
            // SOI + APP1 marker + short length + garbage
            byte[] corrupt = {
                    (byte) 0xFF, (byte) 0xD8, // SOI
                    (byte) 0xFF, (byte) 0xE1, // APP1
                    0x00, 0x10,                // length=16
                    'E', 'x', 'i', 'f', 0, 0, // EXIF header
                    'X', 'X',                  // invalid byte order
                    0, 0, 0, 0, 0, 0           // garbage
            };
            ExifInfo info = ExifOp.read(corrupt);
            assertThat(info).isEqualTo(ExifInfo.empty());
        }

        @Test
        @DisplayName("null输入抛出NullPointerException")
        void testNullBytesThrowsNpe() {
            assertThatThrownBy(() -> ExifOp.read((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("read(Path)测试")
    class ReadPathTests {

        @Test
        @DisplayName("null路径抛出NullPointerException")
        void testNullPathThrowsNpe() {
            assertThatThrownBy(() -> ExifOp.read((java.nio.file.Path) null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("不存在的文件抛出ImageIOException")
        void testNonExistentFileThrowsImageIOException() {
            assertThatThrownBy(() -> ExifOp.read(java.nio.file.Path.of("/nonexistent/photo.jpg")))
                    .isInstanceOf(cloud.opencode.base.image.exception.ImageIOException.class);
        }
    }

    @Nested
    @DisplayName("strip()测试")
    class StripTests {

        @Test
        @DisplayName("strip ALL移除APP1段")
        void testStripAllRemovesApp1() throws Exception {
            byte[] jpeg = createJpegWithExif(6);
            // Verify EXIF is present
            assertThat(ExifOp.read(jpeg).orientation()).isEqualTo(6);

            byte[] stripped = ExifOp.strip(jpeg, ExifTag.ALL);

            // After stripping, EXIF should be gone
            ExifInfo info = ExifOp.read(stripped);
            assertThat(info).isEqualTo(ExifInfo.empty());

            // The result should still be a valid JPEG (starts with SOI)
            assertThat(stripped[0] & 0xFF).isEqualTo(0xFF);
            assertThat(stripped[1] & 0xFF).isEqualTo(0xD8);

            // Should be shorter than original
            assertThat(stripped.length).isLessThan(jpeg.length);
        }

        @Test
        @DisplayName("strip非JPEG数据返回副本")
        void testStripNonJpegReturnsCopy() {
            byte[] data = {0x01, 0x02, 0x03};
            byte[] result = ExifOp.strip(data, ExifTag.ALL);
            assertThat(result).isEqualTo(data);
            assertThat(result).isNotSameAs(data);
        }

        @Test
        @DisplayName("strip无APP1的JPEG返回副本")
        void testStripJpegWithoutApp1ReturnsCopy() throws Exception {
            byte[] jpeg = createMinimalJpeg();
            byte[] result = ExifOp.strip(jpeg, ExifTag.ALL);
            assertThat(result).isEqualTo(jpeg);
            assertThat(result).isNotSameAs(jpeg);
        }

        @Test
        @DisplayName("null输入抛出NullPointerException")
        void testStripNullBytesThrowsNpe() {
            assertThatThrownBy(() -> ExifOp.strip(null, ExifTag.ALL))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null tags抛出NullPointerException")
        void testStripNullTagsThrowsNpe() throws Exception {
            byte[] jpeg = createMinimalJpeg();
            assertThatThrownBy(() -> ExifOp.strip(jpeg, (ExifTag[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("autoOrient()测试")
    class AutoOrientTests {

        @Test
        @DisplayName("orientation=1不变")
        void testOrientation1NoChange() {
            BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = ExifOp.autoOrient(img, 1);
            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(50);
        }

        @Test
        @DisplayName("orientation=6宽高互换")
        void testOrientation6SwapsDimensions() {
            BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = ExifOp.autoOrient(img, 6);
            // 90 CW rotation: width and height swap
            assertThat(result.getWidth()).isEqualTo(50);
            assertThat(result.getHeight()).isEqualTo(100);
        }

        @Test
        @DisplayName("orientation=8宽高互换")
        void testOrientation8SwapsDimensions() {
            BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = ExifOp.autoOrient(img, 8);
            // 270 CW rotation: width and height swap
            assertThat(result.getWidth()).isEqualTo(50);
            assertThat(result.getHeight()).isEqualTo(100);
        }

        @Test
        @DisplayName("orientation=3宽高不变")
        void testOrientation3KeepsDimensions() {
            BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
            BufferedImage result = ExifOp.autoOrient(img, 3);
            // 180 rotation: dimensions stay the same
            assertThat(result.getWidth()).isEqualTo(100);
            assertThat(result.getHeight()).isEqualTo(50);
        }

        @Test
        @DisplayName("null图片抛出NullPointerException")
        void testNullImageThrowsNpe() {
            assertThatThrownBy(() -> ExifOp.autoOrient(null, 1))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
