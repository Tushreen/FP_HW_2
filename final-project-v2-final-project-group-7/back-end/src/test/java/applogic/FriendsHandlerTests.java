package applogic;

import dao.FriendshipDao;
import dao.UserDao;
import handler.FriendsHandler;
import org.bson.Document;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import request.ParsedRequest;
import response.CustomHttpResponse;
import response.StatusCodes;

import java.util.ArrayList;
import java.util.List;

public class FriendsHandlerTests {

    @Test
    public void getFriends_success() {
        // Arrange
        FriendshipDao mockFriendshipDao = Mockito.mock(FriendshipDao.class);
        UserDao mockUserDao = Mockito.mock(UserDao.class);

        // Mongo returns one accepted friendship for user 123
        List<Document> docs = new ArrayList<>();
        docs.add(new Document("friendId", 456L)
                .append("status", "ACCEPTED"));

        Mockito.when(mockFriendshipDao.findByUserIdAndStatus("123", "ACCEPTED"))
                .thenReturn(docs);

        var handler = new FriendsHandler(mockFriendshipDao, mockUserDao);

        ParsedRequest request = new ParsedRequest();
        request.setPath("/users/123/friends");
        request.setMethod("GET");

        // Act
        var builder = handler.handleRequest(request);
        CustomHttpResponse res = builder.build();

        // Assert
        Assert.assertEquals(res.status, StatusCodes.OK);
        Assert.assertEquals(res.headers.get("Content-Type"), "application/json");

        Mockito.verify(mockFriendshipDao, Mockito.times(1))
                .findByUserIdAndStatus("123", "ACCEPTED");
    }

    @Test
    public void sendFriendRequest_insertsWhenNotExisting() {
        // Arrange
        FriendshipDao mockFriendshipDao = Mockito.mock(FriendshipDao.class);
        UserDao mockUserDao = Mockito.mock(UserDao.class);

        Mockito.when(mockFriendshipDao.findByUserIdAndFriendId("10", "20"))
                .thenReturn(null);

        var handler = new FriendsHandler(mockFriendshipDao, mockUserDao);

        ParsedRequest request = new ParsedRequest();
        request.setPath("/users/10/friends/20");
        request.setMethod("POST");

        // Act
        var builder = handler.handleRequest(request);
        CustomHttpResponse res = builder.build();

        // Assert
        Assert.assertEquals(res.status, StatusCodes.OK);

        Mockito.verify(mockFriendshipDao, Mockito.times(1))
                .findByUserIdAndFriendId("10", "20");

        Mockito.verify(mockFriendshipDao, Mockito.times(1))
                .insert(Mockito.any(Document.class));
    }

    @Test
    public void acceptFriendRequest_updatesExisting() {
        // Arrange
        FriendshipDao mockFriendshipDao = Mockito.mock(FriendshipDao.class);
        UserDao mockUserDao = Mockito.mock(UserDao.class);

        // friend 20 sent request to user 10
        Document incoming = new Document("userId", "20")
                .append("friendId", "10")
                .append("status", "PENDING");

        Mockito.when(mockFriendshipDao.findByUserIdAndFriendId("20", "10"))
                .thenReturn(incoming);

        var handler = new FriendsHandler(mockFriendshipDao, mockUserDao);

        ParsedRequest request = new ParsedRequest();
        request.setPath("/users/10/friends/20/accept");
        request.setMethod("PUT");

        // Act
        var builder = handler.handleRequest(request);
        CustomHttpResponse res = builder.build();

        // Assert
        Assert.assertEquals(res.status, StatusCodes.OK);

        Mockito.verify(mockFriendshipDao, Mockito.times(1))
                .findByUserIdAndFriendId("20", "10");

        Mockito.verify(mockFriendshipDao, Mockito.times(1))
                .replace(Mockito.any(Document.class));
    }

    @Test
    public void removeFriend_deletesBothDirections() {
        // Arrange
        FriendshipDao mockFriendshipDao = Mockito.mock(FriendshipDao.class);
        UserDao mockUserDao = Mockito.mock(UserDao.class);

        var handler = new FriendsHandler(mockFriendshipDao, mockUserDao);

        ParsedRequest request = new ParsedRequest();
        request.setPath("/users/5/friends/8");
        request.setMethod("DELETE");

        // Act
        var builder = handler.handleRequest(request);
        CustomHttpResponse res = builder.build();

        // Assert
        Assert.assertEquals(res.status, "204");

        Mockito.verify(mockFriendshipDao, Mockito.times(1))
                .deleteByUserIdAndFriendId("5", "8");
        Mockito.verify(mockFriendshipDao, Mockito.times(1))
                .deleteByUserIdAndFriendId("8", "5");
    }
}
