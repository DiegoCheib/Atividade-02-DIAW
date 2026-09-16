import { cn } from "cn"

/**
 * Marca da aplicacao.
 * `variant="tile"` usa o icone azul (bom sobre fundos claros) e
 * `variant="light"` usa a versao branca da logo, para o painel institucional.
 */
export function BrandMark({
  className,
  variant = "tile",
}: {
  className?: string
  variant?: "tile" | "light"
}) {
  const source = variant === "tile" ? "/android-chrome-192x192.png" : "/images/pucminas-logo.png"

  return (
    <img
      src={source}
      alt="PUC Minas"
      className={cn(
        "shrink-0 object-contain",
        variant === "tile" ? "size-10 rounded-xl shadow-sm" : "h-16 w-auto",
        className,
      )}
    />
  )
}

export function BrandWordmark({
  className,
  tone = "default",
}: {
  className?: string
  tone?: "default" | "light"
}) {
  return (
    <span className={cn("flex items-center gap-3 font-heading", className)}>
      <BrandMark variant={tone === "light" ? "light" : "tile"} className={tone === "light" ? "h-10" : undefined} />
      <span className="flex flex-col leading-none">
        <span className="text-lg font-semibold tracking-tight">Sentinela</span>
        <span
          className={cn(
            "text-[0.68rem] font-medium uppercase tracking-[0.18em]",
            tone === "light" ? "text-white/70" : "text-muted-foreground",
          )}
        >
          PUC Minas &middot; DIAW
        </span>
      </span>
    </span>
  )
}
