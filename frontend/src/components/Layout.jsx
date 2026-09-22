import { NavLink, Link, Outlet, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { notificationApi } from '../api/client'

const NAV = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/challenges', label: 'Challenges' },
  { to: '/projects', label: 'Projects' },
  { to: '/matching', label: 'Matching' },
  { to: '/impact', label: 'Impact' },
  { to: '/analytics', label: 'Analytics' },
]

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)

  useEffect(() => {
    if (user) {
      notificationApi
        .unreadCount()
        .then((r) => setUnread(r.count || 0))
        .catch(() => {})
    }
  }, [user])

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-20 border-b border-slate-200 bg-white">
        <div className="mx-auto flex h-14 max-w-7xl items-center justify-between px-4">
          <div className="flex items-center gap-8">
            <Link to="/dashboard" className="flex items-center gap-2">
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                S
              </span>
              <span className="font-semibold text-slate-800">Setu</span>
            </Link>
            <nav className="hidden items-center gap-1 md:flex">
              {NAV.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-1.5 text-sm font-medium ${
                      isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-3">
            {unread > 0 && (
              <span className="rounded-full bg-rose-100 px-2 py-0.5 text-xs font-semibold text-rose-700">
                {unread} new
              </span>
            )}
            <Link to="/profile" className="flex items-center gap-2">
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-800 text-xs font-bold text-white">
                {(user?.name || 'U').slice(0, 1).toUpperCase()}
              </span>
              <span className="hidden text-sm font-medium text-slate-700 sm:block">{user?.name}</span>
            </Link>
            <button onClick={handleLogout} className="btn-secondary">
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}