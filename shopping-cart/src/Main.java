import model.CartItem;
import model.Product;
import model.User;
import model.WalletRequest;
import service.AuthService;
import service.CartService;
import service.OrderService;
import service.ProductService;
import service.WalletRequestService;
import service.WalletService;
import util.InputUtil;

import java.util.List;

public class Main {
    private static AuthService authService = new AuthService();
    private static ProductService productService = new ProductService();
    private static CartService cartService = new CartService();
    private static WalletService walletService = new WalletService();
    private static WalletRequestService walletRequestService = new WalletRequestService();
    private static OrderService orderService = new OrderService();

    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("Welcome to the Console Shopping Cart System");
        
        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else if ("ADMIN".equals(currentUser.getRole())) {
                showAdminMenu();
            } else if ("CUSTOMER".equals(currentUser.getRole())) {
                showCustomerMenu();
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        int choice = InputUtil.getInt("Select an option: ");

        switch (choice) {
            case 1:
                String loginUser = InputUtil.getString("Username: ");
                String loginPass = InputUtil.getString("Password: ");
                currentUser = authService.login(loginUser, loginPass);
                if (currentUser == null) {
                    System.out.println("Invalid credentials.");
                } else {
                    System.out.println("Login successful! Welcome " + currentUser.getUsername());
                }
                break;
            case 2:
                String regUser = InputUtil.getString("Username: ");
                String regPass = InputUtil.getString("Password: ");
                if (authService.register(regUser, regPass)) {
                    System.out.println("Registration successful. You can now login.");
                }
                break;
            case 3:
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void showAdminMenu() {
        System.out.println("\n--- ADMIN MENU ---");
        System.out.println("1. Add Product");
        System.out.println("2. Modify Product");
        System.out.println("3. Delete Product");
        System.out.println("4. View All Products");
        System.out.println("5. View All Transactions");
        System.out.println("6. Handle Wallet Requests");
        System.out.println("7. Logout (Back)");
        
        int choice = InputUtil.getInt("Select an option: ");

        switch (choice) {
            case 1:
                String name = InputUtil.getString("Product Name: ");
                double price = InputUtil.getDouble("Price: ");
                int stock = InputUtil.getInt("Stock: ");
                java.sql.Date expiry = InputUtil.getDate("Expiry Date");
                productService.addProduct(name, price, stock, expiry);
                break;
            case 2:
                int modId = InputUtil.getInt("Product ID to modify: ");
                int newStock = InputUtil.getInt("New Stock: ");
                java.sql.Date newExpiry = InputUtil.getDate("New Expiry Date");
                productService.modifyProduct(modId, newStock, newExpiry);
                break;
            case 3:
                int delId = InputUtil.getInt("Product ID to delete: ");
                productService.deleteProduct(delId);
                break;
            case 4:
                List<Product> products = productService.getAllProducts();
                System.out.println("\n--- All Products ---");
                if (products.isEmpty()) {
                    System.out.println("No products in the system.");
                } else {
                    for (Product p : products) {
                        System.out.printf("ID: %d | Name: %s | Price: %.2f | Stock: %d | Expiry: %s | Active: %b\n",
                                p.getId(), p.getName(), p.getPrice(), p.getStock(), p.getExpiryDate(), p.isActive());
                    }
                }
                break;
            case 5:
                orderService.viewAllTransactionsAdmin();
                break;
            case 6:
                handleWalletRequestsMenu();
                break;
            case 7:
                currentUser = null;
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void handleWalletRequestsMenu() {
        List<WalletRequest> requests = walletRequestService.getPendingRequests();
        if (requests.isEmpty()) {
            System.out.println("No pending wallet requests.");
            return;
        }

        System.out.println("\n--- Pending Wallet Requests ---");
        for (WalletRequest req : requests) {
            System.out.printf("Request ID: %d | User ID: %d | Amount: %.2f | Date: %s\n",
                    req.getId(), req.getUserId(), req.getAmount(), req.getRequestDate());
        }

        int reqId = InputUtil.getInt("Enter Request ID to handle (or 0 to cancel): ");
        if (reqId == 0) return;

        int action = InputUtil.getInt("1 to Approve, 2 to Reject: ");
        if (action == 1) {
            walletRequestService.handleRequest(reqId, true);
        } else if (action == 2) {
            walletRequestService.handleRequest(reqId, false);
        } else {
            System.out.println("Invalid action.");
        }
    }

    private static void showCustomerMenu() {
        System.out.println("\n--- CUSTOMER MENU ---");
        System.out.println("1. View Products");
        System.out.println("2. Add to Cart");
        System.out.println("3. View Cart");
        System.out.println("4. Modify/Delete Cart Items");
        System.out.println("5. Check Wallet Balance");
        System.out.println("6. Request Money");
        System.out.println("7. Checkout");
        System.out.println("8. View Transactions");
        System.out.println("9. Logout (Back)");

        int choice = InputUtil.getInt("Select an option: ");

        switch (choice) {
            case 1:
                List<Product> availableProducts = productService.getActiveAvailableProducts();
                System.out.println("\n--- Available Products ---");
                if (availableProducts.isEmpty()) {
                    System.out.println("No products are currently available.");
                } else {
                    for (Product p : availableProducts) {
                        System.out.printf("ID: %d | Name: %s | Price: %.2f | Stock: %d | Expiry: %s\n",
                                p.getId(), p.getName(), p.getPrice(), p.getStock(), p.getExpiryDate());
                    }
                }
                break;
            case 2:
                int addProductId = InputUtil.getInt("Product ID to add: ");
                int quantity = InputUtil.getInt("Quantity: ");
                cartService.addToCart(currentUser.getId(), addProductId, quantity);
                break;
            case 3:
                List<CartItem> cartItems = cartService.viewCart(currentUser.getId());
                System.out.println("\n--- Your Cart ---");
                if (cartItems.isEmpty()) {
                    System.out.println("Your cart is currently empty.");
                } else {
                    double total = 0;
                    for (CartItem item : cartItems) {
                        double subtotal = item.getPrice() * item.getQuantity();
                        total += subtotal;
                        System.out.printf("Product ID: %d | Name: %s | Qty: %d | Price: %.2f | Subtotal: %.2f\n",
                                item.getProductId(), item.getProductName(), item.getQuantity(), item.getPrice(), subtotal);
                    }
                    System.out.printf("Estimated Total: %.2f\n", total);
                }
                break;
            case 4:
                int modProductId = InputUtil.getInt("Product ID to modify: ");
                int newQty = InputUtil.getInt("New Quantity (0 to delete): ");
                cartService.modifyCartItem(currentUser.getId(), modProductId, newQty);
                break;
            case 5:
                double balance = walletService.getBalance(currentUser.getId());
                System.out.printf("Your current wallet balance is: $%.2f\n", balance);
                break;
            case 6:
                double reqAmount = InputUtil.getDouble("Amount to request: ");
                walletRequestService.requestMoney(currentUser.getId(), reqAmount);
                break;
            case 7:
                orderService.checkout(currentUser.getId());
                break;
            case 8:
                orderService.viewUserTransactions(currentUser.getId());
                break;
            case 9:
                currentUser = null;
                break;
            default:
                System.out.println("Invalid option.");
        }
    }
}
