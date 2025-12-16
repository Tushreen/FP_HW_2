package handler;

import dao.FriendshipDao;
import dao.UserDao;
import dto.BaseDto;
import dto.FriendDTO;
import dto.FriendsListDTO;
import org.bson.Document;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendsHandler implements BaseHandler {

    private final FriendshipDao friendshipDao;
    private final UserDao userDao; // not used yet, kept for future use

    public FriendsHandler(FriendshipDao friendshipDao, UserDao userDao) {
        this.friendshipDao = friendshipDao;
        this.userDao = userDao;
    }

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        String method = request.getMethod().toUpperCase();
        String path = request.getPath();             // e.g. /users/123/friends/456/accept
        String[] parts = path.split("/");            // ["", "users", "123", "friends", "456", "accept"]

        // Expect at least: /users/{userId}/friends
        if (parts.length < 4 || !"users".equals(parts[1]) || !"friends".equals(parts[3])) {
            return sendEmptyResponse(404, "Not found");
        }

        switch (method) {
            case "GET":
                // GET /users/{userId}/friends
                return handleGetFriends(parts);
            case "POST":
                // POST /users/{userId}/friends/{friendId}
                return handleSendFriendRequest(parts);
            case "PUT":
                // PUT /users/{userId}/friends/{friendId}/accept
                return handleAcceptFriendRequest(parts);
            case "DELETE":
                // DELETE /users/{userId}/friends/{friendId}
                return handleRemoveFriend(parts);
            default:
                return sendEmptyResponse(405, "Method not allowed");
        }
    }


    private ResponseBuilder handleGetFriends(String[] parts) {
        // /users/{userId}/friends
        String userIdString = parts[2];
        Long userId = Long.valueOf(userIdString);

        List<Document> relations = friendshipDao.findByUserIdAndStatus(userIdString, "ACCEPTED");

        List<FriendDTO> friends = new ArrayList<>();

        for (Document rel : relations) {
            // assume friendId is stored as a Long in Mongo
            Long friendId = rel.getLong("friendId");

            FriendDTO dto = new FriendDTO();
            dto.setFriendId(friendId);
            dto.setStatus(rel.getString("status")); // make sure FriendDTO has setStatus(String)

            // username/displayName/avatarUrl can be filled later using UserDao if needed
            friends.add(dto);
        }

        FriendsListDTO friendsListDTO = new FriendsListDTO();
        friendsListDTO.setUserId(userId);
        friendsListDTO.setFriends(friends);

        // Wrap in a list because RestApiAppResponse expects List<T>
        return sendResponse(200, Collections.singletonList(friendsListDTO), null);
    }


    private ResponseBuilder handleSendFriendRequest(String[] parts) {
        if (parts.length < 5) {
            return sendEmptyResponse(400, "Missing friendId");
        }

        String userId = parts[2];
        String friendId = parts[4];

        Document existing = friendshipDao.findByUserIdAndFriendId(userId, friendId);

        if (existing == null) {
            Document friendship = new Document()
                    .append("userId", userId)
                    .append("friendId", friendId)
                    .append("status", "PENDING");
            friendshipDao.insert(friendship);
        }

        return sendEmptyResponse(200, null); // success, no error
    }


    private ResponseBuilder handleAcceptFriendRequest(String[] parts) {
        if (parts.length < 6 || !"accept".equals(parts[5])) {
            return sendEmptyResponse(400, "Invalid accept URL");
        }

        String userId = parts[2];
        String friendId = parts[4];

        // friendId sent request to userId
        Document incoming = friendshipDao.findByUserIdAndFriendId(friendId, userId);

        if (incoming == null) {
            return sendEmptyResponse(404, "Friend request not found");
        }

        incoming.put("status", "ACCEPTED");
        friendshipDao.replace(incoming);

        return sendEmptyResponse(200, null);
    }


    private ResponseBuilder handleRemoveFriend(String[] parts) {
        if (parts.length < 5) {
            return sendEmptyResponse(400, "Missing friendId");
        }

        String userId = parts[2];
        String friendId = parts[4];

        friendshipDao.deleteByUserIdAndFriendId(userId, friendId);
        friendshipDao.deleteByUserIdAndFriendId(friendId, userId);

        return sendEmptyResponse(204, null);
    }



    // Generic helper when you have DTO data
    @SuppressWarnings("rawtypes")
    private ResponseBuilder sendResponse(int statusCode, List data, String errorMessage) {
        // use raw RestApiAppResponse to avoid generic bound issues
        RestApiAppResponse body = new RestApiAppResponse(
                errorMessage == null,  // success = no error
                data,
                errorMessage
        );

        return new ResponseBuilder()
                .setStatus(String.valueOf(statusCode))
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    // Helper for “empty body” responses (no DTOs, just status + optional error)
    private ResponseBuilder sendEmptyResponse(int statusCode, String errorMessage) {
        RestApiAppResponse<BaseDto> body = new RestApiAppResponse<>(
                errorMessage == null,
                Collections.emptyList(),
                errorMessage
        );

        return new ResponseBuilder()
                .setStatus(String.valueOf(statusCode))
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
