import { useEffect, useState } from 'react'
import { impactApi, projectApi } from '../api/client'
import { toNumberOrZero } from '../api/contract'
import { useAuth } from '../auth/AuthContext'
import EmptyState from '../components/EmptyState'
import ErrorBanner from '../components/ErrorBanner'
import FormField from '../components/FormField'
import Spinner from '../components/Spinner'
import StatCard from '../components/StatCard'
import StatusPill from '../components/StatusPill'
import SuccessBanner from '../components/SuccessBanner'
import { errorMessage, fmtDateTime } from '../util'

const EMPTY_IMPACT = {
  projectId: '',
  beneficiaries: '',
  locationsCovered: '',
  costSavings: '',
  problemReduction: '',
  durationMonths: '',
  implementationPercent: '',
  domainMetrics: '',
}

const EMPTY_EVIDENCE = {
  projectId: '',
  impactRecordId: '',
  evidenceType: 'WITNESS_STATEMENT',
  description: '',
  fileUrl: '',
}

export default function ImpactPage() {
  const { user } = useAuth()
  const [records, setRecords] = useState([])
  const [stats, setStats] = useState(null)
  const [projects, setProjects] = useState([])
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState(EMPTY_IMPACT)
  const [evidence, setEvidence] = useState(EMPTY_EVIDENCE)

  const isReviewer = ['GOVERNMENT_ADMIN', 'PLATFORM_ADMIN'].includes(user?.role)

  function load() {
    setError('')
    return Promise.all([impactApi.list(), impactApi.stats()])
      .then(([r, s]) => {
        setRecords(r)
        setStats(s)
      })
      .catch((e) => setError(errorMessage(e)))
  }

  useEffect(() => {
    Promise.all([
      load(),
      projectApi.list().then(setProjects).catch(() => {}),
    ]).finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!evidence.projectId) {
      if (evidence.impactRecordId) {
        setEvidence((ev) => ({ ...ev, impactRecordId: '' }))
      }
      return
    }
    const matchingRecord = records.find((r) => String(r.projectId) === String(evidence.projectId))
    if (matchingRecord && String(evidence.impactRecordId) !== String(matchingRecord.id)) {
      setEvidence((ev) => ({ ...ev, impactRecordId: matchingRecord.id }))
    } else if (!matchingRecord && evidence.impactRecordId) {
      setEvidence((ev) => ({ ...ev, impactRecordId: '' }))
    }
  }, [records, evidence.projectId, evidence.impactRecordId])

  async function submitImpact(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    setSuccess('')
    try {
      await impactApi.submit(Number(form.projectId), {
        beneficiaries: toNumberOrZero(form.beneficiaries),
        locationsCovered: toNumberOrZero(form.locationsCovered),
        costSavings: toNumberOrZero(form.costSavings),
        problemReduction: form.problemReduction,
        durationMonths: toNumberOrZero(form.durationMonths),
        implementationPercent: toNumberOrZero(form.implementationPercent),
        domainMetrics: form.domainMetrics,
      })
      setForm(EMPTY_IMPACT)
      setSuccess('Impact submitted for verification.')
      await load()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function addEvidence(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    if (!evidence.impactRecordId) {
      setError('Select an impact record before attaching evidence.')
      return
    }
    setBusy(true)
    try {
      await impactApi.addEvidence(Number(evidence.projectId), {
        impactRecordId: Number(evidence.impactRecordId),
        evidenceType: evidence.evidenceType,
        description: evidence.description,
        fileUrl: evidence.fileUrl,
      })
      setEvidence(EMPTY_EVIDENCE)
      setSuccess('Evidence attached to the impact record.')
      await load()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  async function review(evidenceId, status) {
    setError('')
    setSuccess('')
    try {
      await impactApi.reviewEvidence(evidenceId, status)
      setSuccess(`Evidence ${status.toLowerCase()} successfully.`)
      await load()
    } catch (err) {
      setError(errorMessage(err))
    }
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-lg font-semibold text-slate-800">Social Impact & Evidence Verification</h1>
        <p className="mt-1 text-sm text-slate-500">Record outcomes and review supporting evidence.</p>
      </div>

      <ErrorBanner message={error} />
      <SuccessBanner message={success} />
      {loading && <Spinner label="Loading impact data..." />}

      {stats && (
        <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
          <StatCard label="Impact records" value={stats.records || 0} />
          <StatCard label="Verified records" value={stats.verifiedRecords || 0} tone="#16a34a" />
          <StatCard label="Total beneficiaries" value={stats.totalBeneficiaries || 0} tone="#6366f1" />
          <StatCard label="Verified beneficiaries" value={stats.verifiedBeneficiaries || 0} tone="#15803d" />
        </div>
      )}

      {!loading && projects.length > 0 && (
        <div className="grid gap-4 lg:grid-cols-2">
          <div className="card">
            <h2 className="mb-3 text-sm font-semibold text-slate-700">Submit impact for a project</h2>
            <form onSubmit={submitImpact} className="grid gap-3">
              <FormField id="impact-project" label="Project">
                <select
                  id="impact-project"
                  className="input"
                  value={form.projectId}
                  onChange={(e) => setForm((f) => ({ ...f, projectId: e.target.value }))}
                  required
                >
                  <option value="">Select project...</option>
                  {projects.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.title}
                    </option>
                  ))}
                </select>
              </FormField>
              <div className="grid grid-cols-2 gap-2">
                <FormField id="impact-beneficiaries" label="Beneficiaries reached">
                  <input id="impact-beneficiaries" className="input" type="number" min="0" value={form.beneficiaries} onChange={(e) => setForm((f) => ({ ...f, beneficiaries: e.target.value }))} required />
                </FormField>
                <FormField id="impact-locations" label="Locations covered">
                  <input id="impact-locations" className="input" type="number" min="0" value={form.locationsCovered} onChange={(e) => setForm((f) => ({ ...f, locationsCovered: e.target.value }))} />
                </FormField>
              </div>
              <FormField id="impact-reduction" label="Problem reduction">
                <input id="impact-reduction" className="input" value={form.problemReduction} onChange={(e) => setForm((f) => ({ ...f, problemReduction: e.target.value }))} />
              </FormField>
              <div className="grid grid-cols-2 gap-2">
                <FormField id="impact-savings" label="Cost savings (INR)">
                  <input id="impact-savings" className="input" type="number" min="0" step="0.01" value={form.costSavings} onChange={(e) => setForm((f) => ({ ...f, costSavings: e.target.value }))} />
                </FormField>
                <FormField id="impact-duration" label="Duration (months)">
                  <input id="impact-duration" className="input" type="number" min="0" value={form.durationMonths} onChange={(e) => setForm((f) => ({ ...f, durationMonths: e.target.value }))} />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <FormField id="impact-implementation" label="Implementation %">
                  <input id="impact-implementation" className="input" type="number" min="0" max="100" value={form.implementationPercent} onChange={(e) => setForm((f) => ({ ...f, implementationPercent: e.target.value }))} required />
                </FormField>
                <FormField id="impact-domain" label="Domain metrics">
                  <input id="impact-domain" className="input" value={form.domainMetrics} onChange={(e) => setForm((f) => ({ ...f, domainMetrics: e.target.value }))} />
                </FormField>
              </div>
              <button className="btn-primary" disabled={busy}>
                {busy ? 'Submitting...' : 'Submit for verification'}
              </button>
            </form>
          </div>

          <div className="card">
            <h2 className="mb-3 text-sm font-semibold text-slate-700">Add evidence</h2>
            <form onSubmit={addEvidence} className="grid gap-3">
              <FormField id="evidence-project" label="Project">
                <select
                  id="evidence-project"
                  className="input"
                  value={evidence.projectId}
                  onChange={(e) => setEvidence((ev) => ({ ...ev, projectId: e.target.value, impactRecordId: '' }))}
                  required
                >
                  <option value="">Select project...</option>
                  {projects.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.title}
                    </option>
                  ))}
                </select>
              </FormField>
              <FormField id="evidence-record" label="Impact record">
                <select
                  id="evidence-record"
                  className="input"
                  value={evidence.impactRecordId}
                  onChange={(e) => setEvidence((ev) => ({ ...ev, impactRecordId: e.target.value }))}
                  required
                >
                  <option value="">Select impact record...</option>
                  {records
                    .filter((r) => String(r.projectId) === String(evidence.projectId))
                    .map((r) => (
                      <option key={r.id} value={r.id}>
                        #{r.id} - {r.beneficiaries} beneficiaries ({r.verificationStatus})
                      </option>
                    ))}
                </select>
              </FormField>
              <FormField id="evidence-type" label="Evidence type">
                <select id="evidence-type" className="input" value={evidence.evidenceType} onChange={(e) => setEvidence((ev) => ({ ...ev, evidenceType: e.target.value }))}>
                  <option>WITNESS_STATEMENT</option>
                  <option>OFFICIAL_RECORD</option>
                  <option>PROJECT_DATA</option>
                  <option>AUDIT_REPORT</option>
                  <option>PHOTO</option>
                </select>
              </FormField>
              <FormField id="evidence-description" label="What does this evidence show?">
                <input id="evidence-description" className="input" value={evidence.description} onChange={(e) => setEvidence((ev) => ({ ...ev, description: e.target.value }))} required />
              </FormField>
              <FormField id="evidence-url" label="File or document URL">
                <input id="evidence-url" className="input" value={evidence.fileUrl} onChange={(e) => setEvidence((ev) => ({ ...ev, fileUrl: e.target.value }))} />
              </FormField>
              <button className="btn-primary" disabled={busy}>
                {busy ? 'Attaching...' : 'Attach evidence'}
              </button>
            </form>
          </div>
        </div>
      )}

      <div className="card">
        <h2 className="mb-3 text-sm font-semibold text-slate-700">Impact records</h2>
        {!loading && records.length === 0 ? (
          <EmptyState title="No impact records yet" description="Submitted records will appear here for verification." />
        ) : (
          <div className="space-y-4" aria-live="polite">
            {records.map((r) => (
              <article key={r.id} className="rounded-lg border border-slate-100 p-3">
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
                          <p className="text-xs text-slate-400">{ev.evidenceType} - by {ev.submittedBy}</p>
                        </div>
                        <div className="flex items-center gap-2">
                          <StatusPill value={ev.status} />
                          {isReviewer && ev.status !== 'VERIFIED' && (
                            <button type="button" className="btn-primary !px-2 !py-1 text-xs" onClick={() => review(ev.id, 'VERIFIED')}>
                              Verify
                            </button>
                          )}
                          {isReviewer && ev.status !== 'REJECTED' && (
                            <button type="button" className="btn-secondary !px-2 !py-1 text-xs" onClick={() => review(ev.id, 'REJECTED')}>
                              Reject
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </article>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
