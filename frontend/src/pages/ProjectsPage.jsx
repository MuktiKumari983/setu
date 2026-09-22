import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { projectApi } from '../api/client'
import StatusPill from '../components/StatusPill'
import { errorMessage, fmtDateTime } from '../util'

export default function ProjectsPage() {
  const [projects, setProjects] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    projectApi
      .list()
      .then(setProjects)
      .catch((e) => setError(errorMessage(e)))
  }, [])

  return (
    <div className="space-y-5">
      <h1 className="text-lg font-semibold text-slate-800">Innovation Projects</h1>
      {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      {projects.length === 0 ? (
        <p className="text-sm text-slate-400">No projects yet. Projects form after a challenge is matched.</p>
      ) : (
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
                      <div className="h-2 w-20 overflow-hidden rounded-full bg-slate-200">
                        <div className="h-full rounded-full bg-brand-500" style={{ width: `${p.progressPercent || 0}%` }} />
                      </div>
                      <span className="text-xs font-medium text-slate-500">{p.progressPercent}%</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <p className="text-xs text-slate-400">{fmtDateTime(new Date().toISOString())}</p>
    </div>
  )
}