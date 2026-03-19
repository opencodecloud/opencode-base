package cloud.opencode.base.deepclone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * CloneContext 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("CloneContext 测试")
class CloneContextTest {

    private CloneContext context;

    @BeforeEach
    void setUp() {
        context = CloneContext.create();
    }

    @Nested
    @DisplayName("create() 工厂方法测试")
    class CreateTests {

        @Test
        @DisplayName("create() 创建默认上下文")
        void testCreate() {
            CloneContext ctx = CloneContext.create();

            assertThat(ctx).isNotNull();
            assertThat(ctx.getDepth()).isEqualTo(0);
            assertThat(ctx.getMaxDepth()).isEqualTo(100);
        }

        @Test
        @DisplayName("create(maxDepth) 创建指定深度上下文")
        void testCreateWithMaxDepth() {
            CloneContext ctx = CloneContext.create(50);

            assertThat(ctx.getMaxDepth()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("克隆跟踪测试")
    class CloneTrackingTests {

        @Test
        @DisplayName("getClonedObjects() 返回不可变映射")
        void testGetClonedObjects() {
            Map<Object, Object> objects = context.getClonedObjects();

            assertThat(objects).isEmpty();
            assertThatThrownBy(() -> objects.put("key", "value"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("isCloned() 未克隆对象返回false")
        void testIsClonedFalse() {
            Object obj = new Object();

            assertThat(context.isCloned(obj)).isFalse();
        }

        @Test
        @DisplayName("isCloned() 已克隆对象返回true")
        void testIsClonedTrue() {
            Object original = new Object();
            Object cloned = new Object();
            context.registerCloned(original, cloned);

            assertThat(context.isCloned(original)).isTrue();
        }

        @Test
        @DisplayName("getCloned() 获取已克隆对象")
        void testGetCloned() {
            String original = "original";
            String cloned = "cloned";
            context.registerCloned(original, cloned);

            String result = context.getCloned(original);

            assertThat(result).isEqualTo("cloned");
        }

        @Test
        @DisplayName("getCloned() 未克隆对象返回null")
        void testGetClonedNull() {
            Object obj = new Object();

            Object result = context.getCloned(obj);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("registerCloned() 注册克隆对象")
        void testRegisterCloned() {
            Object original = new Object();
            Object cloned = new Object();

            context.registerCloned(original, cloned);

            assertThat(context.isCloned(original)).isTrue();
            assertThat((Object) context.getCloned(original)).isSameAs(cloned);
        }

        @Test
        @DisplayName("incrementSkipped() 增加跳过计数")
        void testIncrementSkipped() {
            context.incrementSkipped();
            context.incrementSkipped();

            CloneContext.CloneStatistics stats = context.getStatistics();
            assertThat(stats.objectsSkipped()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("深度跟踪测试")
    class DepthTrackingTests {

        @Test
        @DisplayName("getDepth() 初始深度为0")
        void testGetDepthInitial() {
            assertThat(context.getDepth()).isEqualTo(0);
        }

        @Test
        @DisplayName("incrementDepth() 增加深度")
        void testIncrementDepth() {
            int newDepth = context.incrementDepth();

            assertThat(newDepth).isEqualTo(1);
            assertThat(context.getDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("decrementDepth() 减少深度")
        void testDecrementDepth() {
            context.incrementDepth();
            context.incrementDepth();

            int newDepth = context.decrementDepth();

            assertThat(newDepth).isEqualTo(1);
            assertThat(context.getDepth()).isEqualTo(1);
        }

        @Test
        @DisplayName("isMaxDepthExceeded() 未超过返回false")
        void testIsMaxDepthExceededFalse() {
            assertThat(context.isMaxDepthExceeded()).isFalse();
        }

        @Test
        @DisplayName("isMaxDepthExceeded() 超过返回true")
        void testIsMaxDepthExceededTrue() {
            CloneContext ctx = CloneContext.create(2);
            ctx.incrementDepth();
            ctx.incrementDepth();
            ctx.incrementDepth();

            assertThat(ctx.isMaxDepthExceeded()).isTrue();
        }
    }

    @Nested
    @DisplayName("路径跟踪测试")
    class PathTrackingTests {

        @Test
        @DisplayName("getPath() 初始路径为空")
        void testGetPathInitial() {
            assertThat(context.getPath()).isEmpty();
        }

        @Test
        @DisplayName("pushPath() 压入路径元素")
        void testPushPath() {
            context.pushPath("field1");
            context.pushPath("field2");

            List<String> path = context.getPath();

            assertThat(path).containsExactly("field2", "field1");
        }

        @Test
        @DisplayName("popPath() 弹出路径元素")
        void testPopPath() {
            context.pushPath("field1");
            context.pushPath("field2");
            context.popPath();

            List<String> path = context.getPath();

            assertThat(path).containsExactly("field1");
        }

        @Test
        @DisplayName("popPath() 空路径不抛异常")
        void testPopPathEmpty() {
            assertThatCode(() -> context.popPath()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("getPathString() 返回路径字符串")
        void testGetPathString() {
            context.pushPath("root");
            context.pushPath("child");
            context.pushPath("leaf");

            String pathString = context.getPathString();

            assertThat(pathString).isEqualTo("leaf.child.root");
        }

        @Test
        @DisplayName("getPathString() 空路径返回空字符串")
        void testGetPathStringEmpty() {
            assertThat(context.getPathString()).isEmpty();
        }
    }

    @Nested
    @DisplayName("统计测试")
    class StatisticsTests {

        @Test
        @DisplayName("getStatistics() 返回统计信息")
        void testGetStatistics() {
            // Register some objects
            context.registerCloned("a", "a-clone");
            context.registerCloned("b", "b-clone");
            context.incrementSkipped();
            context.incrementDepth();
            context.incrementDepth();
            context.incrementDepth();

            CloneContext.CloneStatistics stats = context.getStatistics();

            assertThat(stats.objectsCloned()).isEqualTo(2);
            assertThat(stats.objectsSkipped()).isEqualTo(1);
            assertThat(stats.maxDepthReached()).isEqualTo(3);
            assertThat(stats.elapsedNanos()).isGreaterThan(0);
        }

        @Test
        @DisplayName("CloneStatistics.elapsedMillis() 返回毫秒")
        void testElapsedMillis() {
            CloneContext.CloneStatistics stats = context.getStatistics();

            assertThat(stats.elapsedMillis()).isGreaterThanOrEqualTo(0);
        }
    }
}
