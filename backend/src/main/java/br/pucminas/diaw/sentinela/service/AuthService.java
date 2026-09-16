package br.pucminas.diaw.sentinela.service;

import br.pucminas.diaw.sentinela.domain.Role;
import br.pucminas.diaw.sentinela.domain.User;
import br.pucminas.diaw.sentinela.dto.AuthResponse;
import br.pucminas.diaw.sentinela.dto.LoginRequest;
import br.pucminas.diaw.sentinela.dto.RegisterRequest;
import br.pucminas.diaw.sentinela.dto.UserResponse;
import br.pucminas.diaw.sentinela.exception.ApiException;
import br.pucminas.diaw.sentinela.repository.UserRepository;
import br.pucminas.diaw.sentinela.security.JwtService;
import br.pucminas.diaw.sentinela.security.RequestMetadata;
import br.pucminas.diaw.sentinela.security.UserPrincipal;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de cadastro, login, renovacao e encerramento de sessao. */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        String username = normalize(request.username());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("email", "Este e-mail ja esta cadastrado");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw ApiException.conflict("username", "Este nome de usuario ja esta em uso");
        }

        User user = new User(
                request.name().trim(),
                username,
                email,
                passwordEncoder.encode(request.password()),
                Role.USER);

        User saved = userRepository.save(user);
        log.info("Novo usuario cadastrado: {}", saved.getUsername());
        return UserResponse.from(saved);
    }

    @Transactional
    public AuthenticatedSession login(LoginRequest request, RequestMetadata metadata) {
        String identifier = request.identifier().trim();

        Duration lock = loginAttemptService.remainingLock(identifier, metadata.ip());
        if (!lock.isZero()) {
            throw ApiException.tooManyRequests(
                    "Muitas tentativas malsucedidas. Tente novamente em " + lock.toMinutes() + " minuto(s).");
        }

        UserPrincipal principal;
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.password()));
            principal = (UserPrincipal) authentication.getPrincipal();
        } catch (DisabledException e) {
            throw ApiException.forbidden("Esta conta esta desativada. Procure o administrador.");
        } catch (AuthenticationException e) {
            loginAttemptService.registerFailure(identifier, metadata.ip());
            int remaining = loginAttemptService.remainingAttempts(identifier, metadata.ip());
            String suffix = remaining > 0 && remaining <= 2
                    ? " Restam " + remaining + " tentativa(s) antes do bloqueio temporario."
                    : "";
            throw ApiException.unauthorized("Usuario ou senha invalidos." + suffix);
        }

        loginAttemptService.registerSuccess(identifier, metadata.ip());

        User user = principal.getUser();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return buildSession(user, request.rememberMe(), metadata);
    }

    @Transactional
    public AuthenticatedSession refresh(String rawRefreshToken, boolean rememberMe, RequestMetadata metadata) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw ApiException.unauthorized("Sessao nao encontrada. Faca login novamente.");
        }
        var rotation = refreshTokenService.rotate(rawRefreshToken, rememberMe, metadata.userAgent(), metadata.ip());
        User user = rotation.user();
        if (!user.isEnabled()) {
            throw ApiException.forbidden("Esta conta esta desativada.");
        }
        AuthResponse response = AuthResponse.of(
                jwtService.generateAccessToken(user), jwtService.accessTokenTtlSeconds(), UserResponse.from(user));
        return new AuthenticatedSession(response, rotation.token().rawToken(), rotation.token().ttl());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    private AuthenticatedSession buildSession(User user, boolean rememberMe, RequestMetadata metadata) {
        var issued = refreshTokenService.issue(user, rememberMe, metadata.userAgent(), metadata.ip());
        AuthResponse response = AuthResponse.of(
                jwtService.generateAccessToken(user), jwtService.accessTokenTtlSeconds(), UserResponse.from(user));
        return new AuthenticatedSession(response, issued.rawToken(), issued.ttl());
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    /** Resposta da API mais o refresh token que o controller grava no cookie. */
    public record AuthenticatedSession(AuthResponse response, String refreshToken, Duration refreshTtl) {
    }
}
