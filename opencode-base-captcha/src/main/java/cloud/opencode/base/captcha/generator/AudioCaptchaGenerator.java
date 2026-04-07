package cloud.opencode.base.captcha.generator;

import cloud.opencode.base.captcha.Captcha;
import cloud.opencode.base.captcha.CaptchaConfig;
import cloud.opencode.base.captcha.CaptchaType;
import cloud.opencode.base.captcha.support.CaptchaChars;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Audio Captcha Generator - Generates audio CAPTCHA challenges as WAV files
 * 音频验证码生成器 - 生成 WAV 格式的音频验证码挑战
 *
 * <p>This generator creates audio CAPTCHAs by synthesizing unique tone sequences
 * for each character in the verification code. The generated audio includes
 * background noise and interference tones to prevent automated recognition.</p>
 * <p>此生成器通过为验证码中的每个字符合成独特的音调序列来创建音频验证码。
 * 生成的音频包含背景噪声和干扰音调以防止自动识别。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>16kHz 16-bit mono PCM WAV output - 16kHz 16位单声道 PCM WAV 输出</li>
 *   <li>Unique tone sequences per character - 每个字符独特的音调序列</li>
 *   <li>Background white noise (~15dB SNR) - 背景白噪声（约15dB信噪比）</li>
 *   <li>Random interference tones - 随机干扰音调</li>
 *   <li>Variable speed and frequency offsets - 可变速度和频率偏移</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AudioCaptchaGenerator generator = new AudioCaptchaGenerator();
 * Captcha captcha = generator.generate(CaptchaConfig.builder()
 *     .length(4)
 *     .audioSpeedVariation(0.2f)
 *     .build());
 * byte[] wavData = captcha.imageData();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless, uses local SecureRandom) - 线程安全: 是（无状态，使用局部 SecureRandom）</li>
 *   <li>Null-safe: No (config must not be null) - 空值安全: 否（config 不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.3
 */
public final class AudioCaptchaGenerator extends AbstractCaptchaGenerator implements CaptchaGenerator {

    /** Sample rate in Hz | 采样率（Hz） */
    private static final float SAMPLE_RATE = 16000.0f;

    /** Bits per sample | 每样本位数 */
    private static final int SAMPLE_SIZE = 16;

    /** Number of audio channels (mono) | 音频通道数（单声道） */
    private static final int CHANNELS = 1;

    /** Duration of each tone in samples (~150ms) | 每个音调的持续采样数（约150ms） */
    private static final int TONE_SAMPLES = (int) (SAMPLE_RATE * 0.15);

    /** Silence gap between tones in samples (~50ms) | 音调之间静音间隔的采样数（约50ms） */
    private static final int TONE_GAP_SAMPLES = (int) (SAMPLE_RATE * 0.05);

    /** Silence gap between characters in samples (~300ms base) | 字符之间静音间隔的采样数（约300ms基础） */
    private static final int CHAR_GAP_BASE_SAMPLES = (int) (SAMPLE_RATE * 0.3);

    /** WAV header size in bytes | WAV 头大小（字节） */
    private static final int WAV_HEADER_SIZE = 44;

    /**
     * Tone frequency patterns for characters 0-9, a-z.
     * Each character maps to a unique sequence of 2-3 frequencies (Hz).
     * 字符 0-9, a-z 的音调频率模式。每个字符映射到 2-3 个独特的频率序列（Hz）。
     */
    private static final int[][] CHAR_TONES = new int[36][];

