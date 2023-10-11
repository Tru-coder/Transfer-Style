package com.example.transferstylerebuildmaven.controllers.Demo;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class DemoControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testNonSecuredHelloEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/v1/auth/demo-controller")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Hello from non-secured end-point"));
    }
    @Test
    public void testRejectToSaySecuredHelloEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/non-auth/demo-controller")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testSaySecuredHelloEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/non-auth/demo-controller")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Hello from secured end-point"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    public void testGetUsernameUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/demo-controller/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("testUser"));
    }

    @Test
    @WithMockUser(username = "testAdmin", roles = "ADMIN")
    public void testGetUsernameAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/demo-controller/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("testAdmin"));
    }
    @Test
    @WithMockUser(username = "testManager", roles = "MANAGER")
    public void testGetUsernameManager() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/demo-controller/info")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("testManager"));
    }



}