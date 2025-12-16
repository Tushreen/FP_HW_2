package handler;

import auth.AuthFilter;
import dao.MessageDao;
import dto.MessageDto;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.util.List;

public class DeleteMessageHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        ResponseBuilder rb = new ResponseBuilder();

        try {
            // Authenticate user
            AuthFilter.AuthResult authResult = AuthFilter.doFilter(request);
            if (!authResult.isLoggedIn) {
                return rb.setStatus(StatusCodes.UNAUTHORIZED)
                        .setBody(new RestApiAppResponse<>(false, null, "Not authenticated"));
            }

            // Parse messageId from request body
            String body = request.getBody();
            if (body == null || body.trim().isEmpty()) {
                return rb.setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "No message ID provided"));
            }

            // Parse JSON to get messageId
            var parsed = GsonTool.GSON.fromJson(body, java.util.Map.class);
            String messageId = (String) parsed.get("messageId");

            if (messageId == null || messageId.trim().isEmpty()) {
                return rb.setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "Message ID is required"));
            }

            // Verify the message belongs to the authenticated user
            MessageDao messageDao = MessageDao.getInstance();
            List<MessageDto> messages = messageDao.query("_id", new org.bson.types.ObjectId(messageId));

            if (messages.isEmpty()) {
                return rb.setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "Message not found"));
            }

            MessageDto message = messages.get(0);

            // Check if user owns this message
            if (!authResult.userName.equals(message.getFromId())) {
                return rb.setStatus(StatusCodes.BAD_REQUEST)
                        .setBody(new RestApiAppResponse<>(false, null, "Cannot delete another user's message"));
            }

            // Delete the message
            messageDao.delete(messageId);

            return rb.setStatus(StatusCodes.OK)
                    .setBody(new RestApiAppResponse<>(true, null, "Message deleted"));

        } catch (Exception e) {
            return rb.setStatus(StatusCodes.SERVER_ERROR)
                    .setBody(new RestApiAppResponse<>(false, null, "Error deleting message: " + e.getMessage()));
        }
    }
}
