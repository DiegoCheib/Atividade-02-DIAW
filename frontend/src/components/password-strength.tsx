import { cn } from "cn"

const RULES = [
  { label: "8 caracteres", test: (value: string) => value.length >= 8 },
  { label: "letra maiuscula", test: (value: string) => /[A-Z]/.test(value) },
  { label: "letra minuscula", test: (value: string) => /[a-z]/.test(value) },
  { label: "numero", test: (value: string) => /\d/.test(value) },
  { label: "simbolo", test: (value: string) => /[^A-Za-z0-9\s]/.test(value) },
]

const LEVELS = [
  { label: "Muito fraca", bar: "bg-red-500", text: "text-red-500" },
  { label: "Fraca", bar: "bg-orange-500", text: "text-orange-500" },
  { label: "Razoavel", bar: "bg-amber-500", text: "text-amber-500" },
  { label: "Boa", bar: "bg-lime-500", text: "text-lime-500" },
  { label: "Forte", bar: "bg-emerald-500", text: "text-emerald-500" },
]

/** Espelha no cliente a mesma politica de senha validada pelo backend. */
export function PasswordStrength({ value }: { value: string }) {
  const passed = RULES.filter((rule) => rule.test(value))
  const score = passed.length
  const level = LEVELS[Math.max(0, score - 1)]
  const missing = RULES.filter((rule) => !rule.test(value))

  if (!value) {
    return null
  }

  return (
    <div className="space-y-1.5 pt-1">
      <div className="flex gap-1.5" aria-hidden>
        {RULES.map((rule, index) => (
          <span
            key={rule.label}
            className={cn(
              "h-1 flex-1 rounded-full transition-colors duration-300",
              index < score ? level.bar : "bg-muted",
            )}
          />
        ))}
      </div>
      <p className="text-xs text-muted-foreground" aria-live="polite">
        <span className={cn("font-medium", level.text)}>{level.label}.</span>{" "}
        {missing.length > 0
          ? `Falta: ${missing.map((rule) => rule.label).join(", ")}.`
          : "A senha atende a todos os requisitos."}
      </p>
    </div>
  )
}
