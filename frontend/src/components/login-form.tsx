import { useState } from "react"
import { Link, useLocation, useNavigate } from "react-router-dom"
import { LogIn } from "lucide-react"

import { AuthHeader } from "@/components/auth-shell"
import { FormAlert } from "@/components/form-alert"
import { PasswordInput } from "@/components/password-input"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { useAuth } from "@/hooks/use-auth"
import { ApiError } from "@/lib/api"

export function LoginForm() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [identifier, setIdentifier] = useState("")
  const [password, setPassword] = useState("")
  const [rememberMe, setRememberMe] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [submitting, setSubmitting] = useState(false)

  const routeState = location.state as { from?: string; notice?: string } | null
  // Volta para a pagina que exigiu autenticacao, quando houver.
  const redirectTo = routeState?.from ?? "/dashboard"
  const [notice, setNotice] = useState<string | null>(routeState?.notice ?? null)

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    setFieldErrors({})
    setSubmitting(true)

    try {
      await login(identifier.trim(), password, rememberMe)
      navigate(redirectTo, { replace: true })
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
      <AuthHeader title="Bem-vindo de volta" subtitle="Entre com suas credenciais para continuar." />

      <FieldGroup>
        <FormAlert message={notice} variant="success" />
        <FormAlert message={error} />

        <Field data-invalid={fieldErrors.identifier ? true : undefined}>
          <FieldLabel htmlFor="identifier">Usuario ou e-mail</FieldLabel>
          <Input
            id="identifier"
            name="identifier"
            autoComplete="username"
            placeholder="seu.usuario ou voce@pucminas.br"
            value={identifier}
            onChange={(event) => setIdentifier(event.target.value)}
            required
            autoFocus
          />
          <FieldError>{fieldErrors.identifier}</FieldError>
        </Field>

        <Field data-invalid={fieldErrors.password ? true : undefined}>
          <div className="flex items-center justify-between gap-3">
            <FieldLabel htmlFor="password">Senha</FieldLabel>
            <Link
              to="/recoverpassword"
              className="text-sm text-muted-foreground underline-offset-4 transition-colors hover:text-foreground hover:underline"
            >
              Esqueci minha senha
            </Link>
          </div>
          <PasswordInput
            id="password"
            name="password"
            autoComplete="current-password"
            placeholder="Sua senha"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
          <FieldError>{fieldErrors.password}</FieldError>
        </Field>

        <Field orientation="horizontal">
          <Checkbox
            id="rememberMe"
            checked={rememberMe}
            onCheckedChange={(checked) => setRememberMe(checked === true)}
          />
          <FieldLabel htmlFor="rememberMe" className="font-normal text-muted-foreground">
            Manter conectado por 30 dias
          </FieldLabel>
        </Field>

        <Field>
          <Button type="submit" size="xl" disabled={submitting} className="w-full">
            <LogIn className="size-[1.05rem]" aria-hidden />
            {submitting ? "Entrando..." : "Entrar"}
          </Button>
        </Field>

        <FieldDescription className="text-center">
          Ainda nao tem conta?{" "}
          <Link to="/register" className="font-medium text-foreground underline underline-offset-4">
            Criar cadastro
          </Link>
        </FieldDescription>
      </FieldGroup>
    </form>
  )
}
