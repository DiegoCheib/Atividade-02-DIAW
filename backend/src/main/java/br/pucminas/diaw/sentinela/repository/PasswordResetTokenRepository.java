package br.pucminas.diaw.sentinela.repository;

import br.pucminas.diaw.sentinela.domain.PasswordResetToken;
import br.pucminas.diaw.sentinela.domain.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /** Quantos pedidos o usuario abriu na janela recente, para limitar reenvios. */
    long countByUserAndCreatedAtAfter(User user, Instant since);

    /** Um novo pedido invalida os anteriores: so o ultimo link continua valendo. */
    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :now where t.user = :user and t.usedAt is null")
    int invalidateAllForUser(@Param("user") User user, @Param("now") Instant now);

    @Modifying
    @Query("delete from PasswordResetToken t where t.expiresAt < :limit")
    int deleteExpired(@Param("limit") Instant limit);
}
