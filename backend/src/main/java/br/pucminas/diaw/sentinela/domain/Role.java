package br.pucminas.diaw.sentinela.domain;

/** Perfis de acesso suportados pela aplicacao. */
public enum Role {
    USER,
    ADMIN;

    /** Nome da authority no formato esperado pelo Spring Security. */
    public String authority() {
        return "ROLE_" + name();
    }
}
