package app.paysector;


import app.paysector.exception.EmailAlreadyExistsException;
import app.paysector.exception.PasswordsDoNotMatchException;
import app.paysector.exception.UsernameAlreadyExistsException;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.Country;
import app.paysector.user.model.User;
import app.paysector.user.model.UserRole;
import app.paysector.user.repository.UserRepository;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.ChangePasswordRequest;
import app.paysector.web.dto.EditProfileRequest;
import app.paysector.web.dto.RegisterRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WalletService walletService;

    @InjectMocks
    private UserService userService;


    @Test
    void existingUsername_whenRegister_throwsException() {
        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("lenkin")
                .password("asdasd")
                .confirmPassword("asdasd")
                .firstName("Borislav")
                .lastName("Solakov")
                .email("lenkin@mail.bg")
                .country(Country.BULGARIA)
                .build();
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));


        //When and Then
        assertThrows(UsernameAlreadyExistsException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any());
        verify(walletService, never()).createWallet(any());
    }

    @Test
    void existingEmail_whenRegister_throwsException() {
        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("lenkin")
                .password("asdasd")
                .confirmPassword("asdasd")
                .firstName("Borislav")
                .lastName("Solakov")
                .country(Country.BULGARIA)
                .build();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(new User()));


        //When and Then
        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any());
        verify(walletService, never()).createWallet(any());
    }

    @Test
    void passwordsDoNotMatch_whenRegister_throwsException() {
        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("lenkin")
                .password("asdasd")
                .confirmPassword("dsdsad")
                .firstName("Borislav")
                .lastName("Solakov")
                .country(Country.BULGARIA)
                .build();


        //When and Then
        assertThrows(PasswordsDoNotMatchException.class, () -> userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any());
        verify(walletService, never()).createWallet(any());
    }


    @Test
    void successfullyRegisterUser() {

        //Given
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("lenkin")
                .password("asdasd")
                .confirmPassword("asdasd")
                .firstName("Borislav")
                .lastName("Solakov")
                .country(Country.BULGARIA)
                .build();

        User user = User.builder()
            .id(UUID.randomUUID())
            .build();
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);
        when(walletService.createWallet(any())).thenReturn(new Wallet());

        //When
        User registered = userService.registerUser(registerRequest);


        //Then
        verify(userRepository, times(1)).save(registered);
        verify(walletService, times(1)).createWallet(any());
    }

    @Test
    void nonExistingUser_whenEditProfile_thenExceptionIsThrown() {
        //Given
        UUID userId = UUID.randomUUID();
        EditProfileRequest editProfileRequest = EditProfileRequest.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        //When and Then
        assertThrows(RuntimeException.class, () -> userService.editProfile(userId, editProfileRequest));
    }

    @Test
    void existingUser_whenEditProfile_thenEditProfile() {
        //Given
        UUID userId = UUID.randomUUID();
        EditProfileRequest editProfileRequest = EditProfileRequest.builder()
                .firstName("Borislav")
                .lastName("Solakov")
                .email("lenkin@mail.bg")
                .profilePicture("www.pic.com")
                .build();

        User user = User.builder().build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //When
        userService.editProfile(userId, editProfileRequest);

        //Then
        assertEquals("Borislav", user.getFirstName());
        assertEquals("Solakov", user.getLastName());
        assertEquals("lenkin@mail.bg", user.getEmail());
        assertEquals("www.pic.com", user.getProfilePicture());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void wrongOldPassword_whenChangePassword_thenExceptionIsThrown() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("lenkin")
                .password(passwordEncoder.encode("oldpass"))
                .firstName("Borislav")
                .lastName("Solakov")
                .country(Country.BULGARIA)
                .role(UserRole.USER)
                .isActive(true)
                .loans(new ArrayList<>())
                .profilePicture("www.pic.com")
                .wallet(new Wallet())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .transactions(new ArrayList<>())
                .build();


        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("newpass")
                .newPassword("newpass")
                .confirmPassword("newpass")
                .build();


        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(PasswordsDoNotMatchException.class, () -> userService.changePassword(user.getId(), request));
        verify(userRepository, never()).save(any());
    }


    @Test
    void matchingPasswords_whenChangePassword_thenChangePassword() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("lenkin")
                .password("oldpass")
                .firstName("Borislav")
                .lastName("Solakov")
                .country(Country.BULGARIA)
                .role(UserRole.USER)
                .isActive(true)
                .loans(new ArrayList<>())
                .profilePicture("www.pic.com")
                .wallet(new Wallet())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .transactions(new ArrayList<>())
                .build();

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("oldpass")
                .newPassword("newpass")
                .confirmPassword("newpass")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.changePassword(user.getId(), request);

        assertEquals("newpass", user.getPassword());
    }

    @Test
    void missingUsername_whenLoadUserByUsername_throwsException() {
        //Given
        String username = "lenkin";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        //WHen and Then
        assertThrows(RuntimeException.class, () -> userService.loadUserByUsername(username));
    }

    @Test
    void existingUsername_whenLoadUserByUsername_thenReturnCorrectAuthenticationUser() {
        //Given
        String username = "lenkin";
        User user = User.builder()
                .id(UUID.randomUUID())
                .isActive(true)
                .password("asdasd")
                .role(UserRole.USER)
                .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        //When
        UserDetails authenticationMetadata = userService.loadUserByUsername(username);

        //Then
        assertInstanceOf(AuthenticateUser.class, authenticationMetadata);
        AuthenticateUser result = (AuthenticateUser) authenticationMetadata;
        assertEquals(user.getId(), result.getUserId());
        assertEquals(username, result.getUsername());
        assertEquals(user.getPassword(), result.getPassword());
        assertEquals(user.isActive(), result.isActive());
        assertEquals(user.getRole(), result.getRole());
        Assertions.assertThat(result.getAuthorities()).hasSize(1);
        assertEquals("ROLE_USER", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void userWithRoleAdmin_whenSwitchRole_thenSwitchToUser() {
        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.ADMIN)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //When
        userService.changeRole(userId);

        //Then
        assertEquals(user.getRole(), UserRole.USER);
    }

    @Test
    void userWithRoleUser_whenSwitchRole_thenSwitchToAdmin() {

        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .role(UserRole.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //When
        userService.changeRole(userId);


        //Then
        assertEquals(user.getRole(), (UserRole.ADMIN));
    }

    @Test
    void userWithStatusActive_whenChangeStatus_thenSwitchToBanned() {

        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isActive(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //When
        userService.changeStatus(userId);

        //Then
        assertFalse(user.isActive());
    }

    @Test
    void userWithStatusBanned_whenChangeStatus_thenSwitchToActive() {

        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .isActive(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //When
        userService.changeStatus(userId);


        //Then
        assertTrue(user.isActive());
    }
}
