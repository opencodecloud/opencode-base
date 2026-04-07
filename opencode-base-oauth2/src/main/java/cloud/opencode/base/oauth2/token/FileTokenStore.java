package cloud.opencode.base.oauth2.token;

import cloud.opencode.base.oauth2.OAuth2Token;
import cloud.opencode.base.oauth2.exception.OAuth2ErrorCode;
import cloud.opencode.base.oauth2.exception.OAuth2Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * File-based Token Store
 * 文件 Token 存储
 *
 * <p>Persistent file-based implementation of TokenStore.</p>
 * <p>TokenStore 的持久化文件实现。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Persistent storage - 持久化存储</li>
 *   <li>Simple JSON format - 简单 JSON 格式</li>
 *   <li>Automatic directory creation - 自动创建目录</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TokenStore store = new FileTokenStore(Path.of(".tokens"));
 *
 * // Save token
 * store.save("user-1", token);
 *
 * // Load token
 * Optional<OAuth2Token> loaded = store.load("user-1");
 * }</pre>
 *
 * <p><strong>File Format | 文件格式:</strong></p>
 * <p>Tokens are stored as simple property files for easy debugging.</p>
 * <p>令牌以简单的属性文件格式存储，便于调试。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes (validates inputs) - 空值安全: 是（验证输入）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.0
 */
public class FileTokenStore implements TokenStore {

    private static final System.Logger LOGGER = System.getLogger(FileTokenStore.class.getName());
    private static final String TOKEN_EXTENSION = ".token";
    private static final Pattern NON_SAFE_FILENAME_PATTERN = Pattern.compile("[^a-zA-Z0-9_-]");
    private final Path directory;

