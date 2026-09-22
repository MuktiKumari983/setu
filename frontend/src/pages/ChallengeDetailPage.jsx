import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { problemApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import StatusPill from '../components/StatusPill'
import ScoreMeter from '../components/ScoreMeter'
import { errorMessage, fmtDateTime } from '../util'

export default function ChallengeDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const [problem, setProblem] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const isVerifier = ['GOVERNMENT_ADMIN', 'PLATFORM_ADMIN'].includes(user?.role)
  const canEdit = user?.id === problem?.submitter?.id

  function load() {
    setError('')
    problemApi
      .get(id)
      .then(setProblem)
      .catch((e) => setError(errorMessage(e)))
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  async function action(fn, label) {
    if (!window.confirm(`Confirm: ${label}`)) return
    setBusy(true)
    setError('')
    try {
      await fn()
      load()
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  if (!problem && !error) return <p className="text-sm text-slate-500">Loading...</p>

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <Link to="/challenges" className="text-sm text-brand-600 hover:underline">
          &larr; All challenges
        </Link>
        <div className="flex items-center gap-2">
          <Link to={`/matching/${problem?.id}`} className="btn-secondary">
            View matching
          </Link>
          {canEdit && (
            <button className="btn-secondary" onClick={() => action(() => problemApi.analyze(problem.id), 'Request AI re-analysis')}>
              Re-analyze
            </button>
          )}
          {isVerifier && (
            <button
              className="btn-primary"
              disabled={busy || problem?.status === 'VERIFIED' || problem?.status === 'MATCHED'}
              onClick={() => action(() => problemApi.verify(problem.id), 'Verify this challenge')}
            >
              Verify
            </button>
          )}
        </div>
      </div>

      {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      {problem && (
        <>
          <div className="card">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <h1 className="text-lg font-semibold text-slate-800">{problem.title}</h1>
                <p className="text-sm text-slate-500">
                  Submitted by {problem.submitter?.name} ({problem.submitter?.email})
                </p>
              </div>
              <StatusPill value={problem.status} />
            </div>
            <p className="mt-3 text-sm text-slate-600">{problem.description}</p>
            <dl className="mt-4 grid grid-cols-2 gap-3 text-sm md:grid-cols-4">
              <div>
                <dt className="text-xs text-slate-400">Location</dt>
                <dd>{problem.location || '-'}</dd>
              </div>
              <div>
                <dt className="text-xs text-slate-400">Est. beneficiaries</dt>
                <dd>{problem.estimatedBeneficiaries ?? '-'}</dd>
              </div>
              <div>
                <dt className="text-xs text-slate-400">Urgency</dt>
                <dd>
                  <StatusPill value={problem.urgency} />
                </dd>
              </div>
              <div>
                <dt className="text-xs text-slate-400">Submitted</dt>
                <dd>{fmtDateTime(problem.createdAt)}</dd>
              </div>
            </dl>
          </div>

          {problem.aiCategory && (
            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">AI analysis</h3>
              <div className="flex flex-wrap items-center gap-4">
                <span className="rounded-lg bg-brand-50 px-3 py-1.5 text-sm font-semibold text-brand-700">
                  {problem.aiCategory}
                </span>
                <StatusPill value={problem.aiUrgency} />
                <div className="text-xs text-slate-500">
                  Confidence: <strong>{Math.round(problem.aiConfidence * 100)}%</strong>
                </div>
                {problem.aiSimilarity > 0 && (
                  <div className="flex items-center gap-2 text-xs text-slate-500">
                    Similar to existing challenge:
                    {problem.duplicateOf ? (
                      <Link to={`/challenges/${problem.duplicateOf.id}`} className="font-medium text-brand-600 hover:underline">
                        {problem.duplicateOf.title} ({problem.duplicateOf.similarity})
                      </Link>
                    ) : (
                      <span>{problem.aiSimilarity}</span>
                    )}
                  </div>
                )}
              </div>
              <p className="mt-3 text-xs text-slate-500">{problem.aiExplanation}</p>
              <div className="mt-3 flex flex-wrap gap-2">
                {(problem.aiSkills || []).map((s) => (
                  <span key={s} className="rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
                    {s}
                  </span>
                ))}
              </div>
              {(problem.aiKeywords || []).length > 0 && (
                <p className="mt-2 text-xs text-slate-400">Keywords: {problem.aiKeywords.join(', ')}</p>
              )}
            </div>
          )}

          {problem.capabilities && problem.capabilities.length > 0 && (
            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">AI capability decomposition</h3>
              <ul className="space-y-3">
                {problem.capabilities.map((c) => (
                  <li key={c.id} className="rounded-lg border border-slate-100 p-3">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-slate-700">{c.capability}</span>
                      <StatusPill value={c.priority} />
                    </div>
                    <p className="mt-1 text-xs text-slate-500">{c.description}</p>
                    <div className="mt-2 flex flex-wrap gap-1.5">
                      {c.requiredSkills.map((s) => (
                        <span key={s} className="rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-600">
                          {s}
                        </span>
                      ))}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="card">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-slate-700">Best matches so far</h3>
              <Link to={`/matching/${problem.id}`} className="btn-primary">
                Run matching
              </Link>
            </div>
          </div>
        </>
      )}
    </div>
  )
}