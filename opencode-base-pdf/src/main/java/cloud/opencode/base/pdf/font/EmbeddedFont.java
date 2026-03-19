package cloud.opencode.base.pdf.font;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Embedded PDF Font - Custom font embedded in PDF
 * 嵌入式 PDF 字体 - 嵌入 PDF 中的自定义字体
 *
 * <p>Supports TrueType (TTF) and OpenType (OTF) fonts.</p>
 * <p>支持 TrueType (TTF) 和 OpenType (OTF) 字体。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Load fonts from file path or byte array - 从文件路径或字节数组加载字体</li>
 *   <li>Automatic font type detection - 自动字体类型检测</li>
 *   <li>Support for TTF, OTF, and TTC formats - 支持 TTF、OTF 和 TTC 格式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EmbeddedFont font = EmbeddedFont.fromFile("MyFont", Path.of("font.ttf"));
 * EmbeddedFont fromBytes = EmbeddedFont.fromBytes("Custom", data, FontType.TRUETYPE);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes — immutable after construction - 线程安全: 是 — 构造后不可变</li>
 *   <li>Null-safe: Yes — name is validated as non-null - 空值安全: 是 — 名称已验证非空</li>
 *   <li>Defensive copies: Font byte arrays are cloned on input and output - 防御性拷贝: 字体字节数组在输入和输出时进行克隆</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class EmbeddedFont implements PdfFont {

    private final String name;
    private final Path fontPath;
    private final byte[] fontData;
    private final FontType type;

    private EmbeddedFont(String name, Path fontPath, byte[] fontData, FontType type) {
        this.name = Objects.requireNonNull(name, "Font name cannot be null");
        this.fontPath = fontPath;
        this.fontData = fontData;
        this.type = type;
    }

    /**
     * Creates embedded font from file path
     * 从文件路径创建嵌入字体
     *
     * @param name font name | 字体名称
     * @param path font file path | 字体文件路径
     * @return EmbeddedFont instance | EmbeddedFont 实例
     */
    public static EmbeddedFont fromFile(String name, Path path) {
        FontType type = detectFontType(path.toString());
        return new EmbeddedFont(name, path, null, type);
    }

    /**
     * Creates embedded font from byte array
     * 从字节数组创建嵌入字体
     *
     * @param name font name | 字体名称
     * @param data font data | 字体数据
     * @param type font type | 字体类型
     * @return EmbeddedFont instance | EmbeddedFont 实例
     */
    public static EmbeddedFont fromBytes(String name, byte[] data, FontType type) {
        return new EmbeddedFont(name, null, data.clone(), type);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPdfName() {
        return name;
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    /**
     * Gets font file path
     * 获取字体文件路径
     *
     * @return font path, or null if created from bytes | 字体路径，如果从字节创建则返回 null
     */
    public Path getFontPath() {
        return fontPath;
    }

    /**
     * Gets font data
     * 获取字体数据
     *
     * @return font data copy, or null if created from path | 字体数据副本，如果从路径创建则返回 null
     */
    public byte[] getFontData() {
        return fontData != null ? fontData.clone() : null;
    }

    /**
     * Gets font type
     * 获取字体类型
     *
     * @return font type | 字体类型
     */
    public FontType getType() {
        return type;
    }

    private static FontType detectFontType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".ttf")) return FontType.TRUETYPE;
        if (lower.endsWith(".otf")) return FontType.OPENTYPE;
        if (lower.endsWith(".ttc")) return FontType.TRUETYPE_COLLECTION;
        return FontType.TRUETYPE;
    }

    /**
     * Font Type
     * 字体类型
     */
    public enum FontType {
        /** TrueType font (.ttf) | TrueType 字体 */
        TRUETYPE,
        /** OpenType font (.otf) | OpenType 字体 */
        OPENTYPE,
        /** TrueType Collection (.ttc) | TrueType 集合 */
        TRUETYPE_COLLECTION
    }
}
