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
import { Separator } from "@/components/ui/separator"
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

  return (
    <div className="mx-auto flex min-h-svh w-full max-w-5xl flex-col gap-8 p-4 sm:p-6 lg:p-10">
      <header className="flex flex-wrap items-center justify-between gap-4">
        <BrandWordmark />
        <div className="flex items-center gap-3">
          <div className="hidden text-right sm:block">
            <p className="text-sm font-medium leading-tight">{user.name}</p>
            <p className="text-xs text-muted-foreground">{user.email}</p>
          </div>
          <Avatar>
            <AvatarFallback className="bg-primary text-primary-foreground">
              {initials(user.name)}
            </AvatarFallback>
          </Avatar>
          <Button variant="outline" onClick={() => void logout()}>
            <LogOut className="size-4" aria-hidden />
            Sair
          </Button>
        </div>
      </header>

      <FormAlert message={error} />

      <Card className="overflow-hidden">
        <CardContent className="flex flex-wrap items-center justify-between gap-4">
          <div className="space-y-1">
            <p className="text-sm text-muted-foreground">Area restrita</p>
            <h1 className="font-heading text-2xl font-semibold tracking-tight">
              Ola, {user.name.split(" ")[0]}!
            </h1>
            <p className="text-sm text-muted-foreground">
              Esta pagina so carrega com um access token valido no cabecalho Authorization.
            </p>
          </div>
          <Badge variant={user.role === "ADMIN" ? "default" : "secondary"} className="gap-1.5">
            <ShieldCheck className="size-3.5" aria-hidden />
            {user.role === "ADMIN" ? "Administrador" : "Usuario"}
          </Badge>
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard icon={Users} label="Contas cadastradas" value={summary ? String(summary.totalUsers) : "..."} />
        <StatCard
          icon={Activity}
          label="Sessoes ativas"
          value={summary ? String(summary.activeSessions) : "..."}
        />
        <StatCard icon={CalendarClock} label="Ultimo acesso" value={formatDate(user.lastLoginAt)} />
        <StatCard icon={KeyRound} label="Conta criada em" value={formatDate(user.createdAt)} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Dados da conta</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2">
          <Info label="Nome" value={user.name} />
          <Info label="Usuario" value={user.username} />
          <Info label="E-mail" value={user.email} />
          <Info label="Identificador" value={user.id} mono />
        </CardContent>
      </Card>

      {user.role === "ADMIN" && (
        <Card>
          <CardHeader>
            <CardTitle>Usuarios cadastrados</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            <ul className="divide-y divide-border">
              {(admins ?? []).map((item) => (
                <li key={item.id} className="flex flex-wrap items-center justify-between gap-2 px-6 py-3">
                  <div>
                    <p className="text-sm font-medium">{item.name}</p>
                    <p className="text-xs text-muted-foreground">
                      {item.username} &middot; {item.email}
                    </p>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-xs text-muted-foreground">{formatDate(item.createdAt)}</span>
                    <Badge variant={item.role === "ADMIN" ? "default" : "secondary"}>{item.role}</Badge>
                  </div>
                </li>
              ))}
              {admins?.length === 0 && (
                <li className="px-6 py-4 text-sm text-muted-foreground">Nenhum usuario cadastrado.</li>
              )}
            </ul>
          </CardContent>
        </Card>
      )}

      <footer className="mt-auto space-y-3 pt-4 text-center text-xs text-muted-foreground">
        <Separator />
        <p>
          Sentinela &middot; Atividade 02 &middot; Desenvolvimento de Interfaces e Aplicacoes Web &middot; PUC
          Minas
        </p>
      </footer>
    </div>
  )
}

function StatCard({
  icon: Icon,
  label,
  value,
}: {
  icon: typeof Users
  label: string
  value: string
}) {
  return (
    <Card>
      <CardContent className="space-y-2">
        <span className="flex size-9 items-center justify-center rounded-md border border-border text-muted-foreground">
          <Icon className="size-4" aria-hidden />
        </span>
        <p className="text-xs uppercase tracking-wide text-muted-foreground">{label}</p>
        <p className="truncate font-heading text-lg font-semibold">{value}</p>
      </CardContent>
    </Card>
  )
}

function Info({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="space-y-1">
      <p className="text-xs uppercase tracking-wide text-muted-foreground">{label}</p>
      <p className={mono ? "break-all font-mono text-sm" : "text-sm"}>{value}</p>
    </div>
  )
}
