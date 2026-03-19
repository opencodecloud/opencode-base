package cloud.opencode.base.expression.function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * TypeFunctions Tests
 * TypeFunctions 测试
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
@DisplayName("TypeFunctions Tests | TypeFunctions 测试")
class TypeFunctionsTest {

    private static Map<String, Function> functions;

    @BeforeAll
    static void setup() {
        functions = TypeFunctions.getFunctions();
    }

    @Nested
    @DisplayName("Type Checking Tests | 类型检查测试")
    class TypeCheckingTests {

        @Test
        @DisplayName("isnull function | isnull 函数")
        void testIsNull() {
            Function isnull = functions.get("isnull");
            assertThat(isnull.apply((Object) null)).isEqualTo(true);
            assertThat(isnull.apply("hello")).isEqualTo(false);
            assertThat(isnull.apply()).isEqualTo(true);
        }

        @Test
        @DisplayName("isnotnull function | isnotnull 函数")
        void testIsNotNull() {
            Function isnotnull = functions.get("isnotnull");
            assertThat(isnotnull.apply((Object) null)).isEqualTo(false);
            assertThat(isnotnull.apply("hello")).isEqualTo(true);
            assertThat(isnotnull.apply()).isEqualTo(false);
        }

        @Test
        @DisplayName("isnumber function | isnumber 函数")
        void testIsNumber() {
            Function isnumber = functions.get("isnumber");
            assertThat(isnumber.apply(42)).isEqualTo(true);
            assertThat(isnumber.apply(3.14)).isEqualTo(true);
            assertThat(isnumber.apply(BigDecimal.ONE)).isEqualTo(true);
            assertThat(isnumber.apply("42")).isEqualTo(false);
            assertThat(isnumber.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isstring function | isstring 函数")
        void testIsString() {
            Function isstring = functions.get("isstring");
            assertThat(isstring.apply("hello")).isEqualTo(true);
            assertThat(isstring.apply(42)).isEqualTo(false);
            assertThat(isstring.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isboolean function | isboolean 函数")
        void testIsBoolean() {
            Function isboolean = functions.get("isboolean");
            assertThat(isboolean.apply(true)).isEqualTo(true);
            assertThat(isboolean.apply(false)).isEqualTo(true);
            assertThat(isboolean.apply("true")).isEqualTo(false);
            assertThat(isboolean.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isarray function | isarray 函数")
        void testIsArray() {
            Function isarray = functions.get("isarray");
            assertThat(isarray.apply((Object) new int[]{1, 2, 3})).isEqualTo(true);
            assertThat(isarray.apply((Object) new String[]{"a"})).isEqualTo(true);
            assertThat(isarray.apply(List.of(1, 2, 3))).isEqualTo(false);
            assertThat(isarray.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("islist function | islist 函数")
        void testIsList() {
            Function islist = functions.get("islist");
            assertThat(islist.apply(List.of(1, 2, 3))).isEqualTo(true);
            assertThat(islist.apply(new ArrayList<>())).isEqualTo(true);
            assertThat(islist.apply(Set.of(1, 2, 3))).isEqualTo(false);
            assertThat(islist.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("ismap function | ismap 函数")
        void testIsMap() {
            Function ismap = functions.get("ismap");
            assertThat(ismap.apply(Map.of("a", 1))).isEqualTo(true);
            assertThat(ismap.apply(new HashMap<>())).isEqualTo(true);
            assertThat(ismap.apply(List.of())).isEqualTo(false);
            assertThat(ismap.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("iscollection function | iscollection 函数")
        void testIsCollection() {
            Function iscollection = functions.get("iscollection");
            assertThat(iscollection.apply(List.of(1, 2, 3))).isEqualTo(true);
            assertThat(iscollection.apply(Set.of(1, 2, 3))).isEqualTo(true);
            assertThat(iscollection.apply(Map.of("a", 1))).isEqualTo(false);
            assertThat(iscollection.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isdate function | isdate 函数")
        void testIsDate() {
            Function isdate = functions.get("isdate");
            assertThat(isdate.apply(LocalDate.now())).isEqualTo(true);
            assertThat(isdate.apply(LocalDateTime.now())).isEqualTo(true);
            assertThat(isdate.apply(new java.util.Date())).isEqualTo(true);
            assertThat(isdate.apply("2024-01-01")).isEqualTo(false);
            assertThat(isdate.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isinteger function | isinteger 函数")
        void testIsInteger() {
            Function isinteger = functions.get("isinteger");
            assertThat(isinteger.apply(42)).isEqualTo(true);
            assertThat(isinteger.apply(42L)).isEqualTo(true);
            assertThat(isinteger.apply((short) 42)).isEqualTo(true);
            assertThat(isinteger.apply((byte) 42)).isEqualTo(true);
            assertThat(isinteger.apply(BigInteger.ONE)).isEqualTo(true);
            assertThat(isinteger.apply(3.14)).isEqualTo(false);
            assertThat(isinteger.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("isdecimal function | isdecimal 函数")
        void testIsDecimal() {
            Function isdecimal = functions.get("isdecimal");
            assertThat(isdecimal.apply(3.14)).isEqualTo(true);
            assertThat(isdecimal.apply(3.14f)).isEqualTo(true);
            assertThat(isdecimal.apply(BigDecimal.ONE)).isEqualTo(true);
            assertThat(isdecimal.apply(42)).isEqualTo(false);
            assertThat(isdecimal.apply((Object) null)).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Type Conversion Tests | 类型转换测试")
    class TypeConversionTests {

        @Test
        @DisplayName("toint function | toint 函数")
        void testToInt() {
            Function toint = functions.get("toint");
            assertThat(toint.apply(42.9)).isEqualTo(42);
            assertThat(toint.apply("123")).isEqualTo(123);
            assertThat(toint.apply("3.14")).isEqualTo(3);
            assertThat(toint.apply(true)).isEqualTo(1);
            assertThat(toint.apply(false)).isEqualTo(0);
            assertThat(toint.apply((Object) null)).isEqualTo(0);
            assertThat(toint.apply("invalid")).isEqualTo(0);
        }

        @Test
        @DisplayName("tointeger alias | tointeger 别名")
        void testToInteger() {
            assertThat(functions.get("tointeger")).isNotNull();
        }

        @Test
        @DisplayName("tolong function | tolong 函数")
        void testToLong() {
            Function tolong = functions.get("tolong");
            assertThat(tolong.apply(42.9)).isEqualTo(42L);
            assertThat(tolong.apply("123")).isEqualTo(123L);
            assertThat(tolong.apply(true)).isEqualTo(1L);
            assertThat(tolong.apply((Object) null)).isEqualTo(0L);
        }

        @Test
        @DisplayName("todouble function | todouble 函数")
        void testToDouble() {
            Function todouble = functions.get("todouble");
            assertThat(todouble.apply(42)).isEqualTo(42.0);
            assertThat(todouble.apply("3.14")).isEqualTo(3.14);
            assertThat(todouble.apply(true)).isEqualTo(1.0);
            assertThat(todouble.apply((Object) null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("tofloat function | tofloat 函数")
        void testToFloat() {
            Function tofloat = functions.get("tofloat");
            assertThat(tofloat.apply(42)).isEqualTo(42.0f);
            assertThat(tofloat.apply("3.14")).isEqualTo(3.14f);
            assertThat(tofloat.apply((Object) null)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("tostring function | tostring 函数")
        void testToString() {
            Function tostring = functions.get("tostring");
            assertThat(tostring.apply(42)).isEqualTo("42");
            assertThat(tostring.apply(true)).isEqualTo("true");
            assertThat(tostring.apply((Object) null)).isEqualTo("");
        }

        @Test
        @DisplayName("toboolean function | toboolean 函数")
        void testToBoolean() {
            Function toboolean = functions.get("toboolean");
            assertThat(toboolean.apply(true)).isEqualTo(true);
            assertThat(toboolean.apply(false)).isEqualTo(false);
            assertThat(toboolean.apply(1)).isEqualTo(true);
            assertThat(toboolean.apply(0)).isEqualTo(false);
            assertThat(toboolean.apply("true")).isEqualTo(true);
            assertThat(toboolean.apply("false")).isEqualTo(false);
            assertThat(toboolean.apply("")).isEqualTo(false);
            assertThat(toboolean.apply("0")).isEqualTo(false);
            assertThat(toboolean.apply("no")).isEqualTo(false);
            assertThat(toboolean.apply("yes")).isEqualTo(true);
            assertThat(toboolean.apply((Object) null)).isEqualTo(false);
        }

        @Test
        @DisplayName("tobool alias | tobool 别名")
        void testToBool() {
            assertThat(functions.get("tobool")).isNotNull();
        }

        @Test
        @DisplayName("tolist function | tolist 函数")
        void testToList() {
            Function tolist = functions.get("tolist");
            assertThat(tolist.apply(List.of(1, 2, 3))).isEqualTo(List.of(1, 2, 3));
            assertThat(tolist.apply(Set.of(1))).isEqualTo(List.of(1));
            assertThat(tolist.apply((Object) new Integer[]{1, 2})).isEqualTo(List.of(1, 2));
            assertThat(tolist.apply(42)).isEqualTo(List.of(42));
            assertThat(tolist.apply((Object) null)).isEqualTo(new ArrayList<>());
        }

        @Test
        @DisplayName("toset function | toset 函数")
        void testToSet() {
            Function toset = functions.get("toset");
            assertThat(toset.apply(Set.of(1, 2, 3))).isEqualTo(Set.of(1, 2, 3));
            assertThat(toset.apply(List.of(1, 1, 2, 2))).isEqualTo(Set.of(1, 2));
            assertThat(toset.apply((Object) new Integer[]{1, 2})).isEqualTo(Set.of(1, 2));
            assertThat(toset.apply(42)).isEqualTo(Set.of(42));
            assertThat(toset.apply((Object) null)).isEqualTo(new HashSet<>());
        }
    }

    @Nested
    @DisplayName("Type Info Tests | 类型信息测试")
    class TypeInfoTests {

        @Test
        @DisplayName("typeof function | typeof 函数")
        void testTypeof() {
            Function typeof = functions.get("typeof");
            assertThat(typeof.apply((Object) null)).isEqualTo("null");
            assertThat(typeof.apply("hello")).isEqualTo("string");
            assertThat(typeof.apply(42)).isEqualTo("integer");
            assertThat(typeof.apply(42L)).isEqualTo("integer");
            assertThat(typeof.apply(3.14)).isEqualTo("decimal");
            assertThat(typeof.apply(3.14f)).isEqualTo("decimal");
            assertThat(typeof.apply(BigDecimal.ONE)).isEqualTo("number");
            assertThat(typeof.apply(true)).isEqualTo("boolean");
            assertThat(typeof.apply(List.of())).isEqualTo("list");
            assertThat(typeof.apply(Map.of())).isEqualTo("map");
            assertThat(typeof.apply(Set.of())).isEqualTo("set");
            assertThat(typeof.apply((Object) new int[]{})).isEqualTo("array");
            assertThat(typeof.apply(LocalDate.now())).isEqualTo("date");
            assertThat(typeof.apply(LocalDateTime.now())).isEqualTo("datetime");
            assertThat(typeof.apply(LocalTime.now())).isEqualTo("time");
        }

        @Test
        @DisplayName("classname function | classname 函数")
        void testClassName() {
            Function classname = functions.get("classname");
            assertThat(classname.apply((Object) null)).isEqualTo("null");
            assertThat(classname.apply("hello")).isEqualTo("java.lang.String");
            assertThat(classname.apply(42)).isEqualTo("java.lang.Integer");
        }

        @Test
        @DisplayName("simpleclassname function | simpleclassname 函数")
        void testSimpleClassName() {
            Function simpleclassname = functions.get("simpleclassname");
            assertThat(simpleclassname.apply((Object) null)).isEqualTo("null");
            assertThat(simpleclassname.apply("hello")).isEqualTo("String");
            assertThat(simpleclassname.apply(42)).isEqualTo("Integer");
        }
    }

    @Nested
    @DisplayName("Default Values Tests | 默认值测试")
    class DefaultValuesTests {

        @Test
        @DisplayName("nvl function | nvl 函数")
        void testNvl() {
            Function nvl = functions.get("nvl");
            assertThat(nvl.apply(null, "default")).isEqualTo("default");
            assertThat(nvl.apply("value", "default")).isEqualTo("value");
            assertThat(nvl.apply(null, null, "third")).isEqualTo("third");
            assertThat(nvl.apply()).isNull();
        }

        @Test
        @DisplayName("coalesce alias | coalesce 别名")
        void testCoalesce() {
            assertThat(functions.get("coalesce")).isNotNull();
        }

        @Test
        @DisplayName("defaultifnull function | defaultifnull 函数")
        void testDefaultIfNull() {
            Function defaultifnull = functions.get("defaultifnull");
            assertThat(defaultifnull.apply(null, "default")).isEqualTo("default");
            assertThat(defaultifnull.apply("value", "default")).isEqualTo("value");
            assertThat(defaultifnull.apply("value")).isEqualTo("value");
            assertThat(defaultifnull.apply()).isNull();
        }

        @Test
        @DisplayName("defaultifempty function | defaultifempty 函数")
        void testDefaultIfEmpty() {
            Function defaultifempty = functions.get("defaultifempty");
            assertThat(defaultifempty.apply(null, "default")).isEqualTo("default");
            assertThat(defaultifempty.apply("", "default")).isEqualTo("default");
            assertThat(defaultifempty.apply(List.of(), "default")).isEqualTo("default");
            assertThat(defaultifempty.apply(Map.of(), "default")).isEqualTo("default");
            assertThat(defaultifempty.apply("value", "default")).isEqualTo("value");
        }

        @Test
        @DisplayName("defaultifblank function | defaultifblank 函数")
        void testDefaultIfBlank() {
            Function defaultifblank = functions.get("defaultifblank");
            assertThat(defaultifblank.apply(null, "default")).isEqualTo("default");
            assertThat(defaultifblank.apply("  ", "default")).isEqualTo("default");
            assertThat(defaultifblank.apply("value", "default")).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("IsType Tests | istype 测试")
    class IsTypeTests {

        @Test
        @DisplayName("istype function | istype 函数")
        void testIsType() {
            Function istype = functions.get("istype");
            assertThat(istype.apply("hello", "string")).isEqualTo(true);
            assertThat(istype.apply(42, "number")).isEqualTo(true);
            assertThat(istype.apply(42, "integer")).isEqualTo(true);
            assertThat(istype.apply(42, "int")).isEqualTo(true);
            assertThat(istype.apply(42L, "long")).isEqualTo(true);
            assertThat(istype.apply(3.14, "double")).isEqualTo(true);
            assertThat(istype.apply(3.14, "decimal")).isEqualTo(true);
            assertThat(istype.apply(true, "boolean")).isEqualTo(true);
            assertThat(istype.apply(true, "bool")).isEqualTo(true);
            assertThat(istype.apply(List.of(), "list")).isEqualTo(true);
            assertThat(istype.apply(Map.of(), "map")).isEqualTo(true);
            assertThat(istype.apply(Set.of(), "set")).isEqualTo(true);
            assertThat(istype.apply(List.of(), "collection")).isEqualTo(true);
            assertThat(istype.apply((Object) new int[]{}, "array")).isEqualTo(true);
            assertThat(istype.apply(LocalDate.now(), "date")).isEqualTo(true);
            assertThat(istype.apply(LocalDateTime.now(), "datetime")).isEqualTo(true);
            assertThat(istype.apply("hello", "unknown")).isEqualTo(false);
            assertThat(istype.apply((Object) null, "string")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("GetFunctions Tests | getFunctions 测试")
    class GetFunctionsTests {

        @Test
        @DisplayName("getFunctions returns all functions | getFunctions 返回所有函数")
        void testGetFunctionsReturnsAll() {
            Map<String, Function> funcs = TypeFunctions.getFunctions();
            assertThat(funcs).isNotEmpty();
            assertThat(funcs).containsKey("isnull");
            assertThat(funcs).containsKey("typeof");
            assertThat(funcs).containsKey("tostring");
        }
    }
}
