package cloud.opencode.base.xml.bind;

import cloud.opencode.base.xml.XmlDocument;
import cloud.opencode.base.xml.XmlElement;
import cloud.opencode.base.xml.bind.adapter.XmlAdapter;
import cloud.opencode.base.xml.bind.annotation.*;
import cloud.opencode.base.xml.builder.XmlBuilder;
import cloud.opencode.base.xml.dom.DomParser;
import cloud.opencode.base.xml.dom.DomUtil;
import cloud.opencode.base.xml.exception.XmlBindException;

import java.lang.reflect.*;
import java.nio.file.Path;
import java.util.*;

/**
 * XML Binder - Binds XML to/from Java objects
 * XML 绑定器 - 将 XML 绑定到 Java 对象或从 Java 对象绑定
 *
 * <p>This class provides methods for marshalling Java objects to XML and
 * unmarshalling XML to Java objects using annotations.</p>
 * <p>此类提供使用注解将 Java 对象编组为 XML 以及将 XML 解组为 Java 对象的方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>XML to Java object unmarshalling - XML 到 Java 对象解组</li>
 *   <li>Java object to XML marshalling - Java 对象到 XML 编组</li>
 *   <li>Custom type adapter registration - 自定义类型适配器注册</li>
 *   <li>Annotation-driven field mapping - 注解驱动的字段映射</li>
 *   <li>Formatted output with configurable indentation - 可配置缩进的格式化输出</li>
 *   <li>Nested object and collection support - 嵌套对象和集合支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Unmarshal XML to object
 * User user = XmlBinder.create()
 *     .unmarshal(xml, User.class);
 *
 * // Marshal object to XML
 * String xml = XmlBinder.create()
 *     .formatted(true)
 *     .marshal(user);
 *
 * // With custom adapter
 * XmlBinder binder = XmlBinder.create()
 *     .registerAdapter(new LocalDateAdapter());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (mutable configuration state) - 线程安全: 否（可变配置状态）</li>
 *   <li>Null-safe: No (null inputs throw exceptions) - 空值安全: 否（空值输入抛出异常）</li>
 *   <li>Uses reflection with setAccessible - 使用反射和 setAccessible</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-xml V1.0.0
 */
public final class XmlBinder {

    private final Map<Class<?>, XmlAdapter<?, ?>> adapters = new HashMap<>();
    private boolean formatted = false;
    private String encoding = "UTF-8";
    private Path schema;
    private int indent = 4;

    private XmlBinder() {
    }

    /**
     * Creates a new XML binder.
     * 创建新的 XML 绑定器。
     *
     * @return a new binder | 新绑定器
     */
    public static XmlBinder create() {
        return new XmlBinder();
    }

    /**
     * Registers a type adapter.
     * 注册类型适配器。
     *
     * @param adapter the adapter | 适配器
     * @return this binder for chaining | 此绑定器以便链式调用
     */
    public XmlBinder registerAdapter(XmlAdapter<?, ?> adapter) {
        Class<?> boundType = adapter.getBoundType();
        if (boundType != null) {
            adapters.put(boundType, adapter);
        }
        return this;
    }

    /**
     * Sets formatted output.
     * 设置格式化输出。
     *
     * @param formatted whether to format | 是否格式化
     * @return this binder for chaining | 此绑定器以便链式调用
     */
    public XmlBinder formatted(boolean formatted) {
        this.formatted = formatted;
        return this;
    }

    /**
     * Sets the output encoding.
     * 设置输出编码。
     *
     * @param encoding the encoding | 编码
     * @return this binder for chaining | 此绑定器以便链式调用
     */
    public XmlBinder encoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Sets the indentation level.
     * 设置缩进级别。
     *
     * @param indent the indent spaces | 缩进空格数
     * @return this binder for chaining | 此绑定器以便链式调用
     */
    public XmlBinder indent(int indent) {
        this.indent = indent;
        return this;
    }

    /**
     * Sets schema validation.
     * 设置 Schema 验证。
     *
     * @param schemaPath the schema path | Schema 路径
     * @return this binder for chaining | 此绑定器以便链式调用
     */
    public XmlBinder schema(Path schemaPath) {
        this.schema = schemaPath;
        return this;
    }

    // ==================== Unmarshal | 解组 ====================

    /**
     * Unmarshals XML string to object.
     * 将 XML 字符串解组为对象。
     *
     * @param <T>   the type parameter | 类型参数
     * @param xml   the XML string | XML 字符串
     * @param clazz the target type | 目标类型
     * @return the object | 对象
     */
    public <T> T unmarshal(String xml, Class<T> clazz) {
        return unmarshal(XmlDocument.parse(xml), clazz);
    }

