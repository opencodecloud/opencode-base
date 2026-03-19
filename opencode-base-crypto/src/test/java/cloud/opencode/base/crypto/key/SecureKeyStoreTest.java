package cloud.opencode.base.crypto.key;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SecureKeyStore
 *
* 
* @author Leon Soo
 * @since JDK 25, OpenCode-Base-Crypto V1.0.0
 */
@DisplayName("SecureKeyStore Tests")
class SecureKeyStoreTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should create new key store")
    void testCreateKeyStore() {
        try (SecureKeyStore store = SecureKeyStore.create()) {
            assertNotNull(store);
            assertTrue(store.aliases().isEmpty());
        }
    }

    @Test
    @DisplayName("Should store and retrieve secret key")
    void testStoreAndGetSecretKey() {
        try (SecureKeyStore store = SecureKeyStore.create()) {
            SecretKey key = KeyGenerator.generateAes256Key();
            char[] password = "test-password".toCharArray();

            store.store("test-key", key, password);
            assertTrue(store.containsAlias("test-key"));

            SecretKey retrieved = store.getSecretKey("test-key", password);
            assertNotNull(retrieved);
            assertEquals("AES", retrieved.getAlgorithm());
            assertArrayEquals(key.getEncoded(), retrieved.getEncoded());
        }
    }

    @Test
    @DisplayName("Should save and load key store")
    void testSaveAndLoad() {
        Path keystorePath = tempDir.resolve("test.p12");
        char[] storePassword = "store-password".toCharArray();
        char[] keyPassword = "key-password".toCharArray();

        // Create and save
        try (SecureKeyStore store = SecureKeyStore.create()) {
            SecretKey key = KeyGenerator.generateAes256Key();
            store.store("test-key", key, keyPassword);
            store.save(keystorePath, storePassword);
        }

        assertTrue(keystorePath.toFile().exists());

        // Load and verify
        try (SecureKeyStore store = SecureKeyStore.load(keystorePath, storePassword)) {
            assertTrue(store.containsAlias("test-key"));
            SecretKey retrieved = store.getSecretKey("test-key", keyPassword);
            assertNotNull(retrieved);
            assertEquals("AES", retrieved.getAlgorithm());
        }
    }

    @Test
    @DisplayName("Should list all aliases")
    void testAliases() {
        try (SecureKeyStore store = SecureKeyStore.create()) {
            SecretKey key1 = KeyGenerator.generateAes256Key();
            SecretKey key2 = KeyGenerator.generateAes128Key();
            char[] password = "test-password".toCharArray();

            store.store("key1", key1, password);
            store.store("key2", key2, password);

            Set<String> aliases = store.aliases();
            assertEquals(2, aliases.size());
            assertTrue(aliases.contains("key1"));
            assertTrue(aliases.contains("key2"));
        }
    }

    @Test
    @DisplayName("Should delete entry")
    void testDeleteEntry() {
        try (SecureKeyStore store = SecureKeyStore.create()) {
            SecretKey key = KeyGenerator.generateAes256Key();
            char[] password = "test-password".toCharArray();

            store.store("test-key", key, password);
            assertTrue(store.containsAlias("test-key"));

            store.deleteEntry("test-key");
            assertFalse(store.containsAlias("test-key"));
        }
    }

    @Test
    @DisplayName("Should handle multiple keys with different passwords")
    void testMultipleKeysWithDifferentPasswords() {
        try (SecureKeyStore store = SecureKeyStore.create()) {
            SecretKey key1 = KeyGenerator.generateAes256Key();
            SecretKey key2 = KeyGenerator.generateAes128Key();
            char[] password1 = "password1".toCharArray();
            char[] password2 = "password2".toCharArray();

            store.store("key1", key1, password1);
            store.store("key2", key2, password2);

            SecretKey retrieved1 = store.getSecretKey("key1", password1);
            SecretKey retrieved2 = store.getSecretKey("key2", password2);

            assertNotNull(retrieved1);
            assertNotNull(retrieved2);
            assertArrayEquals(key1.getEncoded(), retrieved1.getEncoded());
            assertArrayEquals(key2.getEncoded(), retrieved2.getEncoded());
        }
    }
}
