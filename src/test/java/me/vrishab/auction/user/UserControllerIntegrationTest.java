package me.vrishab.auction.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.user.dto.UserEditableDTO;
import me.vrishab.auction.user.model.CreditCard;
import me.vrishab.auction.utils.Data;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration test for Authorized User API endpoints")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Tag("integration")
@ActiveProfiles("integration_test")
public class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    String token;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultAction = this.mockMvc.perform(post(this.baseUrl + "/users/login")
                .with(httpBasic("name1@domain.tld", "password")));

        MvcResult mvcResult = resultAction.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.token = "Bearer " + json.getJSONObject("data").getString("token");

    }

    @Test
    @DisplayName("Check findAll operation")
    void testFindAllUserSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Find all users"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(9)));
    }


    @Test
    @DisplayName("Check addUser operation")
    void testAddUserSuccess() throws Exception {
        UserEditableDTO userEditableDTO = new UserEditableDTO(
                "name",
                "password",
                "Description",
                "name@domain.tld",
                "1234567890",
                "00000",
                "street",
                "city",
                "country"
        );

        String userJson = this.objectMapper.writeValueAsString(userEditableDTO);

        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Add a user"))
                .andExpect(jsonPath("$.data.username").value("name@domain.tld"));

        MvcResult mvcResult = resultActions.andDo(print()).andReturn();

        String contentString = mvcResult.getResponse().getContentAsString();
        JSONObject jsonObject = new JSONObject(contentString);

        String userID = jsonObject.getJSONObject("data").getString("id");

        this.mockMvc.perform(get("/api/v1/users/name@domain.tld")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Find a user"))
                .andExpect(jsonPath("$.data.id").value(userID))
                .andExpect(jsonPath("$.data.username").value("name@domain.tld"))
                .andExpect(jsonPath("$.data.homeAddress.city").value("city"))
                .andExpect(jsonPath("$.data.homeAddress.street").value("street"))
                .andExpect(jsonPath("$.data.homeAddress.country").value("country"))
                .andExpect(jsonPath("$.data.homeAddress.zipcode").value("00000"));
    }

    @Test
    @DisplayName("Check updateUser operation")
    void testUpdateUserSuccess() throws Exception {

        UserEditableDTO userEditableDTO = new UserEditableDTO(
                "New Name",
                "Password",
                "New Description",
                "name@domain.tld",
                "1234567890",
                "00000",
                "New Street",
                "New City",
                "New Country"
        );


        String json = this.objectMapper.writeValueAsString(userEditableDTO);

        this.mockMvc.perform(put(this.baseUrl + "/user/self")
                        .header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Update a user"))
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.description").value("New Description"))
                .andExpect(jsonPath("$.data.homeAddress.city").value("New City"))
                .andExpect(jsonPath("$.data.homeAddress.street").value("New Street"))
                .andExpect(jsonPath("$.data.homeAddress.country").value("New Country"))
                .andExpect(jsonPath("$.data.homeAddress.zipcode").value("00000"));

    }

    @Test
    @DisplayName("Check deleteUser operation")
    void testDeleteUserSuccess() throws Exception {

        this.mockMvc.perform(delete(this.baseUrl + "/user/self")
                        .header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Delete a user"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check invalid zipcode")
    void testInvalidZipCode() throws Exception {
        UserEditableDTO userEditableDTO = new UserEditableDTO(
                "New Name",
                "Password",
                "New Description",
                "name@domain.tld",
                "1234567890",
                "00-23",
                "New Street",
                "New City",
                "New Country"
        );


        String json = this.objectMapper.writeValueAsString(userEditableDTO);

        this.mockMvc.perform(put(this.baseUrl + "/user/self")
                        .header("Authorization", this.token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("The zipcode 00-23 is invalid"))
                .andExpect(jsonPath("$.data", Matchers.nullValue()));
    }

    @Test
    @DisplayName("Check addBillingDetails operation")
    void testAddBillingDetails() throws Exception {

        CreditCard creditCard = Data.getCreditCard(0);

        String jsonString = this.objectMapper.writeValueAsString(creditCard);

        this.mockMvc.perform(put(this.baseUrl + "/user/self/billingDetails")
                        .header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Add Billing Details"));

        this.mockMvc.perform(get(this.baseUrl + "/user/self/billingDetails")
                        .header("Authorization", this.token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Get Billing Details"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.data[*].cardNumber", Matchers.hasItems(creditCard.getCardNumber())));
    }

}
