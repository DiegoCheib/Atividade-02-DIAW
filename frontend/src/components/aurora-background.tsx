/**
 * Plano de fundo das telas: gradientes animados no azul institucional da
 * PUC Minas. Puramente decorativo, por isso fica fora da arvore de acessibilidade.
 */
export function AuroraBackground() {
  return (
    <div aria-hidden className="pointer-events-none fixed inset-0 -z-10 overflow-hidden bg-background">
      <div className="animate-aurora absolute -left-40 -top-40 size-[38rem] rounded-full bg-brand/40 blur-[120px]" />
      <div
        className="animate-aurora absolute -right-32 top-1/4 size-[32rem] rounded-full bg-brand-soft/20 blur-[120px]"
        style={{ animationDelay: "-6s" }}
      />
      <div
        className="animate-aurora absolute bottom-[-12rem] left-1/3 size-[30rem] rounded-full bg-brand-mid/25 blur-[130px]"
        style={{ animationDelay: "-11s" }}
      />

      <div className="absolute inset-0 bg-gradient-to-b from-background/10 via-transparent to-background/70" />
    </div>
  )
}
