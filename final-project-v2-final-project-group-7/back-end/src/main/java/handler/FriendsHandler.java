package handler;

import dao.FriendshipDao;
import dao.UserDao;
import dto.FriendDTO;
import dto.FriendsListDTO;
import org.bson.Document;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;

import java.util.ArrayList;
import java.util.List;

public class FriendsHandler implements BaseHandler {

    private final FriendshipDao friendshipDao;
    private final UserDao userDao;

    public FriendsHandler(FriendshipDao friendshipDao, UserDao userDao) {
        this.friendshipDao = friendshipDao;
        this.userDao = userDao;
    }

    private <T> ResponseBuilder sendResponse(int statusCode, List<T> data, String errorMessage) {
        // RestApiAppResponse(boolean, List<T>, String) – based on your error message
        RestApiAppResponse<T> body = new RestApiAppResponse<>(
                errorMessage == null,   // success = no error
                data,
                errorMessage
        );

        return new ResponseBuilder()
                .setStatus(String.valueOf(statusCode))
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {

        List<String> path = request.getPathParts();

        // routes:
        // GET    /users/{userId}/friends
        // POST   /users/{userId}/friends/{friendId}
        // PUT    /users/{userId}/friends/{friendId}/accept
        // DELETE /users/{userId}/friends/{friendId}

        String method = request.getMethod().toUpperCase();

        if (method.equals("GET")) {
            return handleGetFriends(path);
        }
        if (method.equals("POST")) {
            return handleSendFriendRequest(path);
        }
        if (method.equals("PUT")) {
            return handleAcceptFriendRequest(path);
        }
        if (method.equals("DELETE")) {
            return handleRemoveFriend(path);
        }

        return errorResponse(405, "Method not allowed");
    }


    private ResponseBuilder handleGetFriends(List<String> path) {

        String userId = path.get(2); // /users/{id}/friends

        List<Document> relations =
                friendshipDao.findByUserIdAndStatus(userId, "ACCEPTED");

        List<FriendDTO> friends = new ArrayList<>();

        for (Document rel : relations) {
            String friendId = rel.getString("friendId");

            Document userDoc = userDao.findById(friendId);
            if (userDoc == null) continue;

            friends.add(new FriendDTO(
                    friendId,
                    userDoc.getString("username"),
                    userDoc.getString("displayName"),
                    userDoc.getString("avatarUrl"),
                    rel.getString("status")
            ));
        }

        FriendsListDTO response = new FriendsListDTO(userId, friends);
        return jsonResponse(200, response);
    }


    private ResponseBuilder handleSendFriendRequest(List<String> path) {

        String userId = path.get(2);
        String friendId = path.get(4);

        Document existing = friendshipDao.findByUserIdAndFriendId(userId, friendId);

        if (existing == null) {
            Document friendship = new Document()
                    .append("userId", userId)
                    .append("friendId", friendId)
                    .append("status", "PENDING");

            friendshipDao.insert(friendship);
        }

        return jsonResponse(200, new RestApiAppResponse("Friend request sent"));
    }


    private ResponseBuilder handleAcceptFriendRequest(List<String> path) {

        String userId = path.get(2);
        String friendId = path.get(4);

        Document incoming =
                friendshipDao.findByUserIdAndFriendId(friendId, userId);

        if (incoming == null) {
            return errorResponse(404, "Friend request not found");
        }

        incoming.put("status", "ACCEPTED");
        friendshipDao.replace(incoming);

        return jsonResponse(200, new RestApiAppResponse("Friend request accepted"));
    }

    // --------------------------------------------------------------------
    // DELETE /users/{userId}/friends/{friendId}
    // --------------------------------------------------------------------
    private ResponseBuilder handleRemoveFriend(List<String> path) {

        String userId = path.get(2);
        String friendId = path.get(4);

        friendshipDao.deleteByUserIdAndFriendId(userId, friendId);
        friendshipDao.deleteByUserIdAndFriendId(friendId, userId);

        return jsonResponse(204, new RestApiAppResponse("Friend removed"));
    }


    private ResponseBuilder jsonResponse(int status, Object dto) {
        return new ResponseBuilder()
                .setStatus(String.valueOf(status))
                .setContentType("application/json")
                .setBody(new RestApiAppResponse(dto));
    }

    private ResponseBuilder errorResponse(int status, String message) {
        return new ResponseBuilder()
                .setStatus(String.valueOf(status))
                .setContentType("application/json")
                .setBody(new RestApiAppResponse(message));
    }
}
