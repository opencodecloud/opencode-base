package cloud.opencode.base.id.nanoid;

import cloud.opencode.base.id.IdGenerator;

import java.security.SecureRandom;
import java.util.Random;

/**
 * NanoID Generator
 * NanoID生成器
 *
 * <p>Generates compact, URL-friendly unique string identifiers.
 * Default length is 21 characters with 126 bits of entropy.</p>
 * <p>生成紧凑、URL友好的唯一字符串标识符。
 * 默认长度为21个字符，具有126位熵。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>URL-safe by default - 默认URL安全</li>
 *   <li>Configurable length - 可配置长度</li>
 *   <li>Custom alphabet support - 自定义字母表支持</li>
 *   <li>Cryptographically secure - 加密安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default 21 characters
 * NanoIdGenerator gen = NanoIdGenerator.create();
 * String id = gen.generate();
 * // -> "V1StGXR8_Z5jdHi6B-myT"
 *
 * // Custom length
 * String shortId = NanoIdGenerator.randomNanoId(10);
 *
 * // Custom alphabet
 * String numericId = NanoIdGenerator.randomNanoId(8, "0123456789");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>~1.5M ops/sec single thread - 单线程约1.5M次/秒</li>
 *   <li>~10M ops/sec with 8 threads - 8线程约10M次/秒</li>
 * </ul>
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
public final class NanoIdGenerator implements IdGenerator<String> {

    /**
     * Default ID length
     * 默认ID长度
     */
    public static final int DEFAULT_SIZE = 21;

    /**
     * Default alphabet
     * 默认字母表
     */
    public static final String DEFAULT_ALPHABET = Alphabet.DEFAULT.getChars();

    private static final SecureRandom DEFAULT_RANDOM = new SecureRandom();
    private static final NanoIdGenerator DEFAULT_INSTANCE =
            new NanoIdGenerator(DEFAULT_SIZE, DEFAULT_ALPHABET, DEFAULT_RANDOM);

    private final int size;
    private final String alphabet;
    private final int alphabetSize;
    private final int mask;
    private final Random random;

    /**
     * Creates a generator with configuration
     * 使用配置创建生成器
     *
     * @param size     the ID length | ID长度
     * @param alphabet the character set | 字符集
     * @param random   the random source | 随机源
     * @throws IllegalArgumentException if size <= 0 or alphabet is invalid
     */
    NanoIdGenerator(int size, String alphabet, Random random) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (alphabet == null || alphabet.length() < 2) {
            throw new IllegalArgumentException("Alphabet must have at least 2 characters");
        }
        if (alphabet.length() > 256) {
            throw new IllegalArgumentException("Alphabet must not exceed 256 characters");
        }
        this.size = size;
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.length();
        this.mask = (2 << (int) Math.floor(Math.log(alphabetSize - 1) / Math.log(2))) - 1;
        this.random = random;
    }

    /**
     * Creates a default generator
     * 创建默认生成器
     *
     * @return generator | 生成器
     */
    public static NanoIdGenerator create() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Creates a generator with specific size
     * 使用指定长度创建生成器
     *
     * @param size the ID length | ID长度
     * @return generator | 生成器
     */
    public static NanoIdGenerator create(int size) {
        return new NanoIdGenerator(size, DEFAULT_ALPHABET, DEFAULT_RANDOM);
    }

    /**
     * Creates a builder for customized generator
     * 创建用于自定义生成器的构建器
     *
     * @return builder | 构建器
     */
    public static NanoIdBuilder builder() {
        return new NanoIdBuilder();
    }

    /**
     * Generates a random NanoID with default settings
     * 使用默认设置生成随机NanoID
     *
     * @return NanoID string | NanoID字符串
     */
    public static String randomNanoId() {
        return DEFAULT_INSTANCE.generate();
    }

    /**
     * Generates a random NanoID with specific size
     * 使用指定长度生成随机NanoID
     *
     * @param size the ID length | ID长度
     * @return NanoID string | NanoID字符串
     */
    public static String randomNanoId(int size) {
        return generateInternal(size, DEFAULT_ALPHABET, DEFAULT_RANDOM);
    }

    /**
     * Generates a random NanoID with specific size and alphabet
     * 使用指定长度和字母表生成随机NanoID
     *
     * @param size     the ID length | ID长度
     * @param alphabet the character set | 字符集
     * @return NanoID string | NanoID字符串
     */
    public static String randomNanoId(int size, String alphabet) {
        return generateInternal(size, alphabet, DEFAULT_RANDOM);
    }

    @Override
    public String generate() {
        return generateInternal(size, alphabet, random);
    }

    /**
     * Gets the ID length
     * 获取ID长度
     *
     * @return length | 长度
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the alphabet
     * 获取字母表
     *
     * @return alphabet | 字母表
     */
    public String getAlphabet() {
        return alphabet;
    }

    /**
     * Calculates collision probability
     * 计算碰撞概率
     *
     * @param size       the ID length | ID长度
     * @param alphabet   the character set | 字符集
     * @param countToGen the number of IDs to generate | 要生成的ID数量
     * @return collision probability | 碰撞概率
     */
    public static double collisionProbability(int size, String alphabet, long countToGen) {
        int alphabetSize = alphabet.length();
        double possibleIds = Math.pow(alphabetSize, size);
        // Birthday paradox approximation
        return 1.0 - Math.exp(-0.5 * countToGen * (countToGen - 1) / possibleIds);
    }

    /**
     * Analyzes collision risk and provides recommendations
     * 分析碰撞风险并提供建议
     *
     * @param countToGen the expected number of IDs to generate | 预期生成的ID数量
     * @param alphabet   the character set | 字符集
     * @return collision analysis result | 碰撞分析结果
     */
    public static CollisionAnalysis analyzeCollision(long countToGen, String alphabet) {
        return analyzeCollision(countToGen, alphabet, 1e-15);
    }

    /**
     * Analyzes collision risk with custom target probability
     * 使用自定义目标概率分析碰撞风险
     *
     * @param countToGen        the expected number of IDs to generate | 预期生成的ID数量
     * @param alphabet          the character set | 字符集
     * @param targetProbability the target collision probability | 目标碰撞概率
     * @return collision analysis result | 碰撞分析结果
     */
    public static CollisionAnalysis analyzeCollision(long countToGen, String alphabet, double targetProbability) {
        int alphabetSize = alphabet.length();

        // Calculate current probability with default size
        double currentProbability = collisionProbability(DEFAULT_SIZE, alphabet, countToGen);

        // Calculate recommended size to achieve target probability
        // Using the birthday paradox formula: p ≈ n²/(2*k^s)
        // Solving for s: s ≈ log(n²/(2p)) / log(k)
        int recommendedSize = DEFAULT_SIZE;
        if (countToGen > 0 && targetProbability > 0) {
            double requiredSpace = (double) countToGen * countToGen / (2 * targetProbability);
            recommendedSize = (int) Math.ceil(Math.log(requiredSpace) / Math.log(alphabetSize));
            recommendedSize = Math.max(recommendedSize, 1);
        }

        // Calculate probability with recommended size
        double recommendedProbability = collisionProbability(recommendedSize, alphabet, countToGen);

        // Human-readable format
        String humanReadable = formatProbability(currentProbability);

        // Calculate entropy bits
        double entropyBits = DEFAULT_SIZE * (Math.log(alphabetSize) / Math.log(2));

        return new CollisionAnalysis(
                currentProbability,
                recommendedSize,
                recommendedProbability,
                humanReadable,
                entropyBits,
                countToGen
        );
    }

    /**
     * Calculates the recommended ID size for a given number of IDs
     * 计算给定ID数量的推荐ID长度
     *
     * @param countToGen        the expected number of IDs | 预期的ID数量
     * @param alphabet          the character set | 字符集
     * @param targetProbability the target collision probability | 目标碰撞概率
     * @return recommended size | 推荐长度
     */
    public static int recommendedSize(long countToGen, String alphabet, double targetProbability) {
        int alphabetSize = alphabet.length();
        if (countToGen <= 0 || targetProbability <= 0) {
            return DEFAULT_SIZE;
        }
        double requiredSpace = (double) countToGen * countToGen / (2 * targetProbability);
        int size = (int) Math.ceil(Math.log(requiredSpace) / Math.log(alphabetSize));
        return Math.max(size, 1);
    }

    private static String formatProbability(double probability) {
        if (probability <= 0) {
            return "0";
        }
        if (probability >= 1) {
            return "~1";
        }
        // Express as "1 in X"
        double inverse = 1.0 / probability;
        if (inverse >= 1e15) {
            int exp = (int) Math.floor(Math.log10(inverse));
            return String.format("1 in 10^%d", exp);
        } else if (inverse >= 1e9) {
            return String.format("1 in %.0f billion", inverse / 1e9);
        } else if (inverse >= 1e6) {
            return String.format("1 in %.0f million", inverse / 1e6);
        } else if (inverse >= 1e3) {
            return String.format("1 in %.0f thousand", inverse / 1e3);
        } else {
            return String.format("1 in %.0f", inverse);
        }
    }

    /**
     * Collision analysis result
     * 碰撞分析结果
     *
     * @param probability            the collision probability with default size | 默认长度的碰撞概率
     * @param recommendedSize        the recommended size for target probability | 达到目标概率的推荐长度
     * @param recommendedProbability the probability with recommended size | 推荐长度的碰撞概率
     * @param humanReadable          human-readable probability description | 人类可读的概率描述
     * @param entropyBits            entropy bits with default size | 默认长度的熵位数
     * @param countToGen             the number of IDs analyzed | 分析的ID数量
     */
    public record CollisionAnalysis(
            double probability,
            int recommendedSize,
            double recommendedProbability,
            String humanReadable,
            double entropyBits,
            long countToGen
    ) {
        /**
         * Checks if the collision risk is acceptable (probability < 1e-15)
         * 检查碰撞风险是否可接受（概率 < 1e-15）
         *
         * @return true if acceptable | 如果可接受返回true
         */
        public boolean isAcceptable() {
            return probability < 1e-15;
        }

        /**
         * Checks if the collision risk is low (probability < 1e-9)
         * 检查碰撞风险是否低（概率 < 1e-9）
         *
         * @return true if low risk | 如果低风险返回true
         */
        public boolean isLowRisk() {
            return probability < 1e-9;
        }

        @Override
        public String toString() {
            return String.format(
                    "CollisionAnalysis{count=%d, probability=%s, recommendedSize=%d, entropyBits=%.1f}",
                    countToGen, humanReadable, recommendedSize, entropyBits
            );
        }
    }

    @Override
    public String getType() {
        return "NanoID";
    }

    private static String generateInternal(int size, String alphabet, Random random) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (alphabet == null || alphabet.length() < 2) {
            throw new IllegalArgumentException("Alphabet must have at least 2 characters");
        }

        int alphabetSize = alphabet.length();
        int mask = (2 << (int) Math.floor(Math.log(alphabetSize - 1) / Math.log(2))) - 1;
        int step = (int) Math.ceil(1.6 * mask * size / alphabetSize);

        StringBuilder sb = new StringBuilder(size);
        byte[] bytes = new byte[step];

        while (sb.length() < size) {
            random.nextBytes(bytes);
            for (int i = 0; i < step && sb.length() < size; i++) {
                int index = bytes[i] & mask;
                if (index < alphabetSize) {
                    sb.append(alphabet.charAt(index));
                }
            }
        }

        return sb.toString();
    }
}
