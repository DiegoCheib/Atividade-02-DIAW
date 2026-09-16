import { createContext, useCallback, useEffect, useMemo, useState } from "react"
import type { ReactNode } from "react"

import { authApi, refreshSession, tokenStore, type AppUser } from "@/lib/api"

type AuthStatus = "loading" | "authenticated" | "anonymous"

export type AuthContextValue = {
  user: AppUser | null
  status: AuthStatus
  login: (identifier: string, password: string, rememberMe: boolean) => Promise<AppUser>
  logout: () => Promise<void>
  refreshUser: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AppUser | null>(null)
  const [status, setStatus] = useState<AuthStatus>("loading")

  // Ao abrir a aplicacao tentamos restaurar a sessao pelo cookie de refresh.
  useEffect(() => {
    let active = true

    refreshSession()
      .then((session) => {
        if (!active) return
        if (session) {
          setUser(session.user)
          setStatus("authenticated")
        } else {
          setStatus("anonymous")
        }
      })
      .catch(() => {
        if (active) setStatus("anonymous")
      })

    return () => {
      active = false
    }
  }, [])

  const login = useCallback(async (identifier: string, password: string, rememberMe: boolean) => {
    const session = await authApi.login(identifier, password, rememberMe)
    tokenStore.set(session.accessToken)
    setUser(session.user)
    setStatus("authenticated")
    return session.user
  }, [])

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      tokenStore.set(null)
      setUser(null)
      setStatus("anonymous")
    }
  }, [])

  const refreshUser = useCallback(async () => {
    const current = await authApi.me()
    setUser(current)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ user, status, login, logout, refreshUser }),
    [user, status, login, logout, refreshUser],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
