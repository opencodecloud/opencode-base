package cloud.opencode.base.classloader.dependency;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class Dependency Analyzer - Bytecode-level dependency analysis utility
 * 类依赖分析器 - 字节码级别的依赖分析工具
 *
 * <p>Parses the constant pool of Java class files to extract dependency
 * information without loading the classes. Uses Tarjan's algorithm to
 * detect cyclic dependencies in dependency graphs.</p>
 * <p>解析 Java 类文件的常量池以提取依赖信息，无需加载类。
 * 使用 Tarjan 算法检测依赖图中的循环依赖。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Analyze class dependencies from bytecode - 从字节码分析类依赖</li>
 *   <li>Build dependency graphs for packages - 为包构建依赖图</li>
 *   <li>Detect cyclic dependencies using Tarjan's SCC algorithm - 使用 Tarjan SCC 算法检测循环依赖</li>
 *   <li>No class loading required — reads raw bytecode only - 无需加载类，仅读取原始字节码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Analyze a single class
 * Set<String> deps = ClassDependencyAnalyzer.analyze(
 *     "com.example.MyClass", Thread.currentThread().getContextClassLoader());
 *
 * // Analyze from bytecode
 * byte[] bytecode = Files.readAllBytes(Path.of("MyClass.class"));
 * Set<String> deps = ClassDependencyAnalyzer.analyze(bytecode);
 *
 * // Detect cycles
 * DependencyGraph graph = ClassDependencyAnalyzer.analyzePackage(
 *     "com.example", classLoader);
 * List<CyclicDependency> cycles = ClassDependencyAnalyzer.detectCycles(graph);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>No class loading — only bytecode reading - 不加载类，仅读取字节码</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see DependencyGraph
 * @see CyclicDependency
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-classloader V1.0.3
 */
public final class ClassDependencyAnalyzer {

    private static final System.Logger LOGGER = System.getLogger(ClassDependencyAnalyzer.class.getName());

    /** Class file magic number | 类文件魔数 */
    private static final int CLASS_MAGIC = 0xCAFEBABE;

    // Constant pool tags | 常量池标签
    private static final int TAG_UTF8 = 1;
    private static final int TAG_INTEGER = 3;
    private static final int TAG_FLOAT = 4;
    private static final int TAG_LONG = 5;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_CLASS = 7;
    private static final int TAG_STRING = 8;
    private static final int TAG_FIELDREF = 9;
    private static final int TAG_METHODREF = 10;
    private static final int TAG_INTERFACE_METHODREF = 11;
    private static final int TAG_NAME_AND_TYPE = 12;
    private static final int TAG_METHOD_HANDLE = 15;
    private static final int TAG_METHOD_TYPE = 16;
    private static final int TAG_DYNAMIC = 17;
    private static final int TAG_INVOKE_DYNAMIC = 18;
    private static final int TAG_MODULE = 19;
    private static final int TAG_PACKAGE = 20;

    /** Prefixes to exclude from dependency results | 从依赖结果中排除的前缀 */
    private static final String[] EXCLUDED_PREFIXES = {"java.", "javax.", "jdk."};

    private ClassDependencyAnalyzer() {
        // Utility class — no instantiation | 工具类，禁止实例化
    }

