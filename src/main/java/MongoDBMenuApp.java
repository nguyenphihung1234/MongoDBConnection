import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

public class MongoDBMenuApp {

    private static MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private static MongoDatabase database = mongoClient.getDatabase("eShop");
    private static MongoCollection<Document> orderCollection = database.getCollection("OrderCollection");
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Tạo chỉ mục duy nhất nếu chưa tồn tại
        createUniqueIndex();

        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Chèn đơn hàng");
            System.out.println("2. Cập nhật địa chỉ giao hàng");
            System.out.println("3. Xóa đơn hàng");
            System.out.println("4. Đọc tất cả đơn hàng");
            System.out.println("5. Tính tổng số tiền");
            System.out.println("6. Đếm số sản phẩm có product_id là 'somi'");
            System.out.println("7. Thoát");
            System.out.print("Chọn tùy chọn (1-7): ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    insertOrder();
                    break;
                case 2:
                    updateDeliveryAddress();
                    break;
                case 3:
                    deleteOrder();
                    break;
                case 4:
                    readAllOrders();
                    break;
                case 5:
                    calculateTotalAmount();
                    break;
                case 6:
                    countProductSomi();
                    break;
                case 7:
                    mongoClient.close();
                    System.out.println("Thoát chương trình.");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ. Vui lòng chọn lại.");
            }
        }
    }

    private static int getIntInput() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Vui lòng nhập một số nguyên.");
                scanner.next(); // Clear the invalid input
            }
        }
    }

    private static void createUniqueIndex() {
        try {
            // Tạo chỉ mục duy nhất trên trường 'orderid'
            orderCollection.createIndex(new Document("orderid", 1), new IndexOptions().unique(true));
            System.out.println("Chỉ mục duy nhất trên 'orderid' đã được tạo.");
        } catch (Exception e) {
            System.out.println("Chỉ mục duy nhất đã tồn tại.");
        }
    }

    private static void insertOrder() {
        scanner.nextLine(); // Clear the newline
        System.out.println("Nhập orderid: ");
        int orderid = getIntInput();
        System.out.println("Nhập địa chỉ giao hàng: ");
        String address = scanner.nextLine();

        // Kiểm tra xem orderid đã tồn tại chưa
        if (orderCollection.find(new Document("orderid", orderid)).first() != null) {
            System.out.println("Lỗi: Đơn hàng với orderid này đã tồn tại.");
            return;
        }

        Document order = new Document("orderid", orderid)
                .append("products", Arrays.asList(
                        new Document("product_id", "quanau").append("product_name", "quan au").append("size", "XL").append("price", 10).append("quantity", 1),
                        new Document("product_id", "somi").append("product_name", "ao so mi").append("size", "XL").append("price", 10.5).append("quantity", 2)
                ))
                .append("total_amount", 31)
                .append("delivery_address", address);

        try {
            orderCollection.insertOne(order);
            System.out.println("Đã chèn đơn hàng thành công.");
        } catch (MongoWriteException e) {
            System.out.println("Lỗi: Không thể chèn đơn hàng.");
        }
    }

    private static void updateDeliveryAddress() {
        System.out.println("Nhập orderid của đơn hàng cần cập nhật: ");
        int orderid = getIntInput();
        scanner.nextLine(); // Clear the newline
        System.out.println("Nhập địa chỉ giao hàng mới: ");
        String newAddress = scanner.nextLine();

        orderCollection.updateOne(Filters.eq("orderid", orderid), Updates.set("delivery_address", newAddress));
        System.out.println("Đã cập nhật địa chỉ giao hàng thành công.");
    }

    private static void deleteOrder() {
        System.out.println("Nhập orderid của đơn hàng cần xóa: ");
        int orderid = getIntInput();

        orderCollection.deleteOne(Filters.eq("orderid", orderid));
        System.out.println("Đã xóa đơn hàng thành công.");
    }
    private static void readAllOrders() {
        System.out.println("No | Product Name | Price | Quantity | Total");

        int i = 1;
        for (Document order : orderCollection.find()) {
            for (Document product : (Iterable<Document>) order.get("products")) {
                // Lấy giá và số lượng, kiểm tra kiểu dữ liệu và chuyển đổi nếu cần
                Object priceObj = product.get("price");
                Object quantityObj = product.get("quantity");

                double price = (priceObj instanceof Double) ? (Double) priceObj : ((Integer) priceObj).doubleValue();
                int quantity = (quantityObj instanceof Integer) ? (Integer) quantityObj : ((Double) quantityObj).intValue();
                double total = price * quantity;

                System.out.println(i + " | " + product.getString("product_name") + " | " + price + " | " + quantity + " | " + total);
                i++;
            }
        }
    }

    private static void calculateTotalAmount() {
        double totalAmount = 0;

        for (Document order : orderCollection.find()) {
            Object totalAmountObj = order.get("total_amount");

            // Kiểm tra kiểu dữ liệu và chuyển đổi nếu cần
            if (totalAmountObj instanceof Integer) {
                totalAmount += ((Integer) totalAmountObj).doubleValue();
            } else if (totalAmountObj instanceof Double) {
                totalAmount += (Double) totalAmountObj;
            }
        }

        System.out.println("Tổng số tiền: " + totalAmount);
    }


    private static void countProductSomi() {
        String productIdToCount = "somi";
        int totalCount = 0;

        for (Document order : orderCollection.find()) {
            for (Document product : (Iterable<Document>) order.get("products")) {
                if (product.getString("product_id").equals(productIdToCount)) {
                    totalCount += product.getInteger("quantity");
                }
            }
        }

        System.out.println("Tổng số sản phẩm 'somi' được bán: " + totalCount);
    }
}
