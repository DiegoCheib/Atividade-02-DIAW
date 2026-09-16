package br.pucminas.diaw.sentinela.controller;

import br.pucminas.diaw.sentinela.dto.UserResponse;
import br.pucminas.diaw.sentinela.repository.UserRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Demonstra a autorizacao por perfil: exige a role ADMIN no JWT. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(UserResponse::from)
                .toList();
    }
}
