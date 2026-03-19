package cloud.opencode.base.crypto.keyexchange;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Key exchange engine interface for various key agreement protocols
 * 密钥协商引擎接口 - 支持各种密钥协商协议
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key pair generation for key exchange - 密钥交换的密钥对生成</li>
 *   <li>Shared secret computation - 共享密钥计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * KeyExchangeEngine engine = X25519Engine.create();
 * KeyPair keyPair = engine.generateKeyPair();
 * byte[] shared = engine.computeSharedSecret(keyPair.getPrivate(), peerPublicKey);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public interface KeyExchangeEngine {

    /**
     * Generates a new key pair for the key exchange
     * 为密钥交换生成新的密钥对
     *
     * @return a new key pair
     */
    KeyPair generateKeyPair();

    /**
     * Sets the private key for this party
     * 设置本方的私钥
     *
     * @param privateKey the private key to use
     * @return this engine for method chaining
     * @throws NullPointerException if privateKey is null
     */
    KeyExchangeEngine setPrivateKey(PrivateKey privateKey);

    /**
     * Sets the remote party's public key
     * 设置对方的公钥
     *
     * @param publicKey the remote public key
     * @return this engine for method chaining
     * @throws NullPointerException if publicKey is null
     */
    KeyExchangeEngine setRemotePublicKey(PublicKey publicKey);

    /**
     * Computes the shared secret using the configured keys
     * 使用配置的密钥计算共享密钥
     *
     * @return the raw shared secret bytes
     * @throws IllegalStateException if required keys are not set
     */
    byte[] computeSharedSecret();

    /**
     * Derives key material from the shared secret using HKDF
     * 使用 HKDF 从共享密钥派生密钥材料
     *
     * @param info optional context and application specific information (can be null)
     * @param length desired key length in bytes
     * @return derived key material
     * @throws IllegalStateException if required keys are not set
     * @throws IllegalArgumentException if length is invalid
     */
    byte[] deriveKey(byte[] info, int length);

    /**
     * Gets the algorithm name of this key exchange engine
     * 获取密钥交换引擎的算法名称
     *
     * @return the algorithm name
     */
    String getAlgorithm();
}
