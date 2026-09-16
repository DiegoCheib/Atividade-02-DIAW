package br.pucminas.diaw.sentinela.repository;

import br.pucminas.diaw.sentinela.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    /** Permite o login tanto por nome de usuario quanto por e-mail. */
    @Query("select u from User u where lower(u.username) = lower(:identifier) or lower(u.email) = lower(:identifier)")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findAllByOrderByCreatedAtDesc();
}
