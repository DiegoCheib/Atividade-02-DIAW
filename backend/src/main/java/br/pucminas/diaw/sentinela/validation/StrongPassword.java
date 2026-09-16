package br.pucminas.diaw.sentinela.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Politica de senha da aplicacao. */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {

    String message() default "A senha deve ter no minimo 8 caracteres, com letra maiuscula, minuscula, numero e simbolo";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
