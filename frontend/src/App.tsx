import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom"

import { GuestRoute, ProtectedRoute } from "@/components/protected-route"
import { AuthProvider } from "@/context/auth-context"
import DashboardPage from "@/pages/dashboard-page"
import LoginPage from "@/pages/login-page"
import NotFoundPage from "@/pages/not-found-page"
import RecoverPasswordPage from "@/pages/recover-password-page"
import RegisterPage from "@/pages/register-page"
import ResetPasswordPage from "@/pages/reset-password-page"

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />

          {/* Telas publicas: quem ja esta logado e levado ao painel */}
          <Route element={<GuestRoute />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/recoverpassword" element={<RecoverPasswordPage />} />
          </Route>

          {/* O link do e-mail deve funcionar mesmo se ja houver uma sessao aberta. */}
          <Route path="/resetpassword" element={<ResetPasswordPage />} />

          {/* Area protegida: exige sessao valida */}
          <Route element={<ProtectedRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
