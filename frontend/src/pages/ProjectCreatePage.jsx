import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { problemApi, projectApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { can } from '../auth/roles'
import EmptyState from '../components/EmptyState'
import ErrorBanner from '../components/ErrorBanner'
import FormField from '../components/FormField'
import Spinner from '../components/Spinner'
import SuccessBanner from '../components/SuccessBanner'
import { errorMessage } from '../util'

export default function ProjectCreatePage() {
  const { user } = useAuth()
  const [problems, setProblems] = useState([])
  const [form, setForm] = useState({ problemId: '', description: '' })
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [createdId, setCreatedId] = useState(null)

  useEffect(() => {
    if (!can(user, 'createProjects')) {
      setLoading(false)
      return
    }
    Promise.all([problemApi.list({ status: 'VERIFIED' }), problemApi.list({ status: 'MATCHED' })])
      .then(([verified, matched]) => {
        const unique = new Map()
        const candidates = [...(verified || []), ...(matched || [])]
        candidates.forEach((problem) => unique.set(problem.id, problem))
        setProblems([...unique.values()])
      })
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
  }, [user])

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    setCreatedId(null)
    try {
      const project = await projectApi.create({
        problemId: Number(form.problemId),
        description: form.description || null,
      })
      setCreatedId(project.id)
      setForm({ problemId: '', description: '' })
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  if (!can(user, 'createProjects')) {
    return (
      <EmptyState
        title="Project creation is not available for this role"
        description="Ask a coordinator or administrator to create the project workspace."
        to="/projects"
        actionLabel="Back to projects"
      />
    )
  }

  return (
    <div className="mx-auto max-w-2xl space-y-5">
      <div>
        <Link to="/projects" className="text-sm text-brand-600 hover:underline">
          &larr; All projects
        </Link>
        <h1 className="mt-2 text-lg font-semibold text-slate-800">Create project workspace</h1>
      </div>

      <ErrorBanner message={error} />
      <SuccessBanner
        message={
          createdId
            ? `Project #${createdId} created. Open the workspace to assign tasks and track delivery.`
            : ''
        }
      />
      {createdId && (
        <Link to={`/projects/${createdId}`} className="text-sm font-medium text-brand-600 hover:underline">
          Open project #{createdId}
        </Link>
      )}

      <div className="card">
        {loading ? (
          <Spinner label="Loading verified challenges..." />
        ) : problems.length === 0 ? (
          <EmptyState
            title="No verified challenges available"
            description="A challenge must be VERIFIED or MATCHED before a project can be created."
            to="/challenges"
            actionLabel="Browse challenges"
          />
        ) : (
          <form onSubmit={submit} className="space-y-4">
            <FormField id="project-problem" label="Verified challenge" hint="The project title and matching stakeholders are derived from this challenge.">
              <select
                id="project-problem"
                className="input"
                value={form.problemId}
                onChange={(e) => setForm((f) => ({ ...f, problemId: e.target.value }))}
                aria-describedby="project-problem-hint"
                required
              >
                <option value="">Select a challenge...</option>
                {problems.map((p) => (
                  <option key={p.id} value={p.id}>
                    #{p.id} — {p.title}
                  </option>
                ))}
              </select>
            </FormField>
            <FormField id="project-description" label="Project description" hint="Optional. Defaults to a generated workspace description.">
              <textarea
                id="project-description"
                className="input min-h-24"
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                aria-describedby="project-description-hint"
                maxLength={2000}
              />
            </FormField>
            <button className="btn-primary" disabled={busy || !form.problemId}>
              {busy ? 'Creating...' : 'Create project'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}
