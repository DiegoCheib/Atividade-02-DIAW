/**
 * Cliente HTTP da aplicacao.
 *
 * O access token (JWT) vive apenas em memoria: nada de localStorage, para
 * reduzir a superficie de ataque de XSS. A sessao longa fica no cookie
 * HttpOnly do refresh token, que o navegador envia sozinho para /api/auth.
 */

const API_BASE_URL = import.meta.env.VITE_API_URL ?? ""

export type FieldIssue = {
  field: string
  message: string
}

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: FieldIssue[]

  constructor(status: number, message: string, fieldErrors: FieldIssue[] = []) {
    super(message)
    this.name = "ApiError"
    this.status = status
    this.fieldErrors = fieldErrors
  }

  /** Mensagem associada a um campo especifico do formulario. */
  fieldError(field: string): string | undefined {
    return this.fieldErrors.find((issue) => issue.field === field)?.message
  }
}

let accessToken: string | null = null

export const tokenStore = {
  get: () => accessToken,
  set: (token: string | null) => {
    accessToken = token
  },
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE"
  body?: unknown
  /** Evita loop infinito de renovacao de token. */
  skipRefresh?: boolean
  signal?: AbortSignal
}

async function parseError(response: Response, fallback: string): Promise<ApiError> {
  try {
    const data = await response.json()
    return new ApiError(
      response.status,
      typeof data?.message === "string" ? data.message : fallback,
      Array.isArray(data?.fieldErrors) ? data.fieldErrors : [],
    )
  } catch {
    return new ApiError(response.status, fallback)
  }
}

async function send(path: string, options: RequestOptions): Promise<Response> {
  const headers: Record<string, string> = { Accept: "application/json" }
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json"
  }
  if (accessToken) {
    headers.Authorization = `Bearer ${accessToken}`
  }

  return fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers,
    // Necessario para o cookie HttpOnly do refresh token
    credentials: "include",
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    signal: options.signal,
  })
}

let refreshInFlight: Promise<boolean> | null = null

/** Renova o access token usando o cookie de refresh. Uma chamada por vez. */
export async function refreshSession(): Promise<AuthSession | null> {
  const response = await send("/api/auth/refresh", { method: "POST", skipRefresh: true })
  if (!response.ok) {
    accessToken = null
    return null
  }
  const session = (await response.json()) as AuthSession
  accessToken = session.accessToken
  return session
}

async function tryRefresh(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = refreshSession()
      .then((session) => session !== null)
      .catch(() => false)
      .finally(() => {
        refreshInFlight = null
      })
  }
  return refreshInFlight
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  let response = await send(path, options)

  // Token expirado: tenta renovar uma unica vez e repete a requisicao original.
  if (response.status === 401 && !options.skipRefresh) {
    const renewed = await tryRefresh()
    if (renewed) {
      response = await send(path, { ...options, skipRefresh: true })
    }
  }

  if (!response.ok) {
    throw await parseError(response, "Nao foi possivel concluir a operacao")
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export type AppUser = {
  id: string
  name: string
  username: string
  email: string
  role: "USER" | "ADMIN"
  enabled: boolean
  createdAt: string
  lastLoginAt: string | null
}

export type AuthSession = {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: AppUser
}

export type DashboardSummary = {
  user: AppUser
  totalUsers: number
  activeSessions: number
  serverTime: string
}

export const authApi = {
  login: (identifier: string, password: string, rememberMe: boolean) =>
    request<AuthSession>("/api/auth/login", {
      method: "POST",
      body: { identifier, password, rememberMe },
      skipRefresh: true,
    }),

  register: (payload: {
    name: string
    username: string
    email: string
    password: string
    confirmPassword: string
  }) => request<AppUser>("/api/auth/register", { method: "POST", body: payload, skipRefresh: true }),

  recoverPassword: (email: string) =>
    request<{ message: string }>("/api/auth/recoverpassword", {
      method: "POST",
      body: { email },
      skipRefresh: true,
    }),

  checkResetToken: (token: string, signal?: AbortSignal) =>
    request<{ email: string }>(`/api/auth/resetpassword?token=${encodeURIComponent(token)}`, {
      skipRefresh: true,
      signal,
    }),

  resetPassword: (token: string, password: string, confirmPassword: string) =>
    request<{ message: string }>("/api/auth/resetpassword", {
      method: "POST",
      body: { token, password, confirmPassword },
      skipRefresh: true,
    }),

  logout: () => request<{ message: string }>("/api/auth/logout", { method: "POST", skipRefresh: true }),

  me: () => request<AppUser>("/api/auth/me"),

  dashboard: () => request<DashboardSummary>("/api/dashboard"),
}
