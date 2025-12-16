package applogic;

import dto.FriendDTO;
import dto.FriendsListDTO;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class FriendsListTests {

    @Test
    public void testFriendsListDtoSettersAndGetters() {
        FriendsListDTO listDTO = new FriendsListDTO();

        listDTO.setUserId(123L);

        FriendDTO f1 = new FriendDTO();
        f1.setFriendId(10L);
        f1.setStatus("ACCEPTED");

        FriendDTO f2 = new FriendDTO();
        f2.setFriendId(20L);
        f2.setStatus("PENDING");

        List<FriendDTO> friends = new ArrayList<>();
        friends.add(f1);
        friends.add(f2);

        listDTO.setFriends(friends);

        Assert.assertEquals(listDTO.getUserId(), Long.valueOf(123));
        Assert.assertEquals(listDTO.getFriends().size(), 2);
        Assert.assertEquals(listDTO.getFriends().get(0).getFriendId(), Long.valueOf(10));
        Assert.assertEquals(listDTO.getFriends().get(1).getStatus(), "PENDING");
    }

    @Test
    public void testFriendsListDtoWorksWithEmptyList() {
        FriendsListDTO listDTO = new FriendsListDTO();
        listDTO.setUserId(555L);
        listDTO.setFriends(new ArrayList<>());

        Assert.assertEquals(listDTO.getUserId(), Long.valueOf(555));
        Assert.assertTrue(listDTO.getFriends().isEmpty());
    }
}
