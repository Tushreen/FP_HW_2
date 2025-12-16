package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class FriendshipDao {

    private final MongoCollection<Document> collection;

    public FriendshipDao(MongoDatabase database) {
        this.collection = database.getCollection("friendships");
    }

    // All friendships for a user
    public List<Document> findByUserId(String userId) {
        List<Document> result = new ArrayList<>();
        collection.find(Filters.eq("userId", userId)).into(result);
        return result;
    }

    // Only accepted friendships for a user
    public List<Document> findByUserIdAndStatus(String userId, String status) {
        Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("status", status)   // store status as a String in Mongo
        );
        List<Document> result = new ArrayList<>();
        collection.find(filter).into(result);
        return result;
    }

    // Specific relation between two users (any status)
    public Document findByUserIdAndFriendId(String userId, String friendId) {
        Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("friendId", friendId)
        );
        return collection.find(filter).first();
    }

    // Insert a friendship (friend request or accepted)
    public void insert(Document friendship) {
        collection.insertOne(friendship);
    }

    // Update an existing friendship document
    public void replace(Document friendship) {
        collection.replaceOne(Filters.eq("_id", friendship.getObjectId("_id")), friendship);
    }

    // Unfriend / remove relation
    public void deleteByUserIdAndFriendId(String userId, String friendId) {
        Bson filter = Filters.and(
                Filters.eq("userId", userId),
                Filters.eq("friendId", friendId)
        );
        collection.deleteOne(filter);
    }
}
