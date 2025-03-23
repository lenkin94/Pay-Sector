package app.paysector.user.service;


import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.model.UserRole;
import app.paysector.user.repository.UserRepository;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.ChangePasswordRequest;
import app.paysector.web.dto.EditProfileRequest;
import app.paysector.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletService walletService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
    }

    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(RegisterRequest registerRequest) {
        Optional<User> byUsername = userRepository.findByUsername(registerRequest.getUsername());

        if (byUsername.isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        Optional<User> byEmail = userRepository.findByEmail(registerRequest.getEmail());

        if (byEmail.isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.save(initializeUser(registerRequest));

        Wallet wallet = walletService.createWallet(user.getId());

        user.setWallet(wallet);

        return userRepository.save(user);
    }

    public User initializeUser(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role(UserRole.USER)
                .isActive(true)
                .country(registerRequest.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User with username [%s] does not exist".formatted(username)));



        return new AuthenticateUser(user.getId(), username, user.getPassword(), user.getRole(), user.isActive());
    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User with id '%s' does not exist".formatted(userId)));
    }

    public Optional<User> getByUsername(String username) {
       return userRepository.findByUsername(username);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void updateUser(User user) {
        userRepository.save(user);
    }


    @CacheEvict(value = "users", allEntries = true)
    public void editProfile(UUID userId, EditProfileRequest editProfileRequest) {
        User user = getById(userId);

        user.setFirstName(editProfileRequest.getFirstName());
        user.setLastName(editProfileRequest.getLastName());
        user.setEmail(editProfileRequest.getEmail());
        user.setProfilePicture(editProfileRequest.getProfilePicture());

        userRepository.save(user);
    }

    public void changePassword(UUID userId, ChangePasswordRequest changePasswordRequest) {

        User user = getById(userId);

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword()) ||
                !changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeStatus(UUID userId) {
        User user = getById(userId);

        user.setActive(!user.isActive());

        userRepository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeRole(UUID userId) {
        User user = getById(userId);
        if (user.getRole() == UserRole.USER) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        userRepository.save(user);
    }

    public boolean isActiveUser(UUID userId) {
        User user = getById(userId);

        return user.isActive();
    }
}
