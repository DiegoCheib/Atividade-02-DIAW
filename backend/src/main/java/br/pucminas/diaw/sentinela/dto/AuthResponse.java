package br.pucminas.diaw.sentinela.dto;

/**
 * Resposta dos endpoints de login e refresh.
 * O refresh token nao aparece aqui: ele viaja em cookie HttpOnly.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {

    public static AuthResponse of(String accessToken, long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, "Bearer", expiresInSeconds, user);
    }
}
