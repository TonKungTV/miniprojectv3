import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Date;
import java.util.HashMap;

class PaymentProcessor {
    private static List<Integer> billNumbers = new ArrayList<>(); // ตัวเก็บหมายเลขบิลที่ถูกสร้างขึ้น

    public Bill processPayment(Customer customer, List<Product> products, boolean payWithWallet) {
        Map<Product, Integer> productQuantities = new HashMap<>(); // ใช้แผนที่เพื่อเก็บสินค้าพร้อมจำนวน
    
        double totalAmount = 0.0;
        int totalPoints = 0;
    
        // นับจำนวนของแต่ละสินค้า
        for (Product product : products) {
            double productPrice = product.getPrice(customer);
            totalAmount += productPrice;
    
            // บันทึกจำนวนสินค้า
            productQuantities.put(product, productQuantities.getOrDefault(product, 0) + 1);
            totalPoints += productPrice;
        }
    
        customer.addPoints(totalPoints);
        System.out.println("Earned " + totalPoints + " points!");
    
        // ลดจำนวนสินค้าในสต็อก
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            product.reduceStock(quantity);
        }
    
        // จัดการการชำระเงิน
        if (payWithWallet) {
            if (customer.getWallet() >= totalAmount) {
                customer.deductFromWallet(totalAmount);
                System.out.println("Payment successful from Wallet!");
            } else {
                System.out.println("Insufficient funds in wallet.");
                return null;
            }
        } else {
            System.out.println("Payment successful with Cash!");
        }
    
        // สร้างบิล
        int billNumber = generateUniqueBillNumber(); // ใช้ฟังก์ชันเพื่อสร้างหมายเลขบิลที่ไม่ซ้ำ
        Bill bill = new Bill(billNumber, new ArrayList<>(productQuantities.keySet()), totalAmount, new Date());
        customer.addBill(bill);
        
        // เพิ่ม membershipId ในการบันทึกบิล
        saveBillToFile(bill, customer, productQuantities, customer.getMembershipId());
        return bill;
    }
    

    private int generateUniqueBillNumber() {
        int billNumber = billNumbers.size() + 1; // เริ่มต้นที่ 1
        while (billNumbers.contains(billNumber)) {
            billNumber++;
        }
        billNumbers.add(billNumber); // เพิ่มหมายเลขบิลลงใน List
        return billNumber;
    }

    public void saveBillToFile(Bill bill, Customer customer, Map<Product, Integer> productQuantities, String membershipId) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject data = (JSONObject) parser.parse(new FileReader("data.json"));
            updateProductsInJson(data, productQuantities);
            updateCustomerInJson(data, customer);
            addBillToJson(data, bill, productQuantities, membershipId); // ส่ง membershipId
    
            try (FileWriter file = new FileWriter("data.json")) {
                file.write(data.toJSONString());
                file.flush();
            }
    
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    

    private void updateProductsInJson(JSONObject data, Map<Product, Integer> productQuantities) {
        JSONArray productsArray = (JSONArray) data.get("products");
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            Product product = entry.getKey();
            int quantityToReduce = entry.getValue();

            for (Object obj : productsArray) {
                JSONObject jsonProduct = (JSONObject) obj;
                if (jsonProduct.get("name").equals(product.getName())) {
                    long currentQuantity = (long) jsonProduct.get("quantity");
                    jsonProduct.put("quantity", currentQuantity - quantityToReduce);
                    break;
                }
            }
        }
    }

    private void updateCustomerInJson(JSONObject data, Customer customer) {
        JSONArray customersArray = (JSONArray) data.get("customers");
        for (Object obj : customersArray) {
            JSONObject jsonCustomer = (JSONObject) obj;
            if (jsonCustomer.get("name").equals(customer.getName())) {
                jsonCustomer.put("wallet", customer.getWallet());
                jsonCustomer.put("points", customer.getPoints());

                JSONArray purchaseHistory = (JSONArray) jsonCustomer.get("purchaseHistory");
                JSONObject newBill = new JSONObject();
                newBill.put("billNumber", customer.getLatestBill().getBillNumber());
                newBill.put("totalAmount", customer.getLatestBill().getTotalAmount());
                newBill.put("date", customer.getLatestBill().getDate().toString());
                purchaseHistory.add(newBill);
                break;
            }
        }
    }

    private void addBillToJson(JSONObject data, Bill bill, Map<Product, Integer> productQuantities, String membershipId) {
        JSONArray billsArray = (JSONArray) data.get("bills");
        if (billsArray == null) {
            billsArray = new JSONArray();
            data.put("bills", billsArray);
        }
    
        JSONObject newBill = new JSONObject();
        newBill.put("billNumber", bill.getBillNumber());
        newBill.put("totalAmount", bill.getTotalAmount());
        newBill.put("date", bill.getDate().toString());
        newBill.put("membershipId", membershipId); // เพิ่ม membershipId
    
        JSONArray billProducts = new JSONArray();
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            JSONObject billProduct = new JSONObject();
            billProduct.put("name", product.getName());
            billProduct.put("quantity", quantity);
            billProduct.put("price", product.getPrice(null));
            billProducts.add(billProduct);
        }
        newBill.put("products", billProducts);
        billsArray.add(newBill);
    }
    
}
