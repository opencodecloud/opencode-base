package cloud.opencode.base.feature.proxy;

import cloud.opencode.base.feature.FeatureContext;
import cloud.opencode.base.feature.annotation.FeatureVariant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;

/**
 * VariantRouter 测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-feature V1.0.0
 */
@DisplayName("VariantRouter 测试")
class VariantRouterTest {

    @Nested
    @DisplayName("builder() 测试")
    class BuilderTests {

        @Test
        @DisplayName("创建Builder")
        void testBuilder() {
            VariantRouter.Builder<String> builder = VariantRouter.builder("test-feature");

            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("null featureKey抛出异常")
        void testNullFeatureKey() {
            assertThatThrownBy(() -> VariantRouter.builder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("空白featureKey抛出异常")
        void testBlankFeatureKey() {
            assertThatThrownBy(() -> VariantRouter.builder("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }

        @Test
        @DisplayName("添加变体")
        void testAddVariant() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .build();

            assertThat(router.getVariantIds()).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("null变体ID抛出异常")
        void testNullVariantId() {
            assertThatThrownBy(() -> VariantRouter.<String>builder("test")
                    .variant(null, "impl", 50))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("空白变体ID抛出异常")
        void testBlankVariantId() {
            assertThatThrownBy(() -> VariantRouter.<String>builder("test")
                    .variant("  ", "impl", 50))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null实现抛出异常")
        void testNullImplementation() {
            assertThatThrownBy(() -> VariantRouter.<String>builder("test")
                    .variant("A", (String) null, 50))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("使用Supplier添加变体")
        void testVariantWithSupplier() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", () -> "implA", 50)
                    .variant("B", () -> "implB", 50)
                    .build();

            assertThat(router.getVariantIds()).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("设置默认变体")
        void testDefaultVariant() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .defaultVariant("B")
                    .build();

            assertThat(router.getVariantIds()).contains("B");
        }

        @Test
        @DisplayName("设置盐值")
        void testSalt() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .salt("my-salt")
                    .build();

            assertThat(router).isNotNull();
        }

        @Test
        @DisplayName("null盐值使用空字符串")
        void testNullSalt() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .salt(null)
                    .build();

            assertThat(router).isNotNull();
        }

        @Test
        @DisplayName("无变体时build抛出异常")
        void testBuildWithNoVariants() {
            assertThatThrownBy(() -> VariantRouter.<String>builder("test").build())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("variant");
        }

        @Test
        @DisplayName("百分比规范化")
        void testPercentageNormalization() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA")
                    .variant("B", "implB")
                    .build();

            assertThat(router.getVariantIds()).hasSize(2);
        }

        @Test
        @DisplayName("百分比超过100时截断")
        void testPercentageClamp() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 150)
                    .build();

            assertThat(router.getVariant("A")).isPresent();
        }

        @Test
        @DisplayName("负百分比变为0")
        void testNegativePercentage() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", -10)
                    .variant("B", "implB", 100)
                    .build();

            assertThat(router.getVariantIds()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("route() 测试")
    class RouteTests {

        @Test
        @DisplayName("路由到变体")
        void testRoute() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 100)
                    .build();

            String result = router.route(FeatureContext.empty());

            assertThat(result).isEqualTo("implA");
        }

        @Test
        @DisplayName("无参数路由")
        void testRouteNoArgs() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 100)
                    .build();

            String result = router.route();

            assertThat(result).isEqualTo("implA");
        }

        @Test
        @DisplayName("为用户路由")
        void testRouteForUser() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .build();

            String result = router.routeForUser("user123");

            assertThat(result).isIn("implA", "implB");
        }

        @Test
        @DisplayName("一致性哈希")
        void testConsistentRouting() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .build();

            String result1 = router.routeForUser("user123");
            String result2 = router.routeForUser("user123");
            String result3 = router.routeForUser("user123");

            assertThat(result1).isEqualTo(result2).isEqualTo(result3);
        }

        @Test
        @DisplayName("变体覆盖")
        void testVariantOverride() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .build();

            Map<String, Object> attrs = new HashMap<>();
            attrs.put("variant", "B");
            FeatureContext ctx = FeatureContext.builder()
                    .userId("user123")
                    .attributes(attrs)
                    .build();

            String result = router.route(ctx);

