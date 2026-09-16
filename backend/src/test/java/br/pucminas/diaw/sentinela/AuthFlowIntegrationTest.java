package br.pucminas.diaw.sentinela;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.pucminas.diaw.sentinela.domain.PasswordResetToken;
import br.pucminas.diaw.sentinela.repository.PasswordResetTokenRepository;
import br.pucminas.diaw.sentinela.repository.RefreshTokenRepository;
import br.pucminas.diaw.sentinela.repository.UserRepository;
import br.pucminas.diaw.sentinela.security.TokenHasher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private static final String EMAIL = "maria@pucminas.br";
    private static final String USERNAME = "maria";
    private static final String PASSWORD = "Senha@2026";

    @BeforeEach
    void cleanDatabase() {
        // Os tokens referenciam os usuarios, entao saem primeiro.
        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private void register() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Maria Silva",
                                "username", USERNAME,
                                "email", EMAIL,
                                "password", PASSWORD,
                                "confirmPassword", PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private String issuePasswordResetToken() {
        String rawToken = TokenHasher.randomToken();
        var user = userRepository.findByEmailIgnoreCase(EMAIL).orElseThrow();
        passwordResetTokenRepository.saveAndFlush(new PasswordResetToken(
                TokenHasher.sha256(rawToken), user, Instant.now().plus(Duration.ofMinutes(30)), "127.0.0.1"));
        return rawToken;
    }

    @Test
    @DisplayName("cadastra o usuario e grava a senha com hash BCrypt")
    void registersUserWithHashedPassword() throws Exception {
        register();

        var saved = userRepository.findByEmailIgnoreCase(EMAIL).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
    }

    @Test
    @DisplayName("bloqueia cadastro com e-mail duplicado")
    void rejectsDuplicatedEmail() throws Exception {
        register();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Outra Maria",
                                "username", "maria2",
                                "email", EMAIL,
                                "password", PASSWORD,
                                "confirmPassword", PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    @Test
    @DisplayName("bloqueia cadastro quando a confirmacao de senha nao confere")
    void rejectsPasswordMismatch() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Maria Silva",
                                "username", USERNAME,
                                "email", EMAIL,
                                "password", PASSWORD,
                                "confirmPassword", "Outra@2026"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("confirmPassword"));
    }

    @Test
    @DisplayName("bloqueia cadastro com senha fraca")
    void rejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "name", "Maria Silva",
                                "username", USERNAME,
                                "email", EMAIL,
                                "password", "123456",
                                "confirmPassword", "123456"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'password')]").exists());
    }

    @Test
    @DisplayName("autentica por e-mail ou usuario e devolve access token com cookie de refresh")
    void loginReturnsTokenAndRefreshCookie() throws Exception {
        register();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("identifier", EMAIL, "password", PASSWORD, "rememberMe", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value(USERNAME))
                .andReturn();

        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookie).contains("sentinela_refresh").contains("HttpOnly");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("identifier", USERNAME, "password", PASSWORD, "rememberMe", false))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("recusa credenciais invalidas com 401")
    void rejectsInvalidCredentials() throws Exception {
        register();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("identifier", EMAIL, "password", "Errada@123", "rememberMe", false))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("invalidos")));
    }

    @Test
    @DisplayName("area protegida exige token valido")
    void protectedAreaRequiresToken() throws Exception {
        register();

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("identifier", EMAIL, "password", PASSWORD, "rememberMe", false))))
                .andReturn();

        String accessToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(get("/api/dashboard").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(EMAIL));

        mockMvc.perform(get("/api/dashboard").header("Authorization", "Bearer token.invalido.aqui"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("usuario comum nao acessa a area administrativa")
    void regularUserCannotReachAdminArea() throws Exception {
        register();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("identifier", EMAIL, "password", PASSWORD, "rememberMe", false))))
                .andReturn();
        String accessToken = objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("refresh rotaciona o cookie e emite novo access token")
    void refreshRotatesSession() throws Exception {
        register();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("identifier", EMAIL, "password", PASSWORD, "rememberMe", false))))
                .andReturn();

        Cookie refreshCookie = login.getResponse().getCookie("sentinela_refresh");
        assertThat(refreshCookie).isNotNull();

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Cookie rotated = refreshed.getResponse().getCookie("sentinela_refresh");
        assertThat(rotated).isNotNull();
        assertThat(rotated.getValue()).isNotEqualTo(refreshCookie.getValue());

        // O cookie antigo foi revogado pela rotacao
        mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("logout revoga a sessao e limpa o cookie")
    void logoutRevokesSession() throws Exception {
        register();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("identifier", EMAIL, "password", PASSWORD, "rememberMe", false))))
                .andReturn();
        Cookie refreshCookie = login.getResponse().getCookie("sentinela_refresh");

        mockMvc.perform(post("/api/auth/logout").cookie(refreshCookie))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("recuperacao de senha responde mensagem generica")
    void recoverPasswordAlwaysReturnsGenericMessage() throws Exception {
        mockMvc.perform(post("/api/auth/recoverpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "naoexiste@pucminas.br"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(post("/recoverpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "naoexiste@pucminas.br"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("recuperacao emite token somente para uma conta cadastrada")
    void recoveryIssuesTokenForRegisteredAccount() throws Exception {
        register();

        mockMvc.perform(post("/api/auth/recoverpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", EMAIL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Se este e-mail")));

        var user = userRepository.findByEmailIgnoreCase(EMAIL).orElseThrow();
        assertThat(passwordResetTokenRepository.countByUserAndCreatedAtAfter(
                user, Instant.now().minus(Duration.ofMinutes(1)))).isEqualTo(1);
    }

    @Test
    @DisplayName("redefine a senha, consome o token e revoga sessoes abertas")
    void resetsPasswordOnceAndRevokesSessions() throws Exception {
        register();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "identifier", EMAIL, "password", PASSWORD, "rememberMe", false))))
                .andExpect(status().isOk())
                .andReturn();
        Cookie oldRefreshCookie = login.getResponse().getCookie("sentinela_refresh");

        String rawToken = issuePasswordResetToken();
        mockMvc.perform(get("/api/auth/resetpassword").param("token", rawToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ma***@pucminas.br"));

        String newPassword = "Nova@2026";
        mockMvc.perform(post("/api/auth/resetpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "token", rawToken,
                                "password", newPassword,
                                "confirmPassword", newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("sucesso")));

        // O mesmo link nao pode ser reutilizado.
        mockMvc.perform(get("/api/auth/resetpassword").param("token", rawToken))
                .andExpect(status().isBadRequest());

        // A senha antiga e a sessao emitida antes da troca deixam de valer.
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "identifier", EMAIL, "password", PASSWORD, "rememberMe", false))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/refresh").cookie(oldRefreshCookie))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "identifier", EMAIL, "password", newPassword, "rememberMe", false))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("rejeita link invalido e dados de nova senha inconsistentes")
    void rejectsInvalidResetLinkAndPassword() throws Exception {
        mockMvc.perform(get("/api/auth/resetpassword").param("token", "token-inexistente"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("invalido")));

        mockMvc.perform(post("/api/auth/resetpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "token", "token-inexistente",
                                "password", "Senha@2027",
                                "confirmPassword", "Outra@2027"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'confirmPassword')]").exists());
    }
}
