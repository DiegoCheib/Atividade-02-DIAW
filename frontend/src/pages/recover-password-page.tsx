import { AuthShell } from "@/components/auth-shell"
import { RecoverPasswordForm } from "@/components/recover-password-form"

export default function RecoverPasswordPage() {
  return (
    <AuthShell>
      <RecoverPasswordForm />
    </AuthShell>
  )
}
