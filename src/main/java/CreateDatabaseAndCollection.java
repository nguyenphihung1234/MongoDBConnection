import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.Arrays;

public class CreateDatabaseAndCollection {
    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017/");
        MongoDatabase database = mongoClient.getDatabase("eShop");
        MongoCollection<Document> collection = database.getCollection("OrderCollection");

        // Chèn một tài liệu để tạo bộ sưu tập
        Document order = new Document("orderid", 1)
            .append("products", Arrays.asList(
                new Document("product_id", "quanau")
                    .append("product_name", "quan au")
                    .append("size", "XL")
                    .append("price", 10)
                    .append("quantity", 1),
                new Document("product_id", "somi")
                    .append("product_name", "ao so mi")
                    .append("size", "XL")
                    .append("price", 10.5)
                    .append("quantity", 2)
            ))
            .append("total_amount", 31)
            .append("delivery_address", "Hanoi");

        collection.insertOne(order);

        // Đóng kết nối
        mongoClient.close();
    }
}
