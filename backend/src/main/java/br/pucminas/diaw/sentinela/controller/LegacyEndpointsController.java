package br.pucminas.diaw.sentinela.controller;

import br.pucminas.diaw.sentinela.dto.MessageResponse;
import br.pucminas.diaw.sentinela.dto.RecoverPasswordRequest;
import br.pucminas.diaw.sentinela.dto.RegisterRequest;
import br.pucminas.diaw.sentinela.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Atalhos exigidos pelo enunciado da atividade (POST /register e POST /recoverpassword).
 * Delegam para o AuthController, que concentra a regra de negocio; os endpoints
 * canonicos usados pelo React sao os de /api/auth.
 */
@RestController
public class LegacyEndpointsController {

    private final AuthController authController;

    public LegacyEndpointsController(AuthController authController) {
        this.authController = authController;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authController.register(request);
    }

    @PostMapping("/recoverpassword")
    public MessageResponse recoverPassword(@Valid @RequestBody RecoverPasswordRequest request,
                                           HttpServletRequest httpRequest) {
        return authController.recoverPassword(request, httpRequest);
    }
}