            assertThat(result).isEqualTo("implB");
        }

        @Test
        @DisplayName("单个变体直接返回")
        void testSingleVariant() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 100)
                    .build();

            String result = router.route(FeatureContext.ofUser("anyuser"));

            assertThat(result).isEqualTo("implA");
        }
    }

    @Nested
    @DisplayName("execute() 测试")
    class ExecuteTests {

        @Test
        @DisplayName("执行有返回值的操作")
        void testExecute() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "hello", 100)
                    .build();

            Integer result = router.execute(FeatureContext.empty(), String::length);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("执行无返回值的操作")
        void testExecuteVoid() {
            AtomicBoolean called = new AtomicBoolean(false);
            VariantRouter<Runnable> router = VariantRouter.<Runnable>builder("test")
                    .variant("A", () -> called.set(true), 100)
                    .build();

            router.executeVoid(FeatureContext.empty(), Runnable::run);

            assertThat(called.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("getVariant() 测试")
    class GetVariantTests {

        @Test
        @DisplayName("获取存在的变体")
        void testGetExistingVariant() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 100)
                    .build();

            Optional<String> result = router.getVariant("A");

            assertThat(result).isPresent().contains("implA");
        }

        @Test
        @DisplayName("获取不存在的变体")
        void testGetNonExistingVariant() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 100)
                    .build();

            Optional<String> result = router.getVariant("B");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getSelectedVariantId() 测试")
    class GetSelectedVariantIdTests {

        @Test
        @DisplayName("获取选择的变体ID")
        void testGetSelectedVariantId() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 100)
                    .build();

            String variantId = router.getSelectedVariantId(FeatureContext.empty());

            assertThat(variantId).isEqualTo("A");
        }
    }

    @Nested
    @DisplayName("getVariantIds() 测试")
    class GetVariantIdsTests {

        @Test
        @DisplayName("获取所有变体ID")
        void testGetVariantIds() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .build();

            Set<String> ids = router.getVariantIds();

            assertThat(ids).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("返回不可修改的集合")
        void testUnmodifiableSet() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 100)
                    .build();

            Set<String> ids = router.getVariantIds();

            assertThatThrownBy(() -> ids.add("B"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("getFeatureKey() 测试")
    class GetFeatureKeyTests {

        @Test
        @DisplayName("获取功能键")
        void testGetFeatureKey() {
            VariantRouter<String> router = VariantRouter.<String>builder("my-feature")
                    .variant("A", "implA", 100)
                    .build();

            assertThat(router.getFeatureKey()).isEqualTo("my-feature");
        }
    }

    @Nested
    @DisplayName("fromAnnotations() 测试")
    class FromAnnotationsTests {

        public static class AnnotatedService {
            @FeatureVariant(feature = "checkout", variant = "A", percentage = 50)
            public String checkoutA() {
                return "A";
            }

            @FeatureVariant(feature = "checkout", variant = "B", percentage = 50)
            public String checkoutB() {
                return "B";
            }

            public String notAnnotated() {
                return "not";
            }
        }

        @Test
        @DisplayName("从注解创建路由器")
        void testFromAnnotations() {
            AnnotatedService service = new AnnotatedService();

            VariantRouter<VariantRouter.MethodVariant<AnnotatedService>> router =
                    VariantRouter.fromAnnotations("checkout", service);

            assertThat(router.getVariantIds()).containsExactlyInAnyOrder("A", "B");
        }

        @Test
        @DisplayName("null featureKey抛出异常")
        void testFromAnnotationsNullFeatureKey() {
            AnnotatedService service = new AnnotatedService();

            assertThatThrownBy(() -> VariantRouter.fromAnnotations(null, service))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("null target抛出异常")
        void testFromAnnotationsNullTarget() {
            assertThatThrownBy(() -> VariantRouter.fromAnnotations("checkout", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("MethodVariant 测试")
    class MethodVariantTests {

        public static class TestTarget {
            public String greet(String name) {
                return "Hello, " + name;
            }
        }

        @Test
        @DisplayName("调用方法")
        void testInvoke() throws Exception {
            TestTarget target = new TestTarget();
            VariantRouter.MethodVariant<TestTarget> methodVariant =
                    new VariantRouter.MethodVariant<>(target, TestTarget.class.getMethod("greet", String.class));

            Object result = methodVariant.invoke("World");

            assertThat(result).isEqualTo("Hello, World");
        }

        @Test
        @DisplayName("获取方法名")
        void testGetMethodName() throws Exception {
            TestTarget target = new TestTarget();
            VariantRouter.MethodVariant<TestTarget> methodVariant =
                    new VariantRouter.MethodVariant<>(target, TestTarget.class.getMethod("greet", String.class));

            assertThat(methodVariant.getMethodName()).isEqualTo("greet");
        }
    }

    @Nested
    @DisplayName("NoVariantException 测试")
    class NoVariantExceptionTests {

        @Test
        @DisplayName("创建异常")
        void testCreateException() {
            VariantRouter.NoVariantException ex = new VariantRouter.NoVariantException("my-feature");

            assertThat(ex.getFeatureKey()).isEqualTo("my-feature");
            assertThat(ex.getMessage()).contains("my-feature");
        }
    }

    @Nested
    @DisplayName("百分比分布测试")
    class PercentageDistributionTests {

        @Test
        @DisplayName("流量按百分比分配")
        void testTrafficDistribution() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 70)
                    .variant("B", "implB", 30)
                    .build();

            int countA = 0;
            int countB = 0;
            int total = 1000;

            for (int i = 0; i < total; i++) {
                String result = router.routeForUser("user" + i);
                if ("implA".equals(result)) {
                    countA++;
                } else {
                    countB++;
                }
            }

            assertThat(countA).isBetween(600, 800);
            assertThat(countB).isBetween(200, 400);
        }

        @Test
        @DisplayName("混合指定和未指定百分比")
        void testMixedPercentages() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB")
                    .variant("C", "implC")
                    .build();

            assertThat(router.getVariantIds()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("空用户ID测试")
    class EmptyUserIdTests {

        @Test
        @DisplayName("空用户ID随机选择")
        void testEmptyUserId() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .build();

            String result = router.route(FeatureContext.empty());

            assertThat(result).isIn("implA", "implB");
        }

        @Test
        @DisplayName("null用户ID随机选择")
        void testNullUserId() {
            VariantRouter<String> router = VariantRouter.<String>builder("test")
                    .variant("A", "implA", 50)
                    .variant("B", "implB", 50)
                    .build();

            String result = router.route(FeatureContext.builder().build());

            assertThat(result).isIn("implA", "implB");
        }
    }
}
