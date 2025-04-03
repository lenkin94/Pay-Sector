package app.paysector.web.dto;

import app.paysector.user.model.Country;
import app.paysector.user.model.User;
import app.paysector.web.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    @Test
    void successfullyMappedUserToDto() {

        //Given
        User user = User.builder()
                .firstName("Borislav")
                .lastName("Solakov")
                .email("lenkin@mail.bg")
                .profilePicture("www.pic.bg")
                .country(Country.BULGARIA)
                .build();

        //When
        EditProfileRequest editProfileRequest = DtoMapper.mapUserToUserEditRequest(user);

        //Then
        assertEquals(user.getFirstName(), editProfileRequest.getFirstName());
        assertEquals(user.getLastName(), editProfileRequest.getLastName());
        assertEquals(user.getEmail(), editProfileRequest.getEmail());
        assertEquals(user.getProfilePicture(), editProfileRequest.getProfilePicture());
        assertEquals(user.getCountry(), editProfileRequest.getCountry());
    }
}
