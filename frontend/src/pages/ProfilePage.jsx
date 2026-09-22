import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { notificationApi } from '../api/client'
import StatusPill from '../components/StatusPill'
import { errorMessage, fmtDateTime } from '../util'

export default function ProfilePage() {
  const { user } = useAuth()
  const [notifications, setNotifications] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    notificationApi
      .list()
      .then(setNotifications)
      .catch((e) => setError(errorMessage(e)))
  }, [])

  async function markRead(id) {
    try {
      await notificationApi.markRead(id)
      setNotifications((list) => list.map((n) => (n.id === id ? { ...n, read: true } : n)))
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  return (
    <div className="space-y-5">
      <h1 className="text-lg font-semibold text-slate-800">My profile</h1>

      <div className="card grid gap-4 md:grid-cols-3">
        <div>
          <label className="label">Name</label>
          <p className="text-sm font-medium text-slate-700">{user?.name}</p>
        </div>
        <div>
          <label className="label">Email</label>
          <p className="text-sm font-medium text-slate-700">{user?.email}</p>
        </div>
        <div>
          <label className="label">Role</label>
          <p className="text-sm font-medium text-slate-700">{user?.role?.replace('_', ' ')}</p>
        </div>
      </div>

      {error && <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{error}</p>}

      <div className="card">
        <h3 className="mb-3 text-sm font-semibold text-slate-700">Notifications</h3>
        {notifications.length === 0 ? (
          <p className="text-sm text-slate-400">No notifications yet.</p>
        ) : (
          <ul className="divide-y divide-slate-100">
            {notifications.map((n) => (
              <li key={n.id} className={`flex items-start justify-between gap-3 py-2.5 ${n.read ? 'opacity-60' : ''}`}>
                <div>
                  <div className="flex items-center gap-2">
                    <StatusPill value={n.type} />
                    <span className="text-sm font-medium text-slate-700">{n.title}</span>
                  </div>
                  <p className="mt-0.5 text-xs text-slate-500">{n.message}</p>
                  <p className="mt-0.5 text-xs text-slate-400">{fmtDateTime(n.createdAt)}</p>
                </div>
                {!n.read && (
                  <button className="btn-secondary !px-2 !py-1 text-xs" onClick={() => markRead(n.id)}>
                    Mark read
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="card">
        <h3 className="mb-2 text-sm font-semibold text-slate-700">Where to go next</h3>
        <ul className="space-y-1 text-sm text-slate-600">
          <li><Link className="text-brand-600 hover:underline" to="/challenges">- Browse & submit community challenges</Link></li>
          <li><Link className="text-brand-600 hover:underline" to="/matching">- Run AI matching and get a recommended stakeholder team</Link></li>
          <li><Link className="text-brand-600 hover:underline" to="/projects">- Manage project workspace, tasks and milestones</Link></li>
          <li><Link className="text-brand-600 hover:underline" to="/impact">- Submit verified social impact evidence</Link></li>
          <li><Link className="text-brand-600 hover:underline" to="/analytics">- View platform-level analytics</Link></li>
        </ul>
      </div>
    </div>
  )
}