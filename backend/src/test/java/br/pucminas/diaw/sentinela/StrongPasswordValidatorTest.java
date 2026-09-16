package br.pucminas.diaw.sentinela;

import static org.assertj.core.api.Assertions.assertThat;

import br.pucminas.diaw.sentinela.validation.StrongPasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StrongPasswordValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"Senha@123", "P@ssw0rd!", "Diaw#2026x", "Curta1!A"})
    @DisplayName("aceita senhas que atendem a politica")
    void acceptsStrongPasswords(String password) {
        assertThat(StrongPasswordValidator.isStrong(password)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "senha@123",   // sem letra maiuscula
            "SENHA@123",   // sem letra minuscula
            "Senha@abc",   // sem numero
            "Senha12345",  // sem simbolo
            "Ab1!"         // menos de 8 caracteres
    })
    @DisplayName("rejeita senhas fora da politica")
    void rejectsWeakPasswords(String password) {
        assertThat(StrongPasswordValidator.isStrong(password)).isFalse();
    }

    @Test
    @DisplayName("rejeita nulo e vazio")
    void rejectsNullAndBlank() {
        assertThat(StrongPasswordValidator.isStrong(null)).isFalse();
        assertThat(StrongPasswordValidator.isStrong("")).isFalse();
    }
}
