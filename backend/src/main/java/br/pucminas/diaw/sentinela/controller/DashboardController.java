package br.pucminas.diaw.sentinela.controller;

import br.pucminas.diaw.sentinela.dto.UserResponse;
import br.pucminas.diaw.sentinela.repository.RefreshTokenRepository;
import br.pucminas.diaw.sentinela.repository.UserRepository;
import br.pucminas.diaw.sentinela.security.UserPrincipal;
import java.time.Instant;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Area protegida: so responde com um access token valido no header Authorization. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public DashboardController(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public record DashboardSummary(
            UserResponse user,
            long totalUsers,
            int activeSessions,
            Instant serverTime) {
    }

    @GetMapping
    public DashboardSummary summary(@AuthenticationPrincipal UserPrincipal principal) {
        int activeSessions = refreshTokenRepository
                .findAllByUserAndRevokedAtIsNull(principal.getUser())
                .stream()
                .filter(token -> token.isActive())
                .toList()
                .size();

        return new DashboardSummary(
                UserResponse.from(principal.getUser()),
                userRepository.count(),
                activeSessions,
                Instant.now());
    }
}
