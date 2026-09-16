import type { ReactNode } from "react"

import { BrandWordmark } from "@/components/brand-mark"
import { Card, CardContent } from "@/components/ui/card"

/**
 * Estrutura comum das telas publicas: contexto e identidade visual a esquerda,
 * com o formulario em uma area clara e focada a direita.
 */
export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <main className="auth-page relative isolate flex min-h-svh w-full items-center justify-center overflow-hidden p-3 sm:p-6 lg:p-8">
      <div aria-hidden className="auth-page-grid absolute inset-0 -z-10" />

      <div className="w-full max-w-6xl">
        <Card className="overflow-hidden rounded-[1.75rem] border-white/70 bg-white/85 p-0 shadow-[0_24px_80px_-32px_rgba(0,51,79,0.38)] ring-1 ring-brand-deep/8 backdrop-blur-xl sm:rounded-[2rem]">
          <CardContent className="grid min-h-[min(720px,calc(100svh-3rem))] p-0 lg:grid-cols-[1.04fr_0.96fr]">
            <BrandPanel />
            <section className="auth-form-panel relative z-10 flex items-center bg-white px-6 py-10 shadow-[0_0_42px_rgba(0,40,61,0.22)] sm:px-10 lg:px-12 xl:px-16">
              <div className="mx-auto w-full max-w-[28rem]">
                {children}

                <p className="mt-8 text-center text-xs text-muted-foreground">
                  Acesso institucional &middot; PUC Minas
                </p>
              </div>
            </section>
          </CardContent>
        </Card>
      </div>
    </main>
  )
}

/** Painel institucional com a identidade e a mensagem principal do produto. */
function BrandPanel() {
  return (
    <aside className="auth-brand-panel relative hidden min-h-[42rem] overflow-hidden bg-brand-deep text-white lg:flex lg:flex-col">
      <div aria-hidden className="absolute inset-0 bg-gradient-to-br from-brand-deep via-brand-deep to-brand" />
      <div aria-hidden className="auth-brand-grid absolute inset-0 opacity-20" />

      <div className="relative flex h-full flex-1 flex-col p-10 xl:p-14">
        <BrandWordmark tone="light" className="text-white" />

        <div className="my-auto max-w-md py-12">
          <h2 className="font-heading text-4xl font-semibold leading-[1.08] tracking-[-0.035em] xl:text-[2.8rem]">
            Segurança para acessar. Simplicidade para continuar.
          </h2>
          <p className="mt-5 max-w-sm text-[0.95rem] leading-6 text-white/65">
            Uma experiência de acesso confiável para manter você conectado ao que importa.
          </p>
        </div>
      </div>
    </aside>
  )
}

/** Cabecalho reutilizado pelos formularios das telas publicas. */
export function AuthHeader({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <header className="mb-8">
      <BrandWordmark className="mb-10 lg:hidden" />
      <div className="mb-3 hidden items-center gap-2 text-xs font-semibold uppercase tracking-[0.16em] text-primary lg:flex">
        <span className="h-px w-6 bg-primary/50" />
        Área do usuário
      </div>
      <h1 className="font-heading text-2xl font-semibold tracking-[-0.025em] sm:text-3xl">{title}</h1>
      <p className="mt-2 text-sm leading-6 text-muted-foreground">{subtitle}</p>
    </header>
  )
}
