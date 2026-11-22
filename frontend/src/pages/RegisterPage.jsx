import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import ErrorBanner from '../components/ErrorBanner'
import FormField from '../components/FormField'
import { errorMessage } from '../util'

const ROLES = ['CITIZEN', 'STUDENT', 'FACULTY', 'UNIVERSITY_ADMIN', 'INDUSTRY']

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '', role: 'CITIZEN', universityId: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  function update(field) {
    return (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  }

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const payload = { ...form, universityId: form.universityId ? Number(form.universityId) : null }
      await register(payload)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-8">
      <div className="w-full max-w-md">
        <div className="mb-6 text-center">
          <Link to="/" className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white" aria-label="Setu home">
            S
          </Link>
          <h1 className="text-xl font-semibold text-slate-800">Create an account</h1>
        </div>

        <div className="card">
          <ErrorBanner message={error} />
          <form onSubmit={submit} className="space-y-4">
            <FormField id="register-name" label="Full name">
              <input id="register-name" className="input" autoComplete="name" autoFocus value={form.name} onChange={update('name')} required />
            </FormField>
            <FormField id="register-email" label="Email">
              <input id="register-email" className="input" type="email" autoComplete="email" value={form.email} onChange={update('email')} required />
            </FormField>
            <FormField id="register-password" label="Password" hint="Use at least 6 characters.">
              <input
                id="register-password"
                className="input"
                type="password"
                autoComplete="new-password"
                value={form.password}
                onChange={update('password')}
                minLength={6}
                aria-describedby="register-password-hint"
                required
              />
            </FormField>
            <FormField id="register-phone" label="Phone (optional)">
              <input id="register-phone" className="input" autoComplete="tel" value={form.phone} onChange={update('phone')} />
            </FormField>
            <FormField id="register-role" label="Role">
              <select id="register-role" className="input" value={form.role} onChange={update('role')}>
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r.replace('_', ' ')}
                  </option>
                ))}
              </select>
            </FormField>
            <FormField id="register-university" label="University ID (optional)" hint="Only for faculty, students, and university administrators.">
              <input
                id="register-university"
                className="input"
                type="number"
                value={form.universityId}
                onChange={update('universityId')}
                aria-describedby="register-university-hint"
              />
            </FormField>
            <button className="w-full btn-primary" disabled={busy}>
              {busy ? 'Creating...' : 'Register'}
            </button>
          </form>
        </div>

        <p className="mt-4 text-center text-sm text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-brand-600 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
