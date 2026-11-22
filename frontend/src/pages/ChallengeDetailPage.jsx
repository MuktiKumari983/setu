import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { problemApi } from '../api/client'
import { aiSourceLabel, asList } from '../api/contract'
import { useAuth } from '../auth/AuthContext'
import ErrorBanner from '../components/ErrorBanner'
import StatusPill from '../components/StatusPill'
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

  if (!problem && !error) return <p className="text-sm text-slate-500" aria-live="polite">Loading...</p>

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
            <button type="button" className="btn-secondary" onClick={() => action(() => problemApi.analyze(problem.id), 'Request AI re-analysis')}>
              Re-analyze
            </button>
          )}
          {isVerifier && (
            <button
              type="button"
              className="btn-primary"
              disabled={busy || problem?.status === 'VERIFIED' || problem?.status === 'MATCHED'}
              onClick={() => action(() => problemApi.verify(problem.id), 'Verify this challenge')}
            >
              Verify
            </button>
          )}
        </div>
      </div>

      <ErrorBanner message={error} />

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
                  <div className="text-xs text-slate-500">
                    Source: <strong>{aiSourceLabel(problem.aiSource)}</strong>
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
                {asList(problem.aiSkills).map((s) => (
                  <span key={s} className="rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
                    {s}
                  </span>
                ))}
              </div>
              {asList(problem.aiKeywords).length > 0 && (
                <p className="mt-2 text-xs text-slate-400">Keywords: {asList(problem.aiKeywords).join(', ')}</p>
              )}
              {[
                ['Root causes', problem.aiRootCauses],
                ['Target beneficiaries', problem.aiTargetBeneficiaries],
                ['Challenges', problem.aiChallenges],
                ['Solution directions', problem.aiSolutionDirections],
                ['Expected outcomes', problem.aiExpectedOutcomes],
              ]
                .filter(([, value]) => asList(value).length > 0)
                .map(([label, value]) => (
                  <div key={label} className="mt-3">
                    <p className="text-xs font-semibold text-slate-600">{label}</p>
                    <ul className="mt-1 list-disc pl-4 text-xs text-slate-500">
                      {asList(value).map((item, index) => (
                        <li key={`${label}-${index}`}>{item}</li>
                      ))}
                    </ul>
                  </div>
                ))}
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
              <span className="sr-only" aria-live="polite">{busy ? 'Working, please wait.' : ''}</span>
            </div>
          </div>
        </>
      )}
    </div>
  )
}