    static {
        // Digits 0-9
        CHAR_TONES[0]  = new int[]{400, 300};       // '0'
        CHAR_TONES[1]  = new int[]{500};             // '1'
        CHAR_TONES[2]  = new int[]{350, 550};        // '2'
        CHAR_TONES[3]  = new int[]{600, 400};        // '3'
        CHAR_TONES[4]  = new int[]{450, 650};        // '4'
        CHAR_TONES[5]  = new int[]{700, 500};        // '5'
        CHAR_TONES[6]  = new int[]{550, 350, 450};   // '6'
        CHAR_TONES[7]  = new int[]{650, 800};        // '7'
        CHAR_TONES[8]  = new int[]{750, 600};        // '8'
        CHAR_TONES[9]  = new int[]{800, 500, 650};   // '9'
        // Letters a-z
        CHAR_TONES[10] = new int[]{600, 400, 500};   // 'a'
        CHAR_TONES[11] = new int[]{700, 500};        // 'b'
        CHAR_TONES[12] = new int[]{500, 800};        // 'c'
        CHAR_TONES[13] = new int[]{900, 600};        // 'd'
        CHAR_TONES[14] = new int[]{650, 450, 550};   // 'e'
        CHAR_TONES[15] = new int[]{750, 550};        // 'f'
        CHAR_TONES[16] = new int[]{850, 700};        // 'g'
        CHAR_TONES[17] = new int[]{500, 900};        // 'h'
        CHAR_TONES[18] = new int[]{1000, 700};       // 'i'
        CHAR_TONES[19] = new int[]{600, 1000, 800};  // 'j'
        CHAR_TONES[20] = new int[]{1100, 800};       // 'k'
        CHAR_TONES[21] = new int[]{700, 1100};       // 'l'
        CHAR_TONES[22] = new int[]{900, 600, 750};   // 'm'
        CHAR_TONES[23] = new int[]{1050, 850};       // 'n'
        CHAR_TONES[24] = new int[]{800, 1050};       // 'o'
        CHAR_TONES[25] = new int[]{950, 700, 850};   // 'p'
        CHAR_TONES[26] = new int[]{1100, 900};       // 'q'
        CHAR_TONES[27] = new int[]{750, 1000};       // 'r'
        CHAR_TONES[28] = new int[]{850, 650};        // 's'
        CHAR_TONES[29] = new int[]{1000, 750, 900};  // 't'
        CHAR_TONES[30] = new int[]{550, 850};        // 'u'
        CHAR_TONES[31] = new int[]{950, 1150};       // 'v'
        CHAR_TONES[32] = new int[]{1050, 800, 950};  // 'w'
        CHAR_TONES[33] = new int[]{1150, 900};       // 'x'
        CHAR_TONES[34] = new int[]{700, 1050, 850};  // 'y'
        CHAR_TONES[35] = new int[]{1200, 950};       // 'z'
    }

    @Override
    public CaptchaType getType() {
        return CaptchaType.AUDIO;
    }

    @Override
    public Captcha generate(CaptchaConfig config) {
        String code = CaptchaChars.generate(CaptchaType.ALPHANUMERIC, config.getLength());
        byte[] audioData = generateAudio(code, config);

        String id = generateId();
        Map<String, Object> metadata = new HashMap<>(createMetadata(config));
        metadata.put("format", "wav");

        Instant now = Instant.now();
        return new Captcha(id, CaptchaType.AUDIO, audioData, code, metadata,
                now, now.plus(config.getExpireTime()));
    }

