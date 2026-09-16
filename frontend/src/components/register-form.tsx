import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { ArrowLeft, UserPlus } from "lucide-react"

import { AuthHeader } from "@/components/auth-shell"
import { FormAlert } from "@/components/form-alert"
import { PasswordInput } from "@/components/password-input"
import { PasswordStrength } from "@/components/password-strength"
import { Button } from "@/components/ui/button"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { ApiError, authApi } from "@/lib/api"

type FormState = {
  name: string
  username: string
  email: string
  password: string
  confirmPassword: string
}

const EMPTY_FORM: FormState = {
  name: "",
  username: "",
  email: "",
  password: "",
  confirmPassword: "",
}

/** Validacoes de primeira linha no cliente; o backend valida tudo de novo. */
function validate(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {}

  if (form.name.trim().length < 3) {
    errors.name = "Informe seu nome completo (minimo 3 caracteres)"
  }
  if (!/^[a-zA-Z0-9._-]{3,40}$/.test(form.username.trim())) {
    errors.username = "Use de 3 a 40 caracteres: letras, numeros, ponto, hifen ou underline"
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = "Informe um e-mail valido"
  }
  if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s]).{8,}$/.test(form.password)) {
    errors.password = "A senha nao atende aos requisitos de seguranca"
  }
  if (form.confirmPassword !== form.password) {
    errors.confirmPassword = "A confirmacao de senha nao confere"
  }

  return errors
}

export function RegisterForm() {
  const navigate = useNavigate()
  const [form, setForm] = useState<FormState>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  function update(field: keyof FormState) {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      setForm((current) => ({ ...current, [field]: event.target.value }))
      setFieldErrors(({ [field]: _removed, ...rest }) => rest)
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    const errors = validate(form)
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      setError("Revise os campos destacados antes de continuar.")
      return
    }

    setFieldErrors({})
    setSubmitting(true)
    try {
      await authApi.register({
        name: form.name.trim(),
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        confirmPassword: form.confirmPassword,
      })
      navigate("/login", {
        replace: true,
        state: { notice: "Cadastro concluido. Faca login para acessar sua conta." },
      })
    } catch (cause) {
      if (cause instanceof ApiError) {
        setError(cause.message)
        setFieldErrors(Object.fromEntries(cause.fieldErrors.map((issue) => [issue.field, issue.message])))
      } else {
        setError("Nao foi possivel falar com o servidor. Verifique se a API esta no ar.")
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <AuthHeader title="Criar conta" subtitle="Preencha os dados abaixo para criar seu acesso." />

      <FieldGroup>
        <FormAlert message={error} />

        <Field data-invalid={fieldErrors.name ? true : undefined}>
          <FieldLabel htmlFor="name">Nome completo</FieldLabel>
          <Input
            id="name"
            name="name"
            autoComplete="name"
            placeholder="Maria Silva"
            value={form.name}
            onChange={update("name")}
            required
            autoFocus
          />
          <FieldError>{fieldErrors.name}</FieldError>
        </Field>

        <div className="grid gap-4 sm:grid-cols-2">
          <Field data-invalid={fieldErrors.username ? true : undefined}>
            <FieldLabel htmlFor="username">Usuario</FieldLabel>
            <Input
              id="username"
              name="username"
              autoComplete="username"
              placeholder="maria.silva"
              value={form.username}
              onChange={update("username")}
              required
            />
            <FieldError>{fieldErrors.username}</FieldError>
          </Field>

          <Field data-invalid={fieldErrors.email ? true : undefined}>
            <FieldLabel htmlFor="email">E-mail</FieldLabel>
            <Input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              placeholder="voce@pucminas.br"
              value={form.email}
              onChange={update("email")}
              required
            />
            <FieldError>{fieldErrors.email}</FieldError>
          </Field>
        </div>

        <Field data-invalid={fieldErrors.password ? true : undefined}>
          <FieldLabel htmlFor="password">Senha</FieldLabel>
          <PasswordInput
            id="password"
            name="password"
            autoComplete="new-password"
            placeholder="Crie uma senha forte"
            value={form.password}
            onChange={update("password")}
            required
          />
          <PasswordStrength value={form.password} />
          <FieldError>{fieldErrors.password}</FieldError>
        </Field>

        <Field data-invalid={fieldErrors.confirmPassword ? true : undefined}>
          <FieldLabel htmlFor="confirmPassword">Confirmar senha</FieldLabel>
          <PasswordInput
            id="confirmPassword"
            name="confirmPassword"
            autoComplete="new-password"
            placeholder="Repita a senha"
            value={form.confirmPassword}
            onChange={update("confirmPassword")}
            required
          />
          <FieldError>{fieldErrors.confirmPassword}</FieldError>
        </Field>

        <Field>
          <Button type="submit" size="xl" disabled={submitting} className="w-full">
            <UserPlus className="size-[1.05rem]" aria-hidden />
            {submitting ? "Cadastrando..." : "Criar conta"}
          </Button>
        </Field>

        <FieldDescription className="text-center">
          <Link
            to="/login"
            className="inline-flex items-center gap-1.5 font-medium text-foreground underline underline-offset-4"
          >
            <ArrowLeft className="size-3.5" aria-hidden />
            Voltar para o login
          </Link>
        </FieldDescription>
      </FieldGroup>
    </form>
  )
}
