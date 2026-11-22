import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { matchingApi, problemApi } from '../api/client'
import { aiSourceLabel, asList } from '../api/contract'
import ErrorBanner from '../components/ErrorBanner'
import ScoreMeter from '../components/ScoreMeter'
import { errorMessage } from '../util'

export default function MatchingPage() {
  const { id: routeId } = useParams()
  const [problems, setProblems] = useState([])
  const [problemId, setProblemId] = useState(routeId || '')
  const [problem, setProblem] = useState(null)
  const [matches, setMatches] = useState([])
  const [recommendation, setRecommendation] = useState(null)
  const [recovery, setRecovery] = useState(null)
  const [missingCapability, setMissingCapability] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    problemApi
      .list({})
      .then(setProblems)
      .catch((e) => setError(errorMessage(e)))
  }, [])

  useEffect(() => {
    if (routeId) {
      setProblemId(routeId)
      load(routeId)
    }
  }, [routeId])

  async function load(pid) {
    if (!pid) return
    setError('')
    setBusy(true)
    try {
      const p = await problemApi.get(pid)
      setProblem(p)
      const m = await matchingApi.results(pid)
      setMatches(m)
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  async function runMatching() {
    setError('')
    setBusy(true)
    try {
      setMatches(await matchingApi.run(problemId))
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  async function runRecommendation() {
    setError('')
    setBusy(true)
    try {
      setRecommendation(await matchingApi.recommend(problemId))
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  async function runRecovery() {
    if (!missingCapability.trim()) return
    setError('')
    setBusy(true)
    try {
      setRecovery(await matchingApi.recovery(problemId, missingCapability.trim()))
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  const groups = {
    UNIVERSITY: matches.filter((m) => m.entityType === 'UNIVERSITY'),
    TEAM: matches.filter((m) => m.entityType === 'TEAM'),
    MENTOR: matches.filter((m) => m.entityType === 'MENTOR'),
    INDUSTRY: matches.filter((m) => m.entityType === 'INDUSTRY'),
  }

  return (
    <div className="space-y-5">
      <h1 className="text-lg font-semibold text-slate-800">AI Matching & Stakeholder Recommendation</h1>

      <div className="card flex flex-wrap items-end gap-3">
        <div className="min-w-64 flex-1">
          <label className="label" htmlFor="matching-challenge">Select a challenge</label>
          <select id="matching-challenge" className="input" value={problemId} onChange={(e) => setProblemId(e.target.value)}>
            <option value="">Choose...</option>
            {problems.map((p) => (
              <option key={p.id} value={p.id}>
                #{p.id} {p.title}
              </option>
            ))}
          </select>
        </div>
        <button type="button" className="btn-primary" onClick={() => load(problemId)} disabled={busy || !problemId}>
          Load
        </button>
        <button type="button" className="btn-primary" onClick={runMatching} disabled={busy || !problemId}>
          Run matching
        </button>
        <button type="button" className="btn-primary" onClick={runRecommendation} disabled={busy || !problemId}>
          Recommend
        </button>
      </div>

      <ErrorBanner message={error} />

      {problem && (
        <div className="card">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-lg font-semibold text-slate-800">{problem.title}</p>
              <p className="text-xs text-slate-500" aria-live="polite">
                Satisfies required skills: {asList(problem.aiSkills).join(', ') || 'analyzed after submit'}
              </p>
            </div>
            <Link to={`/challenges/${problem.id}`} className="text-sm text-brand-600 hover:underline">
              Challenge detail
            </Link>
          </div>
        </div>
      )}

      {matches.length > 0 && (
        <div className="grid gap-4 lg:grid-cols-2">
          {Object.entries(groups).map(([group, list]) => (
            <div key={group} className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">{group.replace('_', ' ').toLowerCase()}</h3>
              {list.length === 0 ? (
                <p className="text-xs text-slate-400" aria-live="polite">No candidates of this type.</p>
              ) : (
                <ul className="space-y-2">
                  {list.map((m) => (
                    <li key={`${m.entityType}-${m.entityId}`} className="rounded-lg border border-slate-100 p-3">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium text-slate-700">{m.entityName || `#${m.entityId}`}</span>
                        <ScoreMeter score={m.score} />
                      </div>
                      <p className="mt-1 text-xs text-slate-500">{m.explanation}</p>
                      <p className="mt-1 text-[10px] uppercase tracking-wide text-slate-400">
                        {aiSourceLabel(m.source)}
                      </p>
                      <p className="mt-1.5 flex flex-wrap gap-1.5">
                        {(m.contributingFactors || []).slice(0, 4).map((f) => (
                          <span key={f} className="rounded bg-slate-100 px-2 py-0.5 text-[10px] text-slate-500">
                            {f}
                          </span>
                        ))}
                      </p>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </div>
      )}

      {recommendation && (
        <div className="card border-brand-200 bg-brand-50/40">
          <h3 className="text-sm font-semibold text-slate-700">Recommended stakeholder team</h3>
          <p className="mt-1 text-xs text-brand-800" aria-live="polite">{recommendation.narrative}</p>
          <p className="mt-1 text-[10px] uppercase tracking-wide text-brand-500">
            {aiSourceLabel(recommendation.source)}
          </p>
          <div className="mt-3 grid gap-3 md:grid-cols-2 lg:grid-cols-4">
            {recommendation.stakeholders?.map((s) => (
              <div key={`${s.type}-${s.id}`} className="rounded-lg border border-brand-100 bg-white p-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-medium uppercase tracking-wide text-brand-500">{s.type}</span>
                  <ScoreMeter score={s.score} />
                </div>
                <p className="mt-1 text-sm font-semibold text-slate-800">{s.name}</p>
                <p className="mt-1 text-xs text-slate-500">{s.reason}</p>
                {s.gapSkills?.length > 0 && (
                  <p className="mt-1 text-xs text-amber-700">Gap: {s.gapSkills.join(', ')}</p>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="card">
        <h3 className="mb-2 text-sm font-semibold text-slate-700">Failure recovery</h3>
        <p id="recovery-help" className="mb-3 text-xs text-slate-500">
          If a pilot stalled because a capability is missing, ask the platform to find who can close the gap.
        </p>
        <div className="flex gap-2">
          <input
            className="input max-w-sm"
            aria-label="Missing capability"
            aria-describedby="recovery-help"
            placeholder="Missing capability, e.g. GIS"
            value={missingCapability}
            onChange={(e) => setMissingCapability(e.target.value)}
          />
          <button type="button" className="btn-secondary" onClick={runRecovery} disabled={busy || !problemId || !missingCapability.trim()}>
            Find recovery partners
          </button>
        </div>

        {recovery && (
          <div className="mt-4 rounded-lg border border-amber-200 bg-amber-50 p-4">
            <p className="text-xs text-amber-800" aria-live="polite">{recovery.narrative}</p>
            <p className="mt-1 text-[10px] uppercase tracking-wide text-amber-600">
              {aiSourceLabel(recovery.source)}
            </p>
            <ul className="mt-3 space-y-2">
              {recovery.recommendations?.map((r, i) => (
                <li key={`${r.id}-${i}`} className="flex items-center justify-between rounded-lg bg-white p-2.5">
                  <div>
                    <p className="text-sm font-medium text-slate-700">
                      {r.name} <span className="text-xs font-normal text-slate-400">({r.entityType})</span>
                    </p>
                    <p className="text-xs text-slate-500">{r.reason}</p>
                  </div>
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                      r.missingGapClosed ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                    }`}
                  >
                    {r.missingGapClosed ? 'Can close gap' : 'Partial'}
                  </span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  )
}