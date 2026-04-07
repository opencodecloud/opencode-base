package cloud.opencode.base.crypto.streaming;

import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.exception.OpenKeyException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Streaming Authenticated Encryption with Associated Data (AEAD) for large data
 * 用于大数据的流式认证加密（AEAD）
 *
 * <p>Encrypts data in fixed-size segments, each independently authenticated.
 * Supports AES-256-GCM and ChaCha20-Poly1305. Each segment has its own nonce
 * derived from a base nonce and segment counter, and includes segment metadata
 * in AAD to prevent reordering and truncation attacks.</p>
 * <p>以固定大小的段加密数据，每个段独立认证。支持 AES-256-GCM 和 ChaCha20-Poly1305。
 * 每个段有自己的 nonce（从基础 nonce 和段计数器派生），并在 AAD 中包含段元数据以防止
 * 重排和截断攻击。</p>
 *
 * <p><strong>Wire Format | 数据格式:</strong></p>
 * <pre>
 * [4-byte header: segment plaintext size (big-endian)]
 * [base nonce (12 bytes)]
 * [segment 0: nonce(12) + ciphertext + tag(16)]
 * [segment 1: nonce(12) + ciphertext + tag(16)]
 * ...
 * [final segment: nonce(12) + ciphertext(shorter or equal) + tag(16)]
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AES-256-GCM streaming encryption - AES-256-GCM 流式加密</li>
 *   <li>ChaCha20-Poly1305 streaming encryption - ChaCha20-Poly1305 流式加密</li>
 *   <li>Per-segment authentication with anti-reorder AAD - 每段认证，AAD 防重排</li>
 *   <li>Anti-truncation via final-segment marker - 通过最终段标记防截断</li>
 *   <li>File encryption/decryption convenience methods - 文件加解密便捷方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // AES-256-GCM streaming encryption
 * try (StreamingAead aead = StreamingAead.aesGcm(key256)
 *         .setSegmentSize(1024 * 1024)
 *         .setAad(associatedData)) {
 *     aead.encryptFile(Path.of("input.bin"), Path.of("output.enc"));
 *     aead.decryptFile(Path.of("output.enc"), Path.of("restored.bin"));
 * }
 *
 * // ChaCha20-Poly1305
 * try (StreamingAead chacha = StreamingAead.chaCha20(key256)) {
 *     chacha.encrypt(inputStream, outputStream);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非并发设计）</li>
 *   <li>Null-safe: No (null key throws exception) - 空值安全: 否（空密钥抛出异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.3
 */
public final class StreamingAead implements AutoCloseable {

    private static final int NONCE_LENGTH = 12;
    private static final int NONCE_PREFIX_LENGTH = 8;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int TAG_LENGTH_BYTES = 16;
    private static final int DEFAULT_SEGMENT_SIZE = 1048576; // 1 MB
    private static final int MIN_SEGMENT_SIZE = 256;
    private static final int MAX_SEGMENT_SIZE = 64 * 1024 * 1024; // 64 MB
    private static final SecureRandom RANDOM = new SecureRandom();

    private final byte[] key;
    private final Algorithm algorithm;
    private int segmentSize = DEFAULT_SEGMENT_SIZE;
    private byte[] aad;
    private volatile boolean closed;

    /**
     * Supported AEAD algorithms.
     * 支持的 AEAD 算法。
     */
    private enum Algorithm {
        AES_GCM("AES/GCM/NoPadding", "AES"),
        CHACHA20_POLY1305("ChaCha20-Poly1305", "ChaCha20");

        final String transformation;
        final String keyAlgorithm;

        Algorithm(String transformation, String keyAlgorithm) {
            this.transformation = transformation;
            this.keyAlgorithm = keyAlgorithm;
        }
    }

    private StreamingAead(byte[] key, Algorithm algorithm) {
        this.key = key.clone();
        this.algorithm = algorithm;
    }

    /**
     * Create a StreamingAead instance using AES-GCM.
     * 创建使用 AES-GCM 的 StreamingAead 实例。
     *
     * @param key AES key (16, 24, or 32 bytes) | AES 密钥（16、24 或 32 字节）
     * @return new StreamingAead instance | 新的 StreamingAead 实例
     * @throws OpenKeyException if the key is null or has invalid length | 当密钥为空或长度无效时抛出
     */
    public static StreamingAead aesGcm(byte[] key) {
        validateAesKey(key);
        return new StreamingAead(key, Algorithm.AES_GCM);
    }

    /**
     * Create a StreamingAead instance using ChaCha20-Poly1305.
     * 创建使用 ChaCha20-Poly1305 的 StreamingAead 实例。
     *
     * @param key 256-bit (32-byte) ChaCha20 key | 256 位（32 字节）ChaCha20 密钥
     * @return new StreamingAead instance | 新的 StreamingAead 实例
     * @throws OpenKeyException if the key is null or not 32 bytes | 当密钥为空或长度不是 32 字节时抛出
     */
    public static StreamingAead chaCha20(byte[] key) {
        if (key == null || key.length != 32) {
            throw new OpenKeyException("ChaCha20-Poly1305 key must be 256 bits (32 bytes)");
        }
        return new StreamingAead(key, Algorithm.CHACHA20_POLY1305);
    }

    /**
     * Set the plaintext segment size in bytes.
     * 设置明文段大小（字节）。
     *
     * <p>Each segment of this size will be independently encrypted and authenticated.
     * Default is 1 MB (1048576 bytes). Minimum is 256 bytes, maximum is 64 MB.</p>
     * <p>此大小的每个段将被独立加密和认证。默认为 1 MB（1048576 字节）。
     * 最小为 256 字节，最大为 64 MB。</p>
     *
     * @param bytes segment size in bytes | 段大小（字节）
     * @return this instance for chaining | 当前实例以支持链式调用
     * @throws IllegalArgumentException if bytes is out of range | 当字节数超出范围时抛出
     */
    public StreamingAead setSegmentSize(int bytes) {
        if (bytes < MIN_SEGMENT_SIZE || bytes > MAX_SEGMENT_SIZE) {
            throw new IllegalArgumentException(
                    "Segment size must be between " + MIN_SEGMENT_SIZE
                            + " and " + MAX_SEGMENT_SIZE + " bytes, got: " + bytes);
        }
        this.segmentSize = bytes;
        return this;
    }

    /**
     * Set additional authenticated data (AAD) for all segments.
     * 为所有段设置附加认证数据（AAD）。
     *
     * <p>The AAD is included in authentication but not encrypted.
     * Per-segment metadata (base nonce, counter, final flag) is appended internally.</p>
     * <p>AAD 包含在认证中但不加密。每段的元数据（基础 nonce、计数器、最终段标记）在内部追加。</p>
     *
     * @param aad additional authenticated data (may be null) | 附加认证数据（可以为 null）
     * @return this instance for chaining | 当前实例以支持链式调用
     */
    public StreamingAead setAad(byte[] aad) {
        this.aad = aad != null ? aad.clone() : null;
        return this;
    }

    /**
     * Securely erase key material and AAD from memory.
     * 安全擦除内存中的密钥材料和 AAD。
     */
    @Override
    public void close() {
        if (!closed) {
            Arrays.fill(key, (byte) 0);
            if (aad != null) {
                Arrays.fill(aad, (byte) 0);
            }
            closed = true;
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("StreamingAead has been closed");
        }
    }

    /**
     * Encrypt data from an input stream to an output stream.
     * 从输入流加密数据到输出流。
     *
     * @param in  source plaintext stream | 源明文流
     * @param out destination ciphertext stream | 目标密文流
     * @throws OpenCryptoException if encryption fails | 当加密失败时抛出
     */
    public void encrypt(InputStream in, OutputStream out) {
        ensureOpen();
        byte[] currentBuf = new byte[segmentSize];
        byte[] nextBuf = null;
        try {
            writeInt(out, segmentSize);

            byte[] baseNonce = new byte[NONCE_LENGTH];
            RANDOM.nextBytes(baseNonce);
            out.write(baseNonce);

            SecretKeySpec secretKey = new SecretKeySpec(key, algorithm.keyAlgorithm);
            Cipher cipher = Cipher.getInstance(algorithm.transformation);

            int currentLen = readFully(in, currentBuf, 0, segmentSize);
            int segmentIndex = 0;

            if (currentLen <= 0) {
                writeEncryptedSegment(out, cipher, secretKey, baseNonce, segmentIndex, new byte[0], 0, true);
            } else if (currentLen < segmentSize) {
                writeEncryptedSegment(out, cipher, secretKey, baseNonce, segmentIndex,
                        currentBuf, currentLen, true);
            } else {
                nextBuf = new byte[segmentSize];
                int nextLen = readFully(in, nextBuf, 0, segmentSize);

                while (nextLen > 0) {
                    writeEncryptedSegment(out, cipher, secretKey, baseNonce, segmentIndex,
                            currentBuf, currentLen, false);
                    segmentIndex++;
                    if (segmentIndex < 0) {
                        throw new OpenCryptoException("Stream too large: segment index overflow");
                    }

                    byte[] tmp = currentBuf;
                    currentBuf = nextBuf;
                    currentLen = nextLen;
                    nextBuf = tmp;
                    nextLen = readFully(in, nextBuf, 0, segmentSize);
                }

                writeEncryptedSegment(out, cipher, secretKey, baseNonce, segmentIndex,
                        currentBuf, currentLen, true);
            }

            out.flush();
        } catch (OpenCryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException("Streaming AEAD encryption failed", e);
        } finally {
            Arrays.fill(currentBuf, (byte) 0);
            if (nextBuf != null) {
                Arrays.fill(nextBuf, (byte) 0);
            }
        }
    }

    /**
     * Decrypt data from an input stream to an output stream.
     * 从输入流解密数据到输出流。
     *
     * @param in  source ciphertext stream | 源密文流
     * @param out destination plaintext stream | 目标明文流
     * @throws OpenCryptoException if decryption or authentication fails | 当解密或认证失败时抛出
     */
    public void decrypt(InputStream in, OutputStream out) {
        ensureOpen();
        try {
            int storedSegmentSize = readInt(in);
            if (storedSegmentSize < MIN_SEGMENT_SIZE || storedSegmentSize > MAX_SEGMENT_SIZE) {
                throw new OpenCryptoException("Invalid segment size in header: " + storedSegmentSize);
            }

            byte[] baseNonce = new byte[NONCE_LENGTH];
            if (readFully(in, baseNonce, 0, NONCE_LENGTH) != NONCE_LENGTH) {
                throw new OpenCryptoException("Truncated stream: missing base nonce");
            }

            SecretKeySpec secretKey = new SecretKeySpec(key, algorithm.keyAlgorithm);
            Cipher cipher = Cipher.getInstance(algorithm.transformation);
            int fullEncSegSize = NONCE_LENGTH + storedSegmentSize + TAG_LENGTH_BYTES;
            if (fullEncSegSize < 0) {
                throw new OpenCryptoException("Segment size overflow");
            }
            int segmentIndex = 0;
            boolean foundFinal = false;

            PushbackInputStream pb = new PushbackInputStream(in, 1);
            byte[] encSegBuf = new byte[fullEncSegSize];
            byte[] segNonce = new byte[NONCE_LENGTH];

            while (!foundFinal) {
                int bytesRead = readFully(pb, encSegBuf, 0, fullEncSegSize);

                if (bytesRead == 0) {
                    throw new OpenCryptoException("Stream truncated: no final segment found");
                }

                if (bytesRead < NONCE_LENGTH + TAG_LENGTH_BYTES) {
                    throw new OpenCryptoException("Truncated stream: segment too short");
                }

                System.arraycopy(encSegBuf, 0, segNonce, 0, NONCE_LENGTH);

                byte[] expectedNonce = deriveNonce(baseNonce, segmentIndex);
                if (!Arrays.equals(segNonce, expectedNonce)) {
                    throw new OpenCryptoException(
                            "Segment " + segmentIndex + " nonce mismatch - possible reordering attack");
                }

                int ciphertextLen = bytesRead - NONCE_LENGTH;
                byte[] ciphertext = new byte[ciphertextLen];
                System.arraycopy(encSegBuf, NONCE_LENGTH, ciphertext, 0, ciphertextLen);

                boolean isFinal;
                if (bytesRead < fullEncSegSize) {
                    isFinal = true;
                } else {
                    int peek = pb.read();
                    if (peek == -1) {
                        isFinal = true;
                    } else {
                        isFinal = false;
                        pb.unread(peek);
                    }
                }

                byte[] segAad = buildSegmentAad(baseNonce, segmentIndex, isFinal);
                initCipher(cipher, Cipher.DECRYPT_MODE, secretKey, segNonce);
                cipher.updateAAD(segAad);
                byte[] plaintext = cipher.doFinal(ciphertext);
                out.write(plaintext);

                foundFinal = isFinal;
                segmentIndex++;
                if (segmentIndex < 0) {
                    throw new OpenCryptoException("Stream too large: segment index overflow");
                }
            }

            out.flush();
        } catch (OpenCryptoException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenCryptoException("Streaming AEAD decryption failed", e);
        }
    }

    /**
     * Encrypt a file.
     * 加密文件。
     *
     * @param source source file path | 源文件路径
     * @param target target file path | 目标文件路径
     * @throws OpenCryptoException if encryption fails | 当加密失败时抛出
     */
    public void encryptFile(Path source, Path target) {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(source));
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
            encrypt(in, out);
        } catch (OpenCryptoException e) {
            throw e;
        } catch (IOException e) {
            throw new OpenCryptoException("Streaming AEAD file encryption failed", e);
        }
    }

    /**
     * Decrypt a file. Writes to a temp file and atomically renames on success
     * to avoid leaving partial unauthenticated plaintext on disk.
     * 解密文件。写入临时文件并在成功时原子性重命名，避免在磁盘上留下部分未认证的明文。
     *
     * @param source source (encrypted) file path | 源（加密）文件路径
     * @param target target (decrypted) file path | 目标（解密）文件路径
     * @throws OpenCryptoException if decryption fails | 当解密失败时抛出
     */
    public void decryptFile(Path source, Path target) {
        // Use random suffix to prevent symlink attacks on predictable temp paths
        String randomSuffix = Long.toHexString(RANDOM.nextLong());
        Path tmp = target.resolveSibling(target.getFileName().toString() + ".tmp." + randomSuffix);
        try {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(source));
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(tmp))) {
                decrypt(in, out);
            }
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (OpenCryptoException e) {
            deleteQuietly(tmp);
            throw e;
        } catch (IOException e) {
            deleteQuietly(tmp);
            throw new OpenCryptoException("Streaming AEAD file decryption failed", e);
        }
    }

    // ---- Internal helpers ----

    private void writeEncryptedSegment(OutputStream out, Cipher cipher, SecretKeySpec secretKey,
                                       byte[] baseNonce, int segmentIndex,
                                       byte[] plaintext, int plaintextLen, boolean isFinal)
            throws IOException, GeneralSecurityException {
        byte[] segNonce = deriveNonce(baseNonce, segmentIndex);
        byte[] segAad = buildSegmentAad(baseNonce, segmentIndex, isFinal);
        initCipher(cipher, Cipher.ENCRYPT_MODE, secretKey, segNonce);
        cipher.updateAAD(segAad);
        byte[] ciphertext = cipher.doFinal(plaintext, 0, plaintextLen);
        out.write(segNonce);
        out.write(ciphertext);
    }

    /**
     * Derive a per-segment nonce from the base nonce and segment index.
     * Uses concatenation: first 8 bytes of base nonce + 4-byte big-endian segment counter.
     * 从基础 nonce 和段索引派生每段的 nonce。
     * 使用连接方式：基础 nonce 的前 8 字节 + 4 字节大端序段计数器。
     */
    private static byte[] deriveNonce(byte[] baseNonce, int segmentIndex) {
        byte[] nonce = new byte[NONCE_LENGTH];
        System.arraycopy(baseNonce, 0, nonce, 0, NONCE_PREFIX_LENGTH);
        nonce[8] = (byte) (segmentIndex >>> 24);
        nonce[9] = (byte) (segmentIndex >>> 16);
        nonce[10] = (byte) (segmentIndex >>> 8);
        nonce[11] = (byte) segmentIndex;
        return nonce;
    }

    /**
     * Build per-segment AAD containing the base nonce, segment index and final flag.
     * Binding the base nonce in AAD prevents cross-stream segment substitution.
     * 构建包含基础 nonce、段索引和最终段标记的每段 AAD。
     * 在 AAD 中绑定基础 nonce 防止跨流段替换。
     */
    private byte[] buildSegmentAad(byte[] baseNonce, int segmentIndex, boolean isFinal) {
        int userAadLen = (aad != null) ? aad.length : 0;
        byte[] segAad = new byte[userAadLen + NONCE_LENGTH + 5];
        if (aad != null) {
            System.arraycopy(aad, 0, segAad, 0, aad.length);
        }
        System.arraycopy(baseNonce, 0, segAad, userAadLen, NONCE_LENGTH);
        int offset = userAadLen + NONCE_LENGTH;
        segAad[offset] = (byte) (segmentIndex >>> 24);
        segAad[offset + 1] = (byte) (segmentIndex >>> 16);
        segAad[offset + 2] = (byte) (segmentIndex >>> 8);
        segAad[offset + 3] = (byte) segmentIndex;
        segAad[offset + 4] = isFinal ? (byte) 1 : (byte) 0;
        return segAad;
    }

    private void initCipher(Cipher cipher, int mode, SecretKeySpec secretKey, byte[] nonce)
            throws GeneralSecurityException {
        if (algorithm == Algorithm.AES_GCM) {
            cipher.init(mode, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
        } else {
            // ChaCha20-Poly1305: SunJCE accepts IvParameterSpec with 12-byte nonce, counter=0
            cipher.init(mode, secretKey, new IvParameterSpec(nonce));
        }
    }

    private static void writeInt(OutputStream out, int value) throws IOException {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private static int readInt(InputStream in) throws IOException {
        byte[] buf = new byte[4];
        if (readFully(in, buf, 0, 4) != 4) {
            throw new OpenCryptoException("Truncated stream: missing header");
        }
        return ((buf[0] & 0xFF) << 24) |
               ((buf[1] & 0xFF) << 16) |
               ((buf[2] & 0xFF) << 8) |
               (buf[3] & 0xFF);
    }

    private static int readFully(InputStream in, byte[] buffer, int offset, int len) throws IOException {
        int totalRead = 0;
        while (totalRead < len) {
            int read = in.read(buffer, offset + totalRead, len - totalRead);
            if (read == -1) {
                break;
            }
            totalRead += read;
        }
        return totalRead;
    }

    private static void validateAesKey(byte[] key) {
        if (key == null) {
            throw new OpenKeyException("AES key must not be null");
        }
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new OpenKeyException(
                    "AES key must be 128, 192, or 256 bits (16, 24, or 32 bytes), got: " + key.length);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best effort cleanup
        }
    }
}
