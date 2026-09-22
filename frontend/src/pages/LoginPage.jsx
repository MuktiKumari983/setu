import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
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
          <span className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white">
            S
          </span>
          <h1 className="text-xl font-semibold text-slate-800">Setu</h1>
          <p className="text-sm text-slate-500">Societal Innovation Collaboration Platform</p>
        </div>

        <div className="card">
          <h2 className="mb-4 text-sm font-semibold text-slate-700">Sign in to your account</h2>
          {error && <p className="mb-3 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}
          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="label">Email</label>
              <input
                className="input"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
                required
              />
            </div>
            <div>
              <label className="label">Password</label>
              <input
                className="input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button className="w-full btn-primary" disabled={busy}>
              {busy ? 'Signing in...' : 'Sign in'}
            </button>
          </form>
        </div>

        <div className="mt-4 rounded-lg border border-slate-200 bg-white p-4 text-xs text-slate-500">
          <p className="mb-1 font-semibold text-slate-700">Demo accounts (password: password123)</p>
          <p>admin@example.com (Platform Admin)</p>
          <p>government@example.com, citizen@example.com</p>
          <p>student@example.com, faculty@example.com, university@example.com, industry@example.com</p>
        </div>

        <p className="mt-4 text-center text-sm text-slate-500">
          Don't have an account?{' '}
          <Link to="/register" className="font-medium text-brand-600 hover:underline">
            Register
          </Link>
        </p>
      </div>
    </div>
  )
}