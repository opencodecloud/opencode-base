package cloud.opencode.base.deepclone.contract;

import cloud.opencode.base.deepclone.Cloner;
import cloud.opencode.base.deepclone.OpenClone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * DeepCloneable 接口测试类
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-deepclone V1.0.0
 */
@DisplayName("DeepCloneable 接口测试")
class DeepCloneableTest {

    // Test implementation
    public static class Product implements DeepCloneable<Product> {
        private String id;
        private String name;
        private double price;

        public Product() {}

        public Product(String id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }

        @Override
        public Product deepClone() {
            return new Product(id + "-cloned", name, price * 2);
        }

        @Override
        public Product deepClone(Cloner cloner) {
            // Override to avoid infinite recursion - use our custom deepClone() instead
            return deepClone();
        }
    }

    // Test implementation using default method
    public static class SimpleProduct implements DeepCloneable<SimpleProduct> {
        private String name;
        private int quantity;

        public SimpleProduct() {}

        public SimpleProduct(String name, int quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }

        @Override
        public SimpleProduct deepClone() {
            return new SimpleProduct(name + "-copy", quantity);
        }

        @Override
        public SimpleProduct deepClone(Cloner cloner) {
            // Override to avoid infinite recursion - use our custom deepClone() instead
            return deepClone();
        }
    }

    @Nested
    @DisplayName("deepClone() 测试")
    class DeepCloneTests {

        @Test
        @DisplayName("使用自定义deepClone实现")
        void testDeepClone() {
            Product original = new Product("P001", "Widget", 9.99);

            Product cloned = original.deepClone();

            assertThat(cloned).isNotSameAs(original);
            assertThat(cloned.getId()).isEqualTo("P001-cloned");
            assertThat(cloned.getName()).isEqualTo("Widget");
            assertThat(cloned.getPrice()).isEqualTo(19.98);
        }

        @Test
        @DisplayName("多次克隆返回不同实例")
        void testMultipleClones() {
            Product original = new Product("P001", "Widget", 9.99);

            Product cloned1 = original.deepClone();
            Product cloned2 = original.deepClone();

            assertThat(cloned1).isNotSameAs(cloned2);
        }
    }

    @Nested
    @DisplayName("deepClone(Cloner) 测试")
    class DeepCloneWithClonerTests {

        @Test
        @DisplayName("使用Cloner克隆")
        void testDeepCloneWithCloner() {
            SimpleProduct original = new SimpleProduct("Item", 10);
            Cloner cloner = OpenClone.getDefaultCloner();

            SimpleProduct cloned = original.deepClone(cloner);

            assertThat(cloned).isNotSameAs(original);
            // The default deepClone method calls cloner.clone() which uses reflection
            // But since SimpleProduct implements DeepCloneable, it uses deepClone()
            assertThat(cloned.getName()).isEqualTo("Item-copy");
        }
    }

    @Nested
    @DisplayName("与OpenClone集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("OpenClone使用DeepCloneable实现")
        void testOpenCloneUsesDeepCloneable() {
            Product original = new Product("P001", "Widget", 9.99);

            Product cloned = OpenClone.clone(original);

            // OpenClone should detect DeepCloneable and use deepClone()
            assertThat(cloned.getId()).isEqualTo("P001-cloned");
            assertThat(cloned.getPrice()).isEqualTo(19.98);
        }

        @Test
        @DisplayName("嵌套DeepCloneable对象")
        void testNestedDeepCloneable() {
            SimpleProduct original = new SimpleProduct("Nested", 5);

            SimpleProduct cloned = OpenClone.clone(original);

            assertThat(cloned.getName()).isEqualTo("Nested-copy");
        }
    }
}