    /**
     * Generates WAV audio data for the given code.
     * 为给定的验证码生成 WAV 音频数据。
     *
     * @param code   the CAPTCHA code | 验证码文本
     * @param config the configuration | 配置
     * @return the WAV audio bytes | WAV 音频字节
     */
    private byte[] generateAudio(String code, CaptchaConfig config) {
        SecureRandom random = new SecureRandom();

        // Calculate total sample count (uses a separate random to avoid consuming state)
        int totalSamples = estimateTotalSamples(code, config);
        // Use int[] for accumulation to avoid short truncation during overlapping additions
        int[] samples = new int[totalSamples];
        int offset = 0;

        // Generate tones for each character
        for (int i = 0; i < code.length(); i++) {
            char ch = Character.toLowerCase(code.charAt(i));
            int charIndex = charToIndex(ch);
            int[] tones = CHAR_TONES[charIndex];

            // Speed variation per character
            float speedFactor = 1.0f + (random.nextFloat() * 2 - 1) * config.getAudioSpeedVariation();

            for (int t = 0; t < tones.length; t++) {
                // Apply +-10% frequency offset
                double freqOffset = 1.0 + (random.nextDouble() * 0.2 - 0.1);
                double frequency = tones[t] * freqOffset;

                int toneDuration = (int) (TONE_SAMPLES * speedFactor);
                if (toneDuration < 1) {
                    toneDuration = 1;
                }

                // Generate sine wave for this tone
                for (int s = 0; s < toneDuration && offset + s < totalSamples; s++) {
                    double time = s / SAMPLE_RATE;
                    double value = Math.sin(2.0 * Math.PI * frequency * time);
                    // Apply amplitude envelope (fade in/out 10%)
                    double envelope = 1.0;
                    int fadeLen = toneDuration / 10;
                    if (fadeLen > 0) {
                        if (s < fadeLen) {
                            envelope = (double) s / fadeLen;
                        } else if (s > toneDuration - fadeLen) {
                            envelope = (double) (toneDuration - s) / fadeLen;
                        }
                    }
                    samples[offset + s] += (int) (value * envelope * 12000);
                }
                offset += toneDuration;

                // Add silence gap between tones (not after last tone of a character)
                if (t < tones.length - 1) {
                    offset += TONE_GAP_SAMPLES;
                }
            }

            // Add silence gap between characters
            if (i < code.length() - 1) {
                int charGap = CHAR_GAP_BASE_SAMPLES + random.nextInt((int) (SAMPLE_RATE * 0.2));
                offset += charGap;
            }
        }

        // Add background white noise (~15dB SNR => noise amplitude ~18% of signal)
        for (int s = 0; s < totalSamples; s++) {
            samples[s] += (int) ((random.nextDouble() * 2 - 1) * 2000);
        }

        // Add 2-4 random interference tones
        int interferenceCount = 2 + random.nextInt(3);
        for (int n = 0; n < interferenceCount; n++) {
            double freq = 200 + random.nextDouble() * 1200;
            int startSample = random.nextInt(Math.max(1, totalSamples - (int) (SAMPLE_RATE * 0.1)));
            int duration = (int) (SAMPLE_RATE * (0.05 + random.nextDouble() * 0.1));
            for (int s = 0; s < duration && startSample + s < totalSamples; s++) {
                double time = s / SAMPLE_RATE;
                samples[startSample + s] += (int) (Math.sin(2.0 * Math.PI * freq * time) * 3000);
            }
        }

        // Clamp samples to short range and convert to short[]
        short[] clampedSamples = new short[totalSamples];
        for (int s = 0; s < totalSamples; s++) {
            int val = samples[s];
            if (val > Short.MAX_VALUE) {
                clampedSamples[s] = Short.MAX_VALUE;
            } else if (val < Short.MIN_VALUE) {
                clampedSamples[s] = Short.MIN_VALUE;
            } else {
                clampedSamples[s] = (short) val;
            }
        }

        return encodeWav(clampedSamples);
    }