    /**
     * Unmarshals XML document to object.
     * 将 XML 文档解组为对象。
     *
     * @param <T>      the type parameter | 类型参数
     * @param document the XML document | XML 文档
     * @param clazz    the target type | 目标类型
     * @return the object | 对象
     */
    public <T> T unmarshal(XmlDocument document, Class<T> clazz) {
        XmlElement root = document.getRoot();
        if (root == null) {
            throw new XmlBindException(clazz, "Document has no root element");
        }
        return unmarshal(root, clazz);
    }

    /**
     * Unmarshals XML element to object.
     * 将 XML 元素解组为对象。
     *
     * <p>Supports both regular classes (via no-arg constructor) and records
     * (via canonical constructor with component values from XML).</p>
     * <p>支持普通类（通过无参构造函数）和记录（通过从 XML 获取组件值的规范构造函数）。</p>
     *
     * @param <T>     the type parameter | 类型参数
     * @param element the XML element | XML 元素
     * @param clazz   the target type | 目标类型
     * @return the object | 对象
     */
    public <T> T unmarshal(XmlElement element, Class<T> clazz) {
        try {
            if (clazz.isRecord()) {
                return unmarshalRecord(element, clazz);
            }
            T instance = createInstance(clazz);
            populateFields(instance, element, clazz);
            return instance;
        } catch (XmlBindException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlBindException(clazz, e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> clazz) throws Exception {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * Unmarshals XML element to a record instance.
     * 将 XML 元素解组为记录实例。
     */
    @SuppressWarnings("unchecked")
    private <T> T unmarshalRecord(XmlElement element, Class<T> clazz) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Class<?>[] paramTypes = new Class<?>[components.length];
        Object[] paramValues = new Object[components.length];

        for (int i = 0; i < components.length; i++) {
            RecordComponent rc = components[i];
            paramTypes[i] = rc.getType();

            // Check if @XmlIgnore is present
            if (rc.isAnnotationPresent(XmlIgnore.class)) {
                paramValues[i] = getDefaultValue(rc.getType());
                continue;
            }

            // Handle @XmlAttribute
            if (rc.isAnnotationPresent(XmlAttribute.class)) {
                XmlAttribute attr = rc.getAnnotation(XmlAttribute.class);
                String name = attr.value().isEmpty() ? rc.getName() : attr.value();
                String value = element.getAttribute(name);
                paramValues[i] = value != null ? convertValue(value, rc.getType()) : getDefaultValue(rc.getType());
                continue;
            }

            // Handle @XmlValue
            if (rc.isAnnotationPresent(XmlValue.class)) {
                String text = element.getText();
                paramValues[i] = (text != null && !text.isBlank())
                    ? convertValue(text, rc.getType()) : getDefaultValue(rc.getType());
                continue;
            }

            // Handle @XmlElementList
            if (rc.isAnnotationPresent(XmlElementList.class)) {
                paramValues[i] = unmarshalRecordList(element, rc);
                continue;
            }

            // Default to XmlElement behavior (annotation or component name)
            cloud.opencode.base.xml.bind.annotation.XmlElement elemAnnotation =
                rc.getAnnotation(cloud.opencode.base.xml.bind.annotation.XmlElement.class);
            String name = (elemAnnotation != null && !elemAnnotation.value().isEmpty())
                ? elemAnnotation.value() : rc.getName();

            XmlElement child = element.getChild(name);
            if (child != null) {
                if (isSimpleType(rc.getType())) {
                    paramValues[i] = convertValue(child.getText(), rc.getType());
                } else if (List.class.isAssignableFrom(rc.getType())) {
                    // List without @XmlElementList — treat children as list items
                    paramValues[i] = unmarshalRecordDefaultList(child, rc);
                } else {
                    paramValues[i] = unmarshal(child, rc.getType());
                }
            } else {
                paramValues[i] = getDefaultValue(rc.getType());
            }
        }

        Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(paramValues);
    }

    /**
     * Unmarshals a list field for a record component with @XmlElementList.
     * 为带有 @XmlElementList 注解的记录组件解组列表字段。
     */
    private Object unmarshalRecordList(XmlElement element, RecordComponent rc) throws Exception {
        XmlElementList annotation = rc.getAnnotation(XmlElementList.class);
        String wrapperName = annotation.value();
        String itemName = annotation.itemName();

        if (itemName.isEmpty()) {
            itemName = rc.getName();
        }

        List<XmlElement> items;
        if (!wrapperName.isEmpty()) {
            XmlElement wrapper = element.getChild(wrapperName);
            items = wrapper != null ? wrapper.getChildren(itemName) : List.of();
        } else {
            items = element.getChildren(itemName);
        }

        Class<?> itemType = getListItemType(rc);

        List<Object> list = new ArrayList<>();
        for (XmlElement item : items) {
            if (isSimpleType(itemType)) {
                list.add(convertValue(item.getText(), itemType));
            } else {
                list.add(unmarshal(item, itemType));
            }
        }

        return list;
    }

    /**
     * Unmarshals a list field for a record component without @XmlElementList.
     * 为不带 @XmlElementList 注解的记录组件解组列表字段。
     */
    private Object unmarshalRecordDefaultList(XmlElement wrapperElement, RecordComponent rc) throws Exception {
        Class<?> itemType = getListItemType(rc);
        List<XmlElement> children = wrapperElement.getChildren();

        List<Object> list = new ArrayList<>();
        for (XmlElement child : children) {
            if (isSimpleType(itemType)) {
                list.add(convertValue(child.getText(), itemType));
            } else {
                list.add(unmarshal(child, itemType));
            }
        }

        return list;
    }

    /**
     * Gets the generic item type of a List record component.
     * 获取 List 记录组件的泛型项目类型。
     */
    private Class<?> getListItemType(RecordComponent rc) {
        Type genericType = rc.getGenericType();
        if (genericType instanceof ParameterizedType pt) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class<?> c) {
                return c;
            }
        }
        return String.class;
    }

