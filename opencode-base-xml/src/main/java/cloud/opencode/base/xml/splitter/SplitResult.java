package cloud.opencode.base.xml.splitter;

import cloud.opencode.base.xml.XmlDocument;

/**
 * Split Result - Holds an indexed XML fragment from a split operation
 * 拆分结果 - 保存拆分操作中带索引的 XML 片段
 *
 * <p>This record pairs a zero-based index with the corresponding XML document fragment
 * extracted by {@link XmlSplitter}.</p>
 * <p>此记录将基于零的索引与 {@link XmlSplitter} 提取的相应 XML 文档片段配对。</p>
 *
 * @param index    the zero-based index of this fragment | 此片段的基于零的索引
 * @param fragment the extracted XML document fragment | 提取的 XML 文档片段
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.3
 */
public record SplitResult(int index, XmlDocument fragment) {}