    /**
     * Estimates the total number of samples needed for the audio using worst-case bounds.
     * Does not consume any random state, ensuring the actual generation stays in bounds.
     * 使用最坏情况上界估算音频所需的总采样数。不消耗任何随机状态，确保实际生成不越界。
     *
     * @param code   the CAPTCHA code | 验证码文本
     * @param config the configuration | 配置
     * @return the estimated sample count (upper bound) | 估算采样数（上界）
     */
    private int estimateTotalSamples(String code, CaptchaConfig config) {
        // Use maximum speed factor for upper-bound estimation
        float maxSpeedFactor = 1.0f + config.getAudioSpeedVariation();

        int total = 0;
        for (int i = 0; i < code.length(); i++) {
            char ch = Character.toLowerCase(code.charAt(i));
            int charIndex = charToIndex(ch);
            int[] tones = CHAR_TONES[charIndex];

            for (int t = 0; t < tones.length; t++) {
                total += (int) (TONE_SAMPLES * maxSpeedFactor);
                if (t < tones.length - 1) {
                    total += TONE_GAP_SAMPLES;
                }
            }

            if (i < code.length() - 1) {
                total += CHAR_GAP_BASE_SAMPLES + (int) (SAMPLE_RATE * 0.2);
            }
        }
        // Extra padding for noise and interference
        total += (int) (SAMPLE_RATE * 0.5);
        return total;
    }

    /**
     * Maps a character to its tone index.
     * 将字符映射到其音调索引。
     *
     * @param ch the character (lowercase) | 字符（小写）
     * @return the index into CHAR_TONES | CHAR_TONES 中的索引
     */
    private static int charToIndex(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'z') {
            return ch - 'a' + 10;
        }
        return 0;
    }

    /**
     * Encodes PCM samples into WAV format bytes.
     * 将 PCM 采样编码为 WAV 格式字节。
     *
     * @param samples the PCM samples | PCM 采样
     * @return the WAV bytes | WAV 字节
     */
    private byte[] encodeWav(short[] samples) {
        int dataSize = samples.length * 2;
        int fileSize = WAV_HEADER_SIZE + dataSize;
        ByteArrayOutputStream out = new ByteArrayOutputStream(fileSize);

        // RIFF header (12 bytes)
        writeBytes(out, "RIFF");
        writeLittleEndianInt(out, fileSize - 8);
        writeBytes(out, "WAVE");

        // fmt chunk (24 bytes)
        writeBytes(out, "fmt ");
        writeLittleEndianInt(out, 16);                           // chunk size
        writeLittleEndianShort(out, (short) 1);                  // PCM format
        writeLittleEndianShort(out, (short) CHANNELS);           // channels
        writeLittleEndianInt(out, (int) SAMPLE_RATE);            // sample rate
        int byteRate = (int) SAMPLE_RATE * CHANNELS * (SAMPLE_SIZE / 8);
        writeLittleEndianInt(out, byteRate);                     // byte rate
        short blockAlign = (short) (CHANNELS * (SAMPLE_SIZE / 8));
        writeLittleEndianShort(out, blockAlign);                 // block align
        writeLittleEndianShort(out, (short) SAMPLE_SIZE);        // bits per sample

        // data chunk
        writeBytes(out, "data");
        writeLittleEndianInt(out, dataSize);

        // PCM samples (little-endian 16-bit)
        for (short sample : samples) {
            out.write(sample & 0xFF);
            out.write((sample >> 8) & 0xFF);
        }

        return out.toByteArray();
    }

    /**
     * Writes ASCII string bytes to the output stream.
     * 将 ASCII 字符串字节写入输出流。
     *
     * @param out the output stream | 输出流
     * @param s   the string | 字符串
     */
    private static void writeBytes(ByteArrayOutputStream out, String s) {
        for (int i = 0; i < s.length(); i++) {
            out.write(s.charAt(i));
        }
    }

    /**
     * Writes a 32-bit integer in little-endian format.
     * 以小端格式写入 32 位整数。
     *
     * @param out the output stream | 输出流
     * @param val the integer value | 整数值
     */
    private static void writeLittleEndianInt(ByteArrayOutputStream out, int val) {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
        out.write((val >> 16) & 0xFF);
        out.write((val >> 24) & 0xFF);
    }

    /**
     * Writes a 16-bit integer in little-endian format.
     * 以小端格式写入 16 位整数。
     *
     * @param out the output stream | 输出流
     * @param val the short value | 短整数值
     */
    private static void writeLittleEndianShort(ByteArrayOutputStream out, short val) {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
    }
}
