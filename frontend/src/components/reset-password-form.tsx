import { useEffect, useState } from "react"
import { Link, useNavigate, useSearchParams } from "react-router-dom"
import { ArrowLeft, KeyRound, RefreshCw } from "lucide-react"

import { AuthHeader } from "@/components/auth-shell"
import { FormAlert } from "@/components/form-alert"
import { PasswordInput } from "@/components/password-input"
import { PasswordStrength } from "@/components/password-strength"
import { Button } from "@/components/ui/button"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { ApiError, authApi } from "@/lib/api"

type TokenState =
  | { status: "checking" }
  | { status: "valid"; email: string }
  | { status: "invalid"; message: string }

const STRONG_PASSWORD = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s]).{8,}$/
const INVALID_LINK_MESSAGE = "Este link de redefinicao e invalido. Solicite um novo."

export function ResetPasswordForm() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get("token")?.trim() ?? ""

  const [tokenState, setTokenState] = useState<TokenState>(() =>
    token ? { status: "checking" } : { status: "invalid", message: INVALID_LINK_MESSAGE },
  )
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!token) {
      return
    }

    const controller = new AbortController()

    authApi
      .checkResetToken(token, controller.signal)
      .then(({ email }) => setTokenState({ status: "valid", email }))
      .catch((cause: unknown) => {
        if (controller.signal.aborted) return
        setTokenState({
          status: "invalid",
          message:
            cause instanceof ApiError
              ? cause.message
              : "Nao foi possivel validar o link. Verifique sua conexao e tente novamente.",
        })
      })

    return () => controller.abort()
  }, [token])

  function updatePassword(event: React.ChangeEvent<HTMLInputElement>) {
    setPassword(event.target.value)
    setFieldErrors(({ password: _removed, ...rest }) => rest)
  }

  function updateConfirmation(event: React.ChangeEvent<HTMLInputElement>) {
    setConfirmPassword(event.target.value)
    setFieldErrors(({ confirmPassword: _removed, ...rest }) => rest)
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    const errors: Record<string, string> = {}
    if (!STRONG_PASSWORD.test(password)) {
      errors.password = "A senha nao atende aos requisitos de seguranca"
    }
    if (confirmPassword !== password) {
      errors.confirmPassword = "A confirmacao de senha nao confere"
    }
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors)
      setError("Revise os campos destacados antes de continuar.")
      return
    }

    setFieldErrors({})
    setSubmitting(true)
    try {
      const response = await authApi.resetPassword(token, password, confirmPassword)
      navigate("/login", { replace: true, state: { notice: response.message } })
    } catch (cause) {
      if (cause instanceof ApiError) {
        setError(cause.message)
        setFieldErrors(Object.fromEntries(cause.fieldErrors.map((issue) => [issue.field, issue.message])))

        if (cause.fieldErrors.length === 0) {
          setTokenState({ status: "invalid", message: cause.message })
        }
      } else {
        setError("Nao foi possivel falar com o servidor. Verifique se a API esta no ar.")
      }
    } finally {
      setSubmitting(false)
    }
  }

  if (tokenState.status === "checking") {
    return (
      <div role="status" className="py-12 text-center" aria-live="polite">
        <RefreshCw className="mx-auto mb-4 size-7 animate-spin text-primary" aria-hidden />
        <p className="text-sm text-muted-foreground">Validando seu link...</p>
      </div>
    )
  }

  if (tokenState.status === "invalid") {
    return (
      <div>
        <AuthHeader title="Link indisponivel" subtitle="Nao foi possivel abrir a redefinicao de senha." />
        <FieldGroup>
          <FormAlert message={tokenState.message} />
          <Button render={<Link to="/recoverpassword" />} size="xl" className="w-full">
            <KeyRound className="size-[1.05rem]" aria-hidden />
            Solicitar novo link
          </Button>
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
      </div>
    )
  }

  return (
    <form onSubmit={handleSubmit} noValidate>
      <AuthHeader
        title="Definir nova senha"
        subtitle={`Escolha uma nova senha para ${tokenState.email}.`}
      />

      <FieldGroup className="stagger">
        <FormAlert message={error} />

        <Field data-invalid={fieldErrors.password ? true : undefined}>
          <FieldLabel htmlFor="password">Nova senha</FieldLabel>
          <PasswordInput
            id="password"
            name="password"
            autoComplete="new-password"
            placeholder="Crie uma senha forte"
            value={password}
            onChange={updatePassword}
            required
            autoFocus
          />
          <PasswordStrength value={password} />
          <FieldError>{fieldErrors.password}</FieldError>
        </Field>

        <Field data-invalid={fieldErrors.confirmPassword ? true : undefined}>
          <FieldLabel htmlFor="confirmPassword">Confirmar nova senha</FieldLabel>
          <PasswordInput
            id="confirmPassword"
            name="confirmPassword"
            autoComplete="new-password"
            placeholder="Repita a nova senha"
            value={confirmPassword}
            onChange={updateConfirmation}
            required
          />
          <FieldError>{fieldErrors.confirmPassword}</FieldError>
        </Field>

        <Field>
          <Button type="submit" size="xl" disabled={submitting} className="w-full">
            <KeyRound className="size-[1.05rem]" aria-hidden />
            {submitting ? "Redefinindo..." : "Redefinir senha"}
          </Button>
        </Field>
      </FieldGroup>
    </form>
  )
}
