import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const FEATURES = [
  { title: 'Capture challenges', text: 'Citizens submit verified local problems with structured AI analysis.' },
  { title: 'Match the right partners', text: 'Universities, teams, faculty, and industries are scored against real capability needs.' },
  { title: 'Deliver with evidence', text: 'Projects track lifecycle stages and record impact with reviewable evidence.' },
]

export default function LandingPage() {
  const { user } = useAuth()

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
          <Link to="/" className="flex items-center gap-2" aria-label="Setu home">
            <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-sm font-bold text-white">
              S
            </span>
            <span className="font-semibold text-slate-800">Setu</span>
          </Link>
          <nav className="flex items-center gap-2" aria-label="Account navigation">
            <Link to={user ? '/dashboard' : '/login'} className="btn-secondary">
              {user ? 'Dashboard' : 'Sign in'}
            </Link>
            {!user && (
              <Link to="/register" className="btn-primary">
                Register
              </Link>
            )}
          </nav>
        </div>
      </header>

      <main id="main-content" tabIndex={-1} className="mx-auto max-w-6xl px-4">
        <section className="grid gap-10 py-16 lg:grid-cols-2 lg:items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wide text-brand-600">
              Societal Innovation Collaboration Platform
            </p>
            <h1 className="mt-3 text-4xl font-semibold leading-tight text-slate-900">
              Turn local challenges into measurable public impact
            </h1>
            <p className="mt-4 text-base text-slate-600">
              Setu connects citizens, academia, government, and industry in one verified workflow — from problem
              discovery to matching, delivery, and evidence-backed impact.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to={user ? '/dashboard' : '/register'} className="btn-primary">
                {user ? 'Open dashboard' : 'Get started'}
              </Link>
              <Link to={user ? '/challenges' : '/login'} className="btn-secondary">
                {user ? 'Browse challenges' : 'Explore sign in'}
              </Link>
            </div>
          </div>
          <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
            <h2 className="text-sm font-semibold text-slate-700">One connected workflow</h2>
            <ol className="mt-5 space-y-4">
              {['Challenge verified', 'Capability match', 'Project delivered', 'Impact verified'].map((step, i) => (
                <li key={step} className="flex items-center gap-3">
                  <span className="flex h-7 w-7 items-center justify-center rounded-full bg-brand-50 text-xs font-bold text-brand-700">
                    {i + 1}
                  </span>
                  <span className="text-sm text-slate-700">{step}</span>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section className="grid gap-4 pb-16 md:grid-cols-3" aria-label="Platform features">
          {FEATURES.map((feature) => (
            <article key={feature.title} className="card">
              <h2 className="text-sm font-semibold text-slate-800">{feature.title}</h2>
              <p className="mt-2 text-sm text-slate-600">{feature.text}</p>
            </article>
          ))}
        </section>
      </main>
    </div>
  )
}
