package br.pucminas.diaw.sentinela.service;

import br.pucminas.diaw.sentinela.config.MailProperties;
import br.pucminas.diaw.sentinela.domain.PasswordResetToken;
import br.pucminas.diaw.sentinela.domain.User;
import br.pucminas.diaw.sentinela.dto.ResetPasswordRequest;
import br.pucminas.diaw.sentinela.exception.ApiException;
import br.pucminas.diaw.sentinela.repository.PasswordResetTokenRepository;
import br.pucminas.diaw.sentinela.repository.UserRepository;
import br.pucminas.diaw.sentinela.security.TokenHasher;
import br.pucminas.diaw.sentinela.service.mail.PasswordResetEmail;
import br.pucminas.diaw.sentinela.service.mail.ResendMailer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fluxo de recuperacao de senha em duas etapas.
 *
 * <p>1) O usuario informa o e-mail e recebe pelo Resend um link com um token
 * opaco de uso unico. 2) O link abre a tela de redefinicao, que envia o token
 * junto da nova senha.
 *
 * <p>A resposta do primeiro passo e sempre a mesma, exista ou nao a conta, para
 * nao permitir enumeracao de e-mails cadastrados. Somente o hash SHA-256 do
 * token e gravado, e ao concluir a troca todas as sessoes ativas caem.
 */
@Service
public class PasswordRecoveryService {

    private static final Logger log = LoggerFactory.getLogger(PasswordRecoveryService.class);

    private static final String GENERIC_MESSAGE =
            "Se este e-mail estiver cadastrado, enviamos um link para redefinir sua senha. "
                    + "Confira tambem a caixa de spam.";

    private static final String INVALID_TOKEN_MESSAGE =
            "Este link de redefinicao e invalido ou ja expirou. Solicite um novo.";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final ResendMailer mailer;
    private final MailProperties mailProperties;

    public PasswordRecoveryService(UserRepository userRepository,
                                   PasswordResetTokenRepository tokenRepository,
                                   PasswordEncoder passwordEncoder,
                                   RefreshTokenService refreshTokenService,
                                   ResendMailer mailer,
                                   MailProperties mailProperties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.mailer = mailer;
        this.mailProperties = mailProperties;
    }

    /** Passo 1: emite o token e dispara o e-mail. A mensagem devolvida e sempre generica. */
    @Transactional
    public String requestRecovery(String email, String ip) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        Optional<User> found = userRepository.findByEmailIgnoreCase(normalized);

        if (found.isEmpty()) {
            log.info("Solicitacao de recuperacao para e-mail nao cadastrado");
            return GENERIC_MESSAGE;
        }

        User user = found.get();
        if (!user.isEnabled()) {
            log.info("Solicitacao de recuperacao ignorada: conta {} desativada", user.getUsername());
            return GENERIC_MESSAGE;
        }

        long recentes = tokenRepository.countByUserAndCreatedAtAfter(user, Instant.now().minus(Duration.ofHours(1)));
        if (recentes >= mailProperties.maxRequestsPerHour()) {
            log.warn("Limite de pedidos de recuperacao atingido pelo usuario {}", user.getUsername());
            return GENERIC_MESSAGE;
        }

        // Um pedido novo cancela os links anteriores: apenas o ultimo e-mail vale.
        tokenRepository.invalidateAllForUser(user, Instant.now());

        Duration ttl = mailProperties.resetTokenTtl();
        String rawToken = TokenHasher.randomToken();
        tokenRepository.save(new PasswordResetToken(
                TokenHasher.sha256(rawToken), user, Instant.now().plus(ttl), ip));

        String link = mailProperties.normalizedBaseUrl()
                + "/resetpassword?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        boolean enviado = mailer.send(
                PasswordResetEmail.build(user.getEmail(), firstName(user.getName()), link, ttl));
        if (enviado) {
            log.info("Link de redefinicao enviado para o usuario {}", user.getUsername());
        }

        return GENERIC_MESSAGE;
    }

    /** Passo 2a: a tela de redefinicao confere o token antes de pedir a nova senha. */
    @Transactional(readOnly = true)
    public String maskedEmailFor(String rawToken) {
        return maskEmail(requireUsableToken(rawToken).getUser().getEmail());
    }

    /** Passo 2b: grava a nova senha, queima o token e derruba as sessoes abertas. */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = requireUsableToken(request.token());
        User user = token.getUser();

        if (passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.badRequest("password", "A nova senha precisa ser diferente da senha atual");
        }

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        token.markUsed();
        // Qualquer sessao aberta com a senha antiga deixa de valer.
        refreshTokenService.revokeAllForUser(user);
        log.info("Senha redefinida por link de e-mail para o usuario {}", user.getUsername());

        return "Senha redefinida com sucesso. Entre com a nova senha.";
    }

    /** Remove diariamente os tokens ja vencidos. */
    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void purgeExpired() {
        int removidos = tokenRepository.deleteExpired(Instant.now());
        if (removidos > 0) {
            log.info("{} tokens de redefinicao expirados removidos", removidos);
        }
    }

    private PasswordResetToken requireUsableToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw ApiException.badRequest(INVALID_TOKEN_MESSAGE);
        }
        PasswordResetToken token = tokenRepository.findByTokenHash(TokenHasher.sha256(rawToken))
                .orElseThrow(() -> ApiException.badRequest(INVALID_TOKEN_MESSAGE));
        if (!token.isUsable()) {
            throw ApiException.badRequest(INVALID_TOKEN_MESSAGE);
        }
        return token;
    }

    private static String firstName(String name) {
        int space = name.indexOf(' ');
        return space > 0 ? name.substring(0, space) : name;
    }

    /** Mostra o suficiente para o usuario reconhecer a conta sem expor o endereco. */
    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        String local = email.substring(0, at);
        String visivel = local.length() <= 2 ? local.substring(0, 1) : local.substring(0, 2);
        return visivel + "***" + email.substring(at);
    }
}