    /**
     * Returns the default value for a type (null for objects, 0 for primitives).
     * 返回类型的默认值（对象为 null，基本类型为 0）。
     */
    private Object getDefaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0;
        if (type == char.class) return '\0';
        return null;
    }

    private void populateFields(Object instance, XmlElement element, Class<?> clazz) throws Exception {
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(XmlIgnore.class)) {
                continue;
            }

            field.setAccessible(true);

            if (field.isAnnotationPresent(XmlAttribute.class)) {
                XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
                String name = attr.value().isEmpty() ? field.getName() : attr.value();
                String value = element.getAttribute(name);
                if (value != null) {
                    setFieldValue(instance, field, value);
                }
            } else if (field.isAnnotationPresent(XmlValue.class)) {
                String text = element.getText();
                if (text != null && !text.isBlank()) {
                    setFieldValue(instance, field, text);
                }
            } else if (field.isAnnotationPresent(XmlElementList.class)) {
                XmlElementList listAnnotation = field.getAnnotation(XmlElementList.class);
                populateListField(instance, field, element, listAnnotation);
            } else {
                // Default to XmlElement behavior
                cloud.opencode.base.xml.bind.annotation.XmlElement elemAnnotation =
                    field.getAnnotation(cloud.opencode.base.xml.bind.annotation.XmlElement.class);
                String name = (elemAnnotation != null && !elemAnnotation.value().isEmpty())
                    ? elemAnnotation.value() : field.getName();

                XmlElement child = element.getChild(name);
                if (child != null) {
                    if (isSimpleType(field.getType())) {
                        setFieldValue(instance, field, child.getText());
                    } else {
                        Object nestedObj = unmarshal(child, field.getType());
                        field.set(instance, nestedObj);
                    }
                }
            }
        }
    }

    private void populateListField(Object instance, Field field, XmlElement element,
                                   XmlElementList annotation) throws Exception {
        String wrapperName = annotation.value();
        String itemName = annotation.itemName();

        if (itemName.isEmpty()) {
            itemName = field.getName();
        }

        List<XmlElement> items;
        if (!wrapperName.isEmpty()) {
            XmlElement wrapper = element.getChild(wrapperName);
            items = wrapper != null ? wrapper.getChildren(itemName) : List.of();
        } else {
            items = element.getChildren(itemName);
        }

        Type genericType = field.getGenericType();
        Class<?> itemType = String.class;
        if (genericType instanceof ParameterizedType pt) {
            itemType = (Class<?>) pt.getActualTypeArguments()[0];
        }

        List<Object> list = new ArrayList<>();
        for (XmlElement item : items) {
            if (isSimpleType(itemType)) {
                list.add(convertValue(item.getText(), itemType));
            } else {
                list.add(unmarshal(item, itemType));
            }
        }

        field.set(instance, list);
    }

    private void setFieldValue(Object instance, Field field, String value) throws Exception {
        if (value == null) return;
        Object converted = convertValue(value, field.getType());
        field.set(instance, converted);
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(String value, Class<?> type) throws Exception {
        if (value == null) return null;

        // Check for custom adapter
        XmlAdapter<String, ?> adapter = (XmlAdapter<String, ?>) adapters.get(type);
        if (adapter != null) {
            return adapter.unmarshal(value);
        }

        return DomUtil.convertText(value, type);
    }

    // ==================== Marshal | 编组 ====================

    /**
     * Marshals object to XML string.
     * 将对象编组为 XML 字符串。
     *
     * @param obj the object | 对象
     * @return the XML string | XML 字符串
     */
    public String marshal(Object obj) {
        return marshal(obj, formatted ? indent : 0);
    }

    /**
     * Marshals object to formatted XML string.
     * 将对象编组为格式化的 XML 字符串。
     *
     * @param obj    the object | 对象
     * @param indent the indentation | 缩进
     * @return the XML string | XML 字符串
     */
    public String marshal(Object obj, int indent) {
        XmlDocument doc = marshalToDocument(obj);
        return indent > 0 ? doc.toXml(indent) : doc.toXml();
    }

    /**
     * Marshals object to XML document.
     * 将对象编组为 XML 文档。
     *
     * @param obj the object | 对象
     * @return the XML document | XML 文档
     */
    public XmlDocument marshalToDocument(Object obj) {
        if (obj == null) {
            throw new XmlBindException("Cannot marshal null object");
        }

        Class<?> clazz = obj.getClass();
        String rootName = getRootElementName(clazz);

        XmlBuilder builder = XmlBuilder.create(rootName).encoding(encoding);

        try {
            marshalFields(obj, builder, clazz);
        } catch (Exception e) {
            throw new XmlBindException(clazz, e.getMessage(), e);
        }

        return builder.build();
    }

    private void marshalFields(Object obj, XmlBuilder builder, Class<?> clazz) throws Exception {
        for (Field field : getAllFields(clazz)) {
            if (field.isAnnotationPresent(XmlIgnore.class)) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(obj);
            if (value == null) continue;

            if (field.isAnnotationPresent(XmlAttribute.class)) {
                XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
                String name = attr.value().isEmpty() ? field.getName() : attr.value();
                builder.attribute(name, convertToString(value));
            } else if (field.isAnnotationPresent(XmlValue.class)) {
                // Text content - handled at element level
            } else if (field.isAnnotationPresent(XmlElementList.class)) {
                marshalListField(value, builder, field);
            } else {
                cloud.opencode.base.xml.bind.annotation.XmlElement elemAnnotation =
                    field.getAnnotation(cloud.opencode.base.xml.bind.annotation.XmlElement.class);
                String name = (elemAnnotation != null && !elemAnnotation.value().isEmpty())
                    ? elemAnnotation.value() : field.getName();

                if (isSimpleType(field.getType())) {
                    builder.element(name, convertToString(value));
                } else {
                    builder.startElement(name);
                    marshalFields(value, builder, field.getType());
                    builder.end();
                }
            }
        }
    }

    private void marshalListField(Object value, XmlBuilder builder, Field field) throws Exception {
        if (!(value instanceof Collection<?> collection)) return;

        XmlElementList annotation = field.getAnnotation(XmlElementList.class);
        String wrapperName = annotation.value();
        String itemName = annotation.itemName();

        if (itemName.isEmpty()) {
            itemName = "item";
        }

        if (!wrapperName.isEmpty()) {
            builder.startElement(wrapperName);
        }

        Type genericType = field.getGenericType();
        Class<?> itemType = String.class;
        if (genericType instanceof ParameterizedType pt) {
            itemType = (Class<?>) pt.getActualTypeArguments()[0];
        }

        for (Object item : collection) {
            if (isSimpleType(itemType)) {
                builder.element(itemName, convertToString(item));
            } else {
                builder.startElement(itemName);
                marshalFields(item, builder, itemType);
                builder.end();
            }
        }

        if (!wrapperName.isEmpty()) {
            builder.end();
        }
    }

    @SuppressWarnings("unchecked")
    private String convertToString(Object value) throws Exception {
        if (value == null) return null;

        XmlAdapter<String, Object> adapter = (XmlAdapter<String, Object>) adapters.get(value.getClass());
        if (adapter != null) {
            return adapter.marshal(value);
        }

        return value.toString();
    }

    // ==================== Helpers | 辅助方法 ====================

    private String getRootElementName(Class<?> clazz) {
        XmlRoot root = clazz.getAnnotation(XmlRoot.class);
        if (root != null && !root.value().isEmpty()) {
            return root.value();
        }
        // Default to class name with lowercase first letter
        String name = clazz.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
               type == String.class ||
               Number.class.isAssignableFrom(type) ||
               type == Boolean.class ||
               type == Character.class ||
               type.isEnum();
    }
}
