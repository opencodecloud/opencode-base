package cloud.opencode.base.id.simple;

import cloud.opencode.base.id.IdGenerator;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Timestamp-based ID Generator
 * 基于时间戳的ID生成器
 *
 * <p>Generates IDs in format: yyyyMMddHHmmssSSS + random digits.
 * Suitable for order numbers, transaction IDs, etc.</p>
 * <p>生成格式为：yyyyMMddHHmmssSSS + 随机数字的ID。
 * 适用于订单号、交易ID等场景。</p>
 *
 * <p><strong>Format | 格式:</strong></p>
 * <pre>
 * 20240101123456789001234
 * |       |    |   | |
 * | date  |time|ms |random
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time-ordered - 时间有序</li>
 *   <li>Human-readable timestamp - 人类可读时间戳</li>
 *   <li>Configurable random length - 可配置随机数长度</li>
 *   <li>Optional prefix support - 可选前缀支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimestampIdGenerator gen = TimestampIdGenerator.create();
 * String id = gen.generate();
 * // -> "202401011234567890001234"
 *
 * // With prefix
 * String orderId = gen.generate("ORD");
 * // -> "ORD202401011234567890001234"
 *
 * // Custom random length
 * TimestampIdGenerator gen2 = TimestampIdGenerator.create(6);
 * String shortId = gen2.generate();
 * // -> "20240101123456789123456"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class TimestampIdGenerator implements IdGenerator<String> {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final int DEFAULT_RANDOM_LENGTH = 6;

    private final int randomLength;
    private final Random random;
    private int sequence = 0;
    private String lastTimestamp = "";

    /**
     * Creates a generator with random length
     * 使用随机数长度创建生成器
     *
     * @param randomLength the random digits length | 随机数字长度
     */
    private TimestampIdGenerator(int randomLength) {
        this.randomLength = randomLength;
        this.random = new SecureRandom();
    }

    /**
     * Creates a generator with default random length (6)
     * 创建使用默认随机长度（6）的生成器
     *
     * @return generator | 生成器
     */
    public static TimestampIdGenerator create() {
        return new TimestampIdGenerator(DEFAULT_RANDOM_LENGTH);
    }

    /**
     * Creates a generator with specific random length
     * 使用指定随机长度创建生成器
     *
     * @param randomLength the random digits length | 随机数字长度
     * @return generator | 生成器
     */
    public static TimestampIdGenerator create(int randomLength) {
        if (randomLength <= 0 || randomLength > 20) {
            throw new IllegalArgumentException("Random length must be between 1 and 20");
        }
        return new TimestampIdGenerator(randomLength);
    }

    @Override
    public String generate() {
        return generateId();
    }

    /**
     * Generates an ID with prefix
     * 生成带前缀的ID
     *
     * @param prefix the prefix | 前缀
     * @return ID string | ID字符串
     */
    public String generate(String prefix) {
        return prefix + generate();
    }

    @Override
    public String getType() {
        return "Timestamp";
    }

    private synchronized String generateId() {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        if (!timestamp.equals(lastTimestamp)) {
            lastTimestamp = timestamp;
            sequence = 0;
        }
        int seq = sequence++;
        return timestamp + formatSequence(seq);
    }

    private String generateRandom() {
        StringBuilder sb = new StringBuilder(randomLength);
        for (int i = 0; i < randomLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String formatSequence(int seq) {
        String seqStr = String.valueOf(seq);
        if (seqStr.length() >= randomLength) {
            // If sequence exceeds the digit width, return the full sequence string
            // to prevent collision from truncation (e.g., truncating "1000000" and "100000" to same value)
            return seqStr;
        }
        // Zero-pad to fixed width to prevent collisions (e.g., seq=1+"28299" vs seq=12+"8299")
        return String.format("%0" + randomLength + "d", seq);
    }
}
