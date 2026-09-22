import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { errorMessage } from '../util'

const ROLES = [
  'CITIZEN',
  'STUDENT',
  'FACULTY',
  'UNIVERSITY_ADMIN',
  'INDUSTRY',
]

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
          <span className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white">
            S
          </span>
          <h1 className="text-xl font-semibold text-slate-800">Create an account</h1>
        </div>

        <div className="card">
          {error && <p className="mb-3 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}
          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="label">Full name</label>
              <input className="input" value={form.name} onChange={update('name')} required />
            </div>
            <div>
              <label className="label">Email</label>
              <input className="input" type="email" value={form.email} onChange={update('email')} required />
            </div>
            <div>
              <label className="label">Password (min 6 chars)</label>
              <input className="input" type="password" value={form.password} onChange={update('password')} minLength={6} required />
            </div>
            <div>
              <label className="label">Phone (optional)</label>
              <input className="input" value={form.phone} onChange={update('phone')} />
            </div>
            <div>
              <label className="label">Role</label>
              <select className="input" value={form.role} onChange={update('role')}>
                {ROLES.map((r) => (
                  <option key={r} value={r}>
                    {r.replace('_', ' ')}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">University ID (optional)</label>
              <input className="input" type="number" value={form.universityId} onChange={update('universityId')} placeholder="Only for faculty / students" />
            </div>
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