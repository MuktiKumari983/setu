import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { projectApi, impactApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import StatusPill from '../components/StatusPill'
import { errorMessage, fmtDate, fmtDateTime } from '../util'

const STAGES = ['SUBMITTED', 'VERIFIED', 'MATCHED', 'TEAM_FORMED', 'PROPOSAL', 'PROTOTYPE', 'REVIEW', 'PILOT', 'IMPLEMENTATION', 'IMPACT', 'NEEDS_SUPPORT', 'COMPLETED']
const TASK_STATUSES = ['OPEN', 'IN_PROGRESS', 'DONE']
const MILESTONE_STATUSES = ['PENDING', 'IN_PROGRESS', 'COMPLETED']

const EDITORS = ['GOVERNMENT_ADMIN', 'PLATFORM_ADMIN', 'UNIVERSITY_ADMIN', 'FACULTY']

export default function ProjectDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const [project, setProject] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [newStatus, setNewStatus] = useState('')
  const [progress, setProgress] = useState(0)
  const [task, setTask] = useState({ title: '', description: '', priority: 'MEDIUM' })
  const [milestone, setMilestone] = useState({ title: '', targetDate: '' })
  const [comment, setComment] = useState({ content: '', commentType: 'GENERAL' })
  const [document, setDocument] = useState({ name: '', url: '' })
  const [failure, setFailure] = useState({ stage: '', reason: '', missingCapability: '' })
  const [recovery, setRecovery] = useState(null)
  const [impactRecords, setImpactRecords] = useState([])
  const [failureMode, setFailureMode] = useState(false)

  const canEdit = EDITORS.includes(user?.role) || user?.role === 'STUDENT'

  function load() {
    setError('')
    projectApi
      .get(id)
      .then((p) => {
        setProject(p)
        setNewStatus(p.status)
        setProgress(p.progressPercent || 0)
      })
      .catch((e) => setError(errorMessage(e)))
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  useEffect(() => {
    if (project && ['IMPACT', 'COMPLETED'].includes(project.status)) {
      impactApi
        .forProject(id)
        .then(setImpactRecords)
        .catch(() => {})
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [project])

  async function run(fn, then) {
    setBusy(true)
    setError('')
    try {
      const res = await fn()
      if (then === 'setRecovery') setRecovery(res)
      else load()
    } catch (e) {
      setError(errorMessage(e))
    } finally {
      setBusy(false)
    }
  }

  async function changeStatus() {
    if (!newStatus || newStatus === project.status) return
    await run(() => projectApi.changeStatus(id, { status: newStatus, note: `Moved to ${newStatus} by ${user?.name}` }))
  }

  async function saveProgress() {
    await run(() => projectApi.progress(id, progress))
  }

  if (!project && !error) return <p className="text-sm text-slate-500">Loading...</p>

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <Link to="/projects" className="text-sm text-brand-600 hover:underline">
          &larr; All projects
        </Link>
        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-400">Created {fmtDateTime(project?.createdAt)}</span>
          <StatusPill value={project?.status} />
        </div>
      </div>

      {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      {project && (
        <>
          <div className="card">
            <h1 className="text-lg font-semibold text-slate-800">{project.title}</h1>
            {project.problemId && (
              <Link to={`/challenges/${project.problemId}`} className="text-sm text-brand-600 hover:underline">
                Challenge: {project.problemTitle}
              </Link>
            )}
            {project.failureReason && (
              <p className="mt-2 rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">
                Blocked: {project.failureReason}
              </p>
            )}
            <dl className="mt-4 grid grid-cols-2 gap-3 text-sm md:grid-cols-4">
              <div>
                <dt className="text-xs text-slate-400">University</dt>
                <dd className="font-medium">{project.university?.name || '-'}</dd>
              </div>
              <div>
                <dt className="text-xs text-slate-400">Student team</dt>
                <dd className="font-medium">{project.team?.name || '-'}</dd>
              </div>
              <div>
                <dt className="text-xs text-slate-400">Lead mentor</dt>
                <dd className="font-medium">{project.mentor?.name || '-'}</dd>
              </div>
              <div>
                <dt className="text-xs text-slate-400">Industry partner</dt>
                <dd className="font-medium">{project.industry?.name || '-'}</dd>
              </div>
            </dl>
            {project.members?.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {project.members.map((m) => (
                  <span key={m.id} className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs text-slate-600">
                    {m.name} ({m.role})
                  </span>
                ))}
              </div>
            )}
          </div>

          {canEdit && (
            <div className="card grid gap-4 md:grid-cols-2">
              <div>
                <label className="label">Lifecycle status</label>
                <div className="flex gap-2">
                  <select className="input" value={newStatus} onChange={(e) => setNewStatus(e.target.value)}>
                    {STAGES.map((s) => (
                      <option key={s}>{s}</option>
                    ))}
                  </select>
                  <button className="btn-primary" onClick={changeStatus} disabled={busy || newStatus === project.status}>
                    Update
                  </button>
                </div>
              </div>
              <div>
                <label className="label">Implementation progress (points)</label>
                <div className="flex items-center gap-2">
                  <input type="range" min={0} max={100} step={5} value={progress} onChange={(e) => setProgress(Number(e.target.value))} className="w-full" />
                  <span className="text-sm font-semibold text-slate-600">{progress}%</span>
                  <button className="btn-secondary" onClick={saveProgress} disabled={busy || progress === (project.progressPercent || 0)}>
                    Save
                  </button>
                </div>
              </div>
            </div>
          )}

          <div className="grid gap-5 lg:grid-cols-2">
            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">Tasks</h3>
              {canEdit && (
                <form
                  className="mb-3 grid gap-2"
                  onSubmit={(e) => {
                    e.preventDefault()
                    run(() => projectApi.addTask(id, task)).then(() => setTask({ title: '', description: '', priority: 'MEDIUM' }))
                  }}
                >
                  <input className="input" placeholder="Task title" value={task.title} onChange={(e) => setTask((t) => ({ ...t, title: e.target.value }))} required />
                  <div className="flex gap-2">
                    <input className="input" placeholder="Details" value={task.description} onChange={(e) => setTask((t) => ({ ...t, description: e.target.value }))} />
                    <select className="input w-32" value={task.priority} onChange={(e) => setTask((t) => ({ ...t, priority: e.target.value }))}>
                      <option>LOW</option>
                      <option>MEDIUM</option>
                      <option>HIGH</option>
                    </select>
                    <button className="btn-primary">Add</button>
                  </div>
                </form>
              )}
              <ul className="space-y-2">
                {project.tasks?.map((t) => (
                  <li key={t.id} className="flex items-center justify-between rounded-lg border border-slate-100 p-2.5">
                    <div>
                      <p className="text-sm font-medium text-slate-700">{t.title}</p>
                      {t.description && <p className="text-xs text-slate-400">{t.description}</p>}
                      {t.assignedToName && <p className="text-xs text-slate-500">Assigned: {t.assignedToName}</p>}
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusPill value={t.status} />
                      {canEdit && (
                        <select
                          className="rounded border border-slate-200 px-1.5 py-1 text-xs"
                          value={t.status}
                          onChange={(e) => run(() => projectApi.updateTask(id, t.id, { status: e.target.value }))}
                        >
                          {TASK_STATUSES.map((s) => (
                            <option key={s}>{s}</option>
                          ))}
                        </select>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </div>

            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">Milestones</h3>
              {canEdit && (
                <form
                  className="mb-3 flex gap-2"
                  onSubmit={(e) => {
                    e.preventDefault()
                    run(() => projectApi.addMilestone(id, milestone)).then(() => setMilestone({ title: '', targetDate: '' }))
                  }}
                >
                  <input className="input" placeholder="Milestone title" value={milestone.title} onChange={(e) => setMilestone((m) => ({ ...m, title: e.target.value }))} required />
                  <input className="input w-40" type="date" value={milestone.targetDate} onChange={(e) => setMilestone((m) => ({ ...m, targetDate: e.target.value }))} />
                  <button className="btn-primary">Add</button>
                </form>
              )}
              <ul className="space-y-2">
                {project.milestones?.map((m) => (
                  <li key={m.id} className="flex items-center justify-between rounded-lg border border-slate-100 p-2.5">
                    <div>
                      <p className="text-sm font-medium text-slate-700">{m.title}</p>
                      <p className="text-xs text-slate-400">Target: {fmtDate(m.targetDate)}{m.completedAt ? ` - completed ${fmtDate(m.completedAt)}` : ''}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <StatusPill value={m.status} />
                      {canEdit && (
                        <select
                          className="rounded border border-slate-200 px-1.5 py-1 text-xs"
                          value={m.status}
                          onChange={(e) => run(() => projectApi.updateMilestone(id, m.id, { status: e.target.value }))}
                        >
                          {MILESTONE_STATUSES.map((s) => (
                            <option key={s}>{s}</option>
                          ))}
                        </select>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </div>

            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">Comments & collaboration</h3>
              <form
                className="mb-3 flex gap-2"
                onSubmit={(e) => {
                  e.preventDefault()
                  run(() => projectApi.addComment(id, comment)).then(() => setComment({ content: '', commentType: 'GENERAL' }))
                }}
              >
                <select className="input w-36" value={comment.commentType} onChange={(e) => setComment((c) => ({ ...c, commentType: e.target.value }))}>
                  <option>GENERAL</option>
                  <option>MENTOR</option>
                  <option>INDUSTRY</option>
                </select>
                <input className="input" placeholder="Add a comment" value={comment.content} onChange={(e) => setComment((c) => ({ ...c, content: e.target.value }))} required />
                <button className="btn-primary">Post</button>
              </form>
              <ul className="space-y-2">
                {project.comments?.map((c) => (
                  <li key={c.id} className="rounded-lg bg-slate-50 p-2.5">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-medium text-slate-600">{c.author}</span>
                      <StatusPill value={c.commentType} />
                    </div>
                    <p className="mt-1 text-sm text-slate-700">{c.content}</p>
                    <p className="mt-1 text-xs text-slate-400">{fmtDateTime(c.createdAt)}</p>
                  </li>
                ))}
              </ul>
            </div>

            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">Documents</h3>
              <form
                className="mb-3 flex gap-2"
                onSubmit={(e) => {
                  e.preventDefault()
                  run(() => projectApi.addDocument(id, document)).then(() => setDocument({ name: '', url: '' }))
                }}
              >
                <input className="input" placeholder="Document name" value={document.name} onChange={(e) => setDocument((d) => ({ ...d, name: e.target.value }))} required />
                <input className="input" placeholder="/files/report.pdf" value={document.url} onChange={(e) => setDocument((d) => ({ ...d, url: e.target.value }))} />
                <button className="btn-primary">Add</button>
              </form>
              <ul className="space-y-1.5">
                {project.documents?.map((d) => (
                  <li key={d.id} className="flex items-center justify-between text-sm">
                    <a href={d.url} className="font-medium text-brand-600 hover:underline">
                      {d.name}
                    </a>
                    <span className="text-xs text-slate-400">by {d.uploader}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          <div className="card">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold text-slate-700">
                {project.status === 'NEEDS_SUPPORT' ? 'Recovery: report or resolve failure' : 'Failure recovery'}
              </h3>
              {!failureMode ? (
                <button className="btn-secondary" onClick={() => setFailureMode(true)}>
                  Report a failure
                </button>
              ) : (
                <button className="btn-secondary" onClick={() => setFailureMode(false)}>
                  Close
                </button>
              )}
            </div>

            {failureMode && (
              <form
                className="mt-3 grid gap-2 md:grid-cols-3"
                onSubmit={(e) => {
                  e.preventDefault()
                  run(() => projectApi.reportFailure(id, failure), 'setRecovery').then(() => {
                    setFailure({ stage: '', reason: '', missingCapability: '' })
                    setFailureMode(false)
                  })
                }}
              >
                <select className="input" value={failure.stage} onChange={(e) => setFailure((f) => ({ ...f, stage: e.target.value }))}>
                  <option value="">Stage</option>
                  {STAGES.map((s) => (
                    <option key={s}>{s}</option>
                  ))}
                </select>
                <input className="input" placeholder="What went wrong" value={failure.reason} onChange={(e) => setFailure((f) => ({ ...f, reason: e.target.value }))} required />
                <input className="input" placeholder="Missing capability (e.g. GIS)" value={failure.missingCapability} onChange={(e) => setFailure((f) => ({ ...f, missingCapability: e.target.value }))} />
                <button className="btn-danger md:col-span-3">Report failure & fetch recovery plan</button>
              </form>
            )}

            {project.status === 'NEEDS_SUPPORT' && (
              <div className="mt-3 flex flex-wrap items-center gap-2">
                <button className="btn-primary" onClick={() => run(() => projectApi.resolveFailure(id))}>
                  Resolve failure (restore to last stage)
                </button>
                <button className="btn-secondary" onClick={() => run(() => projectApi.recovery(id), 'setRecovery')}>
                  Re-run recovery plan
                </button>
              </div>
            )}

            {recovery && (
              <div className="mt-4 rounded-lg border border-amber-200 bg-amber-50 p-4">
                <h4 className="text-sm font-semibold text-amber-900">Recovery plan</h4>
                <p className="mt-1 text-xs text-amber-800">{recovery.narrative}</p>
                <ul className="mt-3 space-y-2">
                  {recovery.recommendations?.map((r, i) => (
                    <li key={`${r.id}-${i}`} className="flex items-center justify-between rounded-lg bg-white p-2.5">
                      <div>
                        <p className="text-sm font-medium text-slate-700">
                          {r.name} <span className="text-xs font-normal text-slate-400">({r.entityType} - {Math.round(r.score * 100)}% match)</span>
                        </p>
                        <p className="text-xs text-slate-500">{r.reason}</p>
                      </div>
                      {r.missingGapClosed ? (
                        <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-xs font-semibold text-emerald-700">
                          Gap closed
                        </span>
                      ) : (
                        <span className="rounded-full bg-slate-100 px-2.5 py-0.5 text-xs text-slate-500">Partial</span>
                      )}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>

          {(project.history?.length > 0) && (
            <div className="card">
              <h3 className="mb-3 text-sm font-semibold text-slate-700">Lifecycle timeline</h3>
              <ol className="space-y-3 border-l border-slate-200 pl-4">
                {project.history.map((h, i) => (
                  <li key={i} className="relative">
                    <span className="absolute -left-[21px] top-1 h-2.5 w-2.5 rounded-full bg-brand-400" />
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium text-slate-700">{h.status}</span>
                      <span className="text-xs text-slate-400">{fmtDateTime(h.changedAt)}</span>
                    </div>
                    {h.note && <p className="text-xs text-slate-500">{h.note}</p>}
                    {h.changedBy && <p className="text-xs text-slate-400">by {h.changedBy}</p>}
                  </li>
                ))}
              </ol>
            </div>
          )}

          <div className="card">
            <h3 className="mb-2 text-sm font-semibold text-slate-700">Social impact record</h3>
            {['IMPACT', 'COMPLETED'].includes(project.status) && impactRecords.length > 0 ? (
              <ul className="space-y-2">
                {impactRecords.map((r) => (
                  <li key={r.id} className="rounded-lg border border-emerald-100 bg-emerald-50/50 p-3 text-sm">
                    <div className="flex items-center justify-between">
                      <span className="font-semibold text-emerald-800">
                        {r.beneficiaries} beneficiaries affected
                      </span>
                      <StatusPill value={r.verificationStatus} />
                    </div>
                    <p className="mt-1 text-xs text-emerald-700">
                      {r.locationsCovered} locations, {r.problemReduction || 'reduction reported'}
                    </p>
                  </li>
                ))}
              </ul>
            ) : (
              <Link to="/impact" className="text-sm text-brand-600 hover:underline">
                View impact records & submit evidence
              </Link>
            )}
          </div>
        </>
      )}
    </div>
  )
}