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

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;

/**
 * PGP Algorithm Configuration - Defines algorithms used in PGP operations
 * PGP 算法配置 - 定义 PGP 操作中使用的算法
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>PGP public key algorithm definitions - PGP 公钥算法定义</li>
 *   <li>PGP symmetric encryption algorithm definitions - PGP 对称加密算法定义</li>
 *   <li>PGP hash algorithm definitions - PGP 哈希算法定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PgpAlgorithm.Symmetric sym = PgpAlgorithm.DEFAULT_SYMMETRIC;
 * PgpAlgorithm.Hash hash = PgpAlgorithm.DEFAULT_HASH;
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.2.0
 */
public final class PgpAlgorithm {

    private PgpAlgorithm() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Public Key Algorithms
     * 公钥算法
     */
    public enum PublicKey {
        RSA_GENERAL(PublicKeyAlgorithmTags.RSA_GENERAL, "RSA"),
        RSA_ENCRYPT(PublicKeyAlgorithmTags.RSA_ENCRYPT, "RSA (Encrypt)"),
        RSA_SIGN(PublicKeyAlgorithmTags.RSA_SIGN, "RSA (Sign)"),
        ELGAMAL_ENCRYPT(PublicKeyAlgorithmTags.ELGAMAL_ENCRYPT, "ElGamal"),
        DSA(PublicKeyAlgorithmTags.DSA, "DSA"),
        ECDH(PublicKeyAlgorithmTags.ECDH, "ECDH"),
        ECDSA(PublicKeyAlgorithmTags.ECDSA, "ECDSA"),
        EDDSA(PublicKeyAlgorithmTags.EDDSA, "EdDSA");

        private final int tag;
        private final String name;

        PublicKey(int tag, String name) {
            this.tag = tag;
            this.name = name;
        }

        public int tag() {
            return tag;
        }

        public String algorithmName() {
            return name;
        }
    }

    /**
     * Symmetric Key Algorithms for encryption
     * 用于加密的对称密钥算法
     */
    public enum Symmetric {
        AES_128(SymmetricKeyAlgorithmTags.AES_128, "AES-128", 128),
        AES_192(SymmetricKeyAlgorithmTags.AES_192, "AES-192", 192),
        AES_256(SymmetricKeyAlgorithmTags.AES_256, "AES-256", 256),
        TWOFISH(SymmetricKeyAlgorithmTags.TWOFISH, "Twofish", 256),
        CAMELLIA_128(SymmetricKeyAlgorithmTags.CAMELLIA_128, "Camellia-128", 128),
        CAMELLIA_192(SymmetricKeyAlgorithmTags.CAMELLIA_192, "Camellia-192", 192),
        CAMELLIA_256(SymmetricKeyAlgorithmTags.CAMELLIA_256, "Camellia-256", 256);

        private final int tag;
        private final String name;
        private final int keySize;

        Symmetric(int tag, String name, int keySize) {
            this.tag = tag;
            this.name = name;
            this.keySize = keySize;
        }

        public int tag() {
            return tag;
        }

        public String algorithmName() {
            return name;
        }

        public int keySize() {
            return keySize;
        }
    }

    /**
     * Hash Algorithms for signatures and integrity
     * 用于签名和完整性的哈希算法
     */
    public enum Hash {
        SHA256(HashAlgorithmTags.SHA256, "SHA-256"),
        SHA384(HashAlgorithmTags.SHA384, "SHA-384"),
        SHA512(HashAlgorithmTags.SHA512, "SHA-512"),
        SHA3_256(HashAlgorithmTags.SHA3_256, "SHA3-256"),
        SHA3_512(HashAlgorithmTags.SHA3_512, "SHA3-512");

        private final int tag;
        private final String name;

        Hash(int tag, String name) {
            this.tag = tag;
            this.name = name;
        }

        public int tag() {
            return tag;
        }

        public String algorithmName() {
            return name;
        }
    }

    /**
     * Default RSA key size in bits.
     * 默认 RSA 密钥大小（位）。
     */
    public static final int DEFAULT_RSA_KEY_SIZE = 4096;

    /**
     * Minimum RSA key size in bits.
     * 最小 RSA 密钥大小（位）。
     */
    public static final int MIN_RSA_KEY_SIZE = 2048;

    /**
     * Default symmetric algorithm.
     * 默认对称算法。
     */
    public static final Symmetric DEFAULT_SYMMETRIC = Symmetric.AES_256;

    /**
     * Default hash algorithm.
     * 默认哈希算法。
     */
    public static final Hash DEFAULT_HASH = Hash.SHA256;
}
