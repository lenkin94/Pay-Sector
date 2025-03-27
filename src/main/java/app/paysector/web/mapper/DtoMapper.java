package app.paysector.web.mapper;

import app.paysector.bill.dto.Bill;
import lombok.experimental.UtilityClass;
import app.paysector.user.model.User;
import app.paysector.web.dto.EditProfileRequest;

@UtilityClass
public class DtoMapper {
    public static EditProfileRequest mapUserToUserEditRequest(User user) {

        return EditProfileRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .country(user.getCountry())
                .build();
    }
}
