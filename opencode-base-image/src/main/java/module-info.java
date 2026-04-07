/**
 * OpenCode Base Image Module
 * OpenCode 图片处理模块
 *
 * <p>Provides image processing capabilities including resize, crop, rotate,
 * watermark, compress, and format conversion.</p>
 * <p>提供图片处理能力，包括缩放、裁剪、旋转、水印、压缩和格式转换。</p>
 *
 * <p><strong>Supported Formats | 支持的格式:</strong></p>
 * <ul>
 *   <li>JPEG, PNG, GIF, BMP - Built-in (JDK ImageIO)</li>
 *   <li>WebP - Optional (requires com.twelvemonkeys.imageio:imageio-webp)</li>
 * </ul>
 *
 * @since JDK 25, opencode-base-image V1.0.0
 */
module cloud.opencode.base.image {

    // Required modules
    requires transitive cloud.opencode.base.core;
    requires java.desktop;  // For AWT/ImageIO

    // Export public API packages
    exports cloud.opencode.base.image;
    exports cloud.opencode.base.image.exception;
    exports cloud.opencode.base.image.watermark;
    exports cloud.opencode.base.image.thumbnail;
    exports cloud.opencode.base.image.validation;
    exports cloud.opencode.base.image.security;
    exports cloud.opencode.base.image.exif;
    exports cloud.opencode.base.image.compare;
    exports cloud.opencode.base.image.filter;
    exports cloud.opencode.base.image.responsive;
    exports cloud.opencode.base.image.threshold;
    exports cloud.opencode.base.image.color;
    exports cloud.opencode.base.image.analysis;
    exports cloud.opencode.base.image.morphology;
    exports cloud.opencode.base.image.histogram;
    exports cloud.opencode.base.image.edge;
    exports cloud.opencode.base.image.draw;
    exports cloud.opencode.base.image.transform;
    exports cloud.opencode.base.image.feature;

    // Internal packages - not exported
    // cloud.opencode.base.image.internal
    // cloud.opencode.base.image.kernel
}
