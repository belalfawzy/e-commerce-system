import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Product cheese = new Product("Cheese", 100.0, 10, 0.4,LocalDate.now().plusDays(10));
        Product biscuits = new Product("Biscuits", 150.0, 5, 0.7,LocalDate.now().plusDays(20));
        Product tv = new Product("TV", 10000.0, 3, 15.0);
        Product scratchCard = new Product("Scratch Card", 10.0, 100);

        Customer c1 = new Customer("Belal", 20000.0);
        Customer c2 = new Customer("Issa", 100.0);
        Customer c3 = new Customer("Lotfy", 1000.0);
        Customer c4 = new Customer("Noussa", 1000.0);
        Customer c5 = new Customer("Sayed", 1000.0);
        Product expiredProduct = new Product("Expired product", 100.0, 10, 0.4,LocalDate.now().minusDays(1));

        c1.addToCart(cheese, 2);
        c1.addToCart(biscuits, 1);
        c1.addToCart(scratchCard, 5);
        c1.checkout();
        System.out.println("-----------------------------------------");

        c2.addToCart(cheese, 1);
        c2.checkout();
        System.out.println("-----------------------------------------");

        c3.addToCart(expiredProduct, 1);
        c3.checkout();
        System.out.println("-----------------------------------------");

        c4.addToCart(biscuits, 10);
        c4.checkout();
        System.out.println("-----------------------------------------");

        c5.checkout();
    }
    public static class Product{
        private String name;
        private double price;
        private int quantity;
        private Double weight;
        private LocalDate expireDate;
        public Product(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
        public Product(String name, double price, int quantity, Double weight) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.weight = weight;
        }
        public Product(String name, double price, int quantity, LocalDate expireDate) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.expireDate = expireDate;
        }
        public Product(String name, double price, int quantity, Double weight, LocalDate expireDate) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.weight = weight;
            this.expireDate = expireDate;
        }

        public String getName() {
            return name;
        }
        public double getPrice() {
            return price;
        }
        public int getQuantity() {
            return quantity;
        }
        public Double getWeight() {
            return weight;
        }
        public LocalDate getExpireDate() {
            return expireDate;
        }
        public boolean isShippable() {
            return weight != null;
        }
        public boolean isExpirable() {
            return expireDate != null;
        }
        public boolean isExpired() {
            return isExpirable() && expireDate.isBefore(LocalDate.now());
        }
        public void decreaseQuantity(int q) {
            this.quantity -= q;
        }
    }
    public static class CartItem{
        Product product;
        int quantity;
        public CartItem(Product product, int quantity){
            this.product = product;
            this.quantity = quantity;
        }
        public int getQuantity() {
            return quantity;
        }
        public Product getProduct() {
            return product;
        }
    }
    public static class Cart{
        List<CartItem> items = new ArrayList<>();
        public void Add(Product p, int q){
            try {
                if(q <= 0){
                    throw new NullPointerException("Quantity Cannot be less than or equal 0");
                }
                if(q > p.getQuantity()){
                    throw new NullPointerException("this Quantity unavailable");
                }
                if(p.isExpired()){
                    throw new NullPointerException("this Product is Expired");
                }
                if(p.getQuantity() <= 0){
                    throw new NullPointerException("this Product out of the stock");
                }
                items.add(new CartItem(p,q));
                System.out.printf("%s added to card successfully \n",p.getName());
            }
            catch (NullPointerException ex){
                System.out.println(ex.getMessage());
            }
        }
        public double calculateSubtotal() {
            return items.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
        }
        public List<CartItem> getItems(){
            return items;
        }
        public void clear() {
            items.clear();
        }
    }

    public static class Customer{
        String name;
        double balance;
        Cart cart = new Cart();
        public Customer(String name, double balance){
            this.name = name;
            this.balance = balance;
        }

        public double getBalance() {
            return balance;
        }

        public String getName() {
            return name;
        }
        public void addToCart(Product p, int q){
            cart.Add(p,q);
        }
        public void checkout(){
            List<CartItem> items = cart.getItems();
            try{
                if(items.isEmpty()){
                    throw new NullPointerException("Your Cart is EMPTY!!");
                }
                double subtotal = cart.calculateSubtotal();
                List<ShippableItem> shippableItems = new ArrayList<>();
                for (CartItem item : items) {
                    Product p = item.getProduct();
                    if (p.isShippable()) {
                        for (int i = 0; i < item.getQuantity(); i++) {
                            shippableItems.add(new ShippableItem() {
                                @Override
                                public String getName() { return p.getName(); }
                                @Override
                                public double getWeight() { return p.getWeight(); }
                            });
                        }
                    }
                }
                double tax = shippableItems.size() * 10;
                double total = subtotal + tax;
                if (balance < total) {
                    throw new NullPointerException("there is not enough balance");
                }
                for (CartItem item : items) {
                    Product p = item.getProduct();
                    p.decreaseQuantity(item.getQuantity());
                }
                balance -= total;

                if (!shippableItems.isEmpty()) {
                    new ShippingService().ship(shippableItems);
                }

                receipt(subtotal, tax, total);
                cart.clear();
            }
            catch (NullPointerException ex){
                System.out.println(ex.getMessage());
            }
        }
        public void receipt(double subtotal, double tax, double total) {
            System.out.println("\n** Checkout receipt **");
            for (CartItem item : cart.getItems()) {
                Product p = item.getProduct();
                System.out.printf("%dx %s %.0f%n", item.getQuantity(), p.getName(), p.getPrice() * item.getQuantity());
            }
            System.out.println("-------------------------");
            System.out.printf("Subtotal: %.2f%n", subtotal);
            System.out.printf("Shipping: %.2f%n", tax);
            System.out.printf("Total; %.2f%n", total);
        }
    }
    public static interface ShippableItem {
        String getName();
        double getWeight();
    }
    public static class ShippingService {
        public void ship(List<ShippableItem> items) {
            Map<String, Integer> mpQuantity = new HashMap<>();
            Map<String, Double> mpWeigth = new HashMap<>();
            double result = 0.0;

            for (ShippableItem item : items) {
                String name = item.getName();
                mpQuantity.put(name, mpQuantity.getOrDefault(name, 0) + 1);
                mpWeigth.put(name, item.getWeight());
                result += item.getWeight();
            }
            System.out.println("** Shipment notice **");
            for (String pName : mpQuantity.keySet()) {
                System.out.printf("%dx %s %.0fg%n", mpQuantity.get(pName), pName, mpWeigth.get(pName) * 1000);
            }
            System.out.printf("Total package weight %.1fkg%n", result);
        }
    }
}