import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function NotFoundPage() {
  const { user } = useAuth()

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="max-w-md text-center">
        <p className="text-sm font-semibold uppercase tracking-wide text-brand-600">404</p>
        <h1 className="mt-2 text-2xl font-semibold text-slate-900">This page could not be found</h1>
        <p className="mt-3 text-sm text-slate-600">
          The link may be outdated or the page may have moved.
        </p>
        <div className="mt-6 flex justify-center gap-3">
          <Link to="/" className="btn-secondary">
            Go home
          </Link>
          <Link to={user ? '/dashboard' : '/login'} className="btn-primary">
            {user ? 'Open dashboard' : 'Sign in'}
          </Link>
        </div>
      </div>
    </div>
  )
}
