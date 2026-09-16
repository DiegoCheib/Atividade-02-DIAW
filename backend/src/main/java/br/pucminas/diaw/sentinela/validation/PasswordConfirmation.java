package br.pucminas.diaw.sentinela.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Garante que os campos senha e confirmacao de senha sejam iguais. */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordConfirmationValidator.class)
public @interface PasswordConfirmation {

    String message() default "A confirmacao de senha nao confere";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
