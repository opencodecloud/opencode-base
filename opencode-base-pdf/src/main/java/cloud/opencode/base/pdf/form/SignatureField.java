package cloud.opencode.base.pdf.form;

/**
 * Signature Form Field
 * 签名表单字段
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Signature status check - 签名状态检查</li>
 *   <li>Signer name, date, reason, and location access - 签名者名称、日期、原因和位置访问</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SignatureField sig = (SignatureField) form.getField("signature1");
 * if (sig.isSigned()) {
 *     String signer = sig.getSignerName();
 *     String date = sig.getSignDate();
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation — getters may return null if not signed - 空值安全: 取决于实现 — 未签名时获取方法可能返回空</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public non-sealed interface SignatureField extends FormField {

    @Override
    default FieldType getType() {
        return FieldType.SIGNATURE;
    }

    /**
     * Checks if signed
     * 检查是否已签名
     *
     * @return true if signed | 如果已签名返回 true
     */
    boolean isSigned();

    /**
     * Gets signer name
     * 获取签名者名称
     *
     * @return signer name, or null if not signed | 签名者名称，如果未签名则返回 null
     */
    String getSignerName();

    /**
     * Gets sign date as string
     * 获取签名日期字符串
     *
     * @return sign date, or null if not signed | 签名日期，如果未签名则返回 null
     */
    String getSignDate();

    /**
     * Gets signing reason
     * 获取签名原因
     *
     * @return reason, or null | 原因，或 null
     */
    String getReason();

    /**
     * Gets signing location
     * 获取签名位置
     *
     * @return location, or null | 位置，或 null
     */
    String getLocation();
}
