package ru.itmo.infosec1;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.itmo.infosec1.repository.UserRepository;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "spring.datasource.url=jdbc:h2:mem:infosec1test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SecurityApiIntegrationTest {
    private static final String TEST_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;

    @BeforeEach
    void resetDatabase() {
        users.deleteAll();
    }

    private String body(String username, String password) throws Exception {
        return json.writeValueAsString(Map.of("username", username, "password", password));
    }

    private void register(String username, String password) throws Exception {
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(username, password)))
                .andExpect(status().isCreated());
    }

    private String login(String username, String password) throws Exception {
        String response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asString();
    }

    @Test
    void protectedEndpointRejectsMissingToken() throws Exception {
        mvc.perform(get("/api/data")).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationStoresBcryptAndValidLoginGrantsAccess() throws Exception {
        register("alice", "StrongPass123!");
        String hash = users.findByUsername("alice").orElseThrow().getPasswordHash();
        assertThat(hash).startsWith("$2a$").isNotEqualTo("StrongPass123!");
        assertThat(passwords.matches("StrongPass123!", hash)).isTrue();

        String token = login("alice", "StrongPass123!");
        mvc.perform(get("/api/data").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasItem("alice")));
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        register("alice", "StrongPass123!");
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("alice", "badPassword123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwtCannotBeForged() throws Exception {
        register("alice", "StrongPass123!");
        mvc.perform(get("/api/data").header("Authorization", "Bearer a.b.c"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredJwtReturns401() throws Exception {
        register("alice", "StrongPass123!");
        String expired = JWT.create()
                .withIssuer("infosec1")
                .withSubject("alice")
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(Base64.getDecoder().decode(TEST_SECRET)));
        mvc.perform(get("/api/data").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sqlInjectionStringDoesNotBypassAuthentication() throws Exception {
        register("alice", "StrongPass123!");
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("' OR 1=1 --", "anything")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void htmlFromUserIsEscapedBeforeReturningJson() throws Exception {
        String username = "<script>alert(1)</script>";
        register(username, "StrongPass123!");
        String token = login(username, "StrongPass123!");
        mvc.perform(get("/api/data").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users", hasItem("&lt;script&gt;alert(1)&lt;/script&gt;")));
    }

    @Test
    void duplicateRegistrationReturns409() throws Exception {
        register("alice", "StrongPass123!");
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("alice", "OtherPassword123!")))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRegistrationReturns400() throws Exception {
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("ab", "short")))
                .andExpect(status().isBadRequest());
    }
}
