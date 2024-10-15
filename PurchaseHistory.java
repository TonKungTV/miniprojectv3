import java.util.Date;
import java.util.List;

public class PurchaseHistory {
    private String membershipId; // ID ของลูกค้า
    private List<Product> products; // รายการสินค้าที่ซื้อ
    private Date purchaseDate; // วันที่ทำการซื้อ

    public PurchaseHistory(String membershipId, List<Product> products) {
        this.membershipId = membershipId;
        this.products = products;
        this.purchaseDate = new Date(); // บันทึกวันที่ปัจจุบัน
    }

    public String getMembershipId() {
        return membershipId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    @Override
    public String toString() {
        return "PurchaseHistory{" +
                "membershipId='" + membershipId + '\'' +
                ", products=" + products +
                ", purchaseDate=" + purchaseDate +
                '}';
    }
}
