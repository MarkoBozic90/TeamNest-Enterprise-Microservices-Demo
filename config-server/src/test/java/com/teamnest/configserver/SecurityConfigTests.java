package com.teamnest.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.teamnest.configserver.TestConstants.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

    @Autowired private MockMvc mockMvc;

    @Test
    void healthShouldBePublic() throws Exception {
        mockMvc.perform(get(ACTUATOR_HEALTH)).andExpect(status().isOk());
    }

    @Test
    void prometheusShouldBePublic() throws Exception {
        mockMvc.perform(get(ACTUATOR_PROM)).andExpect(status().isOk());
    }

    @Test
    void busrefreshShouldRequireAuth() throws Exception {
        mockMvc.perform(post(ACTUATOR_BUSREF)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }
}