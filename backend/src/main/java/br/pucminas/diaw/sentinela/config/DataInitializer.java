package br.pucminas.diaw.sentinela.config;

import br.pucminas.diaw.sentinela.domain.Role;
import br.pucminas.diaw.sentinela.domain.User;
import br.pucminas.diaw.sentinela.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Cria as contas de demonstracao caso ainda nao existam. */
@Component
@ConditionalOnProperty(prefix = "app.demo-user", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoUserProperties properties;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           DemoUserProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(String... args) {
        createIfAbsent(properties.name(), properties.username(), properties.email(),
                properties.password(), Role.USER);
        createIfAbsent(properties.adminName(), properties.adminUsername(), properties.adminEmail(),
                properties.adminPassword(), Role.ADMIN);
    }

    private void createIfAbsent(String name, String username, String email, String rawPassword, Role role) {
        if (userRepository.existsByEmailIgnoreCase(email) || userRepository.existsByUsernameIgnoreCase(username)) {
            return;
        }
        userRepository.save(new User(name, username, email, passwordEncoder.encode(rawPassword), role));
        log.info("Conta de demonstracao criada: {} / {} (perfil {})", username, email, role);
    }
}
