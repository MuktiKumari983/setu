import { useEffect, useState } from 'react'
import { catalogApi } from '../api/client'
import EmptyState from '../components/EmptyState'
import ErrorBanner from '../components/ErrorBanner'
import Spinner from '../components/Spinner'
import { errorMessage } from '../util'

const SECTIONS = [
  {
    key: 'universities',
    title: 'Universities',
    load: catalogApi.universities,
    render: (item) => ({ title: item.name, detail: item.location, meta: item.description }),
  },
  {
    key: 'teams',
    title: 'Student teams',
    load: catalogApi.teams,
    render: (item) => ({ title: item.name, detail: item.university?.name, meta: item.skills }),
  },
  {
    key: 'faculty',
    title: 'Faculty mentors',
    load: catalogApi.faculty,
    render: (item) => ({ title: item.name, detail: item.title, meta: item.expertise }),
  },
  {
    key: 'industries',
    title: 'Industry partners',
    load: catalogApi.industries,
    render: (item) => ({ title: item.name, detail: item.sector, meta: item.expertise }),
  },
]

export default function CatalogPage() {
  const [data, setData] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all(SECTIONS.map((section) => section.load()))
      .then((results) => {
        const next = {}
        SECTIONS.forEach((section, i) => {
          next[section.key] = results[i] || []
        })
        setData(next)
      })
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-lg font-semibold text-slate-800">Collaboration catalog</h1>
        <p className="mt-1 text-sm text-slate-500">Registered partners available for challenge matching.</p>
      </div>

      <ErrorBanner message={error} />
      {loading && <Spinner label="Loading catalog..." />}

      {!loading && (
        <div className="grid gap-5 lg:grid-cols-2">
          {SECTIONS.map((section) => {
            const items = data[section.key] || []
            return (
              <section key={section.key} className="card" aria-labelledby={`catalog-${section.key}`}>
                <h2 id={`catalog-${section.key}`} className="text-sm font-semibold text-slate-700">
                  {section.title} <span className="text-xs font-normal text-slate-400">({items.length})</span>
                </h2>
                <div className="mt-3 space-y-2">
                  {items.length === 0 ? (
                    <EmptyState title="No entries yet" />
                  ) : (
                    items.map((item, i) => {
                      const view = section.render(item)
                      return (
                        <article key={item.id || i} className="rounded-lg border border-slate-100 p-3">
                          <p className="text-sm font-medium text-slate-700">{view.title}</p>
                          {view.detail && <p className="text-xs text-brand-600">{view.detail}</p>}
                          {view.meta && <p className="mt-1 line-clamp-2 text-xs text-slate-500">{view.meta}</p>}
                        </article>
                      )
                    })
                  )}
                </div>
              </section>
            )
          })}
        </div>
      )}
    </div>
  )
}
