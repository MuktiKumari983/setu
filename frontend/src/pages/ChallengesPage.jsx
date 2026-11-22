import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { problemApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import ErrorBanner from '../components/ErrorBanner'
import StatusPill from '../components/StatusPill'
import { errorMessage } from '../util'

const STATUSES = ['', 'SUBMITTED', 'VERIFIED', 'MATCHED', 'TEAM_FORMED', 'PROPOSAL', 'PROTOTYPE', 'REVIEW', 'PILOT', 'IMPLEMENTATION', 'IMPACT']

export default function ChallengesPage() {
  const { user } = useAuth()
  const [problems, setProblems] = useState([])
  const [status, setStatus] = useState('')
  const [category, setCategory] = useState('')
  const [error, setError] = useState('')
  const [form, setForm] = useState({
    title: '',
    description: '',
    location: '',
    estimatedBeneficiaries: '',
    urgency: 'MEDIUM',
    requiredOutcome: '',
  })
  const [showForm, setShowForm] = useState(false)
  const [busy, setBusy] = useState(false)

  const isCitizen = user?.role === 'CITIZEN'
  const isVerifier = ['GOVERNMENT_ADMIN', 'PLATFORM_ADMIN'].includes(user?.role)

  function load() {
    setError('')
    problemApi
      .list({ status, category })
      .then(setProblems)
      .catch((e) => setError(errorMessage(e)))
  }

  useEffect(() => {
    load()
  }, [status, category])

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await problemApi.create({
        ...form,
        estimatedBeneficiaries: form.estimatedBeneficiaries ? Number(form.estimatedBeneficiaries) : null,
      })
      setForm({ title: '', description: '', location: '', estimatedBeneficiaries: '', urgency: 'MEDIUM', requiredOutcome: '' })
      setShowForm(false)
      load()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-slate-800">Community Challenges</h1>
        {isCitizen && (
          <button type="button" className="btn-primary" aria-expanded={showForm} aria-controls={showForm ? 'challenge-form' : undefined} onClick={() => setShowForm((v) => !v)}>
            {showForm ? 'Cancel' : 'Submit a challenge'}
          </button>
        )}
      </div>

      <ErrorBanner message={error} />

      {isCitizen && showForm && (
        <div className="card">
          <h3 className="mb-3 text-sm font-semibold text-slate-700">Describe a community problem</h3>
          <form onSubmit={submit} className="grid gap-4 md:grid-cols-2" id="challenge-form">
            <div className="md:col-span-2">
              <label className="label" htmlFor="challenge-title">Title</label>
              <input id="challenge-title" className="input" value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} required />
            </div>
            <div className="md:col-span-2">
              <label className="label" htmlFor="challenge-description">Description</label>
              <textarea id="challenge-description" className="input" rows={4} value={form.description} onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))} required />
            </div>
            <div>
              <label className="label" htmlFor="challenge-location">Location</label>
              <input id="challenge-location" className="input" value={form.location} onChange={(e) => setForm((f) => ({ ...f, location: e.target.value }))} required />
            </div>
            <div>
              <label className="label" htmlFor="challenge-beneficiaries">Estimated beneficiaries</label>
              <input id="challenge-beneficiaries" className="input" type="number" value={form.estimatedBeneficiaries} onChange={(e) => setForm((f) => ({ ...f, estimatedBeneficiaries: e.target.value }))} />
            </div>
            <div>
              <label className="label" htmlFor="challenge-urgency">Urgency</label>
              <select id="challenge-urgency" className="input" value={form.urgency} onChange={(e) => setForm((f) => ({ ...f, urgency: e.target.value }))}>
                <option>LOW</option>
                <option>MEDIUM</option>
                <option>HIGH</option>
              </select>
            </div>
            <div>
              <label className="label" htmlFor="challenge-outcome">Required outcome</label>
              <input id="challenge-outcome" className="input" value={form.requiredOutcome} onChange={(e) => setForm((f) => ({ ...f, requiredOutcome: e.target.value }))} />
            </div>
            <div className="md:col-span-2">
              <button type="submit" className="btn-primary" disabled={busy}>
                <span aria-live="polite">{busy ? 'Submitting...' : 'Submit for AI analysis & approval'}</span>
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="flex flex-wrap items-center gap-3">
        <select className="input w-48" aria-label="Filter by status" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All statuses</option>
          {STATUSES.filter(Boolean).map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
        <input
          className="input w-64"
          aria-label="Filter by category"
          placeholder="Filter by category..."
          value={category}
          onChange={(e) => setCategory(e.target.value)}
        />
      </div>

      {problems.length === 0 ? (
        <p className="text-sm text-slate-400" aria-live="polite">No challenges match your filters.</p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {problems.map((p) => (
            <Link
              key={p.id}
              to={`/challenges/${p.id}`}
              className="card flex flex-col gap-2 transition-shadow hover:shadow-md"
            >
              <div className="flex items-start justify-between gap-2">
                <h3 className="text-sm font-semibold text-slate-800">{p.title}</h3>
                <StatusPill value={p.status} />
              </div>
              <p className="line-clamp-3 text-xs text-slate-500">{p.description}</p>
              <div className="mt-auto flex items-center justify-between pt-2">
                <span className="text-xs text-slate-400">{p.location || 'Location TBD'}</span>
                {p.aiCategory && (
                  <span className="rounded-md bg-brand-50 px-2 py-0.5 text-xs font-medium text-brand-700">{p.aiCategory}</span>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}

      {isVerifier && problems.length > 0 && (
        <p className="text-xs text-slate-400">
          Use the verification actions on each challenge&apos;s detail page to approve or reject.
        </p>
      )}
    </div>
  )
}