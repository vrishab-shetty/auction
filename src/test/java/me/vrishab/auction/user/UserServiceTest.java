package me.vrishab.auction.user;

import me.vrishab.auction.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    UserService service;

    ArrayList<User> users;

    @BeforeEach
    void setUp() {

        users = new ArrayList<>();
        /*
        {
            "data": {
                "name": "Vicky",
                "description": "A tool lover",
                "enabled": true,
                "email": "name@domain.tld",
                "contact": "1234567890",
                "wishlist": [
                  "2a2a2de5-ecea-4f25-bd1a-99a01a0be135",
                  "2a2a2de5-ecea-4f25-bd1a-99a01a0be134"
                ],
                "auctions": [
                  "0180280f-50e5-44b5-a744-37361f60c611",
                  "0180280f-50e5-44b5-a744-37361f60c612"
                ]
            }
         */
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa27" + i));
            user.setName("Name " + i);
            user.setPassword("password");
            user.setDescription("Description " + i);
            user.setEnabled(true);
            user.setEmail("name" + i + "@domain.tld");
            user.setContact("1234567890");
            users.add(user);
        }

    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testFindByUsernameSuccess() {

        // Given
        given(repository.findByEmail("name1@domain.tld")).willReturn(Optional.ofNullable(users.get(1)));

        // When
        User returnedUser = service.findByUsername("name1@domain.tld");

        // Then
        assertAll(
                () -> assertThat(returnedUser.getId()).isEqualTo(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271")),
                () -> assertThat(returnedUser.getEmail()).isEqualTo("name1@domain.tld")
        );

        verify(repository, times(1)).findByEmail("name1@domain.tld");
    }

    @Test
    void testFindByUsernameNotFound() {

        // Given
        given(repository.findByEmail(Mockito.any(String.class))).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> {
            User returnedUser = service.findByUsername("name1@domain.tld");
        });

        // Then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class).hasMessage("Could find user with username name1@domain.tld");

        verify(repository, times(1)).findByEmail("name1@domain.tld");
    }

    @Test
    void testFindAllUserSuccess() {

        // Given
        given(repository.findAll()).willReturn(users);

        // When
        List<User> returnedUsers = service.findAll();

        // Then
        assertThat(returnedUsers.size()).isEqualTo(users.size());

        verify(repository, times(1)).findAll();

    }

    @Test
    void testSaveUserSuccess() {

        // Given
        User newUser = users.get(5);

        given(repository.save(newUser)).willReturn(newUser);
        given(encoder.encode(newUser.getPassword())).willReturn("$2a$10$SgFzATB7cRNuSmWdCK0EA.bOZorh4/TjouLRLWSDxwCR4hGo/6/5i");

        // When
        User savedUser = service.save(newUser);

        // Then
        assertThat(savedUser.getId().toString()).isEqualTo("9a540a1e-b599-4cec-aeb1-6396eb8fa275");
        verify(repository, times(1)).save(newUser);
    }

    @Test
    void testSaveUserBadRequestEmail() {

        // Given
        User newUser = users.get(0);
        given(repository.existsByEmail(Mockito.anyString())).willReturn(true);

        // When
        Throwable thrown = catchThrowable(() -> {
            service.save(newUser);
        });

        // Then
        assertThat(thrown).isInstanceOf(UserEmailAlreadyExistException.class).hasMessage("The email name0@domain.tld already exist");
    }

    @Test
    void testUpdateUserSuccess() {

        // Given
        User oldUser = new User();
        oldUser.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
        oldUser.setName("Name");
        oldUser.setPassword("Password");
        oldUser.setDescription("Description");
        oldUser.setEmail("name@domain.tld");
        oldUser.setContact("1234567890");
        oldUser.setEnabled(true);

        User update = new User();
        update.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
        update.setName("New Name");
        update.setPassword("$2a$10$w0.OcE5rFi5iXGm/cQjMeOH4ht9SxWoOn8Lao9veuQkZJrxoMQQOm");
        update.setDescription("New Description");
        update.setEmail("name@domain.tld");
        update.setContact("1234567890");
        update.setEnabled(true);

        given(repository.findById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"))).willReturn(Optional.of(oldUser));
        given(encoder.encode(update.getPassword())).willReturn("$2a$10$w0.OcE5rFi5iXGm/cQjMeOH4ht9SxWoOn8Lao9veuQkZJrxoMQQOm");
        given(repository.save(oldUser)).willReturn(update);

        // When
        User updatedUser = service.update("9a540a1e-b599-4cec-aeb1-6396eb8fa271", update);

        // Then
        assertAll(
                () -> assertThat(updatedUser.getId()).isEqualTo(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271")),
                () -> assertThat(updatedUser.getName()).isEqualTo("New Name"),
                () -> assertThat(updatedUser.getDescription()).isEqualTo("New Description")
        );
        verify(repository, times(1)).save(oldUser);
        verify(repository, times(1)).findById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
    }

    @Test
    void testUpdateUserNotFound() {

        // Given
        User update = new User();
        update.setName("New Name");
        update.setPassword("New Password");
        update.setDescription("New Description");
        update.setEmail("name@domain.tld");
        update.setContact("1234567890");
        update.setEnabled(true);

        given(repository.findById(Mockito.any(UUID.class))).willReturn(Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> {
            service.update("9a540a1e-b599-4cec-aeb1-6396eb8fa271", update);
        });

        // Then
        verify(repository, times(1)).findById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
    }

    @Test
    void testDeleteUserSuccess() {

        // Given
        User user = new User();
        user.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
        user.setName("New Name");
        user.setPassword("New Password");
        user.setDescription("New Description");
        user.setEmail("name@domain.tld");
        user.setContact("1234567890");
        user.setEnabled(true);

        given(repository.findById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"))).willReturn(Optional.of(user));
        doNothing().when(repository).deleteById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));

        // When
        service.delete("9a540a1e-b599-4cec-aeb1-6396eb8fa271");

        // Then
        verify(repository, times(1)).findById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
        verify(repository, times(1)).deleteById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
    }

    @Test
    void testDeleteUserNotFound() {

        // Given
        given(repository.findById(Mockito.any(UUID.class))).willReturn(Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> service.delete("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));

        // Then
        verify(repository, times(1)).findById(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
    }
}