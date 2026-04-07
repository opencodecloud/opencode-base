package cloud.opencode.base.core.process;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ProcessConfig - Configuration for process execution
 * ProcessConfig - 进程执行配置
 *
 * <p>Immutable configuration object built via the {@link Builder} pattern.
 * Controls command, working directory, environment variables, timeout,
 * stream redirection, and IO inheritance.</p>
 * <p>通过 {@link Builder} 模式构建的不可变配置对象。
 * 控制命令、工作目录、环境变量、超时时间、流重定向和 IO 继承。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ProcessConfig config = ProcessConfig.builder("ls", "-la")
 *         .workingDirectory(Path.of("/tmp"))
 *         .environment("LANG", "en_US.UTF-8")
 *         .timeout(Duration.ofSeconds(30))
 *         .build();
 *
 * ProcessResult result = ProcessManager.execute(config);
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong> Instances are immutable and thread-safe.
 * 实例是不可变的，线程安全。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ProcessManager
 * @see Builder
 * @since JDK 25, opencode-base-core V1.0.3
 */
public final class ProcessConfig {

    private final List<String> command;
    private final Path workingDirectory;
    private final Map<String, String> environment;
    private final Duration timeout;
    private final boolean redirectErrorStream;
    private final Path stdoutFile;
    private final Path stderrFile;
    private final boolean inheritIO;

