/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.crypto.pgp;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

import java.util.Objects;

/**
 * PGP Key Pair - Holds PGP public and secret key pair
 * PGP 密钥对 - 保存 PGP 公钥和私钥对
 *
 * <p>This record encapsulates a PGP key pair including the public key for encryption
 * and the secret key for decryption/signing operations.</p>
 * <p>此记录封装了 PGP 密钥对，包括用于加密的公钥和用于解密/签名操作的私钥。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PGP public and secret key encapsulation - PGP 公钥和私钥封装</li>
 *   <li>Key capability checks (encrypt/sign) - 密钥能力检查（加密/签名）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PgpKeyPair pair = PgpKeyUtil.generateKeyPair("user@example.com", "pass");
 * String keyId = pair.keyIdHex();
 * boolean canEncrypt = pair.canEncrypt();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @param publicKey  the PGP public key / PGP 公钥
 * @param secretKey  the PGP secret key / PGP 私钥
 * @param userId     the user ID associated with the key / 与密钥关联的用户 ID
 * @param keyId      the key ID / 密钥 ID
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public record PgpKeyPair(
        PGPPublicKey publicKey,
        PGPSecretKey secretKey,
        String userId,
        long keyId
) {

    /**
     * Creates a PGP key pair with the given public and secret keys.
     * 使用给定的公钥和私钥创建 PGP 密钥对。
     *
     * @param publicKey the PGP public key
     * @param secretKey the PGP secret key
     * @param userId    the user ID
     * @param keyId     the key ID
     * @throws NullPointerException if publicKey or secretKey is null
     */
    public PgpKeyPair {
        Objects.requireNonNull(publicKey, "publicKey must not be null");
        Objects.requireNonNull(secretKey, "secretKey must not be null");
    }

    /**
     * Creates a PGP key pair from secret key (derives public key automatically).
     * 从私钥创建 PGP 密钥对（自动派生公钥）。
     *
     * @param secretKey the PGP secret key
     * @param userId    the user ID
     * @return a new PgpKeyPair
     */
    public static PgpKeyPair fromSecretKey(PGPSecretKey secretKey, String userId) {
        Objects.requireNonNull(secretKey, "secretKey must not be null");
        return new PgpKeyPair(
                secretKey.getPublicKey(),
                secretKey,
                userId,
                secretKey.getKeyID()
        );
    }

    /**
     * Returns the key ID as hexadecimal string.
     * 返回十六进制格式的密钥 ID。
     *
     * @return the key ID in hex format
     */
    public String keyIdHex() {
        return Long.toHexString(keyId).toUpperCase();
    }

    /**
     * Checks if this key pair can be used for encryption.
     * 检查此密钥对是否可用于加密。
     *
     * @return true if the public key can encrypt
     */
    public boolean canEncrypt() {
        return publicKey.isEncryptionKey();
    }

    /**
     * Checks if this key pair can be used for signing.
     * 检查此密钥对是否可用于签名。
     *
     * @return true if the secret key can sign
     */
    public boolean canSign() {
        return secretKey.isSigningKey();
    }
}
