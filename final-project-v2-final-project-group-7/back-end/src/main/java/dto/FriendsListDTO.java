package dto;

import java.util.List;

public class FriendsListDTO {

    private Long userId;
    private List<FriendDTO> friends;

    public FriendsListDTO() {
    }

    public FriendsListDTO(Long userId, List<FriendDTO> friends) {
        this.userId = userId;
        this.friends = friends;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<FriendDTO> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendDTO> friends) {
        this.friends = friends;
    }
}
