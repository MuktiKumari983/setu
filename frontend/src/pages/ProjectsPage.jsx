import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { projectApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { can } from '../auth/roles'
import EmptyState from '../components/EmptyState'
import ErrorBanner from '../components/ErrorBanner'
import Spinner from '../components/Spinner'
import StatusPill from '../components/StatusPill'
import { errorMessage } from '../util'

export default function ProjectsPage() {
  const { user } = useAuth()
  const [projects, setProjects] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    projectApi
      .list()
      .then(setProjects)
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-lg font-semibold text-slate-800">Innovation Projects</h1>
          <p className="mt-1 text-sm text-slate-500">Delivery workspaces created from verified challenges.</p>
        </div>
        {can(user, 'createProjects') && (
          <Link to="/projects/new" className="btn-primary">
            New project
          </Link>
        )}
      </div>

      <ErrorBanner message={error} />
      {loading && <Spinner label="Loading projects..." />}

      {!loading && projects.length === 0 && (
        <EmptyState
          title="No projects yet"
          description="Projects form after a challenge is verified or matched."
          to={can(user, 'createProjects') ? '/projects/new' : '/challenges'}
          actionLabel={can(user, 'createProjects') ? 'Create a project' : 'Browse challenges'}
        />
      )}

      {!loading && projects.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full min-w-0 text-sm">
            <thead>
              <tr className="border-b border-slate-200">
                <th className="table-head py-2 pr-4">Project</th>
                <th className="table-head py-2 pr-4">Challenge</th>
                <th className="table-head py-2 pr-4">University</th>
                <th className="table-head py-2 pr-4">Team</th>
                <th className="table-head py-2 pr-4">Status</th>
                <th className="table-head py-2">Progress</th>
              </tr>
            </thead>
            <tbody>
              {projects.map((p) => (
                <tr key={p.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-3 pr-4">
                    <Link to={`/projects/${p.id}`} className="font-medium text-brand-600 hover:underline">
                      {p.title}
                    </Link>
                  </td>
                  <td className="py-3 pr-4 text-slate-500">{p.problemTitle}</td>
                  <td className="py-3 pr-4 text-slate-500">{p.university?.name || '-'}</td>
                  <td className="py-3 pr-4 text-slate-500">{p.team?.name || '-'}</td>
                  <td className="py-3 pr-4">
                    <StatusPill value={p.status} />
                  </td>
                  <td className="py-3">
                    <div className="flex items-center gap-2">
                      <div
                        className="h-2 w-20 overflow-hidden rounded-full bg-slate-200"
                        role="progressbar"
                        aria-valuenow={p.progressPercent || 0}
                        aria-valuemin={0}
                        aria-valuemax={100}
                        aria-label={`${p.title} progress`}
                      >
                        <div className="h-full rounded-full bg-brand-500" style={{ width: `${p.progressPercent || 0}%` }} />
                      </div>
                      <span className="text-xs font-medium text-slate-500">{p.progressPercent || 0}%</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
