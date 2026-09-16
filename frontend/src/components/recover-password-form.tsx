import { useState } from "react"
import { Link } from "react-router-dom"
import { ArrowLeft, MailQuestion } from "lucide-react"

import { AuthHeader } from "@/components/auth-shell"
import { FormAlert } from "@/components/form-alert"
import { Button } from "@/components/ui/button"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { ApiError, authApi } from "@/lib/api"

export function RecoverPasswordForm() {
  const [email, setEmail] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [fieldError, setFieldError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setSuccess(null)
    setFieldError(null)

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      setFieldError("Informe um e-mail valido")
      return
    }

    setSubmitting(true)
    try {
      const response = await authApi.recoverPassword(email.trim())
      setSuccess(response.message)
      setEmail("")
    } catch (cause) {
      if (cause instanceof ApiError) {
        setError(cause.message)
        setFieldError(cause.fieldError("email") ?? null)
      } else {
        setError("Nao foi possivel falar com o servidor. Verifique se a API esta no ar.")
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <AuthHeader
        title="Recuperar senha"
        subtitle="Informe o e-mail cadastrado para solicitar a redefinicao."
      />

      <FieldGroup className="stagger">
        <FormAlert message={error} />
        <FormAlert message={success} variant="success" />

        <Field data-invalid={fieldError ? true : undefined}>
          <FieldLabel htmlFor="email">E-mail cadastrado</FieldLabel>
          <Input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            placeholder="voce@pucminas.br"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
            autoFocus
          />
          <FieldError>{fieldError}</FieldError>
          <FieldDescription>
            Por seguranca a resposta e sempre a mesma, exista ou nao uma conta com este e-mail.
          </FieldDescription>
        </Field>

        <Field>
          <Button type="submit" size="xl" disabled={submitting} className="w-full">
            <MailQuestion className="size-[1.05rem]" aria-hidden />
            {submitting ? "Enviando..." : "Solicitar redefinicao"}
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
