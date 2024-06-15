package me.vrishab.auction.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.security.AuthService;
import me.vrishab.auction.user.UserException.UserNotFoundByIdException;
import me.vrishab.auction.user.UserException.UserNotFoundByUsernameException;
import me.vrishab.auction.user.dto.UserEditableDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.vrishab.auction.user.UserException.UserEmailAlreadyExistException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTest {

    @MockBean
    UserService service;

    @MockBean
    AuthService authService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    List<User> users;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

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
    void testFindUserByUsernameSuccess() throws Exception {

        // Given
        given(service.findByUsername("name3@domain.tld")).willReturn(users.get(3));

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/users/name3@domain.tld").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find a user"))
                .andExpect(jsonPath("$.data.id").value("9a540a1e-b599-4cec-aeb1-6396eb8fa273"))
                .andExpect(jsonPath("$.data.username").value("name3@domain.tld"));
    }

    @Test
    void testFindUserByUsernameNotFound() throws Exception {

        // Given
        given(service.findByUsername(Mockito.anyString())).willThrow(new UserNotFoundByUsernameException("name3@domain.tld"));

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/users/name3@domain.tld").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Could find user with username name3@domain.tld"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllUserSuccess() throws Exception {

        // Given
        given(service.findAll()).willReturn(users);

        // When and Then
        this.mockMvc.perform(get(baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all users"))
                .andExpect(jsonPath("$.data.size()").value(10));

    }

    @Test
    void testAddUserSuccess() throws Exception {

        // Given
        UserEditableDTO userEditableDTO = new UserEditableDTO(
                "Name",
                "Password",
                "Description",
                "name@domain.tld",
                "1234567890"
        );

        String userJson = this.objectMapper.writeValueAsString(userEditableDTO);

        User savedUser = new User();
        savedUser.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
        savedUser.setName("Name");
        savedUser.setPassword("Password");
        savedUser.setDescription("Description");
        savedUser.setEmail("name@domain.tld");
        savedUser.setContact("1234567890");
        savedUser.setEnabled(true);

        given(service.save(Mockito.any(User.class))).willReturn(savedUser);

        // When and then
        this.mockMvc.perform(post(baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Add a user"))
                .andExpect(jsonPath("$.data.id").value("9a540a1e-b599-4cec-aeb1-6396eb8fa271"))
                .andExpect(jsonPath("$.data.username").value("name@domain.tld"));
    }

    @Test
    void testAddUserUsernameAlreadyExist() throws Exception {

        // Given
        UserEditableDTO userEditableDTO = new UserEditableDTO(
                "Name",
                "Password",
                "Description",
                "name@domain.tld",
                "1234567890"
        );

        String userJson = this.objectMapper.writeValueAsString(userEditableDTO);

        given(service.save(Mockito.any(User.class))).willThrow(new UserEmailAlreadyExistException("name@domain.tld"));

        // When and then
        this.mockMvc.perform(post(baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("The email name@domain.tld already exist"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateUserSuccess() throws Exception {

        // Given
        UserEditableDTO userEditableDTO = new UserEditableDTO(
                "New Name",
                "Password",
                "New Description",
                "name@domain.tld",
                "1234567890"
        );

        String json = this.objectMapper.writeValueAsString(userEditableDTO);

        User updatedUser = new User();
        updatedUser.setId(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"));
        updatedUser.setName("New Name");
        updatedUser.setPassword("Password");
        updatedUser.setDescription("New Description");
        updatedUser.setEmail("name@domain.tld");
        updatedUser.setContact("1234567890");
        updatedUser.setEnabled(false);

        given(this.service.update(eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"), Mockito.any(User.class))).willReturn(updatedUser);
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        // When and then

        this.mockMvc.perform(put(baseUrl + "/user/self")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Update a user"))
                .andExpect(jsonPath("$.data.id").value("9a540a1e-b599-4cec-aeb1-6396eb8fa271"))
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.description").value("New Description"));
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        UserEditableDTO userEditableDTO = new UserEditableDTO(
                "New Name",
                "Password",
                "New Description",
                "name@domain.tld",
                "1234567890"
        );

        String json = this.objectMapper.writeValueAsString(userEditableDTO);


        given(this.service.update(eq("9a540a1e-b599-4cec-aeb1-6396eb8fa271"), Mockito.any(User.class)))
                .willThrow(new UserNotFoundByIdException(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271")));
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        // When and then

        this.mockMvc.perform(put(baseUrl + "/user/self")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 9a540a1e-b599-4cec-aeb1-6396eb8fa271"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserSuccess() throws Exception {

        // Given
        doNothing().when(this.service).delete("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        // When and then

        this.mockMvc.perform(delete(baseUrl + "/user/self")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Delete a user"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserNotFound() throws Exception {

        // Given
        doThrow(new UserNotFoundByIdException(UUID.fromString("9a540a1e-b599-4cec-aeb1-6396eb8fa271"))).when(this.service).delete("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        given(this.authService.getUserInfo(Mockito.any())).willReturn("9a540a1e-b599-4cec-aeb1-6396eb8fa271");
        // When and then


        this.mockMvc.perform(delete(baseUrl + "/user/self")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 9a540a1e-b599-4cec-aeb1-6396eb8fa271"))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
