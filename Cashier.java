import java.util.List;
import java.util.Scanner;
class Cashier {
    private String name;
    private PaymentProcessor paymentProcessor = new PaymentProcessor();
    private ReceiptPrinter receiptPrinter = new ReceiptPrinter();


    public Cashier(String name) {
        this.name = name;
    }

    public void displayProducts(List<Product> products) {
        System.out.println("\n--- Product List ---");
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            System.out.printf("%d. %s\n", (i + 1), product);
        }
        System.out.println("----------------------");
    }
    

    public Bill processPayment(Customer customer, List<Product> products, boolean payWithWallet) {
        Bill bill = paymentProcessor.processPayment(customer, products, payWithWallet);
        if (bill != null) {
            receiptPrinter.printReceipt(bill);
        }
        return bill;
    }


    public void registerMembership(Customer customer, Scanner scanner) {
        System.out.print("Would you like to register for membership? (yes/no): ");
        String registerResponse = scanner.next();
        switch (registerResponse.toLowerCase()) {
            case "yes":
                System.out.print("Enter a membership ID: ");
                String membershipId = scanner.next();
                customer.setMembership(true, membershipId);
                System.out.println("Membership registered successfully!");
                break;
            case "no":
                System.out.println("Proceeding without membership.");
                break;
            default:
                System.out.println("Invalid response. Proceeding without membership.");
                break;
        }
    }

    public void checkMembershipStatus(Customer customer, Scanner scanner) {
        System.out.print("Is the customer a member? (yes/no): ");
        String isMemberResponse = scanner.next();
        switch (isMemberResponse.toLowerCase()) {
            case "yes":
                System.out.print("Enter membership ID: ");
                String membershipId = scanner.next();
                customer.setMembership(true, membershipId);
                System.out.println("Membership verified.");
                break;
            case "no":
                registerMembership(customer, scanner);
                break;
            default:
                System.out.println("Invalid response. Registering as non-member.");
                registerMembership(customer, scanner);
                break;
        }
    }

    public void printReceipt(Bill bill) {
        String storeName = "NextMart";
        String storeAddress = "123 Main St, City, Country";
        String storePhone = "Phone: 123-456-7890";
    
        String separator = "--------------------------------------";
        String lineSeparator = "======================================";
        
        System.out.printf("Bill Number: %-29s\n", bill.getBillNumber());
        System.out.println("\n" + lineSeparator);
        System.out.printf("%-20s %20s\n", storeName, " ");
        System.out.printf("%-20s %20s\n", storeAddress, " ");
        System.out.printf("%-20s %20s\n", storePhone, " ");
        System.out.println(lineSeparator);
        System.out.printf("Date: %-29s\n", bill.getDate());
        System.out.println(separator);
        System.out.println("Items:");
        List<Product> products = bill.getProducts();
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            System.out.printf("%-25s %10.2f\n", product.getName(), product.getPrice(null)); // ส่ง null เพราะไม่ต้องใช้ข้อมูล customer
        }
        System.out.printf("Total Amount: %-25.2f\n", bill.getTotalAmount());
        System.out.println(separator);
        System.out.printf("Member information\n");
        System.out.println(lineSeparator);
        System.out.println("Thank you for shopping at " + storeName + "!");
        System.out.println(lineSeparator);
    }
}
