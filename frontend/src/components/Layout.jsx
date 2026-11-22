import { NavLink, Link, Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../auth/AuthContext'
import { can, roleLabel, visibleNavItems } from '../auth/roles'
import { notificationApi } from '../api/client'

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [unread, setUnread] = useState(0)
  const [menuOpen, setMenuOpen] = useState(false)
  const navItems = visibleNavItems(user)

  useEffect(() => {
    if (user) {
      notificationApi
        .unreadCount()
        .then((r) => setUnread(r.count || 0))
        .catch(() => {})
    }
  }, [user])

  useEffect(() => {
    setMenuOpen(false)
  }, [location.pathname])

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:left-4 focus:top-4 focus:z-50 focus:rounded-lg focus:bg-white focus:px-3 focus:py-2 focus:text-sm focus:font-medium focus:text-brand-700 focus:shadow"
      >
        Skip to main content
      </a>
      <header className="sticky top-0 z-20 border-b border-slate-200 bg-white">
        <div className="mx-auto flex h-14 max-w-7xl items-center justify-between px-4">
          <div className="flex items-center gap-3">
            <button
              type="button"
              className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 md:hidden"
              onClick={() => setMenuOpen((open) => !open)}
              aria-expanded={menuOpen}
              aria-controls="primary-navigation"
              aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
            >
              <span className="block h-4 w-4 border-y-2 border-current text-xs" aria-hidden="true" />
              <span className="sr-only">Toggle navigation</span>
            </button>
            <Link to="/dashboard" className="flex items-center gap-2" aria-label="Setu dashboard">
              <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                S
              </span>
              <span className="font-semibold text-slate-800">Setu</span>
            </Link>
            <nav id="primary-navigation" className="hidden items-center gap-1 md:flex" aria-label="Primary navigation">
              {navItems.map((item) => (
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
              <Link
                to="/profile"
                className="rounded-full bg-rose-100 px-2 py-0.5 text-xs font-semibold text-rose-700"
                aria-label={`${unread} unread notifications`}
              >
                {unread} new
              </Link>
            )}
            {can(user, 'createProjects') && (
              <Link to="/projects/new" className="btn-primary hidden lg:inline-block">
                New project
              </Link>
            )}
            <Link to="/profile" className="flex items-center gap-2" aria-label={`Profile: ${user?.name || 'Member'}`}>
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-800 text-xs font-bold text-white">
                {(user?.name || 'U').slice(0, 1).toUpperCase()}
              </span>
              <span className="hidden text-sm font-medium text-slate-700 sm:block">{roleLabel(user?.role)}</span>
            </Link>
            <button type="button" onClick={handleLogout} className="btn-secondary">
              Logout
            </button>
          </div>
        </div>

        {menuOpen && (
          <nav className="border-t border-slate-200 bg-white px-4 py-2 md:hidden" aria-label="Mobile navigation">
            <div className="mx-auto flex max-w-7xl flex-col gap-1">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-2 text-sm font-medium ${
                      isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-100'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
              {can(user, 'createProjects') && (
                <Link to="/projects/new" className="rounded-lg px-3 py-2 text-sm font-medium text-brand-700">
                  New project
                </Link>
              )}
            </div>
          </nav>
        )}
      </header>

      <main id="main-content" tabIndex={-1} className="mx-auto max-w-7xl px-4 py-6 outline-none">
        <Outlet />
      </main>
    </div>
  )
}
