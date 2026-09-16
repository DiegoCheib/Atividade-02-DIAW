import { CircleAlert, CircleCheck } from "lucide-react"

import { Alert, AlertDescription } from "@/components/ui/alert"

/** Mensagem de erro ou de sucesso exibida no topo dos formularios. */
export function FormAlert({
  message,
  variant = "error",
}: {
  message: string | null
  variant?: "error" | "success"
}) {
  if (!message) {
    return null
  }

  const isError = variant === "error"
  const Icon = isError ? CircleAlert : CircleCheck

  return (
    <Alert
      role={isError ? "alert" : "status"}
      className={
        isError
          ? "border-destructive/30 bg-destructive/10 text-destructive"
          : "border-emerald-500/30 bg-emerald-500/10 text-emerald-500"
      }
    >
      <Icon className="size-4" aria-hidden />
      <AlertDescription className="text-current">{message}</AlertDescription>
    </Alert>
  )
}
