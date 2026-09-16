import type { ReactNode } from "react"

import { BrandMark, BrandWordmark } from "@/components/brand-mark"

/**
 * Estrutura comum das telas publicas: formulario a esquerda, sobre branco, e
 * painel institucional a direita com a logo da PUC Minas. O fundo animado fica
 * contido nesse painel, sem invadir a area de leitura do formulario.
 */
export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <main className="flex min-h-svh items-center justify-center p-4 sm:p-6">
      <div className="w-full max-w-5xl overflow-hidden rounded-xl border border-border bg-card elevated">
        <div className="grid lg:grid-cols-2">
          <section className="flex items-center px-6 py-10 sm:px-10 lg:px-12">
            <div className="mx-auto w-full max-w-sm">
              <BrandWordmark className="mb-10 lg:hidden" />
              {children}
            </div>
          </section>

          <BrandPanel />
        </div>
      </div>
    </main>
  )
}

/** Painel da direita: apenas a logo sobre o azul institucional animado. */
function BrandPanel() {
  return (
    <aside className="relative hidden min-h-[34rem] items-center justify-center overflow-hidden bg-brand-deep lg:flex">
      <div aria-hidden className="pointer-events-none absolute inset-0">
        <div className="animate-aurora absolute -left-24 -top-24 size-96 rounded-full bg-brand blur-[80px]" />
        <div
          className="animate-aurora absolute -right-20 top-1/3 size-80 rounded-full bg-brand-soft/45 blur-[80px]"
          style={{ animationDelay: "-6s" }}
        />
        <div
          className="animate-aurora absolute -bottom-24 left-1/4 size-80 rounded-full bg-brand-mid/60 blur-[90px]"
          style={{ animationDelay: "-11s" }}
        />
      </div>

      <BrandMark variant="light" className="relative h-40 w-auto" />
    </aside>
  )
}

/** Cabecalho reutilizado pelos formularios das telas publicas. */
export function AuthHeader({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <header className="mb-6">
      <h1 className="text-xl font-semibold tracking-tight sm:text-2xl">{title}</h1>
      <p className="mt-1.5 text-sm leading-6 text-muted-foreground">{subtitle}</p>
    </header>
  )
}
