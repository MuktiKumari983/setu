import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { problemApi } from '../api/client'
import EmptyState from '../components/EmptyState'
import ErrorBanner from '../components/ErrorBanner'
import Spinner from '../components/Spinner'
import { errorMessage } from '../util'

export default function CapabilitiesPage() {
  const [groups, setGroups] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    problemApi
      .list({ status: 'VERIFIED' })
      .then((verified) => problemApi.list({ status: 'MATCHED' }).then((matched) => [...(verified || []), ...(matched || [])]))
      .then((problems) => {
        const unique = new Map()
        problems.forEach((problem) => unique.set(problem.id, problem))
        return Promise.all(
          [...unique.values()].map((problem) =>
            problemApi
              .capabilities(problem.id)
              .then((capabilities) => ({ problem, capabilities: capabilities || [] }))
              .catch(() => ({ problem, capabilities: [] })),
          ),
        )
      })
      .then((next) => setGroups(next.filter((group) => group.capabilities.length > 0)))
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-lg font-semibold text-slate-800">Capability decomposition</h1>
        <p className="mt-1 text-sm text-slate-500">
          Structured needs extracted from verified challenges and used for matching.
        </p>
      </div>

      <ErrorBanner message={error} />
      {loading && <Spinner label="Loading capabilities..." />}

      {!loading && groups.length === 0 && (
        <EmptyState
          title="No decomposed capabilities yet"
          description="Capabilities appear after a challenge has been analyzed and verified."
          to="/challenges"
          actionLabel="Browse challenges"
        />
      )}

      {!loading && groups.length > 0 && (
        <div className="grid gap-5 lg:grid-cols-2">
          {groups.map(({ problem, capabilities }) => (
            <section key={problem.id} className="card" aria-labelledby={`capability-problem-${problem.id}`}>
              <h2 id={`capability-problem-${problem.id}`} className="text-sm font-semibold text-slate-700">
                <Link to={`/challenges/${problem.id}`} className="hover:underline">
                  {problem.title}
                </Link>
              </h2>
              <ul className="mt-3 space-y-2">
                {capabilities.map((capability, i) => (
                  <li key={capability.id || `${problem.id}-${i}`} className="rounded-lg border border-slate-100 p-3">
                    <div className="flex items-center justify-between gap-2">
                      <p className="text-sm font-medium text-slate-700">{capability.capability}</p>
                      {capability.priority && (
                        <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-600">
                          {capability.priority}
                        </span>
                      )}
                    </div>
                    {capability.requiredSkills && (
                      <p className="mt-1 text-xs text-slate-500">Skills: {capability.requiredSkills}</p>
                    )}
                    {capability.description && (
                      <p className="mt-1 text-xs text-slate-500">{capability.description}</p>
                    )}
                  </li>
                ))}
              </ul>
            </section>
          ))}
        </div>
      )}
    </div>
  )
}
