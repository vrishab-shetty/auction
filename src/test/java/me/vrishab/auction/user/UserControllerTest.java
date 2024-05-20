package me.vrishab.auction.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @MockBean
    UserService service;

    @Autowired
    MockMvc mockMvc;

    List<User> users;

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
        this.mockMvc.perform(get("/api/v1/users/name3@domain.tld").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find a user"))
                .andExpect(jsonPath("$.data.id").value("9a540a1e-b599-4cec-aeb1-6396eb8fa273"))
                .andExpect(jsonPath("$.data.username").value("name3@domain.tld"));
    }

    @Test
    void testFindUserByUsernameNotFound() throws Exception {

        // Given
        given(service.findByUsername(Mockito.anyString())).willThrow(new UserNotFoundException("name3@domain.tld"));

        // When and Then
        this.mockMvc.perform(get("/api/v1/users/name3@domain.tld").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("Could find user with username name3@domain.tld"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testFindAllUserSuccess() throws Exception {

        // Given
        given(service.findAll()).willReturn(users);

        // When and Then
        this.mockMvc.perform(get("/api/v1/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all users"))
                .andExpect(jsonPath("$.data.size()").value(10));

    }

}