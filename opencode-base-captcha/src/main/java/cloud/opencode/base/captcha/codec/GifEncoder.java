package cloud.opencode.base.captcha.codec;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * GIF Encoder - Encodes images to animated GIF format
 * GIF 编码器 - 将图像编码为动画 GIF 格式
 *
 * <p>This class provides GIF encoding functionality for creating animated GIF CAPTCHAs.</p>
 * <p>此类提供 GIF 编码功能，用于创建动画 GIF 验证码。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * GifEncoder encoder = new GifEncoder();
 * encoder.start(outputStream);
 * encoder.setRepeat(0); // loop forever
 * encoder.setDelay(100); // 100ms between frames
 * encoder.addFrame(image1);
 * encoder.addFrame(image2);
 * encoder.finish();
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Animated GIF creation - 创建动画GIF</li>
 *   <li>Frame delay and repeat control - 帧延迟和重复控制</li>
 *   <li>Color quantization support - 颜色量化支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (single-threaded use) - 线程安全: 否（单线程使用）</li>
 *   <li>Null-safe: No (images must not be null) - 空值安全: 否（图像不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(W×H) per frame (W=width, H=height) - 时间复杂度: 每帧 O(W×H)</li>
 *   <li>Space complexity: O(W×H) for indexed pixel buffer - 空间复杂度: O(W×H) 索引像素缓冲区</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class GifEncoder {

    private static final int MAX_COLORS = 256;

    private int width;
    private int height;
    private int repeat = -1;
    private int delay = 0;
    private boolean started = false;
    private OutputStream out;
    private BufferedImage image;
    private byte[] indexedPixels;
    private int colorDepth;
    private byte[] colorTab;
    private boolean[] usedEntry = new boolean[256];
    private int palSize = 7;
    private int dispose = -1;
    private boolean firstFrame = true;
    private int sample = 10;

    /**
     * Sets the delay time between frames.
     * 设置帧之间的延迟时间。
     *
     * @param ms the delay in milliseconds | 延迟（毫秒）
     */
    public void setDelay(int ms) {
        this.delay = Math.round(ms / 10.0f);
    }

    /**
     * Sets the disposal method.
     * 设置处理方法。
     *
     * @param code the disposal code | 处理代码
     */
    public void setDispose(int code) {
        if (code >= 0) {
            this.dispose = code;
        }
    }

    /**
     * Sets the repeat count.
     * 设置重复次数。
     *
     * @param iter 0 for infinite, -1 for no repeat, otherwise repeat count
     *             0 表示无限，-1 表示不重复，否则为重复次数
     */
    public void setRepeat(int iter) {
        if (iter >= 0) {
            this.repeat = iter;
        }
    }

    /**
     * Sets the color quantization quality.
     * 设置颜色量化质量。
     *
     * @param quality 1-30, lower is better quality but slower (default 10)
     *                1-30，越低质量越好但越慢（默认 10）
     */
    public void setQuality(int quality) {
        if (quality < 1) quality = 1;
        this.sample = quality;
    }

    /**
     * Starts GIF encoding to the output stream.
     * 开始 GIF 编码到输出流。
     *
     * @param os the output stream | 输出流
     * @return true if started successfully | 如果成功启动返回 true
     */
    public boolean start(OutputStream os) {
        if (os == null) {
            return false;
        }
        this.out = os;
        try {
            writeString("GIF89a");
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start GIF encoding", e);
        }
        this.started = true;
        return true;
    }

    /**
     * Adds a frame to the GIF.
     * 添加帧到 GIF。
     *
     * @param im the frame image | 帧图像
     * @return true if added successfully | 如果成功添加返回 true
     */
    public boolean addFrame(BufferedImage im) {
        if (im == null || !started) {
            return false;
        }
        try {
            this.image = im;
            this.width = im.getWidth();
            this.height = im.getHeight();

            getImagePixels();
            analyzePixels();

            if (firstFrame) {
                writeLSD();
                writePalette();
                if (repeat >= 0) {
                    writeNetscapeExt();
                }
            }

            writeGraphicCtrlExt();
            writeImageDesc();
            if (!firstFrame) {
                writePalette();
            }
            writePixels();
            firstFrame = false;
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to add frame to GIF", e);
        }
    }

    /**
     * Finishes the GIF encoding.
     * 完成 GIF 编码。
     *
     * @return true if finished successfully | 如果成功完成返回 true
     */
    public boolean finish() {
        if (!started) {
            return false;
        }
        try {
            out.write(0x3b); // GIF trailer
            out.flush();
            started = false;
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to finish GIF encoding", e);
        }
    }

    /**
     * Extracts pixels from the image.
     */
    private void getImagePixels() {
        int w = image.getWidth();
        int h = image.getHeight();
        int type = image.getType();

        if (type != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage temp = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            java.awt.Graphics g = temp.getGraphics();
            try {
                g.drawImage(image, 0, 0, null);
            } finally {
                g.dispose();
            }
            image = temp;
        }
    }

    /**
     * Analyzes pixels and builds color palette.
     */
    private void analyzePixels() {
        int len = width * height;
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        NeuQuantEncoder nq = new NeuQuantEncoder(pixels, len, sample);
        colorTab = nq.process();

        // Map pixels to palette indices
        indexedPixels = new byte[len];
        int k = 0;
        for (int i = 0; i < len; i++) {
            int index = nq.map(
                pixels[k++] & 0xff,
                pixels[k++] & 0xff,
                pixels[k++] & 0xff
            );
            usedEntry[index] = true;
            indexedPixels[i] = (byte) index;
        }

        colorDepth = 8;
        palSize = 7;
    }

    /**
     * Writes the Logical Screen Descriptor.
     */
    private void writeLSD() throws IOException {
        writeShort(width);
        writeShort(height);
        out.write(0x80 | 0x70 | palSize);
        out.write(0);
        out.write(0);
    }

    /**
     * Writes the color palette.
     */
    private void writePalette() throws IOException {
        out.write(colorTab, 0, colorTab.length);
        int n = (3 * MAX_COLORS) - colorTab.length;
        for (int i = 0; i < n; i++) {
            out.write(0);
        }
    }

    /**
     * Writes the Netscape loop extension.
     */
    private void writeNetscapeExt() throws IOException {
        out.write(0x21); // Extension
        out.write(0xff); // Application
        out.write(11);   // Block size
        writeString("NETSCAPE2.0");
        out.write(3);    // Sub-block size
        out.write(1);
        writeShort(repeat);
        out.write(0);    // Block terminator
    }

    /**
     * Writes the Graphic Control Extension.
     */
    private void writeGraphicCtrlExt() throws IOException {
        out.write(0x21); // Extension
        out.write(0xf9); // Graphic control
        out.write(4);    // Block size

        int transp = 0;
        int disp = dispose >= 0 ? dispose << 2 : 0;

        out.write(disp | transp);
        writeShort(delay);
        out.write(0);    // Transparent color index
        out.write(0);    // Block terminator
    }

    /**
     * Writes the Image Descriptor.
     */
    private void writeImageDesc() throws IOException {
        out.write(0x2c); // Image separator
        writeShort(0);   // X position
        writeShort(0);   // Y position
        writeShort(width);
        writeShort(height);
        out.write(0x80 | palSize);
    }

    /**
     * Writes the image pixels using LZW compression.
     */
    private void writePixels() throws IOException {
        LZWEncoder encoder = new LZWEncoder(width, height, indexedPixels, colorDepth);
        encoder.encode(out);
    }

    /**
     * Writes a short value (little endian).
     */
    private void writeShort(int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }

    /**
     * Writes a string.
     */
    private void writeString(String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            out.write((byte) s.charAt(i));
        }
    }
}