    /**
     * Create a new file token store
     * 创建新的文件令牌存储
     *
     * @param directory the storage directory | 存储目录
     * @throws OAuth2Exception if directory creation fails | 如果目录创建失败
     */
    public FileTokenStore(Path directory) {
        this.directory = directory;
        LOGGER.log(System.Logger.Level.WARNING,
            "FileTokenStore stores tokens as PLAINTEXT without encryption. " +
            "This implementation should only be used for development/testing. " +
            "Use an encrypted token store for production environments. Directory: {0}",
            directory);
        try {
            Files.createDirectories(directory);
            setDirectoryOwnerOnly(directory);
        } catch (IOException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_STORE_ERROR,
                    "Failed to create token store directory: " + directory, e);
        }
    }

    @Override
    public void save(String key, OAuth2Token token) {
        if (key == null || token == null) {
            throw new IllegalArgumentException("key and token cannot be null");
        }

        Path file = getTokenFile(key);
        String content = serialize(token);

        try {
            // Write to temp file first, set permissions, then atomically move
            Path temp = Files.createTempFile(directory, ".token", ".tmp");
            try {
                Files.writeString(temp, content);
                setOwnerOnly(temp);
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) {
                // Clean up temp file on failure
                try { Files.deleteIfExists(temp); } catch (IOException ignored) { }
                throw e;
            }
        } catch (IOException e) {
            throw new OAuth2Exception(OAuth2ErrorCode.TOKEN_STORE_ERROR,
                    "Failed to save token: " + key, e);
        }
    }

    @Override
    public Optional<OAuth2Token> load(String key) {
        if (key == null) {
            return Optional.empty();
        }

        Path file = getTokenFile(key);
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try {
            String content = Files.readString(file);
            return Optional.of(deserialize(content));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        if (key == null) {
            return;
        }

        Path file = getTokenFile(key);
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Ignore deletion errors
        }
    }

    @Override
    public void deleteAll() {
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.toString().endsWith(TOKEN_EXTENSION))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        } catch (IOException e) {
            // Ignore
        }
    }

    @Override
    public Set<String> keys() {
        Set<String> result = new HashSet<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.toString().endsWith(TOKEN_EXTENSION))
                    .forEach(p -> {
                        String filename = p.getFileName().toString();
                        result.add(filename.substring(0, filename.length() - TOKEN_EXTENSION.length()));
                    });
        } catch (IOException e) {
            // Return empty set on error
        }
        return result;
    }

    /**
     * Get the file path for a token key
     * 获取令牌键的文件路径
     *
     * @param key the token key | 令牌键
     * @return the file path | 文件路径
     */
    private Path getTokenFile(String key) {
        return directory.resolve(sanitize(key) + TOKEN_EXTENSION);
    }

    /**
     * Sanitize key for use as filename
     * 清理键以用作文件名
     *
     * @param key the key | 键
     * @return the sanitized key | 清理后的键
     */
    private String sanitize(String key) {
        return NON_SAFE_FILENAME_PATTERN.matcher(key).replaceAll("_");
    }

    /**
     * Set file permissions to owner-only (rw-------) on POSIX systems.
     * 在 POSIX 系统上设置文件权限为仅所有者（rw-------）。
     *
     * @param path the file path | 文件路径
     */
    private void setOwnerOnly(Path path) {
        try {
            Set<PosixFilePermission> ownerOnly = Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(path, ownerOnly);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Non-POSIX filesystem (Windows) — permissions not available
        }
    }

    /**
     * Set directory permissions to owner-only (rwx------) on POSIX systems.
     * 在 POSIX 系统上设置目录权限为仅所有者（rwx------）。
     *
     * @param dir the directory path | 目录路径
     */
    private void setDirectoryOwnerOnly(Path dir) {
        try {
            Set<PosixFilePermission> ownerOnly = Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(dir, ownerOnly);
        } catch (UnsupportedOperationException | IOException ignored) {
            // Non-POSIX filesystem (Windows) — permissions not available
        }
    }

    /**
     * Escape newlines and backslashes in a value for safe line-based serialization.
     * 转义值中的换行符和反斜杠，以便安全的基于行的序列化。
     *
     * @param value the value | 值
     * @return the escaped value | 转义后的值
     */
    private String escapeValue(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Unescape a serialized value, reversing escapeValue().
     * 反转义序列化的值，逆转 escapeValue()。
     *
     * @param value the escaped value | 转义后的值
     * @return the original value or null if empty | 原始值，为空则返回 null
     */
    private String unescapeValue(String value) {
        if (value == null || value.isEmpty()) return null;
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(i + 1);
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Serialize token to string
     * 将令牌序列化为字符串
     *
     * @param token the token | 令牌
     * @return the serialized string | 序列化的字符串
     */
    private String serialize(OAuth2Token token) {
        StringBuilder sb = new StringBuilder();
        sb.append("accessToken=").append(escapeValue(token.accessToken())).append("\n");
        sb.append("tokenType=").append(escapeValue(token.tokenType())).append("\n");
        if (token.refreshToken() != null) {
            sb.append("refreshToken=").append(escapeValue(token.refreshToken())).append("\n");
        }
        if (token.idToken() != null) {
            sb.append("idToken=").append(escapeValue(token.idToken())).append("\n");
        }
        if (!token.scopes().isEmpty()) {
            sb.append("scopes=").append(escapeValue(String.join(" ", token.scopes()))).append("\n");
        }
        sb.append("issuedAt=").append(token.issuedAt().toEpochMilli()).append("\n");
        if (token.expiresAt() != null) {
            sb.append("expiresAt=").append(token.expiresAt().toEpochMilli()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Deserialize token from string
     * 从字符串反序列化令牌
     *
     * @param content the serialized content | 序列化的内容
     * @return the token | 令牌
     */
    private OAuth2Token deserialize(String content) {
        OAuth2Token.Builder builder = OAuth2Token.builder();

        for (String line : content.split("\n")) {
            int eq = line.indexOf('=');
            if (eq <= 0) continue;

            String key = line.substring(0, eq);
            String rawValue = line.substring(eq + 1);
            String value = unescapeValue(rawValue);

            switch (key) {
                case "accessToken" -> builder.accessToken(value);
                case "tokenType" -> builder.tokenType(value);
                case "refreshToken" -> builder.refreshToken(value);
                case "idToken" -> builder.idToken(value);
                case "scopes" -> builder.scopeString(value);
                case "issuedAt" -> {
                    try { builder.issuedAt(Instant.ofEpochMilli(Long.parseLong(rawValue))); }
                    catch (NumberFormatException ignored) { /* skip corrupt value */ }
                }
                case "expiresAt" -> {
                    try { builder.expiresAt(Instant.ofEpochMilli(Long.parseLong(rawValue))); }
                    catch (NumberFormatException ignored) { /* skip corrupt value */ }
                }
            }
        }

        return builder.build();
    }
}
