import { useEffect, useState } from "react"
import {
  Activity,
  CalendarClock,
  KeyRound,
  LogOut,
  ShieldCheck,
  Users,
} from "lucide-react"

import { BrandWordmark } from "@/components/brand-mark"
import { FormAlert } from "@/components/form-alert"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { useAuth } from "@/hooks/use-auth"
import { ApiError, authApi, request, type AppUser, type DashboardSummary } from "@/lib/api"

const dateTime = new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" })

function formatDate(value: string | null | undefined) {
  return value ? dateTime.format(new Date(value)) : "-"
}

function initials(name: string) {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("")
}

export default function DashboardPage() {
  const { user, logout } = useAuth()
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [admins, setAdmins] = useState<AppUser[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    authApi
      .dashboard()
      .then(setSummary)
      .catch((cause) =>
        setError(cause instanceof ApiError ? cause.message : "Nao foi possivel carregar o painel."),
      )
  }, [])

  // A listagem completa so responde para quem tem o perfil ADMIN no token.
  useEffect(() => {
    if (user?.role !== "ADMIN") {
      return
    }
    request<AppUser[]>("/api/admin/users")
      .then(setAdmins)
      .catch(() => setAdmins(null))
  }, [user?.role])

  if (!user) {
    return null
  }

  const isAdmin = user.role === "ADMIN"

  return (
    <div className="flex min-h-svh flex-col">
      <header className="sticky top-0 z-10 border-b border-border bg-background">
        <div className="mx-auto flex h-16 w-full max-w-5xl items-center justify-between gap-4 px-4 sm:px-6">
          <BrandWordmark />

          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium leading-tight">{user.name}</p>
              <p className="text-xs text-muted-foreground">{user.email}</p>
            </div>
            <Avatar>
              <AvatarFallback className="bg-primary text-xs font-medium text-primary-foreground">
                {initials(user.name)}
              </AvatarFallback>
            </Avatar>
            <Button variant="outline" onClick={() => void logout()}>
              <LogOut className="size-4" aria-hidden />
              {/* Em telas estreitas o rotulo some, mas segue anunciado por leitores de tela. */}
              <span className="sr-only sm:not-sr-only">Sair</span>
            </Button>
          </div>
        </div>
      </header>

      <main className="mx-auto w-full max-w-5xl flex-1 px-4 py-8 sm:px-6 lg:py-10">
        <FormAlert message={error} />

        <div className="mb-8 flex flex-wrap items-start justify-between gap-x-4 gap-y-3">
          <div>
            <h1 className="font-heading text-2xl font-semibold tracking-tight sm:text-3xl">
              Ola, {user.name.split(" ")[0]}
            </h1>
            <p className="mt-1.5 max-w-xl text-sm leading-6 text-muted-foreground">
              Esta pagina so carrega com um access token valido no cabecalho Authorization.
            </p>
          </div>
          <Badge variant={isAdmin ? "default" : "secondary"} className="gap-1.5">
            <ShieldCheck className="size-3.5" aria-hidden />
            {isAdmin ? "Administrador" : "Usuario"}
          </Badge>
        </div>

        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard icon={Users} label="Contas cadastradas" value={summary?.totalUsers} />
          <StatCard icon={Activity} label="Sessoes ativas" value={summary?.activeSessions} />
          <StatCard icon={CalendarClock} label="Ultimo acesso" value={formatDate(user.lastLoginAt)} />
          <StatCard icon={KeyRound} label="Conta criada em" value={formatDate(user.createdAt)} />
        </section>

        <Card className="elevated-sm mt-6">
          <CardHeader className="border-b pb-4">
            <CardTitle>Dados da conta</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="grid gap-x-8 gap-y-5 sm:grid-cols-2">
              <Info label="Nome" value={user.name} />
              <Info label="Usuario" value={user.username} />
              <Info label="E-mail" value={user.email} />
              <Info label="Identificador" value={user.id} mono />
            </dl>
          </CardContent>
        </Card>

        {isAdmin && (
          <Card className="elevated-sm mt-6">
            <CardHeader className="border-b pb-4">
              <CardTitle>Usuarios cadastrados</CardTitle>
            </CardHeader>
            <CardContent className="px-0">
              <ul className="divide-y divide-border">
                {(admins ?? []).map((item) => (
                  <li
                    key={item.id}
                    className="flex items-center gap-3 px-4 py-3 transition-colors hover:bg-muted/60 sm:px-5"
                  >
                    <Avatar size="sm">
                      <AvatarFallback className="text-[0.65rem] font-medium">
                        {initials(item.name)}
                      </AvatarFallback>
                    </Avatar>

                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-medium">{item.name}</p>
                      <p className="truncate text-xs text-muted-foreground">
                        {item.username} &middot; {item.email}
                      </p>
                    </div>

                    <span className="hidden shrink-0 text-xs text-muted-foreground md:block">
                      {formatDate(item.createdAt)}
                    </span>
                    <Badge variant={item.role === "ADMIN" ? "default" : "outline"} className="shrink-0">
                      {item.role}
                    </Badge>
                  </li>
                ))}
                {admins?.length === 0 && (
                  <li className="px-5 py-4 text-sm text-muted-foreground">Nenhum usuario cadastrado.</li>
                )}
              </ul>
            </CardContent>
          </Card>
        )}
      </main>

      <footer className="border-t border-border">
        <p className="mx-auto w-full max-w-5xl px-4 py-6 text-center text-xs text-muted-foreground sm:px-6">
          Sentinela &middot; Atividade 02 &middot; Desenvolvimento de Interfaces e Aplicacoes Web &middot; PUC
          Minas
        </p>
      </footer>
    </div>
  )
}

/**
 * Indicador do topo do painel. Contagens aparecem em corpo maior que datas,
 * que sao longas o bastante para estourar a largura do cartao nesse tamanho.
 */
function StatCard({
  icon: Icon,
  label,
  value,
}: {
  icon: typeof Users
  label: string
  value: number | string | undefined
}) {
  const carregando = value === undefined
  const contagem = typeof value === "number"

  return (
    <Card className="elevated-sm">
      <CardContent>
        <div className="flex items-start justify-between gap-3">
          <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</p>
          <Icon className="size-4 shrink-0 text-primary" aria-hidden />
        </div>
        <p
          className={
            carregando
              ? "mt-3 text-2xl text-muted-foreground"
              : contagem
                ? "mt-3 font-heading text-3xl font-semibold tracking-tight tabular-nums"
                : "mt-3 truncate font-heading text-base font-semibold tabular-nums"
          }
        >
          {carregando ? "--" : value}
        </p>
      </CardContent>
    </Card>
  )
}

function Info({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div>
      <dt className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</dt>
      <dd className={mono ? "mt-1 break-all font-mono text-sm" : "mt-1 text-sm"}>{value}</dd>
    </div>
  )
}
