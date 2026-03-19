package cloud.opencode.base.pdf.content;

import java.util.ArrayList;
import java.util.List;

/**
 * PDF Table Element - Table with rows and cells
 * PDF 表格元素 - 包含行和单元格的表格
 *
 * <p>Supports headers, custom column widths, borders, and cell styling.</p>
 * <p>支持表头、自定义列宽、边框和单元格样式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Header and data rows - 表头行和数据行</li>
 *   <li>Custom column widths - 自定义列宽</li>
 *   <li>Alternating row colors - 交替行颜色</li>
 *   <li>Border and cell padding configuration - 边框和单元格内边距配置</li>
 *   <li>Fluent builder API - 流畅的构建器 API</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PdfTable table = PdfTable.of(3)
 *     .position(50, 600)
 *     .width(500)
 *     .header("Name", "Age", "City")
 *     .row("Alice", "30", "New York")
 *     .row("Bob", "25", "London")
 *     .headerBackground(PdfColor.LIGHT_GRAY)
 *     .cellPadding(8);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No — mutable builder pattern - 线程安全: 否 — 可变构建器模式</li>
 *   <li>Null-safe: No — callers must ensure non-null values - 空值安全: 否 — 调用方需确保非空值</li>
 *   <li>Defensive copies: Column widths and row data are cloned - 防御性拷贝: 列宽和行数据已克隆</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-pdf V1.0.0
 */
public final class PdfTable implements PdfElement {

    private final int columns;
    private float x;
    private float y;
    private float width;
    private float[] columnWidths;
    private final List<PdfCell[]> headerRows = new ArrayList<>();
    private final List<PdfCell[]> dataRows = new ArrayList<>();
    private float borderWidth = 0.5f;
    private PdfColor borderColor = PdfColor.BLACK;
    private PdfColor headerBackground;
    private PdfColor oddRowColor;
    private PdfColor evenRowColor;
    private float cellPadding = 5f;

    private PdfTable(int columns) {
        this.columns = columns;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a table with specified columns
     * 创建指定列数的表格
     *
     * @param columns number of columns | 列数
     * @return PdfTable instance | PdfTable 实例
     */
    public static PdfTable of(int columns) {
        return new PdfTable(columns);
    }

    /**
     * Creates table builder
     * 创建表格构建器
     *
     * @param columns number of columns | 列数
     * @return Builder instance | Builder 实例
     */
    public static Builder builder(int columns) {
        return new Builder(columns);
    }

    // ==================== Builder Methods | 构建方法 ====================

    /**
     * Sets position
     * 设置位置
     *
     * @param x x coordinate | x 坐标
     * @param y y coordinate | y 坐标
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets table width
     * 设置表格宽度
     *
     * @param width table width | 表格宽度
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable width(float width) {
        this.width = width;
        return this;
    }

    /**
     * Sets column widths
     * 设置列宽
     *
     * @param widths column widths | 列宽数组
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable columnWidths(float... widths) {
        this.columnWidths = widths.clone();
        return this;
    }

    /**
     * Adds a header row
     * 添加表头行
     *
     * @param cells header cells | 表头单元格
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable header(String... cells) {
        PdfCell[] row = new PdfCell[columns];
        for (int i = 0; i < Math.min(cells.length, columns); i++) {
            row[i] = PdfCell.of(cells[i]);
        }
        headerRows.add(row);
        return this;
    }

    /**
     * Adds a header row with PdfCell elements
     * 添加包含 PdfCell 元素的表头行
     *
     * @param cells cell elements | 单元格元素
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable header(PdfCell... cells) {
        PdfCell[] row = new PdfCell[columns];
        System.arraycopy(cells, 0, row, 0, Math.min(cells.length, columns));
        headerRows.add(row);
        return this;
    }

    /**
     * Adds a data row
     * 添加数据行
     *
     * @param cells row cells | 行单元格
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable row(String... cells) {
        PdfCell[] row = new PdfCell[columns];
        for (int i = 0; i < Math.min(cells.length, columns); i++) {
            row[i] = PdfCell.of(cells[i]);
        }
        dataRows.add(row);
        return this;
    }

    /**
     * Adds a row with PdfCell elements
     * 添加包含 PdfCell 元素的行
     *
     * @param cells cell elements | 单元格元素
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable row(PdfCell... cells) {
        PdfCell[] row = new PdfCell[columns];
        System.arraycopy(cells, 0, row, 0, Math.min(cells.length, columns));
        dataRows.add(row);
        return this;
    }

    /**
     * Sets border width
     * 设置边框宽度
     *
     * @param width border width | 边框宽度
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable borderWidth(float width) {
        this.borderWidth = width;
        return this;
    }

    /**
     * Sets border color
     * 设置边框颜色
     *
     * @param color border color | 边框颜色
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable borderColor(PdfColor color) {
        this.borderColor = color;
        return this;
    }

    /**
     * Sets header background color
     * 设置表头背景颜色
     *
     * @param color header background | 表头背景
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable headerBackground(PdfColor color) {
        this.headerBackground = color;
        return this;
    }

    /**
     * Sets alternating row colors
     * 设置交替行颜色
     *
     * @param oddColor  odd row color | 奇数行颜色
     * @param evenColor even row color | 偶数行颜色
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable alternateRowColors(PdfColor oddColor, PdfColor evenColor) {
        this.oddRowColor = oddColor;
        this.evenRowColor = evenColor;
        return this;
    }

    /**
     * Sets cell padding
     * 设置单元格内边距
     *
     * @param padding padding in points | 内边距（点）
     * @return this table for chaining | 当前表格用于链式调用
     */
    public PdfTable cellPadding(float padding) {
        this.cellPadding = padding;
        return this;
    }

    // ==================== Accessors | 访问方法 ====================

    public int getColumns() {
        return columns;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float[] getColumnWidths() {
        return columnWidths != null ? columnWidths.clone() : null;
    }

    public List<PdfCell[]> getHeaderRows() {
        return List.copyOf(headerRows);
    }

    public List<PdfCell[]> getDataRows() {
        return List.copyOf(dataRows);
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public PdfColor getBorderColor() {
        return borderColor;
    }

    public PdfColor getHeaderBackground() {
        return headerBackground;
    }

    public PdfColor getOddRowColor() {
        return oddRowColor;
    }

    public PdfColor getEvenRowColor() {
        return evenRowColor;
    }

    public float getCellPadding() {
        return cellPadding;
    }

    /**
     * Table Builder
     * 表格构建器
     */
    public static final class Builder {
        private final PdfTable table;

        private Builder(int columns) {
            this.table = new PdfTable(columns);
        }

        public Builder columnWidths(float... widths) {
            table.columnWidths(widths);
            return this;
        }

        public Builder header(String... cells) {
            table.header(cells);
            return this;
        }

        public Builder header(PdfCell... cells) {
            table.header(cells);
            return this;
        }

        public Builder row(String... cells) {
            table.row(cells);
            return this;
        }

        public Builder row(PdfCell... cells) {
            table.row(cells);
            return this;
        }

        public Builder borderWidth(float width) {
            table.borderWidth(width);
            return this;
        }

        public Builder borderColor(PdfColor color) {
            table.borderColor(color);
            return this;
        }

        public Builder headerBackground(PdfColor color) {
            table.headerBackground(color);
            return this;
        }

        public Builder cellPadding(float padding) {
            table.cellPadding(padding);
            return this;
        }

        public PdfTable build() {
            return table;
        }
    }
}
