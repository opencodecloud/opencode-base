package cloud.opencode.base.string;

import cloud.opencode.base.string.naming.CaseUtil;
import cloud.opencode.base.string.naming.NamingCase;
import cloud.opencode.base.string.naming.WordUtil;

/**
 * Naming Convention Facade Utility
 * 命名转换门面工具类
 *
 * <p>Provides convenient methods for converting between different naming conventions.</p>
 * <p>提供在不同命名约定之间转换的便捷方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>9 naming style conversions - 9种命名风格转换</li>
 *   <li>Auto-detection of naming style - 命名风格自动检测</li>
 *   <li>Database/Java naming conversion - 数据库/Java命名转换</li>
 *   <li>Word splitting and joining - 单词分割和连接</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert to camelCase
 * String camel = OpenNaming.toCamelCase("get_user_name"); // "getUserName"
 *
 * // Convert to snake_case
 * String snake = OpenNaming.toSnakeCase("getUserName"); // "get_user_name"
 *
 * // Database to Java
 * String className = OpenNaming.tableToClass("sys_user"); // "SysUser"
 * String fieldName = OpenNaming.columnToField("user_name"); // "userName"
 *
 * // Java to Database
 * String tableName = OpenNaming.classToTable("UserInfo"); // "user_info"
 * String columnName = OpenNaming.fieldToColumn("userName"); // "user_name"
 * }</pre>
 *
 * <p><strong>Performance | 性能:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenNaming {

    private OpenNaming() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== Conversion Methods | 转换方法 ====================

    /**
     * Convert to camelCase (first letter lowercase).
     * 转换为驼峰命名（首字母小写）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toCamelCase("get_user_name") = "getUserName"
     * toCamelCase("GetUserName")   = "getUserName"
     * toCamelCase("get-user-name") = "getUserName"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return camelCase string | 驼峰命名字符串
     */
    public static String toCamelCase(String name) {
        return CaseUtil.toCamelCase(name);
    }

    /**
     * Convert to PascalCase (first letter uppercase).
     * 转换为帕斯卡命名（首字母大写）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toPascalCase("get_user_name") = "GetUserName"
     * toPascalCase("getUserName")   = "GetUserName"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return PascalCase string | 帕斯卡命名字符串
     */
    public static String toPascalCase(String name) {
        return CaseUtil.toPascalCase(name);
    }

    /**
     * Convert to snake_case (underscore separated lowercase).
     * 转换为蛇形命名（下划线分隔小写）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toSnakeCase("getUserName") = "get_user_name"
     * toSnakeCase("GetUserName") = "get_user_name"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return snake_case string | 蛇形命名字符串
     */
    public static String toSnakeCase(String name) {
        return CaseUtil.toSnakeCase(name);
    }

    /**
     * Convert to UPPER_SNAKE_CASE (constant naming).
     * 转换为大写蛇形命名（常量命名）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toUpperSnakeCase("maxRetryCount") = "MAX_RETRY_COUNT"
     * toUpperSnakeCase("max-retry-count") = "MAX_RETRY_COUNT"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return UPPER_SNAKE_CASE string | 大写蛇形命名字符串
     */
    public static String toUpperSnakeCase(String name) {
        return CaseUtil.toUpperSnakeCase(name);
    }

    /**
     * Convert to kebab-case (hyphen separated).
     * 转换为短横线命名。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toKebabCase("getUserName") = "get-user-name"
     * toKebabCase("get_user_name") = "get-user-name"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return kebab-case string | 短横线命名字符串
     */
    public static String toKebabCase(String name) {
        return CaseUtil.toKebabCase(name);
    }

    /**
     * Convert to dot.case.
     * 转换为点分隔命名。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toDotCase("getUserName") = "get.user.name"
     * toDotCase("get_user_name") = "get.user.name"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return dot.case string | 点分隔命名字符串
     */
    public static String toDotCase(String name) {
        return CaseUtil.toDotCase(name);
    }

    /**
     * Convert to path/case.
     * 转换为路径命名。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toPathCase("UserController") = "user/controller"
     * toPathCase("user_controller") = "user/controller"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return path/case string | 路径命名字符串
     */
    public static String toPathCase(String name) {
        return CaseUtil.toPathCase(name);
    }

    /**
     * Convert to Title Case (space separated, each word capitalized).
     * 转换为标题形式（空格分隔，每个单词首字母大写）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toTitleCase("getUserName") = "Get User Name"
     * toTitleCase("get_user_name") = "Get User Name"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return Title Case string | 标题形式字符串
     */
    public static String toTitleCase(String name) {
        return CaseUtil.toTitleCase(name);
    }

    /**
     * Convert to Sentence case (space separated, first word capitalized).
     * 转换为句子形式（空格分隔，首词首字母大写）。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * toSentenceCase("getUserName") = "Get user name"
     * toSentenceCase("get_user_name") = "Get user name"
     * </pre>
     *
     * @param name the name to convert | 要转换的名称
     * @return Sentence case string | 句子形式字符串
     */
    public static String toSentenceCase(String name) {
        return CaseUtil.toSentenceCase(name);
    }

    // ==================== General Conversion | 通用转换 ====================

    /**
     * Convert naming style.
     * 转换命名风格。
     *
     * @param name the name to convert | 要转换的名称
     * @param from source naming case | 源命名风格
     * @param to   target naming case | 目标命名风格
     * @return converted string | 转换结果
     */
    public static String convert(String name, NamingCase from, NamingCase to) {
        return CaseUtil.convert(name, from, to);
    }

    /**
     * Auto-detect and convert to target naming style.
     * 自动检测并转换为目标命名风格。
     *
     * @param name the name to convert | 要转换的名称
     * @param to   target naming case | 目标命名风格
     * @return converted string | 转换结果
     */
    public static String convert(String name, NamingCase to) {
        return CaseUtil.convert(name, to);
    }

    /**
     * Detect naming case of a string.
     * 检测字符串的命名风格。
     *
     * @param name the name to detect | 要检测的名称
     * @return detected naming case | 检测到的命名风格
     */
    public static NamingCase detect(String name) {
        return CaseUtil.detect(name);
    }

    // ==================== Word Splitting | 单词分割 ====================

    /**
     * Split compound name into words.
     * 将复合名称分割为单词。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * splitWords("getUserName")   = ["get", "User", "Name"]
     * splitWords("get_user_name") = ["get", "user", "name"]
     * </pre>
     *
     * @param name the compound name | 复合名称
     * @return array of words | 单词数组
     */
    public static String[] splitWords(String name) {
        return WordUtil.splitWords(name);
    }

    /**
     * Join words with naming case.
     * 使用命名风格连接单词。
     *
     * @param words words to join | 要连接的单词
     * @param case_ naming case | 命名风格
     * @return joined string | 连接结果
     */
    public static String joinWords(String[] words, NamingCase case_) {
        if (words == null || words.length == 0) {
            return "";
        }
        return CaseUtil.convert(WordUtil.joinWords(words, " "), case_);
    }

    // ==================== Special Conversions | 特殊转换 ====================

    /**
     * Convert database table name to Java class name.
     * 将数据库表名转换为Java类名。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * tableToClass("sys_user")      = "SysUser"
     * tableToClass("user_info")     = "UserInfo"
     * tableToClass("t_order_item")  = "TOrderItem"
     * </pre>
     *
     * @param tableName database table name | 数据库表名
     * @return Java class name | Java类名
     */
    public static String tableToClass(String tableName) {
        return toPascalCase(tableName);
    }

    /**
     * Convert database column name to Java field name.
     * 将数据库字段名转换为Java属性名。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * columnToField("user_name")    = "userName"
     * columnToField("created_at")   = "createdAt"
     * columnToField("is_deleted")   = "isDeleted"
     * </pre>
     *
     * @param columnName database column name | 数据库字段名
     * @return Java field name | Java属性名
     */
    public static String columnToField(String columnName) {
        return toCamelCase(columnName);
    }

    /**
     * Convert Java class name to database table name.
     * 将Java类名转换为数据库表名。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * classToTable("UserInfo")    = "user_info"
     * classToTable("OrderItem")   = "order_item"
     * classToTable("SysUser")     = "sys_user"
     * </pre>
     *
     * @param className Java class name | Java类名
     * @return database table name | 数据库表名
     */
    public static String classToTable(String className) {
        return toSnakeCase(className);
    }

    /**
     * Convert Java field name to database column name.
     * 将Java属性名转换为数据库字段名。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * fieldToColumn("userName")   = "user_name"
     * fieldToColumn("createdAt")  = "created_at"
     * fieldToColumn("isDeleted")  = "is_deleted"
     * </pre>
     *
     * @param fieldName Java field name | Java属性名
     * @return database column name | 数据库字段名
     */
    public static String fieldToColumn(String fieldName) {
        return toSnakeCase(fieldName);
    }

    /**
     * Convert Java class name to URL path.
     * 将Java类名转换为URL路径。
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * classToPath("UserController")    = "user/controller"
     * classToPath("OrderItemService")  = "order/item/service"
     * </pre>
     *
     * @param className Java class name | Java类名
     * @return URL path | URL路径
     */
    public static String classToPath(String className) {
        return toPathCase(className);
    }
}