    private ProcessConfig(Builder builder) {
        this.command = List.copyOf(builder.command);
        this.workingDirectory = builder.workingDirectory;
        this.environment = builder.environment.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(builder.environment));
        this.timeout = builder.timeout;
        this.redirectErrorStream = builder.redirectErrorStream;
        this.stdoutFile = builder.stdoutFile;
        this.stderrFile = builder.stderrFile;
        this.inheritIO = builder.inheritIO;
    }

    // ==================== Builder Factory | 构建器工厂 ====================

    /**
     * Creates a new builder with the given command.
     * 以给定命令创建新的构建器。
     *
     * @param command the command and arguments - 命令和参数
     * @return a new builder - 新的构建器
     * @throws IllegalArgumentException if command is empty - 若命令为空
     */
    public static Builder builder(String... command) {
        return new Builder(List.of(command));
    }

    /**
     * Creates a new builder with the given command list.
     * 以给定命令列表创建新的构建器。
     *
     * @param command the command and arguments - 命令和参数
     * @return a new builder - 新的构建器
     * @throws IllegalArgumentException if command is empty - 若命令为空
     */
    public static Builder builder(List<String> command) {
        return new Builder(new ArrayList<>(command));
    }

    // ==================== Getters | 访问器 ====================

    /**
     * Returns the command and arguments.
     * 返回命令和参数。
     *
     * @return unmodifiable command list - 不可修改的命令列表
     */
    public List<String> command() {
        return command;
    }

    /**
     * Returns the working directory, or {@code null} to inherit from the parent process.
     * 返回工作目录，若为 {@code null} 则继承父进程。
     *
     * @return the working directory, or null - 工作目录，或 null
     */
    public Path workingDirectory() {
        return workingDirectory;
    }

    /**
     * Returns additional environment variables.
     * 返回附加环境变量。
     *
     * @return unmodifiable environment map - 不可修改的环境变量映射
     */
    public Map<String, String> environment() {
        return environment;
    }

    /**
     * Returns the execution timeout, or {@code null} for no timeout.
     * 返回执行超时时间，若为 {@code null} 则无超时。
     *
     * @return the timeout, or null - 超时时间，或 null
     */
    public Duration timeout() {
        return timeout;
    }

    /**
     * Returns whether stderr should be merged into stdout.
     * 返回是否将 stderr 合并到 stdout。
     *
     * @return true if error stream is redirected - 若错误流被重定向则为 true
     */
    public boolean redirectErrorStream() {
        return redirectErrorStream;
    }

    /**
     * Returns the file to redirect stdout to, or {@code null} for in-memory capture.
     * 返回 stdout 重定向的文件，若为 {@code null} 则在内存中捕获。
     *
     * @return stdout file, or null - stdout 文件，或 null
     */
    public Path stdoutFile() {
        return stdoutFile;
    }

    /**
     * Returns the file to redirect stderr to, or {@code null} for in-memory capture.
     * 返回 stderr 重定向的文件，若为 {@code null} 则在内存中捕获。
     *
     * @return stderr file, or null - stderr 文件，或 null
     */
    public Path stderrFile() {
        return stderrFile;
    }

    /**
     * Returns whether the process should inherit the parent's IO.
     * 返回进程是否继承父进程的 IO。
     *
     * @return true if inheriting IO - 若继承 IO 则为 true
     */
    public boolean inheritIO() {
        return inheritIO;
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for {@link ProcessConfig}.
     * {@link ProcessConfig} 的构建器。
     *
     * @since JDK 25, opencode-base-core V1.0.3
     */
    public static final class Builder {

        private List<String> command;
        private Path workingDirectory;
        private final Map<String, String> environment = new LinkedHashMap<>();
        private Duration timeout;
        private boolean redirectErrorStream;
        private Path stdoutFile;
        private Path stderrFile;
        private boolean inheritIO;

        private Builder(List<String> command) {
            if (command == null || command.isEmpty()) {
                throw new IllegalArgumentException("Command must not be empty | 命令不能为空");
            }
            this.command = new ArrayList<>(command);
        }

        /**
         * Sets the command and arguments.
         * 设置命令和参数。
         *
         * @param command the command and arguments - 命令和参数
         * @return this builder - 此构建器
         */
        public Builder command(String... command) {
            Objects.requireNonNull(command, "command must not be null");
            this.command = new ArrayList<>(List.of(command));
            return this;
        }

        /**
         * Sets the command and arguments.
         * 设置命令和参数。
         *
         * @param command the command list - 命令列表
         * @return this builder - 此构建器
         */
        public Builder command(List<String> command) {
            Objects.requireNonNull(command, "command must not be null");
            this.command = new ArrayList<>(command);
            return this;
        }

        /**
         * Sets the working directory for the process.
         * 设置进程的工作目录。
         *
         * @param dir the working directory, or null to inherit - 工作目录，或 null 以继承
         * @return this builder - 此构建器
         */
        public Builder workingDirectory(Path dir) {
            this.workingDirectory = dir;
            return this;
        }

        /**
         * Adds a single environment variable.
         * 添加单个环境变量。
         *
         * <p><strong>Security Warning | 安全警告:</strong> Do not pass untrusted input as
         * environment variable names or values. Dangerous variables like {@code LD_PRELOAD},
         * {@code PATH}, or {@code DYLD_INSERT_LIBRARIES} can lead to code execution.</p>
         * <p><strong>安全警告:</strong> 切勿将不可信输入作为环境变量名或值传入。
         * {@code LD_PRELOAD}、{@code PATH} 等危险变量可导致代码执行。</p>
         *
         * @param key   the variable name - 变量名
         * @param value the variable value - 变量值
         * @return this builder - 此构建器
         */
        public Builder environment(String key, String value) {
            Objects.requireNonNull(key, "environment key must not be null");
            Objects.requireNonNull(value, "environment value must not be null");
            this.environment.put(key, value);
            return this;
        }

        /**
         * Adds multiple environment variables.
         * 添加多个环境变量。
         *
         * @param env the environment variables - 环境变量
         * @return this builder - 此构建器
         */
        public Builder environment(Map<String, String> env) {
            Objects.requireNonNull(env, "environment map must not be null");
            this.environment.putAll(env);
            return this;
        }

        /**
         * Sets the execution timeout.
         * 设置执行超时时间。
         *
         * @param timeout the timeout duration, or null for no timeout - 超时时间，或 null 表示无超时
         * @return this builder - 此构建器
         */
        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Sets the execution timeout.
         * 设置执行超时时间。
         *
         * @param amount the timeout amount - 超时数量
         * @param unit   the time unit - 时间单位
         * @return this builder - 此构建器
         */
        public Builder timeout(long amount, TimeUnit unit) {
            Objects.requireNonNull(unit, "unit must not be null");
            this.timeout = Duration.ofMillis(unit.toMillis(amount));
            return this;
        }

        /**
         * Sets whether stderr should be merged into stdout.
         * 设置是否将 stderr 合并到 stdout。
         *
         * @param redirect true to merge stderr into stdout - 若为 true 则合并 stderr 到 stdout
         * @return this builder - 此构建器
         */
        public Builder redirectErrorStream(boolean redirect) {
            this.redirectErrorStream = redirect;
            return this;
        }

        /**
         * Redirects stdout to a file.
         * 将 stdout 重定向到文件。
         *
         * @param file the target file, or null for in-memory capture - 目标文件，或 null 以在内存中捕获
         * @return this builder - 此构建器
         */
        public Builder stdoutFile(Path file) {
            this.stdoutFile = file;
            return this;
        }

        /**
         * Redirects stderr to a file.
         * 将 stderr 重定向到文件。
         *
         * @param file the target file, or null for in-memory capture - 目标文件，或 null 以在内存中捕获
         * @return this builder - 此构建器
         */
        public Builder stderrFile(Path file) {
            this.stderrFile = file;
            return this;
        }

        /**
         * Sets whether the process should inherit the parent's IO streams.
         * 设置进程是否继承父进程的 IO 流。
         *
         * <p>When enabled, stdout/stderr will not be captured in memory.</p>
         * <p>启用后，stdout/stderr 将不会在内存中捕获。</p>
         *
         * @param inherit true to inherit IO - 若为 true 则继承 IO
         * @return this builder - 此构建器
         */
        public Builder inheritIO(boolean inherit) {
            this.inheritIO = inherit;
            return this;
        }

        /**
         * Builds the {@link ProcessConfig}.
         * 构建 {@link ProcessConfig}。
         *
         * @return a new immutable ProcessConfig - 新的不可变 ProcessConfig
         * @throws IllegalArgumentException if command is empty - 若命令为空
         */
        public ProcessConfig build() {
            if (command == null || command.isEmpty()) {
                throw new IllegalArgumentException("Command must not be empty | 命令不能为空");
            }
            return new ProcessConfig(this);
        }
    }

    @Override
    public String toString() {
        return "ProcessConfig{command=" + command
                + ", workingDirectory=" + workingDirectory
                + ", timeout=" + timeout
                + ", redirectErrorStream=" + redirectErrorStream
                + ", inheritIO=" + inheritIO
                + "}";
    }
}
