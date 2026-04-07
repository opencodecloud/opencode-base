package cloud.opencode.base.classloader.graalvm;

import cloud.opencode.base.classloader.scanner.ClassScanner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * GraalVM Native Image configuration file generator
 * GraalVM Native Image 配置文件生成器
 *
 * <p>Scans specified packages using {@link ClassScanner} and generates
 * GraalVM native image configuration files (reflect-config.json and
 * resource-config.json).</p>
 * <p>使用 {@link ClassScanner} 扫描指定包并生成 GraalVM Native Image
 * 配置文件（reflect-config.json 和 resource-config.json）。</p>
 *
 * <p><strong>Usage | 使用示例:</strong></p>
 * <pre>{@code
 * NativeImageConfigGenerator.builder()
 *     .addPackage("com.example.model")
 *     .addPackage("com.example.service")
 *     .addResourcePattern("config/.*")
 *     .addResourcePattern("templates/.*")
 *     .outputDir(Path.of("META-INF/native-image"))
 *     .generate();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (Builder is not shared across threads) - 线程安全: 是（Builder 不跨线程共享）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class NativeImageConfigGenerator {

    private static final String REFLECT_CONFIG_FILE = "reflect-config.json";
    private static final String RESOURCE_CONFIG_FILE = "resource-config.json";

    private final List<String> packages;
    private final List<String> resourcePatterns;
    private final Path outputDir;
    private final ClassLoader classLoader;

    private NativeImageConfigGenerator(Builder builder) {
        this.packages = List.copyOf(builder.packages);
        this.resourcePatterns = List.copyOf(builder.resourcePatterns);
        this.outputDir = builder.outputDir;
        this.classLoader = builder.classLoader;
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return new builder instance | 新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Generate native image configuration files
     * 生成 Native Image 配置文件
     *
     * <p>Scans all configured packages, creates reflect-config.json and
     * resource-config.json in the output directory.</p>
     * <p>扫描所有已配置的包，在输出目录中创建 reflect-config.json 和
     * resource-config.json。</p>
     *
     * @throws IOException          if an I/O error occurs | 如果发生 I/O 错误
     * @throws IllegalStateException if outputDir is not set | 如果未设置输出目录
     */
    public void generate() throws IOException {
        if (outputDir == null) {
            throw new IllegalStateException("Output directory must be set");
        }

        Files.createDirectories(outputDir);

        // Scan all packages and collect classes
        Set<Class<?>> allClasses = new LinkedHashSet<>();
        for (String pkg : packages) {
            ClassScanner scanner = ClassScanner.of(pkg);
            if (classLoader != null) {
                scanner.classLoader(classLoader);
            }
            allClasses.addAll(scanner.scan());
        }

        // Generate reflect-config.json
        List<ReflectConfig> reflectConfigs = new ArrayList<>();
        List<String> sortedClassNames = allClasses.stream()
                .map(Class::getName)
                .sorted()
                .toList();

        for (String className : sortedClassNames) {
            reflectConfigs.add(new ReflectConfig(
                    className, true, true, true, true
            ));
        }

        String reflectJson = buildReflectConfigJson(reflectConfigs);
        Files.writeString(
                outputDir.resolve(REFLECT_CONFIG_FILE),
                reflectJson,
                StandardCharsets.UTF_8
        );

        // Generate resource-config.json
        List<ResourceConfig.Pattern> patterns = resourcePatterns.stream()
                .map(ResourceConfig.Pattern::new)
                .toList();

        ResourceConfig resourceConfig = new ResourceConfig(patterns);
        Files.writeString(
                outputDir.resolve(RESOURCE_CONFIG_FILE),
                resourceConfig.toJson(),
                StandardCharsets.UTF_8
        );
    }

    /**
     * Build reflect-config.json content as a JSON array
     * 构建 reflect-config.json 内容为 JSON 数组
     *
     * @param configs list of reflect configurations | 反射配置列表
     * @return JSON array string | JSON 数组字符串
     */
    private static String buildReflectConfigJson(List<ReflectConfig> configs) {
        StringBuilder sb = new StringBuilder(configs.size() * 128);
        sb.append('[');
        for (int i = 0; i < configs.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(configs.get(i).toJson());
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Builder for NativeImageConfigGenerator
     * NativeImageConfigGenerator 的构建器
     *
     * <p><strong>Security | 安全性:</strong></p>
     * <ul>
     *   <li>Thread-safe: No (intended for single-thread construction) - 线程安全: 否（设计为单线程构建）</li>
     * </ul>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-classloader V1.0.3
     */
    public static final class Builder {

        private final List<String> packages = new ArrayList<>();
        private final List<String> resourcePatterns = new ArrayList<>();
        private Path outputDir;
        private ClassLoader classLoader;

        private Builder() {
        }

        /**
         * Add a package to scan for reflection configuration
         * 添加要扫描反射配置的包
         *
         * @param packageName package name | 包名
         * @return this builder | 此构建器
         * @throws NullPointerException if packageName is null | 如果包名为 null 抛出异常
         */
        public Builder addPackage(String packageName) {
            Objects.requireNonNull(packageName, "Package name must not be null");
            packages.add(packageName);
            return this;
        }

        /**
         * Add a resource pattern for resource-config.json
         * 添加资源模式到 resource-config.json
         *
         * @param pattern resource glob pattern | 资源匹配模式
         * @return this builder | 此构建器
         * @throws NullPointerException if pattern is null | 如果模式为 null 抛出异常
         */
        public Builder addResourcePattern(String pattern) {
            Objects.requireNonNull(pattern, "Resource pattern must not be null");
            resourcePatterns.add(pattern);
            return this;
        }

        /**
         * Set the output directory for generated files
         * 设置生成文件的输出目录
         *
         * @param outputDir output directory path | 输出目录路径
         * @return this builder | 此构建器
         * @throws NullPointerException if outputDir is null | 如果输出目录为 null 抛出异常
         */
        public Builder outputDir(Path outputDir) {
            Objects.requireNonNull(outputDir, "Output directory must not be null");
            this.outputDir = outputDir;
            return this;
        }

        /**
         * Set the class loader used for scanning
         * 设置用于扫描的类加载器
         *
         * @param classLoader class loader | 类加载器
         * @return this builder | 此构建器
         */
        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Build and execute the generation
         * 构建并执行生成
         *
         * @throws IOException if an I/O error occurs | 如果发生 I/O 错误
         */
        public void generate() throws IOException {
            new NativeImageConfigGenerator(this).generate();
        }

        /**
         * Build the generator without executing
         * 构建生成器但不执行
         *
         * @return configured generator | 已配置的生成器
         */
        public NativeImageConfigGenerator build() {
            Objects.requireNonNull(outputDir, "Output directory must be set before building");
            return new NativeImageConfigGenerator(this);
        }
    }
}