    /**
     * Analyze the dependencies of a class by reading its bytecode from the ClassLoader.
     * 通过从类加载器读取字节码来分析类的依赖。
     *
     * @param className   the fully qualified class name | 完全限定类名
     * @param classLoader the ClassLoader to read bytecode from | 用于读取字节码的类加载器
     * @return set of dependency class names (excluding java.*, javax.*, jdk.*, arrays, and self)
     *         | 依赖类名集合（排除 java.*、javax.*、jdk.*、数组类型和自身）
     * @throws NullPointerException if any parameter is null | 如果参数为 null 则抛出空指针异常
     * @throws IOException          if the class bytecode cannot be read | 如果无法读取类字节码
     */
    public static Set<String> analyze(String className, ClassLoader classLoader) throws IOException {
        Objects.requireNonNull(className, "className must not be null | className 不能为 null");
        Objects.requireNonNull(classLoader, "classLoader must not be null | classLoader 不能为 null");

        String resourcePath = className.replace('.', '/') + ".class";
        try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Class resource not found: {0}", resourcePath);
                return Set.of();
            }
            byte[] bytecode = is.readAllBytes();
            return analyzeInternal(bytecode, className);
        }
    }

    /**
     * Analyze the dependencies of a class from raw bytecode.
     * 从原始字节码分析类的依赖。
     *
     * @param bytecode the class file bytecode | 类文件字节码
     * @return set of dependency class names (excluding java.*, javax.*, jdk.*, and arrays)
     *         | 依赖类名集合（排除 java.*、javax.*、jdk.* 和数组类型）
     * @throws NullPointerException if bytecode is null | 如果字节码为 null 则抛出空指针异常
     * @throws IOException          if the bytecode is malformed | 如果字节码格式错误
     */
    public static Set<String> analyze(byte[] bytecode) throws IOException {
        Objects.requireNonNull(bytecode, "bytecode must not be null | bytecode 不能为 null");
        return analyzeInternal(bytecode, null);
    }

    /**
     * Analyze all classes in a package and build a dependency graph.
     * 分析包中的所有类并构建依赖图。
     *
     * <p>Note: This method requires the ClassLoader to provide
     * a resource listing for the package directory. Not all ClassLoaders
     * support this.</p>
     * <p>注意：此方法要求类加载器为包目录提供资源列表。
     * 并非所有类加载器都支持此功能。</p>
     *
     * @param packageName the package name | 包名
     * @param classLoader the ClassLoader to scan | 要扫描的类加载器
     * @return the dependency graph for all discovered classes | 所有发现的类的依赖图
     * @throws NullPointerException if any parameter is null | 如果参数为 null 则抛出空指针异常
     * @throws IOException          if bytecode cannot be read | 如果无法读取字节码
     */
    public static DependencyGraph analyzePackage(String packageName, ClassLoader classLoader) throws IOException {
        Objects.requireNonNull(packageName, "packageName must not be null | packageName 不能为 null");
        Objects.requireNonNull(classLoader, "classLoader must not be null | classLoader 不能为 null");

        String packagePath = packageName.replace('.', '/');
        Map<String, Set<String>> adjacency = new LinkedHashMap<>();
        int edgeCount = 0;

        // Discover class names in the package via getResources() + URL scanning
        Set<String> classNames = discoverClassesInPackage(packagePath, packageName, classLoader);

        if (classNames.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "No classes found in package: {0}", packageName);
            return new DependencyGraph(Map.of(), 0, 0);
        }

        for (String className : classNames) {
            try {
                Set<String> deps = analyze(className, classLoader);
                adjacency.put(className, deps);
                edgeCount += deps.size();
            } catch (IOException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to analyze class {0}: {1}", className, e.getMessage());
            }
        }

        return new DependencyGraph(adjacency, adjacency.size(), edgeCount);
    }

    /**
     * Discover class names in a package by scanning classpath URLs.
     * 通过扫描类路径 URL 发现包中的类名。
     */
    private static Set<String> discoverClassesInPackage(String packagePath, String packageName,
                                                         ClassLoader classLoader) throws IOException {
        Set<String> classNames = new LinkedHashSet<>();
        Enumeration<URL> resources = classLoader.getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            String protocol = url.getProtocol();

            if ("file".equals(protocol)) {
                Path dir;
                try {
                    dir = Path.of(url.toURI());
                } catch (URISyntaxException e) {
                    continue;
                }
                if (Files.isDirectory(dir)) {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.class")) {
                        for (Path entry : stream) {
                            String fileName = entry.getFileName().toString();
                            if (!fileName.contains("$") && !fileName.equals("module-info.class")) {
                                String className = packageName + "." +
                                        fileName.substring(0, fileName.length() - ".class".length());
                                classNames.add(className);
                            }
                        }
                    }
                }
            } else if ("jar".equals(protocol)) {
                JarURLConnection conn = (JarURLConnection) url.openConnection();
                conn.setUseCaches(false);
                String prefix = packagePath + "/";
                try (JarFile jarFile = conn.getJarFile()) {
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(prefix) && name.endsWith(".class")
                                && !name.contains("$") && !name.endsWith("module-info.class")) {
                            // Only direct children (no sub-packages)
                            String relative = name.substring(prefix.length());
                            if (!relative.contains("/")) {
                                String className = packageName + "." +
                                        relative.substring(0, relative.length() - ".class".length());
                                classNames.add(className);
                            }
                        }
                    }
                }
            }
        }
        return classNames;
    }

    /**
     * Detect cyclic dependencies in a dependency graph using Tarjan's SCC algorithm.
     * 使用 Tarjan SCC 算法检测依赖图中的循环依赖。
     *
     * @param graph the dependency graph to analyze | 要分析的依赖图
     * @return list of cyclic dependencies (strongly connected components of size &gt; 1)
     *         | 循环依赖列表（大小大于 1 的强连通分量）
     * @throws NullPointerException if graph is null | 如果图为 null 则抛出空指针异常
     */
    public static List<CyclicDependency> detectCycles(DependencyGraph graph) {
        Objects.requireNonNull(graph, "graph must not be null | graph 不能为 null");
        return new TarjanSCC(graph).findCycles();
    }

    /**
     * Internal bytecode analysis — parses constant pool for CONSTANT_Class entries.
     * 内部字节码分析 — 解析常量池中的 CONSTANT_Class 条目。
     *
     * @param bytecode  the raw bytecode | 原始字节码
     * @param selfClass the class name to exclude (nullable) | 要排除的自身类名（可为 null）
     * @return set of dependency class names | 依赖类名集合
     * @throws IOException if bytecode is malformed | 如果字节码格式错误
     */
    private static Set<String> analyzeInternal(byte[] bytecode, String selfClass) throws IOException {
        try (DataInputStream dis = new DataInputStream(new java.io.ByteArrayInputStream(bytecode))) {
            int magic = dis.readInt();
            if (magic != CLASS_MAGIC) {
                throw new IOException("Invalid class file: bad magic number 0x" +
                        Integer.toHexString(magic));
            }

            // Skip minor and major version | 跳过次版本号和主版本号
            dis.readUnsignedShort(); // minor
            dis.readUnsignedShort(); // major

            int constantPoolCount = dis.readUnsignedShort();
            if (constantPoolCount == 0) {
                throw new IOException("Invalid class file: constant pool count is 0");
            }

            // Storage for constant pool entries | 常量池条目存储
            String[] utf8Entries = new String[constantPoolCount];
            int[] classNameIndices = new int[constantPoolCount];
            boolean[] isClassEntry = new boolean[constantPoolCount];

            // Parse constant pool (index 1 to constantPoolCount-1) | 解析常量池
            for (int i = 1; i < constantPoolCount; i++) {
                int tag = dis.readUnsignedByte();
                switch (tag) {
                    case TAG_UTF8 -> {
                        utf8Entries[i] = dis.readUTF();
                    }
                    case TAG_CLASS -> {
                        classNameIndices[i] = dis.readUnsignedShort();
                        isClassEntry[i] = true;
                    }
                    case TAG_INTEGER, TAG_FLOAT -> {
                        dis.skipNBytes(4);
                    }
                    case TAG_LONG, TAG_DOUBLE -> {
                        dis.skipNBytes(8);
                        i++; // Long and Double take two constant pool entries
                    }
                    case TAG_STRING, TAG_METHOD_TYPE, TAG_MODULE, TAG_PACKAGE -> {
                        dis.skipNBytes(2);
                    }
                    case TAG_FIELDREF, TAG_METHODREF, TAG_INTERFACE_METHODREF,
                         TAG_NAME_AND_TYPE, TAG_DYNAMIC, TAG_INVOKE_DYNAMIC -> {
                        dis.skipNBytes(4);
                    }
                    case TAG_METHOD_HANDLE -> {
                        dis.skipNBytes(3);
                    }
                    default -> {
                        // Unknown tag — cannot determine its byte length, so parsing
                        // cannot continue safely. Abort to prevent offset drift.
                        throw new IOException("Unknown constant pool tag " + tag +
                                " at index " + i + "; cannot determine entry size");
                    }
                }
            }

            // Resolve class entries to names | 将类条目解析为名称
            Set<String> dependencies = new LinkedHashSet<>();
            for (int i = 1; i < constantPoolCount; i++) {
                if (isClassEntry[i]) {
                    int nameIndex = classNameIndices[i];
                    if (nameIndex > 0 && nameIndex < constantPoolCount && utf8Entries[nameIndex] != null) {
                        String internalName = utf8Entries[nameIndex];

                        // Skip array types | 跳过数组类型
                        if (internalName.startsWith("[")) {
                            continue;
                        }

                        // Convert internal form to dot form | 将内部形式转换为点形式
                        String dotName = internalName.replace('/', '.');

                        // Skip excluded prefixes | 跳过排除的前缀
                        if (isExcluded(dotName)) {
                            continue;
                        }

                        // Skip self | 跳过自身
                        if (dotName.equals(selfClass)) {
                            continue;
                        }

                        dependencies.add(dotName);
                    }
                }
            }

            return Collections.unmodifiableSet(dependencies);
        }
    }

    /**
     * Check if a class name should be excluded from results.
     * 检查类名是否应从结果中排除。
     *
     * @param className the class name | 类名
     * @return true if the class should be excluded | 如果应排除该类则返回 true
     */
    private static boolean isExcluded(String className) {
        for (String prefix : EXCLUDED_PREFIXES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Iterative Tarjan's Strongly Connected Components algorithm implementation.
     * 迭代式 Tarjan 强连通分量算法实现。
     *
     * <p>Uses an explicit stack to avoid StackOverflowError on large dependency graphs.</p>
     * <p>使用显式栈以避免在大型依赖图上出现 StackOverflowError。</p>
     */
    private static final class TarjanSCC {

        private final DependencyGraph graph;
        private final Set<String> graphNodes;
        private final Map<String, Integer> indexMap = new HashMap<>();
        private final Map<String, Integer> lowlinkMap = new HashMap<>();
        private final Set<String> onStack = new HashSet<>();
        private final Deque<String> sccStack = new ArrayDeque<>();
        private final List<CyclicDependency> cycles = new ArrayList<>();
        private int index = 0;

        TarjanSCC(DependencyGraph graph) {
            this.graph = graph;
            this.graphNodes = graph.classNames();
        }

        /**
         * Find all cyclic dependencies (SCCs of size &gt; 1).
         * 查找所有循环依赖（大小大于 1 的强连通分量）。
         *
         * @return list of cyclic dependencies | 循环依赖列表
         */
        List<CyclicDependency> findCycles() {
            for (String node : graphNodes) {
                if (!indexMap.containsKey(node)) {
                    strongConnectIterative(node);
                }
            }
            return Collections.unmodifiableList(cycles);
        }

        /**
         * Iterative Tarjan's strongconnect using explicit call stack.
         * 使用显式调用栈的迭代式 Tarjan strongconnect。
         *
         * @param root the root node to start from | 起始根节点
         */
        private void strongConnectIterative(String root) {
            // Simulated call stack frame: (node, neighbor iterator, phase)
            record Frame(String v, Iterator<String> neighbors) {}
            Deque<Frame> callStack = new ArrayDeque<>();

            // Initialize root
            indexMap.put(root, index);
            lowlinkMap.put(root, index);
            index++;
            sccStack.push(root);
            onStack.add(root);

            callStack.push(new Frame(root, filteredNeighbors(root)));

            while (!callStack.isEmpty()) {
                Frame frame = callStack.peek();
                String v = frame.v();

                if (frame.neighbors().hasNext()) {
                    String w = frame.neighbors().next();
                    if (!indexMap.containsKey(w)) {
                        // "Recurse" into w: initialize it and push a new frame
                        indexMap.put(w, index);
                        lowlinkMap.put(w, index);
                        index++;
                        sccStack.push(w);
                        onStack.add(w);
                        callStack.push(new Frame(w, filteredNeighbors(w)));
                    } else if (onStack.contains(w)) {
                        lowlinkMap.put(v, Math.min(lowlinkMap.get(v), indexMap.get(w)));
                    }
                } else {
                    // All neighbors processed — "return" from this frame
                    callStack.pop();

                    // Update parent's lowlink (simulates the post-recursion update)
                    if (!callStack.isEmpty()) {
                        String parent = callStack.peek().v();
                        lowlinkMap.put(parent, Math.min(lowlinkMap.get(parent), lowlinkMap.get(v)));
                    }

                    // If v is a root node, pop the SCC
                    if (lowlinkMap.get(v).equals(indexMap.get(v))) {
                        List<String> component = new ArrayList<>();
                        String w;
                        do {
                            w = sccStack.pop();
                            onStack.remove(w);
                            component.add(w);
                        } while (!w.equals(v));

                        if (component.size() > 1) {
                            Collections.reverse(component);
                            cycles.add(new CyclicDependency(component));
                        }
                    }
                }
            }
        }

        /**
         * Get filtered neighbors (only nodes present in the graph).
         * 获取过滤后的邻居节点（仅图中存在的节点）。
         */
        private Iterator<String> filteredNeighbors(String v) {
            return graph.dependenciesOf(v).stream()
                    .filter(graphNodes::contains)
                    .iterator();
        }
    }
}
