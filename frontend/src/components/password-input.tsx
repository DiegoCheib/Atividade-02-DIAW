import { useState } from "react"
import { Eye, EyeOff } from "lucide-react"

import { cn } from "cn"
import { Input } from "@/components/ui/input"

/** Campo de senha com alternancia entre texto oculto e visivel. */
export function PasswordInput({
  className,
  ...props
}: React.ComponentProps<typeof Input>) {
  const [visible, setVisible] = useState(false)

  return (
    <div className="relative">
      <Input
        {...props}
        type={visible ? "text" : "password"}
        className={cn("pr-12", className)}
      />
      <button
        type="button"
        onClick={() => setVisible((current) => !current)}
        aria-label={visible ? "Ocultar senha" : "Mostrar senha"}
        className="absolute inset-y-0 right-0 flex w-12 items-center justify-center rounded-r-xl text-muted-foreground transition-colors hover:text-foreground focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
      >
        {visible ? <EyeOff className="size-[1.05rem]" aria-hidden /> : <Eye className="size-[1.05rem]" aria-hidden />}
      </button>
    </div>
  )
}
