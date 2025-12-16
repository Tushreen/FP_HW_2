package response;

import dto.BaseDto;
import dto.FriendsListDTO;

import java.util.List;

public class RestApiAppResponse<T extends BaseDto> {

    public final boolean status;
    public final List<T> data;
    public final String message;

    public RestApiAppResponse(boolean status, List<T> data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    // success with a list of friends
    List<FriendsListDTO> payload = List.of(FriendsListDTO);
    return sendResponse(200, payload, null);

    // simple “OK” message
    List<String> msg = List.of("Friend request sent");
    return sendResponse(200, msg, null);

// error case
    return sendResponse(404, List.of(), "Friend request not found");

}
