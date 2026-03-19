# OpenCode Base YML

**适用于 Java 25+ 的 YAML 处理库，支持 SPI 架构**

`opencode-base-yml` 提供了一套完整的 YAML 处理工具包，包括解析、写入、对象绑定、文档合并、占位符解析、路径查询和安全特性。采用基于 SPI 的提供者架构，可选集成 SnakeYAML。

## 功能特性

### 核心功能
- **YAML 解析**：将 YAML 字符串、文件和流解析为 Map 或文档
- **YAML 写入**：将对象和 Map 转储为 YAML 字符串或文件
- **多文档**：加载和转储多文档 YAML
- **验证**：检查 YAML 字符串有效性

### 高级功能
- **对象绑定**：将 YAML 文档绑定到 Java 对象，支持注解
- **文档合并**：使用可配置策略合并多个 YAML 文档
- **占位符解析**：解析 `${...}` 占位符，支持系统属性、环境变量或自定义属性
- **路径查询**：使用点号分隔路径导航 YAML 文档
- **节点树**：将 YAML 解析为可导航的节点树
- **SPI 架构**：通过 `YmlProvider` SPI 实现可插拔的 YAML 提供者
- **SnakeYAML 集成**：可选的 SnakeYAML 后端，完整支持 YAML 1.1
- **安全**：安全的 YAML 加载，支持类型限制和深度限制

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-yml</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.yml.*;

// 解析 YAML
Map<String, Object> data = OpenYml.load("server:\n  port: 8080");
YmlDocument doc = OpenYml.parse("server:\n  port: 8080");

// 从文件加载
YmlDocument doc = OpenYml.loadFile(Path.of("config.yml"));

// 通过路径访问值
String port = doc.getString("server.port");
int timeout = doc.getInt("server.timeout", 30);

// 写入 YAML
String yaml = OpenYml.dump(Map.of("name", "test", "version", "1.0"));
OpenYml.writeFile(data, Path.of("output.yml"));

// 对象绑定
@YmlProperty("server")
record ServerConfig(int port, String host) {}
ServerConfig config = OpenYml.bind(doc, "server", ServerConfig.class);

// 合并文档
Map<String, Object> merged = OpenYml.merge(baseMap, overlayMap);
YmlDocument mergedDoc = OpenYml.merge(baseDoc, overlayDoc);

// 占位符解析
String resolved = OpenYml.resolvePlaceholders(
    "server:\n  port: ${SERVER_PORT:8080}");
YmlDocument doc = OpenYml.parseWithPlaceholders(yaml);

// 多文档 YAML
List<YmlDocument> docs = OpenYml.loadAll(multiDocYaml);

// 验证
boolean valid = OpenYml.isValid(yamlString);

// 节点树
YmlNode tree = OpenYml.parseTree(yaml);
```

## 类参考

### 根包 (`cloud.opencode.base.yml`)
| 类 | 说明 |
|----|------|
| `OpenYml` | 主门面：解析、加载、转储、绑定、合并、占位符、验证 |
| `YmlDocument` | 解析后的 YAML 文档，支持路径访问（getString、getInt、getList 等） |
| `YmlNode` | YAML 节点树接口，用于层级导航 |
| `DefaultYmlNode` | 默认 YmlNode 实现 |
| `YmlConfig` | YAML 输出配置（缩进、流式风格等） |

### 绑定 (`yml.bind`)
| 类 | 说明 |
|----|------|
| `YmlBinder` | YAML 与 Java 对象绑定引擎 |
| `ConfigBinder` | 配置特定的绑定，支持前缀 |
| `PropertySource` | 属性源抽象 |
| `@YmlProperty` | 指定字段的 YAML 属性名 |
| `@YmlAlias` | 定义备选属性名 |
| `@YmlIgnore` | 在 YAML 绑定中排除字段 |
| `@YmlNestedProperty` | 标记字段为嵌套 YAML 属性 |
| `@YmlValue` | 将字段映射为标量 YAML 值 |

### 异常 (`yml.exception`)
| 类 | 说明 |
|----|------|
| `OpenYmlException` | YAML 操作基础异常 |
| `YmlBindException` | YAML 绑定错误异常 |
| `YmlParseException` | YAML 解析错误异常 |
| `YmlPathException` | YAML 路径解析错误异常 |
| `YmlPlaceholderException` | 占位符解析错误异常 |
| `YmlSecurityException` | YAML 安全违规异常 |

### 合并 (`yml.merge`)
| 类 | 说明 |
|----|------|
| `YmlMerger` | 使用可配置策略合并多个 YAML Map |
| `MergeStrategy` | 合并策略枚举（OVERLAY、DEEP_MERGE、REPLACE） |

### 路径 (`yml.path`)
| 类 | 说明 |
|----|------|
| `YmlPath` | YAML 路径表达式（点号分隔）解析器 |
| `PathResolver` | 在 YAML Map 上解析点号分隔路径 |

### 占位符 (`yml.placeholder`)
| 类 | 说明 |
|----|------|
| `PlaceholderResolver` | 解析 `${...}` 占位符，支持默认值 |
| `PropertyPlaceholder` | 占位符解析和替换 |

### 安全 (`yml.security`)
| 类 | 说明 |
|----|------|
| `SafeConstructor` | 安全的 YAML 构造器，支持类型限制 |
| `YmlSecurity` | YAML 安全配置 |
| `YmlSafeLoader` | 安全的 YAML 加载器，支持深度和大小限制 |

### SnakeYAML (`yml.snakeyaml`)
| 类 | 说明 |
|----|------|
| `SnakeYamlProvider` | 基于 SnakeYAML 的 YmlProvider 实现 |
| `SnakeYmlNode` | 基于 SnakeYAML 的 YmlNode 实现 |

### SPI (`yml.spi`)
| 类 | 说明 |
|----|------|
| `YmlProvider` | YAML 处理提供者 SPI 接口 |
| `YmlProviderFactory` | 发现和加载 YmlProvider 实现的工厂 |
| `YmlFeature` | YAML 提供者功能标志 |

## 环境要求

- Java 25+
- 核心功能无外部依赖
- 可选：`org.yaml:snakeyaml:2.2`（用于完整 YAML 1.1 支持）

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
