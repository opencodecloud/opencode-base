package cloud.opencode.base.yml.internal;

import cloud.opencode.base.yml.DefaultYmlNode;
import cloud.opencode.base.yml.YmlConfig;
import cloud.opencode.base.yml.YmlNode;
import cloud.opencode.base.yml.exception.OpenYmlException;
import cloud.opencode.base.yml.exception.YmlParseException;
import cloud.opencode.base.yml.security.YmlSecurity;
import cloud.opencode.base.yml.spi.YmlProvider;

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Built-in YAML Provider - Pure JDK YAML parser/dumper implementation
 * 内置 YAML 提供者 - 纯 JDK YAML 解析器/输出器实现
 *
 * <p>This provider implements a YAML parser and dumper using only JDK APIs,
 * without requiring any external dependencies like SnakeYAML.</p>
 * <p>此提供者仅使用 JDK API 实现 YAML 解析器和输出器，
 * 不需要任何外部依赖（如 SnakeYAML）。</p>
 *
 * <p><strong>Supported Features | 支持的特性:</strong></p>
 * <ul>
 *   <li>Key-value mappings - 键值映射</li>
 *   <li>Sequences (lists) with {@code - } prefix - 使用 {@code - } 前缀的序列（列表）</li>
 *   <li>Nested structures (indentation-based) - 嵌套结构（基于缩进）</li>
 *   <li>Quoted and unquoted strings - 带引号和不带引号的字符串</li>
 *   <li>Number, boolean, null type conversion - 数字、布尔、空值类型转换</li>
 *   <li>Multi-line scalars (literal {@code |} and folded {@code >}) - 多行标量</li>
 *   <li>Comments ({@code #}) - 注释</li>
 *   <li>Multi-document ({@code ---} separator) - 多文档</li>
 *   <li>Flow style ({@code {key: value}} and {@code [a, b]}) - 流式风格</li>
 *   <li>YAML bomb protection via security config - 通过安全配置防护 YAML 炸弹</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable config, no shared mutable state) - 线程安全: 是</li>
 *   <li>Null-safe: No (null input may throw exceptions) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.3
 */
public final class BuiltinYmlProvider implements YmlProvider {

    private final YmlConfig config;

    /**
     * Constructs a provider with default configuration.
     * 使用默认配置构造提供者。
     */
    public BuiltinYmlProvider() {
        this.config = YmlConfig.defaults();
    }

    /**
     * Constructs a provider with the given configuration.
     * 使用给定配置构造提供者。
     *
     * @param config the configuration | 配置
     */
    private BuiltinYmlProvider(YmlConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return "builtin";
    }

    @Override
    public int getPriority() {
        return 50;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    // ==================== Loading | 加载 ====================

    @Override
    public Map<String, Object> load(String yaml) {
        if (yaml == null || yaml.isBlank()) {
            return new LinkedHashMap<>();
        }
        validateSecurity(yaml);
        Object result = parseYaml(yaml);
        if (result instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typedMap = (Map<String, Object>) map;
            return typedMap;
        }
        // If the root is not a map, wrap it
        Map<String, Object> wrapped = new LinkedHashMap<>();
        if (result != null) {
            wrapped.put("value", result);
        }
        return wrapped;
    }

    @Override
    public <T> T load(String yaml, Class<T> clazz) {
        Map<String, Object> data = load(yaml);
        return bindToObject(data, clazz);
    }

    @Override
    public Map<String, Object> load(InputStream input) {
        try {
            String yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return load(yaml);
        } catch (IOException e) {
            throw new OpenYmlException("Failed to read YAML from input stream", e);
        }
    }

    @Override
    public <T> T load(InputStream input, Class<T> clazz) {
        Map<String, Object> data = load(input);
        return bindToObject(data, clazz);
    }

    @Override
    public List<Map<String, Object>> loadAll(String yaml) {
        if (yaml == null || yaml.isBlank()) {
            return List.of();
        }
        validateSecurity(yaml);
        List<String> documents = splitDocuments(yaml);
        List<Map<String, Object>> results = new ArrayList<>();
        for (String doc : documents) {
            if (!doc.isBlank()) {
                Object parsed = parseYaml(doc);
                if (parsed instanceof Map<?, ?> map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedMap = (Map<String, Object>) map;
                    results.add(typedMap);
                } else {
                    Map<String, Object> wrapped = new LinkedHashMap<>();
                    if (parsed != null) {
                        wrapped.put("value", parsed);
                    }
                    results.add(wrapped);
                }
            }
        }
        return results;
    }

    @Override
    public <T> List<T> loadAll(String yaml, Class<T> clazz) {
        List<Map<String, Object>> maps = loadAll(yaml);
        List<T> results = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            results.add(bindToObject(map, clazz));
        }
        return results;
    }

    // ==================== Dumping | 输出 ====================

    @Override
    public String dump(Object obj) {
        return dump(obj, config);
    }

    @Override
    public String dump(Object obj, YmlConfig dumpConfig) {
        if (obj == null) {
            return "null\n";
        }
        StringBuilder sb = new StringBuilder();
        int indent = dumpConfig != null ? dumpConfig.getIndent() : config.getIndent();
        dumpValue(sb, obj, 0, indent, false);
        return sb.toString();
    }

    @Override
    public void dump(Object obj, OutputStream output) {
        try {
            output.write(dump(obj).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new OpenYmlException("Failed to write YAML to output stream", e);
        }
    }

    @Override
    public void dump(Object obj, Writer writer) {
        try {
            writer.write(dump(obj));
        } catch (IOException e) {
            throw new OpenYmlException("Failed to write YAML to writer", e);
        }
    }

    @Override
    public String dumpAll(Iterable<?> documents) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object doc : documents) {
            if (!first) {
                sb.append("---\n");
            }
            sb.append(dump(doc));
            first = false;
        }
        return sb.toString();
    }

    // ==================== Tree Parsing | 树解析 ====================

    @Override
    public YmlNode parseTree(String yaml) {
        if (yaml == null || yaml.isBlank()) {
            return DefaultYmlNode.nullNode();
        }
        validateSecurity(yaml);
        Object parsed = parseYaml(yaml);
        return DefaultYmlNode.of(parsed);
    }

    @Override
    public YmlNode parseTree(InputStream input) {
        try {
            String yaml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return parseTree(yaml);
        } catch (IOException e) {
            throw new OpenYmlException("Failed to read YAML from input stream", e);
        }
    }

    // ==================== Validation | 验证 ====================

    @Override
    public boolean isValid(String yaml) {
        if (yaml == null || yaml.isBlank()) {
            return true;
        }
        try {
            parseYaml(yaml);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== Configuration | 配置 ====================

    @Override
    public YmlProvider configure(YmlConfig newConfig) {
        return new BuiltinYmlProvider(newConfig);
    }

    @Override
    public YmlConfig getConfig() {
        return config;
    }

    // ==================== Internal Parsing | 内部解析 ====================

    private void validateSecurity(String yaml) {
        if (config.isSafeMode()) {
            YmlSecurity.validate(yaml, config.getMaxDocumentSize(), config.getMaxAliasesForCollections());
        }
    }

    /**
     * Splits a multi-document YAML string into individual documents.
     * 将多文档 YAML 字符串拆分为单独的文档。
     */
    private List<String> splitDocuments(String yaml) {
        List<String> documents = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : yaml.split("\n", -1)) {
            if (line.equals("---") || line.equals("---\r")) {
                if (!current.isEmpty()) {
                    documents.add(current.toString());
                    current.setLength(0);
                }
            } else if (line.equals("...") || line.equals("...\r")) {
                if (!current.isEmpty()) {
                    documents.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(line).append('\n');
            }
        }
        if (!current.isEmpty()) {
            documents.add(current.toString());
        }
        if (documents.isEmpty()) {
            documents.add(yaml);
        }
        return documents;
    }

    /**
     * Main YAML parser entry point.
     * YAML 解析器主入口。
     */
    private Object parseYaml(String yaml) {
        List<String> lines = new ArrayList<>();
        for (String line : yaml.split("\n", -1)) {
            // Strip carriage return
            if (line.endsWith("\r")) {
                line = line.substring(0, line.length() - 1);
            }
            lines.add(line);
        }
        // Remove leading document marker
        if (!lines.isEmpty() && lines.getFirst().trim().equals("---")) {
            lines.removeFirst();
        }
        // Remove trailing empty lines and document end marker
        while (!lines.isEmpty() && lines.getLast().isBlank()) {
            lines.removeLast();
        }
        if (!lines.isEmpty() && lines.getLast().trim().equals("...")) {
            lines.removeLast();
        }

        if (lines.isEmpty()) {
            return new LinkedHashMap<String, Object>();
        }

        Map<String, Object> anchors = new LinkedHashMap<>();
        int[] pos = {0}; // mutable index into lines
        Object result = parseValue(lines, pos, -1, 0, anchors);

        // Resolve aliases and merge keys
        return resolveAliases(result, anchors);
    }

    /**
     * Resolves aliases (*name) and merge keys (<<) in parsed data.
     * 解析已解析数据中的别名（*name）和合并键（<<）。
     */
    @SuppressWarnings("unchecked")
    private Object resolveAliases(Object value, Map<String, Object> anchors) {
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = (Map<String, Object>) rawMap;
            Map<String, Object> resolved = new LinkedHashMap<>();

            // First, handle merge keys (<<)
            Object mergeValue = map.get("<<");
            if (mergeValue instanceof String mergeStr && mergeStr.startsWith("*")) {
                String aliasName = mergeStr.substring(1);
                Object aliasValue = anchors.get(aliasName);
                if (aliasValue instanceof Map<?, ?> aliasMap) {
                    Map<String, Object> resolvedAlias = (Map<String, Object>) resolveAliases(aliasMap, anchors);
                    resolved.putAll(resolvedAlias);
                }
            }

            // Then, overlay explicit keys
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if ("<<".equals(entry.getKey())) {
                    continue; // Already handled above
                }
                resolved.put(entry.getKey(), resolveAliases(entry.getValue(), anchors));
            }
            return resolved;
        } else if (value instanceof List<?> list) {
            List<Object> resolved = new ArrayList<>();
            for (Object item : list) {
                resolved.add(resolveAliases(item, anchors));
            }
            return resolved;
        } else if (value instanceof String str && str.startsWith("*")) {
            String aliasName = str.substring(1);
            Object aliasValue = anchors.get(aliasName);
            return aliasValue != null ? deepCopy(aliasValue) : value;
        }
        return value;
    }

    /**
     * Deep-copies a value (for alias resolution).
     * 深拷贝一个值（用于别名解析）。
     */
    @SuppressWarnings("unchecked")
    private Object deepCopy(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put((String) entry.getKey(), deepCopy(entry.getValue()));
            }
            return copy;
        } else if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            for (Object item : list) {
                copy.add(deepCopy(item));
            }
            return copy;
        }
        return value; // Immutable scalars are shared
    }

    /**
     * Parses a YAML value starting at the current position.
     * 从当前位置开始解析 YAML 值。
     *
     * @param lines        all lines | 所有行
     * @param pos          current line index (mutable) | 当前行索引（可变）
     * @param parentIndent the indent level of the parent | 父级缩进级别
     * @param depth        current nesting depth | 当前嵌套深度
     * @param anchors      anchor registry | 锚点注册表
     * @return the parsed value | 解析后的值
     */
    private Object parseValue(List<String> lines, int[] pos, int parentIndent, int depth, Map<String, Object> anchors) {
        checkDepth(depth);
        skipBlankAndCommentLines(lines, pos);
        if (pos[0] >= lines.size()) {
            return null;
        }

        String line = lines.get(pos[0]);
        int indent = getIndent(line);
        String trimmed = line.trim();

        // Flow-style map or list
        if (trimmed.startsWith("{")) {
            pos[0]++;
            return parseFlowMapping(trimmed);
        }
        if (trimmed.startsWith("[")) {
            pos[0]++;
            return parseFlowSequence(trimmed);
        }

        // Detect if this is a sequence or mapping
        if (trimmed.startsWith("- ") || trimmed.equals("-")) {
            return parseBlockSequence(lines, pos, indent, depth, anchors);
        }

        // Check if it's a mapping key
        int colonIdx = findMappingColon(trimmed);
        if (colonIdx >= 0) {
            return parseBlockMapping(lines, pos, indent, depth, anchors);
        }

        // Single scalar value
        pos[0]++;
        return convertScalar(trimmed);
    }

    /**
     * Parses a block mapping.
     * 解析块映射。
     */
    private Map<String, Object> parseBlockMapping(List<String> lines, int[] pos, int mappingIndent, int depth, Map<String, Object> anchors) {
        checkDepth(depth);
        Map<String, Object> map = new LinkedHashMap<>();
        Set<String> seenKeys = config.isAllowDuplicateKeys() ? null : new HashSet<>();

        while (pos[0] < lines.size()) {
            skipBlankAndCommentLines(lines, pos);
            if (pos[0] >= lines.size()) {
                break;
            }

            String line = lines.get(pos[0]);
            int indent = getIndent(line);

            // If de-indented, we've exited this mapping
            if (indent < mappingIndent) {
                break;
            }
            // Skip lines that are more indented (shouldn't happen at top level)
            if (indent > mappingIndent) {
                break;
            }

            String trimmed = line.trim();
            int colonIdx = findMappingColon(trimmed);

            if (colonIdx < 0) {
                // Not a mapping entry at this level -- could be a sequence item belonging to parent
                break;
            }

            String key = trimmed.substring(0, colonIdx).trim();
            // Remove quotes from key if present
            key = unquote(key);

            if (seenKeys != null && !seenKeys.add(key)) {
                throw new YmlParseException("Duplicate key: " + key, pos[0] + 1, 0);
            }

            String valueStr = trimmed.substring(colonIdx + 1).trim();

            // Remove inline comment from valueStr
            valueStr = removeInlineComment(valueStr);

            // Detect anchor definition on value: &anchorName value
            String anchorName = null;
            if (valueStr.startsWith("&")) {
                int spaceIdx = valueStr.indexOf(' ');
                if (spaceIdx > 0) {
                    anchorName = valueStr.substring(1, spaceIdx);
                    valueStr = valueStr.substring(spaceIdx + 1).trim();
                } else {
                    // The anchor is the entire value, meaning nested content follows
                    anchorName = valueStr.substring(1);
                    valueStr = "";
                }
            }

            if (valueStr.isEmpty()) {
                // Value is on next lines (nested mapping, sequence, or block scalar)
                pos[0]++;
                skipBlankAndCommentLines(lines, pos);
                if (pos[0] < lines.size()) {
                    String nextLine = lines.get(pos[0]);
                    int nextIndent = getIndent(nextLine);
                    if (nextIndent > mappingIndent) {
                        Object nestedValue = parseValue(lines, pos, mappingIndent, depth + 1, anchors);
                        if (anchorName != null) {
                            anchors.put(anchorName, nestedValue);
                        }
                        map.put(key, nestedValue);
                    } else {
                        map.put(key, null);
                    }
                } else {
                    map.put(key, null);
                }
            } else if (valueStr.startsWith("|") || valueStr.startsWith(">")) {
                // Block scalar
                boolean literal = valueStr.startsWith("|");
                String chomping = valueStr.length() > 1 ? valueStr.substring(1).trim() : "";
                pos[0]++;
                String scalar = parseBlockScalar(lines, pos, mappingIndent, literal, chomping);
                if (anchorName != null) {
                    anchors.put(anchorName, scalar);
                }
                map.put(key, scalar);
            } else if (valueStr.startsWith("{")) {
                // Inline flow mapping
                pos[0]++;
                Object flowMap = parseFlowMapping(valueStr);
                if (anchorName != null) {
                    anchors.put(anchorName, flowMap);
                }
                map.put(key, flowMap);
            } else if (valueStr.startsWith("[")) {
                // Inline flow sequence
                pos[0]++;
                Object flowSeq = parseFlowSequence(valueStr);
                if (anchorName != null) {
                    anchors.put(anchorName, flowSeq);
                }
                map.put(key, flowSeq);
            } else {
                // Inline scalar value
                pos[0]++;
                Object scalarValue = convertScalar(valueStr);
                // Check for invalid indentation: key with inline value
                // must not be followed by deeper-indented content
                int savedPos = pos[0];
                skipBlankAndCommentLines(lines, pos);
                if (pos[0] < lines.size()) {
                    int nextIndent = getIndent(lines.get(pos[0]));
                    if (nextIndent > mappingIndent) {
                        String nextTrimmed = lines.get(pos[0]).trim();
                        // A deeper-indented mapping or sequence after inline value is an error
                        if (findMappingColon(nextTrimmed) >= 0 ||
                            nextTrimmed.startsWith("- ") || nextTrimmed.equals("-")) {
                            throw new YmlParseException(
                                "Invalid indentation: key '" + key + "' has inline value but is followed by nested content",
                                pos[0] + 1, nextIndent);
                        }
                    }
                }
                pos[0] = savedPos;
                if (anchorName != null) {
                    anchors.put(anchorName, scalarValue);
                }
                map.put(key, scalarValue);
            }
        }

        return map;
    }

    /**
     * Parses a block sequence.
     * 解析块序列。
     */
    private List<Object> parseBlockSequence(List<String> lines, int[] pos, int seqIndent, int depth, Map<String, Object> anchors) {
        checkDepth(depth);
        List<Object> list = new ArrayList<>();

        while (pos[0] < lines.size()) {
            skipBlankAndCommentLines(lines, pos);
            if (pos[0] >= lines.size()) {
                break;
            }

            String line = lines.get(pos[0]);
            int indent = getIndent(line);

            if (indent < seqIndent) {
                break;
            }
            if (indent > seqIndent) {
                break;
            }

            String trimmed = line.trim();
            if (!trimmed.startsWith("- ") && !trimmed.equals("-")) {
                break;
            }

            // Get the value after "- "
            String itemValue = trimmed.equals("-") ? "" : trimmed.substring(2).trim();

            if (itemValue.isEmpty()) {
                // Value on next line(s)
                pos[0]++;
                skipBlankAndCommentLines(lines, pos);
                if (pos[0] < lines.size()) {
                    int nextIndent = getIndent(lines.get(pos[0]));
                    if (nextIndent > seqIndent) {
                        list.add(parseValue(lines, pos, seqIndent, depth + 1, anchors));
                    } else {
                        list.add(null);
                    }
                } else {
                    list.add(null);
                }
            } else {
                // Check if item value is itself a key: value (compact nested mapping in sequence)
                int colonIdx = findMappingColon(itemValue);
                if (colonIdx >= 0) {
                    // This is a compact mapping inside sequence, e.g., "- name: John"
                    // We need to parse a full mapping starting from this item
                    // Replace this line with just the key-value and let mapping parser handle it
                    int itemIndent = seqIndent + 2; // indent after "- "
                    // Temporarily modify the line to remove the "- " prefix
                    String originalLine = lines.get(pos[0]);
                    String spaces = " ".repeat(itemIndent);
                    lines.set(pos[0], spaces + itemValue);
                    Map<String, Object> nestedMap = parseBlockMapping(lines, pos, itemIndent, depth + 1, anchors);
                    // Restore original line (for safety, though pos has advanced)
                    // No need to restore since we've already advanced
                    list.add(nestedMap);
                } else if (itemValue.startsWith("{")) {
                    pos[0]++;
                    list.add(parseFlowMapping(itemValue));
                } else if (itemValue.startsWith("[")) {
                    pos[0]++;
                    list.add(parseFlowSequence(itemValue));
                } else if (itemValue.startsWith("|") || itemValue.startsWith(">")) {
                    boolean literal = itemValue.startsWith("|");
                    String chomping = itemValue.length() > 1 ? itemValue.substring(1).trim() : "";
                    pos[0]++;
                    list.add(parseBlockScalar(lines, pos, seqIndent, literal, chomping));
                } else {
                    pos[0]++;
                    list.add(convertScalar(itemValue));
                }
            }
        }

        return list;
    }

    /**
     * Parses a block scalar (literal | or folded >).
     * 解析块标量（字面量 | 或折叠 >）。
     */
    private String parseBlockScalar(List<String> lines, int[] pos, int parentIndent, boolean literal, String chomping) {
        if (pos[0] >= lines.size()) {
            return "";
        }

        // Determine the indent of the scalar content from the first non-blank line
        int contentIndent = -1;
        StringBuilder sb = new StringBuilder();
        List<String> contentLines = new ArrayList<>();

        while (pos[0] < lines.size()) {
            String line = lines.get(pos[0]);

            // Blank line within block scalar
            if (line.isBlank()) {
                contentLines.add("");
                pos[0]++;
                continue;
            }

            int lineIndent = getIndent(line);
            if (contentIndent == -1) {
                if (lineIndent <= parentIndent) {
                    break;
                }
                contentIndent = lineIndent;
            }

            if (lineIndent < contentIndent) {
                break;
            }

            contentLines.add(line.substring(Math.min(contentIndent, line.length())));
            pos[0]++;
        }

        // Remove trailing blank lines for proper chomping
        List<String> trailingBlanks = new ArrayList<>();
        while (!contentLines.isEmpty() && contentLines.getLast().isEmpty()) {
            trailingBlanks.add(contentLines.removeLast());
        }

        if (literal) {
            // Literal: preserve newlines
            sb.append(String.join("\n", contentLines));
        } else {
            // Folded: join lines with spaces, but empty lines become newlines
            boolean firstLine = true;
            for (String cl : contentLines) {
                if (cl.isEmpty()) {
                    sb.append("\n");
                    firstLine = true;
                } else {
                    if (!firstLine) {
                        sb.append(" ");
                    }
                    sb.append(cl);
                    firstLine = false;
                }
            }
        }

        // Apply chomping
        if (chomping.equals("-")) {
            // Strip: no trailing newline
        } else if (chomping.equals("+")) {
            // Keep: preserve all trailing newlines
            sb.append("\n");
            for (int i = 0; i < trailingBlanks.size(); i++) {
                sb.append("\n");
            }
        } else {
            // Default (clip): single trailing newline
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Parses a flow mapping: {key: value, key2: value2}.
     * 解析流式映射：{key: value, key2: value2}。
     */
    private Map<String, Object> parseFlowMapping(String text) {
        text = text.trim();
        if (text.startsWith("{")) {
            text = text.substring(1);
        }
        if (text.endsWith("}")) {
            text = text.substring(0, text.length() - 1);
        } else {
            throw new YmlParseException("Unclosed flow mapping: missing '}'");
        }
        text = text.trim();

        Map<String, Object> map = new LinkedHashMap<>();
        if (text.isEmpty()) {
            return map;
        }

        List<String> pairs = splitFlowItems(text);
        for (String pair : pairs) {
            pair = pair.trim();
            if (pair.isEmpty()) {
                continue;
            }
            int colonIdx = pair.indexOf(':');
            if (colonIdx < 0) {
                continue;
            }
            String key = unquote(pair.substring(0, colonIdx).trim());
            String value = pair.substring(colonIdx + 1).trim();
            if (value.startsWith("{")) {
                map.put(key, parseFlowMapping(value));
            } else if (value.startsWith("[")) {
                map.put(key, parseFlowSequence(value));
            } else {
                map.put(key, convertScalar(value));
            }
        }
        return map;
    }

    /**
     * Parses a flow sequence: [a, b, c].
     * 解析流式序列：[a, b, c]。
     */
    private List<Object> parseFlowSequence(String text) {
        text = text.trim();
        if (text.startsWith("[")) {
            text = text.substring(1);
        }
        if (text.endsWith("]")) {
            text = text.substring(0, text.length() - 1);
        } else {
            throw new YmlParseException("Unclosed flow sequence: missing ']'");
        }
        text = text.trim();

        List<Object> list = new ArrayList<>();
        if (text.isEmpty()) {
            return list;
        }

        List<String> items = splitFlowItems(text);
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) {
                continue;
            }
            if (item.startsWith("{")) {
                list.add(parseFlowMapping(item));
            } else if (item.startsWith("[")) {
                list.add(parseFlowSequence(item));
            } else {
                list.add(convertScalar(item));
            }
        }
        return list;
    }

    /**
     * Splits flow-style items respecting nested braces and brackets.
     * 拆分流式项目，尊重嵌套的大括号和方括号。
     */
    private List<String> splitFlowItems(String text) {
        List<String> items = new ArrayList<>();
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (inSingleQuote) {
                current.append(c);
                if (c == '\'') {
                    inSingleQuote = false;
                }
                continue;
            }
            if (inDoubleQuote) {
                current.append(c);
                if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                    inDoubleQuote = false;
                }
                continue;
            }

            if (c == '\'') {
                inSingleQuote = true;
                current.append(c);
            } else if (c == '"') {
                inDoubleQuote = true;
                current.append(c);
            } else if (c == '{' || c == '[') {
                depth++;
                current.append(c);
            } else if (c == '}' || c == ']') {
                depth--;
                current.append(c);
            } else if (c == ',' && depth == 0) {
                items.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            items.add(current.toString());
        }

        return items;
    }

    // ==================== Scalar Conversion | 标量转换 ====================

    /**
     * Converts a scalar string to the appropriate Java type.
     * 将标量字符串转换为适当的 Java 类型。
     */
    private Object convertScalar(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Remove inline comment
        value = removeInlineComment(value);

        // Reject bare colons that are syntactically ambiguous
        if (value.matches("^:+$") || value.matches("^:+\\s.*")) {
            throw new YmlParseException("Invalid scalar value: " + value);
        }

        // Quoted strings: return as-is (after unquoting)
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            return unquote(value);
        }

        // Null
        if (value.equals("null") || value.equals("~") || value.equals("Null") || value.equals("NULL")) {
            return null;
        }

        // Boolean
        if (!config.isStrictTypes()) {
            if (value.equals("true") || value.equals("True") || value.equals("TRUE") ||
                value.equals("yes") || value.equals("Yes") || value.equals("YES") ||
                value.equals("on") || value.equals("On") || value.equals("ON")) {
                return Boolean.TRUE;
            }
            if (value.equals("false") || value.equals("False") || value.equals("FALSE") ||
                value.equals("no") || value.equals("No") || value.equals("NO") ||
                value.equals("off") || value.equals("Off") || value.equals("OFF")) {
                return Boolean.FALSE;
            }
        } else {
            // Strict: only true/false
            if (value.equals("true") || value.equals("True") || value.equals("TRUE")) {
                return Boolean.TRUE;
            }
            if (value.equals("false") || value.equals("False") || value.equals("FALSE")) {
                return Boolean.FALSE;
            }
        }

        // Integer (decimal, hex, octal)
        if (value.matches("^-?\\d+$")) {
            try {
                long l = Long.parseLong(value);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                    return (int) l;
                }
                return l;
            } catch (NumberFormatException e) {
                // Fall through
            }
        }

        // Hex integer
        if (value.matches("^0x[0-9a-fA-F]+$")) {
            try {
                return Integer.parseInt(value.substring(2), 16);
            } catch (NumberFormatException e) {
                // Fall through
            }
        }

        // Octal integer
        if (value.matches("^0o[0-7]+$")) {
            try {
                return Integer.parseInt(value.substring(2), 8);
            } catch (NumberFormatException e) {
                // Fall through
            }
        }

        // Float/double
        if (value.matches("^-?\\d*\\.\\d+([eE][+-]?\\d+)?$") ||
            value.matches("^-?\\d+[eE][+-]?\\d+$")) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // Fall through
            }
        }

        // Special float values
        if (value.equals(".inf") || value.equals(".Inf") || value.equals(".INF")) {
            return Double.POSITIVE_INFINITY;
        }
        if (value.equals("-.inf") || value.equals("-.Inf") || value.equals("-.INF")) {
            return Double.NEGATIVE_INFINITY;
        }
        if (value.equals(".nan") || value.equals(".NaN") || value.equals(".NAN")) {
            return Double.NaN;
        }

        return value;
    }

    // ==================== Dump Helpers | 输出辅助 ====================

    /**
     * Dumps a value to the StringBuilder.
     * 将值输出到 StringBuilder。
     */
    @SuppressWarnings("unchecked")
    private void dumpValue(StringBuilder sb, Object value, int level, int indentSize, boolean inlineKey) {
        if (value == null) {
            sb.append("null\n");
            return;
        }

        if (value instanceof Map<?, ?> map) {
            dumpMap(sb, (Map<String, Object>) map, level, indentSize, inlineKey);
        } else if (value instanceof List<?> list) {
            dumpList(sb, list, level, indentSize, inlineKey);
        } else if (value instanceof String str) {
            dumpString(sb, str);
        } else if (value instanceof Boolean || value instanceof Number) {
            sb.append(value).append('\n');
        } else {
            // For arbitrary objects, convert to map first
            Map<String, Object> objMap = objectToMap(value);
            dumpMap(sb, objMap, level, indentSize, inlineKey);
        }
    }

    /**
     * Dumps a Map.
     * 输出映射。
     */
    private void dumpMap(StringBuilder sb, Map<String, Object> map, int level, int indentSize, boolean inlineKey) {
        if (map.isEmpty()) {
            sb.append("{}\n");
            return;
        }

        if (inlineKey) {
            sb.append('\n');
        }

        String indent = " ".repeat(level * indentSize);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(indent);
            String key = entry.getKey();
            // Quote key if necessary
            if (needsQuoting(key)) {
                sb.append('"').append(escapeString(key)).append('"');
            } else {
                sb.append(key);
            }
            sb.append(": ");

            Object val = entry.getValue();
            if (val instanceof Map<?, ?> || val instanceof List<?>) {
                if (val instanceof Map<?, ?> m && m.isEmpty()) {
                    sb.append("{}\n");
                } else if (val instanceof List<?> l && l.isEmpty()) {
                    sb.append("[]\n");
                } else {
                    dumpValue(sb, val, level + 1, indentSize, true);
                }
            } else {
                dumpValue(sb, val, level + 1, indentSize, false);
            }
        }
    }

    /**
     * Dumps a List.
     * 输出列表。
     */
    private void dumpList(StringBuilder sb, List<?> list, int level, int indentSize, boolean inlineKey) {
        if (list.isEmpty()) {
            sb.append("[]\n");
            return;
        }

        if (inlineKey) {
            sb.append('\n');
        }

        String indent = " ".repeat(level * indentSize);
        for (Object item : list) {
            sb.append(indent).append("- ");
            if (item instanceof Map<?, ?> || item instanceof List<?>) {
                if (item instanceof Map<?, ?> m && m.isEmpty()) {
                    sb.append("{}\n");
                } else if (item instanceof List<?> l && l.isEmpty()) {
                    sb.append("[]\n");
                } else {
                    dumpValue(sb, item, level + 1, indentSize, true);
                }
            } else {
                dumpValue(sb, item, level, indentSize, false);
            }
        }
    }

    /**
     * Dumps a string value, quoting if necessary.
     * 输出字符串值，必要时加引号。
     */
    private void dumpString(StringBuilder sb, String str) {
        if (str.contains("\n")) {
            // Use literal block scalar for multi-line strings
            sb.append("|\n");
            for (String line : str.split("\n", -1)) {
                sb.append("  ").append(line).append('\n');
            }
            return;
        }
        if (needsQuoting(str)) {
            sb.append('"').append(escapeString(str)).append('"').append('\n');
        } else {
            sb.append(str).append('\n');
        }
    }

    /**
     * Checks if a string needs quoting in YAML output.
     * 检查字符串在 YAML 输出中是否需要引号。
     */
    private boolean needsQuoting(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }
        // Reserved words
        if (str.equals("true") || str.equals("false") || str.equals("null") ||
            str.equals("True") || str.equals("False") || str.equals("Null") ||
            str.equals("TRUE") || str.equals("FALSE") || str.equals("NULL") ||
            str.equals("yes") || str.equals("no") || str.equals("Yes") || str.equals("No") ||
            str.equals("YES") || str.equals("NO") ||
            str.equals("on") || str.equals("off") || str.equals("On") || str.equals("Off") ||
            str.equals("ON") || str.equals("OFF") ||
            str.equals("~")) {
            return true;
        }
        // Looks like a number
        if (str.matches("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$")) {
            return true;
        }
        // Contains special characters
        if (str.contains(": ") || str.contains(" #") || str.contains("{") || str.contains("}") ||
            str.contains("[") || str.contains("]") || str.contains(",") ||
            str.contains("&") || str.contains("*") || str.contains("!") ||
            str.contains("|") || str.contains(">") || str.contains("'") ||
            str.contains("\"") || str.contains("%") || str.contains("@") ||
            str.contains("`") || str.startsWith("- ") || str.startsWith("? ")) {
            return true;
        }
        // Starts or ends with whitespace
        if (str.startsWith(" ") || str.startsWith("\t") ||
            str.endsWith(" ") || str.endsWith("\t")) {
            return true;
        }
        return false;
    }

    /**
     * Escapes a string for double-quoted YAML output.
     * 为双引号 YAML 输出转义字符串。
     */
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    // ==================== Utility Methods | 工具方法 ====================

    /**
     * Gets the indentation level (number of leading spaces) of a line.
     * 获取行的缩进级别（前导空格数）。
     */
    private int getIndent(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
                count++;
            } else if (line.charAt(i) == '\t') {
                throw new YmlParseException("Tabs are not allowed for indentation in YAML");
            } else {
                break;
            }
        }
        return count;
    }

    /**
     * Skips blank lines and comment lines.
     * 跳过空行和注释行。
     */
    private void skipBlankAndCommentLines(List<String> lines, int[] pos) {
        while (pos[0] < lines.size()) {
            String line = lines.get(pos[0]);
            if (line.isBlank() || line.trim().startsWith("#")) {
                pos[0]++;
            } else {
                break;
            }
        }
    }

    /**
     * Finds the colon separating key from value in a mapping, ignoring colons inside quotes.
     * 查找映射中分隔键和值的冒号，忽略引号内的冒号。
     *
     * @return the index of the colon, or -1 if not found
     */
    private int findMappingColon(String trimmed) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);

            if (inSingleQuote) {
                if (c == '\'') {
                    inSingleQuote = false;
                }
                continue;
            }
            if (inDoubleQuote) {
                if (c == '"' && (i == 0 || trimmed.charAt(i - 1) != '\\')) {
                    inDoubleQuote = false;
                }
                continue;
            }

            if (c == '\'') {
                inSingleQuote = true;
            } else if (c == '"') {
                inDoubleQuote = true;
            } else if (c == ':') {
                // Key: must be followed by space, end of string, or be at end
                if (i + 1 >= trimmed.length() || trimmed.charAt(i + 1) == ' ' || trimmed.charAt(i + 1) == '\t') {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Removes an inline comment (# preceded by space) from a value.
     * 从值中删除内联注释（空格后跟 #）。
     */
    private String removeInlineComment(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        // Don't strip from quoted strings
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))) {
            return value;
        }

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (inSingleQuote) {
                if (c == '\'') inSingleQuote = false;
                continue;
            }
            if (inDoubleQuote) {
                if (c == '"' && (i == 0 || value.charAt(i - 1) != '\\')) inDoubleQuote = false;
                continue;
            }
            if (c == '\'') {
                inSingleQuote = true;
            } else if (c == '"') {
                inDoubleQuote = true;
            } else if (c == '#' && i > 0 && value.charAt(i - 1) == ' ') {
                return value.substring(0, i - 1).trim();
            }
        }
        return value;
    }

    /**
     * Unquotes a string value.
     * 去除字符串值的引号。
     */
    private String unquote(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return unescapeDoubleQuoted(value.substring(1, value.length() - 1));
        }
        if (value.startsWith("'") && value.endsWith("'")) {
            // Single quoted: only '' is escaped to '
            return value.substring(1, value.length() - 1).replace("''", "'");
        }
        return value;
    }

    /**
     * Unescapes a double-quoted string.
     * 反转义双引号字符串。
     */
    private String unescapeDoubleQuoted(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    case '/' -> { sb.append('/'); i++; }
                    case '0' -> { sb.append('\0'); i++; }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Checks nesting depth against the security limit.
     * 检查嵌套深度是否超过安全限制。
     */
    private void checkDepth(int depth) {
        if (config.isSafeMode() && depth > config.getMaxNestingDepth()) {
            throw new YmlParseException(
                "Maximum nesting depth exceeded: " + depth + " > " + config.getMaxNestingDepth());
        }
    }

    // ==================== Object Binding | 对象绑定 ====================

    /**
     * Binds a map to a typed object.
     * 将映射绑定到类型化对象。
     */
    @SuppressWarnings("unchecked")
    private <T> T bindToObject(Map<String, Object> data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        if (clazz == Map.class || clazz == LinkedHashMap.class || clazz == HashMap.class) {
            return (T) data;
        }
        return cloud.opencode.base.yml.bind.YmlBinder.bind(data, clazz);
    }

    /**
     * Converts an arbitrary object to a Map for dumping.
     * 将任意对象转换为 Map 以进行输出。
     */
    private Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return new LinkedHashMap<>();
        }
        if (obj instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) map;
            return result;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        Class<?> clazz = obj.getClass();

        // Handle records
        if (clazz.isRecord()) {
            for (RecordComponent component : clazz.getRecordComponents()) {
                try {
                    Object value = component.getAccessor().invoke(obj);
                    if (value != null) {
                        result.put(component.getName(), value);
                    }
                } catch (ReflectiveOperationException e) {
                    // Skip inaccessible fields
                }
            }
            return result;
        }

        // Handle regular objects
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    result.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                // Skip inaccessible fields
            }
        }
        return result;
    }
}
