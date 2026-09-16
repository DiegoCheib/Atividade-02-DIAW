package br.pucminas.diaw.sentinela;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.pucminas.diaw.sentinela.config.JwtProperties;
import br.pucminas.diaw.sentinela.domain.Role;
import br.pucminas.diaw.sentinela.domain.User;
import br.pucminas.diaw.sentinela.security.JwtService;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "segredo-de-teste-com-mais-de-32-caracteres";

    private static JwtProperties properties(Duration accessTtl, String secret) {
        return new JwtProperties(secret, "sentinela-diaw", accessTtl,
                Duration.ofDays(7), Duration.ofDays(30), "sentinela_refresh", "/api/auth", false, "Lax");
    }

    private static User user() throws Exception {
        User user = new User("Ana Souza", "ana", "ana@pucminas.br", "hash", Role.USER);
        Field id = User.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(user, UUID.randomUUID());
        return user;
    }

    @Test
    @DisplayName("gera token com as claims do usuario e valida a assinatura")
    void generatesAndParsesToken() throws Exception {
        JwtService service = new JwtService(properties(Duration.ofMinutes(15), SECRET));
        User user = user();

        String token = service.generateAccessToken(user);
        var claims = service.parseToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.getIssuer()).isEqualTo("sentinela-diaw");
        assertThat(claims.get("username")).isEqualTo("ana");
        assertThat(claims.get("role")).isEqualTo("USER");
        assertThat(service.accessTokenTtlSeconds()).isEqualTo(900L);
    }

    @Test
    @DisplayName("rejeita token assinado com outra chave")
    void rejectsTokenSignedWithAnotherKey() throws Exception {
        JwtService issuer = new JwtService(properties(Duration.ofMinutes(15), SECRET));
        JwtService other = new JwtService(properties(Duration.ofMinutes(15), "outra-chave-de-teste-com-32-caracteres+"));

        String token = issuer.generateAccessToken(user());

        assertThat(other.parseToken(token)).isNull();
    }

    @Test
    @DisplayName("rejeita token expirado")
    void rejectsExpiredToken() throws Exception {
        JwtService service = new JwtService(properties(Duration.ofSeconds(-10), SECRET));

        String token = service.generateAccessToken(user());

        assertThat(service.parseToken(token)).isNull();
    }

    @Test
    @DisplayName("recusa segredo curto demais")
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService(properties(Duration.ofMinutes(15), "curto")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.secret");
    }
}
