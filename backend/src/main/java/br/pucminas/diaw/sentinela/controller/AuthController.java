package br.pucminas.diaw.sentinela.controller;

import br.pucminas.diaw.sentinela.dto.AuthResponse;
import br.pucminas.diaw.sentinela.dto.LoginRequest;
import br.pucminas.diaw.sentinela.dto.MessageResponse;
import br.pucminas.diaw.sentinela.dto.RecoverPasswordRequest;
import br.pucminas.diaw.sentinela.dto.RegisterRequest;
import br.pucminas.diaw.sentinela.dto.ResetPasswordRequest;
import br.pucminas.diaw.sentinela.dto.ResetTokenResponse;
import br.pucminas.diaw.sentinela.dto.UserResponse;
import br.pucminas.diaw.sentinela.security.CookieService;
import br.pucminas.diaw.sentinela.security.RequestMetadata;
import br.pucminas.diaw.sentinela.security.UserPrincipal;
import br.pucminas.diaw.sentinela.service.AuthService;
import br.pucminas.diaw.sentinela.service.PasswordRecoveryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de autenticacao consumidos pelo front-end React. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;
    private final CookieService cookieService;

    public AuthController(AuthService authService,
                          PasswordRecoveryService passwordRecoveryService,
                          CookieService cookieService) {
        this.authService = authService;
        this.passwordRecoveryService = passwordRecoveryService;
        this.cookieService = cookieService;
    }

    /** POST /api/auth/register - cria a conta e devolve os dados publicos do usuario. */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /** POST /api/auth/login - valida credenciais, devolve o access token e grava o cookie de refresh. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        var session = authService.login(request, RequestMetadata.from(httpRequest));
        return ResponseEntity.ok()
                .header(cookieService.header(),
                        cookieService.buildSetCookieHeader(session.refreshToken(), session.refreshTtl()))
                .body(session.response());
    }

    /** POST /api/auth/refresh - rotaciona o refresh token e emite um novo access token. */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest httpRequest) {
        String rawToken = cookieService.readRefreshToken(httpRequest).orElse(null);
        var session = authService.refresh(rawToken, false, RequestMetadata.from(httpRequest));
        return ResponseEntity.ok()
                .header(cookieService.header(),
                        cookieService.buildSetCookieHeader(session.refreshToken(), session.refreshTtl()))
                .body(session.response());
    }

    /** POST /api/auth/logout - revoga o refresh token e limpa o cookie. */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest httpRequest) {
        cookieService.readRefreshToken(httpRequest).ifPresent(authService::logout);
        return ResponseEntity.ok()
                .header(cookieService.header(), cookieService.buildClearCookieHeader())
                .body(new MessageResponse("Sessao encerrada com sucesso"));
    }

    /** GET /api/auth/me - dados do usuario autenticado, resolvidos a partir do JWT. */
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(principal.getUser());
    }

    /** POST /api/auth/recoverpassword - envia por e-mail o link de redefinicao de senha. */
    @PostMapping("/recoverpassword")
    public MessageResponse recoverPassword(@Valid @RequestBody RecoverPasswordRequest request,
                                           HttpServletRequest httpRequest) {
        String message = passwordRecoveryService.requestRecovery(
                request.email(), RequestMetadata.from(httpRequest).ip());
        return new MessageResponse(message);
    }

    /** GET /api/auth/resetpassword - confere o token do link antes de exibir o formulario. */
    @GetMapping("/resetpassword")
    public ResetTokenResponse checkResetToken(@RequestParam(name = "token", required = false) String token) {
        return new ResetTokenResponse(passwordRecoveryService.maskedEmailFor(token));
    }

    /** POST /api/auth/resetpassword - grava a nova senha e encerra as sessoes abertas. */
    @PostMapping("/resetpassword")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return new MessageResponse(passwordRecoveryService.resetPassword(request));
    }
}
