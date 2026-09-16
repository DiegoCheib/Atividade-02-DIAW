import { Link } from "react-router-dom"

import { BrandWordmark } from "@/components/brand-mark"
import { Button } from "@/components/ui/button"

export default function NotFoundPage() {
  return (
    <main className="flex min-h-svh flex-col items-center justify-center gap-6 p-6 text-center">
      <BrandWordmark />
      <div className="space-y-2">
        <p className="font-heading text-6xl font-semibold tracking-tight">404</p>
        <p className="text-muted-foreground">A pagina que voce procurou nao existe.</p>
      </div>
      <Button render={<Link to="/login" />}>Voltar para o login</Button>
    </main>
  )
}
