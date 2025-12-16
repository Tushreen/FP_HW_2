package dto;

public class FriendDTO {

    private Long friendId;       // ID of the friend user
    private String username;     // Friend's username
    private String displayName;  // Friend's display name
    private String avatarUrl;    // Optional profile picture URL
    private String status;       // e.g. "ACCEPTED", "PENDING"

    public FriendDTO() {
    }

    public FriendDTO(Long friendId, String username, String displayName, String avatarUrl, String status) {
        this.friendId = friendId;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.status = status;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
