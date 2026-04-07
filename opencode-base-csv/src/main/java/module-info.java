/**
 * OpenCode Base CSV Module
 * CSV 处理模块
 *
 * <p>Provides lightweight CSV parsing, writing, binding, diffing, querying, transforming,
 * merging, statistics, validation, splitting, sampling, and security based on RFC 4180.</p>
 * <p>提供基于 RFC 4180 的轻量级 CSV 解析、写入、绑定、差异比较、查询、转换、
 * 合并、统计、校验、分割、抽样和安全功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>RFC 4180 compliant parsing and writing - RFC 4180 合规解析和写入</li>
 *   <li>Immutable document model (CsvDocument, CsvRow) - 不可变文档模型</li>
 *   <li>Type-safe field conversion (CsvField) - 类型安全字段转换</li>
 *   <li>Annotation-based POJO binding (@CsvColumn, @CsvFormat) - 注解驱动对象绑定</li>
 *   <li>CSV diff/change detection - CSV差异/变更检测</li>
 *   <li>Fluent query engine (select/where/orderBy/groupBy) - 流式查询引擎</li>
 *   <li>Transformation pipeline (rename/reorder/add/remove/map) - 转换管道</li>
 *   <li>Document merge (concat/innerJoin/leftJoin) - 文档合并</li>
 *   <li>Column statistics (sum/avg/min/max/distinct/frequency) - 列统计</li>
 *   <li>Declarative validation framework - 声明式校验框架</li>
 *   <li>Document splitting and sampling - 文档分割和抽样</li>
 *   <li>Formula injection protection - 公式注入防护</li>
 * </ul>
 *
 * @author Leon Soo
 * @since JDK 25, opencode-base-csv V1.0.3
 */
module cloud.opencode.base.csv {
    requires transitive cloud.opencode.base.core;

    exports cloud.opencode.base.csv;
    exports cloud.opencode.base.csv.bind;
    exports cloud.opencode.base.csv.bind.annotation;
    exports cloud.opencode.base.csv.diff;
    exports cloud.opencode.base.csv.exception;
    exports cloud.opencode.base.csv.merge;
    exports cloud.opencode.base.csv.sampling;
    exports cloud.opencode.base.csv.query;
    exports cloud.opencode.base.csv.security;
    exports cloud.opencode.base.csv.split;
    exports cloud.opencode.base.csv.stats;
    exports cloud.opencode.base.csv.stream;
    exports cloud.opencode.base.csv.transform;
    exports cloud.opencode.base.csv.validator;
}
