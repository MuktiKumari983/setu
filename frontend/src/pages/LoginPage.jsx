import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import ErrorBanner from '../components/ErrorBanner'
import FormField from '../components/FormField'
import { errorMessage } from '../util'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await login(email, password)
      navigate(location.state?.from || '/dashboard', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-sm">
        <div className="mb-8 text-center">
          <Link to="/" className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white" aria-label="Setu home">
            S
          </Link>
          <h1 className="text-xl font-semibold text-slate-800">Setu</h1>
          <p className="text-sm text-slate-500">Societal Innovation Collaboration Platform</p>
        </div>

        <div className="card">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">Sign in to your account</h2>
          <ErrorBanner message={error} />
          <form onSubmit={submit} className="space-y-4">
            <FormField id="login-email" label="Email">
              <input
                id="login-email"
                className="input"
                type="email"
                autoComplete="email"
                autoFocus
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
              />
            </FormField>
            <FormField id="login-password" label="Password">
              <input
                id="login-password"
                className="input"
                type="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </FormField>
            <button className="w-full btn-primary" disabled={busy}>
              {busy ? 'Signing in...' : 'Sign in'}
            </button>
          </form>
        </div>

        {import.meta.env.DEV && (
          <div className="mt-4 rounded-lg border border-slate-200 bg-white p-4 text-xs text-slate-500">
            <p className="mb-1 font-semibold text-slate-700">Demo accounts (password: password123)</p>
            <p>admin@example.com (Platform Admin)</p>
            <p>government@example.com, citizen@example.com</p>
            <p>student@example.com, faculty@example.com, university@example.com, industry@example.com</p>
          </div>
        )}

        <p className="mt-4 text-center text-sm text-slate-500">
          Don&apos;t have an account?{' '}
          <Link to="/register" className="font-medium text-brand-600 hover:underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}
