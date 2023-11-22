package com.example.transferstylerebuildmaven.controllers.auth;

import com.example.transferstylerebuildmaven.models.user.User;
import com.example.transferstylerebuildmaven.repositories.UserRepository;
import com.example.transferstylerebuildmaven.requests.AuthenticationRequest;
import com.example.transferstylerebuildmaven.requests.RegisterRequest;
import com.example.transferstylerebuildmaven.services.AuthenticationService;
import com.example.transferstylerebuildmaven.services.JwtService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static com.example.transferstylerebuildmaven.models.user.Role.ADMIN;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthenticationControllerTest {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRegister() throws JsonProcessingException {
        RegisterRequest registerRequest =  RegisterRequest.builder()
                .username("adminious")
                .firstname("firstname")
                .lastname("lastname")
                .email("admin@mail.com")
                .password("Maximus1#")
                .role("ADMIN")
                .build();


        HttpHeaders headers = new HttpHeaders();
        HttpEntity<RegisterRequest> entity = new HttpEntity<>(registerRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/api/v1/auth/register", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        String accessTokenFromResponse = responseJson.get("access_token").asText();
        String refreshTokenFromResponse = responseJson.get("refresh_token").asText();

        assertEquals(registerRequest.getUsername(), jwtService.extractUsername(accessTokenFromResponse));
        assertEquals(registerRequest.getUsername(), jwtService.extractUsername(refreshTokenFromResponse));

    }

    @Test
    void testCreateUserThenAuthenticate() throws Exception {
        User user = User
                .builder()
                .username("username")
                .firstname("firstname")
                .lastname("lastname")
                .email("admin@mail.com")
                .password(passwordEncoder.encode("password"))
                .role(ADMIN)
                .build();

        userRepository.save(user);

        // see method test Register
        AuthenticationRequest authenticationRequest = AuthenticationRequest
                .builder()
                .username(user.getUsername())
                .password("password")
                .build();

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<AuthenticationRequest> entity = new HttpEntity<>(authenticationRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/api/v1/auth/authenticate", HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        String accessTokenFromResponse = responseJson.get("access_token").asText();
        String refreshTokenFromResponse = responseJson.get("refresh_token").asText();

        assertEquals(authenticationRequest.getUsername(), jwtService.extractUsername(accessTokenFromResponse));
        assertEquals(authenticationRequest.getUsername(), jwtService.extractUsername(refreshTokenFromResponse));


    }

    @Test
    public void testRefreshToken_Success() throws IOException {

    }


    private static String asJsonString(Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}