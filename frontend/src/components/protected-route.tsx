import { Navigate, Outlet, useLocation } from "react-router-dom"

import { useAuth } from "@/hooks/use-auth"

/** Segura a renderizacao enquanto a sessao e restaurada pelo cookie de refresh. */
export function ProtectedRoute() {
  const { status } = useAuth()
  const location = useLocation()

  if (status === "loading") {
    return (
      <div className="flex min-h-svh items-center justify-center">
        <div
          role="status"
          aria-label="Carregando"
          className="size-8 animate-spin rounded-full border-2 border-muted border-t-foreground"
        />
      </div>
    )
  }

  if (status === "anonymous") {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return <Outlet />
}

/** Impede que quem ja esta autenticado volte para login ou cadastro. */
export function GuestRoute() {
  const { status } = useAuth()

  if (status === "loading") {
    return null
  }

  if (status === "authenticated") {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}
