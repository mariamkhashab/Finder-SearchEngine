import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;

import Models.Website;

public class QueryProcessor {

    private MongoClient mongoClient = null;
    MongoCollection<Document> webpagesCollection;


    public void mongoConnect() throws Exception {
        try {

            if (mongoClient == null) 
            {
                mongoClient = MongoClients.create("mongodb+srv://admin:admin@searchengine.g39y2.mongodb.net/SearchEngine?retryWrites=true&w=majority");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void insertWebpage(Website website) {
        try {
            mongoConnect();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            webpagesCollection = db.getCollection("webpages");
            Document doc = new Document();
            doc.append("url", website.getURL());
            doc.append("status", website.getStatus());
            webpagesCollection.insertOne(doc);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void updateWebpage(Website website) {
        try {
            mongoConnect();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            webpagesCollection = db.getCollection("webpages");
            Document doc = new Document();
            doc.append("url", website.getURL());
            doc.append("status", website.getStatus());
            webpagesCollection.updateOne(Filters.eq("_id", website.getID()), new Document("$set", doc));
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public Website getWebpage(String url) {
        try {
            mongoConnect();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            webpagesCollection = db.getCollection("webpages");
            Document doc = webpagesCollection.find(Filters.eq("url", url)).first();
            if (doc != null) {
                Website website = new Website();
                website.setID(doc.getObjectId("_id").toString());
                website.setURL(doc.getString("url"));
                website.setStatus(doc.getInteger("status"));
                return website;
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }
}