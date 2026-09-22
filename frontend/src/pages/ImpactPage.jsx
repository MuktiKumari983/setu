import { useEffect, useState } from 'react'
import { impactApi, projectApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import StatCard from '../components/StatCard'
import StatusPill from '../components/StatusPill'
import { errorMessage, fmtDateTime } from '../util'

export default function ImpactPage() {
  const { user } = useAuth()
  const [records, setRecords] = useState([])
  const [stats, setStats] = useState(null)
  const [projects, setProjects] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [form, setForm] = useState({ projectId: '', beneficiaries: '', locationsCovered: '', costSavings: '', problemReduction: '', durationMonths: '12', implementationPercent: '100' })
  const [evidence, setEvidence] = useState({ projectId: '', evidenceType: 'WITNESS_STATEMENT', description: '', fileUrl: '' })

  const isReviewer = ['GOVERNMENT_ADMIN', 'PLATFORM_ADMIN'].includes(user?.role)

  function load() {
    setError('')
    Promise.all([impactApi.list(), impactApi.stats()])
      .then(([r, s]) => {
        setRecords(r)
        setStats(s)
      })
      .catch((e) => setError(errorMessage(e)))
  }

  useEffect(() => {
    load()
    projectApi
      .list()
      .then(setProjects)
      .catch(() => {})
  }, [])

  async function submitImpact(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await impactApi.submit(Number(form.projectId), {
        beneficiaries: Number(form.beneficiaries),
        locationsCovered: form.locationsCovered ? Number(form.locationsCovered) : 0,
        costSavings: form.costSavings ? Number(form.costSavings) : 0,
        problemReduction: form.problemReduction,
        durationMonths: Number(form.durationMonths),
        implementationPercent: Number(form.implementationPercent),
        domainMetrics: '',
      })
      setForm({ projectId: '', beneficiaries: '', locationsCovered: '', costSavings: '', problemReduction: '', durationMonths: '12', implementationPercent: '100' })
      load()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function addEvidence(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await impactApi.addEvidence(Number(evidence.projectId), {
        impactRecordId: 0,
        evidenceType: evidence.evidenceType,
        description: evidence.description,
        fileUrl: evidence.fileUrl,
      })
      setEvidence({ projectId: '', evidenceType: 'WITNESS_STATEMENT', description: '', fileUrl: '' })
      load()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function review(evidenceId, status) {
    try {
      await impactApi.reviewEvidence(evidenceId, status)
      load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div className="space-y-5">
      <h1 className="text-lg font-semibold text-slate-800">Social Impact & Evidence Verification</h1>
      {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      {stats && (
        <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
          <StatCard label="Impact records" value={stats.records || 0} />
          <StatCard label="Verified records" value={stats.verifiedRecords || 0} tone="#16a34a" />
          <StatCard label="Total beneficiaries" value={stats.totalBeneficiaries || 0} tone="#6366f1" />
          <StatCard label="Verified beneficiaries" value={stats.verifiedBeneficiaries || 0} tone="#15803d" />
        </div>
      )}

      {projects.length > 0 && (
        <div className="grid gap-4 lg:grid-cols-2">
          <div className="card">
            <h3 className="mb-3 text-sm font-semibold text-slate-700">Submit impact for a project</h3>
            <form onSubmit={submitImpact} className="grid gap-2">
              <select className="input" value={form.projectId} onChange={(e) => setForm((f) => ({ ...f, projectId: e.target.value }))} required>
                <option value="">Select project...</option>
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title}
                  </option>
                ))}
              </select>
              <div className="grid grid-cols-2 gap-2">
                <input className="input" type="number" placeholder="Beneficiaries reached" value={form.beneficiaries} onChange={(e) => setForm((f) => ({ ...f, beneficiaries: e.target.value }))} required />
                <input className="input" type="number" placeholder="Locations covered" value={form.locationsCovered} onChange={(e) => setForm((f) => ({ ...f, locationsCovered: e.target.value }))} />
              </div>
              <input className="input" placeholder="Problem reduction (e.g. 40% fewer contamination incidents)" value={form.problemReduction} onChange={(e) => setForm((f) => ({ ...f, problemReduction: e.target.value }))} />
              <div className="grid grid-cols-2 gap-2">
                <input className="input" type="number" step="0.01" placeholder="Cost savings (INR)" value={form.costSavings} onChange={(e) => setForm((f) => ({ ...f, costSavings: e.target.value }))} />
                <input className="input" type="number" placeholder="Duration (months)" value={form.durationMonths} onChange={(e) => setForm((f) => ({ ...f, durationMonths: e.target.value }))} />
              </div>
              <button className="btn-primary" disabled={busy}>
                Submit for verification
              </button>
            </form>
          </div>

          <div className="card">
            <h3 className="mb-3 text-sm font-semibold text-slate-700">Add evidence</h3>
            <form onSubmit={addEvidence} className="grid gap-2">
              <select className="input" value={evidence.projectId} onChange={(e) => setEvidence((ev) => ({ ...ev, projectId: e.target.value }))} required>
                <option value="">Select project...</option>
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title}
                  </option>
                ))}
              </select>
              <select className="input" value={evidence.evidenceType} onChange={(e) => setEvidence((ev) => ({ ...ev, evidenceType: e.target.value }))}>
                <option>WITNESS_STATEMENT</option>
                <option>OFFICIAL_RECORD</option>
                <option>PROJECT_DATA</option>
                <option>AUDIT_REPORT</option>
                <option>PHOTO</option>
              </select>
              <input className="input" placeholder="What does this evidence show?" value={evidence.description} onChange={(e) => setEvidence((ev) => ({ ...ev, description: e.target.value }))} required />
              <input className="input" placeholder="/files/evidence.pdf" value={evidence.fileUrl} onChange={(e) => setEvidence((ev) => ({ ...ev, fileUrl: e.target.value }))} />
              <button className="btn-primary" disabled={busy}>
                Attach evidence
              </button>
            </form>
          </div>
        </div>
      )}

      <div className="card">
        <h3 className="mb-3 text-sm font-semibold text-slate-700">Impact records</h3>
        {records.length === 0 ? (
          <p className="text-sm text-slate-400">No impact records yet.</p>
        ) : (
          <div className="space-y-4">
            {records.map((r) => (
              <div key={r.id} className="rounded-lg border border-slate-100 p-3">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <p className="text-sm font-medium text-slate-700">{r.projectTitle}</p>
                    <p className="text-xs text-slate-400">
                      {r.beneficiaries} beneficiaries, {r.locationsCovered} locations, submitted by {r.submittedBy} on {fmtDateTime(r.submittedAt)}
                    </p>
                  </div>
                  <StatusPill value={r.verificationStatus} />
                </div>
                {r.problemReduction && <p className="mt-1 text-xs text-slate-500">{r.problemReduction}</p>}
                {r.evidence?.length > 0 && (
                  <div className="mt-3 space-y-2">
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Evidence</p>
                    {r.evidence.map((ev) => (
                      <div key={ev.id} className="flex items-center justify-between rounded-lg bg-slate-50 p-2">
                        <div>
                          <p className="text-sm text-slate-600">{ev.description}</p>
                          <p className="text-xs text-slate-400">
                            {ev.evidenceType} - by {ev.submittedBy}
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          <StatusPill value={ev.status} />
                          {isReviewer && ev.status !== 'APPROVED' && (
                            <button className="btn-primary !px-2 !py-1 text-xs" onClick={() => review(ev.id, 'APPROVED')}>
                              Approve
                            </button>
                          )}
                          {isReviewer && ev.status !== 'REJECTED' && (
                            <button className="btn-secondary !px-2 !py-1 text-xs" onClick={() => review(ev.id, 'REJECTED')}>
                              Reject
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}