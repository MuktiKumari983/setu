import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { dashboardApi, problemApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import StatCard from '../components/StatCard'
import StatusPill from '../components/StatusPill'
import { errorMessage, fmtDateTime } from '../util'

export default function DashboardPage() {
  const { user } = useAuth()
  const [analytics, setAnalytics] = useState(null)
  const [problems, setProblems] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([dashboardApi.analytics(), problemApi.list({ status: 'SUBMITTED' })])
      .then(([a, p]) => {
        setAnalytics(a)
        setProblems(p)
      })
      .catch((e) => setError(errorMessage(e)))
  }, [])

  const isVerifier = ['GOVERNMENT_ADMIN', 'PLATFORM_ADMIN'].includes(user?.role)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-lg font-semibold text-slate-800">Welcome, {user?.name}</h1>
        <p className="text-sm text-slate-500">Role: {user?.role?.replace('_', ' ')}</p>
      </div>

      {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      {analytics && (
        <>
          <div className="grid grid-cols-2 gap-3 md:grid-cols-4 lg:grid-cols-6">
            <StatCard label="Challenges" value={analytics.totalChallenges} />
            <StatCard label="Verified" value={analytics.verifiedChallenges} tone="#0ea5e9" />
            <StatCard label="Matched" value={analytics.matchedChallenges} tone="#6366f1" />
            <StatCard label="Active projects" value={analytics.activeProjects} tone="#059669" />
            <StatCard label="Implemented" value={analytics.implementedProjects} tone="#16a34a" />
            <StatCard label="Needs support" value={analytics.projectsNeedingSupport} tone="#dc2626" />
            <StatCard label="Verified beneficiaries" value={analytics.verifiedBeneficiaries} tone="#15803d" />
            <StatCard label="Full implementation %" value={analytics.implementationRate ? `${Math.round(analytics.implementationRate * 100)}%` : '0%'} tone="#0d9488" />
          </div>

          <div className="grid gap-4 lg:grid-cols-2">
            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">Top challenge categories</h3>
              <div className="space-y-2">
                {Object.entries(analytics.byCategory || {})
                  .sort((a, b) => b[1] - a[1])
                  .slice(0, 6)
                  .map(([name, count]) => (
                    <div key={name} className="flex items-center justify-between">
                      <span className="text-sm text-slate-600">{name}</span>
                      <div className="flex items-center gap-3">
                        <div className="h-2 w-40 overflow-hidden rounded-full bg-slate-100">
                          <div
                            className="h-full rounded-full bg-brand-500"
                            style={{ width: `${(count / Math.max(1, Math.max(...Object.values(analytics.byCategory)))) * 100}%` }}
                          />
                        </div>
                        <span className="w-6 text-right text-xs font-semibold text-slate-500">{count}</span>
                      </div>
                    </div>
                  ))}
              </div>
            </div>

            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">Challenges awaiting verification</h3>
              {problems.length === 0 ? (
                <p className="text-sm text-slate-400">No submitted challenges right now.</p>
              ) : (
                <ul className="divide-y divide-slate-100">
                  {problems.slice(0, 8).map((p) => (
                    <li key={p.id} className="flex items-center justify-between py-2">
                      <Link to={`/challenges/${p.id}`} className="text-sm font-medium text-slate-700 hover:text-brand-600">
                        {p.title}
                      </Link>
                      <div className="flex items-center gap-3">
                        <span className="text-xs text-slate-400">{fmtDateTime(p.createdAt)}</span>
                        <StatusPill value={p.status} />
                      </div>
                    </li>
                  ))}
                </ul>
              )}
              {isVerifier && (
                <Link to="/challenges" className="btn-secondary mt-3">
                  Review challenges
                </Link>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  )
}