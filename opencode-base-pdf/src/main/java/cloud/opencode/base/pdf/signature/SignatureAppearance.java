package cloud.opencode.base.pdf.signature;

import cloud.opencode.base.pdf.font.PdfFont;

import java.awt.Color;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Signature Appearance Builder
 * 签名外观构建器
 *
 * <p>Configures the visual appearance of a PDF signature.</p>
 * <p>配置 PDF 签名的可视外观。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Signature image from file or bytes - 从文件或字节设置签名图像</li>
 *   <li>Text description and font configuration - 文本描述和字体配置</li>
 *   <li>Visibility toggles for name, date, reason, location - 名称、日期、原因、位置的可见性切换</li>
 *   <li>Background and border styling - 背景和边框样式</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SignatureAppearance appearance = SignatureAppearance.defaultAppearance()
 *     .image(Path.of("signature.png"))
 *     .showReason(true)
 *     .showLocation(true)
 *     .fontSize(10);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable builder pattern - 线程安全: 否 — 可变构建器模式</li>
 *   <li>Null-safe: Yes — parameters are validated where critical - 空值安全: 是 — 关键参数已验证</li>
 *   <li>Defensive copies: Image byte arrays are cloned on input and output - 防御性拷贝: 图像字节数组在输入和输出时进行克隆</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class SignatureAppearance {

    private Path imagePath;
    private byte[] imageBytes;
    private String description;
    private PdfFont font;
    private float fontSize = 12f;
    private Color textColor = Color.BLACK;
    private Color backgroundColor;
    private float borderWidth;
    private Color borderColor;
    private boolean showSignerName = true;
    private boolean showDate = true;
    private boolean showReason = false;
    private boolean showLocation = false;

    private SignatureAppearance() {}

    // ==================== 构建方法 Builder Methods ====================

    /**
     * Sets signature image from file.
     * 从文件设置签名图像。
     *
     * @param imagePath image file path | 图像文件路径
     * @return this appearance | 当前外观
     */
    public SignatureAppearance image(Path imagePath) {
        this.imagePath = Objects.requireNonNull(imagePath, "imagePath cannot be null");
        this.imageBytes = null;
        return this;
    }

    /**
     * Sets signature image from bytes.
     * 从字节设置签名图像。
     *
     * @param imageBytes image bytes | 图像字节
     * @return this appearance | 当前外观
     */
    public SignatureAppearance image(byte[] imageBytes) {
        this.imageBytes = Objects.requireNonNull(imageBytes, "imageBytes cannot be null").clone();
        this.imagePath = null;
        return this;
    }

    /**
     * Sets description text.
     * 设置描述文本。
     *
     * @param description description text | 描述文本
     * @return this appearance | 当前外观
     */
    public SignatureAppearance description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets font for text.
     * 设置文本字体。
     *
     * @param font font to use | 使用的字体
     * @return this appearance | 当前外观
     */
    public SignatureAppearance font(PdfFont font) {
        this.font = font;
        return this;
    }

    /**
     * Sets font size.
     * 设置字体大小。
     *
     * @param size font size | 字体大小
     * @return this appearance | 当前外观
     */
    public SignatureAppearance fontSize(float size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Font size must be positive");
        }
        this.fontSize = size;
        return this;
    }

    /**
     * Sets text color.
     * 设置文本颜色。
     *
     * @param color text color | 文本颜色
     * @return this appearance | 当前外观
     */
    public SignatureAppearance textColor(Color color) {
        this.textColor = Objects.requireNonNull(color, "color cannot be null");
        return this;
    }

    /**
     * Sets background color.
     * 设置背景颜色。
     *
     * @param color background color | 背景颜色
     * @return this appearance | 当前外观
     */
    public SignatureAppearance backgroundColor(Color color) {
        this.backgroundColor = color;
        return this;
    }

    /**
     * Sets border.
     * 设置边框。
     *
     * @param width border width | 边框宽度
     * @param color border color | 边框颜色
     * @return this appearance | 当前外观
     */
    public SignatureAppearance border(float width, Color color) {
        this.borderWidth = width;
        this.borderColor = color;
        return this;
    }

    /**
     * Shows signer name.
     * 显示签名者名称。
     *
     * @param show whether to show | 是否显示
     * @return this appearance | 当前外观
     */
    public SignatureAppearance showSignerName(boolean show) {
        this.showSignerName = show;
        return this;
    }

    /**
     * Shows signing date.
     * 显示签名日期。
     *
     * @param show whether to show | 是否显示
     * @return this appearance | 当前外观
     */
    public SignatureAppearance showDate(boolean show) {
        this.showDate = show;
        return this;
    }

    /**
     * Shows reason.
     * 显示原因。
     *
     * @param show whether to show | 是否显示
     * @return this appearance | 当前外观
     */
    public SignatureAppearance showReason(boolean show) {
        this.showReason = show;
        return this;
    }

    /**
     * Shows location.
     * 显示位置。
     *
     * @param show whether to show | 是否显示
     * @return this appearance | 当前外观
     */
    public SignatureAppearance showLocation(boolean show) {
        this.showLocation = show;
        return this;
    }

    // ==================== 访问方法 Accessors ====================

    public Path getImagePath() {
        return imagePath;
    }

    public byte[] getImageBytes() {
        return imageBytes != null ? imageBytes.clone() : null;
    }

    public String getDescription() {
        return description;
    }

    public PdfFont getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public Color getTextColor() {
        return textColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public boolean isShowSignerName() {
        return showSignerName;
    }

    public boolean isShowDate() {
        return showDate;
    }

    public boolean isShowReason() {
        return showReason;
    }

    public boolean isShowLocation() {
        return showLocation;
    }

    // ==================== 静态工厂 Static Factory ====================

    /**
     * Creates default appearance.
     * 创建默认外观。
     *
     * @return default appearance | 默认外观
     */
    public static SignatureAppearance defaultAppearance() {
        return new SignatureAppearance();
    }

    /**
     * Creates image-only appearance.
     * 创建仅图像外观。
     *
     * @param imagePath image file path | 图像文件路径
     * @return image appearance | 图像外观
     */
    public static SignatureAppearance imageOnly(Path imagePath) {
        return new SignatureAppearance()
            .image(imagePath)
            .showSignerName(false)
            .showDate(false);
    }

    /**
     * Creates text-only appearance.
     * 创建仅文本外观。
     *
     * @return text appearance | 文本外观
     */
    public static SignatureAppearance textOnly() {
        return new SignatureAppearance();
    }
}
