import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class Main {
    public static void main(String[] args) {
        Manager manager = new Manager("John");
        Cashier cashier = new Cashier("Alice");
        List<Customer> customers = new ArrayList<>();
        loadProductsFromJson(manager, "data.json");

        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        // Role selection
        while (running) {
            System.out.println("Select your role:");
            System.out.println("1. Customer");
            System.out.println("2. Cashier");
            System.out.println("3. Manager");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int roleChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (roleChoice) {
                case 1: // Customer
                    handleCustomerRole(customers, scanner);
                    break;
                case 2: // Cashier
                    handleCashierRole(cashier, manager, customers, scanner);
                    break;
                case 3: // Manager
                    handleManagerRole(manager, scanner);
                    break;
                case 4: // Exit
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
        System.out.println("Thank you for using the system!");
    }

    private static void loadProductsFromJson(Manager manager, String filename) {
        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(filename)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray productsArray = (JSONArray) jsonObject.get("products");

            for (Object obj : productsArray) {
                JSONObject productJson = (JSONObject) obj;
                String name = (String) productJson.get("name");
                double price = (double) productJson.get("price");
                long quantity = (long) productJson.get("quantity");
                String type = (String) productJson.get("type");

                ProductType productType = ProductType.valueOf(type.toUpperCase());
                manager.addProduct(new Product(name, price, (int) quantity, productType));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // Customer role
    private static void handleCustomerRole(List<Customer> customers, Scanner scanner) {
        System.out.print("Enter your Membership ID: ");
        String membershipId = scanner.nextLine();

        Customer customer = findCustomerByMembership(customers, membershipId);
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        boolean customerRunning = true;
        while (customerRunning) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Bill History");
            System.out.println("2. Add Money to Wallet");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int customerChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (customerChoice) {
                case 1:
                    System.out.print("Enter Bill Number or leave blank for all bills: ");
                    String input = scanner.nextLine();
                    if (input.isEmpty()) {
                        customer.viewAllBills(); // แสดงบิลทั้งหมด
                    } else {
                        try {
                            int billNumber = Integer.parseInt(input);
                            customer.viewBill(billNumber); // แสดงบิลเฉพาะบิลที่ระบุ
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid Bill Number. Please enter a valid number.");
                        }
                    }
                    break;
                case 2:
                    System.out.print("Enter amount to add to wallet: ");
                    double amount = scanner.nextDouble();
                    customer.addMoney(amount);
                    System.out.println("Money added successfully.");
                    break;
                case 3:
                    customerRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Cashier role
    private static void handleCashierRole(Cashier cashier, Manager manager, List<Customer> customers, Scanner scanner) {
        System.out.print("Enter Membership ID (leave blank if not a member): ");
        String membershipId = scanner.nextLine();

        Customer customer = null;
        if (!membershipId.isEmpty()) {
            customer = findCustomerByMembership(customers, membershipId);
        }

        if (customer == null) {
            System.out.println("Customer not found. Proceeding as non-member.");
            customer = new Customer("Guest", 100.0); // Create guest customer
            customers.add(customer);
        }

        boolean cashierRunning = true;
        while (cashierRunning) {
            System.out.println("\n--- Cashier Menu ---");
            System.out.println("1. Process Payment");
            System.out.println("2. Register Membership");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int cashierChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (cashierChoice) {
                case 1:
                    cashier.displayProducts(manager.getProducts());

                    List<Product> products = new ArrayList<>();
                    boolean selectingProducts = true;

                    while (selectingProducts) {
                        System.out.print("Enter Product ID (or 'done' to finish): ");
                        String input = scanner.nextLine();

                        if (input.equalsIgnoreCase("done")) {
                            selectingProducts = false;
                            continue; // Exit product selection loop
                        }

                        try {
                            int productId = Integer.parseInt(input.trim()) - 1; // Convert input to zero-based index
                            if (productId >= 0 && productId < manager.getProducts().size()) {
                                Product selectedProduct = manager.getProducts().get(productId);

                                System.out.print("Enter quantity for " + selectedProduct.getName() + ": ");
                                int quantity = scanner.nextInt();
                                scanner.nextLine(); // Consume newline

                                // Validate quantity
                                if (quantity > 0 && quantity <= selectedProduct.getQuantity()) {
                                    // Create copies of the product for the specified quantity
                                    for (int i = 0; i < quantity; i++) {
                                        products.add(selectedProduct);
                                    }
                                    System.out.println(quantity + " of " + selectedProduct.getName() + " added to cart.");
                                } else {
                                    System.out.println("Invalid quantity. Please enter a quantity between 1 and " + selectedProduct.getQuantity());
                                }
                            } else {
                                System.out.println("Invalid Product ID: " + (productId + 1));
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid Product ID or 'done' to finish.");
                        }
                    }

                    System.out.print("Pay with Wallet? (yes/no): ");
                    boolean payWithWallet = scanner.next().equalsIgnoreCase("yes");
                    scanner.nextLine(); // Consume newline

                    Bill bill = cashier.processPayment(customer, products, payWithWallet);
                    if (bill != null) {
                        System.out.println("Payment successful.");

                        // สร้าง ReceiptPrinter และพิมพ์ใบเสร็จ
                        ReceiptPrinter receiptPrinter = new ReceiptPrinter();
                        receiptPrinter.printReceipt(bill);  // พิมพ์ใบเสร็จหลังชำระเงิน
                        customer.addPurchaseHistory(products);
                        manager.addBill(bill); // Add bill to Manager's allBills
                        
                    }
                    break;

                case 2:
                    System.out.println("Registering a new membership...");
                    System.out.print("Enter new member's name: ");
                    String newName = scanner.nextLine();
                    System.out.print("Enter initial wallet amount: ");
                    double initialWallet = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline

                    // Create a new customer and register
                    Customer newCustomer = new Customer(newName, initialWallet);
                    newCustomer.registerNewCustomer(); // Register the new customer in the JSON file
                    customers.add(newCustomer); // Add to the customer list
                    System.out.println("Membership registered successfully.");
                    System.err.println("Your MembershipID is: "+customer.getMembershipId());
                    break;
                case 3:
                    cashierRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // Helper method to find customer by membership ID
    private static Customer findCustomerByMembership(List<Customer> customers, String membershipId) {
        for (Customer customer : customers) {
            if (customer.getMembershipId() != null && customer.getMembershipId().equals(membershipId)) {
                return customer;
            }
        }
        return null;
    }

    // Function to handle the Manager Role
    private static void handleManagerRole(Manager manager, Scanner scanner) {
        boolean managerRunning = true;
        while (managerRunning) {
            System.out.println("\n--- Manager Menu ---");
            System.out.println("1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. View Income");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int managerChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (managerChoice) {
                case 1:
                    System.out.print("Enter Product Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Product Price: ");
                    double price = scanner.nextDouble();
                    System.out.print("Enter Product Quantity: ");
                    int quantity = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    System.out.print("Enter Product Type (SNACK, DRINK, FOOD): ");
                    String type = scanner.nextLine();
                    ProductType productType = ProductType.valueOf(type.toUpperCase());
                    manager.addProduct(new Product(name, price, quantity, productType));
                    System.out.println("Product added successfully.");
                    break;
                case 2:
                    manager.displayProducts();
                    break;
                case 3:
                    System.out.println("Select the period for income view:");
                    System.out.println("1. Daily");
                    System.out.println("2. Weekly");
                    System.out.println("3. Monthly");
                    System.out.println("4. Yearly");
                    System.out.print("Choose an option: ");
                    int viewIncomeChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    switch (viewIncomeChoice) {
                        case 1:
                            manager.viewIncome("daily");
                            break;
                        case 2:
                            manager.viewIncome("weekly");
                            break;
                        case 3:
                            manager.viewIncome("monthly");
                            break;
                        case 4:
                            manager.viewIncome("yearly");
                            break;
                        default:
                            System.out.println("Invalid option. Please try again.");
                            break;
                    }
                    break;
                case 4:
                    managerRunning = false; // Exit manager menu
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
