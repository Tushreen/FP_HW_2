package applogic;

import dto.FriendDTO;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FriendsTest {

    @Test
    public void testFriendDtoSettersGetters() {
        FriendDTO friend = new FriendDTO();

        friend.setFriendId(200L);
        friend.setUsername("johnny");
        friend.setDisplayName("John Doe");
        friend.setAvatarUrl("http://image.png");
        friend.setStatus("PENDING");

        Assert.assertEquals(friend.getFriendId(), Long.valueOf(200));
        Assert.assertEquals(friend.getUsername(), "johnny");
        Assert.assertEquals(friend.getDisplayName(), "John Doe");
        Assert.assertEquals(friend.getAvatarUrl(), "http://image.png");
        Assert.assertEquals(friend.getStatus(), "PENDING");
    }
}
