/**
 * OpenCode Base PDF Module
 * OpenCode PDF 模块
 *
 * <p>Provides zero-dependency PDF processing capabilities including
 * document creation, merge, split, form filling and digital signatures.</p>
 * <p>提供零依赖的 PDF 处理功能，包括文档创建、合并、拆分、表单填充和数字签名。</p>
 *
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
module cloud.opencode.base.pdf {

    // Required modules
    requires java.desktop;  // For java.awt.Color

    // Export all public packages
    exports cloud.opencode.base.pdf;
    exports cloud.opencode.base.pdf.content;
    exports cloud.opencode.base.pdf.document;
    exports cloud.opencode.base.pdf.exception;
    exports cloud.opencode.base.pdf.font;
    exports cloud.opencode.base.pdf.form;
    exports cloud.opencode.base.pdf.operation;
    exports cloud.opencode.base.pdf.signature;

    // Internal packages - not exported
    // cloud.opencode.base.pdf.internal
    // cloud.opencode.base.pdf.internal.parser
    // cloud.opencode.base.pdf.internal.object
    // cloud.opencode.base.pdf.internal.encoding
    // cloud.opencode.base.pdf.internal.cmap
}
