package br.pucminas.diaw.sentinela.validation;

/** Contrato usado pelo validador de {@link PasswordConfirmation}. */
public interface PasswordConfirmable {

    String password();

    String confirmPassword();
}
